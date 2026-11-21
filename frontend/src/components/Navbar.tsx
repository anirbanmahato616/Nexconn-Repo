import { LogOut, UserRound } from "lucide-react";
import { useNavigate } from "react-router-dom";

interface NavbarProps {
  mode: "business" | "customer";
  setMode: (mode: "business" | "customer") => void;
}

export default function Navbar({ mode, setMode }: NavbarProps) {
  const navigate = useNavigate();

  const fullName = sessionStorage.getItem("fullName");

  const handleLogout = () => {
    sessionStorage.clear();
    navigate("/login");
  };

  return (
    <header className="h-16 bg-white border-b border-slate-200
      px-6 flex items-center justify-between">

      <div className="flex items-center gap-2">
        <button
          onClick={() => setMode("business")}
          className={`px-4 py-2 rounded-lg text-sm font-medium ${
            mode === "business"
              ? "bg-blue-600 text-white"
              : "bg-slate-100 text-slate-600"
          }`}
        >
          Business Mode
        </button>

        <button
          onClick={() => setMode("customer")}
          className={`px-4 py-2 rounded-lg text-sm font-medium ${
            mode === "customer"
              ? "bg-blue-600 text-white"
              : "bg-slate-100 text-slate-600"
          }`}
        >
          Customer Mode
        </button>
      </div>

      <div className="flex items-center gap-4">
        <div className="hidden sm:flex items-center gap-2 text-sm text-slate-700">
          <UserRound size={18} />
          {fullName}
        </div>

        <button
          onClick={handleLogout}
          className="flex items-center gap-2 text-sm text-slate-500 hover:text-red-600"
        >
          <LogOut size={18} />
          Logout
        </button>
      </div>

    </header>
  );
}