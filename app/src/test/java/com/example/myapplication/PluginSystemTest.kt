package com.example.myapplication

import com.example.myapplication.plugin.*
import org.junit.Assert.*
import org.junit.Test
import org.mockito.Mockito.mock
import android.content.Context
import android.view.View

class PluginSystemTest {

    @Test
    fun testSyncHook() {
        val hook = SyncHook<String>("test")
        var called = false
        hook.tap { called = true }
        hook.call("data")
        assertTrue(called)
    }

    @Test
    fun testWaterfallHook() {
        val hook = WaterfallHook<Int>("test")
        hook.tap { it + 1 }
        hook.tap { it * 2 }
        val result = hook.call(5)
        // (5 + 1) * 2 = 12
        assertEquals(12, result)
    }

    @Test
    fun testBailHook() {
        val hook = BailHook<String, String>("test")
        hook.tap { null }
        hook.tap { if (it == "stop") "stopped" else null }
        hook.tap { "last" }

        assertEquals("stopped", hook.call("stop"))
        assertEquals("last", hook.call("continue"))
    }

    @Test
    fun testPluginManagerRegistration() {
        val mockContext = mock(Context::class.java)
        val manager = PluginManager(mockContext)
        
        val plugin = object : IPlugin {
            override val name: String = "TestPlugin"
            override val version: String = "1.0"
            var applied = false
            override fun apply(context: PluginContext) {
                applied = true
            }
        }

        manager.register(plugin)
        manager.init()
        assertTrue(plugin.applied)
    }

    @Test
    fun testDependencyOrder() {
        val mockContext = mock(Context::class.java)
        val manager = PluginManager(mockContext)
        val order = mutableListOf<String>()

        val p1 = object : IPlugin {
            override val name = "p1"
            override val version = "1.0"
            override val dependencies = listOf("p2")
            override fun apply(context: PluginContext) { order.add(name) }
        }
        val p2 = object : IPlugin {
            override val name = "p2"
            override val version = "1.0"
            override fun apply(context: PluginContext) { order.add(name) }
        }

        manager.register(p1)
        manager.register(p2)
        manager.init()

        assertEquals(listOf("p2", "p1"), order)
    }

    @Test
    fun testUiSlotRegistration() {
        val mockContext = mock(Context::class.java)
        val manager = PluginManager(mockContext)
        
        manager.registerUiComponent("test_slot") { ctx -> View(ctx) }
        
        val components = manager.getUiComponents("test_slot", mockContext)
        assertEquals(1, components.size)
    }
}
