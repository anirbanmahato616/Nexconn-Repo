export interface Installment {
  id: number;
  commitmentId: number;
  amount: number;
  proposedDate: string;
  status: string;
}

export interface InstallmentRequest {
  commitmentId: number;
  amount: number;
  proposedDate: string;
}