import type { Actions } from './$types';
import { fail } from '@sveltejs/kit';
import { KEYCLOAK_URL, KEYCLOAK_REALM } from '$env/static/private';

const CLIENT_ID = 'shopapi-frontend';

export const actions: Actions = {
	default: async ({ request }) => {
		const data = await request.formData();

		const email = data.get('email') as string;
		const password = data.get('password') as string;

		if (!email || !password) {
			return fail(400, { error: 'Email a heslo su povinne' });
		}

		try {
			const response = await fetch(
				`${KEYCLOAK_URL}/realms/${KEYCLOAK_REALM}/protocol/openid-connect/token`,
				{
					method: 'POST',
					headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
					body: new URLSearchParams({
						grant_type: 'password',
						client_id: CLIENT_ID,
						username: email,
						password
					})
				}
			);

			if (!response.ok) {
				return fail(401, {
					error: 'Nespravny email alebo heslo'
				});
			}

			const tokens = await response.json();

			return {
				success: true,
				access_token: tokens.access_token,
				refresh_token: tokens.refresh_token
			};
		} catch (error) {
			console.error('Login error:', error);
			return fail(500, { error: 'Nepodarilo sa pripojit k serveru' });
		}
	}
};
