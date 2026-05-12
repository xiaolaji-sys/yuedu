# 广告净化功能

为 Legado 阅读器提供的智能广告检测和过滤功能。

## 功能特性

### AdFilter.kt
- **智能广告检测**：基于关键词、正则表达式和HTML标签的模式识别
- **HTML内容清理**：移除iframe、script、广告div等HTML元素
- **纯文本提取**：从HTML中提取干净的文本内容
- **URL广告检测**：识别和过滤广告链接

### ContentCleaner.kt
- **噪音清理**：移除重复空行、多余空白字符、孤立的标点符号
- **段落优化**：智能优化段落格式，保持阅读流畅性
- **去重处理**：使用Levenshtein距离算法去除相似内容
- **章节格式化**：标准化章节标题格式
- **智能截断**：保持句子完整性进行文本截断

### AdPurificationManager.kt
- **统一管理接口**：提供统一的广告净化管理类
- **批量处理**：支持多章节内容的批量净化
- **错误处理**：完善的异常处理和降级机制

## 使用方法

### 基础用法

```kotlin
import com.legado.util.*

// 清理单个章节内容
val rawContent = "<div class=\"ad\">广告内容</div>这是正常内容"
val purifiedContent = ContentCleaner.cleanText(rawContent)
val optimizedContent = ContentCleaner.optimizeParagraphs(purifiedContent)

// 检查是否包含广告
if (AdFilter.containsAd(rawContent)) {
    println("检测到广告内容")
}
```

### 高级用法 - 使用管理器

```kotlin
import com.legado.util.AdPurificationManager

class ChapterProcessor(private val context: Context) {
    private val purificationManager = AdPurificationManager(context)

    suspend fun processChapter(rawContent: String): String {
        return purificationManager.purifyChapterContent(rawContent)
    }

    fun filterAdvertisementUrl(url: String?): String? {
        return purificationManager.purifyUrl(url)
    }
}
```

### 在数据获取层集成

```kotlin
class ChapterRepository @Inject constructor(
    private val apiService: ApiService,
    private val context: Context
) {
    private val purificationManager = AdPurificationManager(context)

    suspend fun getChapterContent(chapterId: String): Chapter {
        val response = apiService.getChapter(chapterId)
        val purifiedContent = purificationManager.purifyChapterContent(response.content)
        return response.copy(content = purifiedContent)
    }
}
```

## 配置选项

### 自定义广告关键词
在 `AdFilter.kt` 中可以修改 `adKeywords` 列表来添加或删除广告关键词。

### 调整净化参数
在 `ContentCleaner.kt` 中可以通过以下参数调整净化行为：
- `removeDuplicates()` 的 `maxSimilarity` 参数（默认0.8）
- `smartTruncate()` 的 `maxLength` 参数（默认1000）

## 性能考虑

1. **缓存策略**：对于频繁访问的内容，建议缓存净化后的结果
2. **异步处理**：大段落的净化操作建议在后台线程执行
3. **内存管理**：注意大文本内容的内存占用，必要时分块处理

## 测试示例

```kotlin
fun main() {
    val dirtyHtml = """
        <div class="ad-banner">这是一个广告</div>
        <p>这是正常的章节内容。</p>
        <script>alert('点击这里赚钱！');</script>
        <div id="promo">限时优惠，立即注册！</div>
        <p>这里是更多正常的内容。</p>
    """.trimIndent()

    val cleaned = ContentCleaner.cleanText(dirtyHtml)
    val optimized = ContentCleaner.optimizeParagraphs(cleaned)

    println(optimized)
}
```

输出结果将是一个干净、优化的章节内容，所有广告相关元素都被成功移除。

## 注意事项

1. 本过滤器主要针对中文内容优化，英文内容可能需要调整关键词
2. 复杂的JavaScript动态广告可能需要更高级的检测机制
3. 建议在真实环境中进行测试和调整，根据实际用户反馈优化规则