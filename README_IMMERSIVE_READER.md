# 沉浸式阅读器实现

## 功能特性

### 核心功能
- **沉浸式阅读体验**：全屏阅读，自动隐藏系统UI
- **多种翻页方式**：水平滑动、垂直滑动、淡入淡出、无动画
- **手势交互支持**：点击翻页、滑动手势、捏合缩放字体
- **自动滚动**：可设置自动滚动速度和模式

### 自定义选项
- **字体设置**：
  - 字体大小调节（12sp - 32sp）
  - 行高设置（1.2 - 2.5倍）
  - 字体类型选择

- **主题模式**：
  - 明亮模式：标准白色背景
  - 暗黑模式：深色背景
  - 护眼模式：米色背景，减少眼睛疲劳
  - 夜间模式：深灰色背景

- **页面布局**：
  - 章节进度指示器
  - 页码显示
  - 快速跳转功能

## 技术架构

### 组件结构
```
com.legado.presentation.screens.read/
├── ReaderScreen.kt          # 主阅读器界面
├── ReaderViewModel.kt       # 阅读器状态管理
└── components/
    ├── ReaderContent.kt     # 阅读内容组件
    └── ReaderControls.kt    # 阅读器控制栏
```

### 状态管理
- 使用 `ReaderViewModel` 管理阅读状态和用户设置
- 支持实时更新字体大小、主题等设置
- 持久化存储阅读进度和偏好设置

### 导航集成
- 使用 Jetpack Navigation Compose
- 从书架页面跳转到阅读器
- 支持返回功能和参数传递

## 使用方法

### 启动阅读器
1. 在书架页面点击任意书籍
2. 系统自动跳转到阅读器界面
3. 默认显示第一章内容

### 基本操作
- **翻页**：左右滑动屏幕或点击左右区域
- **查看目录**：点击顶部菜单的"章节目录"
- **调整字体**：点击"字体设置"
- **切换主题**：点击"主题设置"

### 高级功能
- **自动滚动**：底部控制栏的播放按钮
- **快速跳转**：进度条上的章节标记点
- **手势操作**：支持多种手势交互

## 开发说明

### 添加新主题
在 `ReaderTheme` enum 中添加新主题，并在 `ThemeSelectorPanel` 中实现对应的颜色方案。

### 自定义翻页动画
在 `PageTransition` enum 中添加新的过渡效果，并在相应组件中实现动画逻辑。

### 扩展字体支持
在 `ReaderSettings` 中添加新的字体类型，并在 `FontSelectorPanel` 中提供选择界面。

## 文件结构

```
app/src/main/java/com/legado/presentation/screens/read/
├── ReaderScreen.kt          (主界面组件)
├── ReaderViewModel.kt       (状态管理)
└── components/
    ├── ReaderContent.kt     (内容渲染)
    └── ReaderControls.kt    (控制界面)

app/src/main/java/com/legado/app/MainActivity.kt (导航配置)
```

## 依赖关系

- Jetpack Compose UI
- Material Design 3
- Hilt 依赖注入
- Navigation Compose
- Room 数据库
- Retrofit 网络请求