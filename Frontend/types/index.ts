export interface Product {
  id: string;
  name: string;
  sku: string;
  description?: string;
  costPrice: number;
  sellingPrice: number;
  quantity: number;
  lowStockThreshold: number;
  active: boolean;
  categoryIds?: string[];
  imageUrls?: string[];
  createdAt?: string;
  updatedAt?: string;
}

export interface Category {
  id: string;
  name: string;
  description?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface Employee {
  id: string;
  userId: string;
  firstName: string;
  lastName: string;
  email: string;
  phone?: string;
  role: string;
  department?: string;
  active: boolean;
  hireDate?: string;
  createdAt?: string;
  updatedAt?: string;
  username?: string;
  name?: string;
  profilePictureUrl?: string;
}

export interface AuditLog {
  id: string;
  userId?: string;
  username?: string;
  action: string;
  resourceType?: string;
  entityType?: string;
  entityId?: string;
  details?: string;
  ipAddress?: string;
  correlationId?: string;
  status?: string;
  errorMessage?: string;
  timestamp?: string;
  createdAt?: string;
}

export interface Sale {
  id: string;
  productId: string;
  quantity: number;
  unitPrice: number;
  totalPrice: number;
  saleDate: string;
  customerName?: string;
  status: string;
  createdAt?: string;
  updatedAt?: string;
  productName?: string;
  salePrice?: number;
  timestamp?: string;
  employeeName?: string;
}

export interface Restock {
  id: string;
  productId: string;
  quantity: number;
  unitCost: number;
  totalCost: number;
  supplierName?: string;
  restockDate: string;
  status: string;
  createdAt?: string;
  updatedAt?: string;
  productName?: string;
  timestamp?: string;
  employeeName?: string;
}

export interface PaginationParams {
  page: number;
  size: number;
  sortBy?: string;
  sortDirection?: string;
}

export interface PaginatedResponse<T> {
  content: T[];
  totalPages: number;
  totalElements: number;
  size: number;
  number: number;
}

export interface Transaction {
  id: string;
  type: string;
  productName: string;
  quantity: number;
  totalPrice: number;
  user: string;
  date: string;
}
