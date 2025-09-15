package com.example.virtualqueue.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class KafkaQueueListener {
    @Autowired
    private QueueService queueService;

    @KafkaListener(topics = "ticket-queue", groupId = "queue-group")
    public void listen(String userId) {
        queueService.addToRedisQueue(userId);
    }
}
