package com.vediofun.common.utils;

import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

/**
 * Redis连接测试工具
 * 
 * @author VedioFun Team
 */
@Component
public class RedisConnectionTest {

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private RedisUtil redisUtil;

    /**
     * 测试Redis连接
     * @return 测试结果
     */
    public String testConnection() {
        try {
            String testKey = "vedio-fun:test:connection";
            String testValue = "Connection test at " + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            
            // 测试基本的set和get操作
            redisUtil.set(testKey, testValue, 60, TimeUnit.SECONDS); // 设置60秒过期
            Object result = redisUtil.get(testKey);
            
            if (result != null && result.equals(testValue)) {
                return "✅ Redis连接测试成功！服务器: js2.blockelite.cn:10659";
            } else {
                return "❌ Redis连接测试失败：数据不一致";
            }
        } catch (Exception e) {
            return "❌ Redis连接测试失败：" + e.getMessage();
        }
    }

    /**
     * 测试Redis各种数据类型操作
     * @return 测试结果
     */
    public String testDataTypes() {
        try {
            StringBuilder result = new StringBuilder();
            String prefix = "vedio-fun:test:";
            
            // 测试String类型
            String stringKey = prefix + "string";
            redisUtil.set(stringKey, "Hello VedioFun", 60, TimeUnit.SECONDS);
            result.append("String测试: ").append(redisUtil.get(stringKey)).append("\n");
            
            // 测试Hash类型
            String hashKey = prefix + "hash";
            redisUtil.hSet(hashKey, "service", "model-service");
            redisUtil.hSet(hashKey, "version", "1.0.0");
            redisUtil.hSet(hashKey, "timestamp", System.currentTimeMillis());
            result.append("Hash测试: ").append(redisUtil.hGetAll(hashKey)).append("\n");
            
            // 测试List类型
            String listKey = prefix + "list";
            redisUtil.listRightPush(listKey, "item1");
            redisUtil.listRightPush(listKey, "item2");
            redisUtil.listRightPush(listKey, "item3");
            result.append("List测试: ").append(redisUtil.listRange(listKey, 0, -1)).append("\n");
            
            // 测试Set类型
            String setKey = prefix + "set";
            redisUtil.setAdd(setKey, "user1");
            redisUtil.setAdd(setKey, "user2");
            redisUtil.setAdd(setKey, "user3");
            result.append("Set测试: ").append(redisUtil.setMembers(setKey)).append("\n");
            
            // 测试过期时间
            redisUtil.expire(stringKey, 30, TimeUnit.SECONDS);
            result.append("过期时间测试: ").append(redisUtil.getExpire(stringKey) / 1000).append("秒\n");
            
            return "✅ Redis数据类型测试成功！\n" + result.toString();
        } catch (Exception e) {
            return "❌ Redis数据类型测试失败：" + e.getMessage();
        }
    }

    /**
     * 清理测试数据
     */
    public void cleanupTestData() {
        try {
            // 删除已知的测试key
            redisUtil.delete("vedio-fun:test:connection");
            redisUtil.delete("vedio-fun:test:string");
            redisUtil.delete("vedio-fun:test:hash");
            redisUtil.delete("vedio-fun:test:list");
            redisUtil.delete("vedio-fun:test:set");
        } catch (Exception e) {
            // 忽略清理错误
        }
    }

    /**
     * 获取Redis连接信息
     * @return 连接信息
     */
    public String getConnectionInfo() {
        try {
            return "Redis服务器: js2.blockelite.cn:10659\n" +
                   "连接状态: " + (testConnection().contains("成功") ? "已连接" : "连接失败") + "\n" +
                   "测试时间: " + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (Exception e) {
            return "获取连接信息失败：" + e.getMessage();
        }
    }
} 