package io.github.dwthr.digitallogs.logs.domain.error

sealed interface AppError

sealed interface AppResult<out D, out E: AppError> {
    data class Success<out D, out E: AppError>(val data: D): AppResult<D, E>
    data class Error<out D, out E: AppError>(val error: E): AppResult<D, E>
}