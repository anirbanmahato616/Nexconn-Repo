
export type CommitmentStatus =
  | "PENDING_REVIEW"
  | "APPROVED"
  | "REJECTED";

export interface Commitment {
  id: number;
  businessId: number;
  invoiceId: number;
  conversationId: number | null;
  sourceMessageId: string | null;
  summary: string;
  status: CommitmentStatus;
  aiClassification: string | null;
  proposedAmount: number | null;
  proposedPaymentDate: string | null;
  reviewNote: string | null;
  reviewedBy: number | null;
  reviewedAt: string | null;
  createdAt: string;
}

export interface CustomerCommitment {
  id: number;
  businessId: number;
  invoiceId: number;
  summary: string;
  proposedAmount: number | null;
  proposedPaymentDate: string | null;
  status: CommitmentStatus;
  createdAt: string;
  reviewedAt: string | null;
}
