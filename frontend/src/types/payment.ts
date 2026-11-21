
export type PaymentStatus = "PENDING" | "VERIFIED" | "REJECTED";

export interface Payment {
  id: number;
  businessId: number;
  invoiceId: number;
  amount: number;
  paymentDate: string;
  paymentMethod: string;
  transactionReference: string;
  recordedBy: number;
  verificationStatus: PaymentStatus;
  verifiedBy: number | null;
  verifiedAt: string | null;
  rejectionReason: string | null;
  createdAt: string;
}
