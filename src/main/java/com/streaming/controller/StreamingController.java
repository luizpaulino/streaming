package com.streaming.controller;

import com.streaming.service.StreamingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/streaming")
public class StreamingController {

    @Autowired
    private StreamingService service;

    @GetMapping(value = "/{idVideo}", produces = "video/mp4")
    public Mono<Resource> getVideo(
            @PathVariable String idVideo,
            @RequestHeader("Range") String range,
            @RequestHeader(value = "Cookie") String cookie) {
        return service.getVideo(idVideo, range, cookie);
    }

}
