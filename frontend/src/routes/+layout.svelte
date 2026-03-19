<script lang="ts">
	import './layout.css';
	import { authStore } from '$lib/stores/auth.svelte';
	import { cartStore } from '$lib/stores/cart.svelte';
	import { themeStore } from '$lib/stores/theme.svelte';
	import ThemeToggle from '$lib/components/ThemeToggle.svelte';
	import { Toaster } from 'svelte-sonner';
	import { onMount } from 'svelte';
	import { page } from '$app/stores';

	let { children } = $props();
	let mobileMenuOpen = $state(false);

	onMount(() => {
		authStore.init();
		themeStore.init();
	});

	// Load cart (works for both authenticated and guest users)
	$effect(() => {
		if (authStore.initialized) {
			cartStore.load();
		}
	});

	// Close mobile menu on navigation
	$effect(() => {
		$page.url.pathname;
		mobileMenuOpen = false;
	});

	function toggleMenu() {
		mobileMenuOpen = !mobileMenuOpen;
	}
</script>

<svelte:head>
	<title>Vinoteka</title>
</svelte:head>

<Toaster richColors position="top-right" />

<div class="min-h-screen bg-gray-50 transition-colors duration-300 dark:bg-[#0a0a0a]">
	<nav
		class="bg-white/80 shadow-sm backdrop-blur-lg transition-colors duration-300 dark:bg-[#1c1c1e]/80 dark:shadow-[#2c2c2e]"
	>
		<div class="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
			<div class="flex h-16 justify-between">
				<!-- Logo + Desktop Nav -->
				<div class="flex">
					<a
						href="/"
						class="flex items-center text-xl font-bold text-indigo-600 dark:text-indigo-400"
					>
						Vinoteka
					</a>
					<!-- Desktop Navigation -->
					<div class="ml-10 hidden items-center space-x-4 md:flex">
						<a
							href="/"
							class="rounded-md px-3 py-2 text-sm font-medium text-gray-700 hover:bg-gray-100 hover:text-gray-900 dark:text-gray-300 dark:hover:bg-white/10 dark:hover:text-white"
						>
							Vina
						</a>
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
						{#if authStore.authenticated}
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

				<!-- Desktop Right Side -->
				<div class="hidden items-center space-x-4 md:flex">
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

				<!-- Mobile: Cart + Hamburger -->
				<div class="flex items-center space-x-2 md:hidden">
					<ThemeToggle />
					{#if cartStore.itemCount > 0}
						<a href="/cart" class="relative p-2">
							<svg
								class="h-6 w-6 text-gray-700 dark:text-gray-300"
								fill="none"
								stroke="currentColor"
								viewBox="0 0 24 24"
							>
								<path
									stroke-linecap="round"
									stroke-linejoin="round"
									stroke-width="2"
									d="M3 3h2l.4 2M7 13h10l4-8H5.4M7 13L5.4 5M7 13l-2.293 2.293c-.63.63-.184 1.707.707 1.707H17m0 0a2 2 0 100 4 2 2 0 000-4zm-8 2a2 2 0 11-4 0 2 2 0 014 0z"
								/>
							</svg>
							<span
								class="absolute -top-1 -right-1 flex h-5 w-5 items-center justify-center rounded-full bg-indigo-600 text-xs text-white"
							>
								{cartStore.itemCount}
							</span>
						</a>
					{/if}
					<button
						onclick={toggleMenu}
						class="rounded-md p-2 text-gray-700 hover:bg-gray-100 dark:text-gray-300 dark:hover:bg-white/10"
						aria-label="Menu"
					>
						{#if mobileMenuOpen}
							<svg class="h-6 w-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
								<path
									stroke-linecap="round"
									stroke-linejoin="round"
									stroke-width="2"
									d="M6 18L18 6M6 6l12 12"
								/>
							</svg>
						{:else}
							<svg class="h-6 w-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
								<path
									stroke-linecap="round"
									stroke-linejoin="round"
									stroke-width="2"
									d="M4 6h16M4 12h16M4 18h16"
								/>
							</svg>
						{/if}
					</button>
				</div>
			</div>
		</div>

		<!-- Mobile Menu -->
		{#if mobileMenuOpen}
			<div class="border-t border-gray-200 bg-white dark:border-[#3a3a3c] dark:bg-[#1c1c1e] md:hidden">
				<div class="space-y-1 px-4 py-3">
					<a
						href="/"
						class="block rounded-md px-3 py-2 text-base font-medium text-gray-700 hover:bg-gray-100 dark:text-gray-300 dark:hover:bg-white/10"
					>
						Vina
					</a>
					<a
						href="/cart"
						class="block rounded-md px-3 py-2 text-base font-medium text-gray-700 hover:bg-gray-100 dark:text-gray-300 dark:hover:bg-white/10"
					>
						Kosik
						{#if cartStore.itemCount > 0}
							<span
								class="ml-2 inline-flex items-center justify-center rounded-full bg-indigo-600 px-2 py-0.5 text-xs text-white"
							>
								{cartStore.itemCount}
							</span>
						{/if}
					</a>
					{#if authStore.authenticated}
						<a
							href="/orders"
							class="block rounded-md px-3 py-2 text-base font-medium text-gray-700 hover:bg-gray-100 dark:text-gray-300 dark:hover:bg-white/10"
						>
							Objednavky
						</a>
						{#if authStore.admin}
							<a
								href="/admin"
								class="block rounded-md px-3 py-2 text-base font-medium text-gray-700 hover:bg-gray-100 dark:text-gray-300 dark:hover:bg-white/10"
							>
								Admin
							</a>
						{/if}
					{/if}
				</div>

				<div class="border-t border-gray-200 px-4 py-3 dark:border-[#3a3a3c]">
					{#if !authStore.initialized}
						<span class="text-sm text-gray-500 dark:text-gray-400">Nacitavam...</span>
					{:else if authStore.authenticated}
						<div class="mb-3 text-sm text-gray-700 dark:text-gray-300">
							Prihlaseny: <span class="font-medium">{authStore.username}</span>
						</div>
						<button
							onclick={() => authStore.logout()}
							class="w-full rounded-md bg-gray-100 px-3 py-2 text-sm font-medium text-gray-700 hover:bg-gray-200 dark:bg-white/10 dark:text-gray-300 dark:hover:bg-white/20"
						>
							Odhlasit sa
						</button>
					{:else}
						<div class="flex flex-col space-y-2">
							<a
								href="/login"
								class="rounded-md bg-gray-100 px-3 py-2 text-center text-sm font-medium text-gray-700 hover:bg-gray-200 dark:bg-white/10 dark:text-gray-300 dark:hover:bg-white/20"
							>
								Prihlasit sa
							</a>
							<a
								href="/register"
								class="rounded-md bg-indigo-600 px-3 py-2 text-center text-sm font-medium text-white hover:bg-indigo-700"
							>
								Registracia
							</a>
						</div>
					{/if}
				</div>
			</div>
		{/if}
	</nav>

	<main class="mx-auto max-w-7xl px-4 py-8 sm:px-6 lg:px-8">
		{@render children()}
	</main>
</div>
