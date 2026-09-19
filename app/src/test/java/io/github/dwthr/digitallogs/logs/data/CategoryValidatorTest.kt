package io.github.dwthr.digitallogs.logs.data

import io.github.dwthr.digitallogs.logs.domain.TitleRestrictions
import io.github.dwthr.digitallogs.logs.domain.error.EntityError
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class CategoryValidatorTest {
	@Test
	fun `validate category title blank, returns blank title error`() {
		val resultError = CategoryValidator.validateTitle(
			title = ""
		)

		assertThat(resultError).isEqualTo(EntityError.Title.IS_BLANK)
	}

	@Test
	fun `validate category title not blank, returns null`() {
		val resultError = CategoryValidator.validateTitle(
			title = "sample"
		)

		assertThat(resultError).isNull()
	}

	@Test
	fun `validate category title too long, returns long title error`() {
		val resultError = CategoryValidator.validateTitle(
			title = StringBuilder("0").repeat(TitleRestrictions.CATEGORY.maxChars + 1)
		)

		assertThat(resultError).isEqualTo(EntityError.Title.TOO_LONG)
	}
}