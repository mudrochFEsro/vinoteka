<script lang="ts">
	import { goto } from '$app/navigation';
	import { cartStore } from '$lib/stores/cart.svelte';
	import { authStore } from '$lib/stores/auth.svelte';

	// Load cart on mount
	$effect(() => {
		if (!cartStore.cart && !cartStore.loading) {
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

	function proceedToCheckout() {
		goto('/checkout');
	}
</script>

<div>
	<h1 class="mb-6 text-2xl font-bold text-gray-900 dark:text-white">Kosik</h1>

	{#if cartStore.loading && !cartStore.cart}
		<div class="py-12 text-center">
			<p class="text-gray-500 dark:text-gray-400">Nacitavam...</p>
		</div>
	{:else if cartStore.error}
		<div class="rounded-md bg-red-50 p-4 dark:bg-red-900/20">
			<p class="text-red-700 dark:text-red-400">{cartStore.error}</p>
		</div>
	{:else if !cartStore.cart || cartStore.cart.items.length === 0}
		<div class="py-12 text-center">
			<p class="text-gray-500 dark:text-gray-400">Kosik je prazdny</p>
			<a
				href="/"
				class="mt-4 inline-block text-indigo-600 hover:text-indigo-700 dark:text-indigo-400"
			>
				Prejst na produkty
			</a>
		</div>
	{:else}
		<!-- Desktop table -->
		<div class="hidden overflow-hidden rounded-2xl bg-white shadow dark:bg-[#1c1c1e] md:block">
			<table class="min-w-full divide-y divide-gray-200 dark:divide-[#3a3a3c]">
				<thead class="bg-gray-50 dark:bg-[#2c2c2e]">
					<tr>
						<th
							class="px-6 py-3 text-left text-xs font-medium uppercase text-gray-500 dark:text-gray-400"
						>
							Produkt
						</th>
						<th
							class="px-6 py-3 text-left text-xs font-medium uppercase text-gray-500 dark:text-gray-400"
						>
							Cena
						</th>
						<th
							class="px-6 py-3 text-left text-xs font-medium uppercase text-gray-500 dark:text-gray-400"
						>
							Mnozstvo
						</th>
						<th
							class="px-6 py-3 text-left text-xs font-medium uppercase text-gray-500 dark:text-gray-400"
						>
							Spolu
						</th>
						<th class="px-6 py-3"></th>
					</tr>
				</thead>
				<tbody class="divide-y divide-gray-200 dark:divide-[#3a3a3c]">
					{#each cartStore.cart.items as item (item.id)}
						<tr>
							<td
								class="whitespace-nowrap px-6 py-4 text-sm font-medium text-gray-900 dark:text-white"
							>
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
							<td
								class="whitespace-nowrap px-6 py-4 text-sm font-medium text-gray-900 dark:text-white"
							>
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

		<!-- Mobile cards -->
		<div class="space-y-4 md:hidden">
			{#each cartStore.cart.items as item (item.id)}
				<div class="rounded-2xl bg-white p-4 shadow dark:bg-[#1c1c1e]">
					<div class="flex items-start justify-between">
						<div>
							<h3 class="font-medium text-gray-900 dark:text-white">{item.productName}</h3>
							<p class="mt-1 text-sm text-gray-500 dark:text-gray-400">
								{item.productPrice.toFixed(2)} EUR / ks
							</p>
						</div>
						<button
							onclick={() => cartStore.removeItem(item.id)}
							class="text-red-600 hover:text-red-800 dark:text-red-400 dark:hover:text-red-300"
							aria-label="Odstranit"
						>
							<svg class="h-5 w-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
								<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
							</svg>
						</button>
					</div>
					<div class="mt-3 flex items-center justify-between">
						<div class="flex items-center space-x-3">
							<button
								onclick={() => updateQuantity(item.id, item.quantity - 1)}
								class="flex h-8 w-8 items-center justify-center rounded-lg bg-gray-100 text-gray-700 hover:bg-gray-200 dark:bg-white/10 dark:text-gray-300 dark:hover:bg-white/20"
							>
								-
							</button>
							<span class="w-8 text-center font-medium dark:text-white">{item.quantity}</span>
							<button
								onclick={() => updateQuantity(item.id, item.quantity + 1)}
								class="flex h-8 w-8 items-center justify-center rounded-lg bg-gray-100 text-gray-700 hover:bg-gray-200 dark:bg-white/10 dark:text-gray-300 dark:hover:bg-white/20"
							>
								+
							</button>
						</div>
						<span class="font-medium text-indigo-600 dark:text-indigo-400">
							{item.subtotal.toFixed(2)} EUR
						</span>
					</div>
				</div>
			{/each}
		</div>

		<div
			class="mt-6 flex items-center justify-between rounded-2xl bg-white p-6 shadow dark:bg-[#1c1c1e]"
		>
			<div>
				<span class="text-lg text-gray-700 dark:text-gray-300">Celkova suma:</span>
				<span class="ml-2 text-2xl font-bold text-indigo-600 dark:text-indigo-400">
					{cartStore.cart.totalPrice.toFixed(2)} EUR
				</span>
			</div>
			<button
				onclick={proceedToCheckout}
				class="rounded-full bg-indigo-600 px-6 py-3 text-lg font-medium text-white transition-colors hover:bg-indigo-700"
			>
				Pokracovat k objednavke
			</button>
		</div>

		{#if !authStore.authenticated}
			<p class="mt-4 text-center text-sm text-gray-500 dark:text-gray-400">
				Mozete nakupovat aj bez registracie
			</p>
		{/if}
	{/if}
</div>
