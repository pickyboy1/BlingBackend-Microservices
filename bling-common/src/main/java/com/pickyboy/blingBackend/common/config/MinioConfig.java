package com.pickyboy.blingBackend.common.config;

import io.minio.MinioClient;
import lombok.Data;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.pickyboy.blingBackend.common.utils.MinioUtil;

import java.util.Map;

/**
 * MinIO对象存储配置类
 * 只有在启用MinIO且相关类存在时才会创建相应的Bean
 *
 * 🎯 条件说明：
 * - 需要MinioClient类在classpath中
 * - 需要配置minio.enabled=true（默认false）
 * - 需要配置minio.endpoint、minio.access-key、minio.secret-key
 *
 * 🎯 配置示例：
 * minio:
 *   enabled: true
 *   endpoint: http://localhost:9000
 *   access-key: minioadmin
 *   secret-key: minioadmin
 *   bucket:
 *     default: bling-bucket
 *
 * @author pickyboy
 */
@Data
@Configuration
@ConditionalOnClass(MinioClient.class)
@ConditionalOnProperty(prefix = "minio", name = "enabled", havingValue = "true", matchIfMissing = false)
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "minio")
public class MinioConfig {

    /**
     * 是否启用MinIO
     */
    private boolean enabled = false;

    /**
     * MinIO服务端点
     */
    private String endpoint;

    /**
     * 访问密钥
     */
    private String accessKey;

    /**
     * 秘密密钥
     */
    private String secretKey;

    /**
     * 存储桶配置
     */
    private Map<String, String> bucket;

    /**
     * 注入MinioClient实例到Spring容器
     * 只有在所有必需属性都配置的情况下才创建
     */
    @Bean
    @ConditionalOnProperty(prefix = "minio", name = {"endpoint", "access-key", "secret-key"})
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
    }

    /**
     * 注入MinioUtil工具类到Spring容器
     * 依赖于MinioClient Bean，只有在MinioClient存在时才创建
     */
    @Bean
    public MinioUtil minioUtil(MinioClient minioClient) {
        return new MinioUtil(minioClient, this);
    }
}
