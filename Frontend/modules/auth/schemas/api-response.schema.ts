import { z } from 'zod'

export const apiResponseSchema = <T extends z.ZodTypeAny>(data: T) =>
    z.object({
        success: z.boolean(),
        data,
        message: z.string().optional(),
    })
