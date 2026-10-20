package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.ActionType
import com.example.system.JarvisBrain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
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
        assertEquals("Jarvis AI", appName)
    }

    @Test
    fun `jarvis brain parses call intent`() {
        val brain = JarvisBrain()
        val response = brain.processCommand("Call Rahul")
        assertNotNull(response.action)
        assertEquals(ActionType.CALL_PHONE, response.action?.type)
        assertEquals("Rahul", response.action?.target)
    }

    @Test
    fun `jarvis brain parses app intent`() {
        val brain = JarvisBrain()
        val response = brain.processCommand("Open YouTube")
        assertNotNull(response.action)
        assertEquals(ActionType.OPEN_APP, response.action?.type)
        assertEquals("YouTube", response.action?.target)
    }

    @Test
    fun `jarvis brain parses reminder intent`() {
        val brain = JarvisBrain()
        val response = brain.processCommand("Remind me to drink water at 4 PM")
        assertNotNull(response.action)
        assertEquals(ActionType.SET_REMINDER, response.action?.type)
    }
}
