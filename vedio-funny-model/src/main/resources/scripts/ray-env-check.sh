#!/bin/bash

# =================================================================
# VedioFun Model Service - Ray环境快速检测脚本
# 用途: 快速检测节点上的Ray环境是否准备就绪
# 作者: VedioFun Team
# =================================================================

set -e  # 遇到错误立即退出

# 脚本目录
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# 加载Ray环境变量配置 - 优先使用环境变量传入的路径
if [[ -n "$RAY_ENV_FILE" ]]; then
    ENV_FILE="$RAY_ENV_FILE"
else
    ENV_FILE="$SCRIPT_DIR/../ray.env"
fi

if [[ -f "$ENV_FILE" ]]; then
    source "$ENV_FILE"
else
    echo "❌ 未找到Ray环境配置文件: $ENV_FILE"
    echo "提示: 请设置 RAY_ENV_FILE 环境变量指向正确的ray.env文件路径"
    exit 1
fi

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 检测结果
RAY_READY=false
CONDA_OK=false
RAY_ENV_OK=false
RAY_INSTALLED_OK=false
MODEL_ENGINES_OK=false

# 快速检测conda
quick_check_conda() {
    if [[ -f "${CONDA_HOME}/bin/conda" ]] || command -v conda &> /dev/null; then
        CONDA_OK=true
        return 0
    fi
    CONDA_OK=false
    return 0  # 不要因为conda不存在而退出脚本
}

# 快速检测Ray环境
quick_check_ray_env() {
    if ! $CONDA_OK; then
        RAY_ENV_OK=false
        return 0
    fi
    
    local conda_path
    if [[ -f "${CONDA_HOME}/bin/conda" ]]; then
        conda_path="${CONDA_HOME}/bin/conda"
    else
        conda_path=$(which conda 2>/dev/null || echo "")
    fi
    
    if [[ -n "$conda_path" ]] && $conda_path env list 2>/dev/null | grep -q "^${CONDA_ENV_NAME} "; then
        RAY_ENV_OK=true
        return 0
    fi
    RAY_ENV_OK=false
    return 0
}

# 快速检测Ray安装
quick_check_ray() {
    if ! $RAY_ENV_OK; then
        RAY_INSTALLED_OK=false
        return 0
    fi
    
    local conda_dir=$(dirname "${CONDA_HOME}/bin/conda")
    local conda_base_dir=$(dirname "$conda_dir")
    
    if [[ -f "$conda_base_dir/etc/profile.d/conda.sh" ]]; then
        source "$conda_base_dir/etc/profile.d/conda.sh"
        if conda activate "${CONDA_ENV_NAME}" 2>/dev/null; then
            if python -c "import ray" &>/dev/null; then
                RAY_INSTALLED_OK=true
                conda deactivate 2>/dev/null || true
                return 0
            fi
            conda deactivate 2>/dev/null || true
        fi
    fi
    RAY_INSTALLED_OK=false
    return 0
}

# 快速检测模型引擎依赖
quick_check_model_engines() {
    if ! $RAY_INSTALLED_OK; then
        MODEL_ENGINES_OK=false
        return 0
    fi
    
    local conda_dir=$(dirname "${CONDA_HOME}/bin/conda")
    local conda_base_dir=$(dirname "$conda_dir")
    
    if [[ -f "$conda_base_dir/etc/profile.d/conda.sh" ]]; then
        source "$conda_base_dir/etc/profile.d/conda.sh"
        if conda activate "${CONDA_ENV_NAME}" 2>/dev/null; then
            # 检查VLLM
            if python -c "import vllm" &>/dev/null; then
                # 检查transformers
                if python -c "import transformers" &>/dev/null; then
                    MODEL_ENGINES_OK=true
                    conda deactivate 2>/dev/null || true
                    return 0
                fi
            fi
            conda deactivate 2>/dev/null || true
        fi
    fi
    MODEL_ENGINES_OK=false
    return 0
}

# 输出简要状态
show_status() {
    echo "🚀 Ray环境状态检查"
    echo "=================="
    
    if $CONDA_OK; then
        echo -e "📦 Conda: ${GREEN}✅ 可用${NC}"
    else
        echo -e "📦 Conda: ${RED}❌ 不可用${NC}"
    fi
    
    if $RAY_ENV_OK; then
        echo -e "🐍 Ray环境 (${CONDA_ENV_NAME}): ${GREEN}✅ 存在${NC}"
    else
        echo -e "🐍 Ray环境 (${CONDA_ENV_NAME}): ${RED}❌ 不存在${NC}"
    fi
    
    if $RAY_INSTALLED_OK; then
        echo -e "🚀 Ray: ${GREEN}✅ 已安装${NC}"
    else
        echo -e "🚀 Ray: ${RED}❌ 未安装${NC}"
    fi
    
    if $MODEL_ENGINES_OK; then
        echo -e "🤖 模型引擎依赖: ${GREEN}✅ 已安装${NC}"
    else
        echo -e "🤖 模型引擎依赖: ${RED}❌ 未安装${NC}"
    fi
    
    echo "=================="
    
    # Ray核心环境就绪（用于集群管理）
    if $CONDA_OK && $RAY_ENV_OK && $RAY_INSTALLED_OK; then
        RAY_READY=true
        if $MODEL_ENGINES_OK; then
            echo -e "🎉 Ray环境: ${GREEN}✅ 完全准备就绪${NC}"
        else
            echo -e "🎉 Ray环境: ${GREEN}✅ 核心功能就绪${NC} (模型引擎依赖待安装)"
        fi
        echo ""
        echo "可以执行以下操作:"
        echo "  source $SCRIPT_DIR/setup-conda-env.sh activate"
        echo "  ray start --head --port=${RAY_HEAD_PORT}"
        echo "  ray start --address=<head_node>:${RAY_HEAD_PORT}"
    else
        RAY_READY=false
        echo -e "⚠️  Ray环境: ${YELLOW}❌ 未准备就绪${NC}"
        echo ""
        echo "建议执行:"
        echo "  bash $SCRIPT_DIR/install-miniconda.sh"
    fi
}

# JSON输出
output_json() {
    cat << EOF
{
  "rayReady": $RAY_READY,
  "conda": {
    "available": $CONDA_OK,
    "path": "${CONDA_HOME}"
  },
  "rayEnvironment": {
    "exists": $RAY_ENV_OK,
    "name": "${CONDA_ENV_NAME}"
  },
  "ray": {
    "installed": $RAY_INSTALLED_OK
  },
  "modelEnginesInstalled": $MODEL_ENGINES_OK,
  "config": {
    "headPort": ${RAY_HEAD_PORT},
    "dashboardPort": ${RAY_DASHBOARD_PORT},
    "tmpDir": "${RAY_TMPDIR}"
  },
  "timestamp": "$(date -u +"%Y-%m-%dT%H:%M:%SZ")"
}
EOF
}

# 主函数
main() {
    case "${1:-status}" in
        "status"|"check")
            quick_check_conda
            quick_check_ray_env
            quick_check_ray
            quick_check_model_engines
            show_status
            ;;
        "json")
            quick_check_conda
            quick_check_ray_env  
            quick_check_ray
            quick_check_model_engines
            # 设置RAY_READY状态 - 核心环境就绪即可
            if $CONDA_OK && $RAY_ENV_OK && $RAY_INSTALLED_OK; then
                RAY_READY=true
            else
                RAY_READY=false
            fi
            output_json
            ;;
        "ready")
            quick_check_conda
            quick_check_ray_env
            quick_check_ray
            quick_check_model_engines
            # 设置RAY_READY状态 - 核心环境就绪即可
            if $CONDA_OK && $RAY_ENV_OK && $RAY_INSTALLED_OK; then
                RAY_READY=true
            else
                RAY_READY=false
            fi
            if $RAY_READY; then
                exit 0
            else
                exit 1
            fi
            ;;
        "install")
            echo "🛠️  开始安装Ray环境..."
            bash "$SCRIPT_DIR/install-miniconda.sh"
            ;;
        "help"|"--help"|"-h")
            echo "用法: $0 [命令]"
            echo ""
            echo "命令:"
            echo "  status     显示Ray环境状态 (默认)"
            echo "  json       输出JSON格式状态"
            echo "  ready      检查是否准备就绪（退出码）"
            echo "  install    安装Ray环境"
            echo "  help       显示帮助信息"
            echo ""
            echo "退出码:"
            echo "  0 - Ray环境准备就绪"
            echo "  1 - Ray环境未准备就绪"
            ;;
        *)
            echo "未知命令: $1"
            echo "使用 '$0 help' 查看帮助"
            exit 1
            ;;
    esac
    
    # 设置退出码
    if $RAY_READY; then
        exit 0
    else
        exit 1
    fi
}

# 执行主函数
main "$@" 
            quick_check_ray_env
            quick_check_ray
            quick_check_model_engines
            # 设置RAY_READY状态 - 核心环境就绪即可
            if $CONDA_OK && $RAY_ENV_OK && $RAY_INSTALLED_OK; then
                RAY_READY=true
            else
                RAY_READY=false
            fi
            if $RAY_READY; then
                exit 0
            else
                exit 1
            fi
            ;;
        "install")
            echo "🛠️  开始安装Ray环境..."
            bash "$SCRIPT_DIR/install-miniconda.sh"
            ;;
        "help"|"--help"|"-h")
            echo "用法: $0 [命令]"
            echo ""
            echo "命令:"
            echo "  status     显示Ray环境状态 (默认)"
            echo "  json       输出JSON格式状态"
            echo "  ready      检查是否准备就绪（退出码）"
            echo "  install    安装Ray环境"
            echo "  help       显示帮助信息"
            echo ""
            echo "退出码:"
            echo "  0 - Ray环境准备就绪"
            echo "  1 - Ray环境未准备就绪"
            ;;
        *)
            echo "未知命令: $1"
            echo "使用 '$0 help' 查看帮助"
            exit 1
            ;;
    esac
    
    # 设置退出码
    if $RAY_READY; then
        exit 0
    else
        exit 1
    fi
}

# 执行主函数
main "$@" 