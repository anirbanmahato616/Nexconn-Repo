
import { ArrowRight, Eye, EyeOff, Wallet } from "lucide-react";
import { useState } from "react";
import { Link, useLocation, useNavigate } from "react-router-dom";
import api from "../services/api";

export default function Login() {
  const navigate = useNavigate();
  const location = useLocation();

  const [showPassword, setShowPassword] = useState(false);
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const redirectTo = location.state?.from || "/dashboard";

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    setError("");
    setLoading(true);

    try {
      const response = await api.post("/api/auth/login", {
        email,
        password,
      });

      const data = response.data;

      sessionStorage.setItem("accessToken", data.accessToken);
      sessionStorage.setItem("tokenType", data.tokenType);
      sessionStorage.setItem("userId", String(data.userId));
      sessionStorage.setItem("fullName", data.fullName);
      sessionStorage.setItem("email", data.email);

      navigate(redirectTo, { replace: true });

    } catch (err: any) {
      const responseData = err.response?.data;

      setError(
        typeof responseData === "string"
          ? responseData
          : responseData?.message || "Invalid email or password."
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
            Smarter payment recovery starts here.
          </h1>

          <p className="text-slate-400 mt-6 text-lg leading-relaxed">
            Manage invoices, track commitments and simplify
            payment recovery with RecoverX.
          </p>
        </div>

        <p className="text-slate-500 text-sm">
          © 2026 RecoverX. All rights reserved.
        </p>
      </div>

      {/* Right Section */}
      <div className="flex-1 flex items-center justify-center p-6">

        <div className="w-full max-w-md">

          <div className="lg:hidden flex items-center gap-2 mb-12">
            <div className="bg-blue-600 text-white p-2 rounded-lg">
              <Wallet size={23} />
            </div>
            <span className="text-2xl font-bold text-slate-900">
              RecoverX
            </span>
          </div>

          <h2 className="text-3xl font-bold text-slate-900">
            Welcome back
          </h2>

          <p className="text-slate-500 mt-2 mb-8">
            Sign in to continue to your account.
          </p>

          <form onSubmit={handleSubmit} className="space-y-5">

            <div>
              <label className="block text-sm font-medium mb-2">
                Email address
              </label>

              <input
                type="email"
                required
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="you@example.com"
                className="w-full border border-slate-300 rounded-lg
                px-4 py-3 outline-none focus:ring-2 focus:ring-blue-500"
              />
            </div>

            <div>
              <div className="flex justify-between mb-2">
                <label className="text-sm font-medium">
                  Password
                </label>

                <Link
                  to="/forgot-password"
                  className="text-sm text-blue-600 hover:text-blue-700"
                >
                  Forgot password?
                </Link>
              </div>

              <div className="relative">
                <input
                  type={showPassword ? "text" : "password"}
                  required
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  placeholder="Enter your password"
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

            {error && (
              <div className="bg-red-50 text-red-600 text-sm p-3 rounded-lg">
                {error}
              </div>
            )}

            <button
              type="submit"
              disabled={loading}
              className="w-full bg-blue-600 hover:bg-blue-700
              disabled:bg-blue-400 text-white font-semibold py-3
              rounded-lg flex items-center justify-center gap-2 transition"
            >
              {loading ? "Signing in..." : "Sign in"}
              {!loading && <ArrowRight size={18} />}
            </button>

          </form>

          <p className="text-center text-sm text-slate-600 mt-8">
            Don't have an account?{" "}
            <Link
              to="/register"
              state={{ from: location.state?.from }}
              className="text-blue-600 font-semibold hover:underline"
            >
              Create account
            </Link>
          </p>

        </div>
      </div>
    </div>
  );
}
