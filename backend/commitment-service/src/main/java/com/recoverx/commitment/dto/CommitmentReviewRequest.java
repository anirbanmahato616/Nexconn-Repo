package com.recoverx.commitment.dto;

import jakarta.validation.constraints.Size;

public class CommitmentReviewRequest {
    // Retained for backward compatibility only. The service derives reviewer identity from JWT.
    private Long reviewedBy;

    @Size(max = 500)
    private String note;

    public Long getReviewedBy() { return reviewedBy; }
    public void setReviewedBy(Long reviewedBy) { this.reviewedBy = reviewedBy; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
