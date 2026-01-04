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
    void testHandleLessonBooked() {
        // Given
        Object lesson = new Object();

        // When & Then - Should not throw exception
        assertDoesNotThrow(() -> notificationService.handleLessonBooked(lesson));
    }

    @Test
    void testHandleLessonCancelled() {
        // Given
        Object lesson = new Object();

        // When & Then - Should not throw exception
        assertDoesNotThrow(() -> notificationService.handleLessonCancelled(lesson));
    }

    @Test
    void testHandlePaymentProcessed() {
        // Given
        Object payment = new Object();

        // When & Then - Should not throw exception
        assertDoesNotThrow(() -> notificationService.handlePaymentProcessed(payment));
    }

    @Test
    void testHandleLessonBooked_WithNull() {
        // Given
        Object lesson = null;

        // When & Then - Should not throw exception (handles null gracefully)
        assertDoesNotThrow(() -> notificationService.handleLessonBooked(lesson));
    }

    @Test
    void testHandleLessonCancelled_WithNull() {
        // Given
        Object lesson = null;

        // When & Then - Should not throw exception (handles null gracefully)
        assertDoesNotThrow(() -> notificationService.handleLessonCancelled(lesson));
    }

    @Test
    void testHandlePaymentProcessed_WithNull() {
        // Given
        Object payment = null;

        // When & Then - Should not throw exception (handles null gracefully)
        assertDoesNotThrow(() -> notificationService.handlePaymentProcessed(payment));
    }
}

