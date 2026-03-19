import {
	initAuth,
	isAuthenticated,
	isAdmin,
	getUsername,
	login as authLogin,
	register as authRegister,
	logout as authLogout,
	refreshTokenAsync
} from '$lib/auth';

class AuthStore {
	authenticated = $state(false);
	username = $state<string | undefined>(undefined);
	admin = $state(false);
	initialized = $state(false);

	init() {
		if (this.initialized) return;
		const authenticated = initAuth();
		this.update();
		this.initialized = true;

		// Auto-refresh token every 4 minutes
		if (authenticated) {
			setInterval(async () => {
				if (this.authenticated) {
					await refreshTokenAsync();
					this.update();
				}
			}, 4 * 60 * 1000);
		}
	}

	update() {
		this.authenticated = isAuthenticated();
		this.username = getUsername();
		this.admin = isAdmin();
	}

	// Force refresh from localStorage (used after server-side login)
	refresh() {
		initAuth();
		this.update();
		this.initialized = true;
	}

	async login(username: string, password: string): Promise<{ success: boolean; error?: string }> {
		const result = await authLogin(username, password);
		if (result.success) {
			this.update();
		}
		return result;
	}

	async register(
		email: string,
		password: string,
		firstName: string,
		lastName: string
	): Promise<{ success: boolean; error?: string }> {
		return authRegister(email, password, firstName, lastName);
	}

	logout() {
		authLogout();
		this.authenticated = false;
		this.username = undefined;
		this.admin = false;
	}
}

export const authStore = new AuthStore();
