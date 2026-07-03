import { tokenService } from '@/utils/tokens'

export function useLogout() {
    return () => {
        tokenService.clear()
        window.location.href = '/'
    }
}
