curl -s -X POST http://localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{
    "email": "deadpool45@gmail.com",
    "password": "loveyou3000"
  }' | jq .
