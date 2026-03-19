<script lang="ts">
	import type { Product } from '$lib/api';
	import { cartStore } from '$lib/stores/cart.svelte';
	import { toast } from 'svelte-sonner';

	let { product }: { product: Product } = $props();

	let adding = $state(false);

	async function addToCart() {
		adding = true;
		await cartStore.addItem(product.id, 1, product);
		toast.success(`${product.name} pridane do kosika`);
		adding = false;
	}
</script>

<div class="overflow-hidden rounded-2xl bg-white shadow-sm transition-all duration-300 hover:shadow-lg dark:bg-[#1c1c1e] dark:shadow-[#000]/20">
	{#if product.imageUrl}
		<img src={product.imageUrl} alt={product.name} loading="lazy" class="h-48 w-full object-cover" />
	{:else}
		<div class="flex h-48 items-center justify-center bg-gray-100 dark:bg-[#2c2c2e]">
			<span class="text-gray-400 dark:text-gray-500">Bez obrazka</span>
		</div>
	{/if}
	<div class="p-4">
		<h3 class="text-lg font-semibold text-gray-900 dark:text-white">{product.name}</h3>
		<p class="mt-1 text-sm text-gray-500 dark:text-gray-400">{product.categoryName}</p>
		<p class="mt-2 line-clamp-2 text-sm text-gray-600 dark:text-gray-300">{product.description}</p>
		<div class="mt-4 flex items-center justify-between">
			<span class="text-lg font-bold text-indigo-600 dark:text-indigo-400">{product.price.toFixed(2)} EUR</span>
			<button
				onclick={addToCart}
				disabled={adding || product.stock === 0}
				class="rounded-full bg-indigo-600 px-4 py-2 text-sm font-medium text-white transition-colors hover:bg-indigo-700 disabled:cursor-not-allowed disabled:opacity-50"
			>
				{#if adding}
					Pridavam...
				{:else if product.stock === 0}
					Vypredane
				{:else}
					Pridat do kosika
				{/if}
			</button>
		</div>
		{#if product.stock > 0 && product.stock <= 5}
			<p class="mt-2 text-sm text-orange-600 dark:text-orange-400">Poslednych {product.stock} ks</p>
		{/if}
	</div>
</div>
