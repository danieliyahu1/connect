# Connect - Microservices Backend

This repository contains the backend microservices for the Connect application. The project is built with Spring Boot 3.5.0 and Java 21, following microservices architecture principles. It consists of two main services: Authentication Service and Connector Service.

## 🚀 Project Overview

The Connect backend is designed to be technology-agnostic, allowing frontend developers to build user interfaces using their preferred technology stack (React, Angular, Vue.js, etc.). The services provide RESTful APIs with comprehensive documentation.

### Services

1. **Authentication Service (Port: 4000)**
   - User authentication and authorization
   - JWT token management
   - Google OAuth2 integration
   - User registration and management

2. **Connector Service (Port: 4001)**
   - [Add specific functionality of connector service]
   - [Add key features]

## 🛠️ Prerequisites

Before you begin, ensure you have the following installed:
- Java 21 or later
- Maven 3.6 or later
- PostgreSQL 12 or later
- Git
- Docker (optional, for containerized deployment)

## 📦 Installation

### 1. Clone the Repository

```bash
git clone [your-repository-url]
cd connect
```

### 2. Environment Setup

1. Create a `.env` file in the root directory with the following variables:
   ```bash
   # Database Configuration
   DB_HOST=localhost
   DB_PORT=5432
   DB_NAME=connect_db
   DB_USER=your_username
   DB_PASSWORD=your_password

   # Google OAuth Configuration
   GOOGLE_CLIENT_ID=your_google_client_id
   GOOGLE_CLIENT_SECRET=your_google_client_secret
   GOOGLE_REDIRECT_URI=your_redirect_uri

   # JWT Configuration
   JWT_SECRET=your_jwt_secret
   JWT_EXPIRATION=86400000
   ```

2. Create the PostgreSQL database:
   ```bash
   createdb connect_db
   ```

### 3. Build the Services

```bash
# Build all services
mvn clean install

# Or build individual services
cd auth-service
mvn clean install

cd ../connector-service
mvn clean install
```

### 4. Run the Services

#### Option 1: Run Locally

```bash
# Run Auth Service
cd auth-service
mvn spring-boot:run

# In a new terminal, run Connector Service
cd connector-service
mvn spring-boot:run
```

#### Option 2: Run with Docker

```bash
# Build and run all services
docker-compose up --build
```

## 📚 API Documentation

Once the services are running, you can access the API documentation:

- Auth Service: `http://localhost:4000/swagger-ui.html`
- Connector Service: `http://localhost:4001/swagger-ui.html`

## 🔑 Authentication

The services use JWT (JSON Web Tokens) for authentication. To access protected endpoints:

1. Include the JWT token in the Authorization header:
   ```
   Authorization: Bearer your-jwt-token
   ```

2. For Google OAuth2 authentication:
   - Configure your Google Cloud Console project
   - Set up the OAuth2 credentials
   - Update the environment variables with your credentials

## 🧪 Testing

Run tests for each service:

```bash
# Auth Service tests
cd auth-service
mvn test

# Connector Service tests
cd connector-service
mvn test
```

## 🔄 API Endpoints

### Auth Service (Port: 4000)

- `POST /auth/register` - Register a new user
- `POST /auth/login` - Login with credentials
- `POST /auth/refresh` - Refresh access token
- `POST /auth/logout` - Logout user
- `DELETE /auth/deleteUser` - Delete user account
- `GET /auth/getUserIdFromAccessToken` - Get user ID from token
- `GET /auth/isValidAccessToken` - Validate access token

### Connector Service (Port: 4001)

[Add connector service endpoints]

## 🛠️ Development

### Code Style

- The project uses Lombok for reducing boilerplate code
- Follow Google Java Style Guide
- Use meaningful commit messages

### IDE Setup

1. Install Lombok plugin
2. Enable annotation processing
3. Configure Java 21 SDK

## 🤝 Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📝 License

[Add your license information here]

## 📞 Support

If you encounter any issues or have questions:
- Open an issue in the repository
- Contact: [Add your contact information]

## 🔗 Useful Links

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)
- [JWT Documentation](https://jwt.io/introduction) 