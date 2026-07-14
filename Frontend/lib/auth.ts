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
  emailAndPassword: {
    enabled: true,
    password: {
      verify: async () => {
        // Verified dynamically in the custom before hook; fallback only
        return true;
      },
    },
  },
  hooks: {
    before: async (ctx: any) => {
      if (ctx.path === "/sign-in/email" && ctx.request.method === "POST") {
        const { email, password } = (ctx.body || {}) as any;
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
              // ignore
            }
            return ctx.json({
              error: {
                message: errorMsg,
              }
            }, { status: 400 });
          }

          const data = await res.json(); // role, accessToken, refreshToken, username, email

          // Find or create the user in Better Auth memory DB
          const userResult = await ctx.context.internalAdapter.findUserByEmail(data.email);
          let user = userResult?.user;
          if (!user) {
            user = await ctx.context.internalAdapter.createUser({
              email: data.email,
              name: data.username,
              emailVerified: true,
              role: data.role,
            });
          } else {
            // Sync role if updated
            user = await ctx.context.internalAdapter.updateUser(user.id, {
              role: data.role,
            });
          }

          // Create Session inside Better Auth
          const session = await ctx.context.internalAdapter.createSession(
            user.id,
            false,
            {
              accessToken: data.accessToken,
              refreshToken: data.refreshToken,
              role: data.role,
            }
          );

          // Set session cookie in headers/context
          await ctx.context.setNewSession({
            session,
            user,
          });

          return ctx.json({
            session,
            user,
          });
        } catch (error: any) {
          console.error("Stateless Custom Login Exception:", error);
          return ctx.json({
            error: {
              message: error.message || "Authentication failed",
            }
          }, { status: 500 });
        }
      }
    },
  },
});
