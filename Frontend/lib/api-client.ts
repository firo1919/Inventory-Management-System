import axios from "axios";
import { authClient } from "./auth-client";

export const apiClient = axios.create({
  baseURL: process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080",
  headers: {
    "Content-Type": "application/json",
  },
});

// Set tokens helper
export const setTokens = (accessToken: string, refreshToken: string) => {
  if (typeof window !== "undefined") {
    localStorage.setItem("access_token", accessToken);
    localStorage.setItem("refresh_token", refreshToken);
  }
};

// Clear tokens helper
export const clearTokens = () => {
  if (typeof window !== "undefined") {
    localStorage.removeItem("access_token");
    localStorage.removeItem("refresh_token");
  }
};

// Request Interceptor: Attach the access token
apiClient.interceptors.request.use(
  (config) => {
    if (typeof window !== "undefined") {
      const token = localStorage.getItem("access_token");
      if (token) {
        config.headers.Authorization = `Bearer ${token}`;
      }
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Response Interceptor: Handle 401 Unauthorized with token refresh
let isRefreshing = false;
let failedQueue: any[] = [];

const processQueue = (error: any, token: string | null = null) => {
  failedQueue.forEach((prom) => {
    if (error) {
      prom.reject(error);
    } else {
      prom.resolve(token);
    }
  });
  failedQueue = [];
};

apiClient.interceptors.response.use(
  (response) => {
    return response;
  },
  async (error) => {
    const originalRequest = error.config;

    // Handle token expiration
    if (error.response?.status === 401 && !originalRequest._retry) {
      if (isRefreshing) {
        return new Promise((resolve, reject) => {
          failedQueue.push({ resolve, reject });
        })
          .then((token) => {
            originalRequest.headers.Authorization = `Bearer ${token}`;
            return apiClient(originalRequest);
          })
          .catch((err) => {
            return Promise.reject(err);
          });
      }

      originalRequest._retry = true;
      isRefreshing = true;

      const refreshToken = typeof window !== "undefined" ? localStorage.getItem("refresh_token") : null;

      if (!refreshToken) {
        isRefreshing = false;
        clearTokens();
        // Redirect to login if on client side
        if (typeof window !== "undefined" && window.location.pathname !== "/auth/login") {
          authClient.signOut().then(() => {
            window.location.href = "/auth/login";
          });
        }
        return Promise.reject(error);
      }

      try {
        const apiURL = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080";
        const refreshResponse = await axios.post(`${apiURL}/api/v1/auth/refresh`, {
          refreshToken,
        });

        // The endpoint returns LoginResponseDTO containing new accessToken & refreshToken
        const { accessToken: newAccessToken, refreshToken: newRefreshToken } = refreshResponse.data;

        setTokens(newAccessToken, newRefreshToken);
        originalRequest.headers.Authorization = `Bearer ${newAccessToken}`;
        
        processQueue(null, newAccessToken);
        isRefreshing = false;

        return apiClient(originalRequest);
      } catch (refreshError) {
        processQueue(refreshError, null);
        isRefreshing = false;
        clearTokens();
        if (typeof window !== "undefined" && window.location.pathname !== "/auth/login") {
          authClient.signOut().then(() => {
            window.location.href = "/auth/login";
          });
        }
        return Promise.reject(refreshError);
      }
    }

    return Promise.reject(error);
  }
);
