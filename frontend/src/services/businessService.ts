
import type { Business } from "../types/business";
import api from "./api";

export interface BusinessCreateRequest {
  name: string;
  email?: string;
  phone?: string;
  address?: string;
  industry?: string;
}

export interface BusinessUpdateRequest {
  name: string;
  email?: string;
  phone?: string;
  address?: string;
  industry?: string;
}

export const getMyBusinesses = async (): Promise<Business[]> => {
  const response = await api.get<Business[]>(
    "/api/businesses/owner/me"
  );

  return response.data;
};

export const createBusiness = async (
  data: BusinessCreateRequest
): Promise<Business> => {
  const response = await api.post<Business>(
    "/api/businesses",
    data
  );

  return response.data;
};

export const updateBusiness = async (
  businessId: number,
  data: BusinessUpdateRequest
): Promise<Business> => {
  const response = await api.put<Business>(
    `/api/businesses/${businessId}`,
    data
  );

  return response.data;
};

export const deactivateBusiness = async (
  businessId: number
): Promise<void> => {
  await api.delete(`/api/businesses/${businessId}`);
};

export const getBusinessById = async (
  businessId: number
): Promise<Business> => {
  const response = await api.get<Business>(
    `/api/businesses/${businessId}`
  );

  return response.data;
};