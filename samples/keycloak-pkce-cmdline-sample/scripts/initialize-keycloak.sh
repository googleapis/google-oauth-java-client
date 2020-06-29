#!/bin/sh
# Copyright 2020 Google LLC
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.


#Start keycloak server before running this script:
# docker run -p 8080:8080 -e KEYCLOAK_USER=admin -e KEYCLOAK_PASSWORD=admin quay.io/keycloak/keycloak:10.0.1

# The following script will create a new public client in the running keycloak server
# in which PKCE is required for obtaining an authorization token via the authorization
# code flow. Once this script has been run, the PKCESample.java sample application can
# be run.

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
 -d '{ "clientId": "pkce-test-client", "publicClient": true, "redirectUris": ["http://127.0.0.1*"], "attributes": {"pkce.code.challenge.method": "S256"} }' \
 -H "Content-Type:application/json" \
 -H "Authorization: bearer ${TKN}" 

 