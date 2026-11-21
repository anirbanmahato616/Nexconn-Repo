export interface Customer {
  id: number;
  businessId: number;
  userId: number | null;
  name: string;
  email: string;
  phone: string;
  createdAt: string;
}