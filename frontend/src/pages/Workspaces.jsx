import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import api, { errorText } from "../api.js";
import Navbar from "../components/Navbar.jsx";

export default function Workspaces() {
  const navigate = useNavigate();
  const [workspaces, setWorkspaces] = useState([]);
  const [name, setName] = useState("");
  const [error, setError] = useState("");

  async function load() {
    try {
      const res = await api.get("/workspaces");
      setWorkspaces(res.data);
    } catch (err) {
      setError(errorText(err, "Could not load workspaces"));
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
    } catch (err) {
      setError(errorText(err, "Could not create workspace"));
    }
  }

  return (
    <div>
      <Navbar />
      <div className="page">
        <h2>Your workspaces</h2>
        <form onSubmit={createWorkspace} className="row">
          <input placeholder="New workspace name" value={name}
                 onChange={(e) => setName(e.target.value)} />
          <button type="submit">Create</button>
        </form>
        {error && <p className="error">{error}</p>}
        <div className="grid">
          {workspaces.map((w) => (
            <div key={w.id} className="tile"
                 onClick={() => navigate(`/workspaces/${w.id}`)}>
              <h3>{w.name}</h3>
              <p>Owner: {w.owner?.name}</p>
            </div>
          ))}
          {workspaces.length === 0 && <p>No workspaces yet. Create one above.</p>}
        </div>
      </div>
    </div>
  );
}