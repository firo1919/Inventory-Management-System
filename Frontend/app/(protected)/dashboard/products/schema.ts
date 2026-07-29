import * as z from "zod";

export const productSchema = z.object({
    name: z.string().min(1, "Product name is required"),
    sku: z.string().min(1, "SKU is required"),
    description: z.string().optional(),
    costPrice: z.number().positive("Cost price must be greater than zero"),
    sellingPrice: z
        .number()
        .positive("Selling price must be greater than zero"),
    quantity: z
        .number()
        .int()
        .nonnegative("Quantity must be a positive integer or zero"),
    lowStockThreshold: z
        .number()
        .int()
        .nonnegative("Low stock threshold must be a positive integer or zero"),
    categoryIds: z.array(z.string()),
});

export type ProductInput = z.infer<typeof productSchema>;
