
import {
    CheckCircle,
    Loader2,
    Mail,
    XCircle,
} from "lucide-react";
import { useEffect, useState } from "react";
import {
    Link,
    useLocation,
    useNavigate,
    useSearchParams,
} from "react-router-dom";

import { acceptCustomerInvitation } from "../services/customerService";

export default function AcceptInvitation() {
  const navigate = useNavigate();
  const location = useLocation();
  const [searchParams] = useSearchParams();

  const businessId = Number(searchParams.get("businessId"));
  const token = searchParams.get("token");

  const [loading, setLoading] = useState(false);
  const [success, setSuccess] = useState(false);
  const [error, setError] = useState("");

  const accessToken = sessionStorage.getItem("accessToken");

  useEffect(() => {
    if (!businessId || businessId <= 0 || !token) {
      setError("This invitation link is invalid or incomplete.");
      return;
    }

    if (!accessToken) {
      navigate("/login", {
        replace: true,
        state: {
          from: location.pathname + location.search,
        },
      });
    }
  }, [
    businessId,
    token,
    accessToken,
    navigate,
    location.pathname,
    location.search,
  ]);

  const handleAcceptInvitation = async () => {
    if (!token || !businessId) {
      setError("Invalid invitation link.");
      return;
    }

    setError("");
    setLoading(true);

    try {
      await acceptCustomerInvitation(businessId, token);

      setSuccess(true);

    } catch (err: any) {
      const responseData = err.response?.data;

      setError(
        typeof responseData === "string"
          ? responseData
          : responseData?.message ||
            "Unable to accept the invitation. Please try again."
      );

    } finally {
      setLoading(false);
    }
  };

  if (!accessToken && !error) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-slate-50">
        <Loader2 className="animate-spin text-blue-600" size={35} />
      </div>
    );
  }

  if (success) {
    return (
      <div className="min-h-screen bg-slate-50 flex items-center justify-center p-6">
        <div className="bg-white rounded-2xl shadow-sm border border-slate-200
          w-full max-w-md p-8 text-center">

          <CheckCircle className="text-green-600 mx-auto" size={60} />

          <h1 className="text-2xl font-bold text-slate-900 mt-5">
            Invitation Accepted!
          </h1>

          <p className="text-slate-500 mt-3 leading-relaxed">
            Your RecoverX account has been successfully connected
            to the business. You can now access invoices and
            payment information shared with you.
          </p>

          <button
            onClick={() => navigate("/dashboard", { replace: true })}
            className="w-full mt-7 bg-blue-600 hover:bg-blue-700
              text-white font-semibold py-3 rounded-lg transition"
          >
            Go to Dashboard
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-slate-50 flex items-center justify-center p-6">
      <div className="bg-white rounded-2xl shadow-sm border border-slate-200
        w-full max-w-md p-8 text-center">

        <div className="bg-blue-50 w-16 h-16 rounded-full
          flex items-center justify-center mx-auto">
          <Mail className="text-blue-600" size={30} />
        </div>

        <h1 className="text-2xl font-bold text-slate-900 mt-5">
          Business Invitation
        </h1>

        <p className="text-slate-500 mt-3 leading-relaxed">
          You have been invited to connect with a business
          on RecoverX. Accept the invitation to link your
          customer account.
        </p>

        {error && (
          <div className="mt-6 bg-red-50 text-red-700
            rounded-lg p-4 text-sm flex items-start gap-2 text-left">
            <XCircle size={19} className="shrink-0 mt-0.5" />
            <span>{error}</span>
          </div>
        )}

        {!error && (
          <button
            onClick={handleAcceptInvitation}
            disabled={loading || !token || !businessId}
            className="w-full mt-7 bg-blue-600 hover:bg-blue-700
              disabled:bg-blue-400 text-white font-semibold py-3
              rounded-lg flex items-center justify-center gap-2 transition"
          >
            {loading ? (
              <>
                <Loader2 size={19} className="animate-spin" />
                Accepting Invitation...
              </>
            ) : (
              "Accept Invitation"
            )}
          </button>
        )}

        <Link
          to="/dashboard"
          className="inline-block mt-5 text-sm text-slate-500
            hover:text-blue-600"
        >
          Return to Dashboard
        </Link>
      </div>
    </div>
  );
}
