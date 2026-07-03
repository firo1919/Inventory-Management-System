
// use this when u interact with real backend 

// import { useLoginMutation } from '@/services'


// export function useLogin() {
//     const [login, state] = useLoginMutation()


//     const execute = async (data: any) => {
//         const res = await login(data).unwrap()
//         localStorage.setItem('access_token', res.accessToken)
//         localStorage.setItem('refresh_token', res.refreshToken)
//         return res
//     }


//     return { execute, ...state }
// }


export function useLogin() {
    const execute = async (data: {
        email: string
        password: string
    }) => {
        // DEMO credentials
        if (
            data.email === 'demo@demo.com' &&
            data.password === 'password123'
        ) {
            const mockResponse = {
                accessToken: 'mock_access_token',
                refreshToken: 'mock_refresh_token',
            }

            localStorage.setItem('access_token', mockResponse.accessToken)
            localStorage.setItem('refresh_token', mockResponse.refreshToken)

            return mockResponse
        }

        throw new Error('Invalid demo credentials')
    }

    return {
        execute,
        isLoading: false,
        isError: false,
    }
}
