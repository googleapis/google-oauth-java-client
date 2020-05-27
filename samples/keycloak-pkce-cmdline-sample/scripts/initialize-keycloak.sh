#!/bin/sh

KEYCLOAK_BASE_URL="http://localhost:8080/auth"
KEYCLOAK_REALM="master"
KEYCLOAK_URL="${KEYCLOAK_BASE_URL}/realms/${KEYCLOAK_REALM}"

KEYCLOAK_CLIENT_ID="admin"
KEYCLOAK_CLIENT_SECRET="admin"

export TKN=$(curl -s -X POST "${KEYCLOAK_URL}/protocol/openid-connect/token" \
 -H "Content-Type: application/x-www-form-urlencoded" \
 -d "username=${KEYCLOAK_CLIENT_ID}" \
 -d "password=${KEYCLOAK_CLIENT_SECRET}" \
 -d 'grant_type=password' \
 -d 'client_id=admin-cli' | jq -r '.access_token')

curl -s -X POST "${KEYCLOAK_URL}/clients-registrations/default" \
 -d '{ "clientId": "myclient", "redirectUris": ["http://localhost*"], "attributes": {"pkce.code.challenge.method": "S256"} }' \
 -H "Content-Type:application/json" \
 -H "Authorization: bearer ${TKN}" 

 