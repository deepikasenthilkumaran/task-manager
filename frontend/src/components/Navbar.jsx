import { useEffect, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import api from "../api.js";

export default function Navbar() {
  const navigate = useNavigate();
  const [notes, setNotes] = useState([]);
  const [open, setOpen] = useState(false);

  async function load() {
    try {
      const res = await api.get("/notifications");
      setNotes(res.data);
    } catch {
      // ignore errors here
    }
  }

  useEffect(() => {
    load();
    const timer = setInterval(load, 15000);
    return () => clearInterval(timer);
  }, []);

  async function markSeen(n) {
    await api.put(`/notifications/${n.id}/seen`);
    load();
  }

  function logout() {
    localStorage.clear();
    navigate("/login");
  }

  const unseen = notes.filter((n) => !n.seen).length;

  return (
    <div className="navbar">
      <Link to="/workspaces" className="brand">TaskFlow</Link>
      <div className="nav-right">
        <div className="bell-wrap">
          <button onClick={() => setOpen(!open)}>Notifications ({unseen})</button>
          {open && (
            <div className="dropdown">
              {notes.length === 0 && <p className="muted">No notifications</p>}
              {notes.map((n) => (
                <div key={n.id} className={n.seen ? "note seen" : "note"}
                     onClick={() => !n.seen && markSeen(n)}>
                  {n.message}
                </div>
              ))}
            </div>
          )}
        </div>
        <span>{localStorage.getItem("name")}</span>
        <button onClick={logout}>Log out</button>
      </div>
    </div>
  );
}