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
	<h1 class="mb-6 text-2xl font-bold text-gray-900 dark:text-white">Kosik</h1>

	{#if !authStore.initialized || (cartStore.loading && !cartStore.cart)}
		<div class="py-12 text-center">
			<p class="text-gray-500 dark:text-gray-400">Nacitavam...</p>
		</div>
	{:else if !authStore.authenticated}
		<div class="py-12 text-center">
			<p class="text-gray-500 dark:text-gray-400">Pre zobrazenie kosika sa musite prihlasit</p>
			<a href="/login" class="mt-4 inline-block text-indigo-600 hover:text-indigo-700 dark:text-indigo-400">
				Prihlasit sa
			</a>
		</div>
	{:else if cartStore.loading}
		<div class="py-12 text-center">
			<p class="text-gray-500 dark:text-gray-400">Nacitavam kosik...</p>
		</div>
	{:else if cartStore.error}
		<div class="rounded-md bg-red-50 p-4 dark:bg-red-900/20">
			<p class="text-red-700 dark:text-red-400">{cartStore.error}</p>
		</div>
	{:else if !cartStore.cart || cartStore.cart.items.length === 0}
		<div class="py-12 text-center">
			<p class="text-gray-500 dark:text-gray-400">Kosik je prazdny</p>
			<a href="/" class="mt-4 inline-block text-indigo-600 hover:text-indigo-700 dark:text-indigo-400">
				Prejst na produkty
			</a>
		</div>
	{:else}
		<div class="overflow-hidden rounded-2xl bg-white shadow dark:bg-[#1c1c1e]">
			<table class="min-w-full divide-y divide-gray-200 dark:divide-[#3a3a3c]">
				<thead class="bg-gray-50 dark:bg-[#2c2c2e]">
					<tr>
						<th class="px-6 py-3 text-left text-xs font-medium uppercase text-gray-500 dark:text-gray-400">
							Produkt
						</th>
						<th class="px-6 py-3 text-left text-xs font-medium uppercase text-gray-500 dark:text-gray-400">Cena</th>
						<th class="px-6 py-3 text-left text-xs font-medium uppercase text-gray-500 dark:text-gray-400">
							Mnozstvo
						</th>
						<th class="px-6 py-3 text-left text-xs font-medium uppercase text-gray-500 dark:text-gray-400">Spolu</th>
						<th class="px-6 py-3"></th>
					</tr>
				</thead>
				<tbody class="divide-y divide-gray-200 dark:divide-[#3a3a3c]">
					{#each cartStore.cart.items as item (item.id)}
						<tr>
							<td class="whitespace-nowrap px-6 py-4 text-sm font-medium text-gray-900 dark:text-white">
								{item.productName}
							</td>
							<td class="whitespace-nowrap px-6 py-4 text-sm text-gray-500 dark:text-gray-400">
								{item.productPrice.toFixed(2)} EUR
							</td>
							<td class="whitespace-nowrap px-6 py-4">
								<div class="flex items-center space-x-2">
									<button
										onclick={() => updateQuantity(item.id, item.quantity - 1)}
										class="rounded-lg bg-gray-100 px-2 py-1 text-gray-700 hover:bg-gray-200 dark:bg-white/10 dark:text-gray-300 dark:hover:bg-white/20"
									>
										-
									</button>
									<span class="w-8 text-center dark:text-white">{item.quantity}</span>
									<button
										onclick={() => updateQuantity(item.id, item.quantity + 1)}
										class="rounded-lg bg-gray-100 px-2 py-1 text-gray-700 hover:bg-gray-200 dark:bg-white/10 dark:text-gray-300 dark:hover:bg-white/20"
									>
										+
									</button>
								</div>
							</td>
							<td class="whitespace-nowrap px-6 py-4 text-sm font-medium text-gray-900 dark:text-white">
								{item.subtotal.toFixed(2)} EUR
							</td>
							<td class="whitespace-nowrap px-6 py-4 text-right">
								<button
									onclick={() => cartStore.removeItem(item.id)}
									class="text-red-600 hover:text-red-800 dark:text-red-400 dark:hover:text-red-300"
								>
									Odstranit
								</button>
							</td>
						</tr>
					{/each}
				</tbody>
			</table>
		</div>

		<div class="mt-6 flex items-center justify-between rounded-2xl bg-white p-6 shadow dark:bg-[#1c1c1e]">
			<div>
				<span class="text-lg text-gray-700 dark:text-gray-300">Celkova suma:</span>
				<span class="ml-2 text-2xl font-bold text-indigo-600 dark:text-indigo-400">
					{cartStore.cart.totalPrice.toFixed(2)} EUR
				</span>
			</div>
			<button
				onclick={placeOrder}
				disabled={ordering}
				class="rounded-full bg-indigo-600 px-6 py-3 text-lg font-medium text-white transition-colors hover:bg-indigo-700 disabled:opacity-50"
			>
				{ordering ? 'Objednavanie...' : 'Objednat'}
			</button>
		</div>

		{#if orderError}
			<div class="mt-4 rounded-md bg-red-50 p-4 dark:bg-red-900/20">
				<p class="text-red-700 dark:text-red-400">{orderError}</p>
			</div>
		{/if}
	{/if}
</div>
