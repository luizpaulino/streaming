package com.streaming.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Service
public class StreamingService {


    @Value("${aws.s3.region}")
    private String region;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Value("${aws.s3.file-format}")
    private String fileFormat;

    @Value("${aggregator.service.base-url}")
    private String externalServiceBaseUrl;

    @Value("${aggregator.service.path}")
    private String externalServicePath;

    @Autowired
    private WebClient.Builder webClientBuilder;

    @Autowired
    private DefaultResourceLoader resourceLoader;

    @Autowired
    private ReactiveRedisOperations<String, String> redisOps;

    public Mono<Resource> getVideo(String idVideo, String range, String cookie) {
        String urlVideo = "https://" + bucketName + ".s3." + region + ".amazonaws.com/" + idVideo + "." + fileFormat;

        String redisKey = cookie + idVideo;

        redisOps.opsForValue().get(redisKey)
                .doOnSuccess(id -> {
                    if (id == null) {
                        redisOps.opsForValue().set(redisKey, idVideo, Duration.ofMinutes(300)).subscribe();
                        WebClient webClient = webClientBuilder
                                .baseUrl(externalServiceBaseUrl)
                                .build();
                        webClient.post()
                                .uri(externalServicePath)
                                .contentType(MediaType.APPLICATION_JSON)
                                .retrieve()
                                .bodyToMono(Void.class)
                                .subscribe();
                    }
                }).subscribe();

        return Mono.fromSupplier(() -> resourceLoader.getResource(urlVideo));
    }


}