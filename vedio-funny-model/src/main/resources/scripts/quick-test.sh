#!/bin/bash

# 快速测试脚本 - 验证模型API是否正常工作
MODEL_URL="http://localhost:8000"

echo "🚀 快速测试 VedioFun Model API"
echo "================================"

# 检查服务状态
echo "1. 检查服务状态..."
if curl -s "${MODEL_URL}/health" > /dev/null 2>&1; then
    echo "✅ 服务正在运行"
else
    echo "❌ 服务未运行，请先启动RayLLM服务"
    exit 1
fi

# 测试模型列表
echo -e "\n2. 测试模型列表..."
curl -s -X GET "${MODEL_URL}/v1/models" | head -5

# 测试简单聊天
echo -e "\n3. 测试简单聊天..."
curl -s -X POST "${MODEL_URL}/v1/chat/completions" \
    -H "Content-Type: application/json" \
    -d '{
        "model": "default",
        "messages": [{"role": "user", "content": "你好"}],
        "max_tokens": 20,
        "temperature": 0.7
    }' | head -10

echo -e "\n✅ 快速测试完成！" 

# 快速测试脚本 - 验证模型API是否正常工作
MODEL_URL="http://localhost:8000"

echo "🚀 快速测试 VedioFun Model API"
echo "================================"

# 检查服务状态
echo "1. 检查服务状态..."
if curl -s "${MODEL_URL}/health" > /dev/null 2>&1; then
    echo "✅ 服务正在运行"
else
    echo "❌ 服务未运行，请先启动RayLLM服务"
    exit 1
fi

# 测试模型列表
echo -e "\n2. 测试模型列表..."
curl -s -X GET "${MODEL_URL}/v1/models" | head -5

# 测试简单聊天
echo -e "\n3. 测试简单聊天..."
curl -s -X POST "${MODEL_URL}/v1/chat/completions" \
    -H "Content-Type: application/json" \
    -d '{
        "model": "default",
        "messages": [{"role": "user", "content": "你好"}],
        "max_tokens": 20,
        "temperature": 0.7
    }' | head -10

echo -e "\n✅ 快速测试完成！" 