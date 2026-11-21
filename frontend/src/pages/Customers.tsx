
import {
  CheckCircle,
  Loader2,
  Mail,
  Pencil,
  Plus,
  Search,
  Users,
  X,
} from "lucide-react";

import { useEffect, useMemo, useState, type FormEvent } from "react";
import { useOutletContext } from "react-router-dom";

import type { Business } from "../types/business";
import type { Customer } from "../types/customer";

import {
  createCustomer,
  getBusinessCustomers,
  inviteCustomer,
  updateCustomer,
} from "../services/customerService";

type DashboardContext = {
  mode: "business" | "customer";
  selectedBusiness: Business | null;
  businesses: Business[];
  loading: boolean;
};

type CustomerForm = {
  name: string;
  email: string;
  phone: string;
};

const emptyForm: CustomerForm = {
  name: "",
  email: "",
  phone: "",
};

const getErrorMessage = (error: any) => {
  const data = error.response?.data;

  if (typeof data === "string") return data;
  if (data?.message) return data.message;

  return "Something went wrong. Please try again.";
};

export default function Customers() {
  const { selectedBusiness, loading: businessLoading } =
    useOutletContext<DashboardContext>();

  const [customers, setCustomers] = useState<Customer[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  const [search, setSearch] = useState("");

  const [showForm, setShowForm] = useState(false);
  const [saving, setSaving] = useState(false);
  const [formError, setFormError] = useState("");

  const [editingCustomer, setEditingCustomer] =
    useState<Customer | null>(null);

  const [form, setForm] = useState<CustomerForm>(emptyForm);

  // Invitation states
  const [invitingCustomerId, setInvitingCustomerId] =
    useState<number | null>(null);

  const [invitedCustomerIds, setInvitedCustomerIds] =
    useState<Set<number>>(new Set());

  // Load customers whenever the selected business changes
  useEffect(() => {
    if (!selectedBusiness) {
      setCustomers([]);
      return;
    }

    let cancelled = false;

    const load = async () => {
      try {
        setLoading(true);
        setError("");

        const data = await getBusinessCustomers(selectedBusiness.id);

        if (!cancelled) {
          setCustomers(data);
        }
      } catch (err: any) {
        if (!cancelled) {
          setError(getErrorMessage(err));
        }
      } finally {
        if (!cancelled) {
          setLoading(false);
        }
      }
    };

    setInvitedCustomerIds(new Set());
    load();

    return () => {
      cancelled = true;
    };
  }, [selectedBusiness?.id]);

  // Filter customers
  const filteredCustomers = useMemo(() => {
    const query = search.toLowerCase();

    return customers.filter(
      (customer) =>
        customer.name.toLowerCase().includes(query) ||
        customer.email.toLowerCase().includes(query)
    );
  }, [customers, search]);

  // Open create form
  const openCreateForm = () => {
    setEditingCustomer(null);
    setForm(emptyForm);
    setFormError("");
    setShowForm(true);
  };

  // Open edit form
  const openEditForm = (customer: Customer) => {
    setEditingCustomer(customer);

    setForm({
      name: customer.name,
      email: customer.email,
      phone: customer.phone || "",
    });

    setFormError("");
    setShowForm(true);
  };

  // Create or update customer
  const handleSubmit = async (e: FormEvent<HTMLFormElement>) => {
    e.preventDefault();

    if (!selectedBusiness) return;

    try {
      setSaving(true);
      setFormError("");
      setSuccess("");

      const request = {
        name: form.name.trim(),
        email: form.email.trim(),
        phone: form.phone.trim(),
      };

      if (editingCustomer) {
        await updateCustomer(
          selectedBusiness.id,
          editingCustomer.id,
          request
        );

        setSuccess("Customer updated successfully!");
      } else {
        await createCustomer(selectedBusiness.id, request);

        setSuccess(
          "Customer added successfully! You can now send an invitation."
        );
      }

      const updatedCustomers = await getBusinessCustomers(
        selectedBusiness.id
      );

      setCustomers(updatedCustomers);
      setShowForm(false);
      setEditingCustomer(null);
      setForm(emptyForm);
    } catch (err: any) {
      setFormError(getErrorMessage(err));
    } finally {
      setSaving(false);
    }
  };

  // Send invitation email
  const handleInvite = async (customer: Customer) => {
    if (!selectedBusiness) return;

    try {
      setInvitingCustomerId(customer.id);
      setError("");
      setSuccess("");

      const response = await inviteCustomer(
        selectedBusiness.id,
        customer.id
      );

      setInvitedCustomerIds((previous) => {
        const next = new Set(previous);
        next.add(customer.id);
        return next;
      });

      setSuccess(
        response.message || `Invitation sent to ${customer.email}`
      );
    } catch (err: any) {
      setError(getErrorMessage(err));
    } finally {
      setInvitingCustomerId(null);
    }
  };

  if (businessLoading) {
    return <p className="text-slate-500">Loading business...</p>;
  }

  if (!selectedBusiness) {
    return (
      <div className="bg-white border rounded-xl p-6">
        <h2 className="font-semibold">No business selected</h2>
        <p className="text-sm text-slate-500 mt-2">
          Select a business before managing customers.
        </p>
      </div>
    );
  }

  return (
    <div>
      {/* Heading */}
      <div className="flex flex-wrap items-center justify-between gap-4 mb-6">
        <div>
          <h1 className="text-2xl font-bold text-slate-900">
            Customers
          </h1>

          <p className="text-sm text-slate-500 mt-1">
            Manage customers for {selectedBusiness.name}.
          </p>
        </div>

        <button
          onClick={openCreateForm}
          className="flex items-center gap-2 bg-blue-600 hover:bg-blue-700 text-white px-4 py-2.5 rounded-lg text-sm font-medium"
        >
          <Plus size={18} />
          Add Customer
        </button>
      </div>

      {/* Messages */}
      {success && (
        <div className="bg-green-50 border border-green-200 text-green-700 rounded-lg p-3 mb-5 text-sm">
          {success}
        </div>
      )}

      {error && (
        <div className="bg-red-50 border border-red-200 text-red-600 rounded-lg p-3 mb-5 text-sm">
          {error}
        </div>
      )}

      {/* Search */}
      <div className="bg-white border border-slate-200 rounded-xl p-4 mb-5">
        <div className="relative">
          <Search
            size={18}
            className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400"
          />

          <input
            type="text"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            placeholder="Search customer by name or email..."
            className="w-full border border-slate-300 rounded-lg pl-10 pr-3 py-2.5 text-sm outline-none focus:ring-2 focus:ring-blue-500"
          />
        </div>
      </div>

      {/* Customer list */}
      <div className="bg-white border border-slate-200 rounded-xl overflow-hidden">
        <div className="p-5 border-b border-slate-200 flex items-center justify-between">
          <h2 className="font-semibold text-slate-900">
            All Customers
          </h2>

          <span className="text-sm text-slate-500">
            {filteredCustomers.length} customer(s)
          </span>
        </div>

        {loading ? (
          <div className="p-10 text-center text-slate-500">
            Loading customers...
          </div>
        ) : filteredCustomers.length === 0 ? (
          <div className="p-10 text-center">
            <Users size={36} className="mx-auto text-slate-300" />

            <h3 className="font-semibold mt-3">
              No customers found
            </h3>

            <p className="text-sm text-slate-500 mt-1">
              Add a customer or change your search.
            </p>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-sm text-left">
              <thead className="bg-slate-50 text-slate-500">
                <tr>
                  <th className="px-5 py-4 font-medium">Customer</th>
                  <th className="px-5 py-4 font-medium">Email</th>
                  <th className="px-5 py-4 font-medium">Phone</th>
                  <th className="px-5 py-4 font-medium">Account</th>
                  <th className="px-5 py-4 font-medium">Action</th>
                </tr>
              </thead>

              <tbody>
                {filteredCustomers.map((customer) => (
                  <tr
                    key={customer.id}
                    className="border-t border-slate-100 hover:bg-slate-50"
                  >
                    <td className="px-5 py-4 font-medium text-slate-900">
                      {customer.name}
                    </td>

                    <td className="px-5 py-4 text-slate-600">
                      {customer.email}
                    </td>

                    <td className="px-5 py-4 text-slate-600">
                      {customer.phone || "—"}
                    </td>

                    <td className="px-5 py-4">
                      {customer.userId ? (
                        <span className="bg-green-100 text-green-700 px-2.5 py-1 rounded-full text-xs">
                          Connected
                        </span>
                      ) : invitedCustomerIds.has(customer.id) ? (
                        <span className="bg-blue-100 text-blue-700 px-2.5 py-1 rounded-full text-xs">
                          Invitation sent
                        </span>
                      ) : (
                        <span className="bg-slate-100 text-slate-600 px-2.5 py-1 rounded-full text-xs">
                          Not registered
                        </span>
                      )}
                    </td>

                    <td className="px-5 py-4">
                      <div className="flex items-center gap-4">
                        {!customer.userId && (
                          <button
                            onClick={() => handleInvite(customer)}
                            disabled={invitingCustomerId !== null}
                            className="flex items-center gap-1.5 text-blue-600 hover:text-blue-800 font-medium disabled:opacity-50"
                          >
                            {invitingCustomerId === customer.id ? (
                              <Loader2 size={15} className="animate-spin" />
                            ) : invitedCustomerIds.has(customer.id) ? (
                              <CheckCircle size={15} />
                            ) : (
                              <Mail size={15} />
                            )}

                            {invitingCustomerId === customer.id
                              ? "Sending..."
                              : invitedCustomerIds.has(customer.id)
                                ? "Resend"
                                : "Invite"}
                          </button>
                        )}

                        <button
                          onClick={() => openEditForm(customer)}
                          className="flex items-center gap-1.5 text-slate-600 hover:text-slate-900 font-medium"
                        >
                          <Pencil size={15} />
                          Edit
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Add/Edit customer modal */}
      {showForm && (
        <div className="fixed inset-0 z-50 bg-black/40 flex items-center justify-center p-4">
          <div className="bg-white rounded-2xl shadow-xl w-full max-w-lg">
            <div className="flex items-center justify-between p-5 border-b">
              <div>
                <h2 className="text-lg font-bold">
                  {editingCustomer ? "Edit Customer" : "Add Customer"}
                </h2>

                <p className="text-sm text-slate-500">
                  {editingCustomer
                    ? "Update customer information."
                    : "Add a new customer to your business."}
                </p>
              </div>

              <button
                type="button"
                onClick={() => setShowForm(false)}
                className="text-slate-400 hover:text-slate-700"
              >
                <X size={22} />
              </button>
            </div>

            <form onSubmit={handleSubmit} className="p-5 space-y-4">
              <div>
                <label className="block text-sm font-medium mb-2">
                  Full Name *
                </label>

                <input
                  required
                  maxLength={100}
                  value={form.name}
                  onChange={(e) =>
                    setForm({ ...form, name: e.target.value })
                  }
                  placeholder="Enter customer name"
                  className="w-full border border-slate-300 rounded-lg px-3 py-2.5 text-sm"
                />
              </div>

              <div>
                <label className="block text-sm font-medium mb-2">
                  Email Address *
                </label>

                <input
                  required
                  type="email"
                  maxLength={150}
                  value={form.email}
                  onChange={(e) =>
                    setForm({ ...form, email: e.target.value })
                  }
                  placeholder="customer@example.com"
                  className="w-full border border-slate-300 rounded-lg px-3 py-2.5 text-sm"
                />
              </div>

              <div>
                <label className="block text-sm font-medium mb-2">
                  Phone Number
                </label>

                <input
                  maxLength={20}
                  value={form.phone}
                  onChange={(e) =>
                    setForm({ ...form, phone: e.target.value })
                  }
                  placeholder="Enter phone number"
                  className="w-full border border-slate-300 rounded-lg px-3 py-2.5 text-sm"
                />
              </div>

              {formError && (
                <div className="bg-red-50 text-red-600 rounded-lg p-3 text-sm">
                  {formError}
                </div>
              )}

              <div className="flex justify-end gap-3 pt-3">
                <button
                  type="button"
                  onClick={() => setShowForm(false)}
                  className="px-4 py-2.5 rounded-lg border border-slate-300 text-sm"
                >
                  Cancel
                </button>

                <button
                  type="submit"
                  disabled={saving}
                  className="px-5 py-2.5 rounded-lg bg-blue-600 hover:bg-blue-700 disabled:bg-blue-300 text-white text-sm font-medium"
                >
                  {saving
                    ? "Saving..."
                    : editingCustomer
                      ? "Save Changes"
                      : "Add Customer"}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
