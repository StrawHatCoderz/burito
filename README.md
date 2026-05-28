# Burito

## Running locally

```bash
cp .env.example .env
docker compose up --build
```

## Required ports

| Port | Service      |
|------|--------------|
| 80   | NGINX (entry)|
| 3000 | Frontend     |
| 8080 | Backend      |
| 5432 | PostgreSQL   |

## Endpoints

| Method | Path                        | Auth     |
|--------|-----------------------------|----------|
| POST   | /api/auth/register          | None     |
| POST   | /api/auth/login             | None     |
| GET    | /api/me                     | Bearer   |
| GET    | /api/restaurants/           | Bearer   |
| GET    | /api/restaurants/{id}       | Bearer   |
| GET    | /api/health                 | None     |
