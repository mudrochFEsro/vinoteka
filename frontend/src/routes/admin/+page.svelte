<script lang="ts">
	import { onMount } from 'svelte';
	import { goto } from '$app/navigation';
	import {
		products as productsApi,
		categories as categoriesApi,
		adminOrders,
		adminProducts,
		files,
		type Product,
		type Category,
		type Order
	} from '$lib/api';
	import { authStore } from '$lib/stores/auth.svelte';
	import { PUBLIC_API_URL } from '$env/static/public';

	let products = $state.raw<Product[]>([]);
	let categories = $state.raw<Category[]>([]);
	let orders = $state.raw<Order[]>([]);
	let loading = $state(true);
	let activeTab = $state<'products' | 'orders'>('products');

	// Product form
	let showForm = $state(false);
	let editingProduct = $state<Product | null>(null);
	let productForm = $state({
		name: '',
		description: '',
		price: 0,
		stock: 0,
		imageUrl: '' as string | null,
		categoryId: 0
	});
	let saving = $state(false);
	let uploading = $state(false);
	let imagePreview = $state<string | null>(null);

	const statusLabels: Record<Order['status'], string> = {
		PENDING: 'Caka',
		CONFIRMED: 'Potvrdena',
		SHIPPED: 'Odoslana',
		DELIVERED: 'Dorucena',
		CANCELLED: 'Zrusena'
	};

	onMount(() => {
		// Wait for auth to initialize
		const checkAuth = () => {
			if (!authStore.initialized) {
				setTimeout(checkAuth, 50);
				return;
			}

			if (!authStore.authenticated || !authStore.admin) {
				goto('/');
				return;
			}

			loadData();
		};
		checkAuth();
	});

	async function loadData() {
		try {
			// Load independently so one failure doesn't block others
			const [productsResult, categoriesResult, ordersResult] = await Promise.allSettled([
				productsApi.getAll(),
				categoriesApi.getAll(),
				adminOrders.getAll()
			]);

			if (productsResult.status === 'fulfilled') {
				products = productsResult.value;
			}
			if (categoriesResult.status === 'fulfilled') {
				categories = categoriesResult.value;
			}
			if (ordersResult.status === 'fulfilled') {
				orders = ordersResult.value;
			} else {
				console.error('Failed to load orders:', ordersResult.reason);
			}
		} catch (e) {
			console.error('Failed to load admin data:', e);
		} finally {
			loading = false;
		}
	}

	function editProduct(product: Product) {
		editingProduct = product;
		productForm = {
			name: product.name,
			description: product.description,
			price: product.price,
			stock: product.stock,
			imageUrl: product.imageUrl,
			categoryId: product.categoryId
		};
		imagePreview = product.imageUrl;
		showForm = true;
	}

	function newProduct() {
		editingProduct = null;
		productForm = {
			name: '',
			description: '',
			price: 0,
			stock: 0,
			imageUrl: null,
			categoryId: categories[0]?.id ?? 0
		};
		imagePreview = null;
		showForm = true;
	}

	function cancelForm() {
		showForm = false;
		editingProduct = null;
		productForm = { name: '', description: '', price: 0, stock: 0, imageUrl: null, categoryId: 0 };
		imagePreview = null;
	}

	async function handleImageUpload(event: Event) {
		const input = event.target as HTMLInputElement;
		const file = input.files?.[0];
		if (!file) return;

		// Validate file type
		if (!file.type.startsWith('image/')) {
			alert('Prosim vyberte obrazok (JPG, PNG, GIF, WebP)');
			return;
		}

		// Validate file size (max 5MB)
		if (file.size > 5 * 1024 * 1024) {
			alert('Obrazok je prilis velky. Maximalna velkost je 5MB.');
			return;
		}

		uploading = true;
		try {
			const result = await files.upload(file);
			productForm.imageUrl = `${PUBLIC_API_URL}/files/${result.filename}`;
			imagePreview = productForm.imageUrl;
		} catch (e) {
			console.error('Failed to upload image:', e);
			alert('Nepodarilo sa nahrat obrazok');
		} finally {
			uploading = false;
		}
	}

	function removeImage() {
		productForm.imageUrl = null;
		imagePreview = null;
	}

	async function saveProduct() {
		if (!productForm.name.trim()) {
			alert('Nazov produktu je povinny');
			return;
		}
		if (productForm.price <= 0) {
			alert('Cena musi byt vacsia ako 0');
			return;
		}
		if (!productForm.categoryId) {
			alert('Vyberte kategoriu');
			return;
		}

		saving = true;
		try {
			const data = {
				name: productForm.name,
				description: productForm.description,
				price: productForm.price,
				stock: productForm.stock,
				imageUrl: productForm.imageUrl,
				categoryId: productForm.categoryId
			};

			if (editingProduct) {
				await adminProducts.update(editingProduct.id, data);
			} else {
				await adminProducts.create(data);
			}
			products = await productsApi.getAll();
			cancelForm();
		} catch (e) {
			console.error('Failed to save product:', e);
			alert('Nepodarilo sa ulozit produkt');
		} finally {
			saving = false;
		}
	}

	async function deleteProduct(id: number) {
		if (!confirm('Naozaj chcete zmazat tento produkt?')) return;
		try {
			await adminProducts.delete(id);
			products = products.filter((p) => p.id !== id);
		} catch (e) {
			console.error('Failed to delete product:', e);
			alert('Nepodarilo sa zmazat produkt');
		}
	}

	async function updateOrderStatus(orderId: number, status: Order['status']) {
		try {
			await adminOrders.updateStatus(orderId, status);
			orders = await adminOrders.getAll();
		} catch (e) {
			console.error('Failed to update order status:', e);
		}
	}
</script>

<div>
	<h1 class="mb-6 text-2xl font-bold text-gray-900 dark:text-white">Administracia</h1>

	{#if !authStore.admin}
		<div class="rounded-md bg-red-50 p-4 dark:bg-red-900/20">
			<p class="text-red-700 dark:text-red-400">Nemate opravnenie na pristup k tejto stranke</p>
		</div>
	{:else if loading}
		<div class="py-12 text-center">
			<p class="text-gray-500 dark:text-gray-400">Nacitavam...</p>
		</div>
	{:else}
		<div class="mb-6 border-b border-gray-200 dark:border-[#3a3a3c]">
			<nav class="-mb-px flex space-x-8">
				<button
					onclick={() => (activeTab = 'products')}
					class="border-b-2 px-1 py-4 text-sm font-medium {activeTab === 'products'
						? 'border-indigo-500 text-indigo-600 dark:text-indigo-400'
						: 'border-transparent text-gray-500 hover:border-gray-300 hover:text-gray-700 dark:text-gray-400 dark:hover:text-gray-300'}"
				>
					Produkty ({products.length})
				</button>
				<button
					onclick={() => (activeTab = 'orders')}
					class="border-b-2 px-1 py-4 text-sm font-medium {activeTab === 'orders'
						? 'border-indigo-500 text-indigo-600 dark:text-indigo-400'
						: 'border-transparent text-gray-500 hover:border-gray-300 hover:text-gray-700 dark:text-gray-400 dark:hover:text-gray-300'}"
				>
					Objednavky ({orders.length})
				</button>
			</nav>
		</div>

		{#if activeTab === 'products'}
			<div class="mb-6">
				<button
					onclick={newProduct}
					class="rounded-md bg-indigo-600 px-4 py-2 text-sm font-medium text-white hover:bg-indigo-700"
				>
					+ Novy produkt
				</button>
			</div>

			{#if showForm}
				<div class="mb-6 rounded-lg bg-white p-6 shadow dark:bg-[#1c1c1e]">
					<h3 class="mb-4 text-lg font-medium text-gray-900 dark:text-white">
						{editingProduct ? 'Upravit produkt' : 'Novy produkt'}
					</h3>
					<div class="grid grid-cols-1 gap-4 md:grid-cols-2">
						<div>
							<label class="block text-sm font-medium text-gray-700 dark:text-gray-300">Nazov *</label>
							<input
								type="text"
								bind:value={productForm.name}
								class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 dark:border-[#3a3a3c] dark:bg-[#2c2c2e] dark:text-white"
								placeholder="Napr. Frankovka modra 2021"
							/>
						</div>
						<div>
							<label class="block text-sm font-medium text-gray-700 dark:text-gray-300">Kategoria *</label>
							<select
								bind:value={productForm.categoryId}
								class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 dark:border-[#3a3a3c] dark:bg-[#2c2c2e] dark:text-white"
							>
								<option value={0}>-- Vyberte kategoriu --</option>
								{#each categories as category}
									<option value={category.id}>{category.name}</option>
								{/each}
							</select>
						</div>
						<div class="md:col-span-2">
							<label class="block text-sm font-medium text-gray-700 dark:text-gray-300">Popis</label>
							<textarea
								bind:value={productForm.description}
								rows="3"
								class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 dark:border-[#3a3a3c] dark:bg-[#2c2c2e] dark:text-white"
								placeholder="Popis produktu..."
							></textarea>
						</div>
						<div>
							<label class="block text-sm font-medium text-gray-700 dark:text-gray-300">Cena (EUR) *</label>
							<input
								type="number"
								step="0.01"
								min="0"
								bind:value={productForm.price}
								class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 dark:border-[#3a3a3c] dark:bg-[#2c2c2e] dark:text-white"
							/>
						</div>
						<div>
							<label class="block text-sm font-medium text-gray-700 dark:text-gray-300">Sklad (ks)</label>
							<input
								type="number"
								min="0"
								bind:value={productForm.stock}
								class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 dark:border-[#3a3a3c] dark:bg-[#2c2c2e] dark:text-white"
							/>
						</div>
						<div class="md:col-span-2">
							<label class="block text-sm font-medium text-gray-700 dark:text-gray-300">Obrazok</label>
							<div class="mt-2 flex items-start gap-4">
								{#if imagePreview}
									<div class="relative">
										<img
											src={imagePreview}
											alt="Preview"
											class="h-32 w-32 rounded-lg object-cover"
										/>
										<button
											onclick={removeImage}
											class="absolute -top-2 -right-2 rounded-full bg-red-500 p-1 text-white hover:bg-red-600"
										>
											<svg class="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
												<path
													stroke-linecap="round"
													stroke-linejoin="round"
													stroke-width="2"
													d="M6 18L18 6M6 6l12 12"
												/>
											</svg>
										</button>
									</div>
								{/if}
								<div class="flex-1">
									<label
										class="flex cursor-pointer flex-col items-center justify-center rounded-lg border-2 border-dashed border-gray-300 bg-gray-50 p-4 hover:bg-gray-100 dark:border-[#3a3a3c] dark:bg-[#2c2c2e] dark:hover:bg-[#3a3a3c]"
									>
										{#if uploading}
											<span class="text-sm text-gray-500 dark:text-gray-400">Nahravam...</span>
										{:else}
											<svg
												class="mb-2 h-8 w-8 text-gray-400"
												fill="none"
												stroke="currentColor"
												viewBox="0 0 24 24"
											>
												<path
													stroke-linecap="round"
													stroke-linejoin="round"
													stroke-width="2"
													d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z"
												/>
											</svg>
											<span class="text-sm text-gray-500 dark:text-gray-400">Kliknite pre nahratie obrazka</span>
											<span class="mt-1 text-xs text-gray-400">JPG, PNG, GIF, WebP (max 5MB)</span>
										{/if}
										<input
											type="file"
											accept="image/*"
											class="hidden"
											onchange={handleImageUpload}
											disabled={uploading}
										/>
									</label>
								</div>
							</div>
						</div>
					</div>
					<div class="mt-6 flex justify-end space-x-2">
						<button
							onclick={cancelForm}
							class="rounded-md bg-gray-100 px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-200 dark:bg-[#3a3a3c] dark:text-gray-300 dark:hover:bg-[#4a4a4c]"
						>
							Zrusit
						</button>
						<button
							onclick={saveProduct}
							disabled={saving || uploading}
							class="rounded-md bg-indigo-600 px-4 py-2 text-sm font-medium text-white hover:bg-indigo-700 disabled:opacity-50"
						>
							{saving ? 'Ukladam...' : 'Ulozit produkt'}
						</button>
					</div>
				</div>
			{/if}

			<div class="overflow-hidden rounded-lg bg-white shadow dark:bg-[#1c1c1e]">
				<table class="min-w-full divide-y divide-gray-200 dark:divide-[#3a3a3c]">
					<thead class="bg-gray-50 dark:bg-[#2c2c2e]">
						<tr>
							<th class="px-6 py-3 text-left text-xs font-medium uppercase text-gray-500 dark:text-gray-400">
								Obrazok
							</th>
							<th class="px-6 py-3 text-left text-xs font-medium uppercase text-gray-500 dark:text-gray-400">Nazov</th>
							<th class="px-6 py-3 text-left text-xs font-medium uppercase text-gray-500 dark:text-gray-400">
								Kategoria
							</th>
							<th class="px-6 py-3 text-left text-xs font-medium uppercase text-gray-500 dark:text-gray-400">Cena</th>
							<th class="px-6 py-3 text-left text-xs font-medium uppercase text-gray-500 dark:text-gray-400">Sklad</th>
							<th class="px-6 py-3"></th>
						</tr>
					</thead>
					<tbody class="divide-y divide-gray-200 dark:divide-[#3a3a3c]">
						{#each products as product (product.id)}
							<tr class="hover:bg-gray-50 dark:hover:bg-[#2c2c2e]">
								<td class="whitespace-nowrap px-6 py-4">
									{#if product.imageUrl}
										<img
											src={product.imageUrl}
											alt={product.name}
											class="h-12 w-12 rounded-lg object-cover"
										/>
									{:else}
										<div
											class="flex h-12 w-12 items-center justify-center rounded-lg bg-gray-200 text-xs text-gray-400 dark:bg-[#3a3a3c]"
										>
											N/A
										</div>
									{/if}
								</td>
								<td class="whitespace-nowrap px-6 py-4">
									<div class="text-sm font-medium text-gray-900 dark:text-white">{product.name}</div>
									<div class="max-w-xs truncate text-xs text-gray-500 dark:text-gray-400">{product.description}</div>
								</td>
								<td class="whitespace-nowrap px-6 py-4 text-sm text-gray-500 dark:text-gray-400">
									{product.categoryName}
								</td>
								<td class="whitespace-nowrap px-6 py-4 text-sm font-medium text-gray-900 dark:text-white">
									{product.price.toFixed(2)} EUR
								</td>
								<td class="whitespace-nowrap px-6 py-4 text-sm">
									<span
										class="rounded-full px-2 py-1 text-xs font-medium {product.stock === 0
											? 'bg-red-100 text-red-800 dark:bg-red-900/30 dark:text-red-400'
											: product.stock <= 5
												? 'bg-yellow-100 text-yellow-800 dark:bg-yellow-900/30 dark:text-yellow-400'
												: 'bg-green-100 text-green-800 dark:bg-green-900/30 dark:text-green-400'}"
									>
										{product.stock} ks
									</span>
								</td>
								<td class="whitespace-nowrap px-6 py-4 text-right text-sm">
									<button
										onclick={() => editProduct(product)}
										class="mr-3 text-indigo-600 hover:text-indigo-800 dark:text-indigo-400 dark:hover:text-indigo-300"
									>
										Upravit
									</button>
									<button
										onclick={() => deleteProduct(product.id)}
										class="text-red-600 hover:text-red-800 dark:text-red-400 dark:hover:text-red-300"
									>
										Zmazat
									</button>
								</td>
							</tr>
						{/each}
					</tbody>
				</table>
			</div>
		{:else}
			<div class="overflow-hidden rounded-lg bg-white shadow dark:bg-[#1c1c1e]">
				<table class="min-w-full divide-y divide-gray-200 dark:divide-[#3a3a3c]">
					<thead class="bg-gray-50 dark:bg-[#2c2c2e]">
						<tr>
							<th class="px-6 py-3 text-left text-xs font-medium uppercase text-gray-500 dark:text-gray-400">ID</th>
							<th class="px-6 py-3 text-left text-xs font-medium uppercase text-gray-500 dark:text-gray-400">Datum</th>
							<th class="px-6 py-3 text-left text-xs font-medium uppercase text-gray-500 dark:text-gray-400">
								Polozky
							</th>
							<th class="px-6 py-3 text-left text-xs font-medium uppercase text-gray-500 dark:text-gray-400">Suma</th>
							<th class="px-6 py-3 text-left text-xs font-medium uppercase text-gray-500 dark:text-gray-400">Status</th>
						</tr>
					</thead>
					<tbody class="divide-y divide-gray-200 dark:divide-[#3a3a3c]">
						{#each orders as order (order.id)}
							<tr class="hover:bg-gray-50 dark:hover:bg-[#2c2c2e]">
								<td class="whitespace-nowrap px-6 py-4 text-sm text-gray-500 dark:text-gray-400">#{order.id}</td>
								<td class="whitespace-nowrap px-6 py-4 text-sm text-gray-500 dark:text-gray-400">
									{new Date(order.createdAt).toLocaleDateString('sk-SK')}
								</td>
								<td class="px-6 py-4 text-sm text-gray-500 dark:text-gray-400">
									{order.items.map((i) => `${i.productName} (${i.quantity}x)`).join(', ')}
								</td>
								<td class="whitespace-nowrap px-6 py-4 text-sm font-medium text-gray-900 dark:text-white">
									{order.totalPrice.toFixed(2)} EUR
								</td>
								<td class="whitespace-nowrap px-6 py-4">
									<select
										value={order.status}
										onchange={(e) =>
											updateOrderStatus(order.id, e.currentTarget.value as Order['status'])}
										class="rounded-md border-gray-300 text-sm shadow-sm focus:border-indigo-500 focus:ring-indigo-500 dark:border-[#3a3a3c] dark:bg-[#2c2c2e] dark:text-white"
									>
										{#each Object.entries(statusLabels) as [value, label]}
											<option {value}>{label}</option>
										{/each}
									</select>
								</td>
							</tr>
						{/each}
					</tbody>
				</table>
			</div>
		{/if}
	{/if}
</div>
