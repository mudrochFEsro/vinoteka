import { PUBLIC_API_URL } from '$env/static/public';
import { getToken, isAuthenticated } from './auth';

const API_BASE = PUBLIC_API_URL;

function getAuthHeaders(): HeadersInit {
	const headers: HeadersInit = {
		'Content-Type': 'application/json'
	};

	if (isAuthenticated()) {
		const token = getToken();
		if (token) {
			headers['Authorization'] = `Bearer ${token}`;
		}
	}

	return headers;
}

async function request<T>(endpoint: string, options: RequestInit = {}): Promise<T> {
	const headers = getAuthHeaders();
	const response = await fetch(`${API_BASE}${endpoint}`, {
		...options,
		headers: {
			...headers,
			...options.headers
		}
	});

	if (!response.ok) {
		// If unauthorized, clear tokens and redirect to login
		if (response.status === 401 || response.status === 403) {
			localStorage.removeItem('access_token');
			localStorage.removeItem('refresh_token');
			if (typeof window !== 'undefined') {
				window.location.href = '/login';
			}
			throw new Error('Potrebné prihlásenie');
		}

		const error = await response.text();
		throw new Error(error || `HTTP ${response.status}`);
	}

	if (response.status === 204) {
		return undefined as T;
	}

	return response.json();
}

// Products
export interface Product {
	id: number;
	name: string;
	description: string;
	price: number;
	stock: number;
	imageUrl: string | null;
	categoryId: number;
	categoryName: string;
}

export interface Category {
	id: number;
	name: string;
	slug: string;
	productCount: number;
}

export const products = {
	getAll: () => request<Product[]>('/products'),
	getById: (id: number) => request<Product>(`/products/${id}`),
	getByCategory: (categoryId: number) => request<Product[]>(`/products?categoryId=${categoryId}`),
	create: (data: Omit<Product, 'id' | 'categoryName'>) =>
		request<Product>('/products', { method: 'POST', body: JSON.stringify(data) }),
	update: (id: number, data: Partial<Product>) =>
		request<Product>(`/products/${id}`, { method: 'PUT', body: JSON.stringify(data) }),
	delete: (id: number) => request<void>(`/products/${id}`, { method: 'DELETE' })
};

// Categories
export const categories = {
	getAll: () => request<Category[]>('/categories'),
	create: (data: Omit<Category, 'id' | 'productCount'>) =>
		request<Category>('/categories', { method: 'POST', body: JSON.stringify(data) })
};

// Cart
export interface CartItem {
	id: number;
	productId: number;
	productName: string;
	productPrice: number;
	quantity: number;
	subtotal: number;
}

export interface Cart {
	id: number;
	items: CartItem[];
	totalPrice: number;
}

export const cart = {
	get: () => request<Cart>('/cart'),
	addItem: (productId: number, quantity: number) =>
		request<Cart>('/cart/items', { method: 'POST', body: JSON.stringify({ productId, quantity }) }),
	updateItem: (itemId: number, quantity: number) =>
		request<Cart>(`/cart/items/${itemId}`, { method: 'PUT', body: JSON.stringify({ quantity }) }),
	removeItem: (itemId: number) => request<void>(`/cart/items/${itemId}`, { method: 'DELETE' }),
	clear: () => request<void>('/cart', { method: 'DELETE' })
};

// Orders
export interface OrderItem {
	id: number;
	productId: number;
	productName: string;
	quantity: number;
	priceAtPurchase: number;
	subtotal: number;
}

export interface OrderSummary {
	id: number;
	status: 'PENDING' | 'CONFIRMED' | 'SHIPPED' | 'DELIVERED' | 'CANCELLED';
	totalPrice: number;
	itemCount: number;
	createdAt: string;
}

export interface Order {
	id: number;
	userId: number;
	userEmail: string;
	status: 'PENDING' | 'CONFIRMED' | 'SHIPPED' | 'DELIVERED' | 'CANCELLED';
	totalPrice: number;
	items: OrderItem[];
	createdAt: string;
}

export const orders = {
	getAll: () => request<OrderSummary[]>('/orders'),
	getById: (id: number) => request<Order>(`/orders/${id}`),
	create: () => request<Order>('/orders', { method: 'POST' }),
	cancel: (id: number) => request<Order>(`/orders/${id}/cancel`, { method: 'PUT' })
};

// Admin Orders
export const adminOrders = {
	getAll: () => request<Order[]>('/admin/orders'),
	updateStatus: (id: number, status: Order['status']) =>
		request<Order>(`/admin/orders/${id}/status`, {
			method: 'PUT',
			body: JSON.stringify({ status })
		})
};

// File Upload
export interface UploadResponse {
	filename: string;
	url: string;
}

export const files = {
	upload: async (file: File): Promise<UploadResponse> => {
		const formData = new FormData();
		formData.append('file', file);

		const token = getToken();
		const response = await fetch(`${API_BASE}/files/upload`, {
			method: 'POST',
			headers: token ? { Authorization: `Bearer ${token}` } : {},
			body: formData
		});

		if (!response.ok) {
			const error = await response.text();
			throw new Error(error || `HTTP ${response.status}`);
		}

		return response.json();
	}
};

// Admin Products
export interface CreateProductData {
	name: string;
	description: string;
	price: number;
	stock: number;
	imageUrl?: string | null;
	categoryId: number;
}

export const adminProducts = {
	create: (data: CreateProductData) =>
		request<Product>('/products', { method: 'POST', body: JSON.stringify(data) }),
	update: (id: number, data: Partial<CreateProductData>) =>
		request<Product>(`/products/${id}`, { method: 'PUT', body: JSON.stringify(data) }),
	delete: (id: number) => request<void>(`/products/${id}`, { method: 'DELETE' })
};
