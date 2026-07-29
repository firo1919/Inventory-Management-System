"use client";

import { ReactNode } from "react";
import { useAuthGuard } from "@/hooks/useAuthGuard";

export function Protected({ children }: { children: ReactNode }) {
    useAuthGuard();
    return <>{children}</>;
}
