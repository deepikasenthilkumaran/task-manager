import { useState } from "react";
import { useNavigate } from "react-router-dom";
import api from "../api.js";

export default function Login() {
  const navigate = useNavigate();
  const [mode, setMode] = useState("login"); // "login" or "register"
  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [message, setMessage] = useState("");

  async function handleSubmit(e) {
    e.preventDefault();
    setError("");
    setMessage("");
    try {
      if (mode === "register") {
        await api.post("/auth/register", { name, email, password });
        setMessage("Registered. Please log in.");
        setMode("login");
      } else {
        const res = await api.post("/auth/login", { email, password });
        localStorage.setItem("token", res.data.token);
        localStorage.setItem("name", res.data.name);
                localStorage.setItem("email", email);
        navigate("/workspaces");
      }
    } catch (err) {
      const data = err.response?.data;
      setError(typeof data === "string" ? data : "Something went wrong");
    }
  }

  return (
    <div className="card">
      <h2>{mode === "login" ? "Log in" : "Create account"}</h2>
      <form onSubmit={handleSubmit}>
        {mode === "register" && (
          <input placeholder="Name" value={name}
                 onChange={(e) => setName(e.target.value)} />
        )}
        <input placeholder="Email" value={email}
               onChange={(e) => setEmail(e.target.value)} />
        <input type="password" placeholder="Password" value={password}
               onChange={(e) => setPassword(e.target.value)} />
        <button type="submit">
          {mode === "login" ? "Log in" : "Register"}
        </button>
      </form>
      {error && <p className="error">{error}</p>}
      {message && <p className="success">{message}</p>}
      <p>
        {mode === "login" ? "No account? " : "Have an account? "}
        <button className="link"
                onClick={() => setMode(mode === "login" ? "register" : "login")}>
          {mode === "login" ? "Register" : "Log in"}
        </button>
      </p>
    </div>
  );
}