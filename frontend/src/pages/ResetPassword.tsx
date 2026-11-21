
import { CheckCircle, Eye, EyeOff, LockKeyhole, Wallet } from "lucide-react";
import { useState } from "react";
import { Link, useNavigate, useSearchParams } from "react-router-dom";
import api from "../services/api";

export default function ResetPassword() {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();

  const token = searchParams.get("token") || "";

  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");

  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    setError("");

    if (!token) {
      setError("Reset token is missing. Please request a new password reset link.");
      return;
    }

    if (password.length < 8) {
      setError("Password must contain at least 8 characters.");
      return;
    }

    if (password !== confirmPassword) {
      setError("Passwords do not match.");
      return;
    }

    try {
      setLoading(true);

      await api.post("/api/auth/reset-password", {
        token,
        newPassword: password,
      });

      setSuccess(true);

      setTimeout(() => {
        navigate("/login");
      }, 2500);

    } catch (err: any) {
      const data = err.response?.data;

      setError(
        typeof data === "string"
          ? data
          : data?.message || "Password reset failed. Your link may be invalid or expired."
      );

    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-slate-50 p-6">

      <div className="w-full max-w-md bg-white rounded-2xl border border-slate-200 p-8 shadow-sm">

        <Link
          to="/login"
          className="text-sm text-slate-500 hover:text-blue-600"
        >
          ← Back to login
        </Link>

        <div className="bg-blue-100 text-blue-600 w-12 h-12 rounded-xl flex items-center justify-center mt-8 mb-6">
          <Wallet size={25} />
        </div>

        {!success ? (
          <>
            <h1 className="text-2xl font-bold text-slate-900">
              Reset your password
            </h1>

            <p className="text-slate-500 mt-2 mb-7">
              Choose a new password for your RecoverX account.
            </p>

            <form onSubmit={handleSubmit} className="space-y-5">

              <div>
                <label className="block text-sm font-medium mb-2">
                  New Password
                </label>

                <div className="relative">
                  <LockKeyhole
                    size={18}
                    className="absolute left-4 top-1/2 -translate-y-1/2 text-slate-400"
                  />

                  <input
                    type={showPassword ? "text" : "password"}
                    required
                    minLength={8}
                    maxLength={100}
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    placeholder="Enter new password"
                    className="w-full border border-slate-300 rounded-lg pl-11 pr-12 py-3 outline-none focus:ring-2 focus:ring-blue-500"
                  />

                  <button
                    type="button"
                    onClick={() => setShowPassword(!showPassword)}
                    className="absolute right-4 top-1/2 -translate-y-1/2 text-slate-500"
                  >
                    {showPassword ? <EyeOff size={19} /> : <Eye size={19} />}
                  </button>
                </div>
              </div>

              <div>
                <label className="block text-sm font-medium mb-2">
                  Confirm New Password
                </label>

                <div className="relative">
                  <input
                    type={showConfirmPassword ? "text" : "password"}
                    required
                    value={confirmPassword}
                    onChange={(e) => setConfirmPassword(e.target.value)}
                    placeholder="Confirm new password"
                    className="w-full border border-slate-300 rounded-lg px-4 pr-12 py-3 outline-none focus:ring-2 focus:ring-blue-500"
                  />

                  <button
                    type="button"
                    onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                    className="absolute right-4 top-1/2 -translate-y-1/2 text-slate-500"
                  >
                    {showConfirmPassword ? <EyeOff size={19} /> : <Eye size={19} />}
                  </button>
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
                {loading ? "Updating password..." : "Reset Password"}
              </button>

            </form>
          </>
        ) : (
          <div className="text-center py-4">
            <CheckCircle size={48} className="text-green-500 mx-auto mb-4" />

            <h1 className="text-2xl font-bold text-slate-900">
              Password reset successful!
            </h1>

            <p className="text-slate-500 mt-3">
              Your password has been updated. You can now sign in with your new password.
            </p>

            <p className="text-sm text-slate-400 mt-4">
              Redirecting to login...
            </p>
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
