package edu.stanford.spezi.ui

import androidx.compose.material3.Text
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.isEnabled
import androidx.compose.ui.test.isNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import edu.stanford.spezi.testing.ui.onNodeWithIdentifier
import org.junit.Rule
import org.junit.Test

class AsyncButtonTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val root = AsyncButtonTestIdentifier.ROOT

    @Test
    fun testButtonIsDisplayed() {
        val title = "AsyncButton"
        composeTestRule.setContent {
            AsyncButton {
                Text(text = title)
            }
        }

        composeTestRule.onNodeWithIdentifier(root).assertIsDisplayed()
        composeTestRule.onNodeWithText(title).assertIsDisplayed()
    }

    @Test
    fun testButtonIsEnabled() {
        val title = "AsyncButton"
        composeTestRule.setContent {
            AsyncButton(enabled = true, isLoading = false) {
                Text(text = title)
            }
        }

        composeTestRule.onNodeWithIdentifier(root).assert(isEnabled())
    }

    @Test
    fun testLoadingState() {
        val title = "AsyncButton"
        composeTestRule.setContent {
            AsyncButton(enabled = true, isLoading = true) {
                Text(text = title)
            }
        }

        composeTestRule.onNodeWithIdentifier(root).assert(isNotEnabled())
        composeTestRule.onNodeWithIdentifier(AsyncButtonTestIdentifier.LOADING).assertIsDisplayed()
    }

    @Test
    fun testAsyncTestButton() {
        val title = "AsyncButton"
        composeTestRule.setContent {
            AsyncTextButton(text = title)
        }

        composeTestRule.onNodeWithIdentifier(root).assert(isEnabled())
        composeTestRule.onNodeWithIdentifier(AsyncButtonTestIdentifier.LOADING)
            .assertIsNotDisplayed()
        composeTestRule.onNodeWithText(title).assertIsDisplayed()
    }
}
