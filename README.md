# ShopSphere 🛒

ShopSphere is a full-stack e-commerce project I built to practice and improve my skills in Java backend development and frontend development.

The application allows users to browse products, search by category, add items to their cart, place orders, and check their order details. It also includes admin functionality for managing products.

## Tech Stack

- **Frontend:** React.js, Vite, JavaScript, CSS
- **Backend:** Java 21, Spring Boot
- **Database:** MySQL
- **Testing:** JUnit, Spring Boot Test, H2
- **Tools:** Maven, Git, GitHub, Docker

## What I've Built

- Product listing with search and category filters
- Shopping cart with quantity management
- Cart data saved in localStorage
- Checkout and order placement
- Order tracking using private access tokens
- Admin authentication for product management
- REST APIs connecting the frontend and backend
- Input validation and error handling
- Database handling to prevent stock from being oversold

## Project Structure

```text
ShopSphere/
├── backend/
│   ├── src/
│   │   ├── main/
│   │   └── test/
│   └── pom.xml
├── frontend/
│   ├── src/
│   ├── package.json
│   └── vite.config.js
├── docker-compose.yml
├── README.md
└── VERIFICATION.md
```

## Run the Project Locally

**Prerequisites:** Java 21+, Maven, Node.js, npm and MySQL (or Docker).

**1. Clone the repository**

```bash
git clone https://github.com/Mahaboobbasha3285/StoreSphere.git
cd StoreSphere
```

**2. Start MySQL**

```bash
docker compose up -d
```

**3. Configure admin credentials**

Set `ADMIN_USERNAME` and `ADMIN_PASSWORD` as environment variables before starting the backend. Use your own strong password.

**4. Start the backend**

```bash
cd backend
mvn test
mvn spring-boot:run
```

**5. Start the frontend**

Open another terminal:

```bash
cd frontend
npm install
npm run dev
```

**6. Open the application**

Visit http://localhost:5173

Backend API runs at http://localhost:8080/api.

## A Few Things I Learned

While building ShopSphere, I got practical experience with connecting React to Spring Boot APIs, working with MySQL, handling checkout operations, implementing authentication, validating requests, and writing backend integration tests.

I also worked on handling stock updates safely when multiple orders are placed at the same time.

## Future Improvements

There are still things I'd like to improve, including payment gateway integration, better authentication, and deployment.

## Project Status

The application has been built and tested locally. The backend integration tests passed, and both the frontend and backend were started successfully during local testing.

---

**Built by Rasta Mahaboob Basha**

GitHub: [Mahaboobbasha3285](https://github.com/Mahaboobbasha3285)
