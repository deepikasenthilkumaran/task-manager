import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import api from "../api.js";

export default function Workspaces() {
  const navigate = useNavigate();
  const [workspaces, setWorkspaces] = useState([]);
  const [name, setName] = useState("");
  const [error, setError] = useState("");

  async function load() {
    try {
      const res = await api.get("/workspaces");
      setWorkspaces(res.data);
    } catch {
      setError("Could not load workspaces");
    }
  }

  useEffect(() => {
    load();
  }, []);

  async function createWorkspace(e) {
    e.preventDefault();
    setError("");

    if (!name.trim()) return;

    try {
      await api.post("/workspaces", { name });
      setName("");
      load();
    } catch {
      setError("Could not create workspace");
    }
  }

  function logout() {
    localStorage.clear();
    navigate("/login");
  }

  return (
    <div className="page">
      <div className="topbar">
        <h2>Your workspaces</h2>

        <div>
          <span>
            Hello, {localStorage.getItem("name")}{" "}
          </span>

          <button onClick={logout}>Log out</button>
        </div>
      </div>

      <form onSubmit={createWorkspace} className="row">
        <input
          placeholder="New workspace name"
          value={name}
          onChange={(e) => setName(e.target.value)}
        />

        <button type="submit">Create</button>
      </form>

      {error && <p className="error">{error}</p>}

      <div className="grid">
        {workspaces.map((w) => (
          <div
            key={w.id}
            className="tile"
            onClick={() => navigate(`/workspaces/${w.id}`)}
          >
            <h3>{w.name}</h3>
            <p>Owner: {w.owner?.name}</p>
          </div>
        ))}

        {workspaces.length === 0 && (
          <p>No workspaces yet. Create one above.</p>
        )}
      </div>
    </div>
  );
}