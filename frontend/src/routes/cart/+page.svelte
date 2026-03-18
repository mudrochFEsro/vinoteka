<script lang="ts">
	import { goto } from '$app/navigation';
	import { cartStore } from '$lib/stores/cart.svelte';
	import { authStore } from '$lib/stores/auth.svelte';
	import { orders } from '$lib/api';

	let ordering = $state(false);
	let orderError = $state<string | null>(null);

	// Redirect to login if not authenticated after auth is initialized
	$effect(() => {
		if (authStore.initialized && !authStore.authenticated) {
			goto('/login');
		}
	});

	// Load cart when authenticated
	$effect(() => {
		if (authStore.authenticated && !cartStore.cart && !cartStore.loading) {
			cartStore.load();
		}
	});

	async function updateQuantity(itemId: number, quantity: number) {
		if (quantity < 1) {
			await cartStore.removeItem(itemId);
		} else {
			await cartStore.updateItem(itemId, quantity);
		}
	}

	async function placeOrder() {
		ordering = true;
		orderError = null;
		try {
			const order = await orders.create();
			await cartStore.load();
			goto(`/orders?created=${order.id}`);
		} catch (e) {
			orderError = e instanceof Error ? e.message : 'Chyba pri vytvarani objednavky';
		} finally {
			ordering = false;
		}
	}
</script>

<div>
	<h1 class="mb-6 text-2xl font-bold text-gray-900">Kosik</h1>

	{#if !authStore.initialized || (cartStore.loading && !cartStore.cart)}
		<div class="py-12 text-center">
			<p class="text-gray-500">Nacitavam...</p>
		</div>
	{:else if !authStore.authenticated}
		<div class="py-12 text-center">
			<p class="text-gray-500">Pre zobrazenie kosika sa musite prihlasit</p>
			<a href="/login" class="mt-4 inline-block text-indigo-600 hover:text-indigo-700">
				Prihlasit sa
			</a>
		</div>
	{:else if cartStore.loading}
		<div class="py-12 text-center">
			<p class="text-gray-500">Nacitavam kosik...</p>
		</div>
	{:else if cartStore.error}
		<div class="rounded-md bg-red-50 p-4">
			<p class="text-red-700">{cartStore.error}</p>
		</div>
	{:else if !cartStore.cart || cartStore.cart.items.length === 0}
		<div class="py-12 text-center">
			<p class="text-gray-500">Kosik je prazdny</p>
			<a href="/" class="mt-4 inline-block text-indigo-600 hover:text-indigo-700">
				Prejst na produkty
			</a>
		</div>
	{:else}
		<div class="overflow-hidden rounded-lg bg-white shadow">
			<table class="min-w-full divide-y divide-gray-200">
				<thead class="bg-gray-50">
					<tr>
						<th class="px-6 py-3 text-left text-xs font-medium uppercase text-gray-500">
							Produkt
						</th>
						<th class="px-6 py-3 text-left text-xs font-medium uppercase text-gray-500">Cena</th>
						<th class="px-6 py-3 text-left text-xs font-medium uppercase text-gray-500">
							Mnozstvo
						</th>
						<th class="px-6 py-3 text-left text-xs font-medium uppercase text-gray-500">Spolu</th>
						<th class="px-6 py-3"></th>
					</tr>
				</thead>
				<tbody class="divide-y divide-gray-200">
					{#each cartStore.cart.items as item (item.id)}
						<tr>
							<td class="whitespace-nowrap px-6 py-4 text-sm font-medium text-gray-900">
								{item.productName}
							</td>
							<td class="whitespace-nowrap px-6 py-4 text-sm text-gray-500">
								{item.productPrice.toFixed(2)} EUR
							</td>
							<td class="whitespace-nowrap px-6 py-4">
								<div class="flex items-center space-x-2">
									<button
										onclick={() => updateQuantity(item.id, item.quantity - 1)}
										class="rounded bg-gray-100 px-2 py-1 text-gray-700 hover:bg-gray-200"
									>
										-
									</button>
									<span class="w-8 text-center">{item.quantity}</span>
									<button
										onclick={() => updateQuantity(item.id, item.quantity + 1)}
										class="rounded bg-gray-100 px-2 py-1 text-gray-700 hover:bg-gray-200"
									>
										+
									</button>
								</div>
							</td>
							<td class="whitespace-nowrap px-6 py-4 text-sm font-medium text-gray-900">
								{item.subtotal.toFixed(2)} EUR
							</td>
							<td class="whitespace-nowrap px-6 py-4 text-right">
								<button
									onclick={() => cartStore.removeItem(item.id)}
									class="text-red-600 hover:text-red-800"
								>
									Odstranit
								</button>
							</td>
						</tr>
					{/each}
				</tbody>
			</table>
		</div>

		<div class="mt-6 flex items-center justify-between rounded-lg bg-white p-6 shadow">
			<div>
				<span class="text-lg text-gray-700">Celkova suma:</span>
				<span class="ml-2 text-2xl font-bold text-indigo-600">
					{cartStore.cart.totalPrice.toFixed(2)} EUR
				</span>
			</div>
			<button
				onclick={placeOrder}
				disabled={ordering}
				class="rounded-md bg-indigo-600 px-6 py-3 text-lg font-medium text-white hover:bg-indigo-700 disabled:opacity-50"
			>
				{ordering ? 'Objednavanie...' : 'Objednat'}
			</button>
		</div>

		{#if orderError}
			<div class="mt-4 rounded-md bg-red-50 p-4">
				<p class="text-red-700">{orderError}</p>
			</div>
		{/if}
	{/if}
</div>
