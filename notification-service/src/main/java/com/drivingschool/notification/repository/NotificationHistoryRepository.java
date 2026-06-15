package com.drivingschool.notification.repository;

import com.drivingschool.notification.entity.NotificationHistory;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationHistoryRepository extends MongoRepository<NotificationHistory, String> {
}
