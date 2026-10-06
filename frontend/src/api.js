import axios from "axios";

const api = axios.create({
  baseURL: "http://localhost:8080/api",
});

api.interceptors.request.use((config) => {
  const token = localStorage.getItem("token");
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

export function errorText(err, fallback = "Something went wrong") {
  const d = err.response?.data;
  if (typeof d === "string") return d;
  return d?.message || fallback;
}

export default api;