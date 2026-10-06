import { useEffect, useState } from "react";
import { useParams, Link } from "react-router-dom";
import api, { errorText } from "../api.js";
import Navbar from "../components/Navbar.jsx";

const COLUMNS = [
  { key: "TODO", label: "To Do" },
  { key: "IN_PROGRESS", label: "In Progress" },
  { key: "COMPLETED", label: "Completed" },
];

const EMPTY_FORM = { title: "", priority: "MEDIUM", dueDate: "", labels: "" };

export default function Board() {
  const { wid, pid } = useParams();
  const [tasks, setTasks] = useState([]);
  const [members, setMembers] = useState([]);
  const [workload, setWorkload] = useState([]);
  const [dragId, setDragId] = useState(null);
  const [selectedId, setSelectedId] = useState(null);
  const [edit, setEdit] = useState({});
  const [comments, setComments] = useState([]);
  const [commentText, setCommentText] = useState("");
  const [form, setForm] = useState(EMPTY_FORM);
  const [error, setError] = useState("");

  const myEmail = localStorage.getItem("email");
  const isManager = members.find((m) => m.email === myEmail)?.role === "MANAGER";
  const selected = tasks.find((t) => t.id === selectedId);
  const today = new Date().toLocaleDateString("en-CA");

  async function loadTasks() {
    try {
      const res = await api.get(`/projects/${pid}/tasks`);
      setTasks(res.data);
    } catch (err) {
      setError(errorText(err, "Could not load tasks"));
    }
  }

  async function loadPeople() {
    try {
      const [m, w] = await Promise.all([
        api.get(`/workspaces/${wid}/members`),
        api.get(`/workspaces/${wid}/workload`),
      ]);
      setMembers(m.data);
      setWorkload(w.data);
    } catch (err) {
      setError(errorText(err));
    }
  }

  useEffect(() => {
    loadTasks();
    loadPeople();
  }, [pid, wid]);

  async function createTask(e) {
    e.preventDefault();
    setError("");
    if (!form.title.trim()) return;
    try {
      await api.post(`/projects/${pid}/tasks`, {
        title: form.title,
        priority: form.priority,
        dueDate: form.dueDate || null,
        labels: form.labels || null,
      });
      setForm(EMPTY_FORM);
      loadTasks();
    } catch (err) {
      setError(errorText(err, "Could not create task"));
    }
  }

  async function dropOn(status) {
    const id = dragId;
    setDragId(null);
    if (id == null) return;
    const task = tasks.find((t) => t.id === id);
    if (!task || task.status === status) return;
    try {
      await api.put(`/tasks/${id}/status`, { status });
      loadTasks();
      loadPeople();
    } catch (err) {
      setError(errorText(err, "Could not move task"));
    }
  }

  async function openTask(t) {
    setSelectedId(t.id);
    setEdit({
      title: t.title,
      description: t.description || "",
      priority: t.priority,
      dueDate: t.dueDate || "",
      labels: t.labels || "",
    });
    setCommentText("");
    try {
      const res = await api.get(`/tasks/${t.id}/comments`);
      setComments(res.data);
    } catch {
      setComments([]);
    }
  }

  async function saveEdit() {
    try {
      await api.put(`/tasks/${selectedId}`, {
        title: edit.title,
        description: edit.description,
        priority: edit.priority,
        dueDate: edit.dueDate || null,
        labels: edit.labels,
      });
      loadTasks();
      loadPeople();
    } catch (err) {
      setError(errorText(err, "Could not save changes"));
    }
  }

  async function addComment() {
    if (!commentText.trim()) return;
    try {
      await api.post(`/tasks/${selectedId}/comments`, { text: commentText });
      setCommentText("");
      const res = await api.get(`/tasks/${selectedId}/comments`);
      setComments(res.data);
    } catch (err) {
      setError(errorText(err));
    }
  }

  async function assign(userId, force = false) {
    if (!userId) return;
    try {
      await api.put(`/tasks/${selectedId}/assign`, null,
        { params: { assigneeId: userId, force } });
      loadTasks();
      loadPeople();
    } catch (err) {
      if (err.response?.status === 409 && !force) {
        if (window.confirm(errorText(err) + "\n\nAssign anyway?")) {
          assign(userId, true);
        }
      } else {
        setError(errorText(err, "Could not assign"));
      }
    }
  }

  async function suggest() {
    try {
      const res = await api.get(`/workspaces/${wid}/suggest-assignee`);
      window.alert(`Suggested: ${res.data.name} (${res.data.level}, score ${res.data.score})`);
    } catch (err) {
      setError(errorText(err));
    }
  }

  async function removeTask() {
    if (!window.confirm("Delete this task?")) return;
    try {
      await api.delete(`/tasks/${selectedId}`);
      setSelectedId(null);
      loadTasks();
      loadPeople();
    } catch (err) {
      setError(errorText(err, "Only managers can delete tasks"));
    }
  }

  return (
    <div>
      <Navbar />
      <div className="page">
        <p><Link to={`/workspaces/${wid}`}>Back to workspace</Link></p>
        <h2>Task board</h2>
        {error && <p className="error">{error}</p>}

        <form onSubmit={createTask} className="row">
          <input placeholder="New task title" value={form.title}
                 onChange={(e) => setForm({ ...form, title: e.target.value })} />
          <select value={form.priority}
                  onChange={(e) => setForm({ ...form, priority: e.target.value })}>
            <option>LOW</option>
            <option>MEDIUM</option>
            <option>HIGH</option>
          </select>
          <input type="date" value={form.dueDate}
                 onChange={(e) => setForm({ ...form, dueDate: e.target.value })} />
          <input placeholder="labels (bug,urgent)" value={form.labels}
                 onChange={(e) => setForm({ ...form, labels: e.target.value })} />
          <button type="submit">Add task</button>
        </form>

        <div className="board">
          {COLUMNS.map((col) => {
            const colTasks = tasks.filter((t) => t.status === col.key);
            return (
              <div key={col.key} className="column"
                   onDragOver={(e) => e.preventDefault()}
                   onDrop={() => dropOn(col.key)}>
                <h4>{col.label} ({colTasks.length})</h4>
                {colTasks.map((t) => (
                  <div key={t.id} className="card-task" draggable
                       onDragStart={() => setDragId(t.id)}
                       onClick={() => openTask(t)}>
                    <div className="title">{t.title}</div>
                    <span className={`badge ${t.priority}`}>{t.priority}</span>
                    {t.labels && t.labels.split(",").map((l, i) => (
                      <span key={l + i} className="badge">{l.trim()}</span>
                    ))}
                    <div className="small">
                      {t.assignee ? t.assignee.name : "Unassigned"}
                    </div>
                    {t.dueDate && (
                      <div className={t.dueDate < today && t.status !== "COMPLETED"
                        ? "small overdue" : "small"}>
                        Due {t.dueDate}
                      </div>
                    )}
                  </div>
                ))}
              </div>
            );
          })}
        </div>
      </div>

      {selected && (
        <div className="modal-bg" onClick={() => setSelectedId(null)}>
          <div className="modal" onClick={(e) => e.stopPropagation()}>
            <h3>Task #{selected.id}</h3>

            <label>Title</label>
            <input value={edit.title}
                   onChange={(e) => setEdit({ ...edit, title: e.target.value })} />
            <label>Description</label>
            <textarea rows="3" value={edit.description}
                      onChange={(e) => setEdit({ ...edit, description: e.target.value })} />
            <label>Priority</label>
            <select value={edit.priority}
                    onChange={(e) => setEdit({ ...edit, priority: e.target.value })}>
              <option>LOW</option>
              <option>MEDIUM</option>
              <option>HIGH</option>
            </select>
            <label>Due date</label>
            <input type="date" value={edit.dueDate}
                   onChange={(e) => setEdit({ ...edit, dueDate: e.target.value })} />
            <label>Labels</label>
            <input value={edit.labels}
                   onChange={(e) => setEdit({ ...edit, labels: e.target.value })} />
            <button onClick={saveEdit}>Save changes</button>

            <hr />
            {isManager ? (
              <div>
                <label>Assign to</label>
                <select value={selected.assignee?.id ?? ""}
                        onChange={(e) => assign(Number(e.target.value))}>
                  <option value="">Choose a person</option>
                  {members.map((m) => {
                    const level = workload.find((w) => w.userId === m.userId)?.level;
                    return (
                      <option key={m.userId} value={m.userId}>
                        {m.name} ({level})
                      </option>
                    );
                  })}
                </select>
                <button onClick={suggest}>Suggest assignee</button>
              </div>
            ) : (
              <p>Assigned to: {selected.assignee ? selected.assignee.name : "Nobody"}</p>
            )}

            <hr />
            <h4>Comments</h4>
            {comments.map((c) => (
              <div key={c.id} className="comment">
                <b>{c.author?.name}</b>: {c.text}
              </div>
            ))}
            <input placeholder="Write a comment" value={commentText}
                   onChange={(e) => setCommentText(e.target.value)} />
            <button onClick={addComment}>Add comment</button>

            <hr />
            {isManager && (
              <button className="danger" onClick={removeTask}>Delete task</button>
            )}
            <button onClick={() => setSelectedId(null)}>Close</button>
          </div>
        </div>
      )}
    </div>
  );
}