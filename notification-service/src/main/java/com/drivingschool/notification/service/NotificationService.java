package com.drivingschool.notification.service;

import com.drivingschool.notification.entity.NotificationHistory;
import com.drivingschool.notification.repository.NotificationHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationHistoryRepository notificationHistoryRepository;

    @KafkaListener(topics = "lesson-booked", groupId = "notification-service-group")
    public void handleLessonBooked(Object lessonEvent) {
        Object lesson = extractPayload(lessonEvent);
        log.info("Received lesson booked event: {}", lesson);
        sendNotification("Lesson booked successfully", lesson);
        saveHistory("LESSON_BOOKED", "Lesson booked successfully", lesson);
    }

    @KafkaListener(topics = "lesson-cancelled", groupId = "notification-service-group")
    public void handleLessonCancelled(Object lessonEvent) {
        Object lesson = extractPayload(lessonEvent);
        log.info("Received lesson cancelled event: {}", lesson);
        sendNotification("Lesson cancelled", lesson);
        saveHistory("LESSON_CANCELLED", "Lesson cancelled", lesson);
    }

    @KafkaListener(topics = "payment-processed", groupId = "notification-service-group")
    public void handlePaymentProcessed(Object paymentEvent) {
        Object payment = extractPayload(paymentEvent);
        log.info("Received payment processed event: {}", payment);
        sendNotification("Payment processed successfully", payment);
        saveHistory("PAYMENT_PROCESSED", "Payment processed successfully", payment);
    }

    private void sendNotification(String message, Object data) {
        log.info("Sending notification: {} - Data: {}", message, data);
    }

    private void saveHistory(String type, String message, Object payload) {
        try {
            NotificationHistory history = NotificationHistory.builder()
                    .type(type)
                    .message(message)
                    .payload(payload)
                    .build();
            notificationHistoryRepository.save(history);
            log.info("Saved notification history to MongoDB of type: {}", type);
        } catch (Exception e) {
            log.error("Failed to save notification history to MongoDB", e);
        }
    }

    private Object extractPayload(Object event) {
        if (event instanceof org.apache.kafka.clients.consumer.ConsumerRecord<?, ?> record) {
            return record.value();
        }
        return event;
    }
}

