package com.vediofun.model.repository;

import com.vediofun.model.entity.Model;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ModelRepository extends JpaRepository<Model, Long>, JpaSpecificationExecutor<Model> {
    // 添加自定义查询方法
    boolean existsByName(String name);
} 