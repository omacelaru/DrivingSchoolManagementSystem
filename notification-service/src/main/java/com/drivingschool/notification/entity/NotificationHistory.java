package com.drivingschool.notification.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "notification_history")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationHistory {
    
    @Id
    private String id;
    
    private String type;
    
    private String message;
    
    private Object payload;
    
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
}
