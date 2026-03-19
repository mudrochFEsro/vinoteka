<script lang="ts">
	import { enhance } from '$app/forms';
	import { goto } from '$app/navigation';
	import { authStore } from '$lib/stores/auth.svelte';

	let form = $state({
		email: '',
		password: ''
	});

	let touched = $state<Record<string, boolean>>({});
	let loading = $state(false);
	let error = $state<string | null>(null);

	// Validation rules
	const validations = {
		email: () => {
			if (!form.email) return 'Email je povinny';
			if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email)) return 'Neplatny format emailu';
			return null;
		},
		password: () => {
			if (!form.password) return 'Heslo je povinne';
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

	// Check if field is valid
	function isValid(field: string): boolean {
		return touched[field] && !errors[field];
	}

	// Check if field has error
	function hasError(field: string): boolean {
		return touched[field] && !!errors[field];
	}

	// Mark field as touched on blur
	function onBlur(field: string) {
		touched[field] = true;
	}

	// Check if form is valid
	let formValid = $derived(Object.values(errors).every((e) => e === null));

	// Handle successful login - save tokens and update auth store
	function onSuccess(accessToken: string, refreshToken: string) {
		localStorage.setItem('access_token', accessToken);
		localStorage.setItem('refresh_token', refreshToken);
		// Force refresh auth state (init() skips if already initialized)
		authStore.refresh();
		goto('/');
	}
</script>

<div class="flex min-h-[80vh] items-center justify-center">
	<div class="w-full max-w-md">
		<div class="rounded-2xl bg-white px-8 py-10 shadow-lg transition-colors dark:bg-[#1c1c1e]">
			<h1 class="mb-8 text-center text-2xl font-bold text-gray-900 dark:text-white">
				Prihlasenie
			</h1>

			{#if error}
				<div class="mb-6 rounded-md bg-red-50 p-4 dark:bg-red-900/20">
					<p class="text-sm text-red-700 dark:text-red-400">{error}</p>
				</div>
			{/if}

			<form
				method="POST"
				use:enhance={() => {
					// Mark all as touched on submit
					touched = { email: true, password: true };
					if (!formValid) return;

					loading = true;
					error = null;

					return async ({ result }) => {
						loading = false;
						if (result.type === 'success') {
							const data = result.data as
								| { success?: boolean; access_token?: string; refresh_token?: string }
								| undefined;
							if (data?.success && data.access_token && data.refresh_token) {
								onSuccess(data.access_token, data.refresh_token);
							}
						} else if (result.type === 'failure') {
							const data = result.data as { error?: string } | undefined;
							error = data?.error || 'Prihlasenie zlyhalo';
						}
					};
				}}
				class="space-y-6"
			>
				<div>
					<label for="email" class="block text-sm font-medium text-gray-700 dark:text-gray-300">
						Email
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

				<div>
					<label for="password" class="block text-sm font-medium text-gray-700 dark:text-gray-300">
						Heslo
					</label>
					<div class="relative">
						<input
							type="password"
							id="password"
							name="password"
							bind:value={form.password}
							onblur={() => onBlur('password')}
							placeholder="Zadajte heslo"
							class="mt-1 block w-full rounded-lg border bg-white pr-10 shadow-sm transition-colors focus:ring-2 dark:bg-[#2c2c2e] dark:text-white dark:placeholder-gray-400
							{hasError('password')
								? 'border-red-500 focus:border-red-500 focus:ring-red-200'
								: isValid('password')
									? 'border-green-500 focus:border-green-500 focus:ring-green-200'
									: 'border-gray-300 focus:border-indigo-500 focus:ring-indigo-200 dark:border-[#3a3a3c]'}"
						/>
						{#if isValid('password')}
							<span class="absolute top-1/2 right-3 -translate-y-1/2 text-green-500">&#10003;</span>
						{/if}
					</div>
					{#if hasError('password')}
						<p class="mt-1 text-xs text-red-600 dark:text-red-400">{errors.password}</p>
					{/if}
				</div>

				<button
					type="submit"
					disabled={loading}
					class="w-full rounded-full bg-indigo-600 px-4 py-3 text-sm font-medium text-white transition-colors hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:ring-offset-2 disabled:opacity-50 dark:focus:ring-offset-[#1c1c1e]"
				>
					{loading ? 'Prihlasujem...' : 'Prihlasit sa'}
				</button>
			</form>

			<div class="mt-6 text-center">
				<p class="text-sm text-gray-600 dark:text-gray-400">
					Nemate ucet?
					<a
						href="/register"
						class="font-medium text-indigo-600 hover:text-indigo-500 dark:text-indigo-400"
					>
						Zaregistrujte sa
					</a>
				</p>
			</div>

			<div class="mt-8 border-t border-gray-200 pt-6 dark:border-[#3a3a3c]">
				<p class="text-center text-xs text-gray-500 dark:text-gray-400">Testovacie ucty:</p>
				<div class="mt-2 space-y-1 text-center text-xs text-gray-500 dark:text-gray-400">
					<p><span class="font-medium">Admin:</span> admin@admin.sk / admin</p>
				</div>
			</div>
		</div>
	</div>
</div>
