import { Protected } from "@/components/common/Protected";

export default function ProtectedLayout({
    children,
}: {
    children: React.ReactNode;
}) {
    return <Protected>{children}</Protected>;
}
