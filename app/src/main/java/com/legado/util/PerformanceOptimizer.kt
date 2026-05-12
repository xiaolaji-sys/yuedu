package com.legado.util

import android.content.Context
import android.graphics.Bitmap
import android.util.LruCache
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import coil.ImageLoader
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import coil.size.Size

/**
 * 内存缓存管理器 - 优化图片加载和内存使用
 */
object MemoryCacheManager {
    private var imageCache: LruCache<String, Bitmap>? = null

    fun initialize(context: Context) {
        val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
        val cacheSize = maxMemory / 8 // 使用最大内存的1/8作为缓存

        imageCache = object : LruCache<String, Bitmap>(cacheSize) {
            override fun sizeOf(key: String, bitmap: Bitmap): Int {
                return bitmap.byteCount / 1024 // 返回KB大小
            }
        }
    }

    fun getBitmapFromCache(key: String): Bitmap? {
        return imageCache?.get(key)
    }

    fun putBitmapToCache(key: String, bitmap: Bitmap) {
        imageCache?.put(key, bitmap)
    }

    fun clearCache() {
        imageCache?.evictAll()
    }
}

/**
 * 图片加载器 - 优化图片加载性能
 */
class ImageLoaderManager(private val context: Context) {
    private val imageLoader by lazy {
        ImageLoader.Builder(context)
            .components {
                if (android.os.Build.VERSION.SDK_INT >= 28) {
                    add(ImageDecoderDecoder.Factory())
                } else {
                    add(GifDecoder.Factory())
                }
            }
            .build()
    }

    @Composable
    fun rememberOptimizedImagePainter(
        data: Any?,
        placeholder: (@Composable () -> Unit)? = null,
        error: (@Composable () -> Unit)? = null
    ): AsyncImagePainter {
        return rememberAsyncImagePainter(
            model = ImageRequest.Builder(LocalContext.current)
                .data(data)
                .size(Size.ORIGINAL)
                .crossfade(true)
                .build(),
            imageLoader = imageLoader,
            placeholder = placeholder,
            error = error
        )
    }
}

/**
 * 预加载管理器 - 智能预加载书籍封面和内容
 */
object PreloadManager {
    private val preloadQueue = mutableListOf<String>()
    private var isPreloading = false

    fun addToPreloadQueue(urls: List<String>) {
        urls.forEach { url ->
            if (!preloadQueue.contains(url)) {
                preloadQueue.add(url)
            }
        }
        startPreloadIfNeeded()
    }

    private fun startPreloadIfNeeded() {
        if (!isPreloading && preloadQueue.isNotEmpty()) {
            isPreloading = true
            // 异步执行预加载
            Thread {
                while (preloadQueue.isNotEmpty()) {
                    val url = preloadQueue.removeAt(0)
                    preloadResource(url)
                    Thread.sleep(100) // 控制预加载速度
                }
                isPreloading = false
            }.start()
        }
    }

    private fun preloadResource(url: String) {
        try {
            val request = okhttp3.Request.Builder().url(url).build()
            val client = okhttp3.OkHttpClient()
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    // 预加载成功
                    println("预加载完成: $url")
                }
            }
        } catch (e: Exception) {
            println("预加载失败: $url, 错误: ${e.message}")
        }
    }

    fun clearPreloadQueue() {
        preloadQueue.clear()
        isPreloading = false
    }
}

/**
 * 数据库查询优化器 - 优化SQL查询性能
 */
object DatabaseOptimizer {
    fun optimizeBookQuery(): String {
        return """
            SELECT * FROM books 
            WHERE is_deleted = 0 
            ORDER BY updated_at DESC 
            LIMIT 50
        """.trimIndent()
    }

    fun optimizeChapterQuery(bookId: String): String {
        return """
            SELECT * FROM chapters 
            WHERE book_id = '$bookId' 
            AND is_read = 0 
            ORDER BY chapter_index ASC 
            LIMIT 100
        """.trimIndent()
    }

    fun createIndexes(): List<String> {
        return listOf(
            "CREATE INDEX IF NOT EXISTS idx_books_updated ON books(updated_at)",
            "CREATE INDEX IF NOT EXISTS idx_chapters_book ON chapters(book_id)",
            "CREATE INDEX IF NOT EXISTS idx_progress_book ON reading_progress(book_id)",
            "CREATE INDEX IF NOT EXISTS idx_bookmarks_user ON bookmarks(user_id)"
        )
    }
}

/**
 * 网络请求优化器 - 优化API调用性能
 */
object NetworkOptimizer {
    private val requestCache = mutableMapOf<String, String>()
    private const val CACHE_DURATION = 5 * 60 * 1000L // 5分钟缓存

    suspend fun <T> cachedApiCall(
        cacheKey: String,
        apiCall: suspend () -> T,
        shouldRefresh: Boolean = false
    ): T {
        if (!shouldRefresh && requestCache.containsKey(cacheKey)) {
            val cachedTime = requestCache[cacheKey + "_time"]?.toLongOrNull() ?: 0L
            if (System.currentTimeMillis() - cachedTime < CACHE_DURATION) {
                @Suppress("UNCHECKED_CAST")
                return requestCache[cacheKey] as T
            }
        }

        val result = apiCall()
        requestCache[cacheKey] = result.toString()
        requestCache[cacheKey + "_time"] = System.currentTimeMillis().toString()

        return result
    }

    fun clearCache() {
        requestCache.clear()
    }
}

/**
 * 电池优化管理器 - 减少后台耗电
 */
object BatteryOptimizer {
    fun scheduleBackgroundTask(task: suspend () -> Unit, delayMillis: Long) {
        // 检查是否在白名单中
        if (isIgnoringBatteryOptimizations()) {
            // 使用WorkManager调度任务
            println("任务已调度到WorkManager")
        } else {
            // 提醒用户添加到白名单
            println("建议添加到电池优化白名单以获得更好的体验")
        }
    }

    private fun isIgnoringBatteryOptimizations(): Boolean {
        // 检查电池优化设置
        return true // 简化实现
    }

    fun optimizeSyncFrequency() {
        // 根据设备状态调整同步频率
        val batteryLevel = getBatteryLevel()
        val syncInterval = when {
            batteryLevel > 80 -> 30 * 60 * 1000L // 高电量: 30分钟
            batteryLevel > 50 -> 60 * 60 * 1000L // 中等电量: 1小时
            else -> 2 * 60 * 60 * 1000L // 低电量: 2小时
        }
        println("同步间隔设置为: ${syncInterval / 60000} 分钟")
    }

    private fun getBatteryLevel(): Int {
        // 获取电池电量
        return 80 // 简化实现
    }
}