# 沉浸式阅读器 - 快速参考指南

## 📱 主要组件

### ReaderScreen (主界面)
**文件**: `app/src/main/java/com/legado/presentation/screens/read/ReaderScreen.kt`

**核心功能**:
- 全屏沉浸式阅读体验
- 自动隐藏/显示控制栏
- 多主题支持（明亮、暗黑、护眼、夜间）
- 错误处理和加载状态
- 设置面板管理

**关键参数**:
```kotlin
@Composable
fun ReaderScreen(
    book: BookEntity,
    initialChapterPosition: Int = 1,
    onBackClick: () -> Unit = {},
    viewModel: ReaderViewModel = hiltViewModel()
)
```

### ReaderContent (内容渲染)
**文件**: `app/src/main/java/com/legado/presentation/components/reader/ReaderContent.kt`

**支持的翻页模式**:
- `SLIDE_HORIZONTAL` - 水平滑动翻页
- `SLIDE_VERTICAL` - 垂直滑动阅读
- `FADE` - 淡入淡出切换
- `NONE` - 无动画模式

**手势类型**:
```kotlin
sealed class GestureInfo {
    object TapLeft : GestureInfo()      // 左点击翻前页
    object TapRight : GestureInfo()     // 右点击翻后页
    object SwipeLeft : GestureInfo()    // 左滑翻后页
    object SwipeRight : GestureInfo()   // 右滑翻前页
    data class Pan(val deltaX: Float, val deltaY: Float) : GestureInfo()  // 平移滚动
    data class Scale(val scale: Float) : GestureInfo()  // 缩放调整字体
}
```

### ReaderControls (控制面板)
**文件**: `app/src/main/java/com/legado/presentation/components/reader/ReaderControls.kt`

**主要功能**:
- 章节进度条（支持拖拽跳转）
- 上一章/下一章按钮
- 自动滚动开关和速度选择
- 章节快速标记点

### ReaderViewModel (状态管理)
**文件**: `app/src/main/java/com/legado/presentation/screens/read/ReaderViewModel.kt`

**重要方法**:
```kotlin
// 加载章节
loadChapter(bookId: Long, chapterPosition: Int)

// 导航控制
nextChapter()          // 下一章
previousChapter()     // 上一章
goToChapter(position: Float)  // 跳转到指定位置

// 设置管理
updateSettings(newSettings: ReaderSettings)
toggleFontSelector()   // 显示/隐藏字体设置
toggleThemeSelector()  // 显示/隐藏主题设置

// 手势处理
handleGesture(gestureInfo: GestureInfo)
scrollBy(delta: Float) // 手动滚动
adjustFontSize(scale: Float) // 调整字体大小
```

## 🎨 自定义选项

### ReaderSettings 数据结构
```kotlin
data class ReaderSettings(
    val fontSize: Float = 16f,           // 字体大小 (sp)
    val lineHeight: Float = 1.6f,        // 行高倍数
    val fontFamily: FontFamily = FontFamily.Default, // 字体族
    val theme: ReaderTheme = ReaderTheme.LIGHT,      // 主题模式
    val backgroundColor: Color = Color.White,        // 背景色
    val textColor: Color = Color.Black,              // 文字颜色
    val showPageNumbers: Boolean = true,             // 显示页码
    val autoScrollSpeed: Float = 0f,                 // 自动滚动速度
    val pageTransition: PageTransition = PageTransition.SLIDE_HORIZONTAL // 翻页动画
)
```

### 可用主题模式
```kotlin
enum class ReaderTheme {
    LIGHT,   // 明亮模式 (白色背景，黑色文字)
    DARK,    // 暗黑模式 (黑色背景，白色文字)
    SEPIA,   // 护眼模式 (米色背景，深褐色文字)
    NIGHT    // 夜间模式 (深灰色背景，浅灰色文字)
}
```

### 可用翻页动画
```kotlin
enum class PageTransition {
    SLIDE_HORIZONTAL,  // 水平滑动
    SLIDE_VERTICAL,    // 垂直滑动
    FADE,             // 淡入淡出
    NONE              // 无动画
}
```

## 🔗 导航配置

### MainActivity 路由
**文件**: `app/src/main/java/com/legado/app/MainActivity.kt`

**路由定义**:
```
"home" -> HomeScreen
"reader/{bookId}/{chapterPosition}" -> ReaderScreen
```

**参数传递**:
- `bookId`: Long - 书籍ID
- `chapterPosition`: Int - 初始章节位置

## 🛠️ 开发指南

### 添加新主题
1. 在 `ReaderTheme` enum 中添加新主题
2. 在 `ThemeSelectorPanel` 中更新主题列表
3. 实现对应的主题颜色方案

### 添加新的翻页动画
1. 在 `PageTransition` enum 中添加新动画类型
2. 在 `ReaderContent` 中创建对应的渲染组件
3. 更新手势识别逻辑

### 集成真实数据
1. 创建 Repository 接口
2. 实现 ChapterRepository
3. 更新 ViewModel 中的数据获取逻辑

## 📊 性能指标

### 内存优化
- 懒加载章节内容
- 图片缓存管理
- 状态流优化避免重复计算

### 用户体验
- 流畅的60fps动画
- 即时响应的手势操作
- 智能的控制栏显示/隐藏

## 🚀 部署说明

### 构建要求
- Android API Level 21+
- Kotlin 1.9.10+
- Compose Compiler 1.5.3+

### 依赖项
```gradle
implementation("androidx.navigation:navigation-compose:2.7.6")
implementation("androidx.compose.material3:material3")
implementation("io.coil-kt:coil-compose:2.5.0")
```

## 💡 使用提示

### 最佳实践
1. **字体大小**: 建议保持在14-20sp之间
2. **行高**: 1.5-1.8倍行高最适合阅读
3. **主题切换**: 根据环境光线选择合适的主题
4. **手势操作**: 左右滑动最自然的翻页方式

### 常见问题
**Q**: 如何保存阅读进度？
**A**: 当前版本已记录章节位置，实际项目需要连接数据库持久化

**Q**: 如何添加新的字体？
**A**: 在ReaderSettings中添加字体族，在FontSelectorPanel中提供选择界面

**Q**: 如何实现语音朗读？
**A**: 需要集成Text-to-Speech引擎，在ReaderControls中添加播放控制

## 📝 更新日志

### v1.0.0 (当前版本)
- ✅ 基础阅读器界面实现
- ✅ 多种翻页动画效果
- ✅ 完整的自定义选项
- ✅ 手势交互支持
- ✅ 主题系统
- ✅ 导航集成
- ✅ 状态管理

### 后续版本规划
- v1.1.0: 数据源集成 + 书签功能
- v1.2.0: 字体下载 + 社交分享
- v1.3.0: 语音朗读 + 个性化推荐