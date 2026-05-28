TOKEN="${1:-your_jwt_token_here}"

curl -s -X GET http://localhost:8080/api/restaurants/ \
  -H "Authorization: Bearer $TOKEN" | jq .
