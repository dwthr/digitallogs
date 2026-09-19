package io.github.dwthr.digitallogs.logs.data

import com.google.common.truth.Truth.assertThat
import io.github.dwthr.digitallogs.logs.domain.TitleRestrictions
import io.github.dwthr.digitallogs.logs.domain.error.EntityError
import org.junit.Test

class TagValidatorTest {
	@Test
	fun `validate tag title blank, returns blank title error`() {
		val resultError = TagValidator.validateTitle(
			title = ""
		)

		assertThat(resultError).isEqualTo(EntityError.Title.IS_BLANK)
	}

	@Test
	fun `validate tag title not blank, returns null`() {
		val resultError = TagValidator.validateTitle(
			title = "sample"
		)

		assertThat(resultError).isNull()
	}

	@Test
	fun `validate tag title too long, returns long title error`() {
		val resultError = TagValidator.validateTitle(
			title = StringBuilder("0").repeat(TitleRestrictions.TAG.maxChars + 1)
		)

		assertThat(resultError).isEqualTo(EntityError.Title.TOO_LONG)
	}
}