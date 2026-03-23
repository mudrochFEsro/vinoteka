#!/bin/bash
# Integration test for auth flow
# Usage: ./test-auth.sh

set -e

KEYCLOAK_URL="http://localhost:8180"
API_URL="http://localhost:8081/api"
REALM="shopapi"
CLIENT_ID="shopapi-frontend"
USERNAME="admin@admin.sk"
PASSWORD="admin"

GREEN='\033[0;32m'
RED='\033[0;31m'
NC='\033[0m'

pass() { echo -e "${GREEN}✓ $1${NC}"; }
fail() { echo -e "${RED}✗ $1${NC}"; exit 1; }

echo "=== Auth Integration Tests ==="
echo ""

# Test 1: Keycloak login
echo "Test 1: Keycloak login with valid credentials"
TOKEN_RESPONSE=$(curl -s -X POST "${KEYCLOAK_URL}/realms/${REALM}/protocol/openid-connect/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password&client_id=${CLIENT_ID}&username=${USERNAME}&password=${PASSWORD}")

ACCESS_TOKEN=$(echo $TOKEN_RESPONSE | jq -r '.access_token')
REFRESH_TOKEN=$(echo $TOKEN_RESPONSE | jq -r '.refresh_token')

if [ "$ACCESS_TOKEN" != "null" ] && [ -n "$ACCESS_TOKEN" ]; then
  pass "Got access token"
else
  fail "Failed to get access token"
fi

# Test 2: Invalid credentials
echo "Test 2: Keycloak rejects invalid credentials"
INVALID_RESPONSE=$(curl -s -w "%{http_code}" -o /dev/null -X POST "${KEYCLOAK_URL}/realms/${REALM}/protocol/openid-connect/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password&client_id=${CLIENT_ID}&username=invalid@user.com&password=wrong")

if [ "$INVALID_RESPONSE" = "401" ]; then
  pass "Invalid credentials rejected with 401"
else
  fail "Expected 401, got $INVALID_RESPONSE"
fi

# Test 3: Token refresh
echo "Test 3: Token refresh"
REFRESH_RESPONSE=$(curl -s -X POST "${KEYCLOAK_URL}/realms/${REALM}/protocol/openid-connect/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=refresh_token&client_id=${CLIENT_ID}&refresh_token=${REFRESH_TOKEN}")

NEW_TOKEN=$(echo $REFRESH_RESPONSE | jq -r '.access_token')
if [ "$NEW_TOKEN" != "null" ] && [ -n "$NEW_TOKEN" ] && [ "$NEW_TOKEN" != "$ACCESS_TOKEN" ]; then
  pass "Token refreshed successfully"
else
  fail "Failed to refresh token"
fi

# Test 4: Cart API with token
echo "Test 4: Cart API with valid token"
CART_STATUS=$(curl -s -w "%{http_code}" -o /dev/null "${API_URL}/cart" \
  -H "Authorization: Bearer ${ACCESS_TOKEN}")

if [ "$CART_STATUS" = "200" ]; then
  pass "Cart API returns 200 with valid token"
else
  fail "Cart API returned $CART_STATUS, expected 200"
fi

# Test 5: Cart API without token
echo "Test 5: Cart API without token"
CART_NOAUTH_STATUS=$(curl -s -w "%{http_code}" -o /dev/null "${API_URL}/cart")

if [ "$CART_NOAUTH_STATUS" = "401" ]; then
  pass "Cart API returns 401 without token"
else
  fail "Cart API returned $CART_NOAUTH_STATUS, expected 401"
fi

# Test 6: Orders API with token
echo "Test 6: Orders API with valid token"
ORDERS_STATUS=$(curl -s -w "%{http_code}" -o /dev/null "${API_URL}/orders" \
  -H "Authorization: Bearer ${ACCESS_TOKEN}")

if [ "$ORDERS_STATUS" = "200" ]; then
  pass "Orders API returns 200 with valid token"
else
  fail "Orders API returned $ORDERS_STATUS, expected 200"
fi

# Test 7: Products API (public)
echo "Test 7: Products API (public, no auth required)"
PRODUCTS_STATUS=$(curl -s -w "%{http_code}" -o /dev/null "${API_URL}/products")

if [ "$PRODUCTS_STATUS" = "200" ]; then
  pass "Products API returns 200 (public)"
else
  fail "Products API returned $PRODUCTS_STATUS, expected 200"
fi

# Test 8: Token issuer validation
echo "Test 8: Token has correct issuer"
# Add padding and decode (macOS compatible)
B64=$(echo $ACCESS_TOKEN | cut -d'.' -f2 | tr '_-' '/+')
PADDING=$((4 - ${#B64} % 4))
[ $PADDING -lt 4 ] && B64="${B64}$(printf '=%.0s' $(seq 1 $PADDING))"
PAYLOAD=$(echo $B64 | base64 -D 2>/dev/null || echo $B64 | base64 -d 2>/dev/null)
ISSUER=$(echo $PAYLOAD | jq -r '.iss')

if [ "$ISSUER" = "${KEYCLOAK_URL}/realms/${REALM}" ]; then
  pass "Token issuer is correct: $ISSUER"
else
  fail "Token issuer mismatch: $ISSUER"
fi

# Test 9: Admin role in token
echo "Test 9: Token contains ADMIN role"
HAS_ADMIN=$(echo $PAYLOAD | jq -r '.realm_access.roles | contains(["ADMIN"])')

if [ "$HAS_ADMIN" = "true" ]; then
  pass "Token has ADMIN role"
else
  fail "Token missing ADMIN role"
fi

echo ""
echo "=== All 9 tests passed! ==="
