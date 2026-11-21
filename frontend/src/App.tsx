
import {
  BrowserRouter,
  Navigate,
  Route,
  Routes,
} from "react-router-dom";

import DashboardLayout from "./layouts/DashboardLayout";
import AcceptInvitation from "./pages/AcceptInvitation";
import BusinessSettings from "./pages/BusinessSettings";
import Commitments from "./pages/Commitments";
import Conversations from "./pages/Conversations";
import CreateBusiness from "./pages/CreateBusiness";
import Customers from "./pages/Customers";
import Dashboard from "./pages/Dashboard";
import ForgotPassword from "./pages/ForgotPassword";
import Invoices from "./pages/Invoices";
import Login from "./pages/Login";
import Payments from "./pages/Payments";
import Register from "./pages/Register";
import ResetPassword from "./pages/ResetPassword";

function App() {
  return (
    <BrowserRouter>
      <Routes>
        {/* Authentication */}
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />
        <Route path="/forgot-password" element={<ForgotPassword />} />
        <Route path="/reset-password" element={<ResetPassword />} />

        {/* Customer invitation */}
        <Route
          path="/accept-invitation"
          element={<AcceptInvitation />}
        />

        {/* Dashboard */}
        <Route element={<DashboardLayout />}>
          <Route path="/dashboard" element={<Dashboard />} />
          <Route path="/invoices" element={<Invoices />} />
          <Route path="/customers" element={<Customers />} />
          <Route path="/payments" element={<Payments />} />
          <Route path="/commitments" element={<Commitments />} />
          <Route path="/conversations" element={<Conversations />} />
          <Route path="/create-business" element={<CreateBusiness />} />
          <Route path="/settings" element={<BusinessSettings />} />
        </Route>

        <Route
          path="/"
          element={<Navigate to="/login" replace />}
        />

        <Route
          path="*"
          element={<Navigate to="/dashboard" replace />}
        />
      </Routes>
    </BrowserRouter>
  );
}

export default App;
