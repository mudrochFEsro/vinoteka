<script lang="ts">
	import './layout.css';
	import { authStore } from '$lib/stores/auth.svelte';
	import { cartStore } from '$lib/stores/cart.svelte';
	import { themeStore } from '$lib/stores/theme.svelte';
	import ThemeToggle from '$lib/components/ThemeToggle.svelte';
	import { onMount } from 'svelte';

	let { children } = $props();

	onMount(() => {
		authStore.init();
		themeStore.init();
	});

	// Load cart when authenticated
	$effect(() => {
		if (authStore.authenticated && authStore.initialized) {
			cartStore.load();
		}
	});
</script>

<svelte:head>
	<title>Vinoteka</title>
</svelte:head>

<div class="min-h-screen bg-gray-50 transition-colors duration-300 dark:bg-[#0a0a0a]">
	<nav class="bg-white/80 shadow-sm backdrop-blur-lg transition-colors duration-300 dark:bg-[#1c1c1e]/80 dark:shadow-[#2c2c2e]">
		<div class="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
			<div class="flex h-16 justify-between">
				<div class="flex">
					<a href="/" class="flex items-center text-xl font-bold text-indigo-600 dark:text-indigo-400">Vinoteka</a>
					<div class="ml-10 flex items-center space-x-4">
						<a
							href="/"
							class="rounded-md px-3 py-2 text-sm font-medium text-gray-700 hover:bg-gray-100 hover:text-gray-900 dark:text-gray-300 dark:hover:bg-white/10 dark:hover:text-white"
						>
							Vina
						</a>
						{#if authStore.authenticated}
							<a
								href="/cart"
								class="relative rounded-md px-3 py-2 text-sm font-medium text-gray-700 hover:bg-gray-100 hover:text-gray-900 dark:text-gray-300 dark:hover:bg-white/10 dark:hover:text-white"
							>
								Kosik
								{#if cartStore.itemCount > 0}
									<span
										class="absolute -top-1 -right-1 flex h-5 w-5 items-center justify-center rounded-full bg-indigo-600 text-xs text-white"
									>
										{cartStore.itemCount}
									</span>
								{/if}
							</a>
							<a
								href="/orders"
								class="rounded-md px-3 py-2 text-sm font-medium text-gray-700 hover:bg-gray-100 hover:text-gray-900 dark:text-gray-300 dark:hover:bg-white/10 dark:hover:text-white"
							>
								Objednavky
							</a>
							{#if authStore.admin}
								<a
									href="/admin"
									class="rounded-md px-3 py-2 text-sm font-medium text-gray-700 hover:bg-gray-100 hover:text-gray-900 dark:text-gray-300 dark:hover:bg-white/10 dark:hover:text-white"
								>
									Admin
								</a>
							{/if}
						{/if}
					</div>
				</div>
				<div class="flex items-center space-x-4">
					<ThemeToggle />
					{#if !authStore.initialized}
						<span class="text-sm text-gray-500 dark:text-gray-400">Nacitavam...</span>
					{:else if authStore.authenticated}
						<span class="text-sm text-gray-700 dark:text-gray-300">{authStore.username}</span>
						<button
							onclick={() => authStore.logout()}
							class="rounded-md bg-gray-100 px-3 py-2 text-sm font-medium text-gray-700 hover:bg-gray-200 dark:bg-white/10 dark:text-gray-300 dark:hover:bg-white/20"
						>
							Odhlasit
						</button>
					{:else}
						<a
							href="/login"
							class="rounded-md px-3 py-2 text-sm font-medium text-gray-700 hover:bg-gray-100 dark:text-gray-300 dark:hover:bg-white/10"
						>
							Prihlasit
						</a>
						<a
							href="/register"
							class="rounded-md bg-indigo-600 px-3 py-2 text-sm font-medium text-white hover:bg-indigo-700"
						>
							Registracia
						</a>
					{/if}
				</div>
			</div>
		</div>
	</nav>

	<main class="mx-auto max-w-7xl px-4 py-8 sm:px-6 lg:px-8">
		{@render children()}
	</main>
</div>
