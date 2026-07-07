package com.example.showcase.runtime;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Demonstrates {@code spring.threads.virtual.enabled} (see application.yml): with it set,
 * Tomcat serves every request on a virtual thread instead of a pooled platform thread, and
 * {@code @Async} switches to a virtual-thread-per-task executor. This endpoint reports the
 * request-handling thread itself, proving the effect is application-wide - not just the
 * explicit {@code Thread.ofVirtual()} usage local to the {@code spotlight} package.
 */
@RestController
@RequestMapping("/runtime")
public class ThreadInfoController {

    @GetMapping("/thread-info")
    public ThreadInfo threadInfo() {
        Thread current = Thread.currentThread();
        return new ThreadInfo(current.toString(), current.isVirtual());
    }

    public record ThreadInfo(String description, boolean virtual) {
    }
}
