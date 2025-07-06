import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.atomic.AtomicInteger

// 原始资源数据结构，只有一个id
data class RawResource(val id: Int)
// 转换后资源数据结构，包含id和内容
data class ConvertedResource(val id: Int, val content: String)

// 封装资源的包装类，包含顺序索引、原始资源和异步转换结果
data class ResourceWrapper(
    val index: Int,  // 添加时的顺序索引
    val raw: RawResource,// 原始资源
    val deferred: CompletableDeferred<ConvertedResource>  // 转换任务的异步结果
)

class PlayerQueue {
    // 协程作用域，所有任务都挂在这个scope下，方便统一取消
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    // 存储所有待处理资源的列表，严格顺序
    private val resourceList = mutableListOf<ResourceWrapper>()
    // 保护resourceList的互斥锁，保证多协程安全
    private val listMutex = Mutex()
    // 生成全局自增顺序id
    private val indexGenerator = AtomicInteger(0)

    // 控制标志
    @Volatile private var hasError = false     // 全局错误标志
    @Volatile private var isStopped = false       // 主动停止标志

    init {
        startPlayer()   // 初始化时自动启动播放协程
    }

    // 动态添加一个原始资源
    fun addResource(raw: RawResource) {
        if (hasError || isStopped) return  // 已出错或已停止则不再添加资源

        val wrapper = ResourceWrapper(
            index = indexGenerator.getAndIncrement(),
            raw = raw,
            deferred = CompletableDeferred()
        )
        // 启动协程添加资源并并发转换
        scope.launch {
            // 先顺序加入资源队列
            listMutex.withLock {
                resourceList.add(wrapper)
            }

            // 启动子协程进行耗时转换，完成后填充deferred
            // 启动转换任务
            launch {
                try {
                    println("🔄 开始转换资源 ID: ${raw.id}")
                    val converted = convertResource(wrapper.raw)
                    wrapper.deferred.complete(converted)
                } catch (e: Exception) {
                    wrapper.deferred.completeExceptionally(e)
                    handleConversionError(e)
                }
            }
        }
    }

    // 转换资源的挂起函数，模拟耗时和失败
    private suspend fun convertResource(raw: RawResource): ConvertedResource {
        delay((500..1500).random().toLong()) // 模拟耗时转换

        // 模拟转换失败条件（例如 id == 5）
        if (raw.id == 105) {   // 模拟失败（id==105时失败）
            throw RuntimeException("❌ 转换失败，资源 ID: ${raw.id}")
        }
        // 转换成功
        return ConvertedResource(raw.id, "Converted-Content-${raw.id}")
    }
    // 启动顺序播放逻辑的主协程
    private fun startPlayer() {
        scope.launch {
            var currentIndex = 0 // 当前应播放的资源索引
            while (isActive && !hasError && !isStopped) {
                // 拿到当前要处理的资源包装
                val wrapper: ResourceWrapper? = listMutex.withLock {
                    if (currentIndex < resourceList.size) {
                        resourceList[currentIndex]
                    } else null
                }

                if (wrapper != null) {
                    try {
                        // 等待转换完成
                        val resource = wrapper.deferred.await()
                        // 播放
                        play(resource)
                        currentIndex++
                    } catch (e: Exception) {
                        // 转换或播放失败，触发全局错误
                        handleConversionError(e)
                        break
                    }
                } else {
                    // 还没有新的资源可播，等待
                    delay(200)
                }
            }
        }
    }
    // 实际播放逻辑（这里只打印）
    private suspend  fun play(resource: ConvertedResource) {
        println("🎵 Playing: [ID=${resource.id}] Content=${resource.content}")
        delay(1000) // 模拟播放耗时1秒，挂起期间不会执行下一个资源
    }

    // 错误处理，确保只执行一次，清空队列并取消所有任务
    private fun handleConversionError(e: Exception) {
        if (!hasError && !isStopped) {
            hasError = true
            println("🔥 转换失败：${e.message}，停止播放并清空队列")
            clearAndShutdown()
        }
    }
    // 清空资源队列并停止所有协程
    private fun clearAndShutdown() {
        scope.launch {
            listMutex.withLock {
                resourceList.clear()
            }
            scope.cancel() // 取消所有协程
        }
    }
    // 主动停止队列和协程
    fun stop() {
        if (!isStopped) {
            println("🛑 Stop 被调用，清空队列并终止播放")
            isStopped = true
            clearAndShutdown()
        }
    }

}

fun main() = runBlocking {
    val queue = PlayerQueue()

    // 添加多个资源，有一个会失败触发错误逻辑
    for (i in 1..1000) {
        queue.addResource(RawResource(i))
        delay(300)
    }
    // 也可以通过 stop 主动终止
    delay(500000)
    queue.stop()

    delay(2000) // 等待清理完成
}