
import {
  CheckCircle,
  CreditCard,
  Plus,
  Search,
  X,
  XCircle,
} from "lucide-react";

import {
  useEffect,
  useMemo,
  useState,
} from "react";

import { useOutletContext } from "react-router-dom";

import type { Business } from "../types/business";
import type { Invoice } from "../types/invoice";
import type {
  Payment,
  PaymentStatus,
} from "../types/payment";

import {
  getBusinessPayments,
  getMyPayments,
  rejectPayment,
  submitMyPaymentClaim,
  verifyPayment,
} from "../services/paymentService";

import { getMyInvoices } from "../services/invoiceService";

type DashboardContext = {
  mode: "business" | "customer";
  selectedBusiness: Business | null;
  businesses: Business[];
  loading: boolean;
};

const formatCurrency = (amount: number) =>
  new Intl.NumberFormat("en-IN", {
    style: "currency",
    currency: "INR",
    maximumFractionDigits: 2,
  }).format(Number(amount || 0));

const getToday = () => {
  const date = new Date();
  const offset = date.getTimezoneOffset();

  return new Date(date.getTime() - offset * 60000)
    .toISOString()
    .split("T")[0];
};

const getErrorMessage = (error: any) => {
  const data = error.response?.data;

  if (typeof data === "string") return data;
  if (data?.message) return data.message;

  return "Something went wrong. Please try again.";
};

export default function Payments() {
  const {
    mode,
    selectedBusiness,
    loading: businessLoading,
  } = useOutletContext<DashboardContext>();

  const [payments, setPayments] = useState<Payment[]>([]);
  const [invoices, setInvoices] = useState<Invoice[]>([]);

  const [loading, setLoading] = useState(false);
  const [actionLoading, setActionLoading] = useState<number | null>(null);
  const [submittingClaim, setSubmittingClaim] = useState(false);

  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  const [search, setSearch] = useState("");
  const [statusFilter, setStatusFilter] = useState("ALL");

  const [rejectingPayment, setRejectingPayment] =
    useState<Payment | null>(null);

  const [rejectionReason, setRejectionReason] = useState("");

  // Customer payment claim form
  const [showClaimForm, setShowClaimForm] = useState(false);
  const [selectedInvoiceId, setSelectedInvoiceId] = useState("");
  const [claimAmount, setClaimAmount] = useState("");
  const [paymentDate, setPaymentDate] = useState(getToday());
  const [paymentMethod, setPaymentMethod] = useState("UPI");
  const [transactionReference, setTransactionReference] = useState("");

  const availableInvoices = useMemo(
    () =>
      invoices.filter(
        (invoice) => Number(invoice.outstandingAmount) > 0
      ),
    [invoices]
  );

  const selectedInvoice = useMemo(
    () =>
      availableInvoices.find(
        (invoice) => String(invoice.id) === selectedInvoiceId
      ) ?? null,
    [availableInvoices, selectedInvoiceId]
  );

  // Load the correct payment data for the selected mode.
  useEffect(() => {
    let cancelled = false;

    const load = async () => {
      if (mode === "business" && businessLoading) {
        return;
      }

      if (mode === "business" && !selectedBusiness) {
        setPayments([]);
        setInvoices([]);
        setLoading(false);
        return;
      }

      try {
        setLoading(true);
        setError("");
        setSuccess("");
        setPayments([]);
        setInvoices([]);

        if (mode === "customer") {
          const [paymentData, invoiceData] = await Promise.all([
            getMyPayments(),
            getMyInvoices(),
          ]);

          if (!cancelled) {
            setPayments(paymentData);
            setInvoices(invoiceData);
          }
        } else {
          const paymentData = await getBusinessPayments(
            selectedBusiness!.id
          );

          if (!cancelled) {
            setPayments(paymentData);
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

    load();

    return () => {
      cancelled = true;
    };
  }, [mode, selectedBusiness?.id, businessLoading]);

  const loadBusinessPayments = async () => {
    if (mode !== "business" || !selectedBusiness) return;

    const data = await getBusinessPayments(selectedBusiness.id);
    setPayments(data);
  };

  const loadCustomerData = async () => {
    const [paymentData, invoiceData] = await Promise.all([
      getMyPayments(),
      getMyInvoices(),
    ]);

    setPayments(paymentData);
    setInvoices(invoiceData);
  };

  // Search and status filters.
  const filteredPayments = useMemo(() => {
    const query = search.toLowerCase();

    return payments.filter((payment) => {
      const matchesSearch =
        String(payment.invoiceId).includes(query) ||
        String(payment.id).includes(query) ||
        (payment.transactionReference || "")
          .toLowerCase()
          .includes(query);

      const matchesStatus =
        statusFilter === "ALL" ||
        payment.verificationStatus === statusFilter;

      return matchesSearch && matchesStatus;
    });
  }, [payments, search, statusFilter]);

  const counts = {
    ALL: payments.length,
    PENDING: payments.filter(
      (p) => p.verificationStatus === "PENDING"
    ).length,
    VERIFIED: payments.filter(
      (p) => p.verificationStatus === "VERIFIED"
    ).length,
    REJECTED: payments.filter(
      (p) => p.verificationStatus === "REJECTED"
    ).length,
  };

  // Business Mode only.
  const handleVerify = async (payment: Payment) => {
    if (mode !== "business" || !selectedBusiness) return;

    const confirmed = window.confirm(
      `Verify payment #${payment.id} of ${formatCurrency(
        Number(payment.amount)
      )}?`
    );

    if (!confirmed) return;

    try {
      setActionLoading(payment.id);
      setError("");
      setSuccess("");

      await verifyPayment(selectedBusiness.id, payment.id);
      await loadBusinessPayments();

      setSuccess("Payment verified successfully!");
    } catch (err: any) {
      setError(getErrorMessage(err));
    } finally {
      setActionLoading(null);
    }
  };

  // Business Mode only.
  const handleReject = async () => {
    if (
      mode !== "business" ||
      !selectedBusiness ||
      !rejectingPayment
    ) {
      return;
    }

    if (!rejectionReason.trim()) {
      setError("Please provide a rejection reason.");
      return;
    }

    try {
      setActionLoading(rejectingPayment.id);
      setError("");
      setSuccess("");

      await rejectPayment(
        selectedBusiness.id,
        rejectingPayment.id,
        rejectionReason.trim()
      );

      await loadBusinessPayments();

      setRejectingPayment(null);
      setRejectionReason("");

      setSuccess("Payment rejected successfully!");
    } catch (err: any) {
      setError(getErrorMessage(err));
    } finally {
      setActionLoading(null);
    }
  };

  // Customer Mode: submit a payment claim.
  const handleSubmitClaim = async (
    event: React.FormEvent<HTMLFormElement>
  ) => {
    event.preventDefault();

    if (!selectedInvoice) {
      setError("Please select an invoice.");
      return;
    }

    const amount = Number(claimAmount);

    if (!Number.isFinite(amount) || amount <= 0) {
      setError("Enter a valid payment amount.");
      return;
    }

    if (amount > Number(selectedInvoice.outstandingAmount)) {
      setError("Payment amount cannot exceed the outstanding balance.");
      return;
    }

    try {
      setSubmittingClaim(true);
      setError("");
      setSuccess("");

      await submitMyPaymentClaim(selectedInvoice.businessId, {
        invoiceId: selectedInvoice.id,
        amount,
        paymentDate,
        paymentMethod: paymentMethod.trim(),
        transactionReference: transactionReference.trim(),
      });

      await loadCustomerData();

      setShowClaimForm(false);
      setSelectedInvoiceId("");
      setClaimAmount("");
      setPaymentDate(getToday());
      setPaymentMethod("UPI");
      setTransactionReference("");

      setSuccess(
        "Payment claim submitted successfully. It is awaiting business verification."
      );
    } catch (err: any) {
      setError(getErrorMessage(err));
    } finally {
      setSubmittingClaim(false);
    }
  };

  const closeClaimForm = () => {
    if (submittingClaim) return;

    setShowClaimForm(false);
    setSelectedInvoiceId("");
    setClaimAmount("");
    setPaymentDate(getToday());
    setPaymentMethod("UPI");
    setTransactionReference("");
    setError("");
  };

  const statusStyle = (status: PaymentStatus) => {
    switch (status) {
      case "VERIFIED":
        return "bg-green-100 text-green-700";

      case "PENDING":
        return "bg-yellow-100 text-yellow-700";

      case "REJECTED":
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
          Select a business before managing payments.
        </p>
      </div>
    );
  }

  return (
    <div>
      {/* Heading */}
      <div className="mb-6 flex flex-wrap items-center justify-between gap-3">
        <div>
          <h1 className="text-2xl font-bold text-slate-900">
            {mode === "business" ? "Payments" : "My Payment History"}
          </h1>

          <p className="text-sm text-slate-500 mt-1">
            {mode === "business"
              ? `Review payment claims for ${selectedBusiness?.name}.`
              : "View your payment history and submit new payment claims."}
          </p>
        </div>

        {mode === "customer" && (
          <button
            onClick={() => {
              setError("");
              setSuccess("");
              setShowClaimForm(true);
            }}
            disabled={availableInvoices.length === 0}
            className="inline-flex items-center gap-2 bg-blue-600 hover:bg-blue-700 text-white px-4 py-2.5 rounded-lg text-sm font-medium disabled:opacity-50 disabled:cursor-not-allowed"
          >
            <Plus size={17} />
            Submit Payment Claim
          </button>
        )}
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

      {mode === "customer" && availableInvoices.length === 0 && (
        <div className="bg-blue-50 border border-blue-200 text-blue-700 rounded-lg p-3 mb-5 text-sm">
          You currently have no invoices with an outstanding balance.
        </div>
      )}

      {/* Status summary */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4 mb-5">
        {[
          { label: "All Payments", value: counts.ALL },
          { label: "Pending", value: counts.PENDING },
          { label: "Verified", value: counts.VERIFIED },
          { label: "Rejected", value: counts.REJECTED },
        ].map((item) => (
          <div
            key={item.label}
            className="bg-white border border-slate-200 rounded-xl p-4"
          >
            <p className="text-sm text-slate-500">{item.label}</p>
            <h2 className="text-2xl font-bold mt-2">
              {item.value}
            </h2>
          </div>
        ))}
      </div>

      {/* Search and filter */}
      <div className="bg-white border border-slate-200 rounded-xl p-4 mb-5 flex flex-wrap gap-3">
        <div className="relative flex-1 min-w-48">
          <Search
            size={18}
            className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400"
          />

          <input
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            placeholder="Search payment ID, invoice ID or reference..."
            className="w-full border border-slate-300 rounded-lg pl-10 pr-3 py-2.5 text-sm"
          />
        </div>

        <select
          value={statusFilter}
          onChange={(e) => setStatusFilter(e.target.value)}
          className="border border-slate-300 rounded-lg px-3 py-2.5 text-sm bg-white"
        >
          <option value="ALL">All statuses</option>
          <option value="PENDING">Pending</option>
          <option value="VERIFIED">Verified</option>
          <option value="REJECTED">Rejected</option>
        </select>
      </div>

      {/* Payments table */}
      <div className="bg-white border border-slate-200 rounded-xl overflow-hidden">
        <div className="p-5 border-b flex items-center justify-between">
          <h2 className="font-semibold">
            {mode === "business"
              ? "All Business Payments"
              : "My Payments"}
          </h2>

          <span className="text-sm text-slate-500">
            {filteredPayments.length} payment(s)
          </span>
        </div>

        {loading ? (
          <div className="p-10 text-center text-slate-500">
            Loading payments...
          </div>
        ) : filteredPayments.length === 0 ? (
          <div className="p-10 text-center">
            <CreditCard
              size={36}
              className="mx-auto text-slate-300"
            />

            <h3 className="font-semibold mt-3">
              No payments found
            </h3>

            <p className="text-sm text-slate-500 mt-1">
              {mode === "business"
                ? "Payment claims will appear here."
                : "Payments you submit will appear here."}
            </p>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-sm text-left">
              <thead className="bg-slate-50 text-slate-500">
                <tr>
                  <th className="px-5 py-4 font-medium">Payment</th>
                  <th className="px-5 py-4 font-medium">Invoice ID</th>
                  <th className="px-5 py-4 font-medium">Amount</th>
                  <th className="px-5 py-4 font-medium">Date</th>
                  <th className="px-5 py-4 font-medium">Method</th>
                  <th className="px-5 py-4 font-medium">Reference</th>
                  <th className="px-5 py-4 font-medium">Status</th>

                  {mode === "business" && (
                    <th className="px-5 py-4 font-medium">Action</th>
                  )}
                </tr>
              </thead>

              <tbody>
                {filteredPayments.map((payment) => (
                  <tr
                    key={payment.id}
                    className="border-t border-slate-100 hover:bg-slate-50"
                  >
                    <td className="px-5 py-4 font-medium">
                      #{payment.id}
                    </td>

                    <td className="px-5 py-4">
                      #{payment.invoiceId}
                    </td>

                    <td className="px-5 py-4">
                      {formatCurrency(Number(payment.amount))}
                    </td>

                    <td className="px-5 py-4 text-slate-600">
                      {payment.paymentDate}
                    </td>

                    <td className="px-5 py-4 text-slate-600">
                      {payment.paymentMethod || "—"}
                    </td>

                    <td className="px-5 py-4 text-slate-600">
                      {payment.transactionReference || "—"}
                    </td>

                    <td className="px-5 py-4">
                      <span
                        className={`px-2.5 py-1 rounded-full text-xs font-medium ${statusStyle(
                          payment.verificationStatus
                        )}`}
                      >
                        {payment.verificationStatus}
                      </span>
                    </td>

                    {mode === "business" && (
                      <td className="px-5 py-4">
                        {payment.verificationStatus === "PENDING" ? (
                          <div className="flex items-center gap-3">
                            <button
                              disabled={actionLoading === payment.id}
                              onClick={() => handleVerify(payment)}
                              className="flex items-center gap-1 text-green-600 hover:text-green-800 disabled:opacity-50"
                            >
                              <CheckCircle size={15} />
                              Verify
                            </button>

                            <button
                              disabled={actionLoading === payment.id}
                              onClick={() => {
                                setRejectingPayment(payment);
                                setRejectionReason("");
                              }}
                              className="flex items-center gap-1 text-red-600 hover:text-red-800 disabled:opacity-50"
                            >
                              <XCircle size={15} />
                              Reject
                            </button>
                          </div>
                        ) : (
                          <span className="text-slate-400">—</span>
                        )}
                      </td>
                    )}
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Customer payment claim modal */}
      {mode === "customer" && showClaimForm && (
        <div className="fixed inset-0 z-50 bg-black/40 flex items-center justify-center p-4 overflow-y-auto">
          <div className="bg-white rounded-2xl shadow-xl w-full max-w-lg p-6 my-8">
            <div className="flex items-center justify-between mb-5">
              <div>
                <h2 className="text-lg font-bold">
                  Submit Payment Claim
                </h2>
                <p className="text-sm text-slate-500 mt-1">
                  Enter the details of the payment you made.
                </p>
              </div>

              <button
                type="button"
                onClick={closeClaimForm}
                disabled={submittingClaim}
                className="text-slate-400 hover:text-slate-700 disabled:opacity-50"
              >
                <X size={21} />
              </button>
            </div>

            <form onSubmit={handleSubmitClaim} className="space-y-4">
              {/* Invoice */}
              <div>
                <label className="block text-sm font-medium mb-2">
                  Select Invoice *
                </label>

                <select
                  required
                  value={selectedInvoiceId}
                  onChange={(e) => {
                    setSelectedInvoiceId(e.target.value);
                    setClaimAmount("");
                  }}
                  className="w-full border border-slate-300 rounded-lg px-3 py-2.5 text-sm bg-white"
                >
                  <option value="">Choose an invoice</option>

                  {availableInvoices.map((invoice) => (
                    <option key={invoice.id} value={invoice.id}>
                      {invoice.invoiceNumber} — Outstanding:{" "}
                      {formatCurrency(Number(invoice.outstandingAmount))}
                    </option>
                  ))}
                </select>
              </div>

              {/* Selected invoice balance */}
              {selectedInvoice && (
                <div className="bg-blue-50 border border-blue-100 rounded-lg p-3">
                  <div className="flex justify-between text-sm">
                    <span className="text-slate-600">
                      Invoice amount
                    </span>
                    <span className="font-medium">
                      {formatCurrency(Number(selectedInvoice.amount))}
                    </span>
                  </div>

                  <div className="flex justify-between text-sm mt-2">
                    <span className="text-slate-600">
                      Already paid
                    </span>
                    <span className="font-medium">
                      {formatCurrency(Number(selectedInvoice.paidAmount))}
                    </span>
                  </div>

                  <div className="flex justify-between text-sm mt-2 pt-2 border-t border-blue-200">
                    <span className="font-semibold">
                      Outstanding balance
                    </span>
                    <span className="font-bold text-blue-700">
                      {formatCurrency(
                        Number(selectedInvoice.outstandingAmount)
                      )}
                    </span>
                  </div>
                </div>
              )}

              {/* Amount */}
              <div>
                <label className="block text-sm font-medium mb-2">
                  Payment Amount *
                </label>

                <input
                  type="number"
                  required
                  min="0.01"
                  max={selectedInvoice?.outstandingAmount}
                  step="0.01"
                  value={claimAmount}
                  onChange={(e) => setClaimAmount(e.target.value)}
                  placeholder="Enter amount paid"
                  className="w-full border border-slate-300 rounded-lg px-3 py-2.5 text-sm"
                />

                {selectedInvoice && (
                  <p className="text-xs text-slate-500 mt-1">
                    Maximum:{" "}
                    {formatCurrency(
                      Number(selectedInvoice.outstandingAmount)
                    )}
                  </p>
                )}
              </div>

              {/* Payment date */}
              <div>
                <label className="block text-sm font-medium mb-2">
                  Payment Date *
                </label>

                <input
                  type="date"
                  required
                  max={getToday()}
                  value={paymentDate}
                  onChange={(e) => setPaymentDate(e.target.value)}
                  className="w-full border border-slate-300 rounded-lg px-3 py-2.5 text-sm"
                />
              </div>

              {/* Payment method */}
              <div>
                <label className="block text-sm font-medium mb-2">
                  Payment Method
                </label>

                <select
                  value={paymentMethod}
                  onChange={(e) => setPaymentMethod(e.target.value)}
                  className="w-full border border-slate-300 rounded-lg px-3 py-2.5 text-sm bg-white"
                >
                  <option value="UPI">UPI</option>
                  <option value="BANK_TRANSFER">Bank Transfer</option>
                  <option value="CASH">Cash</option>
                  <option value="CHEQUE">Cheque</option>
                  <option value="CARD">Card</option>
                  <option value="OTHER">Other</option>
                </select>
              </div>

              {/* Transaction reference */}
              <div>
                <label className="block text-sm font-medium mb-2">
                  Transaction Reference
                </label>

                <input
                  type="text"
                  maxLength={150}
                  value={transactionReference}
                  onChange={(e) =>
                    setTransactionReference(e.target.value)
                  }
                  placeholder="Enter UTR / transaction ID (if available)"
                  className="w-full border border-slate-300 rounded-lg px-3 py-2.5 text-sm"
                />
              </div>

              <div className="bg-yellow-50 border border-yellow-200 rounded-lg p-3 text-xs text-yellow-800">
                Your submission is a payment claim, not an automatic
                payment confirmation. The business must verify it before
                your invoice balance is updated.
              </div>

              <div className="flex justify-end gap-3 pt-2">
                <button
                  type="button"
                  onClick={closeClaimForm}
                  disabled={submittingClaim}
                  className="px-4 py-2.5 rounded-lg border text-sm"
                >
                  Cancel
                </button>

                <button
                  type="submit"
                  disabled={
                    submittingClaim ||
                    !selectedInvoice ||
                    !claimAmount ||
                    Number(claimAmount) <= 0 ||
                    Number(claimAmount) >
                      Number(selectedInvoice?.outstandingAmount ?? 0)
                  }
                  className="px-5 py-2.5 rounded-lg bg-blue-600 hover:bg-blue-700 text-white text-sm font-medium disabled:opacity-50"
                >
                  {submittingClaim
                    ? "Submitting..."
                    : "Submit Claim"}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Rejection modal: Business Mode only */}
      {mode === "business" && rejectingPayment && (
        <div className="fixed inset-0 z-50 bg-black/40 flex items-center justify-center p-4">
          <div className="bg-white rounded-2xl shadow-xl w-full max-w-md p-5">
            <h2 className="text-lg font-bold">
              Reject Payment
            </h2>

            <p className="text-sm text-slate-500 mt-1">
              Payment #{rejectingPayment.id} —{" "}
              {formatCurrency(Number(rejectingPayment.amount))}
            </p>

            <label className="block text-sm font-medium mt-5 mb-2">
              Rejection Reason *
            </label>

            <textarea
              rows={4}
              maxLength={500}
              required
              value={rejectionReason}
              onChange={(e) => setRejectionReason(e.target.value)}
              placeholder="Enter a reason for rejecting this payment..."
              className="w-full border border-slate-300 rounded-lg px-3 py-2.5 text-sm resize-none"
            />

            <p className="text-xs text-slate-400 mt-1">
              Maximum 500 characters.
            </p>

            <div className="flex justify-end gap-3 mt-5">
              <button
                onClick={() => setRejectingPayment(null)}
                className="px-4 py-2.5 rounded-lg border text-sm"
              >
                Cancel
              </button>

              <button
                onClick={handleReject}
                disabled={
                  actionLoading === rejectingPayment.id ||
                  !rejectionReason.trim()
                }
                className="px-5 py-2.5 rounded-lg bg-red-600 hover:bg-red-700 text-white text-sm font-medium disabled:opacity-50"
              >
                {actionLoading === rejectingPayment.id
                  ? "Rejecting..."
                  : "Confirm Rejection"}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
