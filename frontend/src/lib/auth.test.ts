import { describe, it, expect, vi, beforeEach } from 'vitest';

// Mock environment variables
vi.mock('$env/static/public', () => ({
	PUBLIC_KEYCLOAK_URL: 'http://localhost:8180',
	PUBLIC_KEYCLOAK_REALM: 'shopapi',
	PUBLIC_KEYCLOAK_CLIENT_ID: 'shopapi-frontend'
}));

describe('Auth Module', () => {
	beforeEach(async () => {
		localStorage.clear();
		vi.clearAllMocks();
		vi.resetModules();
	});

	describe('login', () => {
		it('should return success when credentials are valid', async () => {
			const { login } = await import('./auth');

			const mockTokenResponse = {
				access_token:
					'eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwicHJlZmVycmVkX3VzZXJuYW1lIjoidGVzdHVzZXIiLCJlbWFpbCI6InRlc3RAZXhhbXBsZS5jb20iLCJyZWFsbV9hY2Nlc3MiOnsicm9sZXMiOlsiVVNFUiJdfSwiZXhwIjo5OTk5OTk5OTk5fQ.signature',
				refresh_token: 'mock-refresh-token',
				expires_in: 300,
				refresh_expires_in: 1800,
				token_type: 'Bearer'
			};

			vi.mocked(fetch).mockResolvedValueOnce({
				ok: true,
				json: () => Promise.resolve(mockTokenResponse)
			} as Response);

			const result = await login('testuser', 'password');

			expect(result.success).toBe(true);
			expect(result.error).toBeUndefined();
			expect(localStorage.getItem('access_token')).toBe(mockTokenResponse.access_token);
			expect(localStorage.getItem('refresh_token')).toBe(mockTokenResponse.refresh_token);
		});

		it('should return error when credentials are invalid', async () => {
			const { login } = await import('./auth');

			vi.mocked(fetch).mockResolvedValueOnce({
				ok: false,
				json: () => Promise.resolve({ error_description: 'Invalid credentials' })
			} as Response);

			const result = await login('testuser', 'wrongpassword');

			expect(result.success).toBe(false);
			expect(result.error).toBe('Invalid credentials');
		});

		it('should return error when server is unreachable', async () => {
			const { login } = await import('./auth');

			vi.mocked(fetch).mockRejectedValueOnce(new Error('Network error'));

			const result = await login('testuser', 'password');

			expect(result.success).toBe(false);
			expect(result.error).toBe('Nepodarilo sa pripojiť k serveru');
		});
	});

	describe('isAuthenticated', () => {
		it('should return false when no token is stored', async () => {
			const { isAuthenticated, initAuth } = await import('./auth');
			initAuth();
			expect(isAuthenticated()).toBe(false);
		});

		it('should return true after successful login', async () => {
			const { login, isAuthenticated } = await import('./auth');

			const mockTokenResponse = {
				access_token:
					'eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwicHJlZmVycmVkX3VzZXJuYW1lIjoidGVzdHVzZXIiLCJlbWFpbCI6InRlc3RAZXhhbXBsZS5jb20iLCJyZWFsbV9hY2Nlc3MiOnsicm9sZXMiOlsiVVNFUiJdfSwiZXhwIjo5OTk5OTk5OTk5fQ.signature',
				refresh_token: 'mock-refresh-token',
				expires_in: 300,
				refresh_expires_in: 1800,
				token_type: 'Bearer'
			};

			vi.mocked(fetch).mockResolvedValueOnce({
				ok: true,
				json: () => Promise.resolve(mockTokenResponse)
			} as Response);

			await login('testuser', 'password');
			expect(isAuthenticated()).toBe(true);
		});
	});

	describe('getToken', () => {
		it('should return null when not authenticated', async () => {
			const { getToken, initAuth } = await import('./auth');
			initAuth();
			expect(getToken()).toBeNull();
		});

		it('should return token after login', async () => {
			const { login, getToken } = await import('./auth');

			const mockTokenResponse = {
				access_token:
					'eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwicHJlZmVycmVkX3VzZXJuYW1lIjoidGVzdHVzZXIiLCJlbWFpbCI6InRlc3RAZXhhbXBsZS5jb20iLCJyZWFsbV9hY2Nlc3MiOnsicm9sZXMiOlsiVVNFUiJdfSwiZXhwIjo5OTk5OTk5OTk5fQ.signature',
				refresh_token: 'mock-refresh-token',
				expires_in: 300,
				refresh_expires_in: 1800,
				token_type: 'Bearer'
			};

			vi.mocked(fetch).mockResolvedValueOnce({
				ok: true,
				json: () => Promise.resolve(mockTokenResponse)
			} as Response);

			await login('testuser', 'password');
			expect(getToken()).toBe(mockTokenResponse.access_token);
		});
	});

	describe('hasRole', () => {
		it('should return false when not authenticated', async () => {
			const { hasRole, initAuth } = await import('./auth');
			initAuth();
			expect(hasRole('USER')).toBe(false);
			expect(hasRole('ADMIN')).toBe(false);
		});

		it('should return true when user has role', async () => {
			const { login, hasRole } = await import('./auth');

			const mockTokenResponse = {
				access_token:
					'eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwicHJlZmVycmVkX3VzZXJuYW1lIjoidGVzdHVzZXIiLCJlbWFpbCI6InRlc3RAZXhhbXBsZS5jb20iLCJyZWFsbV9hY2Nlc3MiOnsicm9sZXMiOlsiVVNFUiJdfSwiZXhwIjo5OTk5OTk5OTk5fQ.signature',
				refresh_token: 'mock-refresh-token',
				expires_in: 300,
				refresh_expires_in: 1800,
				token_type: 'Bearer'
			};

			vi.mocked(fetch).mockResolvedValueOnce({
				ok: true,
				json: () => Promise.resolve(mockTokenResponse)
			} as Response);

			await login('testuser', 'password');
			expect(hasRole('USER')).toBe(true);
			expect(hasRole('ADMIN')).toBe(false);
		});
	});
});
