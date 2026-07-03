'use client'

import { useRouter } from 'next/navigation'
import { useLogin } from '../hooks/useLogin'

export function LoginForm() {
    const router = useRouter()
    const { execute } = useLogin()

    const onSubmit = async () => {
        try {
            await execute({
                email: 'demo@demo.com',
                password: 'password123',
            })

            // ✅ redirect after successful login
            router.push('/dashboard')
        } catch {
            alert('Invalid demo credentials')
        }
    }

    return <button onClick={onSubmit}>Login</button>
}
