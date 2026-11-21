
import {
  ArrowRight,
  Eye,
  EyeOff,
  UserPlus,
  Wallet,
} from "lucide-react";
import { useState } from "react";
import { Link, useLocation, useNavigate } from "react-router-dom";
import api from "../services/api";

export default function Register() {
  const navigate = useNavigate();
  const location = useLocation();

  const redirectTo = location.state?.from || "/dashboard";

  const [fullName, setFullName] = useState("");
  const [email, setEmail] = useState("");
  const [phone, setPhone] = useState("");
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");

  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    setError("");
    setSuccess("");

    if (password !== confirmPassword) {
      setError("Passwords do not match.");
      return;
    }

    if (password.length < 8 || password.length > 100) {
      setError("Password must contain 8 to 100 characters.");
      return;
    }

    if (phone && !/^[0-9]{10,15}$/.test(phone)) {
      setError("Phone number must contain 10 to 15 digits.");
      return;
    }

    try {
      setLoading(true);

      const response = await api.post(
        "/api/auth/register",
        {
          fullName,
          email,
          password,
          phone,
        }
      );

      setSuccess(
        response.data?.message || "Account created successfully!"
      );

      setTimeout(() => {
        navigate("/login", {
          replace: true,
          state: { from: redirectTo },
        });
      }, 1500);

    } catch (err: any) {
      const message = err.response?.data;

      setError(
        typeof message === "string"
          ? message
          : message?.message || "Registration failed. Please try again."
      );
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex bg-white">

      {/* Left Section */}
      <div className="hidden lg:flex lg:w-1/2 bg-slate-950 text-white
        flex-col justify-between p-14">

        <div className="flex items-center gap-3">
          <div className="bg-blue-600 p-2 rounded-xl">
            <Wallet size={25} />
          </div>
          <span className="text-2xl font-bold">RecoverX</span>
        </div>

        <div className="max-w-lg">
          <h1 className="text-5xl font-bold leading-tight">
            Take control of your payments.
          </h1>

          <p className="text-slate-400 mt-6 text-lg leading-relaxed">
            Join RecoverX to manage invoices, track payments
            and simplify your payment recovery process.
          </p>
        </div>

        <p className="text-slate-500 text-sm">
          © 2026 RecoverX. All rights reserved.
        </p>
      </div>

      {/* Right Section */}
      <div className="flex-1 flex items-center justify-center p-6">

        <div className="w-full max-w-md">

          <div className="lg:hidden flex items-center gap-2 mb-8">
            <div className="bg-blue-600 text-white p-2 rounded-lg">
              <Wallet size={23} />
            </div>
            <span className="text-2xl font-bold text-slate-900">
              RecoverX
            </span>
          </div>

          <div className="flex items-center gap-2 mb-5">
            <UserPlus className="text-blue-600" size={23} />
            <span className="text-sm font-semibold text-blue-600">
              Create your account
            </span>
          </div>

          <h2 className="text-3xl font-bold text-slate-900">
            Get started
          </h2>

          <p className="text-slate-500 mt-2 mb-6">
            Create your RecoverX account to continue.
          </p>

          <form onSubmit={handleSubmit} className="space-y-4">

            {/* Full Name */}
            <div>
              <label className="block text-sm font-medium mb-2">
                Full Name
              </label>
              <input
                type="text"
                required
                maxLength={100}
                value={fullName}
                onChange={(e) => setFullName(e.target.value)}
                placeholder="Enter your full name"
                className="w-full border border-slate-300 rounded-lg
                px-4 py-3 outline-none focus:ring-2 focus:ring-blue-500"
              />
            </div>

            {/* Email */}
            <div>
              <label className="block text-sm font-medium mb-2">
                Email Address
              </label>
              <input
                type="email"
                required
                maxLength={150}
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="you@example.com"
                className="w-full border border-slate-300 rounded-lg
                px-4 py-3 outline-none focus:ring-2 focus:ring-blue-500"
              />
            </div>

            {/* Phone */}
            <div>
              <label className="block text-sm font-medium mb-2">
                Phone Number
                <span className="text-slate-400 font-normal">
                  {" "}(Optional)
                </span>
              </label>
              <input
                type="tel"
                inputMode="numeric"
                pattern="[0-9]{10,15}"
                maxLength={15}
                value={phone}
                onChange={(e) =>
                  setPhone(e.target.value.replace(/\D/g, ""))
                }
                placeholder="Enter phone number"
                className="w-full border border-slate-300 rounded-lg
                px-4 py-3 outline-none focus:ring-2 focus:ring-blue-500"
              />
            </div>

            {/* Password */}
            <div>
              <label className="block text-sm font-medium mb-2">
                Password
              </label>

              <div className="relative">
                <input
                  type={showPassword ? "text" : "password"}
                  required
                  minLength={8}
                  maxLength={100}
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  placeholder="Minimum 8 characters"
                  className="w-full border border-slate-300 rounded-lg
                  px-4 py-3 pr-12 outline-none focus:ring-2 focus:ring-blue-500"
                />

                <button
                  type="button"
                  onClick={() => setShowPassword(!showPassword)}
                  className="absolute right-4 top-1/2 -translate-y-1/2
                  text-slate-500"
                >
                  {showPassword ? <EyeOff size={19} /> : <Eye size={19} />}
                </button>
              </div>
            </div>

            {/* Confirm Password */}
            <div>
              <label className="block text-sm font-medium mb-2">
                Confirm Password
              </label>

              <div className="relative">
                <input
                  type={showConfirmPassword ? "text" : "password"}
                  required
                  value={confirmPassword}
                  onChange={(e) => setConfirmPassword(e.target.value)}
                  placeholder="Re-enter your password"
                  className="w-full border border-slate-300 rounded-lg
                  px-4 py-3 pr-12 outline-none focus:ring-2 focus:ring-blue-500"
                />

                <button
                  type="button"
                  onClick={() =>
                    setShowConfirmPassword(!showConfirmPassword)
                  }
                  className="absolute right-4 top-1/2 -translate-y-1/2
                  text-slate-500"
                >
                  {showConfirmPassword
                    ? <EyeOff size={19} />
                    : <Eye size={19} />}
                </button>
              </div>
            </div>

            {/* Messages */}
            {error && (
              <div className="bg-red-50 text-red-600 text-sm p-3 rounded-lg">
                {error}
              </div>
            )}

            {success && (
              <div className="bg-green-50 text-green-700 text-sm p-3 rounded-lg">
                {success}
              </div>
            )}

            {/* Submit */}
            <button
              type="submit"
              disabled={loading}
              className="w-full bg-blue-600 hover:bg-blue-700
              disabled:bg-blue-400 text-white font-semibold py-3
              rounded-lg flex items-center justify-center gap-2 transition"
            >
              {loading ? "Creating account..." : "Create Account"}
              {!loading && <ArrowRight size={18} />}
            </button>

          </form>

          <p className="text-center text-sm text-slate-600 mt-6">
            Already have an account?{" "}
            <Link
              to="/login"
              state={{ from: location.state?.from }}
              className="text-blue-600 font-semibold hover:underline"
            >
              Sign in
            </Link>
          </p>

        </div>
      </div>
    </div>
  );
}
