
import {
  CreditCard,
  FileText,
  Handshake,
  LayoutDashboard,
  MessageCircle,
  Settings,
  Users,
  Wallet,
} from "lucide-react";
import { NavLink } from "react-router-dom";

interface SidebarProps {
  mode: "business" | "customer";
  businessName?: string;
}

export default function Sidebar({ mode, businessName }: SidebarProps) {
  const businessLinks = [
    { name: "Dashboard", path: "/dashboard", icon: LayoutDashboard },
    { name: "Customers", path: "/customers", icon: Users },
    { name: "Invoices", path: "/invoices", icon: FileText },
    { name: "Payments", path: "/payments", icon: CreditCard },
    { name: "Commitments", path: "/commitments", icon: Handshake },
    { name: "Conversations", path: "/conversations", icon: MessageCircle },
    { name: "Settings", path: "/settings", icon: Settings },
  ];

  const customerLinks = [
    { name: "Dashboard", path: "/dashboard", icon: LayoutDashboard },
    { name: "My Businesses", path: "/businesses", icon: Wallet },
    { name: "My Invoices", path: "/invoices", icon: FileText },
    { name: "My Payments", path: "/payments", icon: CreditCard },
    { name: "My Commitments", path: "/commitments", icon: Handshake },
    { name: "Conversations", path: "/conversations", icon: MessageCircle },
  ];

  const links = mode === "business" ? businessLinks : customerLinks;

  return (
    <aside className="w-64 min-h-screen bg-slate-950 text-white p-5">

      <div className="flex items-center gap-3 mb-10">
        <div className="bg-blue-600 p-2 rounded-lg">
          <Wallet size={22} />
        </div>
        <span className="text-xl font-bold">RecoverX</span>
      </div>

      {mode === "business" && (
        <div className="mb-7">
          <p className="text-xs uppercase text-slate-500 mb-2">
            Current Business
          </p>

          <div className="bg-slate-800 rounded-lg p-3">
            <p className="text-sm font-semibold truncate">
              {businessName || "No business selected"}
            </p>
          </div>
        </div>
      )}

      <p className="text-xs uppercase text-slate-500 mb-4">
        {mode === "business" ? "Business Workspace" : "Customer Workspace"}
      </p>

      <nav className="space-y-2">
        {links.map((item) => {
          const Icon = item.icon;

          return (
            <NavLink
              key={item.name}
              to={item.path}
              end={item.path === "/dashboard"}
              className={({ isActive }) =>
                `flex items-center gap-3 px-4 py-3 rounded-lg text-sm transition ${
                  isActive
                    ? "bg-blue-600 text-white"
                    : "text-slate-300 hover:bg-slate-800"
                }`
              }
            >
              <Icon size={19} />
              {item.name}
            </NavLink>
          );
        })}
      </nav>

    </aside>
  );
}
