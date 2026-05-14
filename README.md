#  Namma-Kelsa – Hyperlocal Worker Marketplace

Namma-Kelsa is a hyperlocal Android application that connects customers with skilled daily-wage workers such as electricians, plumbers, and carpenters without any middlemen.

The platform enables direct interaction, fair wages, and real-time worker discovery based on location and availability.

---

## 📌 Problem Statement

- Workers lack digital presence  
- Customers struggle to find trusted nearby workers  
- Middlemen take high commissions  
- No proper rating or transparency system  

---

## 💡 Solution

Namma-Kelsa provides a two-sided platform:

### 👷 Worker
- Create and manage profile  
- Add skills and experience  
- Set daily rates  
- Toggle availability  
- Accept or decline bookings  
- View earnings and reviews  

### 🧑 Customer
- Search nearby workers  
- View worker profiles  
- Book services  
- Call workers directly  
- Rate and review services  

---

## 🏗️ Features

### 🔐 Authentication
- Email & Password based login  
- Secure authentication using Firebase  

### 👤 Profile Management
- Worker and customer profiles  
- Portfolio image uploads  
- Real-time updates  

### 🔍 Search & Filter
- Search workers by skill  
- Filter by distance, rating, and availability  

### 📅 Booking System
- Schedule services  
- Track booking status (Pending, Accepted, Completed)  

### ⭐ Ratings & Reviews
- Rate workers from 1–5  
- Provide feedback  

### 📍 Location-Based Services
- Find nearby workers  
- Integrated with Google Maps  

### 📞 Communication
- Direct call to workers  

### 🔔 Notifications
- Booking alerts  
- Status updates  

---

## 🧑‍💻 Tech Stack

### 📱 Frontend
- Kotlin  
- XML (Material Design)  
- MVVM Architecture  

### ☁️ Backend
- Firebase Authentication (Email/Password)  
- Cloud Firestore  
- Firebase Storage  
- Firebase Cloud Messaging  

### 📍 Libraries & APIs
- Google Maps API  
- GeoFirestore  
- Glide  
- Kotlin Coroutines  

---

## 📂 Project Structure
com.nammakelsa
│
├── data/
│   ├── model/        # Data classes (Worker, User, Booking, Review)
│   └── repository/   # Firebase interactions
│
├── ui/
│   ├── auth/         # Login & Registration
│   ├── worker/       # Worker dashboard
│   ├── customer/     # Customer screens
│   └── common/       # Shared components
│
├── viewmodel/        # Business logic (MVVM)
│
└── utils/            # Helper classes

---

## 🔄 Workflow

1. User selects role (Worker / Customer)  
2. Login using Email & Password  
3. Setup profile  
4. Customer searches workers  
5. Customer books service  
6. Worker accepts or rejects  
7. Job completion  
8. Customer gives rating  

---

## 🗄️ Database Schema

### Users Collection
- uid  
- name  
- email  
- userType  

### Workers Collection
- uid  
- skill  
- experience  
- dailyRate  
- location  
- availability  
- rating  

### Bookings Collection
- bookingId  
- workerId  
- customerId  
- date  
- time  
- status  
- amount  

### Reviews Collection
- reviewId  
- workerId  
- customerId  
- rating  
- reviewText  

---

## ⚙️ Installation

### Clone the repository
git clone https://github.com/your-username/namma-kelsa.git

### Open in Android Studio

### Setup Firebase
- Add `google-services.json`
- Enable Authentication (Email/Password)
- Enable Firestore and Storage

### Run the app
Click ▶️ Run in Android Studio

---

## 🧪 Testing

- Login & Registration  
- Booking workflow  
- Real-time updates  
- Notifications  

---

## 🚀 Future Enhancements

- In-app chat  
- Payment integration  
- AI-based recommendations  
- Multi-language support  

---

## 📊 Conclusion

Namma-Kelsa provides a simple and efficient solution to connect customers with skilled workers, ensuring transparency, fair pricing, and ease of use.



