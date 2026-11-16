package com.netdata.lab;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class LoadController {
    private static final Logger log = LoggerFactory.getLogger(LoadController.class);
    private static final int MAX_CPU_SECONDS = 30;
    private static final int MAX_MEMORY_MB = 200;
    private static final int MAX_MEMORY_SECONDS = 30;
    private static final int MAX_DELAY_MS = 10_000;

    @GetMapping("/hello")
    public Map<String, Object> hello() {
        return response("Hello from the Netdata monitoring lab", "normal request completed");
    }

    @GetMapping("/health-check")
    public Map<String, Object> healthCheck() {
        return response("UP", "application endpoint is available");
    }

    /** Local training only: creates a bounded CPU spike in this request thread. */
    @GetMapping("/cpu")
    public ResponseEntity<Map<String, Object>> cpu(@RequestParam(defaultValue = "10") int seconds) {
        if (seconds < 1 || seconds > MAX_CPU_SECONDS) return invalid("seconds must be between 1 and " + MAX_CPU_SECONDS);
        log.warn("Starting local CPU load experiment for {} seconds", seconds);
        long finishAt = System.nanoTime() + TimeUnit.SECONDS.toNanos(seconds);
        double value = 0;
        while (System.nanoTime() < finishAt) value += Math.sqrt(Math.random() * 10_000);
        log.warn("Completed local CPU load experiment");
        return ResponseEntity.ok(response("CPU experiment complete", "durationSeconds=" + seconds + ", checksum=" + value));
    }

    /** Local training only: retains bounded memory temporarily, then lets it be collected. */
    @GetMapping("/memory")
    public ResponseEntity<Map<String, Object>> memory(@RequestParam(defaultValue = "100") int mb,
                                                       @RequestParam(defaultValue = "15") int seconds) throws InterruptedException {
        if (mb < 1 || mb > MAX_MEMORY_MB) return invalid("mb must be between 1 and " + MAX_MEMORY_MB);
        if (seconds < 1 || seconds > MAX_MEMORY_SECONDS) return invalid("seconds must be between 1 and " + MAX_MEMORY_SECONDS);
        log.warn("Starting local memory experiment: {} MB for {} seconds", mb, seconds);
        List<byte[]> allocation = new java.util.ArrayList<>();
        for (int i = 0; i < mb; i++) allocation.add(new byte[1024 * 1024]);
        Thread.sleep(TimeUnit.SECONDS.toMillis(seconds));
        allocation.clear();
        log.warn("Completed local memory experiment");
        return ResponseEntity.ok(response("Memory experiment complete", "allocatedMB=" + mb + ", durationSeconds=" + seconds));
    }

    @GetMapping("/slow")
    public ResponseEntity<Map<String, Object>> slow(@RequestParam(defaultValue = "2000") int delayMs) throws InterruptedException {
        if (delayMs < 0 || delayMs > MAX_DELAY_MS) return invalid("delayMs must be between 0 and " + MAX_DELAY_MS);
        log.warn("Simulating slow request for {} ms", delayMs);
        Thread.sleep(delayMs);
        return ResponseEntity.ok(response("Slow request complete", "delayMs=" + delayMs));
    }

    @GetMapping("/error")
    public ResponseEntity<Map<String, Object>> error() {
        log.error("Intentional local-learning error endpoint invoked");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response("Intentional error", "use this only to observe HTTP 5xx metrics"));
    }

    private ResponseEntity<Map<String, Object>> invalid(String message) {
        return ResponseEntity.badRequest().body(response("Invalid experiment input", message));
    }

    private Map<String, Object> response(String message, String detail) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("message", message); body.put("detail", detail); body.put("timestamp", Instant.now()); body.put("requestId", UUID.randomUUID());
        return body;
    }
}
