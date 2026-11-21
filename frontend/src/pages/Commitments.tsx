
import {
  CheckCircle,
  ChevronDown,
  ChevronUp,
  Clock,
  FileText,
  Plus,
  RefreshCw,
  Search,
  XCircle,
} from "lucide-react";

import { useEffect, useMemo, useState } from "react";
import { useOutletContext } from "react-router-dom";

import type { Business } from "../types/business";
import type {
  Commitment,
  CustomerCommitment,
} from "../types/commitment";
import type { Installment } from "../types/installment";

import {
  approveCommitment,
  getBusinessCommitments,
  getMyCommitments,
  rejectCommitment,
} from "../services/commitmentService";

import {
  createInstallment,
  getInstallments,
} from "../services/installmentService";

type PageContext = {
  mode: "business" | "customer";
  selectedBusiness: Business | null;
};

export default function Commitments() {
  const { mode } = useOutletContext<PageContext>();

  return mode === "customer"
    ? <CustomerCommitments />
    : <BusinessCommitments />;
}

// ======================================================
// BUSINESS MODE
// ======================================================

function BusinessCommitments() {
  const { selectedBusiness } = useOutletContext<PageContext>();

  const [commitments, setCommitments] = useState<Commitment[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  const [search, setSearch] = useState("");
  const [statusFilter, setStatusFilter] = useState("ALL");

  const [processingId, setProcessingId] = useState<number | null>(null);

  const [reviewingCommitment, setReviewingCommitment] =
    useState<Commitment | null>(null);

  const [reviewAction, setReviewAction] =
    useState<"APPROVED" | "REJECTED" | null>(null);

  const [reviewNote, setReviewNote] = useState("");

  const [installments, setInstallments] = useState<
    Record<number, Installment[]>
  >({});

  const [expandedCommitments, setExpandedCommitments] = useState<Set<number>>(
    new Set()
  );

  const [installmentLoading, setInstallmentLoading] = useState<Set<number>>(
    new Set()
  );

  const [installmentTarget, setInstallmentTarget] =
    useState<Commitment | null>(null);

  const [installmentAmount, setInstallmentAmount] = useState("");
  const [installmentDate, setInstallmentDate] = useState("");
  const [creatingInstallment, setCreatingInstallment] = useState(false);

  const businessId = selectedBusiness?.id;

  const loadCommitments = async () => {
    if (!businessId) {
      setCommitments([]);
      return;
    }

    try {
      setLoading(true);
      setError("");

      const data = await getBusinessCommitments(businessId);
      setCommitments(data);
    } catch (err: any) {
      setError(
        err.response?.data?.message ||
          "Unable to load commitments. Please try again."
      );
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    setInstallments({});
    setExpandedCommitments(new Set());
    loadCommitments();
  }, [businessId]);

  const pendingCount = commitments.filter(
    (c) => c.status === "PENDING_REVIEW"
  ).length;

  const approvedCount = commitments.filter(
    (c) => c.status === "APPROVED"
  ).length;

  const rejectedCount = commitments.filter(
    (c) => c.status === "REJECTED"
  ).length;

  const filteredCommitments = useMemo(() => {
    return commitments.filter((commitment) => {
      const query = search.toLowerCase();

      const matchesSearch =
        commitment.summary?.toLowerCase().includes(query) ||
        String(commitment.invoiceId).includes(query) ||
        String(commitment.id).includes(query);

      const matchesStatus =
        statusFilter === "ALL" || commitment.status === statusFilter;

      return matchesSearch && matchesStatus;
    });
  }, [commitments, search, statusFilter]);

  // Load installments for a commitment.
  const loadInstallments = async (commitmentId: number) => {
    if (!businessId) return;

    try {
      setInstallmentLoading((previous) => {
        const next = new Set(previous);
        next.add(commitmentId);
        return next;
      });

      const data = await getInstallments(businessId, commitmentId);

      setInstallments((previous) => ({
        ...previous,
        [commitmentId]: data,
      }));
    } catch (err: any) {
      setError(
        err.response?.data?.message ||
          "Unable to load installments. Please try again."
      );
    } finally {
      setInstallmentLoading((previous) => {
        const next = new Set(previous);
        next.delete(commitmentId);
        return next;
      });
    }
  };

  const toggleInstallments = async (commitmentId: number) => {
    const isExpanded = expandedCommitments.has(commitmentId);

    if (isExpanded) {
      setExpandedCommitments((previous) => {
        const next = new Set(previous);
        next.delete(commitmentId);
        return next;
      });
      return;
    }

    setExpandedCommitments((previous) => {
      const next = new Set(previous);
      next.add(commitmentId);
      return next;
    });

    await loadInstallments(commitmentId);
  };

  // Open review modal.
  const openReview = (
    commitment: Commitment,
    action: "APPROVED" | "REJECTED"
  ) => {
    setReviewingCommitment(commitment);
    setReviewAction(action);
    setReviewNote("");
    setError("");
    setSuccess("");
  };

  const closeReview = () => {
    setReviewingCommitment(null);
    setReviewAction(null);
    setReviewNote("");
  };

  // Approve or reject commitment.
  const submitReview = async () => {
    if (!businessId || !reviewingCommitment || !reviewAction) return;

    if (reviewAction === "REJECTED" && !reviewNote.trim()) {
      setError("Please provide a reason for rejecting this commitment.");
      return;
    }

    try {
      setProcessingId(reviewingCommitment.id);
      setError("");
      setSuccess("");

      if (reviewAction === "APPROVED") {
        await approveCommitment(
          businessId,
          reviewingCommitment.id,
          reviewNote.trim()
        );

        setSuccess("Commitment approved successfully!");
      } else {
        await rejectCommitment(
          businessId,
          reviewingCommitment.id,
          reviewNote.trim()
        );

        setSuccess("Commitment rejected successfully!");
      }

      closeReview();
      await loadCommitments();
    } catch (err: any) {
      setError(
        err.response?.data?.message ||
          "Unable to process the commitment."
      );
    } finally {
      setProcessingId(null);
    }
  };

  // Open installment creation form.
  const openInstallmentForm = (commitment: Commitment) => {
    setInstallmentTarget(commitment);
    setInstallmentAmount("");
    setInstallmentDate("");
    setError("");
  };

  const closeInstallmentForm = () => {
    setInstallmentTarget(null);
    setInstallmentAmount("");
    setInstallmentDate("");
  };

  // Create installment.
  const submitInstallment = async () => {
    if (!businessId || !installmentTarget) return;

    const amount = Number(installmentAmount);

    if (!Number.isFinite(amount) || amount <= 0) {
      setError("Please enter a valid installment amount.");
      return;
    }

    if (!installmentDate) {
      setError("Please select a proposed payment date.");
      return;
    }

    try {
      setCreatingInstallment(true);
      setError("");
      setSuccess("");

      await createInstallment(businessId, {
        commitmentId: installmentTarget.id,
        amount,
        proposedDate: installmentDate,
      });

      const commitmentId = installmentTarget.id;

      closeInstallmentForm();
      setSuccess("Installment created successfully!");

      await loadInstallments(commitmentId);

      setExpandedCommitments((previous) => {
        const next = new Set(previous);
        next.add(commitmentId);
        return next;
      });
    } catch (err: any) {
      setError(
        err.response?.data?.message ||
          "Unable to create installment. Please try again."
      );
    } finally {
      setCreatingInstallment(false);
    }
  };

  const getStatusStyle = (status: string) => {
    switch (status) {
      case "APPROVED":
      case "VERIFIED":
      case "PAID":
        return "bg-green-100 text-green-700";

      case "REJECTED":
        return "bg-red-100 text-red-700";

      default:
        return "bg-yellow-100 text-yellow-700";
    }
  };

  if (!businessId) {
    return (
      <div className="bg-white border rounded-xl p-8 text-center">
        <h2 className="text-lg font-semibold">No business selected</h2>
        <p className="text-slate-500 mt-2">
          Select a business to view its commitments.
        </p>
      </div>
    );
  }

  return (
    <div>
      {/* Header */}
      <div className="flex flex-wrap justify-between items-center gap-4 mb-6">
        <div>
          <h1 className="text-2xl font-bold text-slate-900">
            Commitments
          </h1>

          <p className="text-slate-500 mt-1">
            Review customer payment promises for {selectedBusiness?.name}.
          </p>
        </div>

        <button
          onClick={loadCommitments}
          disabled={loading}
          className="flex items-center gap-2 px-4 py-2 bg-white border rounded-lg hover:bg-slate-50 disabled:opacity-50"
        >
          <RefreshCw size={16} className={loading ? "animate-spin" : ""} />
          Refresh
        </button>
      </div>

      {/* Messages */}
      {success && (
        <div className="mb-5 p-3 rounded-lg bg-green-50 text-green-700 border border-green-200">
          {success}
        </div>
      )}

      {error && (
        <div className="mb-5 p-3 rounded-lg bg-red-50 text-red-700 border border-red-200">
          {error}
        </div>
      )}

      {/* Statistics */}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-5 mb-6">
        <StatCard
          title="Pending Review"
          value={pendingCount}
          icon={<Clock size={22} />}
        />

        <StatCard
          title="Approved"
          value={approvedCount}
          icon={<CheckCircle size={22} />}
        />

        <StatCard
          title="Rejected"
          value={rejectedCount}
          icon={<XCircle size={22} />}
        />
      </div>

      {/* Search and filter */}
      <div className="bg-white border border-slate-200 rounded-xl p-4 mb-5 flex flex-col sm:flex-row gap-3">
        <div className="relative flex-1">
          <Search
            size={18}
            className="absolute left-3 top-3 text-slate-400"
          />

          <input
            type="text"
            placeholder="Search commitment or invoice ID..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            className="w-full border rounded-lg py-2.5 pl-10 pr-3 outline-none focus:ring-2 focus:ring-blue-500"
          />
        </div>

        <select
          value={statusFilter}
          onChange={(e) => setStatusFilter(e.target.value)}
          className="border rounded-lg px-4 py-2.5 bg-white"
        >
          <option value="ALL">All statuses</option>
          <option value="PENDING_REVIEW">Pending review</option>
          <option value="APPROVED">Approved</option>
          <option value="REJECTED">Rejected</option>
        </select>
      </div>

      {/* Commitments list */}
      <div className="bg-white border border-slate-200 rounded-xl overflow-hidden">
        <div className="p-5 border-b flex justify-between items-center">
          <h2 className="font-semibold text-lg">All Commitments</h2>

          <span className="text-sm text-slate-500">
            {filteredCommitments.length} commitment(s)
          </span>
        </div>

        {loading ? (
          <div className="p-10 text-center text-slate-500">
            Loading commitments...
          </div>
        ) : filteredCommitments.length === 0 ? (
          <div className="p-10 text-center">
            <FileText size={36} className="mx-auto text-slate-300" />

            <p className="font-medium mt-3">No commitments found</p>

            <p className="text-sm text-slate-500 mt-1">
              Customer payment promises will appear here.
            </p>
          </div>
        ) : (
          <div className="divide-y">
            {filteredCommitments.map((commitment) => {
              const isExpanded = expandedCommitments.has(commitment.id);

              return (
                <div
                  key={commitment.id}
                  className="p-5 hover:bg-slate-50"
                >
                  <div className="flex flex-col lg:flex-row lg:justify-between gap-4">
                    <div className="flex-1">
                      <div className="flex flex-wrap items-center gap-3 mb-2">
                        <h3 className="font-semibold text-slate-900">
                          Commitment #{commitment.id}
                        </h3>

                        <span
                          className={`text-xs font-medium px-3 py-1 rounded-full ${getStatusStyle(
                            commitment.status
                          )}`}
                        >
                          {commitment.status.replaceAll("_", " ")}
                        </span>
                      </div>

                      <p className="text-slate-600 text-sm">
                        {commitment.summary || "No summary provided"}
                      </p>

                      <div className="flex flex-wrap gap-x-6 gap-y-2 mt-4 text-sm text-slate-500">
                        <span>Invoice: #{commitment.invoiceId}</span>

                        <span>
                          Amount:{" "}
                          {commitment.proposedAmount != null
                            ? `₹${Number(
                                commitment.proposedAmount
                              ).toLocaleString("en-IN")}`
                            : "Not specified"}
                        </span>

                        <span>
                          Payment date:{" "}
                          {commitment.proposedPaymentDate || "Not specified"}
                        </span>
                      </div>

                      {commitment.aiClassification && (
                        <p className="text-xs text-slate-500 mt-3">
                          AI Classification: {commitment.aiClassification}
                        </p>
                      )}

                      {commitment.reviewNote && (
                        <p className="text-sm text-slate-500 mt-2">
                          Review note: {commitment.reviewNote}
                        </p>
                      )}
                    </div>

                    {/* Review actions */}
                    {commitment.status === "PENDING_REVIEW" && (
                      <div className="flex items-center gap-2 lg:self-center">
                        <button
                          onClick={() => openReview(commitment, "APPROVED")}
                          className="px-4 py-2 rounded-lg bg-green-600 text-white text-sm hover:bg-green-700"
                        >
                          Approve
                        </button>

                        <button
                          onClick={() => openReview(commitment, "REJECTED")}
                          className="px-4 py-2 rounded-lg bg-red-600 text-white text-sm hover:bg-red-700"
                        >
                          Reject
                        </button>
                      </div>
                    )}
                  </div>

                  {/* Installment controls for approved commitments */}
                  {commitment.status === "APPROVED" && (
                    <div className="mt-5 pt-4 border-t">
                      <div className="flex flex-wrap justify-between items-center gap-3">
                        <button
                          onClick={() => toggleInstallments(commitment.id)}
                          className="flex items-center gap-2 text-blue-600 hover:text-blue-800 font-medium text-sm"
                        >
                          {isExpanded ? (
                            <>
                              <ChevronUp size={18} />
                              Hide installments
                            </>
                          ) : (
                            <>
                              <ChevronDown size={18} />
                              View installments
                            </>
                          )}
                        </button>

                        <button
                          onClick={() => openInstallmentForm(commitment)}
                          className="flex items-center gap-2 px-3 py-2 rounded-lg bg-blue-600 text-white text-sm hover:bg-blue-700"
                        >
                          <Plus size={16} />
                          Add Installment
                        </button>
                      </div>

                      {isExpanded && (
                        <div className="mt-4 bg-slate-50 rounded-lg p-4">
                          <h4 className="font-semibold text-slate-800 mb-3">
                            Installments
                          </h4>

                          {installmentLoading.has(commitment.id) ? (
                            <p className="text-sm text-slate-500">
                              Loading installments...
                            </p>
                          ) : (installments[commitment.id] || []).length === 0 ? (
                            <p className="text-sm text-slate-500">
                              No installments found for this commitment.
                            </p>
                          ) : (
                            <div className="overflow-x-auto">
                              <table className="w-full text-sm">
                                <thead>
                                  <tr className="text-left text-slate-500 border-b">
                                    <th className="py-3 pr-4">Installment</th>
                                    <th className="py-3 pr-4">Amount</th>
                                    <th className="py-3 pr-4">Proposed Date</th>
                                    <th className="py-3">Status</th>
                                  </tr>
                                </thead>

                                <tbody>
                                  {installments[commitment.id].map(
                                    (installment) => (
                                      <tr
                                        key={installment.id}
                                        className="border-b last:border-0"
                                      >
                                        <td className="py-3 pr-4">
                                          #{installment.id}
                                        </td>

                                        <td className="py-3 pr-4 font-medium">
                                          ₹
                                          {Number(
                                            installment.amount
                                          ).toLocaleString("en-IN", {
                                            minimumFractionDigits: 2,
                                          })}
                                        </td>

                                        <td className="py-3 pr-4">
                                          {installment.proposedDate}
                                        </td>

                                        <td className="py-3">
                                          <span
                                            className={`text-xs font-medium px-3 py-1 rounded-full ${getStatusStyle(
                                              installment.status
                                            )}`}
                                          >
                                            {installment.status.replaceAll(
                                              "_",
                                              " "
                                            )}
                                          </span>
                                        </td>
                                      </tr>
                                    )
                                  )}
                                </tbody>
                              </table>
                            </div>
                          )}

                          <button
                            onClick={() => loadInstallments(commitment.id)}
                            disabled={installmentLoading.has(commitment.id)}
                            className="mt-3 text-sm text-blue-600 hover:underline disabled:opacity-50"
                          >
                            Refresh installments
                          </button>
                        </div>
                      )}
                    </div>
                  )}
                </div>
              );
            })}
          </div>
        )}
      </div>

      {/* Review modal */}
      {reviewingCommitment && reviewAction && (
        <div className="fixed inset-0 z-50 bg-black/40 flex items-center justify-center p-4">
          <div className="bg-white rounded-xl shadow-xl w-full max-w-md p-6">
            <h2 className="text-xl font-semibold">
              {reviewAction === "APPROVED"
                ? "Approve Commitment"
                : "Reject Commitment"}
            </h2>

            <p className="text-sm text-slate-500 mt-2">
              Commitment #{reviewingCommitment.id}
            </p>

            <label className="block text-sm font-medium mt-5 mb-2">
              Review note
              {reviewAction === "REJECTED" && (
                <span className="text-red-500"> *</span>
              )}
            </label>

            <textarea
              value={reviewNote}
              onChange={(e) => setReviewNote(e.target.value)}
              placeholder={
                reviewAction === "APPROVED"
                  ? "Optional review note..."
                  : "Enter reason for rejection..."
              }
              maxLength={500}
              rows={4}
              className="w-full border rounded-lg p-3 outline-none focus:ring-2 focus:ring-blue-500"
            />

            <div className="flex justify-end gap-3 mt-5">
              <button
                onClick={closeReview}
                disabled={processingId !== null}
                className="px-4 py-2 border rounded-lg hover:bg-slate-50"
              >
                Cancel
              </button>

              <button
                onClick={submitReview}
                disabled={processingId !== null}
                className={`px-4 py-2 rounded-lg text-white disabled:opacity-50 ${
                  reviewAction === "APPROVED"
                    ? "bg-green-600 hover:bg-green-700"
                    : "bg-red-600 hover:bg-red-700"
                }`}
              >
                {processingId !== null
                  ? "Processing..."
                  : reviewAction === "APPROVED"
                  ? "Confirm Approval"
                  : "Confirm Rejection"}
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Add installment modal */}
      {installmentTarget && (
        <div className="fixed inset-0 z-50 bg-black/40 flex items-center justify-center p-4">
          <div className="bg-white rounded-xl shadow-xl w-full max-w-md p-6">
            <h2 className="text-xl font-semibold">
              Add Installment
            </h2>

            <p className="text-sm text-slate-500 mt-2">
              Commitment #{installmentTarget.id}
            </p>

            <div className="mt-5">
              <label className="block text-sm font-medium mb-2">
                Installment Amount (₹)
              </label>

              <input
                type="number"
                min="0.01"
                step="0.01"
                value={installmentAmount}
                onChange={(e) => setInstallmentAmount(e.target.value)}
                placeholder="Enter amount"
                className="w-full border rounded-lg p-3 outline-none focus:ring-2 focus:ring-blue-500"
              />
            </div>

            <div className="mt-4">
              <label className="block text-sm font-medium mb-2">
                Proposed Payment Date
              </label>

              <input
                type="date"
                value={installmentDate}
                onChange={(e) => setInstallmentDate(e.target.value)}
                className="w-full border rounded-lg p-3 outline-none focus:ring-2 focus:ring-blue-500"
              />
            </div>

            <div className="flex justify-end gap-3 mt-6">
              <button
                onClick={closeInstallmentForm}
                disabled={creatingInstallment}
                className="px-4 py-2 border rounded-lg hover:bg-slate-50"
              >
                Cancel
              </button>

              <button
                onClick={submitInstallment}
                disabled={creatingInstallment}
                className="px-4 py-2 rounded-lg bg-blue-600 text-white hover:bg-blue-700 disabled:opacity-50"
              >
                {creatingInstallment ? "Creating..." : "Create Installment"}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

// ======================================================
// CUSTOMER MODE
// ======================================================

function CustomerCommitments() {
  const [commitments, setCommitments] = useState<CustomerCommitment[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [search, setSearch] = useState("");
  const [statusFilter, setStatusFilter] = useState("ALL");

  const loadCommitments = async () => {
    try {
      setLoading(true);
      setError("");

      const data = await getMyCommitments();
      setCommitments(data);
    } catch (err: any) {
      setError(
        err.response?.data?.message ||
          "Unable to load your commitments. Please try again."
      );
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadCommitments();
  }, []);

  const pendingCount = commitments.filter(
    (c) => c.status === "PENDING_REVIEW"
  ).length;

  const approvedCount = commitments.filter(
    (c) => c.status === "APPROVED"
  ).length;

  const rejectedCount = commitments.filter(
    (c) => c.status === "REJECTED"
  ).length;

  const filteredCommitments = useMemo(() => {
    const query = search.trim().toLowerCase();

    return commitments.filter((commitment) => {
      const matchesSearch =
        (commitment.summary || "").toLowerCase().includes(query) ||
        String(commitment.invoiceId).includes(query) ||
        String(commitment.id).includes(query) ||
        String(commitment.businessId).includes(query);

      const matchesStatus =
        statusFilter === "ALL" || commitment.status === statusFilter;

      return matchesSearch && matchesStatus;
    });
  }, [commitments, search, statusFilter]);

  const getStatusStyle = (status: string) => {
    switch (status) {
      case "APPROVED":
        return "bg-green-100 text-green-700";
      case "REJECTED":
        return "bg-red-100 text-red-700";
      default:
        return "bg-yellow-100 text-yellow-700";
    }
  };

  const formatCurrency = (amount: number | null) => {
    if (amount === null || amount === undefined) return "Not specified";

    return new Intl.NumberFormat("en-IN", {
      style: "currency",
      currency: "INR",
      maximumFractionDigits: 2,
    }).format(Number(amount));
  };

  const formatDate = (value: string | null) => {
    if (!value) return "Not specified";

    // Avoid timezone shifts for LocalDate values.
    const dateOnly = /^\d{4}-\d{2}-\d{2}$/.test(value);

    if (dateOnly) {
      const [year, month, day] = value.split("-").map(Number);

      return new Date(year, month - 1, day).toLocaleDateString("en-IN", {
        day: "2-digit",
        month: "short",
        year: "numeric",
      });
    }

    const date = new Date(value);

    if (Number.isNaN(date.getTime())) return value;

    return date.toLocaleString("en-IN", {
      day: "2-digit",
      month: "short",
      year: "numeric",
      hour: "2-digit",
      minute: "2-digit",
    });
  };

  return (
    <div>
      {/* Header */}
      <div className="flex flex-wrap justify-between items-center gap-4 mb-6">
        <div>
          <h1 className="text-2xl font-bold text-slate-900">
            My Payment Commitments
          </h1>

          <p className="text-slate-500 mt-1">
            Track payment promises you've made to connected businesses.
          </p>
        </div>

        <button
          onClick={loadCommitments}
          disabled={loading}
          className="flex items-center gap-2 px-4 py-2 bg-white border rounded-lg hover:bg-slate-50 disabled:opacity-50"
        >
          <RefreshCw size={16} className={loading ? "animate-spin" : ""} />
          Refresh
        </button>
      </div>

      {/* Error */}
      {error && (
        <div className="mb-5 p-3 rounded-lg bg-red-50 text-red-700 border border-red-200">
          {error}
        </div>
      )}

      {/* Statistics */}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-5 mb-6">
        <StatCard
          title="Pending Review"
          value={pendingCount}
          icon={<Clock size={22} />}
        />

        <StatCard
          title="Approved"
          value={approvedCount}
          icon={<CheckCircle size={22} />}
        />

        <StatCard
          title="Rejected"
          value={rejectedCount}
          icon={<XCircle size={22} />}
        />
      </div>

      {/* Search and filter */}
      <div className="bg-white border border-slate-200 rounded-xl p-4 mb-5 flex flex-col sm:flex-row gap-3">
        <div className="relative flex-1">
          <Search
            size={18}
            className="absolute left-3 top-3 text-slate-400"
          />

          <input
            type="text"
            placeholder="Search commitment, invoice or business ID..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            className="w-full border rounded-lg py-2.5 pl-10 pr-3 outline-none focus:ring-2 focus:ring-blue-500"
          />
        </div>

        <select
          value={statusFilter}
          onChange={(e) => setStatusFilter(e.target.value)}
          className="border rounded-lg px-4 py-2.5 bg-white"
        >
          <option value="ALL">All statuses</option>
          <option value="PENDING_REVIEW">Pending review</option>
          <option value="APPROVED">Approved</option>
          <option value="REJECTED">Rejected</option>
        </select>
      </div>

      {/* Customer commitments */}
      <div className="bg-white border border-slate-200 rounded-xl overflow-hidden">
        <div className="p-5 border-b flex justify-between items-center">
          <h2 className="font-semibold text-lg">My Commitments</h2>

          <span className="text-sm text-slate-500">
            {filteredCommitments.length} commitment(s)
          </span>
        </div>

        {loading ? (
          <div className="p-10 text-center text-slate-500">
            Loading commitments...
          </div>
        ) : filteredCommitments.length === 0 ? (
          <div className="p-10 text-center">
            <FileText size={36} className="mx-auto text-slate-300" />

            <p className="font-medium mt-3">No commitments found</p>

            <p className="text-sm text-slate-500 mt-1">
              Your payment commitments will appear here.
            </p>
          </div>
        ) : (
          <div className="divide-y">
            {filteredCommitments.map((commitment) => (
              <div
                key={commitment.id}
                className="p-5 hover:bg-slate-50"
              >
                <div className="flex flex-col lg:flex-row lg:justify-between gap-3">
                  <div className="flex-1">
                    <div className="flex flex-wrap items-center gap-3 mb-2">
                      <h3 className="font-semibold text-slate-900">
                        Commitment #{commitment.id}
                      </h3>

                      <span
                        className={`text-xs font-medium px-3 py-1 rounded-full ${getStatusStyle(
                          commitment.status
                        )}`}
                      >
                        {commitment.status.replaceAll("_", " ")}
                      </span>
                    </div>

                    <p className="text-slate-600 text-sm">
                      {commitment.summary || "No summary provided"}
                    </p>

                    <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 mt-5">
                      <div>
                        <p className="text-xs text-slate-500">Business ID</p>
                        <p className="font-medium text-sm mt-1">
                          #{commitment.businessId}
                        </p>
                      </div>

                      <div>
                        <p className="text-xs text-slate-500">Invoice ID</p>
                        <p className="font-medium text-sm mt-1">
                          #{commitment.invoiceId}
                        </p>
                      </div>

                      <div>
                        <p className="text-xs text-slate-500">
                          Proposed Amount
                        </p>
                        <p className="font-semibold text-sm mt-1">
                          {formatCurrency(commitment.proposedAmount)}
                        </p>
                      </div>

                      <div>
                        <p className="text-xs text-slate-500">
                          Proposed Payment Date
                        </p>
                        <p className="font-medium text-sm mt-1">
                          {formatDate(commitment.proposedPaymentDate)}
                        </p>
                      </div>
                    </div>
                  </div>
                </div>

                <div className="flex flex-wrap gap-x-6 gap-y-2 mt-5 pt-4 border-t border-slate-100 text-xs text-slate-500">
                  <span>Submitted: {formatDate(commitment.createdAt)}</span>
                  <span>Reviewed: {formatDate(commitment.reviewedAt)}</span>
                </div>

                {commitment.status === "PENDING_REVIEW" && (
                  <p className="text-xs text-yellow-700 mt-3">
                    Your payment commitment is waiting for business approval.
                  </p>
                )}

                {commitment.status === "APPROVED" && (
                  <p className="text-xs text-green-700 mt-3">
                    Your payment commitment has been approved.
                  </p>
                )}

                {commitment.status === "REJECTED" && (
                  <p className="text-xs text-red-700 mt-3">
                    Your payment commitment was rejected.
                  </p>
                )}
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}

// ======================================================
// SHARED STAT CARD
// ======================================================

function StatCard({
  title,
  value,
  icon,
}: {
  title: string;
  value: number;
  icon: React.ReactNode;
}) {
  return (
    <div className="bg-white border border-slate-200 rounded-xl p-5">
      <div className="flex justify-between items-center">
        <div>
          <p className="text-sm text-slate-500">{title}</p>
          <h2 className="text-2xl font-bold mt-2">{value}</h2>
        </div>

        <div className="bg-blue-50 text-blue-600 p-3 rounded-lg">
          {icon}
        </div>
      </div>
    </div>
  );
}
