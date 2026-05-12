# Legado Android Reader

一个功能强大的开源 Android 阅读器，专为追求极致阅读体验的用户设计。采用现代 Android 开发技术栈，提供无广告、功能丰富、界面美观的阅读体验。

## 🌟 核心特性

### 📚 书源管理
- **多书源支持**: 支持多种小说网站和书籍来源
- **规则引擎**: 智能内容解析和适配
- **搜索发现**: 高质量书源的搜索和发现
- **RSS订阅**: 实时更新订阅内容

### 📖 阅读体验
- **多格式支持**: TXT、EPUB、HTML 等常见格式
- **界面定制**: 字体、颜色、背景、行距、段距自定义
- **翻页模式**: 仿真、滑动、无效果等多种翻页方式
- **听书功能**: TTS 文本转语音朗读
- **简繁转换**: 智能简体中文和繁体中文转换

### 🛠️ 功能工具
- **广告净化**: 自动过滤广告内容，保持阅读纯净
- **笔记书签**: 记录重要内容和位置信息
- **进度同步**: 云端同步阅读进度和书签
- **智能目录**: AI辅助生成章节目录结构
- **批量管理**: 高效管理大量书籍

## 🚀 快速开始

### 环境要求
- Android Studio Arctic Fox+ (2020.3.1)
- JDK 17+
- Kotlin 1.9.0+

### 构建项目
```bash
# 克隆项目
git clone https://github.com/yourusername/legado.git
cd legado

# 使用Android Studio打开项目
# File > Open > 选择legado目录

# 或者使用命令行构建
./gradlew assembleDebug
```

### 运行应用
```bash
# 在Android设备上安装并运行
adb install app/build/outputs/apk/debug/app-debug.apk
```

## 🏗️ 技术架构

### Clean Architecture + MVVM
```
├── Core Layer (基础层)
│   ├── Theme System (动态主题) - Material You
│   ├── DI Framework (Hilt注入) - 依赖管理
│   └── Constants (应用常量) - 全局配置
├── Data Layer (数据层) 
│   ├── Room Database (本地存储) - SQLite ORM
│   ├── Retrofit API (网络请求) - REST客户端
│   ├── CloudSync (云同步) - 数据同步
│   └── Repository (仓库模式) - 数据抽象
├── Domain Layer (领域层)
│   ├── Use Cases (业务用例) - 业务逻辑
│   ├── Entities (实体模型) - 数据模型
│   ├── AI Engine (AI引擎) - 智能分析
│   └── Community (社区) - 用户互动
└── Presentation Layer (表现层)
    ├── Compose UI (现代UI框架) - Jetpack Compose
    ├── ViewModels (状态管理) - StateFlow
    ├── Components (可复用组件) - UI组件
    └── Performance (性能优化) - 内存管理
```

## 🎨 用户界面

### 主书架界面
- **网格/列表视图切换**: 自由选择最舒适的查看方式
- **Material Design 3**: 现代化的设计语言和交互体验
- **响应式布局**: 完美适配不同屏幕尺寸的设备
- **状态管理**: ViewModel + StateFlow 架构确保数据一致性

### 沉浸式阅读器
- **全屏阅读模式**: 减少干扰，专注阅读
- **手势翻页**: 左右滑动轻松切换章节
- **自定义设置**: 字体大小、行间距、背景颜色等个性化选项
- **控制栏动画**: 智能显示/隐藏，保持阅读专注度

## ⚡ 性能优化

### 内存管理
- **LRU缓存**: 优化图片加载和内存使用
- **智能预加载**: 书籍封面和内容预加载
- **资源回收**: 及时释放不再使用的资源

### 电池优化
- **后台任务调度**: WorkManager智能调度
- **同步频率调整**: 根据电量状态调整同步间隔
- **网络优化**: 减少不必要的网络请求

## 🤝 社区功能

### 书评分享
用户可以发布书评，参与讨论：
```kotlin
communityManager.publishReview(userId, bookId, rating, title, content)
// 发布书评，参与讨论，获得推荐
```

## 📱 设备兼容性

- **最低版本**: Android 5.0 (API Level 21)
- **目标版本**: Android 14 (API Level 34)
- **屏幕适配**: 支持各种屏幕尺寸和分辨率
- **横竖屏**: 支持横屏和竖屏阅读模式

## 🔒 隐私和安全

- **数据加密**: 本地敏感数据加密存储
- **权限最小化**: 只请求必要的系统权限
- **隐私保护**: 不会收集或上传个人阅读数据
- **开源透明**: 所有代码公开可审查

## 📈 性能指标

| 指标 | 目标值 | 当前状态 |
|-----|-------|---------|
| 启动时间 | < 2秒 | ✅ 达标 |
| 内存占用 | < 100MB | ✅ 达标 |
| 电池消耗 | 正常范围 | ✅ 达标 |
| UI流畅度 | 60fps | ✅ 达标 |
| 兼容性 | API 21+ | ✅ 达标 |

## 🛠️ 开发者指南

### 环境搭建
```bash
# 克隆项目
git clone https://github.com/yourusername/legado.git
cd legado

# 安装依赖
./gradlew dependencies

# 构建项目
./gradlew build

# 运行测试
./gradlew test
```

### 贡献流程
1. Fork 项目到个人GitHub仓库
2. 创建特性分支 (`git checkout -b feature/new-feature`)
3. 提交更改 (`git commit -am 'Add new feature'`)
4. 推送到分支 (`git push origin feature/new-feature`)
5. 创建Pull Request

## 📄 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情。

## 🙏 致谢

感谢所有为这个项目做出贡献的开发者和测试者！

---

**🎉 Legado 开源阅读应用**

**Legado 正在成为您数字阅读体验的最佳伙伴！** 📚✨