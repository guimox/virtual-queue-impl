# API Testing with curl

Use the following curl commands to test the virtual ticket queue API endpoints.

## 1. Join the Queue

```
curl -X POST "http://localhost:8080/api/queue/join?userId=user123"
```

## 2. Bulk Join Users

```
curl -X POST "http://localhost:8080/api/queue/bulk-join?count=10"
```

## 3. Start Queue Processing (set speed in ms)

```
curl -X POST "http://localhost:8080/api/queue/start-processing?speedMs=1000"
```

## 3b. Stop Queue Processing

To stop the queue processing if it was started:

```
curl -X POST "http://localhost:8080/api/queue/stop-processing"
```

## 4. Check Queue Status for a User

```
curl "http://localhost:8080/api/queue/status?userId=user123"
```

## 5. Get All Queue Users (Full Queue)

```
curl "http://localhost:8080/api/queue/all-status"
```

## 6. See the Queue Live in Browser (Server-Sent Events)

Open this URL in your browser or use an SSE client to see real-time queue updates:

```
http://localhost:8080/api/queue/see
```

The queue will update every second as users join or are processed.

## 7. Purchase Ticket (only works if user is at the front)

```
curl -X POST "http://localhost:8080/api/queue/purchase?userId=user123"
```

## 8. Try with Multiple Users

```
curl -X POST "http://localhost:8080/api/queue/join?userId=userA"
curl -X POST "http://localhost:8080/api/queue/join?userId=userB"
curl -X POST "http://localhost:8080/api/queue/join?userId=userC"

curl "http://localhost:8080/api/queue/status?userId=userA"
curl "http://localhost:8080/api/queue/status?userId=userB"
curl "http://localhost:8080/api/queue/status?userId=userC"
```

## 9. Attempt to Purchase Out of Turn

```
curl -X POST "http://localhost:8080/api/queue/purchase?userId=userB"
```

## 10. Clear All Queues (Admin)

To clear both the Redis queue and the Kafka topic for a fresh start, run:

```
curl -X POST "http://localhost:8080/api/queue/admin/clear"
```

This will remove all users from the queue and delete the Kafka topic.

Replace `user123`, `userA`, `userB`, `userC` with any user IDs you want to test.
