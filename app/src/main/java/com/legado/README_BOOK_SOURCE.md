# 书源管理系统

## 功能概述

书源管理系统为 Legado 阅读器提供自定义书源添加、管理和内容发现功能。该系统支持：

- **书源配置管理**：添加、编辑、删除和启用/禁用书源
- **HTML 内容解析规则引擎**：灵活的 CSS 选择器和正则表达式规则
- **书源搜索和发现功能**：基于书源的书籍搜索和内容发现

## 核心组件

### 1. BookSourceManager (书源管理器)

位于 `app/src/main/java/com/legado/data/source/BookSourceManager.kt`

主要功能：
- 管理所有书源的增删改查操作
- 提供书籍搜索功能（支持多书源）
- 支持书源内容发现和刷新
- 自动处理网络请求和 HTML 解析

```kotlin
// 示例用法
val manager = BookSourceManager(ruleEngine)
manager.addBookSource(bookSource)
val books = manager.searchBooks("query")
```

### 2. RuleEngine (规则引擎)

位于 `app/src/main/java/com/legado/data/source/rules/RuleEngine.kt`

主要功能：
- CSS 选择器解析和元素提取
- 文本内容提取（标题、作者、描述等）
- 属性提取（图片 URL 等）
- 正则表达式替换和清理
- URL 规范化

```kotlin
// 示例规则
val rule = Rule(
    selector = ".book-item",
    attribute = "title"
)
```

### 3. BookSourceScreen (书源管理界面)

位于 `app/src/main/java/com/legado/presentation/screens/source/BookSourceScreen.kt`

主要功能：
- 书源列表展示
- 添加新书源
- 编辑现有书源
- 启用/禁用书源
- 删除书源

## 数据结构

### BookSource (书源)

```kotlin
data class BookSource(
    val id: String,           // 唯一标识符
    val name: String,         // 书源名称
    val baseUrl: String,      // 基础 URL
    val enabled: Boolean,     // 是否启用
    val lastUpdate: Long,     // 最后更新时间
    val description: String?, // 描述信息
    val rules: List<Rule>     // 解析规则
)
```

### Rule (解析规则)

```kotlin
data class Rule(
    val selector: String,     // CSS 选择器
    val attribute: String?,   // 要提取的属性类型
    val regex: String?,       // 正则表达式模式
    val replacement: String?  // 替换文本
)
```

## 解析规则语法

### 基础选择器

```css
.title                    // 类名为 title 的元素
.book-item                // 类名为 book-item 的元素
h1, h2, h3                // 多个选择器
[class*=title]            // 包含 title 的类名
```

### 属性提取

```kotlin
Rule("img", "cover")        // 提取图片 src 属性
Rule(".author")             // 直接提取文本内容
Rule(".category")           // 提取分类信息
```

### 正则表达式替换

```kotlin
Rule(
    selector = ".chapter-title",
    regex = "\\d+\\.\\s*(.*)",
    replacement = "$1"        // 移除章节编号
)
```

## 使用示例

### 添加书源

```kotlin
val bookSource = BookSource(
    id = "novel-site-1",
    name = "小说网站1",
    baseUrl = "https://novel-site.com",
    description = "一个流行的小说网站",
    rules = listOf(
        Rule(selector = ".novel-item", attribute = "title"),
        Rule(selector = ".novel-item", attribute = "author"),
        Rule(selector = ".novel-item img", attribute = "cover")
    )
)

bookSourceManager.addBookSource(bookSource)
```

### 搜索书籍

```kotlin
// 在所有启用的书源中搜索
val results = bookSourceManager.searchBooks("斗破苍穹")

// 在特定书源中搜索
val specificResults = bookSourceManager.searchBooks("斗破苍穹", listOf("novel-site-1"))
```

### 内容发现

```kotlin
// 发现特定书源的热门书籍
val popularBooks = bookSourceManager.discoverBooks("novel-site-1")
```

## 集成说明

### 1. 依赖注入

系统通过 Dagger Hilt 进行依赖注入，确保各组件正确连接。

### 2. UI 集成

要将书源管理界面添加到应用中：

```kotlin
// 在导航配置中添加路由
composable("book_source") {
    BookSourceScreen()
}
```

### 3. API 集成

系统同时支持本地处理和远程 API 调用，可通过配置文件切换模式。

## 错误处理

- 网络请求超时设置为 10 秒
- HTML 解析异常会自动跳过当前书源
- 无效的 CSS 选择器会返回空结果而不是抛出异常

## 性能优化

- 异步操作使用协程处理
- HTML 解析在主线程外执行
- 缓存机制防止重复请求
- 批量处理多个书源的搜索结果

## 扩展性

系统设计考虑了未来扩展：

- 支持自定义解析插件
- 可扩展的书源验证机制
- 支持增量更新和缓存策略
- 可配置的搜索算法