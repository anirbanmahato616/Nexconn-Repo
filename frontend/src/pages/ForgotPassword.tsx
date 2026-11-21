
import { ArrowLeft, CheckCircle, Mail, Wallet } from "lucide-react";
import { useState } from "react";
import { Link } from "react-router-dom";
import api from "../services/api";

export default function ForgotPassword() {
  const [email, setEmail] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    setError("");
    setSuccess(false);
    setLoading(true);

    try {
      await api.post("/api/auth/forgot-password", { email });
      setSuccess(true);
    } catch (err: any) {
      const data = err.response?.data;

      setError(
        typeof data === "string"
          ? data
          : data?.message || "Unable to process your request. Please try again."
      );
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-slate-50 p-6">
      <div className="w-full max-w-md bg-white rounded-2xl border border-slate-200 p-8 shadow-sm">

        <Link to="/login" className="inline-flex items-center gap-2 text-sm text-slate-500 hover:text-blue-600 mb-8">
          <ArrowLeft size={17} /> Back to login
        </Link>

        <div className="bg-blue-100 text-blue-600 w-12 h-12 rounded-xl flex items-center justify-center mb-6">
          <Wallet size={25} />
        </div>

        {!success ? (
          <>
            <h1 className="text-2xl font-bold text-slate-900">
              Forgot your password?
            </h1>

            <p className="text-slate-500 mt-2 mb-7">
              Enter your registered email address. We'll send you a link to reset your password.
            </p>

            <form onSubmit={handleSubmit} className="space-y-5">

              <div>
                <label className="block text-sm font-medium mb-2">
                  Email address
                </label>

                <div className="relative">
                  <Mail size={18} className="absolute left-4 top-1/2 -translate-y-1/2 text-slate-400" />

                  <input
                    type="email"
                    required
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    placeholder="you@example.com"
                    className="w-full border border-slate-300 rounded-lg pl-11 pr-4 py-3 outline-none focus:ring-2 focus:ring-blue-500"
                  />
                </div>
              </div>

              {error && (
                <div className="bg-red-50 text-red-600 text-sm p-3 rounded-lg">
                  {error}
                </div>
              )}

              <button
                type="submit"
                disabled={loading}
                className="w-full bg-blue-600 hover:bg-blue-700 disabled:bg-blue-400 text-white font-semibold py-3 rounded-lg transition"
              >
                {loading ? "Sending..." : "Send Reset Link"}
              </button>

            </form>
          </>
        ) : (
          <div className="text-center">
            <CheckCircle size={48} className="text-green-500 mx-auto mb-4" />

            <h1 className="text-2xl font-bold text-slate-900">
              Check your email
            </h1>

            <p className="text-slate-500 mt-3">
              If an account exists for <strong>{email}</strong>, a password reset link has been sent.
            </p>

            <p className="text-sm text-slate-400 mt-4">
              Check your inbox and spam folder.
            </p>

            <button
              onClick={() => setSuccess(false)}
              className="text-blue-600 font-semibold text-sm mt-6 hover:underline"
            >
              Try another email
            </button>
          </div>
        )}

        <p className="text-center text-sm text-slate-500 mt-8">
          Remember your password?{" "}
          <Link to="/login" className="text-blue-600 font-semibold hover:underline">
            Sign in
          </Link>
        </p>

      </div>
    </div>
  );
}
