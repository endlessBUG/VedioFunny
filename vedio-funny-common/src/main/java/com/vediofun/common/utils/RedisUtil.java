package com.vediofun.common.utils;

import org.redisson.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Redisson Redis工具类
 * 提供分布式缓存、分布式锁、分布式集合等功能
 * 
 * @author VedioFun Team
 */
@Component
public class RedisUtil {

    @Autowired
    private RedissonClient redissonClient;

    // =============================基础操作============================

    /**
     * 获取RedissonClient实例
     */
    public RedissonClient getClient() {
        return redissonClient;
    }

    /**
     * 判断key是否存在
     */
    public boolean hasKey(String key) {
        RBucket<Object> bucket = redissonClient.getBucket(key);
        return bucket.isExists();
    }

    /**
     * 删除key
     */
    public boolean delete(String key) {
        return redissonClient.getBucket(key).delete();
    }

    /**
     * 批量删除key
     */
    public long delete(Collection<String> keys) {
        return redissonClient.getKeys().delete(keys.toArray(new String[0]));
    }

    /**
     * 设置key的过期时间
     */
    public boolean expire(String key, long timeout, TimeUnit timeUnit) {
        RBucket<Object> bucket = redissonClient.getBucket(key);
        return bucket.expire(Duration.ofMillis(timeUnit.toMillis(timeout)));
    }

    /**
     * 获取key的剩余过期时间（毫秒）
     */
    public long getExpire(String key) {
        RBucket<Object> bucket = redissonClient.getBucket(key);
        return bucket.remainTimeToLive();
    }

    // =============================String操作============================

    /**
     * 普通缓存获取
     */
    public <T> T get(String key, Class<T> clazz) {
        RBucket<T> bucket = redissonClient.getBucket(key);
        return bucket.get();
    }

    /**
     * 普通缓存获取（Object类型）
     */
    public Object get(String key) {
        return get(key, Object.class);
    }

    /**
     * 普通缓存放入
     */
    public <T> void set(String key, T value) {
        RBucket<T> bucket = redissonClient.getBucket(key);
        bucket.set(value);
    }

    /**
     * 普通缓存放入并设置时间
     */
    public <T> void set(String key, T value, long timeout, TimeUnit timeUnit) {
        RBucket<T> bucket = redissonClient.getBucket(key);
        bucket.set(value, timeout, timeUnit);
    }

    /**
     * 普通缓存放入并设置时间（秒）
     */
    public <T> void set(String key, T value, long seconds) {
        set(key, value, seconds, TimeUnit.SECONDS);
    }

    /**
     * 如果key不存在则设置
     */
    public <T> boolean setIfAbsent(String key, T value) {
        RBucket<T> bucket = redissonClient.getBucket(key);
        return bucket.trySet(value);
    }

    /**
     * 如果key不存在则设置并指定过期时间
     */
    public <T> boolean setIfAbsent(String key, T value, long timeout, TimeUnit timeUnit) {
        RBucket<T> bucket = redissonClient.getBucket(key);
        return bucket.trySet(value, timeout, timeUnit);
    }

    /**
     * 递增
     */
    public long increment(String key) {
        RAtomicLong atomicLong = redissonClient.getAtomicLong(key);
        return atomicLong.incrementAndGet();
    }

    /**
     * 增加指定值
     */
    public long increment(String key, long delta) {
        RAtomicLong atomicLong = redissonClient.getAtomicLong(key);
        return atomicLong.addAndGet(delta);
    }

    /**
     * 递减
     */
    public long decrement(String key) {
        RAtomicLong atomicLong = redissonClient.getAtomicLong(key);
        return atomicLong.decrementAndGet();
    }

    /**
     * 减少指定值
     */
    public long decrement(String key, long delta) {
        RAtomicLong atomicLong = redissonClient.getAtomicLong(key);
        return atomicLong.addAndGet(-delta);
    }

    // =============================Hash操作============================

    /**
     * 获取Hash中的值
     */
    public <T> T hGet(String key, String field, Class<T> clazz) {
        RMap<String, T> map = redissonClient.getMap(key);
        return map.get(field);
    }

    /**
     * 获取Hash中的值（Object类型）
     */
    public Object hGet(String key, String field) {
        return hGet(key, field, Object.class);
    }

    /**
     * 设置Hash中的值
     */
    public <T> void hSet(String key, String field, T value) {
        RMap<String, T> map = redissonClient.getMap(key);
        map.put(field, value);
    }

    /**
     * 获取整个Hash
     */
    public <T> Map<String, T> hGetAll(String key) {
        RMap<String, T> map = redissonClient.getMap(key);
        return map.readAllMap();
    }

    /**
     * 设置整个Hash
     */
    public <T> void hSetAll(String key, Map<String, T> hash) {
        RMap<String, T> map = redissonClient.getMap(key);
        map.putAll(hash);
    }

    /**
     * 删除Hash中的字段
     */
    public long hDelete(String key, String... fields) {
        RMap<String, Object> map = redissonClient.getMap(key);
        return map.fastRemove(fields);
    }

    /**
     * 判断Hash中是否存在字段
     */
    public boolean hExists(String key, String field) {
        RMap<String, Object> map = redissonClient.getMap(key);
        return map.containsKey(field);
    }

    /**
     * 获取Hash的大小
     */
    public int hSize(String key) {
        RMap<String, Object> map = redissonClient.getMap(key);
        return map.size();
    }

    // =============================List操作============================

    /**
     * 获取List
     */
    public <T> RList<T> getList(String key) {
        return redissonClient.getList(key);
    }

    /**
     * 在List右侧添加元素
     */
    public <T> void listRightPush(String key, T value) {
        RList<T> list = redissonClient.getList(key);
        list.add(value);
    }

    /**
     * 在List左侧添加元素
     */
    public <T> void listLeftPush(String key, T value) {
        RList<T> list = redissonClient.getList(key);
        list.add(0, value);
    }

    /**
     * 从List右侧弹出元素
     */
    public <T> T listRightPop(String key) {
        RList<T> list = redissonClient.getList(key);
        if (list.isEmpty()) {
            return null;
        }
        return list.remove(list.size() - 1);
    }

    /**
     * 从List左侧弹出元素
     */
    public <T> T listLeftPop(String key) {
        RList<T> list = redissonClient.getList(key);
        if (list.isEmpty()) {
            return null;
        }
        return list.remove(0);
    }

    /**
     * 获取List指定范围的元素
     */
    public <T> List<T> listRange(String key, int start, int end) {
        RList<T> list = redissonClient.getList(key);
        return list.range(start, end);
    }

    /**
     * 获取List大小
     */
    public int listSize(String key) {
        RList<Object> list = redissonClient.getList(key);
        return list.size();
    }

    // =============================Set操作============================

    /**
     * 获取Set
     */
    public <T> RSet<T> getSet(String key) {
        return redissonClient.getSet(key);
    }

    /**
     * 向Set添加元素
     */
    public <T> boolean setAdd(String key, T value) {
        RSet<T> set = redissonClient.getSet(key);
        return set.add(value);
    }

    /**
     * 从Set删除元素
     */
    public <T> boolean setRemove(String key, T value) {
        RSet<T> set = redissonClient.getSet(key);
        return set.remove(value);
    }

    /**
     * 判断Set中是否存在元素
     */
    public <T> boolean setContains(String key, T value) {
        RSet<T> set = redissonClient.getSet(key);
        return set.contains(value);
    }

    /**
     * 获取Set的所有元素
     */
    public <T> Set<T> setMembers(String key) {
        RSet<T> set = redissonClient.getSet(key);
        return set.readAll();
    }

    /**
     * 获取Set大小
     */
    public int setSize(String key) {
        RSet<Object> set = redissonClient.getSet(key);
        return set.size();
    }

    // =============================分布式锁============================

    /**
     * 获取分布式锁
     */
    public RLock getLock(String key) {
        return redissonClient.getLock(key);
    }

    /**
     * 获取公平锁
     */
    public RLock getFairLock(String key) {
        return redissonClient.getFairLock(key);
    }

    /**
     * 获取读写锁
     */
    public RReadWriteLock getReadWriteLock(String key) {
        return redissonClient.getReadWriteLock(key);
    }

    /**
     * 尝试获取锁
     */
    public boolean tryLock(String key, long waitTime, long leaseTime, TimeUnit unit) {
        RLock lock = getLock(key);
        try {
            return lock.tryLock(waitTime, leaseTime, unit);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    /**
     * 释放锁
     */
    public void unlock(String key) {
        RLock lock = getLock(key);
        if (lock.isHeldByCurrentThread()) {
            lock.unlock();
        }
    }

    // =============================分布式对象============================

    /**
     * 获取分布式原子长整型
     */
    public RAtomicLong getAtomicLong(String key) {
        return redissonClient.getAtomicLong(key);
    }

    /**
     * 获取分布式原子双精度浮点型
     */
    public RAtomicDouble getAtomicDouble(String key) {
        return redissonClient.getAtomicDouble(key);
    }

    /**
     * 获取分布式计数信号量
     */
    public RSemaphore getSemaphore(String key) {
        return redissonClient.getSemaphore(key);
    }

    /**
     * 获取分布式闭锁
     */
    public RCountDownLatch getCountDownLatch(String key) {
        return redissonClient.getCountDownLatch(key);
    }

    // =============================发布订阅============================

    /**
     * 获取主题
     */
    public RTopic getTopic(String topicName) {
        return redissonClient.getTopic(topicName);
    }

    /**
     * 发布消息
     */
    public long publish(String topicName, Object message) {
        RTopic topic = redissonClient.getTopic(topicName);
        return topic.publish(message);
    }

    // =============================限流器============================

    /**
     * 获取限流器
     */
    public RRateLimiter getRateLimiter(String key) {
        return redissonClient.getRateLimiter(key);
    }

    /**
     * 初始化限流器
     */
    public void initRateLimiter(String key, long rate, long rateInterval, TimeUnit unit) {
        RRateLimiter rateLimiter = getRateLimiter(key);
        RateIntervalUnit rateIntervalUnit = RateIntervalUnit.valueOf(unit.name());
        rateLimiter.trySetRate(RateType.OVERALL, rate, rateInterval, rateIntervalUnit);
    }

    /**
     * 尝试获取许可
     */
    public boolean tryAcquire(String key) {
        RRateLimiter rateLimiter = getRateLimiter(key);
        return rateLimiter.tryAcquire();
    }

    /**
     * 尝试获取指定数量的许可
     */
    public boolean tryAcquire(String key, long permits) {
        RRateLimiter rateLimiter = getRateLimiter(key);
        return rateLimiter.tryAcquire(permits);
    }
} 