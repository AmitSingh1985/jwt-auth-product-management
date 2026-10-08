# JWT Authentication & Product Management System

A full-stack authentication and product management application built as a machine-test/full-stack assessment using **Spring Boot, Spring Security, JWT, Angular, and PostgreSQL**.

The application provides secure user registration with OTP verification, JWT-based authentication, role-based authorization, product CRUD operations, administrator-only settings, logout/token invalidation, country-based signup restriction, and Swagger/OpenAPI documentation.

---

## 1. Technology Stack

### Backend

| Technology        | Purpose                         |
| ----------------- | ------------------------------- |
| Java 17/21        | Programming language            |
| Spring Boot       | Backend framework               |
| Spring Web        | REST APIs                       |
| Spring Security   | Authentication & authorization  |
| JWT (JJWT)        | Stateless authentication tokens |
| Spring Data JPA   | Database persistence            |
| Hibernate         | ORM                             |
| PostgreSQL        | Persistent application database |
| BCrypt            | Password hashing                |
| Bean Validation   | Request validation              |
| Lombok            | Boilerplate reduction           |
| Springdoc OpenAPI | Swagger API documentation       |
| Maven             | Build and dependency management |

### Frontend

| Technology         | Purpose                               |
| ------------------ | ------------------------------------- |
| Angular 17         | Frontend framework                    |
| TypeScript         | Frontend programming language         |
| HTML5              | UI                                    |
| CSS3               | Styling                               |
| Angular HttpClient | REST API communication                |
| Angular Router     | Navigation                            |
| Angular Guards     | Protected routes                      |
| HTTP Interceptor   | Automatic JWT Bearer token attachment |

---

# 2. Architecture

The project is organized as a monorepo:

```text
jwt-auth-product-management/
│
├── backend/
│   ├── src/
│   ├── pom.xml
│   └── ...
│
├── frontend/
│   ├── src/
│   ├── package.json
│   └── ...
│
├── README.md
└── .gitignore
```

---

# 3. Prerequisites

Before running the application, install the following:

### Required

* Java 17 or higher
* Maven
* Node.js 20.x
* Angular CLI 17
* PostgreSQL
* Git

### Verify installations

```bash
java -version
mvn -version
node -v
npm -v
ng version
psql --version
```

---

# 4. Database Configuration

## PostgreSQL is required

The application uses PostgreSQL as the **persistent data store**.

Create the database:

```sql
CREATE DATABASE jwt_auth_db;
```

The application expects PostgreSQL to be available at:

```text
localhost:5432
```

Default configuration:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/jwt_auth_db
spring.datasource.username=postgres
spring.datasource.password=YOUR_PASSWORD
```

Update `application.properties` with the PostgreSQL username and password for your local machine.

Example:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/jwt_auth_db
spring.datasource.username=postgres
spring.datasource.password=YOUR_PASSWORD
spring.datasource.driver-class-name=org.postgresql.Driver

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
```

> Do not commit real database passwords to Git.

---

# 5. PostgreSQL vs In-Memory Components

The application uses both persistent and in-memory components.

## PostgreSQL — Persistent

The following application data is stored in PostgreSQL:

```text
users
products
pending_signups
```

### Users

Stores registered users, including:

* Username
* Email
* BCrypt password hash
* User type/role

### Products

Stores:

* Product ID
* Product name
* Price
* Quantity

### Pending Signups

Temporarily stores registration information while OTP verification is pending.

The password is stored as a **BCrypt hash**, not plaintext.

---

## In-Memory — Temporary

JWT blacklist information is currently maintained in memory.

The application uses:

```text
TokenBlacklistService
```

to store logged-out JWT tokens in an in-memory concurrent set.

### Important

This means:

```text
Application running
        │
        ├── JWT blacklist exists in memory
        │
        └── Application restart
                │
                └── In-memory blacklist is cleared
```

PostgreSQL remains the persistent source for users and products.

For this machine-test implementation, the in-memory blacklist is intentionally kept simple.

For a production distributed system, a shared store such as Redis or a database-backed token/session mechanism could be used.

---

# 6. High-Level Application Flow

```text
                         ┌──────────────────────┐
                         │       Angular UI     │
                         │     localhost:4200   │
                         └──────────┬───────────┘
                                    │
                                    │ HTTP / JSON
                                    ▼
                         ┌──────────────────────┐
                         │   Spring Boot API    │
                         │     localhost:8080   │
                         └──────────┬───────────┘
                                    │
                  ┌─────────────────┼─────────────────┐
                  │                 │                 │
                  ▼                 ▼                 ▼
          ┌──────────────┐  ┌──────────────┐  ┌──────────────┐
          │ Spring       │  │ JWT Security │  │ REST         │
          │ Security     │  │ Filter       │  │ Controllers  │
          └──────────────┘  └──────────────┘  └──────┬───────┘
                                                     │
                                                     ▼
                                            ┌─────────────────┐
                                            │ Service Layer   │
                                            │ Auth / Product  │
                                            │ OTP / Country   │
                                            └────────┬────────┘
                                                     │
                                                     ▼
                                            ┌─────────────────┐
                                            │ Spring Data JPA │
                                            └────────┬────────┘
                                                     │
                                                     ▼
                                            ┌─────────────────┐
                                            │   PostgreSQL    │
                                            │ jwt_auth_db     │
                                            └─────────────────┘


                 JWT Logout / Blacklist
                         │
                         ▼
              ┌────────────────────────┐
              │ TokenBlacklistService  │
              │      In Memory         │
              └────────────────────────┘
```

---

# 7. Authentication Flow

```text
                    User
                     │
                     ▼
              ┌─────────────┐
              │ Login Page  │
              └──────┬──────┘
                     │
                     │ username + password
                     ▼
              ┌─────────────┐
              │ POST /login │
              └──────┬──────┘
                     │
                     ▼
              Validate User
                     │
              ┌──────┴──────┐
              │             │
            Invalid        Valid
              │             │
              ▼             ▼
       "Login Failed"   Generate JWT
                            │
                            ▼
                     Store token in
                     Angular localStorage
                            │
                            ▼
                        Dashboard
```

---

# 8. Registration + OTP Flow

Registration is available without authentication.

```text
Login Page
    │
    │ Register here
    ▼
Signup Page
    │
    │ username
    │ email
    │ password
    │ user type
    ▼
POST /api/auth/signup/request-otp
    │
    ├── Validate request
    ├── Validate country
    ├── Check duplicate user
    ├── BCrypt password
    ├── Generate 6-digit OTP
    └── Store pending signup
            │
            ▼
       OTP returned
       in test mode
            │
            ▼
      Enter OTP
            │
            ▼
POST /api/auth/signup/verify-otp
            │
            ├── Validate OTP
            ├── Check expiration
            ├── Create User
            └── Delete pending signup
                    │
                    ▼
             Registration successful
                    │
                    ▼
                  Login
```

---

# 9. JWT Security Flow

After successful login, the backend generates a signed JWT.

The Angular application stores the token locally.

For protected API calls, the Angular HTTP interceptor automatically adds:

```text
Authorization: Bearer <JWT>
```

Example:

```text
Angular
   │
   │ GET /api/products
   │ Authorization: Bearer eyJ...
   ▼
JWT Authentication Filter
   │
   ├── Check token
   ├── Check expiration
   ├── Check blacklist
   └── Set authenticated user
          │
          ▼
      Controller
```

---

# 10. Role-Based Authorization

Two user types are supported:

```text
STANDARD_USER
ADMINISTRATOR
```

### Standard User

Can access authenticated application features such as:

* Dashboard
* Products
* Product CRUD APIs

### Administrator

Can additionally access:

* Settings

The backend enforces administrator access using Spring Security:

```text
/api/settings
        │
        ├── ADMINISTRATOR → Allowed
        │
        └── STANDARD_USER  → 403 Forbidden
```

The frontend also hides the Settings button for non-administrators, but the backend remains the authoritative security layer.

---

# 11. Logout Flow

```text
User clicks Logout
        │
        ▼
POST /api/auth/logout
        │
        ▼
JWT validated
        │
        ▼
Token added to
TokenBlacklistService
        │
        ▼
Angular clears localStorage
        │
        ▼
Redirect to Login
```

After logout, the blacklisted token cannot be used to access protected endpoints while the application instance is running.

---

# 12. Country Restriction

Signup includes country-based validation.

Restricted countries include:

```text
SY - Syria
AF - Afghanistan
IR - Iran
```

The backend attempts to determine the country using the client's IP address.

Configuration:

```properties
country.restriction.test-mode=false
```

For local development/testing, test mode can temporarily be enabled:

```properties
country.restriction.test-mode=true
```

---

# 13. OTP Configuration

Current development configuration:

```properties
otp.test-mode=true
otp.expiration-minutes=5
```

When test mode is enabled, the generated OTP is returned in the API response so that the complete registration flow can be tested without an external email/SMS provider.

For a production implementation, OTP delivery should be connected to an email/SMS provider and the OTP should not be returned in the API response.

---

# 14. Backend API Endpoints

## Authentication

| Method | Endpoint                       | Authentication |
| ------ | ------------------------------ | -------------- |
| POST   | `/api/auth/signup/request-otp` | Public         |
| POST   | `/api/auth/signup/verify-otp`  | Public         |
| POST   | `/api/auth/login`              | Public         |
| POST   | `/api/auth/logout`             | JWT Required   |

## Dashboard

| Method | Endpoint         | Authentication |
| ------ | ---------------- | -------------- |
| GET    | `/api/dashboard` | JWT Required   |

## Settings

| Method | Endpoint        | Authentication |
| ------ | --------------- | -------------- |
| GET    | `/api/settings` | Administrator  |

## Products

| Method | Endpoint             | Authentication |
| ------ | -------------------- | -------------- |
| POST   | `/api/products`      | JWT Required   |
| GET    | `/api/products`      | JWT Required   |
| GET    | `/api/products/{id}` | JWT Required   |
| PUT    | `/api/products/{id}` | JWT Required   |
| DELETE | `/api/products/{id}` | JWT Required   |

---

# 15. Backend Setup

Navigate to:

```bash
cd backend
```

Build the project:

```bash
mvn clean install
```

Run the application:

```bash
mvn spring-boot:run
```

Backend will run on:

```text
http://localhost:8080
```

---

# 16. Frontend Setup

Navigate to:

```bash
cd frontend
```

Install dependencies:

```bash
npm install
```

Run Angular:

```bash
ng serve
```

Frontend will run on:

```text
http://localhost:4200
```

---

# 17. Swagger / OpenAPI

Swagger UI is available at:

```text
http://localhost:8080/swagger-ui/index.html
```

Open Swagger and click **Authorize**.

Enter the JWT token received from the login API.

The application uses Bearer JWT authentication for protected endpoints.

---

# 18. Postman Testing

The application can also be tested using Postman.

Recommended testing sequence:

### 1. Request OTP

```text
POST /api/auth/signup/request-otp
```

### 2. Verify OTP

```text
POST /api/auth/signup/verify-otp
```

### 3. Login

```text
POST /api/auth/login
```

Copy the JWT token.

### 4. Dashboard

```text
GET /api/dashboard
Authorization: Bearer <JWT>
```

### 5. Products

Test:

```text
POST /api/products
GET /api/products
GET /api/products/{id}
PUT /api/products/{id}
DELETE /api/products/{id}
```

### 6. Settings

Test with:

* Standard User → `403 Forbidden`
* Administrator → `200 OK`

### 7. Logout

```text
POST /api/auth/logout
Authorization: Bearer <JWT>
```

After logout, try accessing Dashboard or Products again with the same token.

Expected:

```text
401 Unauthorized
```

---

# 19. Default Test Users

If the automatic `DataInitializer` is enabled, the application creates the following users when they do not already exist.

### Administrator

```text
Username: adminuser
Password: Admin@1234
Role: ADMINISTRATOR
```

### Standard User

```text
Username: standarduser
Password: Test@1234
Role: STANDARD_USER
```

Passwords are stored using BCrypt hashing.

---

# 20. Validation

The application validates:

### Username

* Required
* Minimum 8 characters
* Maximum 50 characters

### Email

* Required
* Basic email format validation

### Password

* 8–12 characters
* At least one uppercase letter
* At least one lowercase letter
* At least one number
* At least one special character

### OTP

* Exactly 6 digits
* Five-minute expiration

---

# 21. Security Features

The application implements:

* BCrypt password hashing
* JWT authentication
* JWT expiration
* JWT signature validation
* JWT role claim
* JWT blacklist after logout
* Stateless Spring Security sessions
* Role-based authorization
* Backend API protection
* Angular route guard
* Angular JWT HTTP interceptor
* Input validation
* Global exception handling
* Country-based signup restriction
* No plaintext passwords

---

# 22. Error Handling

The backend provides centralized exception handling using:

```text
GlobalExceptionHandler
```

Examples:

```text
Invalid credentials
        → 401 Login Failed

Unauthorized request
        → 401

Insufficient role
        → 403

Validation error
        → 400

Resource not found
        → 400
```

---

# 23. Frontend Route Protection

Public routes:

```text
/login
/signup
```

Protected routes:

```text
/dashboard
/products
/settings
```

Angular `authGuard` prevents unauthenticated users from accessing protected frontend routes.

The backend independently validates JWT authentication, so frontend route protection is not relied upon as the security boundary.

---

# 24. Important Configuration

Before committing the project, replace local database credentials with environment variables or another secure configuration mechanism.

Do not commit:

```text
PostgreSQL passwords
Production JWT secrets
Production OTP credentials
Email/SMS provider credentials
```

---

# 25. High-Level Feature Summary

```text
┌──────────────────────────────────────────────────────────────┐
│                    JWT AUTH SYSTEM                           │
├──────────────────────────────────────────────────────────────┤
│                                                              │
│  Registration                                                │
│     └── OTP → Country Check → BCrypt → PostgreSQL            │
│                                                              │
│  Login                                                       │
│     └── Credentials → JWT → Angular localStorage             │
│                                                              │
│  Authentication                                              │
│     └── JWT Interceptor → JWT Filter → Protected APIs        │
│                                                              │
│  Authorization                                               │
│     ├── Standard User → Products                             │
│     └── Administrator → Products + Settings                  │
│                                                              │
│  Logout                                                      │
│     └── Blacklist JWT → Clear Angular Session                │
│                                                              │
│  Product Management                                          │
│     └── Create / Read / Update / Delete → PostgreSQL         │
│                                                              │
│  API Documentation                                           │
│     └── Swagger / OpenAPI                                    │
│                                                              │
└──────────────────────────────────────────────────────────────┘
```

---

# 26. Project Structure

```text
backend/
└── src/main/java/com/amit/auth/
    ├── config/
    │   ├── SecurityConfig.java
    │   ├── PasswordConfig.java
    │   ├── OpenApiConfig.java
    │   ├── CorsConfig.java
    │   └── DataInitializer.java
    │
    ├── controller/
    │   ├── AuthController.java
    │   ├── DashboardController.java
    │   ├── SettingsController.java
    │   └── ProductController.java
    │
    ├── dto/
    │   ├── SignupRequest.java
    │   ├── SignupOtpRequest.java
    │   ├── VerifyOtpRequest.java
    │   ├── OtpResponse.java
    │   ├── LoginRequest.java
    │   ├── AuthResponse.java
    │   └── ApiResponse.java
    │
    ├── entity/
    │   ├── User.java
    │   ├── UserType.java
    │   ├── Product.java
    │   └── PendingSignup.java
    │
    ├── repository/
    │   ├── UserRepository.java
    │   ├── ProductRepository.java
    │   └── PendingSignupRepository.java
    │
    ├── security/
    │   ├── JwtService.java
    │   ├── JwtAuthenticationFilter.java
    │   └── TokenBlacklistService.java
    │
    ├── service/
    │   ├── AuthService.java
    │   ├── OtpService.java
    │   ├── ProductService.java
    │   └── CountryRestrictionService.java
    │
    └── exception/
        ├── GlobalExceptionHandler.java
        └── InvalidCredentialsException.java


frontend/
└── src/app/
    ├── auth/
    │   ├── login/
    │   └── signup/
    │
    ├── dashboard/
    ├── settings/
    ├── products/
    │
    └── core/
        ├── services/
        ├── interceptors/
        └── guards/
```

---

## 27. Running the Complete Application

Start PostgreSQL first.

Then:

```text
1. Create jwt_auth_db
2. Configure PostgreSQL credentials
3. Start Spring Boot backend
4. Start Angular frontend
5. Open http://localhost:4200
6. Register or use a seeded user
7. Login
8. Access Dashboard
9. Manage Products
10. Test Administrator Settings
11. Test Logout
```

The application is now ready for local evaluation and API testing.
