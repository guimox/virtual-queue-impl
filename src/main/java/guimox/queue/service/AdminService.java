package guimox.queue.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.core.KafkaAdmin;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.DeleteTopicsResult;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.concurrent.ExecutionException;

@Service
public class AdminService {

    private final StringRedisTemplate redisTemplate;
    private final KafkaAdmin kafkaAdmin;

    public AdminService(StringRedisTemplate redisTemplate, KafkaAdmin kafkaAdmin) {
        this.redisTemplate = redisTemplate;
        this.kafkaAdmin = kafkaAdmin;
    }

    public void clearRedisQueue() {
        redisTemplate.delete("queue:list");
    }

    public void deleteKafkaTopic(String topic) throws ExecutionException, InterruptedException {
        try (AdminClient adminClient = AdminClient.create(kafkaAdmin.getConfigurationProperties())) {
            DeleteTopicsResult result = adminClient.deleteTopics(Collections.singletonList(topic));
            result.all().get();
        }
    }
}
