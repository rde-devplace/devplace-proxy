#!/bin/bash

# 환경 변수에서 값 가져오기
KEYCLOAK_URI="https://console-dev.amdp-dev.cloudzcp.io/iam"
CLIENT_ID="kube-proxy-renew"
CLIENT_SECRET="B3x4C4DARIYPVemmXghmxZZdFGhfofWN"
USERNAME="himang10"
PASSWORD="ywyi1004"
PROXY_URI="http://kube-proxy.amdp-dev.skamdp.org


# 토큰 가져오기
TOKEN_RESPONSE=$(curl -s -L -X POST \
  "$KEYCLOAK_URI/realms/amdp-dev/protocol/openid-connect/token" \
  -H 'Content-Type: application/x-www-form-urlencoded' \
  --data-urlencode "client_id=$CLIENT_ID" \
  --data-urlencode "client_secret=$CLIENT_SECRET" \
  --data-urlencode 'grant_type=password' \
  --data-urlencode 'scope=email roles profile' \
  --data-urlencode "username=$USERNAME" \
  --data-urlencode "password=$PASSWORD")

# TOKEN_RESPONSE의 JSON 값을 화면에 출력
echo "Token Response:"
echo "$TOKEN_RESPONSE" | jq
echo ""

# access_token 추출
ACCESS_TOKEN=$(echo $TOKEN_RESPONSE | jq -r .access_token)

echo $ACCESS_TOKEN

