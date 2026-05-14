Namma-Kelsa – Hyperlocal Worker Marketplace

Namma-Kelsa is a hyperlocal Android marketplace application that connects customers with skilled daily-wage workers such as electricians, plumbers, carpenters, and more — without middlemen.

It enables direct communication, fair wages, and real-time worker discovery based on location and availability.

📌 Problem Statement
-Workers lack digital presence
-Customers struggle to find trusted nearby workers
-Middlemen take 20–40% commission
-No proper rating or verification system

💡 Solution
Namma-Kelsa provides a two-sided platform:
👷 Worker
-Create profile with skills & experience
-Upload work portfolio
-Set daily rates
-Toggle availability
-Accept/decline bookings
-Receive ratings & reviews
🧑 Customer
-Search workers by skill & distance
-View profiles & ratings
-Book appointments
-Call workers directly
-Rate & review services
🏗️ Features
🔐 Authentication
Email & Password based login (Firebase Auth)
Secure user session management
👤 Profile Management
Worker & Customer profiles
Portfolio image uploads
Real-time updates
🔍 Search & Filter
Search by skill (Electrician, Plumber, etc.)
Filter by:
Distance
Rating
Availability
📅 Booking System
Schedule jobs (date & time)
Auto price calculation
Booking status tracking:
Pending
Accepted
Completed
⭐ Ratings & Reviews
Customers rate workers (1–5 stars)
Feedback system builds trust
📍 Location-Based Services
Nearby worker discovery using GeoFirestore
Google Maps integration
📞 Communication
Direct call to workers via phone dialer
🔔 Notifications
Booking alerts
Status updates using Firebase Cloud Messaging
🧑‍💻 Tech Stack
📱 Frontend (Android)
Kotlin
XML (Material Design 3)
MVVM Architecture
RecyclerView + LiveData
☁️ Backend (Firebase)
Firebase Authentication (Email/Password)
Cloud Firestore (Database)
Firebase Storage (Images)
Firebase Cloud Messaging (Notifications)
📍 APIs & Libraries
Google Maps API
GeoFirestore (Location queries)
Glide (Image loading)
Coroutines (Async operations)
📂 Project Structure
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
🔄 Workflow
User selects role (Worker / Customer)
Login using Email & Password
Profile setup (first-time users)
Customer searches workers
Customer books service
Worker accepts/rejects booking
Job completed
Customer gives rating & review
🗄️ Database Schema (Firestore)
📁 Collections
1. users
-uid
-name
-email
-userType (worker/customer)
2. workers
-uid
-skill
-experience
-dailyRate
-location (GeoPoint)
-isAvailable
-rating
3. bookings
-bookingId
-workerId
-customerId
-date & time
-status
-totalPrice
4. reviews
-reviewId
-workerId
-customerId
-rating
-comment

⚙️ Installation & Setup
1️⃣ Clone Repository
git clone https://github.com/your-username/namma-kelsa.git
2️⃣ Open in Android Studio
3️⃣ Firebase Setup
Create project in Firebase Console
Add google-services.json to /app
Enable:
Authentication (Email/Password)
Firestore
Storage
4️⃣ Run App
Click Run  in Android Studio
✅ Testing
User login/logout
Worker availability toggle
Booking flow
Real-time updates
Notifications
🚀 Future Enhancements
💬 In-app chat system
🤖 AI-based worker recommendations
📊 Worker analytics dashboard

Namma-Kelsa simplifies the process of finding skilled workers by providing a transparent, efficient, and real-time platform. It empowers workers digitally while offering customers a seamless service experience.
