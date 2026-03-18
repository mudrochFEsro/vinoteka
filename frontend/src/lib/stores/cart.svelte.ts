import { cart as cartApi, type Cart } from '$lib/api';

class CartStore {
	cart = $state.raw<Cart | null>(null);
	loading = $state(false);
	error = $state<string | null>(null);

	itemCount = $derived(this.cart?.items.reduce((sum, item) => sum + item.quantity, 0) ?? 0);

	async load() {
		this.loading = true;
		this.error = null;
		try {
			this.cart = await cartApi.get();
		} catch (e) {
			this.error = e instanceof Error ? e.message : 'Failed to load cart';
			this.cart = null;
		} finally {
			this.loading = false;
		}
	}

	async addItem(productId: number, quantity: number = 1) {
		this.loading = true;
		try {
			this.cart = await cartApi.addItem(productId, quantity);
		} catch (e) {
			this.error = e instanceof Error ? e.message : 'Failed to add item';
		} finally {
			this.loading = false;
		}
	}

	async updateItem(itemId: number, quantity: number) {
		this.loading = true;
		try {
			this.cart = await cartApi.updateItem(itemId, quantity);
		} catch (e) {
			this.error = e instanceof Error ? e.message : 'Failed to update item';
		} finally {
			this.loading = false;
		}
	}

	async removeItem(itemId: number) {
		this.loading = true;
		try {
			await cartApi.removeItem(itemId);
			await this.load();
		} catch (e) {
			this.error = e instanceof Error ? e.message : 'Failed to remove item';
		} finally {
			this.loading = false;
		}
	}

	async clear() {
		this.loading = true;
		try {
			await cartApi.clear();
			this.cart = null;
		} catch (e) {
			this.error = e instanceof Error ? e.message : 'Failed to clear cart';
		} finally {
			this.loading = false;
		}
	}
}

export const cartStore = new CartStore();
