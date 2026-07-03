export const endpoints = {
    auth: {
        login: '/auth/login',
        refresh: '/auth/refresh',
        logout: '/auth/logout',
    },
    user: {
        me: '/users/me',
    },
} as const satisfies Record<string, Record<string, string>>