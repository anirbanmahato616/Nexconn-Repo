
import { useState } from "react";
import { useNavigate, useOutletContext } from "react-router-dom";
import {
    createBusiness,
    type BusinessCreateRequest,
} from "../services/businessService";
import type { Business } from "../types/business";

interface DashboardContext {
  refreshBusinesses: (businessId?: number) => Promise<void>;
}

export default function CreateBusiness() {
  const navigate = useNavigate();

  const { refreshBusinesses } =
    useOutletContext<DashboardContext>();

  const [formData, setFormData] = useState<BusinessCreateRequest>({
    name: "",
    email: "",
    phone: "",
    address: "",
    industry: "",
  });

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const handleChange = (
    e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>
  ) => {
    const { name, value } = e.target;

    setFormData((previous) => ({
      ...previous,
      [name]: value,
    }));
  };

  const handleSubmit = async (
    e: React.FormEvent<HTMLFormElement>
  ) => {
    e.preventDefault();

    if (!formData.name.trim()) {
      setError("Business name is required.");
      return;
    }

    try {
      setLoading(true);
      setError("");

      const payload: BusinessCreateRequest = {
        name: formData.name.trim(),
        email: formData.email?.trim() || undefined,
        phone: formData.phone?.trim() || undefined,
        address: formData.address?.trim() || undefined,
        industry: formData.industry?.trim() || undefined,
      };

      const business: Business = await createBusiness(payload);

      await refreshBusinesses(business.id);

      navigate("/dashboard");
    } catch (err: any) {
      setError(
        err.response?.data?.message ||
        "Unable to create business. Please try again."
      );
    } finally {
      setLoading(false);
    }
  };

  const inputClass =
    "w-full rounded-lg border border-slate-300 px-4 py-2.5 text-sm outline-none focus:border-blue-500 focus:ring-2 focus:ring-blue-100";

  return (
    <div className="max-w-3xl mx-auto">
      <div className="mb-6">
        <h1 className="text-2xl font-bold text-slate-800">
          Create Your Business
        </h1>
        <p className="mt-2 text-sm text-slate-500">
          Add your business details to start using RecoverX.
        </p>
      </div>

      <form
        onSubmit={handleSubmit}
        className="bg-white border border-slate-200 rounded-xl p-6 shadow-sm space-y-5"
      >
        {error && (
          <div className="bg-red-50 text-red-600 rounded-lg p-3 text-sm">
            {error}
          </div>
        )}

        <div>
          <label className="block text-sm font-medium text-slate-700 mb-2">
            Business Name *
          </label>
          <input
            type="text"
            name="name"
            value={formData.name}
            onChange={handleChange}
            placeholder="Enter business name"
            maxLength={150}
            required
            className={inputClass}
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
            placeholder="business@example.com"
            className={inputClass}
          />
        </div>

        <div>
          <label className="block text-sm font-medium text-slate-700 mb-2">
            Phone Number
          </label>
          <input
            type="tel"
            name="phone"
            value={formData.phone}
            onChange={handleChange}
            placeholder="Enter phone number"
            maxLength={20}
            className={inputClass}
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
            placeholder="Enter business address"
            maxLength={255}
            rows={3}
            className={inputClass}
          />
        </div>

        <div>
          <label className="block text-sm font-medium text-slate-700 mb-2">
            Industry
          </label>
          <input
            type="text"
            name="industry"
            value={formData.industry}
            onChange={handleChange}
            placeholder="e.g. IT Services, Retail, Manufacturing"
            maxLength={100}
            className={inputClass}
          />
        </div>

        <div className="flex justify-end gap-3 pt-3">
          <button
            type="button"
            onClick={() => navigate("/dashboard")}
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
            {loading ? "Creating..." : "Create Business"}
          </button>
        </div>
      </form>
    </div>
  );
}
