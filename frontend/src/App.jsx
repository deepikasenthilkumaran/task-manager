import { Routes, Route, Navigate } from "react-router-dom";
import Login from "./pages/Login.jsx";
import Workspaces from "./pages/Workspaces.jsx";

function PrivateRoute({ children }) {
  return localStorage.getItem("token") ? children : <Navigate to="/login" />;
}

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<Login />} />
      <Route
        path="/workspaces"
        element={
          <PrivateRoute>
            <Workspaces />
          </PrivateRoute>
        }
      />
      <Route path="*" element={<Navigate to="/workspaces" />} />
    </Routes>
  );
}