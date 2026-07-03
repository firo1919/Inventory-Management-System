import { Protected } from '@/components/Protected'

export default function ProtectedLayout({
    children,
}: {
    children: React.ReactNode
}) {
    return <Protected>{children}</Protected>
}
