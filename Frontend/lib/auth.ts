import { betterAuth } from "better-auth";
import { nextCookies } from "better-auth/next-js";

export const auth = betterAuth({
  // Stateless mode: omit 'database' property to store sessions in encrypted cookies.
  session: {
    cookieCache: {
      enabled: true,
      maxAge: 5 * 60, // Cache for 5 minutes
    },
    additionalFields: {
      accessToken: {
        type: "string",
        required: false,
      },
      refreshToken: {
        type: "string",
        required: false,
      },
      role: {
        type: "string",
        required: false,
      },
    },
  },
  plugins: [
    nextCookies()
  ],
  user: {
    additionalFields: {
      role: {
        type: "string",
        required: false,
      },
    },
  },
  credentials: {
    emailAndPassword: {
      async authorize(credentials: any) {
        const { email, password } = credentials || {};
        try {
          const apiURL = process.env.BACKEND_URL || "http://localhost:8080";
          const res = await fetch(`${apiURL}/api/v1/auth/login`, {
            method: "POST",
            headers: {
              "Content-Type": "application/json",
            },
            body: JSON.stringify({ email, password }),
          });


          if (!res.ok) {
            let errorMsg = "Invalid email or password";
            try {
              const err = await res.json();
              if (err && err.message) {
                errorMsg = err.message;
              }
            } catch (e) {
              // ignore json parse error
            }
            throw new Error(errorMsg);
          }

          const data = await res.json(); // LoginResponseDTO: role, accessToken, refreshToken, username, email
          
          return {
            id: data.email, // Use email as unique id since it's the subject
            name: data.username,
            email: data.email,
            role: data.role, // 'ADMIN' or 'EMPLOYEE'
            accessToken: data.accessToken,
            refreshToken: data.refreshToken,
          };
        } catch (error: any) {
          console.error("Authorize Callback Exception:", error);
          throw new Error(error.message || "Authentication failed");
        }
      },
    },
  },
});
