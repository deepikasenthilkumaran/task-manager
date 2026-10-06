import { useNavigate } from "react-router-dom";

export default function Workspaces() {
  const navigate = useNavigate();

  function logout() {
    localStorage.clear();
    navigate("/login");
  }

  return (
    <div className="card">
      <h2>Welcome, {localStorage.getItem("name")}</h2>
      <p>You are logged in. Workspaces will appear here.</p>
      <button onClick={logout}>Log out</button>
    </div>
  );
}