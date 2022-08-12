package com.android.systemui.statusbar.notification.stack

import android.widget.FrameLayout
import androidx.test.filters.SmallTest
import com.android.systemui.R
import com.android.systemui.SysuiTestCase
import com.android.systemui.statusbar.notification.row.ExpandableNotificationRow
import com.android.systemui.statusbar.notification.stack.StackScrollAlgorithm.BypassController
import com.android.systemui.statusbar.notification.stack.StackScrollAlgorithm.SectionProvider
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when` as whenever

@SmallTest
class StackScrollAlgorithmTest : SysuiTestCase() {

    private val hostView = FrameLayout(context)
    private val stackScrollAlgorithm = StackScrollAlgorithm(context, hostView)
    private val expandableViewState = ExpandableViewState()
    private val notificationRow = mock(ExpandableNotificationRow::class.java)
    private val ambientState = AmbientState(
            context,
            SectionProvider { _, _ -> false },
            BypassController { false })

    @Before
    fun setUp() {
        whenever(notificationRow.viewState).thenReturn(expandableViewState)
        hostView.addView(notificationRow)
    }

    @Test
    fun testUpTranslationSetToDefaultValue() {
        whenever(notificationRow.isPinned).thenReturn(true)
        whenever(notificationRow.isHeadsUp).thenReturn(true)

        stackScrollAlgorithm.resetViewStates(ambientState, 0)

        assertThat(expandableViewState.yTranslation).isEqualTo(stackScrollAlgorithm.mHeadsUpInset)
    }

    @Test
    fun testHeadsUpTranslationChangesBasedOnStackMargin() {
        whenever(notificationRow.isPinned).thenReturn(true)
        whenever(notificationRow.isHeadsUp).thenReturn(true)
        val minHeadsUpTranslation = context.resources
                .getDimensionPixelSize(R.dimen.notification_side_paddings)

        // split shade case with top margin introduced by shade's status bar
        ambientState.stackTopMargin = 100
        stackScrollAlgorithm.resetViewStates(ambientState, 0)

        // top margin presence should decrease heads up translation up to minHeadsUpTranslation
        assertThat(expandableViewState.yTranslation).isEqualTo(minHeadsUpTranslation)
    }
}