### PaySnap — QR-Based Payment Link Generator API  
**Secure, Scalable Microservice Architecture for Modern Payment Processing**  

---

#### **Overview**  
PaySnap is a distributed system enabling secure payment link and QR code generation via Stripe. It features JWT authentication with Redis-backed token revocation, real-time webhook processing, PDF receipt generation, and role-based access control. Built with Spring Boot microservices, Eureka service discovery, and API Gateway routing.  

---

#### **Key Features**  
1. **Stripe Payment Integration**  
   - Create payment sessions with configurable TTL (default: 15 minutes).  
   - Short URLs (e.g., `http://localhost:8080/pay/{shortUrl}`) for easy sharing.  
   - Multi-currency support (USD, EUR, etc.).  

2. **QR Code Generation**  
   - Dynamically generate PNG/PDF QR codes encoding Stripe checkout URLs using ZXing.  
   - Download endpoints for PNG/PDF formats.  

3. **Real-Time Payment Tracking**  
   - Webhook listener updates payment statuses (`SUCCEED`, `EXPIRED`, `FAILED`).  
   - PostgreSQL-backed audit trails with timestamps.  

4. **JWT Authentication & Security**  
   - Redis-blacklisted tokens on logout.  
   - Role-based access control (`USER`, `ADMIN`).  

5. **Automated Receipts & Notifications**  
   - PDF receipts with order details (amount, currency, timestamp).  
   - Email delivery via JavaMailSender.  

6. **Order Management**  
   - History endpoint for user-specific payments.  
   - Admin access to all transactions.  

---

## **Microservices & Their Responsibilities**  

### **1. API Gateway (`api-gateway`)**  
- **Centralized request routing** to all microservices  
- **JWT validation** for every request  
- **Token blacklist check** (Redis) to prevent revoked token usage  
- **Public/Private endpoint filtering** (e.g., `/auth` is public, `/payments` is private)  

### **2. Discovery Server (`discovery-server`)**  
- **Service registration & discovery** via **Eureka**  

### **3. Authentication Service (`ms-auth`)**  
- **Generates and validates JWT tokens**  
- **Handles logout** by blacklisting tokens in Redis  
- **Role extraction** (USER/ADMIN) for downstream services  

### **4. User Service (`ms-user`)**  
- **User & Admin CRUD operations**  
- **Role-based access control** (e.g., only admins can delete users)  
- **Data validation** (unique username, email, phone)  
- **Password encryption** (BCrypt)  

### **5. Payment Service (`ms-payment`)**  
- **Stripe Checkout Sessions** – Creates payment links with configurable TTL  
- **QR Code Generation** – Encodes payment URLs for mobile scanning  
- **PDF Receipts** – Automatically generates downloadable receipts  
- **Short URLs** – Base62-encoded for easy sharing (e.g., `/pay/abc123`)  
- **Stripe Webhook Handling** – Real-time payment status updates (success/fail/expiry)  

---

#### **Technology Stack**  
| **Category**       | **Technologies**                                                                 |  
|--------------------|----------------------------------------------------------------------------------|  
| Core               | Java 21, Spring Boot 3.5.4, Spring Cloud 2025.0.0                               |  
| Database           | PostgreSQL (User/Payment data), Redis (Token blacklist)                         |  
| Payment Processing | Stripe SDK, Webhooks                                                             |  
| Utilities          | ZXing (QR codes), OpenPDF (receipts), JavaMailSender (notifications)            |  
| Infrastructure     | Docker, Eureka Service Discovery, Spring Cloud Gateway                           |  
| API Docs           | Springdoc OpenAPI                                                                |  

---

## **Setup & Deployment**  
### **Prerequisites**  
- Java 21  
- Docker (for PostgreSQL & Redis)  
- Stripe API keys  

### **Running Locally**  
1. **Start Infrastructure:**  
   ```bash
   docker-compose up -d postgres redis
   ```  
2. **Launch Services in Order:**  
   ```bash
   ./gradlew bootRun 
   ```  
   (Run for `discovery-server`, `api-gateway`, `ms-auth`, `ms-user`, `ms-payment`)  
3. **Access APIs:**  
   - **Swagger UI:** `http://localhost:8080/swagger-ui/index.html`  
   - **Eureka Dashboard:** `http://localhost:8761`  

---

## **Configuration**  
### **Required Environment Variables**  
| **Service**       | **Key Configurations** |  
|------------------|----------------------|  
| `ms-auth`        | `jwt.secret-key`, `spring.data.redis.host` |  
| `ms-payment`     | `stripe.secret-key`, `stripe.webhook-secret` |  
| `api-gateway`    | `eureka.client.service-url.defaultZone` |  

Example (`application.yml` snippet for `ms-payment`):  
```yaml
stripe:
  secret-key: sk_test_...
  webhook-secret: whsec_...
```

---

## **API Endpoints (via Gateway)**  
| **Endpoint**               | **Service**    | **Description**                     |  
|---------------------------|--------------|------------------------------------|  
| `POST /auth`              | `ms-auth`     | Login (returns JWT)                |  
| `POST /auth/logout`       | `ms-auth`     | Logs out (blacklists token)        |  
| `POST /user`              | `ms-user`     | Create user (self-registration)    |  
| `POST /admin`             | `ms-user`     | Create admin (requires ADMIN role) |  
| `POST /payments`          | `ms-payment`  | Create Stripe payment session      |  
| `GET /pay/{shortUrl}`     | `ms-payment`  | Redirects to Stripe checkout       |  

---

## **Why This Architecture?**  
✅ **Scalability** – Microservices can be deployed independently  
✅ **Security** – JWT validation + Redis blacklisting for secure sessions  
✅ **Reliability** – Stripe webhooks ensure payment status accuracy  
✅ **Maintainability** – Clear separation of concerns  
