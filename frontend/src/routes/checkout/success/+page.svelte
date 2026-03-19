<script lang="ts">
	import { page } from '$app/stores';

	let orderId = $derived($page.url.searchParams.get('order'));
</script>

<div class="mx-auto max-w-lg py-12 text-center">
	<div class="mb-6 inline-flex h-16 w-16 items-center justify-center rounded-full bg-green-100 dark:bg-green-900/30">
		<svg class="h-8 w-8 text-green-600 dark:text-green-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
			<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
		</svg>
	</div>

	<h1 class="mb-4 text-2xl font-bold text-gray-900 dark:text-white">Objednavka uspesna!</h1>

	{#if orderId}
		<p class="mb-6 text-gray-600 dark:text-gray-400">
			Cislo objednavky: <span class="font-medium text-gray-900 dark:text-white">#{orderId}</span>
		</p>
	{/if}

	<p class="mb-8 text-gray-600 dark:text-gray-400">
		Dakujeme za vasu objednavku. Na vas email sme odoslali potvrdenie.
	</p>

	<a
		href="/"
		class="inline-block rounded-full bg-indigo-600 px-6 py-3 font-medium text-white transition-colors hover:bg-indigo-700"
	>
		Pokracovat v nakupovani
	</a>
</div>
