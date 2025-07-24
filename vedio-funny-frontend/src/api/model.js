// model.js
import request from '@/utils/request'

export function getModelList() {
  return request({
    url: '/api/model/model/list',
    method: 'get'
  })
}

// 部署模型到集群
export function deployModelToCluster(data) {
  return request({
    url: '/api/model/model/deploy-cluster',
    method: 'post',
    data: {
      modelId: data.modelId || 1, // 默认值
      modelName: data.modelName,
      modelSource: data.modelSource,
      nodeIds: data.nodeIds,
      deploymentType: data.deploymentType,
      rayConfig: data.rayConfig ? JSON.parse(data.rayConfig) : {}
    }
  })
}

// Ray模型部署相关API（暂时保留，可能后续需要）

/**
 * 检查节点环境
 * @param {Array} nodeIds 节点ID列表
 */
export function checkNodeEnvironment(nodeIds) {
  return request({
    url: '/api/model/ray/check-environment',
    method: 'post',
    data: { nodeIds }
  })
}

/**
 * 安装Python和Ray环境
 * @param {Array} nodeIds 节点ID列表
 */
export function installRayEnvironment(nodeIds) {
  return request({
    url: '/api/model/ray/install-environment',
    method: 'post',
    data: { nodeIds }
  })
}

/**
 * 创建Ray集群
 * @param {Object} clusterConfig 集群配置
 */
export function createRayCluster(clusterConfig) {
  return request({
    url: '/api/model/ray/create-cluster',
    method: 'post',
    data: clusterConfig
  })
}

/**
 * 下载模型到主节点
 * @param {Object} downloadConfig 下载配置
 */
export function downloadModel(downloadConfig) {
  return request({
    url: '/api/model/ray/download-model',
    method: 'post',
    data: downloadConfig
  })
}

/**
 * 启动RayLLM服务
 * @param {Object} launchConfig 启动配置
 */
export function launchRayLLM(launchConfig) {
  return request({
    url: '/api/model/ray/launch-rayllm',
    method: 'post',
    data: launchConfig
  })
}

/**
 * 获取部署状态
 * @param {String} deploymentId 部署ID
 */
export function getDeploymentStatus(deploymentId) {
  return request({
    url: `/api/model/ray/deployment-status/${deploymentId}`,
    method: 'get'
  })
}

/**
 * 停止Ray部署
 * @param {String} deploymentId 部署ID
 */
export function stopRayDeployment(deploymentId) {
  return request({
    url: `/api/model/ray/stop-deployment/${deploymentId}`,
    method: 'post'
  })
}