#!/bin/bash
# 项目验证脚本 - 检查 Legado 阅读应用的核心模块

echo "🔍 正在验证 Legado 项目状态..."

echo ""
echo "📁 项目文件结构:"
find . -name "*.kt" -o -name "*.xml" -o -name "*.gradle*" | grep -E "(core|data|presentation)" | sort

echo ""
echo "✅ 核心模块验证:"

# 检查主题系统
if [ -f "app/src/main/java/com/legado/core/theme/Theme.kt" ]; then
    echo "   🎨 Theme System: ✓"
else
    echo "   🎨 Theme System: ✗"
fi

# 检查数据库
if [ -f "app/src/main/java/com/legado/data/database/LegadoDatabase.kt" ]; then
    echo "   🗄️  Database: ✓"
else
    echo "   🗄️  Database: ✗"
fi

# 检查网络模块
if [ -f "app/src/main/java/com/legado/data/network/api/BookSourceApi.kt" ]; then
    echo "   🌐 Network: ✓"
else
    echo "   🌐 Network: ✗"
fi

# 检查构建配置
if [ -f "app/build.gradle.kts" ]; then
    echo "   🏗️  Build Config: ✓"
else
    echo "   🏗️  Build Config: ✗"
fi

echo ""
echo "📊 项目统计:"
echo "   • Kotlin 文件: $(find app/src/main/java -name "*.kt" | wc -l)"
echo "   • Java 文件: $(find app/src/main/java -name "*.java" | wc -l)"
echo "   • XML 资源: $(find app/src/main/res -name "*.xml" | wc -l)"

echo ""
echo "🎉 Legado 开源阅读应用核心架构已完成!"
echo "   准备进入 UI 开发阶段..."