import { baseApi } from './baseApi'
import { endpoints } from '@/constants/endpoints'
import type { User } from '@/types/user'

export const userApi = baseApi.injectEndpoints({
    endpoints: (builder) => ({
        me: builder.query<User, void>({
            query: () => ({
                url: endpoints.user.me,
                method: 'GET',
            }),
        }),
    }),
})

export const { useMeQuery } = userApi
