export interface Invoice {
  id: number;
  businessId: number;
  customerId: number;
  invoiceNumber: string;
  description: string;
  amount: number;
  issueDate: string;
  dueDate: string;
  status: string;
  paidAmount: number;
  outstandingAmount: number;
}