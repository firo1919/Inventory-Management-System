import * as z from "zod";

export const saleSchema = z.object({
  productId: z.string().min(1, "Product selection is required"),
  quantity: z
    .number()
    .int()
    .positive("Quantity must be a positive integer greater than zero"),
  salePrice: z.number().nonnegative("Sale price must be positive or zero"),
});

export type SaleInput = z.infer<typeof saleSchema>;
