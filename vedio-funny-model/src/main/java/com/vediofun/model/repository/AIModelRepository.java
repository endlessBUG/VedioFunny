package com.vediofun.model.repository;

import com.vediofun.model.entity.AIModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * AI模型数据访问层
 */
@Repository
public interface AIModelRepository extends JpaRepository<AIModel, Long> {
    
    /**
     * 根据模型名称查找模型
     */
    Optional<AIModel> findByModelName(String modelName);
    
    /**
     * 根据模型名称和版本查找模型
     */
    Optional<AIModel> findByModelNameAndVersion(String modelName, String version);
    
    /**
     * 根据状态查找模型列表
     */
    List<AIModel> findByStatus(AIModel.ModelStatus status);
    
    /**
     * 根据模型类型查找模型列表
     */
    List<AIModel> findByModelType(String modelType);
    
    /**
     * 查找活跃状态的模型
     */
    @Query("SELECT m FROM AIModel m WHERE m.status = 'ACTIVE'")
    List<AIModel> findActiveModels();
    
    /**
     * 根据模型名称模糊查询
     */
    @Query("SELECT m FROM AIModel m WHERE m.modelName LIKE %:name%")
    List<AIModel> findByModelNameContaining(@Param("name") String name);
    
    /**
     * 统计各状态的模型数量
     */
    @Query("SELECT m.status, COUNT(m) FROM AIModel m GROUP BY m.status")
    List<Object[]> countByStatus();
} 