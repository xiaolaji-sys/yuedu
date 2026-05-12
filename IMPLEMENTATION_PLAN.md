# Legado - 开源 Android 阅读应用实现计划

> **为 Hermes Agent:** 使用 subagent-driven-development 技能按任务逐步实现此计划。

**目标:** 创建一个功能完整的开源 Android 阅读器，专为追求极致阅读体验的用户设计。

**架构:** 采用 Clean Architecture + Jetpack Compose，确保代码清晰、可维护性强。Material You 设计规范，提供现代化用户体验。

**技术栈:**
- Kotlin + Jetpack Compose (UI)
- Hilt (依赖注入)  
- Room + SQLite (本地存储)
- Retrofit + OkHttp (网络请求)
- WorkManager (后台任务)
- ExoPlayer (音频播放)
- Coil (图片加载)

---

## Phase 1: 基础架构搭建 (2周)

### Task 1: 项目初始化与依赖配置

**目标:** 建立标准 Android 项目结构，配置所有必要的依赖项

**文件:**
- Create: `app/build.gradle.kts`
- Create: `gradle/libs.versions.toml` (版本管理)
- Create: `settings.gradle.kts`

**Step 1: 编写版本管理配置**
```toml
[versions]
kotlin = "1.9.0"
agp = "8.2.0"
compose-bom = "2023.10.01"

[libraries]
androidx-core-ktx = { group = "androidx.core", name = "core-ktx", version.ref = "core" }
androidx-compose-bom = { group = "androidx.compose", name = "compose-bom", version.ref = "compose-bom" }
androidx-lifecycle-runtime-ktx = { group = "androidx.lifecycle", name = "lifecycle-runtime-ktx" }
androidx-activity-compose = { group = "androidx.activity", name = "activity-compose" }

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
hilt = { id = "dagger.hilt.android.plugin", version = "2.50" }
```

**Step 2: 运行验证**
```bash
./gradlew dependencies --configuration implementation | grep "androidx.compose"
```
Expected: 显示所有 Compose 相关依赖已正确解析

**Step 3: 提交**
```bash
git add gradle/libs.versions.toml gradle/settings.gradle.kts
git commit -m "build: initialize project structure and dependency management"
```

---

### Task 2: 核心架构设计

**目标:** 定义 Clean Architecture 分层结构和基础组件

**文件:**
- Create: `app/src/main/java/com/legado/core/di/DependencyInjection.kt`
- Create: `app/src/main/java/com/legado/core/constants/AppConstants.kt`
- Create: `app/src/main/java/com/legado/core/network/NetworkModule.kt`

**Step 1: 创建应用常量**
```kotlin
object AppConstants {
    const val DATABASE_NAME = "legado_database"
    const val DATABASE_VERSION = 1
    
    object Api {
        const val BASE_URL = "https://api.example.com/"
        const val TIMEOUT_SECONDS = 30L
    }
    
    object Preferences {
        const val THEME_MODE = "theme_mode"
        const val FONT_SIZE = "font_size"
        const val LINE_SPACING = "line_spacing"
    }
}
```

**Step 2: 运行验证**
```bash
./gradlew build --dry-run
```
Expected: 编译通过，无错误

**Step 3: 提交**
```bash
git add app/src/main/java/com/legado/core/
git commit -m "feat: core architecture setup with constants and DI foundation"
```

---

### Task 3: 主题系统设计

**目标:** 创建支持 Material You 的动态主题系统

**文件:**
- Create: `app/src/main/java/com/legado/core/theme/ColorScheme.kt`
- Create: `app/src/main/java/com/legado/core/theme/Theme.kt`
- Create: `app/src/main/java/com/legado/core/theme/Type.kt`

**Step 1: 编写动态颜色方案**
```kotlin
@Composable
fun getDynamicColorScheme(
    isDarkTheme: Boolean,
    colorScheme: ColorScheme = if (isDarkTheme) darkColorScheme() else lightColorScheme()
): ColorScheme {
    return colorScheme.copy(
        primary = MaterialTheme.colorScheme.primary,
        secondary = MaterialTheme.colorScheme.secondary,
        tertiary = MaterialTheme.colorScheme.tertiary
    )
}
```

**Step 2: 运行验证**
```bash
./gradlew compileDebugKotlin
```
Expected: Kotlin 编译成功

**Step 3: 提交**
```bash
git add app/src/main/java/com/legado/core/theme/
git commit -m "design: implement dynamic theme system with Material You support"
```

---

## Phase 2: 数据层开发 (2周)

### Task 4: 数据库架构设计

**目标:** 设计并实现 Room 数据库架构，支持书籍、书源、阅读进度等

**文件:**
- Create: `app/src/main/java/com/legado/data/database/entities/BookEntity.kt`
- Create: `app/src/main/java/com/legado/data/database/dao/BookDao.kt`
- Create: `app/src/main/java/com/legado/data/database/LegadoDatabase.kt`

**Step 1: 创建书籍实体**
```kotlin
@Entity(tableName = "books")
data class BookEntity(
    @PrimaryKey @ColumnInfo(name = "book_id") val bookId: String,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "author") val author: String,
    @ColumnInfo(name = "cover_url") val coverUrl: String?,
    @ColumnInfo(name = "description") val description: String?,
    @ColumnInfo(name = "source_type") val sourceType: String,
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "updated_at") val updatedAt: Long = System.currentTimeMillis()
)
```

**Step 2: 运行验证**
```bash
./gradlew build
```
Expected: 编译通过，Room 注解处理器正常工作

**Step 3: 提交**
```bash
git add app/src/main/java/com/legado/data/database/
git commit -m "data: implement Room database entities and DAOs for books"
```

---

### Task 5: 网络模块开发

**目标:** 创建支持多种书源的网络请求模块

**文件:**
- Create: `app/src/main/java/com/legado/data/network/api/BookSourceApi.kt`
- Create: `app/src/main/java/com/legado/data/network/model/BookResponse.kt`
- Create: `app/src/main/java/com/legado/data/repository/BookRepository.kt`

**Step 1: 创建书源 API 接口**
```kotlin
interface BookSourceApi {
    @GET("books/{id}")
    suspend fun getBook(@Path("id") bookId: String): Response<BookResponse>
    
    @GET("chapters/{bookId}/{chapterIndex}")
    suspend fun getChapter(
        @Path("bookId") bookId: String,
        @Path("chapterIndex") chapterIndex: Int
    ): Response<ChapterResponse>
}
```

**Step 2: 运行验证**
```bash
./gradlew compileDebugKotlin
```
Expected: Retrofit 注解正确解析

**Step 3: 提交**
```bash
git add app/src/main/java/com/legado/data/network/ app/src/main/java/com/legado/data/repository/
git commit -m "network: implement Retrofit API interfaces and repository layer"
```

---

## Phase 3: UI 层开发 (3周)

### Task 6: 主界面框架

**目标:** 创建主书架界面，支持网格/列表视图切换

**文件:**
- Create: `app/src/main/java/com/legado/presentation/screens/home/HomeScreen.kt`
- Create: `app/src/main/java/com/legado/presentation/components/book/BookGridItem.kt`
- Create: `app/src/main/java/com/legado/presentation/components/book/BookListItem.kt`

**Step 1: 编写主界面**
```kotlin
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: HomeViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Legado") },
                actions = {
                    IconButton(onClick = { viewModel.toggleViewMode() }) {
                        Icon(
                            imageVector = if (uiState.isGridView) Icons.Default.List else Icons.Default.GridView,
                            contentDescription = "切换视图"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        when (uiState.viewMode) {
            ViewMode.GRID -> BooksGrid(
                books = uiState.books,
                onBookClick = viewModel::onBookClicked,
                modifier = Modifier.padding(paddingValues)
            )
            ViewMode.LIST -> BooksList(
                books = uiState.books,
                onBookClick = viewModel::onBookClicked,
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}
```

**Step 2: 运行验证**
```bash
./gradlew assembleDebug
```
Expected: APK 生成成功，无编译错误

**Step 3: 提交**
```bash
git add app/src/main/java/com/legado/presentation/screens/home/ app/src/main/java/com/legado/presentation/components/book/
git commit -m "ui: implement main home screen with grid/list view toggle"
```

---

### Task 7: 阅读器核心界面

**目标:** 创建沉浸式阅读界面，支持各种自定义选项

**文件:**
- Create: `app/src/main/java/com/legado/presentation/screens/read/ReaderScreen.kt`
- Create: `app/src/main/java/com/legado/presentation/components/reader/ReaderContent.kt`
- Create: `app/src/main/java/com/legado/presentation/components/reader/ReaderControls.kt`

**Step 1: 编写阅读器内容组件**
```kotlin
@Composable
fun ReaderContent(
    text: String,
    settings: ReadingSettings,
    onTextSizeChange: (Float) -> Unit,
    onLineSpacingChange: (Float) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(settings.backgroundColor)
    ) {
        Text(
            text = text,
            fontSize = settings.fontSize.sp,
            lineHeight = settings.lineSpacing.sp,
            fontWeight = if (settings.bold) FontWeight.Bold else FontWeight.Normal,
            color = settings.textColor,
            fontFamily = settings.fontFamily,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
        )
    }
}
```

**Step 2: 运行验证**
```bash
./gradlew testDebugUnitTest
```
Expected: 单元测试通过

**Step 3: 提交**
```bash
git add app/src/main/java/com/legado/presentation/screens/read/ app/src/main/java/com/legado/presentation/components/reader/
git commit -m "reader: implement core reading interface with customization options"
```

---

## Phase 4: 功能模块开发 (4周)

### Task 8: 书源管理系统

**目标:** 实现自定义书源添加和管理功能

**文件:**
- Create: `app/src/main/java/com/legado/data/source/BookSourceManager.kt`
- Create: `app/src/main/java/com/legado/data/source/rules/RuleEngine.kt`
- Create: `app/src/main/java/com/legado/presentation/screens/source/BookSourceScreen.kt`

**Step 1: 创建规则引擎**
```kotlin
class RuleEngine {
    fun parseChapterList(html: String, rule: ChapterRule): List<Chapter> {
        val pattern = Regex(rule.pattern ?: "")
        return pattern.findAll(html)
            .mapIndexed { index, match ->
                Chapter(
                    index = index,
                    title = match.groupValues.getOrElse(rule.titleGroup) { "" },
                    url = resolveUrl(match.groupValues.getOrElse(rule.urlGroup) { "" }, rule.baseUrl)
                )
            }.toList()
    }
}
```

**Step 2: 运行验证**
```bash
./gradlew build
```
Expected: 编译通过，HTML 解析逻辑正常

**Step 3: 提交**
```bash
git add app/src/main/java/com/legado/data/source/ app/src/main/java/com/legado/presentation/screens/source/
git commit -m "feature: implement book source management with rule engine"
```

---

### Task 9: 广告净化功能

**目标:** 实现智能广告检测和过滤

**文件:**
- Create: `app/src/main/java/com/legado/util/AdFilter.kt`
- Create: `app/src/main/java/com/legado/util/ContentCleaner.kt`

**Step 1: 创建广告过滤器**
```kotlin
object AdFilter {
    private val adPatterns = listOf(
        Regex("\\d+万字免费章节"),
        Regex("点击.*领取.*福利"),
        Regex("关注.*获得.*更新"),
        Regex("\\d+VIP.*会员")
    )
    
    fun cleanContent(content: String): String {
        var cleaned = content
        
        // 移除广告段落
        adPatterns.forEach { pattern ->
            cleaned = cleaned.replace(pattern, "")
        }
        
        // 清理多余的空白行
        cleaned = cleaned.replace(Regex("^\\s*\\n", RegexOption.MULTILINE), "\n")
        
        return cleaned.trim()
    }
}
```

**Step 2: 运行验证**
```bash
./gradlew testDebugUnitTest
```
Expected: 广告过滤功能测试通过

**Step 3: 提交**
```bash
git add app/src/main/java/com/legado/util/
git commit -m "feature: implement ad filtering and content cleaning utilities"
```

---

### Task 10: TTS 听书功能

**目标:** 集成文本转语音功能

**文件:**
- Create: `app/src/main/java/com/legado/service/TTSService.kt`
- Create: `app/src/main/java/com/legado/presentation/components/player/AudioPlayer.kt`

**Step 1: 创建 TTS 服务**
```kotlin
class TTSService @Inject constructor(
    private val context: Context
) {
    private val tts = TextToSpeech(context) { status ->
        if (status == TextToSpeech.SUCCESS) {
            tts.language = Locale.CHINA
        }
    }
    
    fun speak(text: String, onComplete: () -> Unit = {}) {
        tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onDone(utteranceId: String?) = onComplete()
            override fun onError(utteranceId: String?) {}
            override fun onStart(utteranceId: String?) {}
        })
        
        val params = Bundle().apply {
            putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "tts_utterance")
        }
        
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, params, "tts_utterance")
    }
}
```

**Step 2: 运行验证**
```bash
./gradlew assembleDebug
```
Expected: TTS 相关权限正确配置

**Step 3: 提交**
```bash
git add app/src/main/java/com/legado/service/ app/src/main/java/com/legado/presentation/components/player/
git commit -m "feature: implement TTS audio reading functionality"
```

---

## Phase 5: 高级功能 (2周)

### Task 11: AI 智能目录生成

**目标:** 集成 AI 辅助生成书籍目录

**文件:**
- Create: `app/src/main/java/com/legado/ai/DirectoryGenerator.kt`
- Create: `app/src/main/java/com/legado/data/model/AIResponse.kt`

**Step 1: 创建 AI 目录生成器**
```kotlin
class DirectoryGenerator @Inject constructor(
    private val apiService: AIService
) {
    suspend fun generateTableOfContents(content: String): TableOfContents? {
        return try {
            val prompt = """
                基于以下小说内容，分析并生成章节目录结构：
                
                $content
                
                请返回JSON格式：
                {
                    "chapters": [
                        {"title": "第一章", "startPosition": 0, "endPosition": 1000},
                        {"title": "第二章", "startPosition": 1001, "endPosition": 2000}
                    ]
                }
            """.trimIndent()
            
            val response = apiService.generateCompletion(prompt)
            Json.decodeFromString<TableOfContents>(response)
        } catch (e: Exception) {
            null
        }
    }
}
```

**Step 2: 运行验证**
```bash
./gradlew build
```
Expected: AI 集成模块编译成功

**Step 3: 提交**
```bash
git add app/src/main/java/com/legado/ai/ app/src/main/java/com/legado/data/model/
git commit -m "ai: integrate AI-powered table of contents generation"
```

---

### Task 12: 云同步功能

**目标:** 实现阅读进度和书签的云同步

**文件:**
- Create: `app/src/main/java/com/legado/data/sync/SyncService.kt`
- Create: `app/src/main/java/com/legado/data/sync/SyncRepository.kt`

**Step 1: 创建同步服务**
```kotlin
class SyncService @Inject constructor(
    private val apiService: CloudSyncApi,
    private val localRepository: LocalRepository
) {
    suspend fun syncReadingProgress(userId: String) {
        // 上传本地进度
        val progressList = localRepository.getAllReadingProgress()
        progressList.forEach { progress ->
            try {
                apiService.uploadProgress(SyncProgress(
                    userId = userId,
                    bookId = progress.bookId,
                    currentChapter = progress.currentChapter,
                    progressPercentage = progress.progress
                ))
            } catch (e: Exception) {
                // 记录同步失败，下次重试
                Log.e("SyncService", "Failed to upload progress for book ${progress.bookId}", e)
            }
        }
    }
}
```

**Step 2: 运行验证**
```bash
./gradlew testDebugUnitTest
```
Expected: 同步功能测试通过

**Step 3: 提交**
```bash
git add app/src/main/java/com/legado/data/sync/
git commit -m "sync: implement cloud synchronization for reading progress and bookmarks"
```

---

## Phase 6: 优化与发布 (1周)

### Task 13: 性能优化

**目标:** 优化应用性能和内存使用

**文件:**
- Modify: `app/build.gradle.kts` (添加 ProGuard 规则)
- Create: `app/src/main/proguard-rules.pro`

**Step 1: 添加 ProGuard 规则**
```proguard
# Keep Compose related classes
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# Keep data models
-keep class com.legado.data.model.** { *; }

# Keep Hilt generated code
-keep class dagger.hilt.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager
```

**Step 2: 运行验证**
```bash
./gradlew assembleRelease
```
Expected: Release APK 生成成功，体积合理

**Step 3: 提交**
```bash
git add app/build.gradle.kts app/src/main/proguard-rules.pro
git commit -m "perf: optimize release build with ProGuard rules"
```

---

### Task 14: CI/CD 流水线

**目标:** 配置 GitHub Actions 自动化构建和发布

**文件:**
- Create: `.github/workflows/android.yml`
- Create: `.github/workflows/test.yml`

**Step 1: 编写 CI 配置文件**
```yaml
name: Android CI

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main ]

jobs:
  build:
    runs-on: ubuntu-latest
    
    steps:
    - uses: actions/checkout@v3
    
    - name: Setup JDK 17
      uses: actions/setup-java@v3
      with:
        java-version: '17'
        distribution: 'temurin'
    
    - name: Setup Android SDK
      uses: android-actions/setup-android@v2
      
    - name: Build Debug APK
      run: ./gradlew assembleDebug
      
    - name: Upload Artifact
      uses: actions/upload-artifact@v3
      with:
        name: debug-apk
        path: app/build/outputs/apk/debug/app-debug.apk
```

**Step 2: 运行验证**
```bash
git add .github/workflows/
git commit -m "ci: configure GitHub Actions for automated builds"
```

---

## 📋 质量保障

### 测试策略
- **单元测试:** 覆盖核心业务逻辑 (覆盖率 > 70%)
- **集成测试:** UI 交互和功能流程测试
- **UI 测试:** Espresso 自动化测试关键路径

### 发布准备
- [ ] 版本号更新
- [ ] CHANGELOG.md 更新
- [ ] LICENSE 文件检查
- [ ] README.md 完善
- [ ] 代码风格统一 (ktlint)
- [ ] 性能分析报告
- [ ] 安全扫描结果

### 文档要求
- [ ] API 文档生成
- [ ] 用户手册编写
- [ ] 开发者指南更新
- [ ] 贡献者指南完善

---

**计划完成!** 接下来我将使用 subagent-driven-development 技能，按任务逐步实现这个完整的项目。每个任务都将经过两阶段审核：规范合规性检查和代码质量审查。

您希望我现在就开始执行这个计划吗？