package com.vediofun.model.service;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.region.Region;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import jakarta.annotation.PostConstruct;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Slf4j
@Service
public class CosService {

    @Value("${tencent.cos.secret-id}")
    private String secretId;

    @Value("${tencent.cos.secret-key}")
    private String secretKey;

    @Value("${tencent.cos.bucket-name}")
    private String bucketName;

    @Value("${tencent.cos.region}")
    private String region;

    @Value("${tencent.cos.base-url}")
    private String baseUrl;

    private COSClient cosClient;

    @PostConstruct
    public void init() {
        COSCredentials credentials = new BasicCOSCredentials(secretId, secretKey);
        ClientConfig clientConfig = new ClientConfig(new Region(region));
        cosClient = new COSClient(credentials, clientConfig);
    }

    public String uploadFile(MultipartFile file, String directory) {
        try {
            // 生成文件名：目录/日期/UUID-原始文件名
            String fileName = generateFileName(file.getOriginalFilename(), directory);
            
            // 设置文件元数据
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());
            metadata.setContentType(file.getContentType());
            
            // 上传文件
            PutObjectRequest putObjectRequest = new PutObjectRequest(
                bucketName, 
                fileName, 
                file.getInputStream(),
                metadata
            );
            cosClient.putObject(putObjectRequest);
            
            // 返回文件访问URL
            return baseUrl + fileName;
        } catch (IOException e) {
            log.error("Failed to upload file to COS", e);
            throw new RuntimeException("Failed to upload file", e);
        }
    }

    private String generateFileName(String originalFilename, String directory) {
        String datePath = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String uuid = UUID.randomUUID().toString();
        return String.format("%s/%s/%s-%s", directory, datePath, uuid, originalFilename);
    }
} 