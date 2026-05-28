curl -s -X POST http://localhost:8080/api/auth/register \
  -H 'Content-Type: application/json' \
  -d '{
    "full_name": "Deadpool",
    "email": "deadpool45@gmail.com",
    "password": "loveyou3000"
  }' | jq .
