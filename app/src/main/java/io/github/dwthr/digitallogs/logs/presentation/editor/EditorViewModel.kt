package io.github.dwthr.digitallogs.logs.presentation.editor

import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import io.github.dwthr.digitallogs.audio.AppAudioHandler
import io.github.dwthr.digitallogs.audio.data.AudioException
import io.github.dwthr.digitallogs.audio.presentation.getExceptionDisplayText
import io.github.dwthr.digitallogs.common.domain.combine
import io.github.dwthr.digitallogs.common.stateInWhileSubscribed
import io.github.dwthr.digitallogs.logs.domain.EntryData
import io.github.dwthr.digitallogs.logs.domain.EntryData.Text
import io.github.dwthr.digitallogs.logs.domain.repository.LocalRepository
import io.github.dwthr.digitallogs.logs.presentation.editor.AudioState.AudioPlaying
import io.github.dwthr.digitallogs.logs.presentation.editor.LogEditorLoadState.IsLoading
import io.github.dwthr.digitallogs.logs.presentation.editor.LogEditorLoadState.Loaded
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlin.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class EditorViewModel(
	private val repository: LocalRepository,
	private val noteId: Long,
	val audioPlayer: Player, //TODO: Bundle into player class
	private val audioHandler: AppAudioHandler,
) : ViewModel() {

	private val _loadingRefId = MutableStateFlow<String?>(null)
	private val loadingRefId = _loadingRefId
		.stateInWhileSubscribed(null)

	init {
		audioPlayer.prepare()
	}

	@SuppressLint("EmptySuperCall")
	override fun onCleared() { //TODO: Don't override Activity lifecycle methods, such as onResume, to run UI-related tasks. Instead, use Compose's LifecycleEffects or lifecycle-aware coroutine scopes: https://developer.android.com/topic/architecture/recommendations?hl=en
		audioPlayer.release()
		super.onCleared()
	}

	private val _logAndCategory = repository.getLogWithCategory(noteId)
		.catch { _state.update { it.copy(loadState = LogEditorLoadState.IsError) } }
		.stateInWhileSubscribed(null)

	private val _amplitudeData = MutableStateFlow(emptyList<Float>())
	private val amplitudeData = _amplitudeData
		.stateInWhileSubscribed(emptyList())

	private val _recordingStartInstant = MutableStateFlow<Instant?>(null)
	private val recordingAtTimestamp = _recordingStartInstant
		.stateInWhileSubscribed(null)

	private val _audioState = MutableStateFlow<AudioState?>(null)
	private val audioState: StateFlow<AudioState?> = _audioState.onEach { audioState ->
		when(audioState) {
			AudioState.MicRecording -> {
				_amplitudeData.update { emptyList() }
				try {
					audioHandler.recordAudioAsRef(
						amplitudeFlow = { amplitudeFlow ->
							viewModelScope.launch {
								amplitudeFlow.collect { amplitude ->
									_amplitudeData.update {
										(it + amplitude).takeLast(state.value.recordAmplitudeCount)
									}
								}
							}
						}
					)
					_recordingStartInstant.update { Clock.System.now() }
				} catch (e: Exception) {
					if (e !is AudioException.Record) throw e
					println(e.getExceptionDisplayText()) //TODO: Have user-visible error
				}
			}
			null -> {
				_recordingStartInstant.update { null }
			}
			else -> Unit
		}
	}.stateInWhileSubscribed(null)

	private val _state = MutableStateFlow(LogEditorState())
	internal val state = combine(_state, _logAndCategory, audioState, loadingRefId, amplitudeData, recordingAtTimestamp) { state, logAndCategory, audioState, loadingRefId, amplitudeData, recordingAtTimestamp ->
		if (state.loadState is LogEditorLoadState.IsError) return@combine state
		state.copy(
			loadState = if (logAndCategory == null) {
				IsLoading
			} else  {
				var newLoadState = state.loadState

				when(state.loadState) {
					is Loaded -> Unit
					else -> {
						newLoadState = Loaded(
							logTitle = logAndCategory.log.title,
							logEntries = logAndCategory.log.entries
						)
					}
				}

				newLoadState.copy(
					logTitle = logAndCategory.log.title,
					logEntries = logAndCategory.log.entries,
					audioState = audioState,
					loadingRefId = loadingRefId,
					recordAmplitude = amplitudeData,
					recordMediaTimestamp = recordingAtTimestamp
				)
			}
		)
	}.stateInWhileSubscribed(
		LogEditorState()
	)

    fun onEvent(event: EditorEvent) {
		(state.value.loadState as? Loaded)?.let { loadedState ->
			val log = _logAndCategory.value?.log
			if (log == null) {
				Log.v(this::class.simpleName, "onEvent called from unloaded state")
				return
			}

			when(event) {
				EditorEvent.OnBack -> {
					viewModelScope.launch {
						repository.updateLog(log)
					}
				}

				EditorEvent.OnTextEntryAdd -> {
					loadedState.logEntryTextValue.ifBlank { return }

					viewModelScope.launch {
						repository.updateLog(
							log = log.addTimestampedLog(
								Text(loadedState.logEntryTextValue)
							)
						)
					}
					_state.update {
						it.copy(
							loadState = loadedState.copy(
								logEntryTextValue = ""
							)
						)
					}
				}

				is EditorEvent.OnLogTextFieldChanged -> {
					_state.update {
						it.copy(
							loadState = loadedState.copy(
								logEntryTextValue = event.text
							)
						)
					}
				}

				is EditorEvent.OnMicRecordToggle -> { //TODO: play sound or vibrate
					Log.i(this::class.simpleName, "Mic record toggled")
					Log.d(this::class.simpleName, "Audio state: ${_audioState.value}\nState: ${loadedState.toString().take(200)}")
					if (_audioState.value == AudioState.MicRecording) stopRecordingSaveRef()
				}

				is EditorEvent.OnAudioEntryPressed -> {
					_loadingRefId.value?.let { loadingRefId ->
						audioPlayer.clearMediaItems()

						val updatedLoadRefId = event.audioRefUuid.takeIf { it != loadingRefId }
						_loadingRefId.update { updatedLoadRefId }

						if (updatedLoadRefId == null) return
					}


					viewModelScope.launch {
						_loadingRefId.update { event.audioRefUuid }

						_loadingRefId.collectLatest { audioRefUuid ->
							if (audioRefUuid == null) return@collectLatest

							audioHandler.getAudioUriFromRefUuid("$audioRefUuid/audioRecording.ogg")?.let { audioUri -> //TODO: Implement properly. Use proper data class for item (uri + refId)?
								_audioState.update { AudioPlaying(audioRefUuid) }
								audioPlayer.setMediaItem(MediaItem.fromUri(audioUri))
								audioPlayer.play()
							} ?: Log.e(this::class.simpleName, "Unable to find audio with given uuid")

							_loadingRefId.update { null }
						}
					}
				}

				EditorEvent.OnMicRecordCancel -> {
					viewModelScope.launch {
						audioHandler.cancelRecording()
						_audioState.update { null }
						_recordingStartInstant.update { null }
					}
				}

				is EditorEvent.OnEntrySelectionToggle -> {
					_state.update { it.copy(
						loadState = loadedState.copy(
							selectedEntries = loadedState.selectedEntries.let { selectedEntries ->
								if (event.entry in selectedEntries) {
									selectedEntries.minus(event.entry)
								} else {
									selectedEntries + event.entry
								}
							}
						)
					) }
				}

				is EditorEvent.OnEntryExpandedChanged -> {
					_state.update { it.copy(
						loadState = loadedState.copy(
							expandedEntry = event.entry
						)
					) }
				}

				is EditorEvent.OnSelectedEntriesDelete -> {
					_state.update {
						it.copy(
							visibleDialogType = DialogType.SelectDeleteConfirmation
						)
					}
				}

				EditorEvent.OnEntrySelectionClear -> clearSelection(loadedState)

				EditorEvent.OnMicRecordPermissionRejected -> {
					_state.update { it.copy(visibleDialogType = DialogType.MicPermissionRejected) }
				}

				EditorEvent.OnDialogConfirm -> {
					when(state.value.visibleDialogType) {
						DialogType.SelectDeleteConfirmation -> {
							viewModelScope.launch {
								_logAndCategory.value?.log?.let { log ->
									repository.deleteEntriesFromLog(
										logId = log.id,
										logEntry = loadedState.selectedEntries.toTypedArray(),
										onLastMediaRefDelete = { mediaRefs ->
											mediaRefs.forEach {
												audioHandler.deleteRecording(it)
											}
										}
									)
								} ?: Log.w(this::class.simpleName, "Log entry delete called with undefined log")

								clearSelection(loadedState)
							}
						}
						else -> Unit
					}
					_state.update { it.copy(visibleDialogType = null) }
				}

				EditorEvent.OnMicRecordPermissionGranted -> {
					_audioState.update { AudioState.MicRecording }
				}

				EditorEvent.OnMicRecordPermissionDisabled -> {
					_state.update { it.copy(
						visibleDialogType = DialogType.MicPermissionDisabled
					) }
				}

				EditorEvent.OnNavigatePermissions -> Unit
				EditorEvent.OnShareSelectedEntries -> clearSelection(loadedState)
				EditorEvent.OnDialogDismiss -> _state.update { it.copy(visibleDialogType = null) }
			}
		} ?: {
			audioPlayer.clearMediaItems()
			Log.i(this::class.simpleName, "Event called from unloaded state")
		}
    }

	private fun clearSelection(loadedState: Loaded) {
		_state.update { it.copy(
			loadState = loadedState.copy(selectedEntries = emptySet())
		) }
	}

	private fun stopRecordingSaveRef() {
		viewModelScope.launch {
			val log = _logAndCategory.value?.log ?: return@launch

			audioHandler.stopRecordingAndSave()?.let { mediaRefId ->
				repository.updateLog(
					log = _recordingStartInstant.value?.let { timestamp ->
						log.addTimestampedLog(
							newLog = EntryData.Media(
								fileReferenceUuid = mediaRefId.toString()
							),
							atTimestamp = timestamp
						)
					} ?: log.addTimestampedLog(
						newLog = EntryData.Media(
							fileReferenceUuid = mediaRefId.toString()
						),
					).also {
						Log.w(
							this::class.simpleName,
							"Recording timestamp null; Using current time"
						)
					}
				)
			} ?: Log.w(
				this::class.simpleName,
				"No media reference returned on stopRecording"
			)

			_audioState.update { null }
		}
	}
}