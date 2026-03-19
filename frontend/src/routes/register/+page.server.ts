import type { Actions } from './$types';
import { fail } from '@sveltejs/kit';
import { KEYCLOAK_URL, KEYCLOAK_REALM } from '$env/static/private';

export const actions: Actions = {
	default: async ({ request }) => {
		const data = await request.formData();

		const email = data.get('email') as string;
		const password = data.get('password') as string;
		const firstName = data.get('firstName') as string;
		const lastName = data.get('lastName') as string;

		// Server-side validation
		if (!email || !password || !firstName || !lastName) {
			return fail(400, { error: 'Všetky polia sú povinné' });
		}

		if (password.length < 6) {
			return fail(400, { error: 'Heslo musí mať aspoň 6 znakov' });
		}

		try {
			// Get admin token
			const adminTokenResponse = await fetch(
				`${KEYCLOAK_URL}/realms/master/protocol/openid-connect/token`,
				{
					method: 'POST',
					headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
					body: new URLSearchParams({
						grant_type: 'password',
						client_id: 'admin-cli',
						username: 'admin',
						password: 'admin'
					})
				}
			);

			if (!adminTokenResponse.ok) {
				return fail(503, { error: 'Registrácia nie je dostupná' });
			}

			const adminTokens = await adminTokenResponse.json();

			// Create user in Keycloak
			const createUserResponse = await fetch(
				`${KEYCLOAK_URL}/admin/realms/${KEYCLOAK_REALM}/users`,
				{
					method: 'POST',
					headers: {
						'Content-Type': 'application/json',
						Authorization: `Bearer ${adminTokens.access_token}`
					},
					body: JSON.stringify({
						username: email,
						email,
						firstName,
						lastName,
						enabled: true,
						emailVerified: true,
						credentials: [{ type: 'password', value: password, temporary: false }]
					})
				}
			);

			if (!createUserResponse.ok) {
				if (createUserResponse.status === 409) {
					return fail(409, { error: 'Používateľ s týmto emailom už existuje' });
				}
				const error = await createUserResponse.json();
				return fail(400, { error: error.errorMessage || 'Registrácia zlyhala' });
			}

			// Assign USER role
			const usersResponse = await fetch(
				`${KEYCLOAK_URL}/admin/realms/${KEYCLOAK_REALM}/users?email=${encodeURIComponent(email)}`,
				{ headers: { Authorization: `Bearer ${adminTokens.access_token}` } }
			);

			if (usersResponse.ok) {
				const users = await usersResponse.json();
				if (users.length > 0) {
					const userId = users[0].id;

					const rolesResponse = await fetch(
						`${KEYCLOAK_URL}/admin/realms/${KEYCLOAK_REALM}/roles/USER`,
						{ headers: { Authorization: `Bearer ${adminTokens.access_token}` } }
					);

					if (rolesResponse.ok) {
						const userRole = await rolesResponse.json();
						await fetch(
							`${KEYCLOAK_URL}/admin/realms/${KEYCLOAK_REALM}/users/${userId}/role-mappings/realm`,
							{
								method: 'POST',
								headers: {
									'Content-Type': 'application/json',
									Authorization: `Bearer ${adminTokens.access_token}`
								},
								body: JSON.stringify([userRole])
							}
						);
					}
				}
			}

			return { success: true, email };
		} catch (error) {
			console.error('Registration error:', error);
			return fail(500, { error: 'Nepodarilo sa pripojiť k serveru' });
		}
	}
};
