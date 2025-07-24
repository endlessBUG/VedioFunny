package com.vediofun.common.utils;

import org.redisson.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Redisson功能示例
 * 展示各种分布式功能的使用方法
 * 
 * @author VedioFun Team
 */
@Component
public class RedissonExample {

    @Autowired
    private RedisUtil redisUtil;

    /**
     * 分布式锁示例
     */
    public void distributedLockExample() {
        String lockKey = "vedio-fun:lock:resource";
        RLock lock = redisUtil.getLock(lockKey);
        
        try {
            // 尝试获取锁，最多等待3秒，锁定30秒后自动释放
            if (lock.tryLock(3, 30, TimeUnit.SECONDS)) {
                System.out.println("✅ 获取分布式锁成功");
                
                // 执行业务逻辑
                Thread.sleep(1000); // 模拟业务处理
                
                System.out.println("✅ 业务处理完成");
            } else {
                System.out.println("❌ 获取分布式锁失败");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            // 释放锁
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
                System.out.println("✅ 分布式锁已释放");
            }
        }
    }

    /**
     * 分布式限流示例
     */
    public void rateLimiterExample() {
        String limiterKey = "vedio-fun:limiter:api";
        
        // 初始化限流器：每秒最多10个请求
        redisUtil.initRateLimiter(limiterKey, 10, 1, TimeUnit.SECONDS);
        
        // 尝试获取许可
        if (redisUtil.tryAcquire(limiterKey)) {
            System.out.println("✅ 获取API调用许可成功");
            // 执行API调用逻辑
        } else {
            System.out.println("❌ API调用频率过高，请稍后重试");
        }
    }

    /**
     * 分布式计数器示例
     */
    public void atomicCounterExample() {
        String counterKey = "vedio-fun:counter:page-views";
        
        // 增加页面访问计数
        long count = redisUtil.increment(counterKey);
        System.out.println("页面访问次数: " + count);
        
        // 获取原子长整型对象
        RAtomicLong atomicLong = redisUtil.getAtomicLong(counterKey);
        long currentValue = atomicLong.get();
        System.out.println("当前计数值: " + currentValue);
        
        // 设置初始值（如果不存在）
        atomicLong.compareAndSet(0, 1000);
    }

    /**
     * 分布式信号量示例
     */
    public void semaphoreExample() {
        String semaphoreKey = "vedio-fun:semaphore:concurrent-tasks";
        RSemaphore semaphore = redisUtil.getSemaphore(semaphoreKey);
        
        try {
            // 设置信号量许可数（最多5个并发任务）
            semaphore.trySetPermits(5);
            
            // 尝试获取许可
            if (semaphore.tryAcquire(1, TimeUnit.SECONDS)) {
                System.out.println("✅ 获取并发任务许可成功");
                
                // 执行任务
                Thread.sleep(2000); // 模拟任务执行
                
                System.out.println("✅ 任务执行完成");
            } else {
                System.out.println("❌ 并发任务数已达上限");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            // 释放许可
            semaphore.release();
            System.out.println("✅ 并发任务许可已释放");
        }
    }

    /**
     * 分布式队列示例
     */
    public void queueExample() {
        String queueKey = "vedio-fun:queue:tasks";
        RQueue<String> queue = redisUtil.getClient().getQueue(queueKey);
        
        // 添加任务到队列
        queue.offer("任务1：处理视频上传");
        queue.offer("任务2：生成缩略图");
        queue.offer("任务3：推送通知");
        
        System.out.println("队列大小: " + queue.size());
        
        // 从队列取出任务
        String task = queue.poll();
        if (task != null) {
            System.out.println("处理任务: " + task);
        }
    }

    /**
     * 发布订阅示例
     */
    public void publishSubscribeExample() {
        String topicName = "vedio-fun:notifications";
        
        // 发布消息
        long clientsReceived = redisUtil.publish(topicName, "新用户注册通知");
        System.out.println("消息已发送到 " + clientsReceived + " 个客户端");
        
        // 订阅消息（通常在另一个服务中）
        RTopic topic = redisUtil.getTopic(topicName);
        topic.addListener(String.class, (channel, message) -> {
            System.out.println("收到消息: " + message);
            // 处理通知逻辑
        });
    }

    /**
     * 分布式Map示例
     */
    public void distributedMapExample() {
        String mapKey = "vedio-fun:map:user-sessions";
        RMap<String, Object> map = redisUtil.getClient().getMap(mapKey);
        
        // 添加用户会话
        map.put("user:1001", "session-abc123");
        map.put("user:1002", "session-def456");
        
        // 设置过期时间
        map.expire(30, TimeUnit.MINUTES);
        
        // 检查用户是否在线
        boolean isOnline = map.containsKey("user:1001");
        System.out.println("用户1001是否在线: " + isOnline);
        
        // 获取在线用户数
        int onlineUsers = map.size();
        System.out.println("当前在线用户数: " + onlineUsers);
    }

    /**
     * 综合示例：用户签到功能
     */
    public void userCheckInExample(String userId) {
        String todayKey = "vedio-fun:checkin:today:" + 
                         java.time.LocalDate.now().toString();
        String userCounterKey = "vedio-fun:checkin:counter:" + userId;
        
        // 使用分布式锁防止重复签到
        String lockKey = "vedio-fun:lock:checkin:" + userId;
        RLock lock = redisUtil.getLock(lockKey);
        
        try {
            if (lock.tryLock(1, 10, TimeUnit.SECONDS)) {
                // 检查今天是否已签到
                if (redisUtil.setContains(todayKey, userId)) {
                    System.out.println("❌ 今天已经签到过了");
                    return;
                }
                
                // 添加到今日签到集合
                redisUtil.setAdd(todayKey, userId);
                // 设置当天过期
                redisUtil.expire(todayKey, 24, TimeUnit.HOURS);
                
                // 增加用户签到计数
                long totalCheckIns = redisUtil.increment(userCounterKey);
                
                System.out.println("✅ 签到成功！累计签到 " + totalCheckIns + " 天");
                
                // 发布签到事件
                redisUtil.publish("vedio-fun:events:checkin", 
                                "用户 " + userId + " 签到成功");
                
            } else {
                System.out.println("❌ 签到请求过于频繁，请稍后重试");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
} 