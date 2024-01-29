package com.streaming.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.data.redis.core.ReactiveValueOperations;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import java.time.Duration;
import static org.mockito.Mockito.*;

class StreamingServiceTest {

    @InjectMocks
    private StreamingService streamingService;

    @Mock
    private WebClient.Builder webClientBuilder;
    @Mock
    private WebClient webClient;

    @Mock
    private DefaultResourceLoader resourceLoader;

    @Mock
    private ReactiveRedisOperations<String, String> redisOps;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }
    @Test
    void testGetVideoWithCacheMiss() {
        String idVideo = "exampleVideo";
        String range = "bytes=0-100";
        String cookie = "sessionId=123";

        ReactiveRedisOperations<String, String> redisOps = Mockito.mock(ReactiveRedisOperations.class);
        ReactiveValueOperations<String, String> valueOps = Mockito.mock(ReactiveValueOperations.class);
        when(redisOps.opsForValue()).thenReturn(valueOps);
        when(valueOps.get(any())).thenReturn(Mono.empty());
        when(valueOps.set(any(), any(), any())).thenReturn(Mono.empty());
        when(valueOps.set(any(), any(), any())).thenReturn(Mono.just(true));
        when(valueOps.get(any())).thenReturn(Mono.empty());
        ReflectionTestUtils.setField(streamingService, "redisOps", redisOps);

        when(webClientBuilder.baseUrl("http://localhost:9090")).thenReturn(webClientBuilder);
        when(webClientBuilder.filter(any())).thenReturn(webClientBuilder);

        WebClient.RequestHeadersUriSpec<?> requestUriSpec = Mockito.mock(WebClient.RequestHeadersUriSpec.class);
        WebClient.ResponseSpec responseSpec = Mockito.mock(WebClient.ResponseSpec.class);
        when(requestUriSpec.retrieve()).thenReturn(responseSpec);

        WebClient.RequestBodyUriSpec requestBodyUriSpecMock = Mockito.mock(WebClient.RequestBodyUriSpec.class);

        when(webClientBuilder.build()).thenReturn(webClient);
        when(webClient.post()).thenReturn(requestBodyUriSpecMock);
        when(webClient.post().uri("/api/aggregators/watched")).thenReturn(requestBodyUriSpecMock);
        when(webClient.post().uri("/api/aggregators/watched").contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodyUriSpecMock);
        when(webClient.post().uri("/api/aggregators/watched").contentType(MediaType.APPLICATION_JSON).retrieve()).thenReturn(Mockito.mock(WebClient.ResponseSpec.class));
        when(webClient.post().uri("/api/aggregators/watched").contentType(MediaType.APPLICATION_JSON).retrieve().bodyToMono(String.class)).thenReturn(Mono.empty());


        ReflectionTestUtils.setField(streamingService, "bucketName", "example-bucket");
        ReflectionTestUtils.setField(streamingService, "region", "us-east-1");
        ReflectionTestUtils.setField(streamingService, "fileFormat", "mp4");
        ReflectionTestUtils.setField(streamingService, "externalServiceBaseUrl", "http://localhost:9090");
        ReflectionTestUtils.setField(streamingService, "externalServicePath", "/api/aggregators/watched");
        ReflectionTestUtils.setField(streamingService, "redisOps", redisOps);

        // Executar o método e verificar interações
        streamingService.getVideo(idVideo, range, cookie).block();
        verify(valueOps, times(1)).get(cookie + idVideo);
        verify(valueOps, times(1)).set(cookie + idVideo, idVideo, Duration.ofDays(10));
        verify(webClientBuilder, times(1)).baseUrl("http://localhost:9090");
        verify(webClient.post(), times(1));
    }
}

