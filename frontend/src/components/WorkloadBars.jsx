export default function WorkloadBars({ workload }) {
  return (
    <div>
      {workload.map((w) => (
        <div key={w.userId} className="wl-row">
          <span className="wl-name">{w.name}</span>
          <div className="wl-track">
            <div className={`wl-bar ${w.level}`}
                 style={{ width: `${Math.max(Math.min(w.score * 7, 100), 3)}%` }} />
          </div>
          <span className="wl-text">{w.score} pts, {w.level}</span>
        </div>
      ))}
    </div>
  );
}