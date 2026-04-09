export type ApiResponse<T> = {
  success: boolean;
  message: string | null;
  data: T;
  fieldErrors?: Record<string, string> | null;
};

export type PaginatedResponse<T> = {
  items: T[];
  currentPage: number;
  pageSize: number;
  totalPages: number;
  totalItems: number;
  hasNext: boolean;
  hasPrevious: boolean;
};

export type Option = {
  id: number;
  value: string;
  label: string;
  active: boolean | null;
};

export type CsrfPayload = {
  headerName: string;
  parameterName: string;
  token: string;
};

export type AuthUser = {
  userId: number | null;
  username: string | null;
  email: string | null;
  fullName: string | null;
  phone: string | null;
  address: string | null;
  avatarUrl: string | null;
  provider: string | null;
  phoneVerified: boolean | null;
  emailVerified: boolean | null;
  enabled: boolean | null;
  authenticated: boolean;
  admin: boolean;
  roles: string[];
};

export type ProductCard = {
  watchId: number;
  watchName: string;
  description: string | null;
  brandName: string | null;
  categoryName: string | null;
  imageUrl: string | null;
  price: number;
  priceAfterDiscount: number;
  discountPercent: number | null;
  stockQuantity: number | null;
  soldCount: number | null;
  active: boolean | null;
};

export type ProductImage = {
  id: number;
  url: string;
  primary: boolean | null;
};

export type ProductDetail = {
  watchId: number;
  watchName: string;
  description: string | null;
  brandName: string | null;
  brandId: number | null;
  categoryName: string | null;
  categoryId: number | null;
  price: number;
  priceAfterDiscount: number;
  discountPercent: number | null;
  stockQuantity: number | null;
  soldCount: number | null;
  active: boolean | null;
  images: ProductImage[];
  relatedProducts: ProductCard[];
};

export type HomePageData = {
  bestSellers: ProductCard[];
  newestProducts: ProductCard[];
  biggestDiscounts: ProductCard[];
  brands: Option[];
  categories: Option[];
};

export type CartItem = {
  cartItemId: number;
  quantity: number;
  selected: boolean | null;
  itemTotal: number;
  watch: ProductCard;
};

export type Cart = {
  items: CartItem[];
  subtotal: number;
  shippingFee: number;
  total: number;
  totalItemCount: number;
  selectedItemCount: number;
};

export type CheckoutSummary = {
  subtotal: number;
  discountAmount: number;
  shippingFee: number;
  totalAmount: number;
  depositAmount: number;
  depositRequired: boolean;
  couponCode: string | null;
};

export type BankAccount = {
  bankAccountId: number;
  bankName: string;
  bankCode: string;
  accountNumber: string;
  accountHolder: string;
  qrImageUrl: string | null;
  active: boolean | null;
  displayOrder: number | null;
  createdAt?: string;
  updatedAt?: string;
};

export type PaymentMethod = {
  paymentMethodId: number;
  methodName: string;
  description: string | null;
  active: boolean | null;
  createdDate?: string;
  updatedDate?: string;
};

export type CheckoutContext = {
  user: AuthUser;
  cart: Cart;
  summary: CheckoutSummary;
  paymentMethods: PaymentMethod[];
  bankAccounts: BankAccount[];
};

export type OrderDetailItem = {
  orderDetailId: number;
  quantity: number;
  unitPrice: number;
  discountAmount: number;
  subtotal: number;
  watch: ProductCard;
};

export type Order = {
  orderId: number;
  orderCode: string;
  receiverName: string;
  shippingPhone: string;
  shippingAddress: string;
  orderStatus: string;
  notes: string | null;
  totalAmount: number;
  discountAmount: number;
  depositRequired: boolean | null;
  depositAmount: number | null;
  depositPaid: boolean | null;
  remainingAmount: number | null;
  orderDate: string;
  updatedDate: string;
  paymentMethod: PaymentMethod | null;
  bankAccount: BankAccount | null;
  user: AuthUser | null;
  orderDetails: OrderDetailItem[];
};

export type ChartSeries = {
  labels: string[];
  data: number[];
};

export type DashboardData = {
  revenue: number;
  orderCount: number;
  productCount: number;
  userCount: number;
  orderGrowth: number;
  orderStatsByStatus: Record<string, number>;
  orderStatsByBrand: Record<string, number>;
  revenueChart: ChartSeries;
  brandChart: ChartSeries;
  recentOrders: Order[];
};

export type Brand = {
  brandId: number;
  brandName: string;
  description: string | null;
  logoUrl: string | null;
  active: boolean | null;
  watchCount: number;
};

export type Category = {
  categoryId: number;
  categoryName: string;
  description: string | null;
  active: boolean | null;
};

export type User = {
  userId: number;
  username: string;
  email: string;
  fullName: string | null;
  phone: string | null;
  address: string | null;
  avatarUrl: string | null;
  provider: string | null;
  emailVerified: boolean | null;
  phoneVerified: boolean | null;
  enabled: boolean | null;
  banned: boolean | null;
  createdDate: string;
  updatedDate: string;
  roles: string[];
  orderCount: number;
  totalSpent: number;
};

export type PaymentTransaction = {
  transactionId: number;
  transactionCode: string | null;
  amount: number;
  status: string;
  transactionDate: string;
  responseData: string | null;
  paymentMethod: PaymentMethod | null;
  orderId: number | null;
  orderCode: string | null;
  customerName: string | null;
};

export type WatchOptionsPayload = {
  brands: Brand[];
  categories: Category[];
};

export type UserOptionsPayload = {
  roles: Option[];
};

export type AdminOrderStats = {
  totalOrders: number;
  pendingCount: number;
  shippingCount: number;
  deliveredCount: number;
  cancelledCount: number;
};

export type AdminOrderListPayload = {
  orders: PaginatedResponse<Order>;
  stats: AdminOrderStats;
};

export type AdminOrderDetailPayload = {
  order: Order;
  validStatuses: string[];
};

export type PaymentTransactionsPayload = {
  transactions: PaginatedResponse<PaymentTransaction>;
  paymentMethods: PaymentMethod[];
};
