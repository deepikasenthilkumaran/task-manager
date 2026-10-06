import { useEffect, useState } from "react";
import { useParams, Link } from "react-router-dom";
import api, { errorText } from "../api.js";
import Navbar from "../components/Navbar.jsx";
import WorkloadBars from "../components/WorkloadBars.jsx";

export default function Dashboard() {
  const { wid } = useParams();
  const [data, setData] = useState(null);
  const [workload, setWorkload] = useState([]);
  const [error, setError] = useState("");

  useEffect(() => {
    async function load() {
      try {
        const d = await api.get(`/workspaces/${wid}/dashboard`);
        const w = await api.get(`/workspaces/${wid}/workload`);
        setData(d.data);
        setWorkload(w.data);
      } catch (err) {
        setError(err.response?.status === 403
          ? "Only managers can view the dashboard"
          : errorText(err));
      }
    }
    load();
  }, [wid]);

  if (error) {
    return (
      <div>
        <Navbar />
        <div className="page">
          <p className="error">{error}</p>
          <Link to={`/workspaces/${wid}`}>Back to workspace</Link>
        </div>
      </div>
    );
  }

  if (!data) {
    return (
      <div>
        <Navbar />
        <div className="page"><p>Loading...</p></div>
      </div>
    );
  }

  return (
    <div>
      <Navbar />
      <div className="page">
        <p><Link to={`/workspaces/${wid}`}>Back to workspace</Link></p>
        <h2>Manager dashboard</h2>

        <div className="stats">
          <div className="stat"><div className="num">{data.totalTasks}</div>Total tasks</div>
          <div className="stat"><div className="num">{data.byStatus.TODO}</div>To Do</div>
          <div className="stat"><div className="num">{data.byStatus.IN_PROGRESS}</div>In Progress</div>
          <div className="stat"><div className="num">{data.byStatus.COMPLETED}</div>Completed</div>
          <div className="stat"><div className="num overdue">{data.overdueCount}</div>Overdue</div>
        </div>

        <div className="section">
          <h3>Overdue tasks</h3>
          {data.overdueTasks.length === 0 ? (
            <p>Nothing is overdue.</p>
          ) : (
            <table className="simple">
              <thead>
                <tr><th>Task</th><th>Assigned to</th><th>Due</th></tr>
              </thead>
              <tbody>
                {data.overdueTasks.map((t) => (
                  <tr key={t.id}>
                    <td>{t.title}</td><td>{t.assignee}</td>
                    <td className="overdue">{t.dueDate}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>

        <div className="section">
          <h3>Open tasks per person</h3>
          {Object.keys(data.openTasksPerPerson).length === 0 ? (
            <p>No open assigned tasks.</p>
          ) : (
            <table className="simple">
              <tbody>
                {Object.entries(data.openTasksPerPerson).map(([name, count]) => (
                  <tr key={name}><td>{name}</td><td>{count}</td></tr>
                ))}
              </tbody>
            </table>
          )}
        </div>

        <div className="section">
          <h3>Team workload</h3>
          <WorkloadBars workload={workload} />
        </div>
      </div>
    </div>
  );
}