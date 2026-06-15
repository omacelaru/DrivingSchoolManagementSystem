package com.drivingschool.notification.service;

import com.drivingschool.notification.entity.NotificationHistory;
import com.drivingschool.notification.repository.NotificationHistoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationHistoryRepository notificationHistoryRepository;

    @InjectMocks
    private NotificationService notificationService;

    @Test
    void whenHandleLessonBooked_thenSendsNotificationAndSavesHistory() {
        // Given
        Object lesson = new Object();

        // When
        notificationService.handleLessonBooked(lesson);

        // Then
        verify(notificationHistoryRepository, times(1)).save(argThat(history -> 
            "LESSON_BOOKED".equals(history.getType()) &&
            "Lesson booked successfully".equals(history.getMessage()) &&
            lesson.equals(history.getPayload())
        ));
    }

    @Test
    void whenHandleLessonCancelled_thenSendsNotificationAndSavesHistory() {
        // Given
        Object lesson = new Object();

        // When
        notificationService.handleLessonCancelled(lesson);

        // Then
        verify(notificationHistoryRepository, times(1)).save(argThat(history -> 
            "LESSON_CANCELLED".equals(history.getType()) &&
            "Lesson cancelled".equals(history.getMessage()) &&
            lesson.equals(history.getPayload())
        ));
    }

    @Test
    void whenHandlePaymentProcessed_thenSendsNotificationAndSavesHistory() {
        // Given
        Object payment = new Object();

        // When
        notificationService.handlePaymentProcessed(payment);

        // Then
        verify(notificationHistoryRepository, times(1)).save(argThat(history -> 
            "PAYMENT_PROCESSED".equals(history.getType()) &&
            "Payment processed successfully".equals(history.getMessage()) &&
            payment.equals(history.getPayload())
        ));
    }

    @Test
    void whenHandleLessonBookedWithNull_thenSavesHistorySuccessfully() {
        // Given
        Object lesson = null;

        // When
        notificationService.handleLessonBooked(lesson);

        // Then
        verify(notificationHistoryRepository, times(1)).save(argThat(history -> 
            "LESSON_BOOKED".equals(history.getType()) &&
            history.getPayload() == null
        ));
    }

    @Test
    void whenHandleLessonCancelledWithNull_thenSavesHistorySuccessfully() {
        // Given
        Object lesson = null;

        // When
        notificationService.handleLessonCancelled(lesson);

        // Then
        verify(notificationHistoryRepository, times(1)).save(argThat(history -> 
            "LESSON_CANCELLED".equals(history.getType()) &&
            history.getPayload() == null
        ));
    }

    @Test
    void whenHandlePaymentProcessedWithNull_thenSavesHistorySuccessfully() {
        // Given
        Object payment = null;

        // When
        notificationService.handlePaymentProcessed(payment);

        // Then
        verify(notificationHistoryRepository, times(1)).save(argThat(history -> 
            "PAYMENT_PROCESSED".equals(history.getType()) &&
            history.getPayload() == null
        ));
    }

    @Test
    void whenRepositoryThrowsException_thenHandlesGracefully() {
        // Given
        Object lesson = new Object();
        when(notificationHistoryRepository.save(any(NotificationHistory.class)))
                .thenThrow(new RuntimeException("Database down"));

        // When & Then - Should not throw exception
        assertDoesNotThrow(() -> notificationService.handleLessonBooked(lesson));
    }
}
