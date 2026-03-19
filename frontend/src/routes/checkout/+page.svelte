<script lang="ts">
	import { goto } from '$app/navigation';
	import { cartStore } from '$lib/stores/cart.svelte';
	import { authStore } from '$lib/stores/auth.svelte';
	import { onMount } from 'svelte';
	import { browser } from '$app/environment';

	const STORAGE_KEY = 'checkout_form';

	let ordering = $state(false);
	let orderError = $state<string | null>(null);

	type DeliveryMethod = 'PACKETA_COURIER' | 'PACKETA_PICKUP';

	const DELIVERY_PRICES: Record<DeliveryMethod, number> = {
		PACKETA_COURIER: 3.99,
		PACKETA_PICKUP: 2.49
	};

	// Packeta Widget API key (get yours at https://client.packeta.com/)
	const PACKETA_API_KEY = ''; // TODO: Add your Packeta API key

	let form = $state({
		email: '',
		firstName: '',
		lastName: '',
		phone: '',
		street: '',
		houseNumber: '',
		city: '',
		postalCode: '',
		country: 'SK',
		isCompany: false,
		companyName: '',
		ico: '',
		dic: '',
		icDph: '',
		deliveryMethod: 'PACKETA_COURIER' as DeliveryMethod,
		packetaPointId: '',
		packetaPointName: '',
		paymentMethod: 'CASH_ON_DELIVERY'
	});

	let deliveryPrice = $derived(DELIVERY_PRICES[form.deliveryMethod]);

	// Open Packeta widget for pickup point selection
	function openPacketaWidget() {
		if (!browser) return;

		// Packeta widget options
		const options = {
			country: form.country.toLowerCase(),
			language: 'sk',
			appIdentity: 'vinoteka-eshop',
			// Use geolocation for better UX
			defaultLocation: null as { latitude: number; longitude: number } | null
		};

		// Try to get current location
		if (navigator.geolocation) {
			navigator.geolocation.getCurrentPosition(
				(position) => {
					options.defaultLocation = {
						latitude: position.coords.latitude,
						longitude: position.coords.longitude
					};
					showPacketaWidget(options);
				},
				() => {
					// Geolocation failed, show widget without location
					showPacketaWidget(options);
				},
				{ timeout: 5000 }
			);
		} else {
			showPacketaWidget(options);
		}
	}

	function showPacketaWidget(options: {
		country: string;
		language: string;
		appIdentity: string;
		defaultLocation: { latitude: number; longitude: number } | null;
	}) {
		// Check if Packeta widget is loaded
		const Packeta = (window as Window & { Packeta?: PacketaWidget }).Packeta;
		if (!Packeta) {
			// Load Packeta widget script dynamically
			const script = document.createElement('script');
			script.src = 'https://widget.packeta.com/v6/www/js/library.js';
			script.onload = () => {
				const PacketaLoaded = (window as Window & { Packeta?: PacketaWidget }).Packeta;
				if (PacketaLoaded) {
					PacketaLoaded.Widget.pick(
						PACKETA_API_KEY,
						handlePacketaPointSelected,
						options
					);
				}
			};
			document.head.appendChild(script);
		} else {
			Packeta.Widget.pick(PACKETA_API_KEY, handlePacketaPointSelected, options);
		}
	}

	interface PacketaWidget {
		Widget: {
			pick: (
				apiKey: string,
				callback: (point: PacketaPoint | null) => void,
				options: object
			) => void;
		};
	}

	interface PacketaPoint {
		id: string;
		name: string;
		place: string;
		street: string;
		city: string;
		zip: string;
	}

	function handlePacketaPointSelected(point: PacketaPoint | null) {
		if (point) {
			form.packetaPointId = point.id;
			form.packetaPointName = `${point.name}, ${point.street}, ${point.city}`;
			touched['packetaPointId'] = true;
		}
	}

	let touched = $state<Record<string, boolean>>({});

	// Load form from sessionStorage on mount
	onMount(() => {
		if (browser) {
			const saved = sessionStorage.getItem(STORAGE_KEY);
			if (saved) {
				try {
					const parsed = JSON.parse(saved);
					form = { ...form, ...parsed };
				} catch {
					// Ignore invalid JSON
				}
			}

			// Pre-populate email for authenticated users
			if (authStore.authenticated && authStore.username && !form.email) {
				form.email = authStore.username;
			}
		}
	});

	// Save form to sessionStorage on change
	$effect(() => {
		if (browser) {
			sessionStorage.setItem(STORAGE_KEY, JSON.stringify(form));
		}
	});

	// Address required only for courier delivery
	let requiresAddress = $derived(form.deliveryMethod !== 'PACKETA_PICKUP');

	// Validation rules
	const validations: Record<string, () => string | null> = {
		email: () => {
			if (!form.email) return 'Email je povinny';
			if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email)) return 'Neplatny format';
			return null;
		},
		firstName: () => (form.firstName ? null : 'Meno je povinne'),
		lastName: () => (form.lastName ? null : 'Priezvisko je povinne'),
		street: () => {
			if (!requiresAddress) return null;
			return form.street ? null : 'Ulica je povinna';
		},
		houseNumber: () => {
			if (!requiresAddress) return null;
			return form.houseNumber ? null : 'Cislo je povinne';
		},
		city: () => {
			if (!requiresAddress) return null;
			return form.city ? null : 'Mesto je povinne';
		},
		postalCode: () => {
			if (!requiresAddress) return null;
			if (!form.postalCode) return 'PSC je povinne';
			if (!/^\d{3} ?\d{2}$/.test(form.postalCode)) return 'Format: XXX XX';
			return null;
		},
		packetaPointId: () => {
			if (form.deliveryMethod !== 'PACKETA_PICKUP') return null;
			return form.packetaPointId ? null : 'Vyberte vydajne miesto';
		},
		// Company validations (only when isCompany is true)
		companyName: () => (form.isCompany && !form.companyName ? 'Nazov firmy je povinny' : null),
		ico: () => {
			if (!form.isCompany) return null;
			if (!form.ico) return 'ICO je povinne';
			if (!/^\d{8}$/.test(form.ico)) return 'ICO musi mat 8 cislic';
			return null;
		}
	};

	let errors = $derived.by(() => {
		const result: Record<string, string | null> = {};
		for (const [key, validator] of Object.entries(validations)) {
			result[key] = validator();
		}
		return result;
	});

	function isValid(field: string): boolean {
		return touched[field] && !errors[field];
	}

	function hasError(field: string): boolean {
		return touched[field] && !!errors[field];
	}

	function onBlur(field: string) {
		touched[field] = true;
	}

	let formValid = $derived(Object.values(errors).every((e) => e === null));

	// Redirect if cart is empty
	$effect(() => {
		if (cartStore.cart && cartStore.cart.items.length === 0) {
			goto('/cart');
		}
	});

	async function placeOrder() {
		// Mark all required fields as touched
		for (const key of Object.keys(validations)) {
			touched[key] = true;
		}

		if (!formValid) return;

		ordering = true;
		orderError = null;

		try {
			// Get items for guest users (auth users will use their cart on backend)
			const items = authStore.authenticated ? undefined : cartStore.getLocalItems();

			const response = await fetch('/checkout', {
				method: 'POST',
				headers: { 'Content-Type': 'application/json' },
				body: JSON.stringify({
					...form,
					items
				})
			});

			const result = await response.json();

			if (result.success) {
				// Clear cart
				if (authStore.authenticated) {
					await cartStore.load();
				} else {
					cartStore.clearLocalCart();
					cartStore.clear();
				}

				if (browser) {
					sessionStorage.removeItem(STORAGE_KEY);
				}

				goto(`/checkout/success?order=${result.orderId}`);
			} else {
				orderError = result.error || 'Chyba pri vytvarani objednavky';
			}
		} catch (e) {
			orderError = e instanceof Error ? e.message : 'Chyba pri vytvarani objednavky';
		} finally {
			ordering = false;
		}
	}

	// Helper function for input classes
	function inputClass(field: string): string {
		const base =
			'mt-1 block w-full rounded-lg border bg-white pr-10 shadow-sm transition-colors focus:ring-2 dark:bg-[#2c2c2e] dark:text-white';
		if (hasError(field)) {
			return `${base} border-red-500 focus:border-red-500 focus:ring-red-200`;
		}
		if (isValid(field)) {
			return `${base} border-green-500 focus:border-green-500 focus:ring-green-200`;
		}
		return `${base} border-gray-300 focus:border-indigo-500 focus:ring-indigo-200 dark:border-[#3a3a3c]`;
	}
</script>

<div class="mx-auto max-w-2xl">
	<h1 class="mb-6 text-2xl font-bold text-gray-900 dark:text-white">Dokoncenie objednavky</h1>

	{#if !cartStore.cart || cartStore.cart.items.length === 0}
		<div class="py-12 text-center">
			<p class="text-gray-500 dark:text-gray-400">Kosik je prazdny</p>
		</div>
	{:else}
		<!-- Order Summary -->
		<div class="mb-6 rounded-2xl bg-white p-6 shadow dark:bg-[#1c1c1e]">
			<h2 class="mb-4 text-lg font-medium text-gray-900 dark:text-white">Zhrnutie objednavky</h2>
			<div class="space-y-2">
				{#each cartStore.cart.items as item (item.productId)}
					<div class="flex justify-between text-sm">
						<span class="text-gray-600 dark:text-gray-400">
							{item.productName} x {item.quantity}
						</span>
						<span class="text-gray-900 dark:text-white">{item.subtotal.toFixed(2)} EUR</span>
					</div>
				{/each}
				<div class="flex justify-between text-sm">
					<span class="text-gray-600 dark:text-gray-400">Doprava</span>
					<span class="text-gray-900 dark:text-white">{deliveryPrice.toFixed(2)} EUR</span>
				</div>
				<div class="border-t border-gray-200 pt-2 dark:border-[#3a3a3c]">
					<div class="flex justify-between font-medium">
						<span class="text-gray-900 dark:text-white">Celkom</span>
						<span class="text-indigo-600 dark:text-indigo-400">
							{(cartStore.cart.totalPrice + deliveryPrice).toFixed(2)} EUR
						</span>
					</div>
				</div>
			</div>
		</div>

		{#if orderError}
			<div class="mb-6 rounded-md bg-red-50 p-4 dark:bg-red-900/20">
				<p class="text-sm text-red-700 dark:text-red-400">{orderError}</p>
			</div>
		{/if}

		<!-- Checkout Form -->
		<form
			onsubmit={(e) => {
				e.preventDefault();
				placeOrder();
			}}
			class="space-y-6"
		>
			<!-- Contact Section -->
			<div class="rounded-2xl bg-white p-6 shadow dark:bg-[#1c1c1e]">
				<h2 class="mb-4 text-lg font-medium text-gray-900 dark:text-white">Kontaktne udaje</h2>

				{#if authStore.authenticated}
					<p class="mb-4 text-sm text-gray-500 dark:text-gray-400">
						Prihlaseny ako: <span class="font-medium text-gray-900 dark:text-white"
							>{authStore.username}</span
						>
					</p>
				{/if}

				<div class="space-y-4">
					<!-- Name -->
					<div class="grid grid-cols-2 gap-4">
						<div>
							<label for="firstName" class="block text-sm font-medium text-gray-700 dark:text-gray-300"
								>Meno *</label
							>
							<div class="relative">
								<input
									type="text"
									id="firstName"
									bind:value={form.firstName}
									onblur={() => onBlur('firstName')}
									class={inputClass('firstName')}
								/>
								{#if isValid('firstName')}
									<span class="absolute top-1/2 right-3 -translate-y-1/2 text-green-500"
										>&#10003;</span
									>
								{/if}
							</div>
							{#if hasError('firstName')}
								<p class="mt-1 text-xs text-red-600 dark:text-red-400">{errors.firstName}</p>
							{/if}
						</div>
						<div>
							<label for="lastName" class="block text-sm font-medium text-gray-700 dark:text-gray-300"
								>Priezvisko *</label
							>
							<div class="relative">
								<input
									type="text"
									id="lastName"
									bind:value={form.lastName}
									onblur={() => onBlur('lastName')}
									class={inputClass('lastName')}
								/>
								{#if isValid('lastName')}
									<span class="absolute top-1/2 right-3 -translate-y-1/2 text-green-500"
										>&#10003;</span
									>
								{/if}
							</div>
							{#if hasError('lastName')}
								<p class="mt-1 text-xs text-red-600 dark:text-red-400">{errors.lastName}</p>
							{/if}
						</div>
					</div>

					<!-- Email & Phone -->
					<div class="grid grid-cols-2 gap-4">
						<div>
							<label for="email" class="block text-sm font-medium text-gray-700 dark:text-gray-300"
								>Email *</label
							>
							<div class="relative">
								<input
									type="email"
									id="email"
									bind:value={form.email}
									onblur={() => onBlur('email')}
									placeholder="vas@email.sk"
									class="{inputClass('email')} dark:placeholder-gray-400"
								/>
								{#if isValid('email')}
									<span class="absolute top-1/2 right-3 -translate-y-1/2 text-green-500"
										>&#10003;</span
									>
								{/if}
							</div>
							{#if hasError('email')}
								<p class="mt-1 text-xs text-red-600 dark:text-red-400">{errors.email}</p>
							{/if}
						</div>
						<div>
							<label for="phone" class="block text-sm font-medium text-gray-700 dark:text-gray-300"
								>Telefon</label
							>
							<input
								type="tel"
								id="phone"
								bind:value={form.phone}
								placeholder="+421 9XX XXX XXX"
								class="mt-1 block w-full rounded-lg border border-gray-300 bg-white shadow-sm focus:border-indigo-500 focus:ring-2 focus:ring-indigo-200 dark:border-[#3a3a3c] dark:bg-[#2c2c2e] dark:text-white dark:placeholder-gray-400"
							/>
						</div>
					</div>
				</div>
			</div>

			<!-- Delivery Section -->
			<div class="rounded-2xl bg-white p-6 shadow dark:bg-[#1c1c1e]">
				<div class="mb-4 flex items-center gap-2">
					<img src="https://www.packeta.com/favicon.ico" alt="Packeta" class="h-6 w-6" />
					<h2 class="text-lg font-medium text-gray-900 dark:text-white">Sposob dopravy</h2>
				</div>

				<div class="space-y-3">
					<label
						class="flex cursor-pointer items-center gap-3 rounded-lg border p-4 transition-colors {form.deliveryMethod ===
						'PACKETA_COURIER'
							? 'border-indigo-500 bg-indigo-50 dark:bg-indigo-900/20'
							: 'border-gray-200 hover:border-gray-300 dark:border-[#3a3a3c]'}"
					>
						<input
							type="radio"
							name="deliveryMethod"
							value="PACKETA_COURIER"
							bind:group={form.deliveryMethod}
							class="h-4 w-4 text-indigo-600 focus:ring-indigo-500"
						/>
						<div class="flex-1">
							<span class="font-medium text-gray-900 dark:text-white">Packeta kurier</span>
							<p class="text-sm text-gray-500 dark:text-gray-400">Dorucenie na vasu adresu</p>
						</div>
						<span class="font-medium text-gray-900 dark:text-white">3.99 EUR</span>
					</label>

					<label
						class="flex cursor-pointer items-center gap-3 rounded-lg border p-4 transition-colors {form.deliveryMethod ===
						'PACKETA_PICKUP'
							? 'border-indigo-500 bg-indigo-50 dark:bg-indigo-900/20'
							: 'border-gray-200 hover:border-gray-300 dark:border-[#3a3a3c]'}"
					>
						<input
							type="radio"
							name="deliveryMethod"
							value="PACKETA_PICKUP"
							bind:group={form.deliveryMethod}
							class="h-4 w-4 text-indigo-600 focus:ring-indigo-500"
						/>
						<div class="flex-1">
							<span class="font-medium text-gray-900 dark:text-white">Packeta vydajne miesto</span>
							<p class="text-sm text-gray-500 dark:text-gray-400">Vyzdvihnite si balik na pobocke</p>
						</div>
						<span class="font-medium text-gray-900 dark:text-white">2.49 EUR</span>
					</label>
				</div>

				{#if form.deliveryMethod === 'PACKETA_PICKUP'}
					<div class="mt-4 border-t border-gray-200 pt-4 dark:border-[#3a3a3c]">
						<label for="packetaPointName" class="block text-sm font-medium text-gray-700 dark:text-gray-300"
							>Vydajne miesto *</label
						>
						<div class="mt-1 flex gap-2">
							<input
								type="text"
								id="packetaPointName"
								bind:value={form.packetaPointName}
								readonly
								placeholder="Kliknite na 'Vybrat' pre vyber miesta"
								class="flex-1 rounded-lg border px-3 py-2 {form.packetaPointId
									? 'border-green-500 bg-green-50 text-green-800 dark:bg-green-900/20 dark:text-green-300'
									: 'border-gray-300 bg-gray-50 text-gray-500 dark:border-[#3a3a3c] dark:bg-[#2c2c2e] dark:text-gray-400'}"
							/>
							<button
								type="button"
								onclick={openPacketaWidget}
								class="rounded-lg bg-[#ba1b02] px-4 py-2 text-sm font-medium text-white hover:bg-[#a01800] transition-colors"
							>
								Vybrat miesto
							</button>
						</div>
						{#if hasError('packetaPointId')}
							<p class="mt-1 text-xs text-red-600 dark:text-red-400">{errors.packetaPointId}</p>
						{/if}
						<p class="mt-2 text-xs text-gray-500 dark:text-gray-400">
							Z-BOXy, pobocky Packeta a vybrane partnerske predajne
						</p>
					</div>
				{/if}
			</div>

			<!-- Address Section (only for courier delivery) -->
			{#if requiresAddress}
			<div class="rounded-2xl bg-white p-6 shadow dark:bg-[#1c1c1e]">
				<h2 class="mb-4 text-lg font-medium text-gray-900 dark:text-white">Adresa dorucenia</h2>

				<div class="space-y-4">
					<div class="grid grid-cols-3 gap-4">
						<div class="col-span-2">
							<label for="street" class="block text-sm font-medium text-gray-700 dark:text-gray-300"
								>Ulica *</label
							>
							<div class="relative">
								<input
									type="text"
									id="street"
									bind:value={form.street}
									onblur={() => onBlur('street')}
									class={inputClass('street')}
								/>
								{#if isValid('street')}
									<span class="absolute top-1/2 right-3 -translate-y-1/2 text-green-500"
										>&#10003;</span
									>
								{/if}
							</div>
							{#if hasError('street')}
								<p class="mt-1 text-xs text-red-600 dark:text-red-400">{errors.street}</p>
							{/if}
						</div>
						<div>
							<label for="houseNumber" class="block text-sm font-medium text-gray-700 dark:text-gray-300"
								>Cislo *</label
							>
							<div class="relative">
								<input
									type="text"
									id="houseNumber"
									bind:value={form.houseNumber}
									onblur={() => onBlur('houseNumber')}
									class={inputClass('houseNumber')}
								/>
								{#if isValid('houseNumber')}
									<span class="absolute top-1/2 right-3 -translate-y-1/2 text-green-500"
										>&#10003;</span
									>
								{/if}
							</div>
							{#if hasError('houseNumber')}
								<p class="mt-1 text-xs text-red-600 dark:text-red-400">{errors.houseNumber}</p>
							{/if}
						</div>
					</div>

					<div class="grid grid-cols-3 gap-4">
						<div>
							<label for="city" class="block text-sm font-medium text-gray-700 dark:text-gray-300"
								>Mesto *</label
							>
							<div class="relative">
								<input
									type="text"
									id="city"
									bind:value={form.city}
									onblur={() => onBlur('city')}
									class={inputClass('city')}
								/>
								{#if isValid('city')}
									<span class="absolute top-1/2 right-3 -translate-y-1/2 text-green-500"
										>&#10003;</span
									>
								{/if}
							</div>
							{#if hasError('city')}
								<p class="mt-1 text-xs text-red-600 dark:text-red-400">{errors.city}</p>
							{/if}
						</div>
						<div>
							<label for="postalCode" class="block text-sm font-medium text-gray-700 dark:text-gray-300">PSC *</label
							>
							<div class="relative">
								<input
									type="text"
									id="postalCode"
									bind:value={form.postalCode}
									onblur={() => onBlur('postalCode')}
									placeholder="821 07"
									class="{inputClass('postalCode')} dark:placeholder-gray-400"
								/>
								{#if isValid('postalCode')}
									<span class="absolute top-1/2 right-3 -translate-y-1/2 text-green-500"
										>&#10003;</span
									>
								{/if}
							</div>
							{#if hasError('postalCode')}
								<p class="mt-1 text-xs text-red-600 dark:text-red-400">{errors.postalCode}</p>
							{/if}
						</div>
						<div>
							<label for="country" class="block text-sm font-medium text-gray-700 dark:text-gray-300"
								>Krajina *</label
							>
							<select
								id="country"
								bind:value={form.country}
								class="mt-1 block w-full rounded-lg border border-gray-300 bg-white shadow-sm focus:border-indigo-500 focus:ring-2 focus:ring-indigo-200 dark:border-[#3a3a3c] dark:bg-[#2c2c2e] dark:text-white"
							>
								<option value="SK">Slovensko</option>
								<option value="CZ">Cesko</option>
							</select>
						</div>
					</div>
				</div>
			</div>
			{/if}

			<!-- Payment Section -->
			<div class="rounded-2xl bg-white p-6 shadow dark:bg-[#1c1c1e]">
				<h2 class="mb-4 text-lg font-medium text-gray-900 dark:text-white">Sposob platby</h2>

				<div
					class="flex items-center gap-3 rounded-lg border border-indigo-500 bg-indigo-50 p-4 dark:bg-indigo-900/20"
				>
					<div
						class="flex h-10 w-10 items-center justify-center rounded-full bg-indigo-100 dark:bg-indigo-800"
					>
						<svg
							class="h-5 w-5 text-indigo-600 dark:text-indigo-300"
							fill="none"
							stroke="currentColor"
							viewBox="0 0 24 24"
						>
							<path
								stroke-linecap="round"
								stroke-linejoin="round"
								stroke-width="2"
								d="M17 9V7a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2m2 4h10a2 2 0 002-2v-6a2 2 0 00-2-2H9a2 2 0 00-2 2v6a2 2 0 002 2zm7-5a2 2 0 11-4 0 2 2 0 014 0z"
							/>
						</svg>
					</div>
					<div class="flex-1">
						<span class="font-medium text-gray-900 dark:text-white">Dobierka</span>
						<p class="text-sm text-gray-600 dark:text-gray-400">Platba pri prevzati zasielky</p>
					</div>
				</div>

				<div class="mt-4 rounded-lg bg-gray-50 p-4 dark:bg-[#2c2c2e]">
					<p class="text-sm font-medium text-gray-700 dark:text-gray-300">Moznosti platby:</p>
					<ul class="mt-2 space-y-1 text-sm text-gray-600 dark:text-gray-400">
						{#if form.deliveryMethod === 'PACKETA_COURIER'}
							<li class="flex items-center gap-2">
								<span class="text-green-500">&#10003;</span> Hotovost kurierovi
							</li>
							<li class="flex items-center gap-2">
								<span class="text-green-500">&#10003;</span> Platba kartou kurierovi
							</li>
						{:else}
							<li class="flex items-center gap-2">
								<span class="text-green-500">&#10003;</span> Platba kartou v Packeta appke
							</li>
						{/if}
					</ul>
				</div>
			</div>

			<!-- Company Section -->
			<div class="rounded-2xl bg-white p-6 shadow dark:bg-[#1c1c1e]">
				<label class="flex cursor-pointer items-center gap-3">
					<input
						type="checkbox"
						bind:checked={form.isCompany}
						class="h-5 w-5 rounded border-gray-300 text-indigo-600 focus:ring-indigo-500 dark:border-[#3a3a3c] dark:bg-[#2c2c2e]"
					/>
					<span class="text-lg font-medium text-gray-900 dark:text-white">Nakupujem na firmu</span>
				</label>

				{#if form.isCompany}
					<div class="mt-4 space-y-4 border-t border-gray-200 pt-4 dark:border-[#3a3a3c]">
						<div>
							<label for="companyName" class="block text-sm font-medium text-gray-700 dark:text-gray-300"
								>Nazov firmy *</label
							>
							<div class="relative">
								<input
									type="text"
									id="companyName"
									bind:value={form.companyName}
									onblur={() => onBlur('companyName')}
									class={inputClass('companyName')}
								/>
								{#if isValid('companyName')}
									<span class="absolute top-1/2 right-3 -translate-y-1/2 text-green-500"
										>&#10003;</span
									>
								{/if}
							</div>
							{#if hasError('companyName')}
								<p class="mt-1 text-xs text-red-600 dark:text-red-400">{errors.companyName}</p>
							{/if}
						</div>

						<div class="grid grid-cols-3 gap-4">
							<div>
								<label for="ico" class="block text-sm font-medium text-gray-700 dark:text-gray-300"
									>ICO *</label
								>
								<div class="relative">
									<input
										type="text"
										id="ico"
										bind:value={form.ico}
										onblur={() => onBlur('ico')}
										placeholder="12345678"
										class="{inputClass('ico')} dark:placeholder-gray-400"
									/>
									{#if isValid('ico')}
										<span class="absolute top-1/2 right-3 -translate-y-1/2 text-green-500"
											>&#10003;</span
										>
									{/if}
								</div>
								{#if hasError('ico')}
									<p class="mt-1 text-xs text-red-600 dark:text-red-400">{errors.ico}</p>
								{/if}
							</div>
							<div>
								<label for="dic" class="block text-sm font-medium text-gray-700 dark:text-gray-300">DIC</label
								>
								<input
									type="text"
									id="dic"
									bind:value={form.dic}
									placeholder="2012345678"
									class="mt-1 block w-full rounded-lg border border-gray-300 bg-white shadow-sm focus:border-indigo-500 focus:ring-2 focus:ring-indigo-200 dark:border-[#3a3a3c] dark:bg-[#2c2c2e] dark:text-white dark:placeholder-gray-400"
								/>
							</div>
							<div>
								<label for="icDph" class="block text-sm font-medium text-gray-700 dark:text-gray-300"
									>IC DPH</label
								>
								<input
									type="text"
									id="icDph"
									bind:value={form.icDph}
									placeholder="SK2012345678"
									class="mt-1 block w-full rounded-lg border border-gray-300 bg-white shadow-sm focus:border-indigo-500 focus:ring-2 focus:ring-indigo-200 dark:border-[#3a3a3c] dark:bg-[#2c2c2e] dark:text-white dark:placeholder-gray-400"
								/>
							</div>
						</div>
					</div>
				{/if}
			</div>

			<!-- Submit -->
			<button
				type="submit"
				disabled={ordering}
				class="w-full rounded-full bg-indigo-600 px-6 py-3 text-lg font-medium text-white transition-colors hover:bg-indigo-700 disabled:opacity-50"
			>
				{ordering ? 'Objednavanie...' : 'Objednat'}
			</button>

			{#if !authStore.authenticated}
				<p class="text-center text-sm text-gray-500 dark:text-gray-400">
					Mate ucet?
					<a
						href="/login?redirect=/checkout"
						class="font-medium text-indigo-600 hover:text-indigo-500 dark:text-indigo-400"
					>
						Prihlaste sa
					</a>
				</p>
			{/if}
		</form>
	{/if}
</div>
