export enum Role {
  STUDENT = "STUDENT",
  VENDOR = "VENDOR",
  ADMIN = "ADMIN",
}

export enum Vertical {
  GROCERY = "GROCERY",
  RESTAURANT = "RESTAURANT",
}

export enum StockStatus {
  IN_STOCK = "IN_STOCK",
  LOW_STOCK = "LOW_STOCK",
  UNAVAILABLE = "UNAVAILABLE",
}

export enum OrderStatus {
  PLACED = "PLACED",
  CONFIRMED = "CONFIRMED",
  PREPARING = "PREPARING",
  OUT_FOR_DELIVERY = "OUT_FOR_DELIVERY",
  DELIVERED = "DELIVERED",
  CANCELLED = "CANCELLED",
}

export enum WalletTransactionType {
  CREDIT = "CREDIT",
  DEBIT = "DEBIT",
}
