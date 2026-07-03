import { storage } from './storage'

const ACCESS = 'access_token'
const REFRESH = 'refresh_token'

export const tokenService = {
    getAccess() {
        return storage.get(ACCESS)
    },
    getRefresh() {
        return storage.get(REFRESH)
    },
    setTokens(access: string, refresh: string) {
        storage.set(ACCESS, access)
        storage.set(REFRESH, refresh)
    },
    clear() {
        storage.remove(ACCESS)
        storage.remove(REFRESH)
    },
}
