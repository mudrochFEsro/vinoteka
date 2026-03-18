import { describe, it, expect, vi, beforeEach } from 'vitest';

// Mock environment and auth modules
vi.mock('$env/static/public', () => ({
	PUBLIC_API_URL: 'http://localhost:8081/api'
}));

vi.mock('./auth', () => ({
	isAuthenticated: vi.fn(() => false),
	getToken: vi.fn(() => null)
}));

const apiModule = await import('./api');
const { products, categories, cart } = apiModule;
const { isAuthenticated, getToken } = await import('./auth');

describe('API Module', () => {
	beforeEach(() => {
		vi.clearAllMocks();
	});

	describe('products', () => {
		it('getAll should fetch all products', async () => {
			const mockProducts = [
				{ id: 1, name: 'Wine 1', price: 10.99 },
				{ id: 2, name: 'Wine 2', price: 15.99 }
			];

			vi.mocked(fetch).mockResolvedValueOnce({
				ok: true,
				status: 200,
				json: () => Promise.resolve(mockProducts)
			} as Response);

			const result = await products.getAll();

			expect(fetch).toHaveBeenCalledWith('http://localhost:8081/api/products', expect.any(Object));
			expect(result).toEqual(mockProducts);
		});

		it('getById should fetch single product', async () => {
			const mockProduct = { id: 1, name: 'Wine 1', price: 10.99 };

			vi.mocked(fetch).mockResolvedValueOnce({
				ok: true,
				status: 200,
				json: () => Promise.resolve(mockProduct)
			} as Response);

			const result = await products.getById(1);

			expect(fetch).toHaveBeenCalledWith(
				'http://localhost:8081/api/products/1',
				expect.any(Object)
			);
			expect(result).toEqual(mockProduct);
		});

		it('should throw error on failed request', async () => {
			vi.mocked(fetch).mockResolvedValueOnce({
				ok: false,
				status: 404,
				text: () => Promise.resolve('Product not found')
			} as Response);

			await expect(products.getById(999)).rejects.toThrow('Product not found');
		});
	});

	describe('categories', () => {
		it('getAll should fetch all categories', async () => {
			const mockCategories = [
				{ id: 1, name: 'Red Wine', slug: 'red-wine' },
				{ id: 2, name: 'White Wine', slug: 'white-wine' }
			];

			vi.mocked(fetch).mockResolvedValueOnce({
				ok: true,
				status: 200,
				json: () => Promise.resolve(mockCategories)
			} as Response);

			const result = await categories.getAll();

			expect(fetch).toHaveBeenCalledWith(
				'http://localhost:8081/api/categories',
				expect.any(Object)
			);
			expect(result).toEqual(mockCategories);
		});
	});

	describe('cart', () => {
		beforeEach(() => {
			// Set up authenticated state for cart tests
			vi.mocked(isAuthenticated).mockReturnValue(true);
			vi.mocked(getToken).mockReturnValue('mock-token');
		});

		it('get should fetch cart with auth header', async () => {
			const mockCart = { id: 1, items: [], total: 0 };

			vi.mocked(fetch).mockResolvedValueOnce({
				ok: true,
				status: 200,
				json: () => Promise.resolve(mockCart)
			} as Response);

			const result = await cart.get();

			expect(fetch).toHaveBeenCalledWith(
				'http://localhost:8081/api/cart',
				expect.objectContaining({
					headers: expect.objectContaining({
						Authorization: 'Bearer mock-token'
					})
				})
			);
			expect(result).toEqual(mockCart);
		});

		it('addItem should send POST request', async () => {
			const mockCart = { id: 1, items: [{ productId: 1, quantity: 2 }], total: 20 };

			vi.mocked(fetch).mockResolvedValueOnce({
				ok: true,
				status: 200,
				json: () => Promise.resolve(mockCart)
			} as Response);

			const result = await cart.addItem(1, 2);

			expect(fetch).toHaveBeenCalledWith(
				'http://localhost:8081/api/cart/items',
				expect.objectContaining({
					method: 'POST',
					body: JSON.stringify({ productId: 1, quantity: 2 })
				})
			);
			expect(result).toEqual(mockCart);
		});

		it('should redirect to login on 401', async () => {
			// Mock window.location
			const originalLocation = window.location;
			delete (window as unknown as { location?: Location }).location;
			window.location = { href: '' } as Location;

			vi.mocked(fetch).mockResolvedValueOnce({
				ok: false,
				status: 401,
				text: () => Promise.resolve('Unauthorized')
			} as Response);

			await expect(cart.get()).rejects.toThrow('Potrebné prihlásenie');
			expect(window.location.href).toBe('/login');

			// Restore
			window.location = originalLocation;
		});
	});
});
