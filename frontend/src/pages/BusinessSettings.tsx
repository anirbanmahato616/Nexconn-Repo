
import { useState } from "react";
import { useNavigate, useOutletContext } from "react-router-dom";
import {
    deactivateBusiness,
    updateBusiness,
    type BusinessUpdateRequest,
} from "../services/businessService";
import type { Business } from "../types/business";

interface DashboardContext {
  selectedBusiness: Business | null;
  refreshBusinesses: (businessId?: number) => Promise<void>;
}

export default function BusinessSettings() {
  const { selectedBusiness, refreshBusinesses } =
    useOutletContext<DashboardContext>();

  const navigate = useNavigate();

  const [editing, setEditing] = useState(false);
  const [loading, setLoading] = useState(false);
  const [deactivating, setDeactivating] = useState(false);
  const [showConfirmation, setShowConfirmation] = useState(false);

  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  const [formData, setFormData] = useState<BusinessUpdateRequest>({
    name: selectedBusiness?.name || "",
    email: selectedBusiness?.email || "",
    phone: selectedBusiness?.phone || "",
    address: selectedBusiness?.address || "",
    industry: selectedBusiness?.industry || "",
  });

  if (!selectedBusiness) {
    return (
      <div className="bg-white rounded-xl border p-6">
        <h2 className="text-xl font-semibold text-slate-800">
          No business selected
        </h2>
        <p className="text-sm text-slate-500 mt-2">
          Create or select a business to manage its settings.
        </p>
      </div>
    );
  }

  const handleEdit = () => {
    setFormData({
      name: selectedBusiness.name,
      email: selectedBusiness.email || "",
      phone: selectedBusiness.phone || "",
      address: selectedBusiness.address || "",
      industry: selectedBusiness.industry || "",
    });

    setError("");
    setSuccess("");
    setEditing(true);
  };

  const handleCancel = () => {
    setEditing(false);
    setError("");
  };

  const handleChange = (
    e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>
  ) => {
    const { name, value } = e.target;

    setFormData((previous) => ({
      ...previous,
      [name]: value,
    }));
  };

  const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();

    if (!formData.name.trim()) {
      setError("Business name is required.");
      return;
    }

    try {
      setLoading(true);
      setError("");
      setSuccess("");

      const payload: BusinessUpdateRequest = {
        name: formData.name.trim(),
        email: formData.email?.trim() || undefined,
        phone: formData.phone?.trim() || undefined,
        address: formData.address?.trim() || undefined,
        industry: formData.industry?.trim() || undefined,
      };

      await updateBusiness(selectedBusiness.id, payload);

      await refreshBusinesses(selectedBusiness.id);

      setEditing(false);
      setSuccess("Business details updated successfully.");
    } catch (err: any) {
      setError(
        err.response?.data?.message ||
        "Unable to update business details."
      );
    } finally {
      setLoading(false);
    }
  };

  const handleDeactivate = async () => {
    try {
      setDeactivating(true);
      setError("");
      setSuccess("");

      await deactivateBusiness(selectedBusiness.id);

      await refreshBusinesses();

      setShowConfirmation(false);
      navigate("/dashboard");
    } catch (err: any) {
      setError(
        err.response?.data?.message ||
        "Unable to deactivate business."
      );
      setShowConfirmation(false);
    } finally {
      setDeactivating(false);
    }
  };

  const inputClass =
    "w-full rounded-lg border border-slate-300 px-4 py-2.5 text-sm outline-none focus:border-blue-500 focus:ring-2 focus:ring-blue-100";

  return (
    <div className="max-w-3xl mx-auto">
      <div className="mb-6">
        <h1 className="text-2xl font-bold text-slate-800">
          Business Settings
        </h1>
        <p className="mt-2 text-sm text-slate-500">
          Manage your business information.
        </p>
      </div>

      {/* Business Information */}
      <div className="bg-white border border-slate-200 rounded-xl p-6 shadow-sm">
        <div className="flex items-center justify-between mb-6">
          <div>
            <h2 className="text-lg font-semibold text-slate-800">
              Business Information
            </h2>
            <p className="text-sm text-slate-500 mt-1">
              Update your business profile details.
            </p>
          </div>

          {!editing && (
            <button
              type="button"
              onClick={handleEdit}
              className="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-lg text-sm font-medium"
            >
              Edit Details
            </button>
          )}
        </div>

        {error && (
          <div className="mb-5 bg-red-50 text-red-600 rounded-lg p-3 text-sm">
            {error}
          </div>
        )}

        {success && (
          <div className="mb-5 bg-green-50 text-green-700 rounded-lg p-3 text-sm">
            {success}
          </div>
        )}

        <form onSubmit={handleSubmit} className="space-y-5">
          <div>
            <label className="block text-sm font-medium text-slate-700 mb-2">
              Business Name *
            </label>
            <input
              name="name"
              value={formData.name}
              onChange={handleChange}
              disabled={!editing}
              required
              maxLength={150}
              className={`${inputClass} disabled:bg-slate-50 disabled:text-slate-600`}
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-slate-700 mb-2">
              Business Email
            </label>
            <input
              type="email"
              name="email"
              value={formData.email}
              onChange={handleChange}
              disabled={!editing}
              className={`${inputClass} disabled:bg-slate-50 disabled:text-slate-600`}
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-slate-700 mb-2">
              Phone Number
            </label>
            <input
              name="phone"
              value={formData.phone}
              onChange={handleChange}
              disabled={!editing}
              maxLength={20}
              className={`${inputClass} disabled:bg-slate-50 disabled:text-slate-600`}
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-slate-700 mb-2">
              Address
            </label>
            <textarea
              name="address"
              value={formData.address}
              onChange={handleChange}
              disabled={!editing}
              maxLength={255}
              rows={3}
              className={`${inputClass} disabled:bg-slate-50 disabled:text-slate-600`}
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-slate-700 mb-2">
              Industry
            </label>
            <input
              name="industry"
              value={formData.industry}
              onChange={handleChange}
              disabled={!editing}
              maxLength={100}
              className={`${inputClass} disabled:bg-slate-50 disabled:text-slate-600`}
            />
          </div>

          <div className="border-t pt-5">
            <p className="text-xs text-slate-500">
              Business ID: {selectedBusiness.id}
            </p>
            <p className="text-xs text-slate-500 mt-1">
              Status: {selectedBusiness.active ? "Active" : "Inactive"}
            </p>
          </div>

          {editing && (
            <div className="flex justify-end gap-3">
              <button
                type="button"
                onClick={handleCancel}
                disabled={loading}
                className="px-5 py-2.5 rounded-lg border border-slate-300 text-slate-700 text-sm font-medium hover:bg-slate-50 disabled:opacity-50"
              >
                Cancel
              </button>

              <button
                type="submit"
                disabled={loading}
                className="px-5 py-2.5 rounded-lg bg-blue-600 text-white text-sm font-medium hover:bg-blue-700 disabled:opacity-50"
              >
                {loading ? "Saving..." : "Save Changes"}
              </button>
            </div>
          )}
        </form>
      </div>

      {/* Danger Zone */}
      <div className="mt-8 bg-white border border-red-200 rounded-xl p-6 shadow-sm">
        <h2 className="text-lg font-semibold text-red-600">
          Danger Zone
        </h2>

        <p className="text-sm text-slate-600 mt-2">
          Deactivating this business will make it unavailable for normal
          business operations.
        </p>

        <p className="text-sm text-slate-500 mt-2">
          Historical invoices, payments, customers and commitments
          are not automatically deleted by this action.
        </p>

        <button
          type="button"
          onClick={() => {
            setError("");
            setShowConfirmation(true);
          }}
          disabled={editing || loading || deactivating}
          className="mt-5 bg-red-600 hover:bg-red-700 text-white px-5 py-2.5 rounded-lg text-sm font-medium disabled:opacity-50"
        >
          Deactivate Business
        </button>
      </div>

      {/* Confirmation Dialog */}
      {showConfirmation && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4">
          <div className="bg-white w-full max-w-md rounded-xl p-6 shadow-xl">
            <h2 className="text-xl font-semibold text-slate-800">
              Deactivate Business?
            </h2>

            <p className="text-sm text-slate-600 mt-3">
              Are you sure you want to deactivate{" "}
              <strong>{selectedBusiness.name}</strong>?
            </p>

            <p className="text-sm text-red-600 mt-2">
              This action will deactivate the business. You may need
              backend support to reactivate it.
            </p>

            <div className="flex justify-end gap-3 mt-7">
              <button
                type="button"
                onClick={() => setShowConfirmation(false)}
                disabled={deactivating}
                className="px-4 py-2 rounded-lg border border-slate-300 text-slate-700 text-sm font-medium hover:bg-slate-50 disabled:opacity-50"
              >
                Cancel
              </button>

              <button
                type="button"
                onClick={handleDeactivate}
                disabled={deactivating}
                className="px-4 py-2 rounded-lg bg-red-600 hover:bg-red-700 text-white text-sm font-medium disabled:opacity-50"
              >
                {deactivating ? "Deactivating..." : "Yes, Deactivate"}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
