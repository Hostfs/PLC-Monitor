package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.UserRole
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("PLC Monitor", appName)
    }

    @Test
    fun `verify role permissions matrix`() {
        val op = UserRole.OPERATOR
        assertTrue(op.canAcknowledgeAlarms)
        assertFalse(op.canEditSetpoints)
        assertFalse(op.canControlEStop)
        assertFalse(op.canModifyCommConfig)

        val eng = UserRole.ENGINEER
        assertTrue(eng.canAcknowledgeAlarms)
        assertTrue(eng.canEditSetpoints)
        assertTrue(eng.canControlEStop)
        assertFalse(eng.canModifyCommConfig)

        val admin = UserRole.ADMIN
        assertTrue(admin.canAcknowledgeAlarms)
        assertTrue(admin.canEditSetpoints)
        assertTrue(admin.canControlEStop)
        assertTrue(admin.canModifyCommConfig)
        assertTrue(admin.canManageUsers)
    }
}
