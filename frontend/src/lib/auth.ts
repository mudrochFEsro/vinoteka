import {
	PUBLIC_KEYCLOAK_URL,
	PUBLIC_KEYCLOAK_REALM,
	PUBLIC_KEYCLOAK_CLIENT_ID
} from '$env/static/public';

const KEYCLOAK_URL = PUBLIC_KEYCLOAK_URL;
const REALM = PUBLIC_KEYCLOAK_REALM;
const CLIENT_ID = PUBLIC_KEYCLOAK_CLIENT_ID;

interface TokenResponse {
	access_token: string;
	refresh_token: string;
	expires_in: number;
	refresh_expires_in: number;
	token_type: string;
}

interface TokenPayload {
	sub: string;
	preferred_username: string;
	email: string;
	realm_access?: { roles: string[] };
	resource_access?: { [key: string]: { roles: string[] } };
	exp: number;
}

let accessToken: string | null = null;
let refreshToken: string | null = null;
let tokenPayload: TokenPayload | null = null;

function parseJwt(token: string): TokenPayload {
	const base64Url = token.split('.')[1];
	const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
	const jsonPayload = decodeURIComponent(
		atob(base64)
			.split('')
			.map((c) => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
			.join('')
	);
	return JSON.parse(jsonPayload);
}

function saveTokens(tokens: TokenResponse) {
	accessToken = tokens.access_token;
	refreshToken = tokens.refresh_token;
	tokenPayload = parseJwt(tokens.access_token);

	localStorage.setItem('access_token', tokens.access_token);
	localStorage.setItem('refresh_token', tokens.refresh_token);
}

function clearTokens() {
	accessToken = null;
	refreshToken = null;
	tokenPayload = null;
	localStorage.removeItem('access_token');
	localStorage.removeItem('refresh_token');
}

export function initAuth(): boolean {
	const storedAccessToken = localStorage.getItem('access_token');
	const storedRefreshToken = localStorage.getItem('refresh_token');

	if (storedAccessToken && storedRefreshToken) {
		try {
			const payload = parseJwt(storedAccessToken);
			// Check if token is expired (with 30 second buffer)
			if (payload.exp * 1000 > Date.now() + 30000) {
				accessToken = storedAccessToken;
				refreshToken = storedRefreshToken;
				tokenPayload = payload;
				return true;
			} else {
				// Token expired, try to refresh
				refreshTokenAsync();
			}
		} catch {
			clearTokens();
		}
	}
	return false;
}

export async function login(username: string, password: string): Promise<{ success: boolean; error?: string }> {
	try {
		const response = await fetch(`${KEYCLOAK_URL}/realms/${REALM}/protocol/openid-connect/token`, {
			method: 'POST',
			headers: {
				'Content-Type': 'application/x-www-form-urlencoded'
			},
			body: new URLSearchParams({
				grant_type: 'password',
				client_id: CLIENT_ID,
				username,
				password
			})
		});

		if (!response.ok) {
			const error = await response.json();
			return {
				success: false,
				error: error.error_description || 'Neplatné prihlasovacie údaje'
			};
		}

		const tokens: TokenResponse = await response.json();
		saveTokens(tokens);
		return { success: true };
	} catch (error) {
		return {
			success: false,
			error: 'Nepodarilo sa pripojiť k serveru'
		};
	}
}

export async function register(
	email: string,
	password: string,
	firstName: string,
	lastName: string
): Promise<{ success: boolean; error?: string }> {
	try {
		// First, get admin token
		const adminTokenResponse = await fetch(`${KEYCLOAK_URL}/realms/master/protocol/openid-connect/token`, {
			method: 'POST',
			headers: {
				'Content-Type': 'application/x-www-form-urlencoded'
			},
			body: new URLSearchParams({
				grant_type: 'password',
				client_id: 'admin-cli',
				username: 'admin',
				password: 'admin'
			})
		});

		if (!adminTokenResponse.ok) {
			return { success: false, error: 'Registrácia nie je dostupná' };
		}

		const adminTokens = await adminTokenResponse.json();

		// Create user - use email as username
		const createUserResponse = await fetch(
			`${KEYCLOAK_URL}/admin/realms/${REALM}/users`,
			{
				method: 'POST',
				headers: {
					'Content-Type': 'application/json',
					'Authorization': `Bearer ${adminTokens.access_token}`
				},
				body: JSON.stringify({
					username: email,
					email,
					firstName,
					lastName,
					enabled: true,
					emailVerified: true,
					credentials: [{
						type: 'password',
						value: password,
						temporary: false
					}],
					realmRoles: ['USER']
				})
			}
		);

		if (!createUserResponse.ok) {
			if (createUserResponse.status === 409) {
				return { success: false, error: 'Používateľ s týmto emailom už existuje' };
			}
			const error = await createUserResponse.json();
			return { success: false, error: error.errorMessage || 'Registrácia zlyhala' };
		}

		// Get user ID to assign role
		const usersResponse = await fetch(
			`${KEYCLOAK_URL}/admin/realms/${REALM}/users?email=${encodeURIComponent(email)}`,
			{
				headers: {
					'Authorization': `Bearer ${adminTokens.access_token}`
				}
			}
		);

		if (usersResponse.ok) {
			const users = await usersResponse.json();
			if (users.length > 0) {
				const userId = users[0].id;

				// Get USER role
				const rolesResponse = await fetch(
					`${KEYCLOAK_URL}/admin/realms/${REALM}/roles/USER`,
					{
						headers: {
							'Authorization': `Bearer ${adminTokens.access_token}`
						}
					}
				);

				if (rolesResponse.ok) {
					const userRole = await rolesResponse.json();

					// Assign role to user
					await fetch(
						`${KEYCLOAK_URL}/admin/realms/${REALM}/users/${userId}/role-mappings/realm`,
						{
							method: 'POST',
							headers: {
								'Content-Type': 'application/json',
								'Authorization': `Bearer ${adminTokens.access_token}`
							},
							body: JSON.stringify([userRole])
						}
					);
				}
			}
		}

		return { success: true };
	} catch (error) {
		return {
			success: false,
			error: 'Nepodarilo sa pripojiť k serveru'
		};
	}
}

export async function refreshTokenAsync(): Promise<boolean> {
	const storedRefreshToken = refreshToken || localStorage.getItem('refresh_token');
	if (!storedRefreshToken) return false;

	try {
		const response = await fetch(`${KEYCLOAK_URL}/realms/${REALM}/protocol/openid-connect/token`, {
			method: 'POST',
			headers: {
				'Content-Type': 'application/x-www-form-urlencoded'
			},
			body: new URLSearchParams({
				grant_type: 'refresh_token',
				client_id: CLIENT_ID,
				refresh_token: storedRefreshToken
			})
		});

		if (!response.ok) {
			clearTokens();
			return false;
		}

		const tokens: TokenResponse = await response.json();
		saveTokens(tokens);
		return true;
	} catch {
		clearTokens();
		return false;
	}
}

export function logout() {
	clearTokens();
	window.location.href = '/';
}

export function getToken(): string | null {
	return accessToken;
}

export function isAuthenticated(): boolean {
	if (!tokenPayload) return false;
	return tokenPayload.exp * 1000 > Date.now();
}

export function getUsername(): string | undefined {
	return tokenPayload?.preferred_username;
}

export function getEmail(): string | undefined {
	return tokenPayload?.email;
}

export function hasRole(role: string): boolean {
	if (!tokenPayload) return false;

	const realmRoles = tokenPayload.realm_access?.roles || [];
	const clientRoles = tokenPayload.resource_access?.[CLIENT_ID]?.roles || [];

	return realmRoles.includes(role) || clientRoles.includes(role);
}

export function isAdmin(): boolean {
	return hasRole('ADMIN') || hasRole('admin');
}
