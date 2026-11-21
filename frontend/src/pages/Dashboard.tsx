
import {
  CheckCircle,
  Clock,
  FileText,
  Wallet,
} from "lucide-react";
import { useEffect, useState } from "react";
import { useOutletContext } from "react-router-dom";

import type { Business } from "../types/business";
import type { Invoice } from "../types/invoice";
import type { Payment } from "../types/payment";

import { getBusinessInvoices } from "../services/invoiceService";
import { getBusinessPayments } from "../services/paymentService";

type DashboardContext = {
  mode: "business" | "customer";
  selectedBusiness: Business | null;
  businesses: Business[];
  loading: boolean;
};

export default function Dashboard() {
  const { mode, selectedBusiness, loading: businessLoading } =
    useOutletContext<DashboardContext>();

  const fullName = sessionStorage.getItem("fullName");

  const [invoices, setInvoices] = useState<Invoice[]>([]);
  const [payments, setPayments] = useState<Payment[]>([]);

  const [dataLoading, setDataLoading] = useState(false);
  const [error, setError] = useState("");

  // Fetch invoice and payment data for the selected business
  useEffect(() => {
    if (mode !== "business" || !selectedBusiness) {
      setInvoices([]);
      setPayments([]);
      return;
    }

    let cancelled = false;

    const fetchDashboardData = async () => {
      try {
        setDataLoading(true);
        setError("");

        const [invoiceData, paymentData] = await Promise.all([
          getBusinessInvoices(selectedBusiness.id),
          getBusinessPayments(selectedBusiness.id),
        ]);

        if (!cancelled) {
          setInvoices(invoiceData);
          setPayments(paymentData);
        }
      } catch (err: any) {
        if (!cancelled) {
          setError(
            err.response?.data?.message ||
            "Unable to load dashboard information."
          );
        }
      } finally {
        if (!cancelled) {
          setDataLoading(false);
        }
      }
    };

    fetchDashboardData();

    return () => {
      cancelled = true;
    };
  }, [mode, selectedBusiness?.id]);

  // Calculate business statistics
  const totalInvoices = invoices.length;

  const totalOutstanding = invoices.reduce(
    (total, invoice) =>
      total + Number(invoice.outstandingAmount || 0),
    0
  );

  const totalCollected = payments
    .filter(
      (payment) =>
        String(payment.verificationStatus).toUpperCase() === "VERIFIED"
    )
    .reduce(
      (total, payment) => total + Number(payment.amount || 0),
      0
    );

  const today = new Date().toISOString().slice(0, 10);

  const overdueInvoices = invoices.filter(
    (invoice) =>
      Number(invoice.outstandingAmount) > 0 &&
      invoice.dueDate < today
  ).length;

  const formatCurrency = (amount: number) =>
    new Intl.NumberFormat("en-IN", {
      style: "currency",
      currency: "INR",
      maximumFractionDigits: 2,
    }).format(amount);

  const businessStats = [
    {
      title: "Total Invoices",
      value: totalInvoices.toString(),
      icon: FileText,
    },
    {
      title: "Outstanding",
      value: formatCurrency(totalOutstanding),
      icon: Wallet,
    },
    {
      title: "Collected",
      value: formatCurrency(totalCollected),
      icon: CheckCircle,
    },
    {
      title: "Overdue",
      value: overdueInvoices.toString(),
      icon: Clock,
    },
  ];

  const customerStats = [
    { title: "Connected Businesses", value: "0", icon: Wallet },
    { title: "My Invoices", value: "0", icon: FileText },
    { title: "Amount Paid", value: "₹0", icon: CheckCircle },
    { title: "Outstanding", value: "₹0", icon: Clock },
  ];

  const stats = mode === "business" ? businessStats : customerStats;

  const isLoading = businessLoading || dataLoading;

  return (
    <div>

      {/* Dashboard Heading */}
      <div className="mb-8">
        <h1 className="text-2xl font-bold text-slate-900">
          Welcome back, {fullName}!
        </h1>

        <p className="text-slate-500 mt-1">
          {mode === "business"
            ? `Here's an overview of ${selectedBusiness?.name || "your business"}.`
            : "Here's an overview of your payments."}
        </p>
      </div>

      {/* Selected Business Information */}
      {mode === "business" && selectedBusiness && (
        <div className="bg-white border border-slate-200 rounded-xl p-5 mb-6">
          <div className="flex flex-wrap justify-between gap-4">

            <div>
              <h2 className="text-lg font-semibold text-slate-900">
                {selectedBusiness.name}
              </h2>

              <p className="text-sm text-slate-500 mt-1">
                {selectedBusiness.industry || "Business"}
              </p>
            </div>

            <div className="text-sm text-slate-500">
              <p>{selectedBusiness.email}</p>
              <p>{selectedBusiness.phone}</p>
            </div>

          </div>

          {selectedBusiness.address && (
            <p className="text-sm text-slate-500 mt-3">
              {selectedBusiness.address}
            </p>
          )}
        </div>
      )}

      {/* Loading */}
      {isLoading && (
        <p className="text-sm text-blue-600 mb-4">
          Loading dashboard information...
        </p>
      )}

      {/* API Error */}
      {error && (
        <div className="bg-red-50 border border-red-200 text-red-600 p-4 rounded-lg mb-5">
          {error}
        </div>
      )}

      {/* Statistics */}
      <div className="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-4 gap-5">

        {stats.map((item) => {
          const Icon = item.icon;

          return (
            <div
              key={item.title}
              className="bg-white border border-slate-200 rounded-xl p-5"
            >
              <div className="flex justify-between items-center">

                <div>
                  <p className="text-sm text-slate-500">
                    {item.title}
                  </p>

                  <h2 className="text-2xl font-bold mt-2">
                    {mode === "business" && isLoading
                      ? "..."
                      : item.value}
                  </h2>
                </div>

                <div className="bg-blue-50 text-blue-600 p-3 rounded-lg">
                  <Icon size={22} />
                </div>

              </div>
            </div>
          );
        })}

      </div>

      {/* Recent Activity */}
      <div className="mt-8 bg-white border border-slate-200 rounded-xl p-6">

        <h2 className="text-lg font-semibold">
          {mode === "business" ? "Recent Activity" : "Recent Payments"}
        </h2>

        {mode === "business" && !isLoading && invoices.length > 0 ? (
          <div className="mt-4 space-y-3">

            {invoices.slice(0, 5).map((invoice) => (
              <div
                key={invoice.id}
                className="flex flex-wrap justify-between gap-3 border-b border-slate-100 pb-3"
              >
                <div>
                  <p className="font-medium text-sm">
                    {invoice.invoiceNumber}
                  </p>
                  <p className="text-xs text-slate-500">
                    Due: {invoice.dueDate}
                  </p>
                </div>

                <div className="text-right">
                  <p className="font-medium text-sm">
                    {formatCurrency(Number(invoice.amount))}
                  </p>
                  <p className="text-xs text-slate-500">
                    {invoice.status}
                  </p>
                </div>
              </div>
            ))}

          </div>
        ) : (
          <p className="text-slate-500 text-sm mt-3">
            {error
              ? "Activity could not be loaded."
              : "No recent activity available."}
          </p>
        )}

      </div>

    </div>
  );
}
