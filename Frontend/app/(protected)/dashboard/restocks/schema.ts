import * as z from "zod";

export const restockSchema = z.object({
    productId: z.string().min(1, "Product selection is required"),
    quantity: z
        .number()
        .int()
        .positive("Quantity must be a positive integer greater than zero"),
});

export type RestockInput = z.infer<typeof restockSchema>;
