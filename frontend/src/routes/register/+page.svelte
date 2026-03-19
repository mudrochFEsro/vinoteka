<script lang="ts">
	import { enhance } from '$app/forms';
	import { goto } from '$app/navigation';

	let form = $state({
		email: '',
		password: '',
		confirmPassword: '',
		firstName: '',
		lastName: ''
	});

	let touched = $state<Record<string, boolean>>({});
	let loading = $state(false);
	let serverError = $state<string | null>(null);
	let success = $state(false);

	// Validation rules
	const validations = {
		firstName: () => (form.firstName.length > 0 ? null : 'Meno je povinne'),
		lastName: () => (form.lastName.length > 0 ? null : 'Priezvisko je povinne'),
		email: () => {
			if (!form.email) return 'Email je povinny';
			if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email)) return 'Neplatny format emailu';
			return null;
		},
		password: () => {
			if (!form.password) return 'Heslo je povinne';
			if (form.password.length < 6) return 'Heslo musi mat aspon 6 znakov';
			return null;
		},
		confirmPassword: () => {
			if (!form.confirmPassword) return 'Potvrdte heslo';
			if (form.password !== form.confirmPassword) return 'Hesla sa nezhoduju';
			return null;
		}
	};

	// Get current errors
	let errors = $derived.by(() => {
		const result: Record<string, string | null> = {};
		for (const [key, validator] of Object.entries(validations)) {
			result[key] = validator();
		}
		return result;
	});

	// Check if field is valid (for showing green checkmark)
	function isValid(field: string): boolean {
		return touched[field] && !errors[field];
	}

	// Check if field has error (for showing red border)
	function hasError(field: string): boolean {
		return touched[field] && !!errors[field];
	}

	// Mark field as touched on blur
	function onBlur(field: string) {
		touched[field] = true;
	}

	// Check if form is valid
	let formValid = $derived(Object.values(errors).every((e) => e === null));

	// Handle successful registration
	function onSuccess(_email: string) {
		success = true;
		// Redirect to login page after 1.5s
		setTimeout(() => goto('/login'), 1500);
	}
</script>

<div class="flex min-h-[80vh] items-center justify-center py-8">
	<div class="w-full max-w-md">
		<div class="rounded-2xl bg-white px-8 py-10 shadow-lg transition-colors dark:bg-[#1c1c1e]">
			<h1 class="mb-8 text-center text-2xl font-bold text-gray-900 dark:text-white">
				Registracia
			</h1>

			{#if success}
				<div class="mb-6 rounded-md bg-green-50 p-4 dark:bg-green-900/20">
					<p class="text-sm text-green-700 dark:text-green-400">
						Registracia uspesna! Presmerovavam na prihlasenie...
					</p>
				</div>
			{/if}

			{#if serverError}
				<div class="mb-6 rounded-md bg-red-50 p-4 dark:bg-red-900/20">
					<p class="text-sm text-red-700 dark:text-red-400">{serverError}</p>
				</div>
			{/if}

			<form
				method="POST"
				use:enhance={() => {
					loading = true;
					serverError = null;
					return async ({ result }) => {
						loading = false;
						if (result.type === 'success') {
							const data = result.data as { success?: boolean; email?: string } | undefined;
							if (data?.success && data?.email) {
								onSuccess(data.email);
							}
						} else if (result.type === 'failure') {
							const data = result.data as { error?: string } | undefined;
							serverError = data?.error || 'Registracia zlyhala';
						}
					};
				}}
				class="space-y-5"
			>
				<!-- Name fields -->
				<div class="grid grid-cols-2 gap-4">
					<div>
						<label
							for="firstName"
							class="block text-sm font-medium text-gray-700 dark:text-gray-300"
						>
							Meno *
						</label>
						<div class="relative">
							<input
								type="text"
								id="firstName"
								name="firstName"
								bind:value={form.firstName}
								onblur={() => onBlur('firstName')}
								class="mt-1 block w-full rounded-lg border bg-white pr-10 shadow-sm transition-colors focus:ring-2 dark:bg-[#2c2c2e] dark:text-white
								{hasError('firstName')
									? 'border-red-500 focus:border-red-500 focus:ring-red-200'
									: isValid('firstName')
										? 'border-green-500 focus:border-green-500 focus:ring-green-200'
										: 'border-gray-300 focus:border-indigo-500 focus:ring-indigo-200 dark:border-[#3a3a3c]'}"
							/>
							{#if isValid('firstName')}
								<span class="absolute top-1/2 right-3 -translate-y-1/2 text-green-500">&#10003;</span
								>
							{/if}
						</div>
						{#if hasError('firstName')}
							<p class="mt-1 text-xs text-red-600 dark:text-red-400">{errors.firstName}</p>
						{/if}
					</div>
					<div>
						<label
							for="lastName"
							class="block text-sm font-medium text-gray-700 dark:text-gray-300"
						>
							Priezvisko *
						</label>
						<div class="relative">
							<input
								type="text"
								id="lastName"
								name="lastName"
								bind:value={form.lastName}
								onblur={() => onBlur('lastName')}
								class="mt-1 block w-full rounded-lg border bg-white pr-10 shadow-sm transition-colors focus:ring-2 dark:bg-[#2c2c2e] dark:text-white
								{hasError('lastName')
									? 'border-red-500 focus:border-red-500 focus:ring-red-200'
									: isValid('lastName')
										? 'border-green-500 focus:border-green-500 focus:ring-green-200'
										: 'border-gray-300 focus:border-indigo-500 focus:ring-indigo-200 dark:border-[#3a3a3c]'}"
							/>
							{#if isValid('lastName')}
								<span class="absolute top-1/2 right-3 -translate-y-1/2 text-green-500">&#10003;</span
								>
							{/if}
						</div>
						{#if hasError('lastName')}
							<p class="mt-1 text-xs text-red-600 dark:text-red-400">{errors.lastName}</p>
						{/if}
					</div>
				</div>

				<!-- Email -->
				<div>
					<label for="email" class="block text-sm font-medium text-gray-700 dark:text-gray-300">
						Email *
					</label>
					<div class="relative">
						<input
							type="email"
							id="email"
							name="email"
							bind:value={form.email}
							onblur={() => onBlur('email')}
							placeholder="vas@email.sk"
							class="mt-1 block w-full rounded-lg border bg-white pr-10 shadow-sm transition-colors focus:ring-2 dark:bg-[#2c2c2e] dark:text-white dark:placeholder-gray-400
							{hasError('email')
								? 'border-red-500 focus:border-red-500 focus:ring-red-200'
								: isValid('email')
									? 'border-green-500 focus:border-green-500 focus:ring-green-200'
									: 'border-gray-300 focus:border-indigo-500 focus:ring-indigo-200 dark:border-[#3a3a3c]'}"
						/>
						{#if isValid('email')}
							<span class="absolute top-1/2 right-3 -translate-y-1/2 text-green-500">&#10003;</span>
						{/if}
					</div>
					{#if hasError('email')}
						<p class="mt-1 text-xs text-red-600 dark:text-red-400">{errors.email}</p>
					{/if}
				</div>

				<!-- Password fields -->
				<div class="grid grid-cols-2 gap-4">
					<div>
						<label
							for="password"
							class="block text-sm font-medium text-gray-700 dark:text-gray-300"
						>
							Heslo *
						</label>
						<div class="relative">
							<input
								type="password"
								id="password"
								name="password"
								bind:value={form.password}
								onblur={() => onBlur('password')}
								oninput={() => {
									if (touched.confirmPassword) touched.confirmPassword = true;
								}}
								class="mt-1 block w-full rounded-lg border bg-white pr-10 shadow-sm transition-colors focus:ring-2 dark:bg-[#2c2c2e] dark:text-white
								{hasError('password')
									? 'border-red-500 focus:border-red-500 focus:ring-red-200'
									: isValid('password')
										? 'border-green-500 focus:border-green-500 focus:ring-green-200'
										: 'border-gray-300 focus:border-indigo-500 focus:ring-indigo-200 dark:border-[#3a3a3c]'}"
							/>
							{#if isValid('password')}
								<span class="absolute top-1/2 right-3 -translate-y-1/2 text-green-500">&#10003;</span
								>
							{/if}
						</div>
						{#if hasError('password')}
							<p class="mt-1 text-xs text-red-600 dark:text-red-400">{errors.password}</p>
						{:else if touched.password && form.password.length > 0 && form.password.length < 6}
							<p class="mt-1 text-xs text-amber-600 dark:text-amber-400">
								{6 - form.password.length} znakov este
							</p>
						{/if}
					</div>
					<div>
						<label
							for="confirmPassword"
							class="block text-sm font-medium text-gray-700 dark:text-gray-300"
						>
							Potvrdte heslo *
						</label>
						<div class="relative">
							<input
								type="password"
								id="confirmPassword"
								bind:value={form.confirmPassword}
								onblur={() => onBlur('confirmPassword')}
								class="mt-1 block w-full rounded-lg border bg-white pr-10 shadow-sm transition-colors focus:ring-2 dark:bg-[#2c2c2e] dark:text-white
								{hasError('confirmPassword')
									? 'border-red-500 focus:border-red-500 focus:ring-red-200'
									: isValid('confirmPassword')
										? 'border-green-500 focus:border-green-500 focus:ring-green-200'
										: 'border-gray-300 focus:border-indigo-500 focus:ring-indigo-200 dark:border-[#3a3a3c]'}"
							/>
							{#if isValid('confirmPassword')}
								<span class="absolute top-1/2 right-3 -translate-y-1/2 text-green-500">&#10003;</span
								>
							{/if}
						</div>
						{#if hasError('confirmPassword')}
							<p class="mt-1 text-xs text-red-600 dark:text-red-400">{errors.confirmPassword}</p>
						{/if}
					</div>
				</div>

				<button
					type="submit"
					disabled={loading || success || !formValid}
					class="w-full rounded-full bg-indigo-600 px-4 py-3 text-sm font-medium text-white transition-colors hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50 dark:focus:ring-offset-[#1c1c1e]"
				>
					{loading ? 'Registrujem...' : 'Zaregistrovat sa'}
				</button>
			</form>

			<div class="mt-6 text-center">
				<p class="text-sm text-gray-600 dark:text-gray-400">
					Uz mate ucet?
					<a
						href="/login"
						class="font-medium text-indigo-600 hover:text-indigo-500 dark:text-indigo-400"
					>
						Prihlaste sa
					</a>
				</p>
			</div>
		</div>
	</div>
</div>
