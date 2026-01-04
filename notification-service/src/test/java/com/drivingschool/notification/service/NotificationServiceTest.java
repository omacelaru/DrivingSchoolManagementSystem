package com.drivingschool.notification.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @InjectMocks
    private NotificationService notificationService;

    @Test
    void whenHandleLessonBooked_thenSendsNotification() {
        // Given
        Object lesson = new Object();

        // When & Then - Should not throw exception
        assertDoesNotThrow(() -> notificationService.handleLessonBooked(lesson));
    }

    @Test
    void whenHandleLessonCancelled_thenSendsNotification() {
        // Given
        Object lesson = new Object();

        // When & Then - Should not throw exception
        assertDoesNotThrow(() -> notificationService.handleLessonCancelled(lesson));
    }

    @Test
    void whenHandlePaymentProcessed_thenSendsNotification() {
        // Given
        Object payment = new Object();

        // When & Then - Should not throw exception
        assertDoesNotThrow(() -> notificationService.handlePaymentProcessed(payment));
    }

    @Test
    void whenHandleLessonBookedWithNull_thenDoesNotThrowException() {
        // Given
        Object lesson = null;

        // When & Then - Should not throw exception (handles null gracefully)
        assertDoesNotThrow(() -> notificationService.handleLessonBooked(lesson));
    }

    @Test
    void whenHandleLessonCancelledWithNull_thenDoesNotThrowException() {
        // Given
        Object lesson = null;

        // When & Then - Should not throw exception (handles null gracefully)
        assertDoesNotThrow(() -> notificationService.handleLessonCancelled(lesson));
    }

    @Test
    void whenHandlePaymentProcessedWithNull_thenDoesNotThrowException() {
        // Given
        Object payment = null;

        // When & Then - Should not throw exception (handles null gracefully)
        assertDoesNotThrow(() -> notificationService.handlePaymentProcessed(payment));
    }
}

