import { env } from '$env/dynamic/public';

export const API_URL = env.PUBLIC_API_URL || 'http://localhost:8081/api';
export const KEYCLOAK_URL = env.PUBLIC_KEYCLOAK_URL || 'http://localhost:8180';
