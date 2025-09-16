package guimox.queue.service;

import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;

@Service
@EnableAsync
public class QueueProcessorService {
    private final QueueService queueService;
    private final KafkaTemplate<String, String> kafkaTemplate;

    private volatile long lastSpeedMs = 1000;
    private final Random random = new Random();
    private final AtomicBoolean running = new AtomicBoolean(false);

    public QueueProcessorService(QueueService queueService, KafkaTemplate<String, String> kafkaTemplate) {
        this.queueService = queueService;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Async
    public void processQueue(long delayMillis) {
        lastSpeedMs = delayMillis;
        running.set(true);
        while (running.get()) {
            String userId = queueService.getFirstUser();
            if (userId == null) break;
            queueService.removeFromQueue(userId);
            try {
                Thread.sleep(delayMillis);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        running.set(false);
    }

    @Async
    public void processQueueRandom(long maxDelayMillis) {
        lastSpeedMs = maxDelayMillis;
        running.set(true);
        while (running.get()) {
            String userId = queueService.getFirstUser();
            if (userId == null) break;
            queueService.removeFromQueue(userId);
            try {
                long delay = random.nextInt((int) maxDelayMillis + 1);
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        running.set(false);
    }

    @Async
    public void processQueueBatch(long delayMillis, int batchSize) {
        lastSpeedMs = delayMillis;
        running.set(true);
        while (running.get()) {
            List<String> batch = queueService.getBatchUsers(batchSize);
            if (batch == null || batch.isEmpty()) break;
            for (String userId : batch) {
                // Publish to process-ticket topic for each user in the batch
                kafkaTemplate.send("process-ticket", userId);
            }
            queueService.removeBatchFromQueue(batch);
            try {
                Thread.sleep(delayMillis);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        running.set(false);
    }

    public void stopProcessing() {
        running.set(false);
    }

    public boolean isProcessing() {
        return running.get();
    }

    public long getLastSpeedMs() {
        return lastSpeedMs;
    }
}
