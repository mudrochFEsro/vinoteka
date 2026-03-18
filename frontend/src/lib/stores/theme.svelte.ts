const THEME_KEY = 'theme';

type Theme = 'light' | 'dark';

function createThemeStore() {
	let theme = $state<Theme>('light');
	let initialized = $state(false);

	function init() {
		if (typeof window === 'undefined') return;

		const stored = localStorage.getItem(THEME_KEY) as Theme | null;
		const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches;

		theme = stored ?? (prefersDark ? 'dark' : 'light');
		applyTheme();
		initialized = true;
	}

	function applyTheme() {
		if (typeof document === 'undefined') return;
		document.documentElement.classList.toggle('dark', theme === 'dark');
	}

	function toggle() {
		theme = theme === 'light' ? 'dark' : 'light';
		localStorage.setItem(THEME_KEY, theme);
		applyTheme();
	}

	function set(newTheme: Theme) {
		theme = newTheme;
		localStorage.setItem(THEME_KEY, theme);
		applyTheme();
	}

	return {
		get theme() {
			return theme;
		},
		get isDark() {
			return theme === 'dark';
		},
		get initialized() {
			return initialized;
		},
		init,
		toggle,
		set
	};
}

export const themeStore = createThemeStore();
