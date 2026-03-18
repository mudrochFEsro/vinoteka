# Vinoteka - Wine E-commerce Application

Full-stack e-commerce application for a wine shop built with Spring Boot and SvelteKit.

## Tech Stack

### Backend
- **Java 21** + **Spring Boot 3.5**
- **PostgreSQL 16** - database
- **Flyway** - database migrations
- **Keycloak 25** - authentication (OAuth2/JWT)
- **Spring Security** - authorization with role-based access control
- **Lombok** - reduce boilerplate code
- **SpringDoc OpenAPI** - API documentation (Swagger UI)

### Frontend
- **SvelteKit 2** + **Svelte 5** (with Runes)
- **TypeScript**
- **Tailwind CSS 4**
- **Vite 7**

### Infrastructure
- **Docker** - containerization
- **Docker Compose** - local development

## Project Structure

```
webshop/
├── backend/                    # Spring Boot application
│   ├── src/main/java/
│   │   └── com/shopapi/backend/
│   │       ├── config/         # Security, CORS configuration
│   │       ├── controller/     # REST API endpoints
│   │       ├── dto/            # Data Transfer Objects
│   │       ├── entity/         # JPA entities
│   │       ├── exception/      # Custom exceptions
│   │       ├── repository/     # Spring Data JPA repositories
│   │       └── service/        # Business logic
│   ├── src/main/resources/
│   │   ├── db/migration/       # Flyway SQL migrations
│   │   └── application.yml     # Configuration
│   └── uploads/                # Uploaded product images
│
├── frontend/                   # SvelteKit application
│   ├── src/
│   │   ├── lib/
│   │   │   ├── components/     # Svelte components
│   │   │   ├── stores/         # Svelte 5 reactive stores
│   │   │   ├── api.ts          # API client
│   │   │   └── auth.ts         # Keycloak authentication
│   │   └── routes/             # SvelteKit pages
│   └── static/                 # Static assets
│
└── docker-compose.yml          # Docker services
```

## Features

### Customer Features
- Browse wine catalog with filtering by category and search
- View product details with images
- Shopping cart management
- Place orders
- View order history

### Admin Features
- Product management (CRUD)
- Image upload for products
- Order management with status updates
- View all orders

## Getting Started

### Prerequisites
- Docker & Docker Compose
- Java 21 (for local development)
- Node.js 20+ with pnpm

### 1. Start Infrastructure

```bash
docker-compose up -d
```

This starts:
- PostgreSQL on port 5432
- Keycloak on port 8180

### 2. Start Backend

```bash
cd backend
./mvnw spring-boot:run
```

Backend runs on http://localhost:8081

### 3. Start Frontend

```bash
cd frontend
pnpm install
pnpm dev
```

Frontend runs on http://localhost:5173

## API Documentation

Swagger UI available at: http://localhost:8081/swagger-ui.html

### Main Endpoints

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | `/api/products` | List products | Public |
| GET | `/api/products/{id}` | Get product | Public |
| POST | `/api/products` | Create product | Admin |
| PUT | `/api/products/{id}` | Update product | Admin |
| DELETE | `/api/products/{id}` | Delete product | Admin |
| GET | `/api/categories` | List categories | Public |
| GET | `/api/cart` | Get user's cart | User |
| POST | `/api/cart/items` | Add to cart | User |
| PUT | `/api/cart/items/{id}` | Update cart item | User |
| DELETE | `/api/cart/items/{id}` | Remove from cart | User |
| GET | `/api/orders` | Get user's orders | User |
| POST | `/api/orders` | Create order | User |
| GET | `/api/admin/orders` | Get all orders | Admin |
| PUT | `/api/admin/orders/{id}/status` | Update order status | Admin |
| POST | `/api/files/upload` | Upload image | Admin |
| GET | `/api/files/{filename}` | Get image | Public |

## Authentication

Using Keycloak with direct access grants (password grant).

### Default Users

| Username | Password | Role |
|----------|----------|------|
| admin | admin | ADMIN |
| testuser | testuser | USER |

### Keycloak Admin Console
http://localhost:8180/admin (admin/admin)

## Testing

### Backend Tests
```bash
cd backend
./mvnw test
```

### Frontend Tests
```bash
cd frontend
pnpm test:run
```

## Environment Variables

### Backend (application.yml)
- `SPRING_DATASOURCE_URL` - PostgreSQL connection URL
- `SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI` - Keycloak issuer

### Frontend (.env)
- `PUBLIC_API_URL` - Backend API URL
- `PUBLIC_KEYCLOAK_URL` - Keycloak URL
- `PUBLIC_KEYCLOAK_REALM` - Keycloak realm
- `PUBLIC_KEYCLOAK_CLIENT_ID` - Keycloak client ID

## Docker Deployment

### Build Images

```bash
# Backend
cd backend
docker build -t webshop-backend .

# Frontend
cd frontend
docker build -t webshop-frontend .
```

### Run with Docker Compose

```bash
docker-compose -f docker-compose.prod.yml up -d
```

## Database Schema

### Tables
- `users` - User accounts (linked to Keycloak)
- `categories` - Wine categories
- `products` - Wine products
- `carts` - Shopping carts
- `cart_items` - Cart items
- `orders` - Customer orders
- `order_items` - Order items

## License

MIT
