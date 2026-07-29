import * as z from "zod";

export const registerSchema = z
    .object({
        firstName: z.string().min(1, "First name is required"),
        lastName: z.string().min(1, "Last name is required"),
        username: z.string().min(3, "Username must be at least 3 characters"),
        email: z
            .string()
            .min(1, "Email is required")
            .email("Invalid email address"),
        phone: z.string().min(5, "Phone number must be at least 5 characters"),
        password: z.string().min(8, "Password must be at least 8 characters"),
        confirmPassword: z.string().min(8, "Confirm password is required"),
        bootstrapToken: z
            .string()
            .min(32, "Bootstrap token must be at least 32 characters"),
    })
    .refine((data) => data.password === data.confirmPassword, {
        message: "Passwords do not match",
        path: ["confirmPassword"],
    });

export type RegisterInput = z.infer<typeof registerSchema>;
