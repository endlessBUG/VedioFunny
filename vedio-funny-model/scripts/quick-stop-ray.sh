#!/bin/bash

# =================================================================
# 快速停止Ray集群脚本
# 用途: 快速停止Ray集群和相关服务
# =================================================================

echo "🛑 快速停止Ray集群..."

# 获取脚本所在目录的上级目录（项目根目录）
script_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
project_root="$(dirname "$script_dir")"

# 加载ray.env环境
if [ -f "$project_root/resources/ray.env" ]; then
    source "$project_root/resources/ray.env"
else
    echo "❌ 找不到ray.env文件: $project_root/resources/ray.env"
    exit 1
fi

# 激活conda环境
if [ -n "$CONDA_HOME" ] && [ -f "$CONDA_HOME/etc/profile.d/conda.sh" ]; then
    source "$CONDA_HOME/etc/profile.d/conda.sh"
    conda activate "$RAY_ENV_NAME"
else
    echo "❌ 找不到conda环境或ray.env配置不正确"
    exit 1
fi

# 停止Ray集群
echo "📋 停止Ray集群..."
ray stop 2>/dev/null || echo "Ray集群已停止或未运行"

# 停止RayLLM相关进程
echo "🔧 停止RayLLM服务..."
pkill -f "rayllm" 2>/dev/null || echo "未找到rayllm进程"
pkill -f "vllm" 2>/dev/null || echo "未找到vllm进程"
pkill -f "tgi" 2>/dev/null || echo "未找到tgi进程"

# 清理临时文件
echo "🧹 清理临时文件..."
rm -rf /tmp/ray/* 2>/dev/null || echo "Ray临时目录已清理"
rm -rf /tmp/ray-test 2>/dev/null || echo "Ray测试目录已清理"

echo "✅ Ray集群停止完成！" 

# =================================================================
# 快速停止Ray集群脚本
# 用途: 快速停止Ray集群和相关服务
# =================================================================

echo "🛑 快速停止Ray集群..."

# 获取脚本所在目录的上级目录（项目根目录）
script_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
project_root="$(dirname "$script_dir")"

# 加载ray.env环境
if [ -f "$project_root/resources/ray.env" ]; then
    source "$project_root/resources/ray.env"
else
    echo "❌ 找不到ray.env文件: $project_root/resources/ray.env"
    exit 1
fi

# 激活conda环境
if [ -n "$CONDA_HOME" ] && [ -f "$CONDA_HOME/etc/profile.d/conda.sh" ]; then
    source "$CONDA_HOME/etc/profile.d/conda.sh"
    conda activate "$RAY_ENV_NAME"
else
    echo "❌ 找不到conda环境或ray.env配置不正确"
    exit 1
fi

# 停止Ray集群
echo "📋 停止Ray集群..."
ray stop 2>/dev/null || echo "Ray集群已停止或未运行"

# 停止RayLLM相关进程
echo "🔧 停止RayLLM服务..."
pkill -f "rayllm" 2>/dev/null || echo "未找到rayllm进程"
pkill -f "vllm" 2>/dev/null || echo "未找到vllm进程"
pkill -f "tgi" 2>/dev/null || echo "未找到tgi进程"

# 清理临时文件
echo "🧹 清理临时文件..."
rm -rf /tmp/ray/* 2>/dev/null || echo "Ray临时目录已清理"
rm -rf /tmp/ray-test 2>/dev/null || echo "Ray测试目录已清理"

echo "✅ Ray集群停止完成！" 