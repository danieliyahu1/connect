# Connect - Traveler & Local Connection Platform

Connect is a microservices-based backend platform designed to facilitate connections between travelers and locals. The platform enables travelers to discover locals in their destination cities and vice versa, creating opportunities for authentic travel experiences and cultural exchanges.

## 🌍 What Connect Does

Connect is a social platform that bridges the gap between travelers and locals:

- **For Travelers**: Discover locals in your destination who can show you authentic experiences, hidden gems, and provide insider knowledge about their city
- **For Locals**: Connect with travelers visiting your city, share your local expertise, and potentially earn income by offering guided experiences
- **Smart Matching**: AI-powered discovery service that suggests relevant connections based on location, interests, and availability

## 🏗️ System Architecture

Connect follows a microservices architecture with the following components:

### Core Services

1. **API Gateway** (Port: 4003)
   - Single entry point for all client requests
   - Routes requests to appropriate microservices
   - Handles authentication and authorization
   - No database required (stateless routing)

2. **Authentication Service** (Port: 4000)
   - **Database**: `connect_auth_db` - User authentication data, refresh tokens
   - User registration and login
   - JWT token management
   - Google OAuth2 integration
   - Password-based authentication

3. **Connector Service** (Port: 4001)
   - **Database**: `connect_connector_db` - User profiles, images, social media links
   - User profile management
   - Profile photos and gallery
   - Social media links
   - Location-based profile data

4. **Trip Service** (Port: 4002)
   - **Database**: `connect_trip_db` - Trip data, travel plans
   - Trip planning and management
   - Travel dates and destinations
   - Trip discovery for matching

5. **Discovery Service** (Port: 4004)
   - **Database**: `connect_discovery_db` - Discovery preferences, matching data
   - AI-powered user matching
   - Local and traveler discovery
   - Smart recommendations

### Inter-Service Communication

Each service maintains its own database and communicates with other services via HTTP requests when needed. Services use internal endpoints for cross-service communication.

## 🚀 Quick Start Guide

### Prerequisites

- **Java 21** or later
- **Maven 3.9.9** or later
- **Spring Boot 3.5.3** or later
- **PostgreSQL**
- **Docker** (optional, for containerized deployment)

### Environment Setup

1. **Clone the repository**
   ```bash
   git clone <your-repository-url>
   cd connect
   ```

2. **Create databases for each service**
   ```bash
   createdb connect_auth_db
   createdb connect_connector_db
   createdb connect_trip_db
   createdb connect_discovery_db
   ```

3. **Set up environment variables**

   Each service requires its own environment configuration. You can use `.env` files, run configurations, or any method that injects variables into the application properties.

   **Auth Service Environment Variables:**
   ```bash
   SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/connect_auth_db
   SPRING_DATASOURCE_USERNAME=your_username
   SPRING_DATASOURCE_PASSWORD=your_password
   JWT_PRIVATE_KEY=your_private_key_here
   JWT_PUBLIC_KEY=your_public_key_here
   GOOGLE_CLIENT_ID=your_google_client_id
   GOOGLE_CLIENT_SECRET=your_google_client_secret
   GOOGLE_REDIRECT_URI=http://localhost:3000/auth/callback
   ```

   **Connector Service Environment Variables:**
   ```bash
   SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/connect_connector_db
   SPRING_DATASOURCE_USERNAME=your_username
   SPRING_DATASOURCE_PASSWORD=your_password
   JWT_PUBLIC_KEY=your_public_key_here
   CLOUDINARY_API_KEY=your_cloudinary_api_key
   CLOUDINARY_API_SECRET=your_cloudinary_api_secret
   CLOUDINARY_CLOUD_NAME=your_cloudinary_cloud_name
   ```

   **Trip Service Environment Variables:**
   ```bash
   SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/connect_trip_db
   SPRING_DATASOURCE_USERNAME=your_username
   SPRING_DATASOURCE_PASSWORD=your_password
   JWT_PUBLIC_KEY=your_public_key_here
   ```

   **Discovery Service Environment Variables:**
   ```bash
   JWT_PUBLIC_KEY=your_public_key_here
   # OpenAI/Groq configuration
   GROQ_API_KEY=your_groq_api_key
   GROQ_BASE_URL=your_groq_base_url
   GROQ_TEMPERATURE=0.7  # or your preferred value
   GROQ_MODEL=llama3-70b-8192  # or your preferred model
   # Service URLs
   TRIP_SERVICE_BASE_URL=http://localhost:4002
   CONNECTOR_SERVICE_BASE_URL=http://localhost:4001
   ```

4. **Generate JWT keys** (if you don't have them)
   ```bash
   # Generate RSA key pair for JWT signing
   openssl genrsa -out private.pem 2048
   openssl rsa -in private.pem -pubout -out public.pem
   ```

### Running the Services

#### Option 1: Run Locally

```bash
# Build all services
mvn clean install

# Run services in separate terminals
cd auth-service && mvn spring-boot:run
cd connector-service && mvn spring-boot:run
cd trip-service && mvn spring-boot:run
cd discovery-service && mvn spring-boot:run
cd api-gateway && mvn spring-boot:run
```

#### Option 2: Run with Docker

```bash
# Build and run all services
docker-compose up --build
```

## 🔌 API Endpoints

All endpoints are accessible through the API Gateway at `http://localhost:4003/api/`

### Authentication Endpoints

| Method   | Endpoint                    | Description           | Auth Required |
|----------|-----------------------------|----------------------|----------------|
| POST     | `/api/auth/register`        | Register new user     | No            |
| POST     | `/api/auth/login`           | Login with credentials| No            |
| POST     | `/api/auth/refresh`         | Refresh access token  | No            |
| POST     | `/api/auth/logout`          | Logout user           | Yes           |
| DELETE   | `/api/auth/deleteUser`      | Delete user account   | Yes           |

### Profile Management (Connector Service)

| Method   | Endpoint                                    | Description                | Auth Required |
|----------|---------------------------------------------|----------------------------|---------------|
| POST     | `/api/connectors/me`                        | Create user profile        | Yes           |
| GET      | `/api/connectors/me`                        | Get my profile             | Yes           |
| PUT      | `/api/connectors/me`                        | Update my profile          | Yes           |
| GET      | `/api/connectors/public/{userId}`           | Get public profile         | No            |
| POST     | `/api/connectors/me/gallery`                | Add profile photo          | Yes           |
| DELETE   | `/api/connectors/me/gallery/{orderIndex}`   | Delete profile photo       | Yes           |
| POST     | `/api/connectors/me/social-media`           | Add social media link      | Yes           |
| PUT      | `/api/connectors/me/social-media/{platform}`| Update social media link   | Yes           |
| DELETE   | `/api/connectors/me/social-media/{platform}`| Delete social media link   | Yes           |

### Trip Management

| Method   | Endpoint                    | Description      | Auth Required |
|----------|-----------------------------|------------------|---------------|
| POST     | `/api/trips/me`             | Create new trip  | Yes           |
| GET      | `/api/trips/me`             | Get my trips     | Yes           |
| PUT      | `/api/trips/me/{publicId}`  | Update trip      | Yes           |
| DELETE   | `/api/trips/me/{publicId}`  | Delete trip      | Yes           |

### Discovery Service

| Method   | Endpoint                            | Description         | Auth Required |
|----------|-------------------------------------|---------------------|---------------|
| GET      | `/api/discovery/public/locals`      | Discover locals     | Yes           |
| GET      | `/api/discovery/public/travelers`   | Discover travelers  | Yes           |

## 🔐 Authentication

The platform uses JWT (JSON Web Tokens) for authentication. To access protected endpoints, include the access token in the Authorization header:

```
Authorization: Bearer your-jwt-token
```

## 🖼️ Image Upload Flow

The platform uses Cloudinary for image storage:

1. Generate upload signature from Connector Service
2. Upload image to Cloudinary using the signature
3. Add image URL to user profile via Connector Service

## 🧪 Testing the API

### Using Postman
Import the provided Postman collection: `Postman/connect.postman_collection.json`

### Using curl
```bash
# Auth Service tests
cd auth-service
mvn test

# Connector Service tests
cd connector-service
mvn test
```

## 🔧 Configuration Details

### Port Configuration
- API Gateway: 4003
- Auth Service: 4000
- Connector Service: 4001
- Trip Service: 4002
- Discovery Service: 4004

## 🚀 Deployment

### Production Considerations
1. **Databases**: Use separate managed PostgreSQL instances for each service
2. **Image Storage**: Cloudinary (already configured)
3. **JWT Keys**: Store securely in environment variables
4. **API Gateway**: Consider using AWS API Gateway or similar
5. **Monitoring**: Implement logging and monitoring (ELK stack, Prometheus)
6. **Service Discovery**: Implement service discovery for inter-service communication

### Docker Deployment
```bash
# Build images
docker build -t connect-auth-service ./auth-service
docker build -t connect-connector-service ./connector-service
docker build -t connect-trip-service ./trip-service
docker build -t connect-discovery-service ./discovery-service
docker build -t connect-api-gateway ./api-gateway

# Run with docker-compose
docker-compose up -d
```

## 📚 API Documentation

1. Install Lombok plugin
2. Enable annotation processing
3. Configure Java 21 SDK

## 🆘 Support

If you encounter any issues:
- Check the API documentation at the Swagger endpoints
- Review the Postman collection for working examples
- Open an issue in the repository with detailed error information

## 🔗 Useful Resources

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)
- [JWT Documentation](https://jwt.io/introduction)
- [Cloudinary Documentation](https://cloudinary.com/documentation)
- [OpenAI API Documentation](https://platform.openai.com/docs) 