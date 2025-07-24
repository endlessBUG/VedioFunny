package com.vediofun.model.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * ModelScope模型下载服务
 * 支持下载完整的模型目录结构
 */
@Slf4j
@Service
public class ModelScopeDownloadService {
    
    private static final String MODELSCOPE_API_BASE = "https://www.modelscope.cn/api/v1/models";
    private static final String MODELSCOPE_FILES_API = "https://www.modelscope.cn/api/v1/models/%s/repo/files";
    private static final String MODELSCOPE_DOWNLOAD_BASE = "https://www.modelscope.cn/api/v1/models/%s/repo?Revision=master&FilePath=%s";
    
    private final ExecutorService executorService = Executors.newFixedThreadPool(4);
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * 下载ModelScope模型
     */
    public Map<String, Object> downloadModelScopeModel(String modelName, String targetDir) {
        long startTime = System.currentTimeMillis();
        Map<String, Object> result = new HashMap<>();
        List<String> downloadedFiles = new ArrayList<>();
        List<String> failedFiles = new ArrayList<>();
        
        try {
            log.info("开始下载ModelScope模型: {}", modelName);
            
            // 创建目标目录
            Path modelPath = Paths.get(targetDir, modelName.replace("/", "_"));
            Files.createDirectories(modelPath);
            
            // 获取模型文件列表
            List<ModelFile> modelFiles = getModelFilesList(modelName);
            log.info("发现 {} 个文件需要下载", modelFiles.size());
            
            // 并发下载文件
            List<CompletableFuture<DownloadResult>> downloadFutures = new ArrayList<>();
            
            for (ModelFile file : modelFiles) {
                CompletableFuture<DownloadResult> future = CompletableFuture.supplyAsync(() -> {
                    try {
                        return downloadSingleFile(modelName, file, modelPath.toString());
                    } catch (Exception e) {
                        log.error("下载文件失败: {} - {}", file.getPath(), e.getMessage());
                        return new DownloadResult(file.getPath(), false, e.getMessage());
                    }
                }, executorService);
                downloadFutures.add(future);
            }
            
            // 等待所有下载完成
            for (CompletableFuture<DownloadResult> future : downloadFutures) {
                try {
                    DownloadResult downloadResult = future.get();
                    if (downloadResult.isSuccess()) {
                        downloadedFiles.add(downloadResult.getFilePath());
                    } else {
                        failedFiles.add(downloadResult.getFilePath() + ": " + downloadResult.getError());
                    }
                } catch (Exception e) {
                    log.error("获取下载结果失败: {}", e.getMessage());
                    failedFiles.add("Unknown file: " + e.getMessage());
                }
            }
            
            long duration = System.currentTimeMillis() - startTime;
            
            result.put("status", "success");
            result.put("message", "ModelScope模型下载完成");
            result.put("modelName", modelName);
            result.put("downloadPath", modelPath.toString());
            result.put("totalFiles", modelFiles.size());
            result.put("successCount", downloadedFiles.size());
            result.put("failedCount", failedFiles.size());
            result.put("downloadedFiles", downloadedFiles);
            result.put("failedFiles", failedFiles);
            result.put("downloadTime", duration / 1000.0 + " seconds");
            
            log.info("ModelScope模型下载完成 - 模型: {}, 成功: {}, 失败: {}, 耗时: {}ms", 
                    modelName, downloadedFiles.size(), failedFiles.size(), duration);
            
        } catch (Exception e) {
            log.error("ModelScope模型下载失败", e);
            result.put("status", "error");
            result.put("message", "下载失败: " + e.getMessage());
            result.put("error", e.getClass().getSimpleName());
        }
        
        return result;
    }
    
    /**
     * 获取模型文件列表
     */
    private List<ModelFile> getModelFilesList(String modelName) throws IOException {
        String apiUrl = String.format(MODELSCOPE_FILES_API, modelName);
        log.info("获取模型文件列表: {}", apiUrl);
        
        HttpURLConnection connection = (HttpURLConnection) new URL(apiUrl).openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("User-Agent", "VedioFun-Model-Downloader/1.0");
        connection.setConnectTimeout(30000);
        connection.setReadTimeout(60000);
        
        List<ModelFile> files = new ArrayList<>();
        
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(connection.getInputStream()))) {
            
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            
            // 解析JSON响应
            JsonNode rootNode = objectMapper.readTree(response.toString());
            JsonNode dataNode = rootNode.get("Data");
            
            if (dataNode != null && dataNode.isArray()) {
                for (JsonNode fileNode : dataNode) {
                    String path = fileNode.get("Path").asText();
                    String type = fileNode.get("Type").asText();
                    long size = fileNode.get("Size").asLong(0);
                    
                    // 只下载文件，跳过目录
                    if (!"tree".equals(type)) {
                        files.add(new ModelFile(path, size, type));
                    }
                }
            }
        }
        
        // 如果API失败，创建常见的模型文件列表
        if (files.isEmpty()) {
            log.warn("无法获取文件列表，使用默认文件列表");
            files = getDefaultModelFiles();
        }
        
        return files;
    }
    
    /**
     * 下载单个文件
     */
    private DownloadResult downloadSingleFile(String modelName, ModelFile file, String targetDir) {
        try {
            String downloadUrl = String.format(MODELSCOPE_DOWNLOAD_BASE, modelName, file.getPath());
            Path filePath = Paths.get(targetDir, file.getPath());
            
            // 创建父目录
            Files.createDirectories(filePath.getParent());
            
            // 检查文件是否已存在
            if (Files.exists(filePath)) {
                long localFileSize = Files.size(filePath);
                log.info("文件已存在，跳过下载: {} ({})",
                        file.getPath(), formatFileSize(localFileSize));
                return new DownloadResult(file.getPath(), true, "文件已存在，跳过下载");
            }
            
            log.info("下载文件: {} -> {}", downloadUrl, filePath);
            
            HttpURLConnection connection = (HttpURLConnection) new URL(downloadUrl).openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "VedioFun-Model-Downloader/1.0");
            connection.setConnectTimeout(30000);
            connection.setReadTimeout(0); // 无超时限制
            
            // 检查响应码
            int responseCode = connection.getResponseCode();
            if (responseCode != 200) {
                return new DownloadResult(file.getPath(), false, "HTTP " + responseCode);
            }
            
            long remoteFileSize = connection.getContentLengthLong();
            
            // 下载文件
            try (InputStream inputStream = connection.getInputStream();
                 FileOutputStream outputStream = new FileOutputStream(filePath.toFile());
                 BufferedInputStream bufferedInput = new BufferedInputStream(inputStream);
                 BufferedOutputStream bufferedOutput = new BufferedOutputStream(outputStream)) {
                
                byte[] buffer = new byte[8192];
                int bytesRead;
                long totalBytesRead = 0;
                long lastProgressTime = System.currentTimeMillis();
                
                while ((bytesRead = bufferedInput.read(buffer)) != -1) {
                    bufferedOutput.write(buffer, 0, bytesRead);
                    totalBytesRead += bytesRead;
                    
                    // 每5秒输出一次进度
                    long currentTime = System.currentTimeMillis();
                    if (currentTime - lastProgressTime > 5000) {
                        double progress = remoteFileSize > 0 ? (double) totalBytesRead / remoteFileSize * 100 : 0;
                        log.info("{}下载进度: {}% ({}/{})", 
                                file.getPath(), String.format("%.1f", progress), formatFileSize(totalBytesRead), 
                                remoteFileSize > 0 ? formatFileSize(remoteFileSize) : "未知");
                        lastProgressTime = currentTime;
                    }
                }
                
                log.info("文件下载完成: {} ({})", file.getPath(), formatFileSize(totalBytesRead));
                
                // 验证下载完整性
                if (remoteFileSize > 0 && totalBytesRead != remoteFileSize) {
                    log.warn("下载文件大小不匹配: {} 期望={}, 实际={}", 
                            file.getPath(), formatFileSize(remoteFileSize), formatFileSize(totalBytesRead));
                }
                
                return new DownloadResult(file.getPath(), true, null);
            }
            
        } catch (Exception e) {
            log.error("下载文件失败: {} - {}", file.getPath(), e.getMessage());
            return new DownloadResult(file.getPath(), false, e.getMessage());
        }
    }
    
    /**
     * 格式化文件大小
     */
    private String formatFileSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.1f KB", bytes / 1024.0);
        } else if (bytes < 1024 * 1024 * 1024) {
            return String.format("%.1f MB", bytes / (1024.0 * 1024));
        } else {
            return String.format("%.1f GB", bytes / (1024.0 * 1024 * 1024));
        }
    }
    
    /**
     * 获取默认模型文件列表（当API失败时使用）
     */
    private List<ModelFile> getDefaultModelFiles() {
        List<ModelFile> files = new ArrayList<>();
        files.add(new ModelFile("config.json", 1000, "blob"));
        files.add(new ModelFile("README.md", 5000, "blob"));
        files.add(new ModelFile("tokenizer.json", 10000, "blob"));
        files.add(new ModelFile("tokenizer_config.json", 2000, "blob"));
        files.add(new ModelFile("model.safetensors", 1000000000, "blob")); // 1GB 占位
        return files;
    }
    
    /**
     * 模型文件信息
     */
    private static class ModelFile {
        private final String path;
        private final long size;
        private final String type;
        
        public ModelFile(String path, long size, String type) {
            this.path = path;
            this.size = size;
            this.type = type;
        }
        
        public String getPath() { return path; }
        public long getSize() { return size; }
        public String getType() { return type; }
    }
    
    /**
     * 下载结果
     */
    private static class DownloadResult {
        private final String filePath;
        private final boolean success;
        private final String error;
        
        public DownloadResult(String filePath, boolean success, String error) {
            this.filePath = filePath;
            this.success = success;
            this.error = error;
        }
        
        public String getFilePath() { return filePath; }
        public boolean isSuccess() { return success; }
        public String getError() { return error; }
    }
} 