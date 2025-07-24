package com.vediofun.common.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vediofun.common.entity.BaseEntity;

import java.io.Serializable;
import java.util.List;

/**
 * 通用服务基类
 * 
 * @param <M> Mapper类型
 * @param <T> 实体类型
 * @author VedioFun Team
 */
public abstract class BaseService<M extends BaseMapper<T>, T extends BaseEntity> extends ServiceImpl<M, T> {

    /**
     * 根据ID查询（排除逻辑删除）
     */
    public T getByIdNotDeleted(Serializable id) {
        QueryWrapper<T> wrapper = new QueryWrapper<>();
        wrapper.eq("id", id).eq("deleted", 0);
        return this.getOne(wrapper);
    }

    /**
     * 分页查询（排除逻辑删除）
     */
    public Page<T> pageNotDeleted(Page<T> page) {
        QueryWrapper<T> wrapper = new QueryWrapper<>();
        wrapper.eq("deleted", 0).orderByDesc("create_time");
        return this.page(page, wrapper);
    }

    /**
     * 分页查询（排除逻辑删除，自定义条件）
     */
    public Page<T> pageNotDeleted(Page<T> page, QueryWrapper<T> queryWrapper) {
        queryWrapper.eq("deleted", 0);
        return this.page(page, queryWrapper);
    }

    /**
     * 查询所有（排除逻辑删除）
     */
    public List<T> listNotDeleted() {
        QueryWrapper<T> wrapper = new QueryWrapper<>();
        wrapper.eq("deleted", 0).orderByDesc("create_time");
        return this.list(wrapper);
    }

    /**
     * 查询列表（排除逻辑删除，自定义条件）
     */
    public List<T> listNotDeleted(QueryWrapper<T> queryWrapper) {
        queryWrapper.eq("deleted", 0);
        return this.list(queryWrapper);
    }

    /**
     * 逻辑删除
     */
    public boolean removeLogically(Serializable id) {
        T entity = this.getById(id);
        if (entity != null) {
            entity.setDeleted(1);
            return this.updateById(entity);
        }
        return false;
    }

    /**
     * 批量逻辑删除
     */
    public boolean removeLogicallyByIds(List<? extends Serializable> ids) {
        List<T> entities = this.listByIds(ids);
        entities.forEach(entity -> entity.setDeleted(1));
        return this.updateBatchById(entities);
    }

    /**
     * 统计数量（排除逻辑删除）
     */
    public long countNotDeleted() {
        QueryWrapper<T> wrapper = new QueryWrapper<>();
        wrapper.eq("deleted", 0);
        return this.count(wrapper);
    }

    /**
     * 统计数量（排除逻辑删除，自定义条件）
     */
    public long countNotDeleted(QueryWrapper<T> queryWrapper) {
        queryWrapper.eq("deleted", 0);
        return this.count(queryWrapper);
    }
} 