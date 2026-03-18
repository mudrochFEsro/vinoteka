<script lang="ts">
	import { goto } from '$app/navigation';
	import { authStore } from '$lib/stores/auth.svelte';

	let username = $state('');
	let email = $state('');
	let password = $state('');
	let confirmPassword = $state('');
	let firstName = $state('');
	let lastName = $state('');
	let loading = $state(false);
	let error = $state<string | null>(null);
	let success = $state(false);

	async function handleSubmit(e: Event) {
		e.preventDefault();
		loading = true;
		error = null;

		if (password !== confirmPassword) {
			error = 'Heslá sa nezhodujú';
			loading = false;
			return;
		}

		if (password.length < 6) {
			error = 'Heslo musí mať aspoň 6 znakov';
			loading = false;
			return;
		}

		const result = await authStore.register(username, email, password, firstName, lastName);

		if (result.success) {
			success = true;
			// Auto-login after registration
			const loginResult = await authStore.login(username, password);
			if (loginResult.success) {
				setTimeout(() => goto('/'), 1500);
			}
		} else {
			error = result.error || 'Registrácia zlyhala';
		}

		loading = false;
	}
</script>

<div class="flex min-h-[80vh] items-center justify-center py-8">
	<div class="w-full max-w-md">
		<div class="rounded-lg bg-white px-8 py-10 shadow-lg">
			<h1 class="mb-8 text-center text-2xl font-bold text-gray-900">Registrácia</h1>

			{#if success}
				<div class="mb-6 rounded-md bg-green-50 p-4">
					<p class="text-sm text-green-700">
						Registrácia úspešná! Presmerovávam na hlavnú stránku...
					</p>
				</div>
			{/if}

			{#if error}
				<div class="mb-6 rounded-md bg-red-50 p-4">
					<p class="text-sm text-red-700">{error}</p>
				</div>
			{/if}

			<form onsubmit={handleSubmit} class="space-y-5">
				<div class="grid grid-cols-2 gap-4">
					<div>
						<label for="firstName" class="block text-sm font-medium text-gray-700">Meno</label>
						<input
							type="text"
							id="firstName"
							bind:value={firstName}
							required
							class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500"
						/>
					</div>
					<div>
						<label for="lastName" class="block text-sm font-medium text-gray-700">Priezvisko</label>
						<input
							type="text"
							id="lastName"
							bind:value={lastName}
							required
							class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500"
						/>
					</div>
				</div>

				<div>
					<label for="username" class="block text-sm font-medium text-gray-700">
						Používateľské meno
					</label>
					<input
						type="text"
						id="username"
						bind:value={username}
						required
						class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500"
					/>
				</div>

				<div>
					<label for="email" class="block text-sm font-medium text-gray-700">Email</label>
					<input
						type="email"
						id="email"
						bind:value={email}
						required
						class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500"
					/>
				</div>

				<div>
					<label for="password" class="block text-sm font-medium text-gray-700">Heslo</label>
					<input
						type="password"
						id="password"
						bind:value={password}
						required
						minlength="6"
						class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500"
					/>
				</div>

				<div>
					<label for="confirmPassword" class="block text-sm font-medium text-gray-700">
						Potvrďte heslo
					</label>
					<input
						type="password"
						id="confirmPassword"
						bind:value={confirmPassword}
						required
						class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500"
					/>
				</div>

				<button
					type="submit"
					disabled={loading || success}
					class="w-full rounded-md bg-indigo-600 px-4 py-3 text-sm font-medium text-white hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:ring-offset-2 disabled:opacity-50"
				>
					{loading ? 'Registrujem...' : 'Zaregistrovať sa'}
				</button>
			</form>

			<div class="mt-6 text-center">
				<p class="text-sm text-gray-600">
					Už máte účet?
					<a href="/login" class="font-medium text-indigo-600 hover:text-indigo-500">
						Prihláste sa
					</a>
				</p>
			</div>
		</div>
	</div>
</div>
