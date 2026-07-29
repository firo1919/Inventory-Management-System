import { type DefaultSession } from "next-auth";

declare module "next-auth" {
    interface Session {
        user: {
            role?: string;
            accessToken?: string;
            refreshToken?: string;
        } & DefaultSession["user"];
    }

    interface User {
        role?: string;
        accessToken?: string;
        refreshToken?: string;
    }
}

declare module "next-auth/jwt" {
    interface JWT {
        role?: string;
        accessToken?: string;
        refreshToken?: string;
    }
}
