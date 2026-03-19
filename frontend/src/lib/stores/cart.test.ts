import { describe, it, expect, beforeEach, vi } from 'vitest';

// Mock localStorage
const localStorageMock = (() => {
	let store: Record<string, string> = {};
	return {
		getItem: (key: string) => store[key] || null,
		setItem: (key: string, value: string) => {
			store[key] = value;
		},
		removeItem: (key: string) => {
			delete store[key];
		},
		clear: () => {
			store = {};
		}
	};
})();

Object.defineProperty(global, 'localStorage', { value: localStorageMock });

// Mock auth
vi.mock('$lib/auth', () => ({
	isAuthenticated: vi.fn(() => false),
	getToken: vi.fn(() => null)
}));

// Mock API
vi.mock('$lib/api', () => ({
	cart: {
		get: vi.fn(),
		addItem: vi.fn(),
		updateItem: vi.fn(),
		removeItem: vi.fn(),
		clear: vi.fn()
	},
	products: {
		getById: vi.fn()
	}
}));

describe('CartStore - Guest Cart', () => {
	beforeEach(() => {
		localStorageMock.clear();
		vi.clearAllMocks();
	});

	it('should start with empty cart', async () => {
		const { cartStore } = await import('./cart.svelte');
		await cartStore.load();

		expect(cartStore.cart).not.toBeNull();
		expect(cartStore.cart?.items.length).toBe(0);
		expect(cartStore.itemCount).toBe(0);
	});

	it('should add item to guest cart', async () => {
		const { cartStore } = await import('./cart.svelte');
		const { products } = await import('$lib/api');

		vi.mocked(products.getById).mockResolvedValue({
			id: 1,
			name: 'Test Wine',
			description: 'A test wine',
			price: 15.99,
			stock: 10,
			imageUrl: null,
			categoryId: 1,
			categoryName: 'Red'
		});

		await cartStore.addItem(1, 2);

		expect(cartStore.cart?.items.length).toBe(1);
		expect(cartStore.cart?.items[0].productName).toBe('Test Wine');
		expect(cartStore.cart?.items[0].quantity).toBe(2);
		expect(cartStore.itemCount).toBe(2);
	});

	it('should persist guest cart in localStorage', async () => {
		const { cartStore } = await import('./cart.svelte');
		const { products } = await import('$lib/api');

		vi.mocked(products.getById).mockResolvedValue({
			id: 1,
			name: 'Test Wine',
			description: 'A test wine',
			price: 15.99,
			stock: 10,
			imageUrl: null,
			categoryId: 1,
			categoryName: 'Red'
		});

		await cartStore.addItem(1, 1);

		const stored = localStorage.getItem('guest_cart');
		expect(stored).not.toBeNull();

		const parsed = JSON.parse(stored!);
		expect(parsed.items.length).toBe(1);
		expect(parsed.items[0].productId).toBe(1);
	});

	it('should update item quantity in guest cart', async () => {
		const { cartStore } = await import('./cart.svelte');
		const { products } = await import('$lib/api');

		vi.mocked(products.getById).mockResolvedValue({
			id: 1,
			name: 'Test Wine',
			description: 'A test wine',
			price: 15.99,
			stock: 10,
			imageUrl: null,
			categoryId: 1,
			categoryName: 'Red'
		});

		await cartStore.addItem(1, 1);
		await cartStore.updateItem(0, 5);

		expect(cartStore.cart?.items[0].quantity).toBe(5);
		expect(cartStore.itemCount).toBe(5);
	});

	it('should remove item from guest cart', async () => {
		const { cartStore } = await import('./cart.svelte');
		const { products } = await import('$lib/api');

		vi.mocked(products.getById).mockResolvedValue({
			id: 1,
			name: 'Test Wine',
			description: 'A test wine',
			price: 15.99,
			stock: 10,
			imageUrl: null,
			categoryId: 1,
			categoryName: 'Red'
		});

		await cartStore.addItem(1, 1);
		expect(cartStore.cart?.items.length).toBe(1);

		await cartStore.removeItem(0);
		expect(cartStore.cart?.items.length).toBe(0);
	});

	it('should clear guest cart', async () => {
		const { cartStore } = await import('./cart.svelte');
		const { products } = await import('$lib/api');

		vi.mocked(products.getById).mockResolvedValue({
			id: 1,
			name: 'Test Wine',
			description: 'A test wine',
			price: 15.99,
			stock: 10,
			imageUrl: null,
			categoryId: 1,
			categoryName: 'Red'
		});

		await cartStore.addItem(1, 2);
		expect(cartStore.itemCount).toBe(2);

		await cartStore.clear();
		expect(cartStore.itemCount).toBe(0);
		expect(localStorage.getItem('guest_cart')).toBeNull();
	});

	it('should calculate total price correctly', async () => {
		const { cartStore } = await import('./cart.svelte');
		const { products } = await import('$lib/api');

		vi.mocked(products.getById).mockResolvedValue({
			id: 1,
			name: 'Test Wine',
			description: 'A test wine',
			price: 10.0,
			stock: 10,
			imageUrl: null,
			categoryId: 1,
			categoryName: 'Red'
		});

		await cartStore.addItem(1, 3);

		expect(cartStore.cart?.totalPrice).toBe(30.0);
	});

	it('should increase quantity when adding same product', async () => {
		const { cartStore } = await import('./cart.svelte');
		const { products } = await import('$lib/api');

		vi.mocked(products.getById).mockResolvedValue({
			id: 1,
			name: 'Test Wine',
			description: 'A test wine',
			price: 10.0,
			stock: 10,
			imageUrl: null,
			categoryId: 1,
			categoryName: 'Red'
		});

		await cartStore.addItem(1, 2);
		await cartStore.addItem(1, 3);

		expect(cartStore.cart?.items.length).toBe(1);
		expect(cartStore.cart?.items[0].quantity).toBe(5);
	});

	it('should get local items for guest checkout', async () => {
		const { cartStore } = await import('./cart.svelte');
		const { products } = await import('$lib/api');

		vi.mocked(products.getById).mockResolvedValue({
			id: 1,
			name: 'Test Wine',
			description: 'A test wine',
			price: 15.99,
			stock: 10,
			imageUrl: null,
			categoryId: 1,
			categoryName: 'Red'
		});

		await cartStore.addItem(1, 2);

		const items = cartStore.getLocalItems();
		expect(items.length).toBe(1);
		expect(items[0].productId).toBe(1);
		expect(items[0].quantity).toBe(2);
	});
});
