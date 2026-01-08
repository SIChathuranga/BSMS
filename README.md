# 🏍️ BSMS - Motorbike Spare Parts Management System

A modern, full-stack e-commerce platform for motorbike spare parts built with Spring Boot, Firestore, and a responsive Vanilla JS frontend.

![BSMS Hero](https://images.unsplash.com/photo-1558981806-ec527fa84f3d?ixlib=rb-4.0.3&auto=format&fit=crop&w=1200&q=80)

## ✨ Features

### For Customers
- 🔍 **Smart Search & Filtering** - Find parts by category, brand, or price range
- 🛒 **Dynamic Shopping Cart** - Real-time cart management with local storage persistence
- ⚡ **Instant Checkout** - Streamlined order placement (Cash on Delivery)
- 📦 **Order Tracking** - Real-time order status tracking with visual progress
- ❤️ **Wishlist** - Save favorite items for later purchase
- 📱 **Responsive Design** - Optimized for mobile, tablet, and desktop
- 🌓 **Dark Mode** - Built-in theme switching capability

### For Administrators
- 📊 **Dashboard Analytics** - view total revenue, active orders, and customer stats
- 📦 **Inventory Management** - Add, edit, and delete products easily
- ⚠️ **Stock Alerts** - visual indicators for low-stock items
- 📋 **Order Management** - View and process customer orders (Backend ready)

## 🛠️ Tech Stack

### Frontend
- **HTML5 & CSS3** - Modern semantic markup and CSS variables
- **JavaScript (ES6+)** - Modular architecture without heavy frameworks
- **Bootstrap 5** - Responsive layout and components
- **Firebase Auth SDK** - Secure user authentication
- **AOS** - Scroll animations

### Backend
- **Java 17+**
- **Spring Boot 3.x** - REST API Framework
- **Spring Security** - JWT and Role-based access control
- **Maven** - Dependency management

### Database & Services
- **Firebase Firestore** - NoSQL Cloud Database
- **Firebase Authentication** - Identity management
- **Firebase Admin SDK** - Server-side operations

## 📁 Project Structure

```
BSMS/
├── spareparts-backend/          # Spring Boot Application
│   ├── src/main/java/           # Java Source Code
│   │   ├── config/              # Firebase & Security Config
│   │   ├── controller/          # REST API Controllers (v2)
│   │   ├── model/               # Firestore Data Models
│   │   ├── repository/          # Firestore Repositories
│   │   └── service/             # Business Logic
│   └── src/main/resources/      # Properties & Keys
│
├── spareparts-frontend/         # Client Application
│   ├── css/                     # Custom Design System
│   ├── js/                      # App Logic & API Integration
│   ├── admin/                   # Admin Dashboard Panels
│   ├── profile/                 # User Profile Pages
│   ├── orders/                  # Order Tracking
│   ├── index.html               # Main Storefront
│   └── serve.py                 # Local Python Server
│
└── README.md
```

## 🚀 Getting Started

### Prerequisites
- Java JDK 17+
- Maven
- Python 3.x (for serving frontend)
- Firebase Project

### 1. Clone the Repository
```bash
git clone https://github.com/yourusername/BSMS.git
cd BSMS
```

### 2. Backend Setup
```bash
cd spareparts-backend

# 1. Place 'firebase-sa.json' in src/main/resources/static/
# 2. Update application-firestore.properties if needed

# Run with seed profile to populate initial data
mvn spring-boot:run -Dspring-boot.run.profiles=seed

# Subsequent runs
mvn spring-boot:run
```

### 3. Frontend Setup
```bash
cd spareparts-frontend

# Start the simple server
python serve.py
# Server runs at http://localhost:5500
```

## 🔐 Security Rules (Firestore)

Recommended security rules for your Firebase console:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Products: Public read, Admin write
    match /products/{id} {
      allow read: if true;
      allow write: if request.auth.token.role == 'ADMIN';
    }
    
    // Users: Owners can read/write their own data
    match /users/{userId} {
      allow read, write: if request.auth.uid == userId;
    }
    
    // Orders: Users create, Admins read process
    match /orders/{orderId} {
      allow create: if request.auth != null;
      allow read: if request.auth.uid == resource.data.userId || request.auth.token.role == 'ADMIN';
    }
  }
}
```

## 📱 Pages & Routes

### Public
| Page | Description |
|------|-------------|
| `index.html` | Home, Product Grid, Search |
| `product.html?id=ID` | Product Details & Reviews |

### Customer (Protected)
| Page | Description |
|------|-------------|
| `profile/index.html` | User Settings & Address |
| `orders/index.html` | Order History & Tracking |
| `wishlist.html` | Saved Items |

### Admin (Protected)
| Page | Description |
|------|-------------|
| `admin/index.html` | Dashboard & Analytics |
| `admin/products.html` | Catalog Management |

## 🔌 API Endpoints (v2)

### Public
- `GET /api/v2/public/products` - List catalog
- `GET /api/v2/public/categories` - List categories

### User
- `POST /api/v2/user/orders` - Place order
- `GET /api/v2/user/orders/{uid}` - Get history
- `POST /api/v2/user/profile` - Update profile

### Admin
- `GET /api/v2/admin/dashboard` - Get stats
- `POST /api/v2/admin/products` - Add product
- `PUT /api/v2/admin/products/{id}` - Update product

## 🎨 Design System

We use a custom CSS variable system for easy theming:

### Color Palette
- **Primary**: Orange Fire (`#ff6b35`)
- **Secondary**: Slate Dark (`#1e293b`)
- **Success**: Emerald (`#10b981`)
- **Warning**: Amber (`#f59e0b`)
- **Error**: Rose (`#ef4444`)

### Typography
- **Headings**: 'Inter', sans-serif (Bold/Black)
- **Body**: 'Inter', sans-serif (Regular)

## 📄 License

This project is licensed under the MIT License.

## 👥 Authors

- **SIChathuranga** - *Developer*
