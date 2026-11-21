package com.recoverx.commitment.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "commitment_installments", indexes = @Index(name = "idx_installment_commitment", columnList = "commitment_id"))
public class CommitmentInstallment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "commitment_id", nullable = false)
    private Commitment commitment;
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;
    @Column(name = "proposed_date", nullable = false)
    private LocalDate proposedDate;
    @Column(nullable = false, length = 30)
    private String status;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Commitment getCommitment() { return commitment; }
    public void setCommitment(Commitment commitment) { this.commitment = commitment; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public LocalDate getProposedDate() { return proposedDate; }
    public void setProposedDate(LocalDate proposedDate) { this.proposedDate = proposedDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
