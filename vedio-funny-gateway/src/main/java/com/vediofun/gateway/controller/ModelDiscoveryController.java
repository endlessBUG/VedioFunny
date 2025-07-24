package com.vediofun.gateway.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/registry")
@RequiredArgsConstructor
public class ModelDiscoveryController {

    private final DiscoveryClient discoveryClient;

    @GetMapping("/vedio-funny-model/instances")
    public Result<List<InstanceInfo>> getModelInstances() {
        List<ServiceInstance> instances = discoveryClient.getInstances("vedio-funny-model");
        List<InstanceInfo> data = instances.stream().map(instance -> new InstanceInfo(
                instance.getInstanceId(),
                instance.getHost(),
                instance.getPort(),
                instance.isSecure() ? "SECURE" : "UP",
                instance.getMetadata()
        )).collect(Collectors.toList());
        return Result.success(data);
    }

    public record InstanceInfo(
            String instanceId,
            String ip,
            int port,
            String status,
            java.util.Map<String, String> metadata
    ) {}
} 