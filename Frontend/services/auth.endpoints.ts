import { baseApi } from './baseApi'
import { endpoints } from '@/constants/endpoints'
import type { LoginRequest, LoginResponse } from '@/modules/auth/types'

export const authApi = baseApi.injectEndpoints({
    endpoints: (builder) => ({
        login: builder.mutation<LoginResponse, LoginRequest>({
            query: (body) => ({
                url: endpoints.auth.login,
                method: 'POST',
                body,
            }),
        }),
    }),
})

export const { useLoginMutation } = authApi
