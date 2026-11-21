package com.recoverx.conversation.nexconn.entity;

import java.time.LocalDateTime;
import jakarta.persistence.*;

@Entity
@Table(name = "nexconn_webhook_events", uniqueConstraints =
    @UniqueConstraint(name = "uk_nexconn_webhook_event_id", columnNames = "event_id"))
public class NexconnWebhookEvent {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "event_id", nullable = false, length = 128)
    private String eventId;
    @Column(name = "event_type", nullable = false, length = 80)
    private String eventType;
    @Column(name = "payload", nullable = false, columnDefinition = "LONGTEXT")
    private String payload;
    @Column(name = "received_at", nullable = false, updatable = false)
    private LocalDateTime receivedAt;
    @Column(name = "processed", nullable = false)
    private boolean processed = false;
    @Column(name = "processing_status", length = 80)
    private String processingStatus;
    @Column(name = "ai_analysis_json", columnDefinition = "LONGTEXT")
    private String aiAnalysisJson;
    @PrePersist public void onCreate() { receivedAt = LocalDateTime.now(); }
    public Long getId() { return id; }
    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public String getPayload() { return payload; }
    public void setPayload(String payload) { this.payload = payload; }
    public LocalDateTime getReceivedAt() { return receivedAt; }
    public boolean isProcessed() { return processed; }
    public void setProcessed(boolean processed) { this.processed = processed; }
    public String getProcessingStatus() { return processingStatus; }
    public void setProcessingStatus(String processingStatus) { this.processingStatus = processingStatus; }
    public String getAiAnalysisJson() { return aiAnalysisJson; }
    public void setAiAnalysisJson(String aiAnalysisJson) { this.aiAnalysisJson = aiAnalysisJson; }
}
