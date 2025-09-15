package com.example.virtualqueue.controller;

import com.example.virtualqueue.service.QueueService;
import com.example.virtualqueue.service.QueueProcessorService;
import com.example.virtualqueue.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ExecutionException;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/api/queue")
public class QueueController {

    @Autowired
    private QueueService queueService;

    @Autowired
    private QueueProcessorService queueProcessorService;

    @Autowired
    private AdminService adminService;

    private final ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public QueueController() {
        scheduler.initialize();
    }

    @PostMapping("/join")
    public ResponseEntity<String> joinQueue(@RequestParam String userId) {
        queueService.joinQueue(userId);
        return ResponseEntity.ok("User " + userId + " joined the queue");
    }

    @PostMapping("/bulk-join")
    public ResponseEntity<String> bulkJoin(@RequestParam int count) {
        int joined = 0;
        for (int i = 1; i <= count; i++) {
            try {
                queueService.joinQueue("bulkUser" + i);
                joined++;
            } catch (Exception e) {
                // Log or ignore, continue adding others
            }
        }
        return ResponseEntity.ok(joined + " users joined the queue");
    }

    @PostMapping("/start-processing")
    public ResponseEntity<String> startProcessing(@RequestParam(defaultValue = "1000") long speedMs) {
        queueProcessorService.processQueueBatch(speedMs, 10);
        return ResponseEntity.ok("Queue processing started with random speed up to " + speedMs + " ms per user");
    }

    @PostMapping("/stop-processing")
    public ResponseEntity<String> stopProcessing() {
        queueProcessorService.stopProcessing();
        return ResponseEntity.ok("Queue processing stopped.");
    }

    @GetMapping("/status")
    public ResponseEntity<String> getStatus(@RequestParam String userId) {
        Long position = queueService.getQueuePosition(userId);
        if (position == 0) {
            return ResponseEntity.ok("User not in queue");
        }
        return ResponseEntity.ok("User " + userId + " is at position " + position);
    }

    @GetMapping("/all-status")
    public ResponseEntity<List<String>> getAllStatus() {
        List<String> allUsers = queueService.getAllQueueUsers();
        return ResponseEntity.ok(allUsers);
    }

    @PostMapping("/purchase")
    public ResponseEntity<String> purchaseTicket(@RequestParam String userId) {
        if (queueService.isUserAtFront(userId)) {
            queueService.removeFromQueue(userId);
            return ResponseEntity.ok("Ticket purchased for user " + userId);
        } else {
            return ResponseEntity.status(403).body("Not your turn yet");
        }
    }

    @GetMapping(value = "/see", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter seeQueue() {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        final Runnable sendUpdate = new Runnable() {
            @Override
            public void run() {
                try {
                    List<String> allUsers = queueService.getAllQueueUsers();
                    Map<String, Object> data = new HashMap<>();
                    data.put("queueSize", allUsers != null ? allUsers.size() : 0);
                    data.put("queue", allUsers);
                    data.put("processingSpeedMs", queueProcessorService.getLastSpeedMs());
                    data.put("datetime", java.time.LocalDateTime.now().format(formatter));
                    String json = objectMapper.writeValueAsString(data);
                    emitter.send(SseEmitter.event().name("update").data(json));
                } catch (IOException e) {
                    emitter.completeWithError(e);
                }
            }
        };
        ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(sendUpdate, Duration.ofSeconds(1));
        emitter.onCompletion(() -> future.cancel(true));
        emitter.onTimeout(() -> future.cancel(true));
        return emitter;
    }

    @PostMapping("/admin/clear")
    public ResponseEntity<String> clearAll() {
        try {
            adminService.clearRedisQueue();
            adminService.deleteKafkaTopic("ticket-queue");
            return ResponseEntity.ok("Kafka topic and Redis queue cleared.");
        } catch (ExecutionException | InterruptedException e) {
            return ResponseEntity.status(500).body("Error clearing Kafka: " + e.getMessage());
        }
    }
}
