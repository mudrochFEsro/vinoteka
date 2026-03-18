<script lang="ts">
	import { Toggle } from 'melt/builders';
	import { themeStore } from '$lib/stores/theme.svelte';

	const toggle = new Toggle({
		value: () => themeStore.isDark,
		onValueChange: () => themeStore.toggle()
	});
</script>

<button
	{...toggle.trigger}
	class="relative h-8 w-14 rounded-full transition-colors duration-300 focus:outline-none focus-visible:ring-2 focus-visible:ring-offset-2 {themeStore.isDark
		? 'bg-indigo-600 focus-visible:ring-indigo-500'
		: 'bg-gray-300 focus-visible:ring-gray-400'}"
	aria-label={themeStore.isDark ? 'Prepnúť na svetlý režim' : 'Prepnúť na tmavý režim'}
>
	<span
		class="absolute top-1 left-1 flex h-6 w-6 items-center justify-center rounded-full bg-white shadow-md transition-transform duration-300 ease-in-out {themeStore.isDark
			? 'translate-x-6'
			: 'translate-x-0'}"
	>
		{#if themeStore.isDark}
			<svg class="h-4 w-4 text-indigo-600" fill="currentColor" viewBox="0 0 20 20">
				<path d="M17.293 13.293A8 8 0 016.707 2.707a8.001 8.001 0 1010.586 10.586z" />
			</svg>
		{:else}
			<svg class="h-4 w-4 text-amber-500" fill="currentColor" viewBox="0 0 20 20">
				<path
					fill-rule="evenodd"
					d="M10 2a1 1 0 011 1v1a1 1 0 11-2 0V3a1 1 0 011-1zm4 8a4 4 0 11-8 0 4 4 0 018 0zm-.464 4.95l.707.707a1 1 0 001.414-1.414l-.707-.707a1 1 0 00-1.414 1.414zm2.12-10.607a1 1 0 010 1.414l-.706.707a1 1 0 11-1.414-1.414l.707-.707a1 1 0 011.414 0zM17 11a1 1 0 100-2h-1a1 1 0 100 2h1zm-7 4a1 1 0 011 1v1a1 1 0 11-2 0v-1a1 1 0 011-1zM5.05 6.464A1 1 0 106.465 5.05l-.708-.707a1 1 0 00-1.414 1.414l.707.707zm1.414 8.486l-.707.707a1 1 0 01-1.414-1.414l.707-.707a1 1 0 011.414 1.414zM4 11a1 1 0 100-2H3a1 1 0 000 2h1z"
					clip-rule="evenodd"
				/>
			</svg>
		{/if}
	</span>
</button>
