package guimox.queue.kafka;

import guimox.queue.service.QueueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class KafkaQueueListener {
    private final QueueService queueService;

    public KafkaQueueListener(QueueService queueService) {
        this.queueService = queueService;
    }

    @KafkaListener(topics = "ticket-queue", groupId = "queue-group")
    public void listen(String userId) {
        queueService.addToRedisQueue(userId);
    }
}
