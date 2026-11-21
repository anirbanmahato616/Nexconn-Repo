package com.recoverx.conversation.nexconn.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.recoverx.conversation.nexconn.entity.NexconnWebhookEvent;

@Repository
public interface NexconnWebhookEventRepository extends JpaRepository<NexconnWebhookEvent, Long> {
    boolean existsByEventId(String eventId);
    java.util.Optional<NexconnWebhookEvent> findByEventId(String eventId);
}
