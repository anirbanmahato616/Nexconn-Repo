
import { useCallback, useEffect, useState } from "react";
import { Outlet, useNavigate } from "react-router-dom";
import Navbar from "../components/Navbar";
import Sidebar from "../components/Sidebar";
import { getMyBusinesses } from "../services/businessService";
import type { Business } from "../types/business";

export default function DashboardLayout() {
  const navigate = useNavigate();

  const [mode, setMode] = useState<"business" | "customer">("business");

  const [businesses, setBusinesses] = useState<Business[]>([]);
  const [selectedBusiness, setSelectedBusiness] =
    useState<Business | null>(null);

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const refreshBusinesses = useCallback(
    async (selectBusinessId?: number) => {
      try {
        setLoading(true);
        setError("");

        const data = await getMyBusinesses();

        const activeBusinesses = data.filter(
          (business) => business.active
        );

        setBusinesses(activeBusinesses);

        const businessToSelect = selectBusinessId
          ? activeBusinesses.find(
              (business) => business.id === selectBusinessId
            )
          : null;

        setSelectedBusiness(
          businessToSelect || activeBusinesses[0] || null
        );
      } catch (err: any) {
        setError(
          err.response?.data?.message ||
          "Unable to load your businesses."
        );
      } finally {
        setLoading(false);
      }
    },
    []
  );

  useEffect(() => {
    refreshBusinesses();
  }, [refreshBusinesses]);

  return (
    <div className="min-h-screen flex bg-slate-50">
      <Sidebar
        mode={mode}
        businessName={selectedBusiness?.name}
      />

      <div className="flex-1 min-w-0">
        <Navbar mode={mode} setMode={setMode} />

        <main className="p-6">
          {mode === "business" && (
            <div className="mb-6 flex flex-wrap items-center justify-between gap-3">
              <div>
                <h2 className="font-semibold text-slate-800">
                  Business Workspace
                </h2>
                <p className="text-sm text-slate-500">
                  Select the business you want to manage.
                </p>
              </div>

              <div className="flex items-center gap-3">
                {loading ? (
                  <p className="text-sm text-slate-500">
                    Loading businesses...
                  </p>
                ) : businesses.length > 0 ? (
                  <select
                    value={selectedBusiness?.id ?? ""}
                    onChange={(e) => {
                      const business = businesses.find(
                        (item) => item.id === Number(e.target.value)
                      );

                      setSelectedBusiness(business || null);
                    }}
                    className="bg-white border border-slate-300 rounded-lg px-4 py-2 text-sm"
                  >
                    {businesses.map((business) => (
                      <option key={business.id} value={business.id}>
                        {business.name}
                      </option>
                    ))}
                  </select>
                ) : null}

                <button
                  type="button"
                  onClick={() => navigate("/create-business")}
                  className="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-lg text-sm font-medium transition-colors"
                >
                  + Create Business
                </button>
              </div>
            </div>
          )}

          {error && mode === "business" && (
            <div className="mb-5 bg-red-50 text-red-600 p-3 rounded-lg text-sm">
              {error}
            </div>
          )}

          {mode === "business" &&
            !loading &&
            businesses.length === 0 &&
            !error && (
              <div className="mb-5 bg-white border rounded-xl p-5">
                <h3 className="font-semibold text-slate-800">
                  No business found
                </h3>
                <p className="text-sm text-slate-500 mt-1">
                  Create a business to start managing invoices and customers.
                </p>
              </div>
            )}

          <Outlet
            context={{
              mode,
              selectedBusiness,
              businesses,
              loading,
              refreshBusinesses,
            }}
          />
        </main>
      </div>
    </div>
  );
}
