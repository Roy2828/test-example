package com.example.myapplication.xunfei.feiqi

import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

class OrderedPlayer<T>(
    private val resources: List<T>,
    private val play: (T) -> Boolean  // 返回true表示成功，false表示失败
) {
    private val successList = mutableListOf<T>()
    private val failed = AtomicBoolean(false)

    fun playAll(maxWorkers: Int = 8) {
        val executor = Executors.newFixedThreadPool(maxWorkers)
        val lock = Object()
        var nextIndex = 0

        for ((index, resource) in resources.withIndex()) {
            executor.submit {
                // 提前检查是否失败
                if (failed.get()) return@submit

                // 等待轮到自己
                synchronized(lock) {
                    while (index != nextIndex && !failed.get()) {
                        try {
                            lock.wait()
                        } catch (e: InterruptedException) {
                            Thread.currentThread().interrupt()
                        }
                    }
                    if (failed.get()) {
                        lock.notifyAll()
                        return@synchronized
                    }
                    // 开始处理
                    val ok = try {
                        play(resource)
                    } catch (e: Exception) {
                        false
                    }
                    if (ok) {
                        println("成功播放: $resource")
                        successList.add(resource)
                        nextIndex++
                        lock.notifyAll()
                    } else {
                        println("播放失败: $resource")
                        failed.set(true)
                        lock.notifyAll()
                    }
                }
            }
        }
        executor.shutdown()
        while (!executor.isTerminated) {
            Thread.sleep(10)
        }
        println("成功播放数量: ${successList.size}")
    }
}

// 示例用法
fun main() {
    val resources = (1..15000).map { "资源$it" }
    val player = OrderedPlayer(resources) { res ->
        // 模拟播放逻辑，1%概率失败
        if ((1..100).random() == 1) throw RuntimeException("模拟失败: $res")
        Thread.sleep((10..50).random().toLong())
        true
    }
    player.playAll(maxWorkers = 16)
}