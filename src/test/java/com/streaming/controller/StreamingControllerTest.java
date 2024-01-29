package com.streaming.controller;

import com.streaming.service.StreamingService;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;

import static org.junit.jupiter.api.Assertions.assertTrue;
@WebFluxTest(StreamingController.class)
class StreamingControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private StreamingService streamingService;

    @Test
    void testGetVideo() {
        String idVideo = "exampleVideo";
        String range = "bytes=0-100";
        String cookie = "sessionId=123";

        byte[] videoContent = "Sample video content".getBytes();
        Resource videoResource = new ByteArrayResource(videoContent);

        when(streamingService.getVideo(idVideo, range, cookie)).thenReturn(Mono.just(videoResource));

        webTestClient.get()
                .uri("/streaming/{idVideo}", idVideo)
                .header(HttpHeaders.RANGE, range)
                .header(HttpHeaders.COOKIE, cookie)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.PARTIAL_CONTENT)
                .expectHeader().contentType(MediaType.valueOf("video/mp4"))
                .expectBody(byte[].class).isEqualTo(videoContent);

        verify(streamingService, times(1)).getVideo(idVideo, range, cookie);
    }

    @Test
    void testGetVideoWithInvalidRangeHeader() {
        String idVideo = "exampleVideo";
        String range = "invalidRange";
        String cookie = "sessionId=123";

        when(streamingService.getVideo(idVideo, range, cookie)).thenReturn(Mono.empty());

        webTestClient.get()
                .uri("/streaming/{idVideo}", idVideo)
                .header(HttpHeaders.RANGE, range)
                .header(HttpHeaders.COOKIE, cookie)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE);

        verify(streamingService, times(1)).getVideo(idVideo, range, cookie);
    }


}
