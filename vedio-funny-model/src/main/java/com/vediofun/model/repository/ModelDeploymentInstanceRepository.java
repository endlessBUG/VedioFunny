package com.vediofun.model.repository;

import com.vediofun.model.entity.ModelDeploymentInstance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 模型部署实例Repository接口
 */
@Repository
public interface ModelDeploymentInstanceRepository extends JpaRepository<ModelDeploymentInstance, Long> {

    /**
     * 根据模型ID查找部署实例
     */
    List<ModelDeploymentInstance> findByModelId(Long modelId);

    /**
     * 根据模型ID和状态查找部署实例
     */
    List<ModelDeploymentInstance> findByModelIdAndStatus(Long modelId, ModelDeploymentInstance.DeploymentStatus status);

    /**
     * 根据状态查找所有部署实例
     */
    List<ModelDeploymentInstance> findByStatus(ModelDeploymentInstance.DeploymentStatus status);

    /**
     * 根据部署类型查找部署实例
     */
    List<ModelDeploymentInstance> findByDeploymentType(String deploymentType);

    /**
     * 根据集群地址查找部署实例
     */
    List<ModelDeploymentInstance> findByClusterAddress(String clusterAddress);

    /**
     * 根据部署者查找部署实例
     */
    List<ModelDeploymentInstance> findByDeployedBy(Long deployedBy);

    /**
     * 查找运行中的部署实例
     */
    @Query("SELECT d FROM ModelDeploymentInstance d WHERE d.status = 'RUNNING' ORDER BY d.createdTime DESC")
    List<ModelDeploymentInstance> findRunningInstances();

    /**
     * 根据服务端点查找部署实例
     */
    Optional<ModelDeploymentInstance> findByServiceEndpoint(String serviceEndpoint);

    /**
     * 统计某个模型的部署实例数量
     */
    @Query("SELECT COUNT(d) FROM ModelDeploymentInstance d WHERE d.modelId = :modelId")
    long countByModelId(@Param("modelId") Long modelId);

    /**
     * 统计各状态的部署实例数量
     */
    @Query("SELECT d.status, COUNT(d) FROM ModelDeploymentInstance d GROUP BY d.status")
    List<Object[]> countByStatus();
} 