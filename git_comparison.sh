#!/bin/bash

echo "======================================="
echo "      Git Merge vs Rebase 对比"
echo "======================================="
echo
echo "🔀 Git Merge 策略 - 保留分支历史："
echo "A---B---C---D-------M     ← main (合并后)"
echo "         \\         /"
echo "          E---F---G       ← feature (分支)"
echo
echo "特点："
echo "  ✅ 保留完整的分支历史"
echo "  ✅ 显示真实的合并过程"
echo "  ❌ 历史图可能显得复杂"
echo
echo "📏 Git Rebase 策略 - 线性历史："
echo "变基前："
echo "A---B---C---D             ← main"
echo "         \\"
echo "          E---F---G       ← feature"
echo
echo "变基后："
echo "A---B---C---D---E'---F'---G'    ← main (线性历史)"
echo "         \\"
echo "          ❌ E---F---G           ← 原提交被重写"
echo
echo "特点："
echo "  ✅ 创建整洁的线性历史"
echo "  ✅ 更容易理解和追踪"
echo "  ❌ 重写了提交历史"
echo "  ❌ 可能丢失时间信息"
echo
echo "======================================="
