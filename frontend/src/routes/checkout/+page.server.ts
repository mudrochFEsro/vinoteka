import type { Actions } from './$types';
import { PUBLIC_API_URL } from '$env/static/public';

interface CheckoutItem {
	productId: number;
	quantity: number;
}

type DeliveryMethod = 'PACKETA_COURIER' | 'PACKETA_PICKUP';
type PaymentMethod = 'CASH_ON_DELIVERY';

interface CheckoutRequest {
	email: string;
	firstName: string;
	lastName: string;
	phone?: string;
	street?: string;
	houseNumber?: string;
	city?: string;
	postalCode?: string;
	country?: string;
	isCompany: boolean;
	companyName?: string;
	ico?: string;
	dic?: string;
	icDph?: string;
	deliveryMethod: DeliveryMethod;
	packetaPointId?: string;
	packetaPointName?: string;
	paymentMethod: PaymentMethod;
	items?: CheckoutItem[];
}

export const actions: Actions = {
	default: async ({ request, cookies }) => {
		try {
			const data: CheckoutRequest = await request.json();

			// Validate required fields
			if (!data.email || !data.firstName || !data.lastName) {
				return { success: false, error: 'Chybaju povinne udaje' };
			}

			// Validate delivery method
			if (!data.deliveryMethod) {
				return { success: false, error: 'Vyberte sposob dopravy' };
			}

			// Validate address for courier delivery
			if (data.deliveryMethod !== 'PACKETA_PICKUP') {
				if (!data.street || !data.houseNumber || !data.city || !data.postalCode) {
					return { success: false, error: 'Vyplnte adresu dorucenia' };
				}
			} else {
				// Validate Packeta point for pickup
				if (!data.packetaPointId) {
					return { success: false, error: 'Vyberte vydajne miesto' };
				}
			}

			// Validate payment method
			if (!data.paymentMethod) {
				return { success: false, error: 'Vyberte sposob platby' };
			}

			// Validate company fields if isCompany
			if (data.isCompany) {
				if (!data.companyName) {
					return { success: false, error: 'Nazov firmy je povinny' };
				}
				if (!data.ico || !/^\d{8}$/.test(data.ico)) {
					return { success: false, error: 'ICO musi mat 8 cislic' };
				}
			}

			// Get auth token if available
			const accessToken = cookies.get('access_token');

			// For guest users, items are required
			if (!accessToken && (!data.items || data.items.length === 0)) {
				return { success: false, error: 'Kosik je prazdny' };
			}

			// Build headers
			const headers: Record<string, string> = {
				'Content-Type': 'application/json'
			};

			if (accessToken) {
				headers['Authorization'] = `Bearer ${accessToken}`;
			}

			// Create order via backend API
			const response = await fetch(`${PUBLIC_API_URL}/orders/checkout`, {
				method: 'POST',
				headers,
				body: JSON.stringify(data)
			});

			if (!response.ok) {
				const error = await response.text();
				try {
					const parsed = JSON.parse(error);
					return { success: false, error: parsed.message || parsed.error || 'Chyba pri vytvarani objednavky' };
				} catch {
					return { success: false, error: error || 'Chyba pri vytvarani objednavky' };
				}
			}

			const order = await response.json();
			return { success: true, orderId: order.id };
		} catch (error) {
			console.error('Checkout error:', error);
			return { success: false, error: 'Chyba pri vytvarani objednavky' };
		}
	}
};
