package com.drivingschool.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    @KafkaListener(topics = "lesson-booked", groupId = "notification-service-group")
    public void handleLessonBooked(Object lesson) {
        log.info("Received lesson booked event: {}", lesson);
        sendNotification("Lesson booked successfully", lesson);
    }

    @KafkaListener(topics = "lesson-cancelled", groupId = "notification-service-group")
    public void handleLessonCancelled(Object lesson) {
        log.info("Received lesson cancelled event: {}", lesson);
        sendNotification("Lesson cancelled", lesson);
    }

    @KafkaListener(topics = "payment-processed", groupId = "notification-service-group")
    public void handlePaymentProcessed(Object payment) {
        log.info("Received payment processed event: {}", payment);
        sendNotification("Payment processed successfully", payment);
    }

    private void sendNotification(String message, Object data) {
        log.info("Sending notification: {} - Data: {}", message, data);
    }
}

