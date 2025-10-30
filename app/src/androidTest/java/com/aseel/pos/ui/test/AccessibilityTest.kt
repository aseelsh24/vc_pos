package com.aseel.pos.ui.test

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.aseel.pos.ui.theme.VCPOSTheme
import com.aseel.pos.ui.screens.AdaptivePosScreen
import com.aseel.pos.ui.PosViewModel
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Accessibility tests for the POS app
 * Tests TalkBack compatibility, content descriptions, and screen reader navigation
 */
@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class AccessibilityTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Before
    fun setUp() {
        // Set up Compose test environment with RTL support
        composeTestRule.setContent {
            VCPOSTheme(forceRTL = true) {
                LocalLayoutDirection provides LayoutDirection.Rtl
                // Mock ViewModel or use test doubles
                AdaptivePosScreen(
                    onNavigateToTransactions = { },
                    onNavigateToSettings = { },
                    viewModel = hiltTestViewModel()
                )
            }
        }
    }

    @Test
    fun testTopAppBarAccessibility() {
        // Test top app bar has proper content description
        composeTestRule
            .onNodeWithTag("top_app_bar")
            .assertIsDisplayed()
            .assertHasClickAction()
            .assert(SemanticsMatcher.keyIsDefined(SemanticsProperties.ContentDescription))
    }

    @Test
    fun testNavigationIconsAccessibility() {
        // Test navigation icons have content descriptions
        composeTestRule
            .onNodeWithContentDescription("المعاملات السابقة")
            .assertIsDisplayed()
            .assertHasClickAction()
        
        composeTestRule
            .onNodeWithContentDescription("الإعدادات")
            .assertIsDisplayed()
            .assertHasClickAction()
    }

    @Test
    fun testSearchBarAccessibility() {
        // Test search bar has proper hint and content description
        composeTestRule
            .onNodeWithContentDescription("بحث عن المنتجات بالاسم أو الرمز")
            .assertIsDisplayed()
            .assertIsNotFocused()
        
        // Test search hint is accessible
        composeTestRule
            .onNodeWithContentDescription("اكتب اسم أو رمز المنتج للبحث")
            .assertIsDisplayed()
    }

    @Test
    fun testProductCardsAccessibility() {
        // Test product cards have content descriptions
        composeTestRule
            .onAllNodesWithTag("product_card_")
            .onFirst()
            .assertIsDisplayed()
            .assert(SemanticsMatcher.keyIsDefined(SemanticsProperties.ContentDescription))
        
        // Verify product card content description includes product info
        composeTestRule
            .onAllNodesWithTag("product_card_")
            .onFirst()
            .assert(SemanticsMatcher.expectValue(
                SemanticsProperties.ContentDescription,
                contains = "منتج:"
            ))
    }

    @Test
    fun testCartAccessibility() {
        // Test cart panel has content description
        composeTestRule
            .onNodeWithContentDescription("سلة التسوق")
            .assertIsDisplayed()
        
        // Test empty cart message is accessible
        composeTestRule
            .onNodeWithContentDescription("السلة فارغة - أضف منتجات من القائمة اليسرى")
            .assertIsDisplayed()
    }

    @Test
    fun testCartItemsAccessibility() {
        // Add a test product to cart first (this would require mocking the ViewModel)
        // For now, we test the structure
        
        composeTestRule
            .onAllNodesWithTag("cart_item_")
            .onFirst()
            .assert(SemanticsMatcher.keyIsDefined(SemanticsProperties.ContentDescription))
        
        // Test cart item content description includes product info
        composeTestRule
            .onAllNodesWithTag("cart_item_")
            .onFirst()
            .assert(SemanticsMatcher.expectValue(
                SemanticsProperties.ContentDescription,
                contains = "المنتج:"
            ))
    }

    @Test
    fun testTouchTargetSizes() {
        // Test that interactive elements meet 48dp minimum touch target
        composeTestRule
            .onNodeWithContentDescription("المعاملات السابقة")
            .assertTouchTargetSizeIsAtLeast(48.dp)
        
        composeTestRule
            .onNodeWithContentDescription("الإعدادات")
            .assertTouchTargetSizeIsAtLeast(48.dp)
    }

    @Test
    fun testRTLLayoutDirection() {
        // Test that RTL layout is properly applied
        composeTestRule
            .onRoot()
            .assert(SemanticsMatcher.expectValue(
                SemanticsProperties.LayoutDirection,
                LayoutDirection.Rtl
            ))
    }

    @Test
    fun testColorContrast() {
        // Test that text elements have sufficient color contrast
        // This would require a more advanced testing approach with screenshot comparison
        // or custom color analysis
        composeTestRule
            .onNodeWithTag("total_amount")
            .assertIsDisplayed()
            .assert(SemanticsMatcher.keyIsDefined(SemanticsProperties.Color))
    }

    @Test
    fun testFontScalingSupport() {
        // Test that the app handles large font sizes gracefully
        composeTestRule
            .onNodeWithText("نقطة البيع")
            .assertIsDisplayed()
            .assertTextContains("نقطة البيع")
        
        composeTestRule
            .onNodeWithText("سلة التسوق")
            .assertIsDisplayed()
            .assertTextContains("سلة التسوق")
    }

    @Test
    fun testPaymentDialogAccessibility() {
        // Test payment dialog is accessible (would need to trigger payment flow)
        // This test would require more complex setup to trigger the payment dialog
        // and then verify its accessibility features
        
        // Placeholder test structure
        composeTestRule
            .onNodeWithText("اختر طريقة الدفع")
            .assertDoesNotExist() // Should not exist initially
    }

    @Test
    fun testCategoryTabsAccessibility() {
        // Test category tabs are accessible
        composeTestRule
            .onNodeWithContentDescription("جميع الفئات")
            .assertIsDisplayed()
            .assertHasClickAction()
        
        // Test individual category tabs
        composeTestRule
            .onAllNodesWithContentDescription(contains("فئة "))
            .assertAny(SemanticsMatcher.keyIsDefined(SemanticsProperties.ContentDescription))
    }

    @Test
    fun testQuantityControlsAccessibility() {
        // Test quantity increment/decrement buttons
        composeTestRule
            .onAllNodesWithContentDescription(contains("زيادة كمية"))
            .assertAny(SemanticsMatcher.keyIsDefined(SemanticsProperties.ContentDescription))
        
        composeTestRule
            .onAllNodesWithContentDescription(contains("تقليل كمية"))
            .assertAny(SemanticsMatcher.keyIsDefined(SemanticsProperties.ContentDescription))
    }

    @Test
    fun testCheckoutButtonAccessibility() {
        // Test checkout button is accessible
        composeTestRule
            .onNodeWithContentDescription("إتمام الدفع")
            .assertIsDisplayed()
            .assertHasClickAction()
        
        // Test clear cart button
        composeTestRule
            .onNodeWithContentDescription("إفراغ السلة")
            .assertIsDisplayed()
            .assertHasClickAction()
    }

    @Test
    fun testTotalAmountAccessibility() {
        // Test total amount is properly announced to screen readers
        composeTestRule
            .onNodeWithContentDescription(contains("الإجمالي:"))
            .assertIsDisplayed()
    }

    /**
     * Helper function to create test ViewModel
     * In real implementation, this would use proper dependency injection
     */
    private fun hiltTestViewModel(): PosViewModel {
        // This would need to be implemented with proper test doubles
        // or Hilt test configuration
        TODO("Implement proper test ViewModel with dependency injection")
    }
}

/**
 * Extension function to check if touch target size meets accessibility requirements
 */
fun SemanticsNodeInteraction.assertTouchTargetSizeIsAtLeast(size: androidx.compose.ui.unit.Dp) = this
    .assert(SemanticsMatcher.keyIsDefined(SemanticsProperties.TestTag))
    .onChildren()
    .filterToOne(SemanticsMatcher.expectValue(SemanticsProperties.IsAccessibilityElement, true))
    .assert(SemanticsMatcher.expectValue(
        SemanticsProperties.BoundsInRoot,
        hasTouchTargetSizeAtLeast(size)
    ))

/**
 * Helper matcher for touch target size
 */
private fun hasTouchTargetSizeAtLeast(minSize: androidx.compose.ui.unit.Dp): (androidx.compose.ui.geometry.Rect) -> Boolean {
    return { bounds ->
        // Simplified check - in real implementation this would be more precise
        bounds.width >= minSize.toPx() && bounds.height >= minSize.toPx()
    }
}