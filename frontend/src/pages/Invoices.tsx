
import { Eye, FileText, Plus, Search, X } from "lucide-react";
import {
  useEffect,
  useMemo,
  useState,
  type FormEvent,
} from "react";
import { useOutletContext } from "react-router-dom";

import type { Business } from "../types/business";
import type { Customer } from "../types/customer";
import type { Invoice } from "../types/invoice";

import {
  createInvoice,
  getBusinessInvoices,
  getMyInvoiceById,
  getMyInvoices,
} from "../services/invoiceService";

import { getBusinessCustomers } from "../services/customerService";

type DashboardContext = {
  mode: "business" | "customer";
  selectedBusiness: Business | null;
  businesses: Business[];
  loading: boolean;
};

type InvoiceForm = {
  customerId: string;
  invoiceNumber: string;
  description: string;
  amount: string;
  issueDate: string;
  dueDate: string;
};

const today = () => {
  const date = new Date();

  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, "0");
  const day = String(date.getDate()).padStart(2, "0");

  return `${year}-${month}-${day}`;
};

const emptyForm = (): InvoiceForm => ({
  customerId: "",
  invoiceNumber: "",
  description: "",
  amount: "",
  issueDate: today(),
  dueDate: "",
});

const formatCurrency = (amount: number) =>
  new Intl.NumberFormat("en-IN", {
    style: "currency",
    currency: "INR",
    maximumFractionDigits: 2,
  }).format(Number(amount || 0));

const getErrorMessage = (error: any) => {
  const data = error.response?.data;

  if (typeof data === "string") return data;
  if (data?.message) return data.message;

  return "Something went wrong. Please try again.";
};

export default function Invoices() {
  const {
    mode,
    selectedBusiness,
    loading: businessLoading,
  } = useOutletContext<DashboardContext>();

  const [invoices, setInvoices] = useState<Invoice[]>([]);
  const [customers, setCustomers] = useState<Customer[]>([]);

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  const [search, setSearch] = useState("");
  const [statusFilter, setStatusFilter] = useState("ALL");

  const [showForm, setShowForm] = useState(false);
  const [saving, setSaving] = useState(false);
  const [formError, setFormError] = useState("");
  const [form, setForm] = useState<InvoiceForm>(emptyForm());

  const [selectedInvoice, setSelectedInvoice] =
    useState<Invoice | null>(null);

  const [detailLoading, setDetailLoading] = useState(false);

  // Load data according to the selected workspace mode.
  useEffect(() => {
    let cancelled = false;

    const loadData = async () => {
      // Wait for business information only in Business Mode.
      if (mode === "business" && businessLoading) {
        return;
      }

      if (mode === "business" && !selectedBusiness) {
        setInvoices([]);
        setCustomers([]);
        setLoading(false);
        return;
      }

      try {
        setLoading(true);
        setError("");
        setSuccess("");

        setInvoices([]);
        setCustomers([]);
        setSelectedInvoice(null);

        if (mode === "customer") {
          // The backend identifies the logged-in customer from JWT.
          const invoiceData = await getMyInvoices();

          if (!cancelled) {
            setInvoices(invoiceData);
          }
        } else if (selectedBusiness) {
          const [invoiceData, customerData] = await Promise.all([
            getBusinessInvoices(selectedBusiness.id),
            getBusinessCustomers(selectedBusiness.id),
          ]);

          if (!cancelled) {
            setInvoices(invoiceData);
            setCustomers(customerData);
          }
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

    loadData();

    return () => {
      cancelled = true;
    };
  }, [mode, selectedBusiness?.id, businessLoading]);

  // Search and status filtering.
  const filteredInvoices = useMemo(() => {
    return invoices.filter((invoice) => {
      const matchesSearch =
        (invoice.invoiceNumber || "")
          .toLowerCase()
          .includes(search.toLowerCase()) ||
        (invoice.description || "")
          .toLowerCase()
          .includes(search.toLowerCase());

      const matchesStatus =
        statusFilter === "ALL" ||
        invoice.status === statusFilter;

      return matchesSearch && matchesStatus;
    });
  }, [invoices, search, statusFilter]);

  // Create invoice: Business Mode only.
  const handleCreateInvoice = async (
    e: FormEvent<HTMLFormElement>
  ) => {
    e.preventDefault();

    if (mode !== "business" || !selectedBusiness) {
      return;
    }

    if (!form.customerId) {
      setFormError("Please select a customer.");
      return;
    }

    if (form.dueDate < form.issueDate) {
      setFormError("Due date cannot be before issue date.");
      return;
    }

    try {
      setSaving(true);
      setFormError("");
      setSuccess("");

      await createInvoice(selectedBusiness.id, {
        customerId: Number(form.customerId),
        invoiceNumber: form.invoiceNumber.trim(),
        description: form.description.trim(),
        amount: Number(form.amount),
        issueDate: form.issueDate,
        dueDate: form.dueDate,
      });

      const updatedInvoices = await getBusinessInvoices(
        selectedBusiness.id
      );

      setInvoices(updatedInvoices);
      setForm(emptyForm());
      setShowForm(false);

      setSuccess("Invoice created successfully!");
    } catch (err: any) {
      setFormError(getErrorMessage(err));
    } finally {
      setSaving(false);
    }
  };

  // Fetch invoice details using the appropriate endpoint.
  const handleViewInvoice = async (invoiceId: number) => {
    try {
      setDetailLoading(true);
      setError("");

      let invoice: Invoice;

      if (mode === "customer") {
        invoice = await getMyInvoiceById(invoiceId);
      } else {
        if (!selectedBusiness) return;

        const { getInvoiceById } = await import(
          "../services/invoiceService"
        );

        invoice = await getInvoiceById(
          selectedBusiness.id,
          invoiceId
        );
      }

      setSelectedInvoice(invoice);
    } catch (err: any) {
      setError(getErrorMessage(err));
    } finally {
      setDetailLoading(false);
    }
  };

  const statusStyle = (status: string) => {
    switch (status) {
      case "PAID":
        return "bg-green-100 text-green-700";

      case "PARTIALLY_PAID":
        return "bg-yellow-100 text-yellow-700";

      case "UNPAID":
        return "bg-red-100 text-red-700";

      default:
        return "bg-slate-100 text-slate-600";
    }
  };

  if (mode === "business" && businessLoading) {
    return <p className="text-slate-500">Loading business...</p>;
  }

  if (mode === "business" && !selectedBusiness) {
    return (
      <div className="bg-white border rounded-xl p-6">
        <h2 className="font-semibold">No business selected</h2>
        <p className="text-sm text-slate-500 mt-2">
          Select a business before managing invoices.
        </p>
      </div>
    );
  }

  return (
    <div>
      {/* Page heading */}
      <div className="flex flex-wrap items-center justify-between gap-4 mb-6">
        <div>
          <h1 className="text-2xl font-bold text-slate-900">
            {mode === "business" ? "Business Invoices" : "My Invoices"}
          </h1>

          <p className="text-sm text-slate-500 mt-1">
            {mode === "business"
              ? `Manage invoices for ${selectedBusiness?.name}.`
              : "View invoices issued to you by connected businesses."}
          </p>
        </div>

        {mode === "business" && (
          <button
            onClick={() => {
              setForm(emptyForm());
              setFormError("");
              setShowForm(true);
            }}
            className="flex items-center gap-2 bg-blue-600 hover:bg-blue-700 text-white px-4 py-2.5 rounded-lg text-sm font-medium"
          >
            <Plus size={18} />
            Create Invoice
          </button>
        )}
      </div>

      {/* Success message */}
      {success && (
        <div className="bg-green-50 border border-green-200 text-green-700 rounded-lg p-3 mb-5 text-sm">
          {success}
        </div>
      )}

      {/* API error */}
      {error && (
        <div className="bg-red-50 border border-red-200 text-red-600 rounded-lg p-3 mb-5 text-sm">
          {error}
        </div>
      )}

      {/* Search and filters */}
      <div className="bg-white border border-slate-200 rounded-xl p-4 mb-5 flex flex-wrap gap-3 justify-between">
        <div className="relative flex-1 min-w-48">
          <Search
            size={18}
            className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400"
          />

          <input
            type="text"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            placeholder="Search invoice number or description..."
            className="w-full border border-slate-300 rounded-lg pl-10 pr-3 py-2.5 text-sm outline-none focus:ring-2 focus:ring-blue-500"
          />
        </div>

        <select
          value={statusFilter}
          onChange={(e) => setStatusFilter(e.target.value)}
          className="border border-slate-300 rounded-lg px-3 py-2.5 text-sm bg-white"
        >
          <option value="ALL">All statuses</option>
          <option value="UNPAID">Unpaid</option>
          <option value="PARTIALLY_PAID">Partially Paid</option>
          <option value="PAID">Paid</option>
        </select>
      </div>

      {/* Invoice list */}
      <div className="bg-white border border-slate-200 rounded-xl overflow-hidden">
        <div className="p-5 border-b border-slate-200 flex items-center justify-between">
          <h2 className="font-semibold text-slate-900">
            {mode === "business" ? "All Invoices" : "Invoices Issued to Me"}
          </h2>

          <span className="text-sm text-slate-500">
            {filteredInvoices.length} invoice(s)
          </span>
        </div>

        {loading ? (
          <div className="p-10 text-center text-slate-500">
            Loading invoices...
          </div>
        ) : filteredInvoices.length === 0 ? (
          <div className="p-10 text-center">
            <FileText size={36} className="mx-auto text-slate-300" />

            <h3 className="font-semibold mt-3">
              No invoices found
            </h3>

            <p className="text-sm text-slate-500 mt-1">
              {mode === "business"
                ? "Create an invoice or change your search/filter."
                : "You don't have any invoices from connected businesses yet."}
            </p>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-sm text-left">
              <thead className="bg-slate-50 text-slate-500">
                <tr>
                  <th className="px-5 py-4 font-medium">Invoice</th>
                  <th className="px-5 py-4 font-medium">Description</th>
                  <th className="px-5 py-4 font-medium">Amount</th>
                  <th className="px-5 py-4 font-medium">Due Date</th>
                  <th className="px-5 py-4 font-medium">Outstanding</th>
                  <th className="px-5 py-4 font-medium">Status</th>
                  <th className="px-5 py-4 font-medium">Action</th>
                </tr>
              </thead>

              <tbody>
                {filteredInvoices.map((invoice) => (
                  <tr
                    key={invoice.id}
                    className="border-t border-slate-100 hover:bg-slate-50"
                  >
                    <td className="px-5 py-4 font-medium text-slate-900">
                      {invoice.invoiceNumber}
                    </td>

                    <td className="px-5 py-4 text-slate-600">
                      {invoice.description || "—"}
                    </td>

                    <td className="px-5 py-4">
                      {formatCurrency(invoice.amount)}
                    </td>

                    <td className="px-5 py-4 text-slate-600">
                      {invoice.dueDate}
                    </td>

                    <td className="px-5 py-4">
                      {formatCurrency(invoice.outstandingAmount)}
                    </td>

                    <td className="px-5 py-4">
                      <span
                        className={`px-2.5 py-1 rounded-full text-xs font-medium ${statusStyle(invoice.status)}`}
                      >
                        {invoice.status.replace("_", " ")}
                      </span>
                    </td>

                    <td className="px-5 py-4">
                      <button
                        onClick={() => handleViewInvoice(invoice.id)}
                        className="inline-flex items-center gap-1.5 text-blue-600 hover:text-blue-800 font-medium"
                      >
                        <Eye size={16} />
                        View
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Invoice details modal */}
      {selectedInvoice && (
        <div className="fixed inset-0 z-50 bg-black/40 flex items-center justify-center p-4">
          <div className="bg-white rounded-2xl shadow-xl w-full max-w-lg">
            <div className="flex items-center justify-between p-5 border-b">
              <div>
                <h2 className="text-lg font-bold">
                  Invoice Details
                </h2>
                <p className="text-sm text-slate-500">
                  {selectedInvoice.invoiceNumber}
                </p>
              </div>

              <button
                onClick={() => setSelectedInvoice(null)}
                className="text-slate-400 hover:text-slate-700"
              >
                <X size={22} />
              </button>
            </div>

            <div className="p-5 space-y-4 text-sm">
              <Detail label="Invoice Number" value={selectedInvoice.invoiceNumber} />
              <Detail label="Description" value={selectedInvoice.description || "—"} />
              <Detail label="Issue Date" value={selectedInvoice.issueDate} />
              <Detail label="Due Date" value={selectedInvoice.dueDate} />
              <Detail label="Invoice Amount" value={formatCurrency(selectedInvoice.amount)} />
              <Detail label="Paid Amount" value={formatCurrency(selectedInvoice.paidAmount)} />
              <Detail label="Outstanding Amount" value={formatCurrency(selectedInvoice.outstandingAmount)} />

              <div className="flex items-center justify-between pt-2">
                <span className="text-slate-500">Payment Status</span>
                <span
                  className={`px-2.5 py-1 rounded-full text-xs font-medium ${statusStyle(selectedInvoice.status)}`}
                >
                  {selectedInvoice.status.replace("_", " ")}
                </span>
              </div>
            </div>

            <div className="p-5 border-t flex justify-end">
              <button
                onClick={() => setSelectedInvoice(null)}
                className="px-4 py-2 rounded-lg border text-sm"
              >
                Close
              </button>
            </div>
          </div>
        </div>
      )}

      {detailLoading && (
        <div className="fixed inset-0 z-40 bg-black/20 flex items-center justify-center">
          <div className="bg-white rounded-lg px-5 py-3 shadow text-sm">
            Loading invoice details...
          </div>
        </div>
      )}

      {/* Create invoice modal: Business Mode only */}
      {mode === "business" && showForm && (
        <div className="fixed inset-0 z-50 bg-black/40 flex items-center justify-center p-4">
          <div className="bg-white rounded-2xl shadow-xl w-full max-w-xl max-h-[90vh] overflow-y-auto">
            <div className="flex items-center justify-between p-5 border-b">
              <div>
                <h2 className="text-lg font-bold">
                  Create Invoice
                </h2>
                <p className="text-sm text-slate-500">
                  Add a new invoice for {selectedBusiness?.name}.
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

            <form onSubmit={handleCreateInvoice} className="p-5 space-y-4">
              <div>
                <label className="block text-sm font-medium mb-2">
                  Customer *
                </label>

                <select
                  required
                  value={form.customerId}
                  onChange={(e) =>
                    setForm({ ...form, customerId: e.target.value })
                  }
                  className="w-full border border-slate-300 rounded-lg px-3 py-2.5 text-sm"
                >
                  <option value="">Select customer</option>

                  {customers.map((customer) => (
                    <option key={customer.id} value={customer.id}>
                      {customer.name} ({customer.email})
                    </option>
                  ))}
                </select>

                {customers.length === 0 && (
                  <p className="text-xs text-red-500 mt-1">
                    No customers found. Add a customer before creating an invoice.
                  </p>
                )}
              </div>

              <div>
                <label className="block text-sm font-medium mb-2">
                  Invoice Number *
                </label>

                <input
                  required
                  maxLength={100}
                  value={form.invoiceNumber}
                  onChange={(e) =>
                    setForm({ ...form, invoiceNumber: e.target.value })
                  }
                  placeholder="e.g. REC-2026-001"
                  className="w-full border border-slate-300 rounded-lg px-3 py-2.5 text-sm"
                />
              </div>

              <div>
                <label className="block text-sm font-medium mb-2">
                  Description
                </label>

                <textarea
                  maxLength={1000}
                  rows={3}
                  value={form.description}
                  onChange={(e) =>
                    setForm({ ...form, description: e.target.value })
                  }
                  placeholder="Enter invoice description"
                  className="w-full border border-slate-300 rounded-lg px-3 py-2.5 text-sm resize-none"
                />
              </div>

              <div>
                <label className="block text-sm font-medium mb-2">
                  Amount (₹) *
                </label>

                <input
                  required
                  type="number"
                  min="0.01"
                  max="9999999999.99"
                  step="0.01"
                  value={form.amount}
                  onChange={(e) =>
                    setForm({ ...form, amount: e.target.value })
                  }
                  placeholder="Enter invoice amount"
                  className="w-full border border-slate-300 rounded-lg px-3 py-2.5 text-sm"
                />
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium mb-2">
                    Issue Date *
                  </label>

                  <input
                    required
                    type="date"
                    value={form.issueDate}
                    onChange={(e) =>
                      setForm({ ...form, issueDate: e.target.value })
                    }
                    className="w-full border border-slate-300 rounded-lg px-3 py-2.5 text-sm"
                  />
                </div>

                <div>
                  <label className="block text-sm font-medium mb-2">
                    Due Date *
                  </label>

                  <input
                    required
                    type="date"
                    min={form.issueDate}
                    value={form.dueDate}
                    onChange={(e) =>
                      setForm({ ...form, dueDate: e.target.value })
                    }
                    className="w-full border border-slate-300 rounded-lg px-3 py-2.5 text-sm"
                  />
                </div>
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
                  disabled={saving || customers.length === 0}
                  className="px-5 py-2.5 rounded-lg bg-blue-600 hover:bg-blue-700 disabled:bg-blue-300 text-white text-sm font-medium"
                >
                  {saving ? "Creating..." : "Create Invoice"}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}

function Detail({ label, value }: { label: string; value: string }) {
  return (
    <div className="flex items-start justify-between gap-4">
      <span className="text-slate-500">{label}</span>
      <span className="font-medium text-slate-900 text-right">
        {value}
      </span>
    </div>
  );
}
