import { useEffect, useState } from "react";
import { useNavigate, useParams, Link } from "react-router-dom";
import api, { errorText } from "../api.js";
import Navbar from "../components/Navbar.jsx";
import WorkloadBars from "../components/WorkloadBars.jsx";

export default function WorkspacePage() {
  const { wid } = useParams();
  const navigate = useNavigate();
  const [projects, setProjects] = useState([]);
  const [members, setMembers] = useState([]);
  const [workload, setWorkload] = useState([]);
  const [pName, setPName] = useState("");
  const [pDesc, setPDesc] = useState("");
  const [mEmail, setMEmail] = useState("");
  const [mRole, setMRole] = useState("MEMBER");
  const [error, setError] = useState("");

  const myEmail = localStorage.getItem("email");
  const isManager = members.find((m) => m.email === myEmail)?.role === "MANAGER";

  async function load() {
    try {
      const [p, m, w] = await Promise.all([
        api.get(`/workspaces/${wid}/projects`),
        api.get(`/workspaces/${wid}/members`),
        api.get(`/workspaces/${wid}/workload`),
      ]);
      setProjects(p.data);
      setMembers(m.data);
      setWorkload(w.data);
    } catch (err) {
      setError(errorText(err, "Could not load this workspace"));
    }
  }

  useEffect(() => {
    load();
  }, [wid]);

  async function createProject(e) {
    e.preventDefault();
    setError("");
    if (!pName.trim()) return;
    try {
      await api.post(`/workspaces/${wid}/projects`, { name: pName, description: pDesc });
      setPName("");
      setPDesc("");
      load();
    } catch (err) {
      setError(errorText(err, "Could not create project"));
    }
  }

  async function addMember(e) {
    e.preventDefault();
    setError("");
    try {
      await api.post(`/workspaces/${wid}/members`, { email: mEmail, role: mRole });
      setMEmail("");
      load();
    } catch (err) {
      setError(errorText(err, "Could not add member"));
    }
  }

  return (
    <div>
      <Navbar />
      <div className="page">
        <p><Link to="/workspaces">Back to all workspaces</Link></p>
        <div className="topbar">
          <h2>Workspace</h2>
          {isManager && (
            <button onClick={() => navigate(`/workspaces/${wid}/dashboard`)}>
              Manager dashboard
            </button>
          )}
        </div>
        {error && <p className="error">{error}</p>}

        <div className="section">
          <h3>Projects</h3>
          {isManager && (
            <form onSubmit={createProject} className="row">
              <input placeholder="Project name" value={pName}
                     onChange={(e) => setPName(e.target.value)} />
              <input placeholder="Description" value={pDesc}
                     onChange={(e) => setPDesc(e.target.value)} />
              <button type="submit">Add project</button>
            </form>
          )}
          <div className="grid">
            {projects.map((p) => (
              <div key={p.id} className="tile"
                   onClick={() => navigate(`/workspaces/${wid}/projects/${p.id}`)}>
                <h3>{p.name}</h3>
                <p>{p.description}</p>
              </div>
            ))}
            {projects.length === 0 && <p>No projects yet.</p>}
          </div>
        </div>

        <div className="section">
          <h3>Team workload</h3>
          <WorkloadBars workload={workload} />
        </div>

        <div className="section">
          <h3>Members</h3>
          <table className="simple">
            <thead>
              <tr><th>Name</th><th>Email</th><th>Role</th></tr>
            </thead>
            <tbody>
              {members.map((m) => (
                <tr key={m.userId}>
                  <td>{m.name}</td><td>{m.email}</td><td>{m.role}</td>
                </tr>
              ))}
            </tbody>
          </table>
          {isManager && (
            <form onSubmit={addMember} className="row">
              <input placeholder="Email of a registered user" value={mEmail}
                     onChange={(e) => setMEmail(e.target.value)} />
              <select value={mRole} onChange={(e) => setMRole(e.target.value)}>
                <option value="MEMBER">MEMBER</option>
                <option value="MANAGER">MANAGER</option>
              </select>
              <button type="submit">Add member</button>
            </form>
          )}
        </div>
      </div>
    </div>
  );
}