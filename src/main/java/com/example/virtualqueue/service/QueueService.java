package com.example.virtualqueue.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QueueService {
    private static final String QUEUE_TOPIC = "ticket-queue";
    private static final String QUEUE_KEY = "queue:list";

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private StringRedisTemplate redisTemplate;

    public void joinQueue(String userId) {
        kafkaTemplate.send(QUEUE_TOPIC, userId);
    }

    public void addToRedisQueue(String userId) {
        redisTemplate.opsForList().rightPush(QUEUE_KEY, userId);
    }

    public Long getQueuePosition(String userId) {
        List<String> queue = redisTemplate.opsForList().range(QUEUE_KEY, 0, -1);
        if (queue == null) return 0L;
        return (long) (queue.indexOf(userId) + 1);
    }

    public boolean isUserAtFront(String userId) {
        String front = redisTemplate.opsForList().index(QUEUE_KEY, 0);
        return userId.equals(front);
    }

    public void removeFromQueue(String userId) {
        redisTemplate.opsForList().remove(QUEUE_KEY, 1, userId);
    }

    public List<String> getAllQueueUsers() {
        return redisTemplate.opsForList().range(QUEUE_KEY, 0, -1);
    }

    public String getFirstUser() {
        return redisTemplate.opsForList().index(QUEUE_KEY, 0);
    }

    public List<String> getBatchUsers(int batchSize) {
        List<String> queue = redisTemplate.opsForList().range(QUEUE_KEY, 0, batchSize - 1);
        return queue != null ? queue : List.of();
    }

    public void removeBatchFromQueue(List<String> batch) {
        for (String userId : batch) {
            redisTemplate.opsForList().remove(QUEUE_KEY, 1, userId);
        }
    }
}
