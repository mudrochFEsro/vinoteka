import { cart as cartApi, products as productsApi, type Cart, type CartItem, type Product } from '$lib/api';
import { isAuthenticated } from '$lib/auth';

const LOCAL_CART_KEY = 'guest_cart';

export interface LocalCartItem {
	productId: number;
	productName: string;
	productPrice: number;
	quantity: number;
}

interface LocalCart {
	items: LocalCartItem[];
}

class CartStore {
	cart = $state.raw<Cart | null>(null);
	loading = $state(false);
	error = $state<string | null>(null);

	itemCount = $derived(this.cart?.items.reduce((sum, item) => sum + item.quantity, 0) ?? 0);
	totalPrice = $derived(this.cart?.totalPrice ?? 0);

	// Load cart - from server if authenticated, from localStorage if not
	async load() {
		this.loading = true;
		this.error = null;
		try {
			if (isAuthenticated()) {
				// Load server cart
				this.cart = await cartApi.get();
				// Merge any local items
				await this.mergeLocalCart();
			} else {
				// Load from localStorage
				this.cart = this.getLocalCart();
			}
		} catch (e) {
			this.error = e instanceof Error ? e.message : 'Chyba pri nacitani kosika';
			// Fallback to local cart
			this.cart = this.getLocalCart();
		} finally {
			this.loading = false;
		}
	}

	// Get cart from localStorage
	private getLocalCart(): Cart {
		if (typeof window === 'undefined') {
			return { id: 0, items: [], totalPrice: 0 };
		}

		try {
			const stored = localStorage.getItem(LOCAL_CART_KEY);
			if (stored) {
				const local: LocalCart = JSON.parse(stored);
				const items: CartItem[] = local.items.map((item, index) => ({
					id: index,
					productId: item.productId,
					productName: item.productName,
					productPrice: item.productPrice,
					quantity: item.quantity,
					subtotal: item.productPrice * item.quantity
				}));
				const totalPrice = items.reduce((sum, item) => sum + item.subtotal, 0);
				return { id: 0, items, totalPrice };
			}
		} catch {
			// Invalid stored data
		}
		return { id: 0, items: [], totalPrice: 0 };
	}

	// Save to localStorage
	private saveLocalCart(items: LocalCartItem[]) {
		if (typeof window === 'undefined') return;
		const local: LocalCart = { items };
		localStorage.setItem(LOCAL_CART_KEY, JSON.stringify(local));
	}

	// Clear localStorage cart
	clearLocalCart() {
		if (typeof window === 'undefined') return;
		localStorage.removeItem(LOCAL_CART_KEY);
	}

	// Merge local cart to server cart on login
	private async mergeLocalCart() {
		const localCart = this.getLocalCart();
		if (localCart.items.length === 0) return;

		try {
			for (const item of localCart.items) {
				await cartApi.addItem(item.productId, item.quantity);
			}
			// Reload server cart
			this.cart = await cartApi.get();
			// Clear local cart
			this.clearLocalCart();
		} catch (e) {
			console.error('Failed to merge local cart:', e);
		}
	}

	// Add item to cart
	async addItem(productId: number, quantity: number = 1, product?: Product) {
		this.loading = true;
		this.error = null;
		try {
			if (isAuthenticated()) {
				this.cart = await cartApi.addItem(productId, quantity);
			} else {
				// Add to local cart - need product info
				let productInfo = product;
				if (!productInfo) {
					productInfo = await productsApi.getById(productId);
				}

				const localCart = this.getLocalCart();
				const existingIndex = localCart.items.findIndex(i => i.productId === productId);

				const localItems: LocalCartItem[] = localCart.items.map(i => ({
					productId: i.productId,
					productName: i.productName,
					productPrice: i.productPrice,
					quantity: i.quantity
				}));

				if (existingIndex >= 0) {
					localItems[existingIndex].quantity += quantity;
				} else {
					localItems.push({
						productId,
						productName: productInfo.name,
						productPrice: productInfo.price,
						quantity
					});
				}

				this.saveLocalCart(localItems);
				this.cart = this.getLocalCart();
			}
		} catch (e) {
			this.error = e instanceof Error ? e.message : 'Chyba pri pridavani do kosika';
		} finally {
			this.loading = false;
		}
	}

	// Update item quantity
	async updateItem(itemId: number, quantity: number) {
		this.loading = true;
		this.error = null;
		try {
			if (isAuthenticated()) {
				this.cart = await cartApi.updateItem(itemId, quantity);
			} else {
				const localCart = this.getLocalCart();
				const localItems: LocalCartItem[] = localCart.items.map((item, index) => {
					if (index === itemId) {
						return { ...item, quantity };
					}
					return item;
				});
				this.saveLocalCart(localItems);
				this.cart = this.getLocalCart();
			}
		} catch (e) {
			this.error = e instanceof Error ? e.message : 'Chyba pri aktualizacii';
		} finally {
			this.loading = false;
		}
	}

	// Remove item
	async removeItem(itemId: number) {
		this.loading = true;
		this.error = null;
		try {
			if (isAuthenticated()) {
				await cartApi.removeItem(itemId);
				await this.load();
			} else {
				const localCart = this.getLocalCart();
				const localItems: LocalCartItem[] = localCart.items
					.filter((_, index) => index !== itemId)
					.map(item => ({
						productId: item.productId,
						productName: item.productName,
						productPrice: item.productPrice,
						quantity: item.quantity
					}));
				this.saveLocalCart(localItems);
				this.cart = this.getLocalCart();
			}
		} catch (e) {
			this.error = e instanceof Error ? e.message : 'Chyba pri odstranovani';
		} finally {
			this.loading = false;
		}
	}

	// Clear cart
	async clear() {
		this.loading = true;
		this.error = null;
		try {
			if (isAuthenticated()) {
				await cartApi.clear();
			}
			this.clearLocalCart();
			this.cart = { id: 0, items: [], totalPrice: 0 };
		} catch (e) {
			this.error = e instanceof Error ? e.message : 'Chyba pri mazani kosika';
		} finally {
			this.loading = false;
		}
	}

	// Get items for guest checkout
	getLocalItems(): LocalCartItem[] {
		const cart = this.getLocalCart();
		return cart.items.map(item => ({
			productId: item.productId,
			productName: item.productName,
			productPrice: item.productPrice,
			quantity: item.quantity
		}));
	}
}

export const cartStore = new CartStore();
