import { Routes, Route, Navigate } from "react-router-dom";
import Login from "./pages/Login.jsx";
import Workspaces from "./pages/Workspaces.jsx";
import WorkspacePage from "./pages/WorkspacePage.jsx";
import Board from "./pages/Board.jsx";
import Dashboard from "./pages/Dashboard.jsx";

function PrivateRoute({ children }) {
  return localStorage.getItem("token") ? children : <Navigate to="/login" />;
}

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<Login />} />
      <Route path="/workspaces" element={<PrivateRoute><Workspaces /></PrivateRoute>} />
      <Route path="/workspaces/:wid" element={<PrivateRoute><WorkspacePage /></PrivateRoute>} />
      <Route path="/workspaces/:wid/projects/:pid" element={<PrivateRoute><Board /></PrivateRoute>} />
      <Route path="/workspaces/:wid/dashboard" element={<PrivateRoute><Dashboard /></PrivateRoute>} />
      <Route path="*" element={<Navigate to="/workspaces" />} />
    </Routes>
  );
}