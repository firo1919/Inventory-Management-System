import { createApi, fetchBaseQuery } from '@reduxjs/toolkit/query/react'


export const baseApi = createApi({
    reducerPath: 'api',
    baseQuery: fetchBaseQuery({
        baseUrl: process.env.NEXT_PUBLIC_API_URL,
        prepareHeaders: (headers) => {
            if (typeof window !== 'undefined') {
                const token = localStorage.getItem('access_token')
                if (token) headers.set('authorization', `Bearer ${token}`)
            }
            return headers
        },
    }),
    endpoints: () => ({}),
})