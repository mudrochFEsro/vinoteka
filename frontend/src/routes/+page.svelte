<script lang="ts">
	import { onMount } from 'svelte';
	import { products, categories, type Product, type Category } from '$lib/api';
	import ProductCard from '$lib/components/ProductCard.svelte';

	let allProducts = $state.raw<Product[]>([]);
	let allCategories = $state.raw<Category[]>([]);
	let loading = $state(true);
	let error = $state<string | null>(null);

	let search = $state('');
	let selectedCategory = $state<number | null>(null);

	let filteredProducts = $derived.by(() => {
		let result = allProducts;

		if (selectedCategory !== null) {
			result = result.filter((p) => p.categoryId === selectedCategory);
		}

		if (search.trim()) {
			const searchLower = search.toLowerCase();
			result = result.filter(
				(p) =>
					p.name.toLowerCase().includes(searchLower) ||
					p.description.toLowerCase().includes(searchLower)
			);
		}

		return result;
	});

	onMount(async () => {
		try {
			[allProducts, allCategories] = await Promise.all([products.getAll(), categories.getAll()]);
		} catch (e) {
			error = e instanceof Error ? e.message : 'Chyba pri nacitani produktov';
		} finally {
			loading = false;
		}
	});
</script>

<div>
	<h1 class="mb-6 text-2xl font-bold text-gray-900 dark:text-white">Nase vina</h1>

	<div class="mb-6 flex flex-col gap-4 sm:flex-row">
		<input
			type="text"
			bind:value={search}
			placeholder="Hladat produkty..."
			class="flex-1 rounded-md border-gray-300 bg-white shadow-sm focus:border-indigo-500 focus:ring-indigo-500 dark:border-[#3a3a3c] dark:bg-[#2c2c2e] dark:text-white dark:placeholder-gray-400"
		/>
		<select
			bind:value={selectedCategory}
			class="rounded-md border-gray-300 bg-white shadow-sm focus:border-indigo-500 focus:ring-indigo-500 dark:border-[#3a3a3c] dark:bg-[#2c2c2e] dark:text-white"
		>
			<option value={null}>Vsetky kategorie</option>
			{#each allCategories as category (category.id)}
				<option value={category.id}>{category.name}</option>
			{/each}
		</select>
	</div>

	{#if loading}
		<div class="py-12 text-center">
			<p class="text-gray-500 dark:text-gray-400">Nacitavam produkty...</p>
		</div>
	{:else if error}
		<div class="rounded-md bg-red-50 p-4 dark:bg-red-900/20">
			<p class="text-red-700 dark:text-red-400">{error}</p>
		</div>
	{:else if filteredProducts.length === 0}
		<div class="py-12 text-center">
			<p class="text-gray-500 dark:text-gray-400">Ziadne produkty</p>
		</div>
	{:else}
		<div class="grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
			{#each filteredProducts as product (product.id)}
				<ProductCard {product} />
			{/each}
		</div>
	{/if}
</div>
