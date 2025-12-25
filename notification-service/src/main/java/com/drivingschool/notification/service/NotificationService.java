package com.drivingschool.notification.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {
    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    @KafkaListener(topics = "lesson-booked", groupId = "notification-service-group")
    public void handleLessonBooked(Object lesson) {
        log.info("Received lesson booked event: {}", lesson);
        // Send notification (email, SMS, etc.)
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
        // Implementation for sending notifications
        // This could integrate with email service, SMS gateway, etc.
        log.info("Sending notification: {} - Data: {}", message, data);
    }
}

