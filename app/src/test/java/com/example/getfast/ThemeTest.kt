package com.example.getfast

import android.content.Context
import android.util.TypedValue
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ThemeTest {
    @Test
    fun themeIsResolvable() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        context.setTheme(R.style.Theme_GetFast)
        val typedValue = TypedValue()
        val resolved = context.theme.resolveAttribute(android.R.attr.colorBackground, typedValue, true)
        assertTrue("Theme should resolve colorBackground attribute", resolved)
    }
}
