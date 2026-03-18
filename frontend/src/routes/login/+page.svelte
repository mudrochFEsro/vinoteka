<script lang="ts">
	import { goto } from '$app/navigation';
	import { authStore } from '$lib/stores/auth.svelte';

	let username = $state('');
	let password = $state('');
	let loading = $state(false);
	let error = $state<string | null>(null);

	async function handleSubmit(e: Event) {
		e.preventDefault();
		loading = true;
		error = null;

		const result = await authStore.login(username, password);

		if (result.success) {
			goto('/');
		} else {
			error = result.error || 'Prihlásenie zlyhalo';
		}

		loading = false;
	}
</script>

<div class="flex min-h-[80vh] items-center justify-center">
	<div class="w-full max-w-md">
		<div class="rounded-2xl bg-white px-8 py-10 shadow-lg transition-colors dark:bg-[#1c1c1e]">
			<h1 class="mb-8 text-center text-2xl font-bold text-gray-900 dark:text-white">Prihlásenie</h1>

			{#if error}
				<div class="mb-6 rounded-md bg-red-50 p-4 dark:bg-red-900/20">
					<p class="text-sm text-red-700 dark:text-red-400">{error}</p>
				</div>
			{/if}

			<form onsubmit={handleSubmit} class="space-y-6">
				<div>
					<label for="username" class="block text-sm font-medium text-gray-700 dark:text-gray-300">
						Používateľské meno
					</label>
					<input
						type="text"
						id="username"
						bind:value={username}
						required
						class="mt-1 block w-full rounded-lg border-gray-300 bg-white shadow-sm focus:border-indigo-500 focus:ring-indigo-500 dark:border-[#3a3a3c] dark:bg-[#2c2c2e] dark:text-white dark:placeholder-gray-400"
						placeholder="Zadajte používateľské meno"
					/>
				</div>

				<div>
					<label for="password" class="block text-sm font-medium text-gray-700 dark:text-gray-300">Heslo</label>
					<input
						type="password"
						id="password"
						bind:value={password}
						required
						class="mt-1 block w-full rounded-lg border-gray-300 bg-white shadow-sm focus:border-indigo-500 focus:ring-indigo-500 dark:border-[#3a3a3c] dark:bg-[#2c2c2e] dark:text-white dark:placeholder-gray-400"
						placeholder="Zadajte heslo"
					/>
				</div>

				<button
					type="submit"
					disabled={loading}
					class="w-full rounded-full bg-indigo-600 px-4 py-3 text-sm font-medium text-white transition-colors hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:ring-offset-2 disabled:opacity-50 dark:focus:ring-offset-[#1c1c1e]"
				>
					{loading ? 'Prihlasujem...' : 'Prihlásiť sa'}
				</button>
			</form>

			<div class="mt-6 text-center">
				<p class="text-sm text-gray-600 dark:text-gray-400">
					Nemáte účet?
					<a href="/register" class="font-medium text-indigo-600 hover:text-indigo-500 dark:text-indigo-400">
						Zaregistrujte sa
					</a>
				</p>
			</div>

			<div class="mt-8 border-t border-gray-200 pt-6 dark:border-[#3a3a3c]">
				<p class="text-center text-xs text-gray-500 dark:text-gray-400">Testovacie účty:</p>
				<div class="mt-2 space-y-1 text-center text-xs text-gray-500 dark:text-gray-400">
					<p><span class="font-medium">Používateľ:</span> testuser / testuser</p>
					<p><span class="font-medium">Admin:</span> admin / admin</p>
				</div>
			</div>
		</div>
	</div>
</div>
