package io.github.dwthr.digitallogs.logs.data

import io.github.dwthr.digitallogs.logs.domain.TitleRestrictions
import io.github.dwthr.digitallogs.logs.domain.error.EntityError
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class LogValidatorTest {
	@Test
	fun `validate log title blank, returns blank title error`() {
		val resultError = LogValidator.validateTitle(
			title = ""
		)

		assertThat(resultError).isEqualTo(EntityError.Title.IS_BLANK)
	}

	@Test
	fun `validate log title not blank, returns null`() {
		val resultError = LogValidator.validateTitle(
			title = "sample"
		)

		assertThat(resultError).isNull()
	}

	@Test
	fun `validate log title too long, returns long title error`() {
		val resultError = LogValidator.validateTitle(
			title = StringBuilder("0").repeat(TitleRestrictions.LOG.maxChars + 1)
		)

		assertThat(resultError).isEqualTo(EntityError.Title.TOO_LONG)
	}
}