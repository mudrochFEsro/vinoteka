<script lang="ts">
	import { goto } from '$app/navigation';
	import { page } from '$app/state';
	import { orders as ordersApi, type OrderSummary } from '$lib/api';
	import { authStore } from '$lib/stores/auth.svelte';

	let allOrders = $state.raw<OrderSummary[]>([]);
	let loading = $state(true);
	let error = $state<string | null>(null);

	let createdOrderId = $derived(page.url.searchParams.get('created'));

	const statusLabels: Record<OrderSummary['status'], string> = {
		PENDING: 'Caka na spracovanie',
		CONFIRMED: 'Potvrdena',
		SHIPPED: 'Odoslana',
		DELIVERED: 'Dorucena',
		CANCELLED: 'Zrusena'
	};

	const statusColors: Record<OrderSummary['status'], string> = {
		PENDING: 'bg-yellow-100 text-yellow-800',
		CONFIRMED: 'bg-blue-100 text-blue-800',
		SHIPPED: 'bg-purple-100 text-purple-800',
		DELIVERED: 'bg-green-100 text-green-800',
		CANCELLED: 'bg-red-100 text-red-800'
	};

	// Redirect to login if not authenticated
	$effect(() => {
		if (authStore.initialized && !authStore.authenticated) {
			goto('/login');
		}
	});

	// Load orders when authenticated
	$effect(() => {
		if (authStore.authenticated && loading) {
			loadOrders();
		}
	});

	async function loadOrders() {
		try {
			allOrders = await ordersApi.getAll();
		} catch (e) {
			error = e instanceof Error ? e.message : 'Chyba pri nacitani objednavok';
		} finally {
			loading = false;
		}
	}

	function formatDate(dateString: string) {
		return new Date(dateString).toLocaleDateString('sk-SK', {
			day: '2-digit',
			month: '2-digit',
			year: 'numeric',
			hour: '2-digit',
			minute: '2-digit'
		});
	}
</script>

<div>
	<h1 class="mb-6 text-2xl font-bold text-gray-900">Moje objednavky</h1>

	{#if createdOrderId}
		<div class="mb-6 rounded-md bg-green-50 p-4">
			<p class="text-green-700">Objednavka #{createdOrderId} bola uspesne vytvorena!</p>
		</div>
	{/if}

	{#if !authStore.initialized || loading}
		<div class="py-12 text-center">
			<p class="text-gray-500">Nacitavam...</p>
		</div>
	{:else if !authStore.authenticated}
		<div class="py-12 text-center">
			<p class="text-gray-500">Pre zobrazenie objednavok sa musite prihlasit</p>
			<a href="/login" class="mt-4 inline-block text-indigo-600 hover:text-indigo-700">
				Prihlasit sa
			</a>
		</div>
	{:else if error}
		<div class="rounded-md bg-red-50 p-4">
			<p class="text-red-700">{error}</p>
		</div>
	{:else if allOrders.length === 0}
		<div class="py-12 text-center">
			<p class="text-gray-500">Zatial ziadne objednavky</p>
			<a href="/" class="mt-4 inline-block text-indigo-600 hover:text-indigo-700">
				Prejst na produkty
			</a>
		</div>
	{:else}
		<div class="space-y-4">
			{#each allOrders as order (order.id)}
				<div class="overflow-hidden rounded-lg bg-white shadow hover:shadow-md transition-shadow">
					<div class="flex items-center justify-between px-6 py-4">
						<div class="flex items-center gap-6">
							<div>
								<span class="text-lg font-medium text-gray-900">Objednavka #{order.id}</span>
								<p class="text-sm text-gray-500">{formatDate(order.createdAt)}</p>
							</div>
							<div class="text-sm text-gray-600">
								{order.itemCount} {order.itemCount === 1 ? 'polozka' : order.itemCount < 5 ? 'polozky' : 'poloziek'}
							</div>
						</div>
						<div class="flex items-center gap-4">
							<span class="text-lg font-bold text-gray-900">
								{order.totalPrice.toFixed(2)} EUR
							</span>
							<span class="rounded-full px-3 py-1 text-sm font-medium {statusColors[order.status]}">
								{statusLabels[order.status]}
							</span>
						</div>
					</div>
				</div>
			{/each}
		</div>
	{/if}
</div>
