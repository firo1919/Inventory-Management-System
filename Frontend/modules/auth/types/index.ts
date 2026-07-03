import { z } from 'zod'
import { loginSchema } from '../schemas/login.schema'


export type LoginRequest = z.infer<typeof loginSchema>


export interface LoginResponse {
    accessToken: string
    refreshToken: string
}