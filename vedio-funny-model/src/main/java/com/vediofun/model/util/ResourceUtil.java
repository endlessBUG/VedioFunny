package com.vediofun.model.util;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * 资源文件工具类
 * 用于从classpath中加载配置文件、脚本等资源
 */
@Slf4j
@Component
public class ResourceUtil {
    

    
    /**
     * 获取VedioFunny应用的基础目录
     * 使用用户主目录下的vedio-funny子目录，跨平台兼容
     */
    private String getVedioFunnyBaseDir() {
        String userHome = System.getProperty("user.home");
        return Paths.get(userHome, "vedio-funny").toString();
    }
    
    /**
     * 获取classpath中脚本的绝对路径
     * 将classpath中的脚本复制到临时目录并返回绝对路径
     */
    public String getScriptPath(String scriptName) {
        try {
            // 从classpath加载脚本资源
            Resource resource = new ClassPathResource("scripts/" + scriptName);
            
            if (!resource.exists()) {
                log.error("脚本文件不存在: scripts/{}", scriptName);
                return null;
            }
            
            // 创建固定目录
            Path tempDir = Paths.get(getVedioFunnyBaseDir(), "scripts");
            Files.createDirectories(tempDir);
            
            // 复制脚本到临时目录
            Path tempScript = tempDir.resolve(scriptName);
            try (InputStream inputStream = resource.getInputStream()) {
                Files.copy(inputStream, tempScript, StandardCopyOption.REPLACE_EXISTING);
            }
            
            // 设置执行权限
            tempScript.toFile().setExecutable(true);
            
            log.debug("脚本已复制到临时目录: {}", tempScript.toString());
            return tempScript.toString();
            
        } catch (IOException e) {
            log.error("获取脚本路径失败: {}", scriptName, e);
            return null;
        }
    }
    

    
    /**
     * 创建包含RAY_ENV_FILE环境变量的ProcessBuilder
     * 用于执行脚本时传递正确的ray.env文件路径
     */
    public ProcessBuilder createScriptProcessBuilder(String scriptPath, String... args) {
        java.util.List<String> command = new java.util.ArrayList<>();
        command.add("bash");
        command.add("-c");
        
        // 构建完整命令，包括导出环境变量
        StringBuilder fullCommand = new StringBuilder();
        
        // 获取ray.env文件路径并设置环境变量
        String rayEnvPath = getRayEnvPath();
        if (rayEnvPath != null) {
            fullCommand.append("export RAY_ENV_FILE='").append(rayEnvPath).append("' && ");
        }
        
        // 获取installers目录路径并设置环境变量
        String installersPath = getInstallersDirectoryPath();
        if (installersPath != null) {
            fullCommand.append("export INSTALLERS_DIR='").append(installersPath).append("' && ");
        }
        
        // 添加脚本执行命令
        fullCommand.append(scriptPath);
        for (String arg : args) {
            fullCommand.append(" ").append(arg);
        }
        
        command.add(fullCommand.toString());
        
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);
        
        log.debug("创建脚本进程: {}", fullCommand.toString());
        return pb;
    }
    
    /**
     * 获取ray.env配置文件的绝对路径
     */
    public String getRayEnvPath() {
        try {
            Resource resource = new ClassPathResource("ray.env");
            
            if (!resource.exists()) {
                log.error("ray.env文件不存在");
                return null;
            }
            
            // 创建固定目录
            Path tempDir = Paths.get(getVedioFunnyBaseDir(), "config");
            Files.createDirectories(tempDir);
            
            // 复制配置文件到固定目录
            Path tempEnvFile = tempDir.resolve("ray.env");
            try (InputStream inputStream = resource.getInputStream()) {
                Files.copy(inputStream, tempEnvFile, StandardCopyOption.REPLACE_EXISTING);
            }
            
            log.debug("ray.env已复制到固定目录: {}", tempEnvFile.toString());
            return tempEnvFile.toString();
            
        } catch (IOException e) {
            log.error("获取ray.env路径失败", e);
            return null;
        }
    }
    
    /**
     * 获取配置文件的绝对路径
     */
    public String getConfigPath(String configName) {
        try {
            Resource resource = new ClassPathResource("config/" + configName);
            
            if (!resource.exists()) {
                log.error("配置文件不存在: config/{}", configName);
                return null;
            }
            
            // 创建固定目录
            Path tempDir = Paths.get(getVedioFunnyBaseDir(), "config");
            Files.createDirectories(tempDir);
            
            // 复制配置文件到固定目录
            Path tempConfigFile = tempDir.resolve(configName);
            try (InputStream inputStream = resource.getInputStream()) {
                Files.copy(inputStream, tempConfigFile, StandardCopyOption.REPLACE_EXISTING);
            }
            
            // 设置执行权限（如果是shell脚本）
            if (configName.endsWith(".sh")) {
                tempConfigFile.toFile().setExecutable(true);
            }
            
            log.debug("配置文件已复制到固定目录: {}", tempConfigFile.toString());
            return tempConfigFile.toString();
            
        } catch (IOException e) {
            log.error("获取配置文件路径失败: {}", configName, e);
            return null;
        }
    }
    
    /**
     * 获取installers目录的绝对路径
     * 统一使用临时目录风格，确保JAR兼容性
     */
    public String getInstallersDirectoryPath() {
        try {
            // 创建固定目录
            Path tempDir = Paths.get(getVedioFunnyBaseDir(), "installers");
            Files.createDirectories(tempDir);
            
            // 检查目录是否已有文件，避免重复复制
            if (tempDir.toFile().list().length > 0) {
                log.debug("使用已存在的installers目录: {}", tempDir.toString());
                return tempDir.toString();
            }
            
            // 从classpath复制installers目录到固定目录
            Resource installersResource = new ClassPathResource("installers");
            if (installersResource.exists()) {
                copyResourceDirectory("installers", tempDir);
                log.debug("installers目录已复制到固定目录: {}", tempDir.toString());
                return tempDir.toString();
            }
            
            log.error("installers目录不存在于classpath中");
            return null;
            
        } catch (Exception e) {
            log.error("获取installers目录路径失败", e);
            return null;
        }
    }
    
    /**
     * 复制classpath中的目录到指定路径
     */
    private void copyResourceDirectory(String resourcePath, Path targetDir) throws IOException {
        // 这里简化处理，实际可能需要更复杂的目录复制逻辑
        // 对于常见的installer文件，我们可以逐个复制
        String[] installerFiles = {
            "Miniconda3-latest-Linux-x86_64.sh",
            "Miniconda3-latest-Linux-aarch64.sh", 
            "Miniconda3-latest-MacOSX-x86_64.sh",
            "Miniconda3-latest-MacOSX-arm64.sh"
        };
        
        for (String fileName : installerFiles) {
            try {
                Resource fileResource = new ClassPathResource(resourcePath + "/" + fileName);
                if (fileResource.exists()) {
                    Path targetFile = targetDir.resolve(fileName);
                    try (InputStream inputStream = fileResource.getInputStream()) {
                        Files.copy(inputStream, targetFile, StandardCopyOption.REPLACE_EXISTING);
                        targetFile.toFile().setExecutable(true);
                    }
                    log.debug("已复制安装包: {}", fileName);
                }
            } catch (Exception e) {
                log.debug("跳过不存在的安装包: {}", fileName);
            }
        }
    }
    
    /**
     * 从classpath读取ray.env配置并解析为Map
     */
    public java.util.Map<String, String> loadRayEnvConfig() {
        java.util.Map<String, String> config = new java.util.HashMap<>();
        
        try {
            Resource resource = new ClassPathResource("ray.env");
            
            if (!resource.exists()) {
                log.warn("ray.env配置文件不存在");
                return config;
            }
            
            try (InputStream inputStream = resource.getInputStream();
                 java.io.BufferedReader reader = new java.io.BufferedReader(
                     new java.io.InputStreamReader(inputStream))) {
                
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    
                    // 跳过注释和空行
                    if (line.isEmpty() || line.startsWith("#")) {
                        continue;
                    }
                    
                    // 解析 KEY=VALUE 格式
                    int equalIndex = line.indexOf('=');
                    if (equalIndex > 0) {
                        String key = line.substring(0, equalIndex).trim();
                        String value = line.substring(equalIndex + 1).trim();
                        
                        // 移除引号
                        if (value.startsWith("\"") && value.endsWith("\"")) {
                            value = value.substring(1, value.length() - 1);
                        }
                        
                        // 简单的变量替换
                        value = value.replace("${HOME}", System.getProperty("user.home"));
                        value = value.replace("${HOSTNAME}", getHostname());
                        
                        config.put(key, value);
                    }
                }
            }
            
            log.debug("成功加载ray.env配置，包含{}个配置项", config.size());
            
        } catch (IOException e) {
            log.error("加载ray.env配置失败", e);
        }
        
        return config;
    }
    
    /**
     * 验证所有资源是否可用
     */
    public boolean validateResources() {
        boolean allValid = true;
        
        // 验证ray.env
        Resource rayEnv = new ClassPathResource("ray.env");
        if (!rayEnv.exists()) {
            log.error("ray.env配置文件不存在");
            allValid = false;
        } else {
            log.info("✅ ray.env配置文件存在");
        }
        
        // 验证关键脚本
        String[] criticalScripts = {
            "install-miniconda.sh",
            "setup-ray-env.sh", 
            "ray-env-check.sh",
            "start-ray-cluster.sh",
            "stop-ray-cluster.sh"
        };
        
        for (String script : criticalScripts) {
            Resource scriptResource = new ClassPathResource("scripts/" + script);
            if (!scriptResource.exists()) {
                log.error("关键脚本不存在: scripts/{}", script);
                allValid = false;
            } else {
                log.info("✅ 脚本存在: {}", script);
            }
        }
        
        // 验证配置文件
        Resource configResource = new ClassPathResource("config/miniconda-env.sh");
        if (!configResource.exists()) {
            log.error("配置文件不存在: config/miniconda-env.sh");
            allValid = false;
        } else {
            log.info("✅ 配置文件存在: miniconda-env.sh");
        }
        
        log.info("资源验证结果: {}", allValid ? "所有资源正常" : "部分资源缺失");
        return allValid;
    }
    
    private String getHostname() {
        try {
            return java.net.InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            return "localhost";
        }
    }
} 