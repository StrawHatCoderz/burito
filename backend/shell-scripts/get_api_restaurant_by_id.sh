TOKEN="${1:-your_jwt_token_here}"
RESTAURANT_ID="${2:-your_restaurant_id_here}"

curl -s -X GET "http://localhost:8080/api/restaurants/$RESTAURANT_ID" \
  -H "Authorization: Bearer $TOKEN" | jq .
