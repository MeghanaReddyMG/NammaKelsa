# NAMMA-KELSA: COMPLETE SOP & DEVELOPMENT GUIDE

**For: Anti-Gravity Development Team**  
**Version: 1.0 | January 2025**  
**Document Type: Complete Project Specification & Development Guide**

---

## TABLE OF CONTENTS

1. Executive Summary & Project Overview
2. Technology Stack & Requirements  
3. User Roles & Authentication Flow
4. Complete Screen Designs & Features
5. Screen Connections & Navigation Map
6. Database Schema (Firestore)
7. Android Implementation Steps
8. Feature-by-Feature Development Guide
9. Testing & Deployment Checklist
10. Appendix: Quick Reference

---

## 1. EXECUTIVE SUMMARY & PROJECT OVERVIEW

### What is Namma-Kelsa?

Namma-Kelsa is a **hyperlocal marketplace** connecting customers with skilled daily-wage workers (electricians, plumbers, carpenters, painters, gardeners, AC technicians, etc.) **directly**—bypassing middlemen and enabling fair wages, direct communication, and skill-based discovery.

### Core Problem Solved

- **Workers are invisible digitally** — They have no online presence or way to showcase skills
- **Customers can't find trustworthy workers nearby** — Manual searching, word-of-mouth only
- **Middlemen exploit both parties** — Taking 20-40% commissions from workers
- **No way to verify worker skills or experience** — No reviews, no proof of capability

### Solution: Two-User Model

**WORKER USER:**
- Creates digital profile with skills, daily rates, availability
- Uploads portfolio (3+ photos of recent work)
- Toggles "Available Today" to appear in searches
- Receives ratings & reviews from customers
- Builds professional reputation

**CUSTOMER USER:**
- Searches workers by skill (Electrician, Plumber, etc.) & distance (2km, 5km, etc.)
- Views worker profiles with ratings, reviews, and work samples
- Books appointments directly (no middleman)
- Calls worker directly (no commission layers)
- Rates & reviews workers after job completion

### Success Metrics

- ✅ **Real-time Sync:** Availability toggle updates search results in < 1 second
- ✅ **Call Button:** Opens phone dialer with one tap (no app-to-app confusion)
- ✅ **Simple UI:** Worker can manage profile without technical training
- ✅ **Discoverability:** 100+ skill-specific workers discoverable within 2km radius

---

## 2. TECHNOLOGY STACK & REQUIREMENTS

### Frontend (Android)

| Component | Specification |
|-----------|---------------|
| **Language** | Kotlin 1.9+ |
| **IDE** | Android Studio Hedgehog or latest |
| **Min SDK** | API 28 (Android 9) |
| **Target SDK** | API 34 (Android 14) |
| **Architecture** | MVVM (Model-View-ViewModel) |
| **UI Framework** | Material Design 3 |
| **Layout Engine** | ConstraintLayout |
| **Lists** | RecyclerView with DiffUtil |
| **Selections** | ChipGroup, MaterialButton |

### Backend (Firebase)

| Service | Purpose |
|---------|---------|
| **Firestore** | Real-time NoSQL database (worker data, bookings, reviews) |
| **Firebase Auth** | Phone number + OTP authentication |
| **Firebase Storage** | Profile photos, work portfolio images |
| **Firebase Cloud Messaging** | Push notifications (booking requests, acceptances) |
| **Google Maps API** | Worker location markers, nearby search |
| **GeoFirestore** | Radius-based queries (workers within 2km) |

### Additional Libraries

| Library | Purpose |
|---------|---------|
| **Glide** | Fast image loading & caching |
| **Coroutines** | Async operations, Firestore listeners |
| **LiveData** | Real-time UI updates |
| **ViewModel** | Business logic, data persistence |

---

## 3. USER ROLES & AUTHENTICATION FLOW

### User Roles (Detailed)

#### WORKER
- **Definition:** Skilled laborer with one primary skill
- **Profile Data:** Name, skill type, daily rate, years of experience, address, profile photo, work portfolio (3+ photos)
- **Key Feature:** "Available Today" toggle → appears/disappears from customer search instantly
- **Earnings:** Track total earnings, completed jobs, rating
- **Can perform:** Update profile, toggle availability, accept/decline bookings, view earnings

#### CUSTOMER
- **Definition:** Person looking to hire a skilled worker
- **Profile Data:** Name, address, phone number
- **Key Features:** Search, filter, book appointments, rate & review
- **Can perform:** Search by skill & distance, view worker details, call directly, book, rate & review

### Authentication Flow (Both Users)

```
1. App Launch
   ↓
2. Check if user logged in (stored session)
   ├─ YES → Route to appropriate dashboard (Worker or Customer)
   └─ NO → Continue to step 3
   ↓
3. Show "SCREEN 1: Role Selection"
   "Are you a Worker or Customer?"
   ├─ Worker → Go to step 4
   └─ Customer → Go to step 4
   ↓
4. Show "SCREEN 2: OTP Login"
   - Enter phone number (10 digits)
   - Send OTP via Firebase Auth
   - Enter 6-digit OTP
   - Verify
   ↓
5. Check if first-time user (Firestore query)
   ├─ YES → Show Profile Setup (SCREEN 3A/3B for Worker, SCREEN 3C for Customer)
   │         User completes profile
   │         Save to Firestore
   │         Route to Dashboard
   └─ NO → Load existing profile from Firestore
           Route to Dashboard
```

### Session Persistence (Critical Feature)

**Key Behavior:** Once logged in with a phone number, that phone number always returns to the same account with all previous data.

**Implementation:**
```
1. After successful OTP verification:
   - Store Firebase Auth token locally (Android handles automatically)
   - Store userType (worker/customer) in SharedPreferences
   - Store uid in SharedPreferences

2. On next app launch:
   - Check if Auth token exists
   - If exists: Query Firestore using stored uid
   - Load user data (name, profile, settings, favorites, bookings)
   - Route to appropriate dashboard

3. If user logs out:
   - Clear Auth token
   - Clear SharedPreferences
   - Show Role Selection on next launch

4. Multiple users on same phone:
   - Logout current user
   - Login with different phone number
   - New account created OR existing account loaded for that phone
```

---

## 4. COMPLETE SCREEN DESIGNS & FEATURES

### SCREEN 1: Role Selection (Entry/Splash)

**Purpose:** First screen shown when user is not logged in. User chooses their role.

**Layout:**
```
┌─────────────────────────────────────────┐
│          NAMMA KELSA                    │  ← Logo/Title centered
│     [Logo/Icon]                         │
└─────────────────────────────────────────┘

        "Who are you?"

┌─────────────────────────────────────────┐
│     🔨 I'M A WORKER                    │  ← Solid button, colored
│                                         │
│     (Electrician, Plumber, etc.)       │
└─────────────────────────────────────────┘

┌─────────────────────────────────────────┐
│     🔍 I'M A CUSTOMER                  │  ← Solid button, colored
│                                         │
│     (Looking for workers)              │
└─────────────────────────────────────────┘

Already have account? Tap to login
```

**UI Elements:**
- Title: "NAMMA KELSA" (center-aligned, 40pt, bold)
- Subtitle: "Who are you?" (20pt)
- Button 1: "I'M A WORKER" with hammer icon
- Button 2: "I'M A CUSTOMER" with search icon
- Both buttons: Full width, 50pt height, Material Design 3 colors

**Interactions:**
- Tap "I'M A WORKER" → Go to SCREEN 2 (OTP with userType='worker')
- Tap "I'M A CUSTOMER" → Go to SCREEN 2 (OTP with userType='customer')

**Navigation:**
- No back button (first screen)
- Can add "Already have account? Login" link for direct login (optional)

---

### SCREEN 2: Authentication (Phone OTP)

**Purpose:** User authenticates with phone number + OTP

**Phase 1: Enter Phone Number**

```
┌─────────────────────────────────────────┐
│    LOGIN WITH PHONE                    │  ← Title
│                                         │
│  Enter your 10-digit phone number      │  ← Helper text
│                                         │
│  ┌─────────────────────────────────┐  │
│  │ ┌─────────────────────────────┐ │  │ ← Phone input
│  │ │  9876543210                 │ │  │
│  │ └─────────────────────────────┘ │  │
│  └─────────────────────────────────┘  │
│                                         │
│  We'll send you an OTP to verify       │  ← Explanation
│                                         │
│  ┌─────────────────────────────────┐  │
│  │    SEND OTP                     │  │  ← Button
│  └─────────────────────────────────┘  │
└─────────────────────────────────────────┘
```

**UI Elements:**
- Title: "Login with Phone"
- Input field: Phone number (10 digits, numeric keyboard)
- Helper text: "We'll send you an OTP to verify"
- Button: "SEND OTP" (full width, disabled until 10 digits entered)

**Interaction:**
1. User enters phone number
2. Tap "SEND OTP"
3. App calls Firebase `PhoneAuthProvider.verifyPhoneNumber()`
4. Firebase sends SMS with 6-digit OTP to phone
5. Show loading indicator "Sending OTP..."
6. After SMS sent, transition to Phase 2

**Phase 2: Enter OTP**

```
┌─────────────────────────────────────────┐
│    VERIFY OTP                          │  ← Title
│                                         │
│  Enter 6-digit OTP sent to             │  ← Helper
│  +91 9876543210                        │
│                                         │
│  ┌──┐ ┌──┐ ┌──┐ ┌──┐ ┌──┐ ┌──┐      │
│  │ 1│ │ 2│ │ 3│ │ 4│ │ 5│ │ 6│      │  ← 6 boxes (1 digit each)
│  └──┘ └──┘ └──┘ └──┘ └──┘ └──┘      │
│                                         │
│  ┌─────────────────────────────────┐  │
│  │    VERIFY                       │  │  ← Button
│  └─────────────────────────────────┘  │
│                                         │
│  Resend OTP (30s)                      │  ← Countdown timer
└─────────────────────────────────────────┘
```

**UI Elements:**
- Title: "Verify OTP"
- Helper: "Enter 6-digit OTP sent to +91 9876543210"
- 6 input boxes (each accepts 1 digit, auto-focus next)
- Button: "VERIFY" (enabled only when all 6 digits filled)
- Resend link: "Resend OTP (30s)" with countdown

**Interaction:**
1. User enters 6-digit OTP
2. Tap "VERIFY"
3. Show loading: "Verifying..."
4. Call Firebase `signInWithCredential(PhoneAuthCredential)`
5. On success:
   - Save Firebase Auth token (automatic)
   - Get user uid from FirebaseAuth.getInstance().currentUser
   - Check if user exists in Firestore `users/{uid}`
   - If NOT exist → Go to SCREEN 3 (Profile Setup)
   - If exist → Load from Firestore and Go to Dashboard (SCREEN 5 or 6)

**Error Handling:**
- Invalid OTP: Show "Incorrect OTP. Try again or request a new OTP"
- OTP expired: Show "OTP expired. Request a new one"
- Network error: Show "Network error. Please try again"

---

### SCREEN 3A: Worker Profile Setup (First Login)

**Purpose:** Collect worker's skill, rates, and basic info on first login

```
┌─────────────────────────────────────────┐
│    COMPLETE YOUR PROFILE                │  ← Title
│    (Step 1 of 2)                       │
│                                         │
│    Full Name                            │
│  ┌─────────────────────────────────┐  │
│  │ Raju Kumar                      │  │
│  └─────────────────────────────────┘  │
│                                         │
│    What's your skill?                  │
│  ┌─────────────────────────────────┐  │
│  │ ▼ Select Skill                  │  │  ← Dropdown
│  │   ○ Electrician                 │  │
│  │   ○ Plumber                     │  │
│  │   ○ Carpenter                   │  │
│  │   ○ Painter                     │  │
│  │   ○ Mason                       │  │
│  │   ○ Gardener                    │  │
│  │   ○ AC Technician               │  │
│  │   ○ Other                       │  │
│  └─────────────────────────────────┘  │
│                                         │
│    Daily Rate (₹)                      │
│  ┌─────────────────────────────────┐  │
│  │ 800                             │  │  ← Numeric input
│  └─────────────────────────────────┘  │
│                                         │
│    Years of Experience                 │
│  ┌─────────────────────────────────┐  │
│  │ 5                               │  │  ← Numeric input
│  └─────────────────────────────────┘  │
│                                         │
│    City / Address                      │
│  ┌─────────────────────────────────┐  │
│  │ Bangalore, Karnataka            │  │
│  └─────────────────────────────────┘  │
│                                         │
│    ┌─────────────────────────────────┐ │
│    │  NEXT (Upload Photos)          │ │
│    └─────────────────────────────────┘ │
└─────────────────────────────────────────┘
```

**UI Elements:**
- Full Name: Text input
- Skill: Dropdown with options (Electrician, Plumber, Carpenter, Painter, Mason, Gardener, AC Technician, Other)
- Daily Rate: Numeric input with currency symbol (₹)
- Years of Experience: Numeric spinner (0-50)
- City/Address: Text input (auto-filled from device location if available)
- Button: "NEXT (Upload Photos)"

**Validation:**
- Name: Not empty
- Skill: Must select one
- Daily Rate: > 0
- Experience: 0-50
- Address: Not empty

**Interactions:**
- User fills all fields
- Tap "NEXT"
- Save data to SharedPreferences (temporary)
- Go to SCREEN 3B (Work Portfolio)

---

### SCREEN 3B: Worker Profile Setup - Work Portfolio (First Login, Step 2)

**Purpose:** Upload 3 photos of recent work

```
┌─────────────────────────────────────────┐
│    UPLOAD YOUR WORK PHOTOS              │
│    (Step 2 of 2)                       │
│                                         │
│    Show us 3 recent projects           │
│                                         │
│    PHOTO 1                              │
│  ┌─────────────────────────────────┐  │
│  │         📷                      │  │
│  │   Tap to add photo              │  │  ← Clickable area
│  │   (Camera or Gallery)           │  │
│  └─────────────────────────────────┘  │
│                                         │
│    PHOTO 2                              │
│  ┌─────────────────────────────────┐  │
│  │         📷                      │  │
│  │   Tap to add photo              │  │
│  │   (Camera or Gallery)           │  │
│  └─────────────────────────────────┘  │
│                                         │
│    PHOTO 3                              │
│  ┌─────────────────────────────────┐  │
│  │         📷                      │  │
│  │   Tap to add photo              │  │
│  │   (Camera or Gallery)           │  │
│  └─────────────────────────────────┘  │
│                                         │
│    ┌─────────────────────────────────┐ │
│    │  PROFILE PICTURE                │ │  ← Profile photo
│    ├─────────────────────────────────┤ │
│    │  ┌──────────────────────────┐   │ │
│    │  │     Tap to upload       │   │ │
│    │  │     profile photo       │   │ │
│    │  │      (Selfie OK)        │   │ │
│    │  └──────────────────────────┘   │ │
│    └─────────────────────────────────┘ │
│                                         │
│    ┌─────────────────────────────────┐ │
│    │  COMPLETE SETUP                 │ │
│    └─────────────────────────────────┘ │
└─────────────────────────────────────────┘
```

**UI Elements:**
- 3 photo slots (for work portfolio)
- 1 profile photo slot (for profile picture)
- Each slot: Clickable area with camera icon
- Tap to open: Image picker (Camera or Gallery)
- Preview: Show selected image with X button to remove
- Button: "COMPLETE SETUP"

**Interactions:**
1. User taps photo slot
2. Shows dialog: "Camera" or "Gallery"
3. User selects/captures image
4. Image displayed in slot
5. User repeats for 3 work photos + 1 profile photo
6. Tap "COMPLETE SETUP"
7. Upload all images to Firebase Storage:
   ```
   - workers/{uid}/profile.jpg
   - workers/{uid}/work_1.jpg
   - workers/{uid}/work_2.jpg
   - workers/{uid}/work_3.jpg
   ```
8. Get download URLs for each image
9. Create worker document in Firestore with:
   ```
   {
     uid: currentUser.uid,
     name: savedName,
     skill: savedSkill,
     dailyRate: savedRate,
     experience: savedExperience,
     address: savedAddress,
     profileImage: profileURL,
     workImages: [url1, url2, url3],
     isAvailable: false,  // Default OFF
     rating: 0,
     reviewCount: 0,
     phone: currentUser.phone,
     location: GeoPoint(lat, lng)
   }
   ```
10. Go to SCREEN 5 (Worker Dashboard)

**Note:** Profile photo is required for profile picture. Work photos can be optional (allow skip).

---

### SCREEN 3C: Customer Profile Setup (First Login)

**Purpose:** Collect minimal info from customer

```
┌─────────────────────────────────────────┐
│    WELCOME!                             │
│    Let's set up your profile           │
│                                         │
│    Name                                 │
│  ┌─────────────────────────────────┐  │
│  │ Amit Kumar                      │  │
│  └─────────────────────────────────┘  │
│                                         │
│    Address                              │
│  ┌─────────────────────────────────┐  │
│  │ 123 Main Street, Bangalore      │  │
│  └─────────────────────────────────┘  │
│                                         │
│    Profile Photo (Optional)             │
│  ┌─────────────────────────────────┐  │
│  │         📷                      │  │
│  │   Tap to add photo              │  │
│  │   (Camera or Gallery)           │  │
│  └─────────────────────────────────┘  │
│                                         │
│    ┌─────────────────────────────────┐ │
│    │  START EXPLORING                │ │
│    └─────────────────────────────────┘ │
└─────────────────────────────────────────┘
```

**UI Elements:**
- Name: Text input
- Address: Text input
- Profile photo: Optional
- Button: "START EXPLORING"

**Interactions:**
1. User fills name & address
2. Optionally upload profile photo
3. Tap "START EXPLORING"
4. Save to Firestore `users/{uid}`:
   ```
   {
     uid: currentUser.uid,
     phone: currentUser.phone,
     userType: "customer",
     name: enteredName,
     address: enteredAddress,
     profileImage: photoURL,
     createdAt: timestamp
   }
   ```
5. Go to SCREEN 6 (Customer Dashboard - Search)

---

### SCREEN 5: Worker Dashboard (Main View)

**Purpose:** Worker's home screen. Manage availability, view bookings, earnings.

```
┌─────────────────────────────────────────┐
│  👤 Welcome, Raju!                     │  ← Greeting
│                                         │
│  ┌─────────────────────────────────┐  │
│  │  [Photo]  Raju Kumar            │  │
│  │           Electrician           │  │
│  │           ₹800/day              │  │
│  └─────────────────────────────────┘  │
│                                         │
│  AVAILABLE TODAY?                      │  ← Critical toggle
│  ◎─────●  ON                          │
│  "Turn ON to appear in search results" │
│                                         │
│  STATS                                  │
│  ┌──────────┬──────────┬──────────┐   │
│  │ Earnings │  Jobs    │ Rating   │   │
│  │ ₹ 0      │    0     │  4.5 ⭐  │   │
│  └──────────┴──────────┴──────────┘   │
│                                         │
│  RECENT BOOKINGS                       │
│  ┌─────────────────────────────────┐  │
│  │ Amit Kumar                      │  │  ← Booking card
│  │ 📅 15 Jan, 2025 • 09:00 - 05:00 PM │
│  │ 🏠 Bangalore                    │  │
│  │ Status: ⏳ PENDING              │  │
│  └─────────────────────────────────┘  │
│                                         │
└─────────────────────────────────────────┘

BOTTOM NAVIGATION:
┌─────────────────────────────────────────┐
│  🏠 Dashboard │ 📋 Bookings │ 💰 Earnings │
│  👤 Profile │ ⚙️ Settings  │
└─────────────────────────────────────────┘
```

**UI Elements:**

1. **Greeting Section:**
   - Text: "Welcome, [Name]!"
   - Logout button (optional, in menu)

2. **Profile Card:**
   - Profile photo (circular)
   - Name
   - Skill
   - Daily rate
   - Tap to edit profile

3. **Availability Section:** ⭐ **CRITICAL FEATURE**
   - Label: "Available Today?"
   - Material SwitchMaterial component
   - Default: OFF (off during first login)
   - Helper text: "Turn ON to appear in search results"
   - **On toggle:**
     ```
     When toggled ON:
     → Write to Firestore: workers/{uid} → isAvailable = true
     → Customers' searches with snapshot listener get instant update
     → Worker appears in search results
     
     When toggled OFF:
     → Write to Firestore: workers/{uid} → isAvailable = false
     → Customers' searches remove this worker instantly
     → Worker disappears from search results
     ```

4. **Stats Cards:**
   - Total Earnings: Shows sum of completed bookings
   - Jobs Completed: Count of completed bookings
   - Rating: Average rating (e.g., 4.5⭐)

5. **Recent Bookings Section:**
   - Shows 2-3 most recent bookings
   - Each shows: Customer name, date, time, status
   - Color-coded status:
     - PENDING: Yellow/Orange
     - ACCEPTED: Blue
     - COMPLETED: Green
   - Tap to view full details

6. **Bottom Navigation:**
   - Tab 1: Dashboard (home icon) - current
   - Tab 2: Bookings (list icon)
   - Tab 3: Earnings (money icon)
   - Tab 4: Profile (user icon)
   - Tab 5: Settings (gear icon)

**Interactions:**
- Tap "Available Today?" toggle → Update Firestore instantly
- Tap profile card → Go to SCREEN 5B (Edit Profile)
- Tap booking card → Go to booking details (manage acceptance/decline)
- Tap tab → Switch to that tab

---

### SCREEN 5B: Worker Edit Profile

**Purpose:** Worker can update their profile details anytime

**Fields (same as SCREEN 3A + SCREEN 3B):**
- Name
- Skill
- Daily Rate
- Experience
- Address
- Profile Photo
- Work Portfolio (3 photos)

**Interactions:**
- User can update any field
- Tap "SAVE CHANGES"
- Update Firestore `workers/{uid}`
- Show success notification
- Return to SCREEN 5

---

### SCREEN 6: Customer Dashboard (Search & Filter)

**Purpose:** Customer's main screen. Search for workers by skill, distance, rating.

```
┌─────────────────────────────────────────┐
│  👤 Amit Kumar                         │  ← Greeting
│  FIND WORKERS                          │
│                                         │
│  ┌──────────────────────────────────┐ │
│  │ 🔍 Search by name or skill    │ │  ← Search bar
│  └──────────────────────────────────┘ │
│  [Filter] [Map] [Favorites]           │  ← Quick filters
│                                         │
│  FILTERS: Electrician • 2km • 4⭐+     │
│                                         │
│  ┌──────────────────────────────────┐ │
│  │  📷 Raju Kumar                   │ │
│  │  ⭐ Electrician                  │ │
│  │  ⭐ 4.8 (245 reviews)            │ │
│  │  ₹800/day • 2.5 km away         │ │
│  │  ✅ Available Now        ❤️      │ │  ← Worker card
│  │  [CALL] [BOOK] [+FAV]           │ │
│  └──────────────────────────────────┘ │
│                                         │
│  ┌──────────────────────────────────┐ │
│  │  📷 Vijay Kumar                  │ │
│  │  ⭐ Electrician                  │ │
│  │  ⭐ 4.5 (120 reviews)            │ │
│  │  ₹700/day • 3.2 km away         │ │
│  │  ✅ Available Now        ♡      │ │
│  │  [CALL] [BOOK] [+FAV]           │ │
│  └──────────────────────────────────┘ │
│                                         │
│  [Load more...]                        │
└─────────────────────────────────────────┘

BOTTOM NAVIGATION:
┌─────────────────────────────────────────┐
│ 🔍 Search │ ❤️ Favorites │ 📋 Bookings │
│🗺️ Map │ 👤 Profile │
└─────────────────────────────────────────┘
```

**UI Elements:**

1. **Search Bar:**
   - Search by worker name or skill
   - Icon: magnifying glass
   - Placeholder: "Search by name or skill"

2. **Filter Buttons:**
   - [Filter] button → Opens SCREEN 6B (Filter Bottom Sheet)
   - [Map] button → Opens SCREEN 14 (Map View)
   - [Favorites] shortcut (or use bottom tab)

3. **Active Filters Display:**
   - Show current filters as chips
   - E.g., "Electrician • 2km • 4⭐+"
   - Tap to remove individual filters

4. **Worker Cards (RecyclerView):**
   Each card displays:
   - Profile photo (80x80dp, rounded corners)
   - Name
   - Skill (with icon)
   - Rating: "⭐ 4.8 (245 reviews)"
   - Daily rate: "₹800/day"
   - Distance: "2.5 km away"
   - Availability badge: "✅ Available Now" or "❌ Offline"
   - Heart icon (toggle favorite)
   - Buttons: [CALL] [BOOK] [+FAV]

5. **Bottom Navigation:**
   - Tab 1: Search (home icon) - current
   - Tab 2: Favorites (heart icon)
   - Tab 3: Bookings (list icon)
   - Tab 4: Map (map icon)
   - Tab 5: Profile (user icon)

**Interactions:**

- **Search:**
  - User types in search bar
  - Query Firestore: `workers` where `skill` contains search term
  - Show matching workers with current filters
  
- **Filter:**
  - Tap [Filter] → Go to SCREEN 6B
  
- **Worker Card Tap:**
  - Tap card → Go to SCREEN 7 (Worker Details)
  
- **Call Button:**
  - Tap [CALL] → Open phone dialer with worker's number
  
- **Book Button:**
  - Tap [BOOK] → Go to SCREEN 8 (Booking Form)
  
- **Favorite Button:**
  - Tap heart icon → Add to favorites
  - Fill heart → Added
  - Empty heart → Not added
  - Write to Firestore `favorites/` collection

**Auto-Refresh:**
- Use Firestore snapshot listener on workers query
- When worker toggles availability:
  - Customer's list updates instantly (< 1 second)
  - Worker appears/disappears based on `isAvailable` value

---

### SCREEN 6B: Filter Bottom Sheet

**Purpose:** User sets search filters

```
BOTTOM SHEET (Slides up from bottom):

┌─────────────────────────────────────────┐
│   ═══════════════════════════════════  │  ← Drag handle
│                                         │
│   FILTERS                              │
│                                         │
│   SKILL                                 │
│   [Electrician] [Plumber] [Carpenter]  │  ← ChipGroup
│   [Painter] [Mason] [Gardener]         │
│                                         │
│   DISTANCE                              │
│   ◉─────────● (2km)                    │  ← Slider
│   2km        5km        10km       20km │
│                                         │
│   RATING                                │
│   ○ All ratings                        │  ← Radio buttons
│   ◉ 4★ & above                        │
│   ○ 3★ & above                        │
│                                         │
│   AVAILABILITY                          │
│   [☐] Show only available workers      │  ← Checkbox
│                                         │
│   ┌─────────────────────────────────┐ │
│   │   APPLY FILTERS                 │ │
│   └─────────────────────────────────┘ │
│   [CLEAR ALL]                         │
└─────────────────────────────────────────┘
```

**UI Elements:**
- Skill: ChipGroup (multiple selection allowed)
- Distance: Slider (2km to 20km)
- Rating: Radio buttons (All, 4★+, 3★+)
- Availability: Checkbox "Show only available workers"
- Buttons: [APPLY FILTERS] [CLEAR ALL]

**Interactions:**
1. User selects filters
2. Tap [APPLY FILTERS]
3. Close bottom sheet
4. Update worker list with new filters
5. Show selected filters as chips above list

**Firestore Query (after filters applied):**
```
firestore.collection("workers")
  .whereEqualTo("skill", selectedSkill)      // ← If selected
  .whereEqualTo("isAvailable", true)         // ← If checkbox ON
  .whereGreaterThanOrEqualTo("rating", 4.0) // ← If rating filter
  .addSnapshotListener { ... }
```

---

### SCREEN 7: Worker Details & Booking

**Purpose:** Show full worker profile and allow booking/calling

```
┌─────────────────────────────────────────┐
│  ◄────  [Back]                          │
│                                         │
│  ┌─────────────────────────────────┐  │
│  │                                 │  │
│  │     [LARGE PROFILE PHOTO]       │  │
│  │         (400x300)               │  │
│  │                                 │  │
│  └─────────────────────────────────┘  │
│                                         │
│  Raju Kumar                             │  ← Name
│  ⚡ Electrician                         │  ← Skill badge
│  ⭐ 4.8 (245 reviews)                   │
│  ₹800/day                               │
│  5 years experience                    │
│  📍 2.5 km away                         │
│  ✅ Available Now                       │
│                                         │
│  Bangalore, Karnataka                   │  ← Address
│                                         │
│  ┌──────────────────────────────────┐ │
│  │  [☎️ CALL]  [📅 BOOK]  [❤️ FAV]   │
│  └──────────────────────────────────┘ │
│                                         │
│  WORK GALLERY                           │
│  ┌─────┬─────┬─────┐                  │
│  │ 📷  │ 📷  │ 📷  │                  │  ← Horizontal scroll
│  │ [1] │ [2] │ [3] │                  │
│  └─────┴─────┴─────┘                  │
│  Tap to enlarge                        │
│                                         │
│  RECENT REVIEWS                        │  ← Reviews preview
│  ┌─────────────────────────────────┐  │
│  │ Amit Kumar                      │  │
│  │ ⭐⭐⭐⭐⭐                           │
│  │ "Great work, very professional" │  │
│  │ 5 days ago                      │  │
│  └─────────────────────────────────┘  │
│  [View All Reviews]                    │
│                                         │
└─────────────────────────────────────────┘
```

**UI Elements:**

1. **Header:**
   - Back button
   - Share button (optional)

2. **Profile Section:**
   - Large profile photo (400x300)
   - Name (24pt, bold)
   - Skill badge (with icon)
   - Rating (⭐ with count)
   - Daily rate
   - Experience
   - Distance
   - Availability status

3. **Action Buttons (Full Width):**
   - [☎️ CALL]: Opens phone dialer
   - [📅 BOOK]: Opens booking form
   - [❤️ FAV]: Toggles favorite (unfilled/filled heart)

4. **Work Gallery:**
   - Horizontal RecyclerView of work photos
   - Click to enlarge

5. **Reviews Section:**
   - Show 2-3 recent reviews
   - Each: Customer name, rating, comment, date
   - Link: "View All Reviews" → Go to SCREEN 10

**Interactions:**

- **CALL Button:**
  ```kotlin
  callButton.setOnClickListener {
    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:+91${worker.phone}"))
    startActivity(intent)  // Opens native phone dialer
  }
  ```

- **BOOK Button:**
  - Tap → Go to SCREEN 8 (Booking Form)

- **Favorite Button:**
  - Tap → Add to `favorites/` collection
  - Change icon from outline to filled

- **Gallery Photo:**
  - Tap → Show full-screen image viewer

- **View All Reviews:**
  - Tap → Go to SCREEN 10 (All Reviews)

---

### SCREEN 8: Book Appointment

**Purpose:** Customer creates booking

```
┌─────────────────────────────────────────┐
│  ◄────  BOOK APPOINTMENT                │
│                                         │
│  WORKER SUMMARY                         │
│  ┌─────────────────────────────────┐  │
│  │ [Photo] Raju Kumar              │  │
│  │ ⚡ Electrician • ₹800/day       │  │
│  │ ⭐ 4.8 (245 reviews)            │  │
│  └─────────────────────────────────┘  │
│                                         │
│  SELECT DATE                            │
│  ┌─────────────────────────────────┐  │
│  │ 📅 15 January, 2025             │  │  ← Date picker
│  └─────────────────────────────────┘  │
│                                         │
│  SELECT TIME                            │
│  Start: ┌──────────┐  ⏰ 09:00 AM      │
│  End:   ┌──────────┐  ⏰ 05:00 PM      │
│                                         │
│  WORK ADDRESS                           │
│  ┌─────────────────────────────────┐  │
│  │ 123 Main St, Bangalore          │  │
│  └─────────────────────────────────┘  │
│                                         │
│  SPECIAL NOTES (Optional)               │
│  ┌─────────────────────────────────┐  │
│  │ Need ceiling fan installation   │  │
│  └─────────────────────────────────┘  │
│                                         │
│  TOTAL PRICE                            │
│  ₹6400  (8 hours × ₹800)               │
│                                         │
│  ┌─────────────────────────────────┐  │
│  │  ✅ CONFIRM BOOKING             │  │
│  └─────────────────────────────────┘  │
│  [Cancel]                              │
└─────────────────────────────────────────┘
```

**UI Elements:**

1. **Worker Summary Card:**
   - Profile photo
   - Name
   - Skill
   - Daily rate
   - Rating

2. **Date Picker:**
   - Tap to select date
   - Must be today or future

3. **Time Inputs:**
   - Start time (time picker)
   - End time (time picker)
   - Calculate hours automatically

4. **Address:**
   - Pre-filled from user's address
   - Editable

5. **Special Notes:**
   - Optional text area
   - Max 500 characters

6. **Price Calculation:**
   - Show: "₹[dailyRate] × [hours] = ₹[total]"
   - Auto-update when time changes

7. **Buttons:**
   - [CONFIRM BOOKING] (disabled until all required fields filled)
   - [CANCEL] (link)

**Interactions:**

1. User fills all fields
2. System auto-calculates price
3. Tap [CONFIRM BOOKING]
4. Create document in Firestore `bookings/`:
   ```
   {
     id: auto-generated,
     workerId: selectedWorker.uid,
     customerId: currentUser.uid,
     date: selectedDate,
     startTime: startTime,
     endTime: endTime,
     address: selectedAddress,
     notes: specialNotes,
     totalPrice: calculatedPrice,
     status: "pending",
     createdAt: timestamp
   }
   ```
5. Send notification to worker
6. Go to SCREEN 9 (Confirmation)

---

### SCREEN 9: Booking Confirmation

**Purpose:** Show booking success

```
┌─────────────────────────────────────────┐
│                                         │
│            ✅                           │
│    BOOKING CONFIRMED!                  │
│                                         │
│    Booking ID: #BK123456               │
│                                         │
│    Raju Kumar (Electrician)             │
│    📅 15 Jan, 2025                      │
│    ⏰ 09:00 AM - 05:00 PM              │
│    📍 123 Main St, Bangalore            │
│    💰 ₹6400                             │
│                                         │
│    Status: ⏳ PENDING                   │
│    Waiting for worker to accept...     │
│                                         │
│    ┌─────────────────────────────────┐ │
│    │  VIEW BOOKING STATUS            │ │
│    └─────────────────────────────────┘ │
│    [Back to Search]                    │
│                                         │
└─────────────────────────────────────────┘
```

**UI Elements:**
- Checkmark icon (large)
- Success message
- Booking ID
- Booking details (worker, date, time, address, price)
- Status
- Buttons: [VIEW BOOKING STATUS] [Back to Search]

**Interactions:**
- [VIEW BOOKING STATUS] → Go to booking detail view
- [Back to Search] → Return to SCREEN 6

---

### SCREEN 10: Reviews & Ratings (View All)

**Purpose:** Show all reviews for a worker

```
┌─────────────────────────────────────────┐
│  ◄────  REVIEWS & RATINGS              │
│                                         │
│  RATING SUMMARY                        │
│  ⭐⭐⭐⭐⭐ 4.8 out of 5                  │
│  Based on 245 reviews                  │
│                                         │
│  ⭐⭐⭐⭐⭐ 150 (61%)  ████████████      │
│  ⭐⭐⭐⭐  60  (24%)  █████             │
│  ⭐⭐⭐   20  (8%)   ██                 │
│  ⭐⭐     10  (4%)   █                  │
│  ⭐       5  (2%)   ░                  │
│                                         │
│  ──────────────────────────────────── │
│                                         │
│  REVIEWS                                │
│  ┌─────────────────────────────────┐  │
│  │ Amit Kumar                      │  │
│  │ ⭐⭐⭐⭐⭐  5.0                      │
│  │ "Excellent work! Very quick and │  │
│  │ professional. Will hire again."  │  │
│  │ 5 days ago                      │  │
│  └─────────────────────────────────┘  │
│                                         │
│  ┌─────────────────────────────────┐  │
│  │ Priya Singh                     │  │
│  │ ⭐⭐⭐⭐⭐  5.0                      │
│  │ "Perfect! Did a great ceiling   │  │
│  │ fan installation."              │  │
│  │ 2 weeks ago                     │  │
│  └─────────────────────────────────┘  │
│                                         │
│  [Load more...]                        │
│                                         │
└─────────────────────────────────────────┘
```

**UI Elements:**
- Average rating (large, centered)
- Rating distribution (bar chart)
- Reviews list (RecyclerView)
- Each review: Customer name, rating, comment, date
- Load more button

**Interactions:**
- Scroll to see more reviews
- Tap review to report (optional feature)

---

### SCREEN 11: Add/Edit Review

**Purpose:** Customer rates and reviews worker after booking completed

**Shown when:** Booking status changes to "Completed"

```
┌─────────────────────────────────────────┐
│  ◄────  RATE & REVIEW                  │
│                                         │
│  Raju Kumar (Electrician)               │
│  Jan 15, 2025                          │
│                                         │
│  HOW WAS YOUR EXPERIENCE?              │
│  ⭐  ⭐  ⭐  ⭐  ⭐                        │  ← Tap to select
│                                         │
│  COMMENTS (Optional)                   │
│  ┌─────────────────────────────────┐  │
│  │ Great work, very professional.  │  │
│  │ Will hire again!                │  │
│  │ (200/500)                       │  │
│  └─────────────────────────────────┘  │
│                                         │
│  ┌─────────────────────────────────┐  │
│  │  SUBMIT REVIEW                  │  │
│  └─────────────────────────────────┘  │
│  [Skip]                                │
│                                         │
└─────────────────────────────────────────┘
```

**UI Elements:**
- Worker name & booking date
- Star rating (1-5, tap to select)
- Comment box (optional, max 500 chars)
- [SUBMIT REVIEW] button
- [SKIP] link

**Interactions:**
1. User taps stars (1-5)
2. Optionally writes comment
3. Tap [SUBMIT REVIEW]
4. Create document in Firestore `reviews/`:
   ```
   {
     id: auto-generated,
     workerId: booking.workerId,
     customerId: currentUser.uid,
     rating: selectedStars,
     comment: comment,
     timestamp: now
   }
   ```
5. Recalculate worker's average rating
6. Update worker document: `rating` and `reviewCount`
7. Show success: "Thanks for your review!"
8. Return to bookings list

---

### SCREEN 12: Favorites (Saved Workers)

**Purpose:** Customer's saved list of favorite workers

```
┌─────────────────────────────────────────┐
│  ❤️  FAVORITE WORKERS                   │
│                                         │
│  Empty State (if no favorites):         │
│  🤍 No favorite workers yet            │
│  [Explore Workers]                      │
│                                         │
│  Filled State (with favorites):        │
│  ┌──────────────────────────────────┐ │
│  │  📷 Raju Kumar                   │ │
│  │  ⭐ Electrician                  │ │
│  │  ⭐ 4.8 (245 reviews)            │ │
│  │  ₹800/day • 2.5 km away         │ │
│  │  ✅ Available Now                │ │
│  │  [CALL] [BOOK] [×REMOVE]         │ │
│  └──────────────────────────────────┘ │
│                                         │
│  ┌──────────────────────────────────┐ │
│  │  📷 Vijay Kumar                  │ │
│  │  ⭐ Electrician                  │ │
│  │  ⭐ 4.5 (120 reviews)            │ │
│  │  ₹700/day • 3.2 km away         │ │
│  │  ❌ Offline                      │ │
│  │  [CALL] [BOOK] [×REMOVE]         │ │
│  └──────────────────────────────────┘ │
│                                         │
└─────────────────────────────────────────┘
```

**UI Elements:**
- If empty: Show "No favorites yet" with button to explore
- If filled: List of worker cards
- Each card: Profile, skill, rating, rate, distance, availability, buttons
- [CALL] [BOOK] [×REMOVE]

**Interactions:**
- Tap worker card → Go to SCREEN 7
- Tap [CALL] → Open dialer
- Tap [BOOK] → Go to SCREEN 8
- Tap [×REMOVE] → Remove from favorites

**Data Source:**
```
Query: firestore.collection("favorites")
  .whereEqualTo("userId", currentUser.uid)
Then load full worker details for each workerIds
```

---

### SCREEN 13: Bookings History (Both User Types)

**Purpose:** View past and upcoming bookings

**For CUSTOMER:**
```
┌─────────────────────────────────────────┐
│  📋 MY BOOKINGS                         │
│                                         │
│  UPCOMING                               │
│  ┌─────────────────────────────────┐  │
│  │ Raju Kumar (Electrician)        │  │
│  │ 📅 15 Jan, 2025 • 09:00-05:00  │  │
│  │ 📍 Bangalore                    │  │
│  │ 💰 ₹6400                        │  │
│  │ Status: ⏳ PENDING              │  │
│  └─────────────────────────────────┘  │
│                                         │
│  COMPLETED                              │
│  ┌─────────────────────────────────┐  │
│  │ Vijay Kumar (Plumber)           │  │
│  │ 📅 10 Jan, 2025 • 10:00-04:00  │  │
│  │ 📍 Bangalore                    │  │
│  │ 💰 ₹5600                        │  │
│  │ Status: ✅ COMPLETED            │  │
│  │ [RATE & REVIEW]                 │  │
│  └─────────────────────────────────┘  │
│                                         │
└─────────────────────────────────────────┘
```

**For WORKER:**
```
┌─────────────────────────────────────────┐
│  📋 MY BOOKINGS                         │
│                                         │
│  PENDING REQUESTS                      │
│  ┌─────────────────────────────────┐  │
│  │ Amit Kumar                      │  │
│  │ 📅 15 Jan, 2025 • 09:00-05:00  │  │
│  │ 📍 123 Main St, Bangalore       │  │
│  │ 💰 ₹6400                        │  │
│  │ Status: ⏳ PENDING              │  │
│  │ [✅ ACCEPT] [❌ DECLINE]        │  │
│  └─────────────────────────────────┘  │
│                                         │
│  ACCEPTED                               │
│  ┌─────────────────────────────────┐  │
│  │ Priya Singh                     │  │
│  │ 📅 12 Jan, 2025 • 10:00-04:00  │  │
│  │ 📍 456 Oak Ave, Bangalore       │  │
│  │ 💰 ₹5600                        │  │
│  │ Status: 🔵 ACCEPTED             │  │
│  └─────────────────────────────────┘  │
│                                         │
└─────────────────────────────────────────┘
```

**UI Elements:**
- Grouped by status: Pending, Accepted, Completed, Cancelled
- Each booking card shows: Customer/Worker name, date, time, address, price, status
- Color-coded status badges
- Worker sees: [✅ ACCEPT] [❌ DECLINE] buttons on pending
- Customer sees: [RATE & REVIEW] on completed

**Interactions:**
- Tap booking → View full details
- Worker taps [ACCEPT] → Update status to "accepted", send notification to customer
- Worker taps [DECLINE] → Update status to "cancelled", send notification
- Customer taps [RATE & REVIEW] → Go to SCREEN 11

---

### SCREEN 14: Map View

**Purpose:** Show workers on a map

```
┌─────────────────────────────────────────┐
│  🗺️  NEARBY WORKERS                     │
│                                         │
│  ┌─────────────────────────────────┐  │
│  │                                 │  │
│  │    [Google Map View]            │  │
│  │    ├─ Your location (blue dot)  │  │
│  │    ├─ Worker markers (pins)     │  │
│  │    │  - Raju (Electrician)      │  │
│  │    │  - Vijay (Plumber)         │  │
│  │    └─ Circles showing 2km       │  │
│  │                                 │  │
│  │                                 │  │
│  │       [Map continues...]        │  │
│  │                                 │  │
│  └─────────────────────────────────┘  │
│                                         │
│  [+] [-] [Current Location]            │  ← Map controls
│  [≡ FILTER]                            │  ← Filter FAB
│                                         │
│  ┌─────────────────────────────────┐  │  ← Bottom sheet
│  │ Raju Kumar (Electrician)        │  │
│  │ ⭐ 4.8 | ₹800/day | 2.5 km away │  │
│  │ [CALL] [BOOK]                   │  │
│  └─────────────────────────────────┘  │
│                                         │
└─────────────────────────────────────────┘
```

**UI Elements:**
- Full-screen Google Map
- User's location (blue dot)
- Worker markers (pins with skill icon)
- When tapping marker: Show bottom sheet with worker info
- [+] [-] zoom controls
- [≡ FILTER] FAB (opens filter options)

**Interactions:**
- Tap worker marker → Show bottom sheet
- Tap [CALL] → Open dialer
- Tap [BOOK] → Go to SCREEN 8
- Tap [FILTER] → Filter workers on map

**Data:**
- Query workers near user location
- Use GeoFirestore for radius query
- Show only available workers (or based on filters)

---

## 5. SCREEN CONNECTIONS & NAVIGATION MAP

### Complete User Journey Flows

#### WORKER JOURNEY:

```
START
  ↓
SCREEN 1: Role Selection
  ├─ Tap "I'm a Worker"
  ↓
SCREEN 2: OTP Login
  ├─ First login: Phone number + OTP verified
  ├─ Not in users collection yet
  ↓
SCREEN 3A: Worker Profile Setup
  ├─ Enter: Name, Skill, Rate, Experience, Address
  ├─ Button: "NEXT"
  ↓
SCREEN 3B: Work Portfolio
  ├─ Upload: 3 work photos + 1 profile photo
  ├─ Button: "COMPLETE SETUP"
  ├─ [Save to Firestore: workers/{uid}]
  ↓
SCREEN 5: Worker Dashboard
  ├─ Default tab: Dashboard
  ├─ Tabs: Dashboard | Bookings | Earnings | Profile | Settings
  ├─ Key feature: "Available Today?" toggle
  │
  ├─ Tab 2: Bookings
  │  ├─ Shows pending bookings
  │  ├─ Actions: [ACCEPT] [DECLINE]
  │  └─ Tap booking → View details
  │
  ├─ Tab 3: Earnings
  │  ├─ Shows total earnings
  │  ├─ Completed jobs count
  │  └─ Recent earnings breakdown
  │
  ├─ Tab 4: Profile
  │  ├─ Go to SCREEN 5B (Edit Profile)
  │  └─ Update any field
  │
  └─ Tab 5: Settings
     ├─ Help
     └─ Logout
```

#### CUSTOMER JOURNEY:

```
START
  ↓
SCREEN 1: Role Selection
  ├─ Tap "I'm a Customer"
  ↓
SCREEN 2: OTP Login
  ├─ First login: Phone number + OTP verified
  ├─ Not in users collection yet
  ↓
SCREEN 3C: Customer Profile Setup
  ├─ Enter: Name, Address, Photo (optional)
  ├─ Button: "START EXPLORING"
  ├─ [Save to Firestore: users/{uid}]
  ↓
SCREEN 6: Customer Dashboard (Search)
  ├─ Default tab: Search workers
  ├─ Features: Search bar, Filter, List of workers
  ├─ Tap worker card → SCREEN 7
  │
  ├─ Tab 2: Favorites
  │  ├─ Go to SCREEN 12
  │  ├─ View saved workers
  │  └─ [CALL] [BOOK] [REMOVE]
  │
  ├─ Tab 3: Bookings
  │  ├─ Go to SCREEN 13
  │  ├─ View all bookings (pending, accepted, completed)
  │  └─ Completed: [RATE & REVIEW]
  │
  ├─ Tab 4: Map
  │  ├─ Go to SCREEN 14
  │  ├─ View workers on map
  │  └─ Tap marker → Show worker details
  │
  └─ Tab 5: Profile
     ├─ Edit name, address, photo
     └─ Logout

SCREEN 7: Worker Details
  ├─ Show worker profile, ratings, gallery
  ├─ Actions:
  │  ├─ [CALL] → Opens phone dialer
  │  ├─ [BOOK] → SCREEN 8
  │  ├─ [+FAV] → Add to favorites
  │  └─ View All Reviews → SCREEN 10
  ↓
SCREEN 8: Booking Form
  ├─ Enter: Date, Time, Address, Notes
  ├─ Button: "CONFIRM BOOKING"
  ├─ [Create booking in Firestore]
  ↓
SCREEN 9: Booking Confirmation
  ├─ Show success message
  ├─ Button: "VIEW BOOKING STATUS"
  ├─ Link: "Back to Search"
  ↓
(Booking Status in SCREEN 13)
  ├─ Status: Pending → Waiting for worker
  ├─ Status: Accepted → Worker accepted
  ├─ Status: Completed → Job done
  ├─ If completed: Show [RATE & REVIEW]
  ↓
SCREEN 11: Rate & Review
  ├─ User rates worker (1-5 stars)
  ├─ User writes optional comment
  ├─ Button: "SUBMIT REVIEW"
  ├─ [Update worker's rating in Firestore]
  └─ Return to bookings list

SCREEN 10: All Reviews
  ├─ Shown from SCREEN 7 → "View All Reviews"
  ├─ List all reviews for worker
  └─ Rating distribution chart
```

---

## 6. DATABASE SCHEMA (FIRESTORE)

### Overview

Firestore is a NoSQL database. Data is organized into Collections and Documents.

### Collection: users/

**Purpose:** Store authentication & basic info for both worker & customer

**Document ID:** Firebase Auth UID (auto-generated, unique)

**Fields:**

| Field | Type | Description |
|-------|------|-------------|
| `uid` | String | User's unique ID (same as document ID) |
| `phone` | String | 10-digit phone number |
| `userType` | String | "worker" or "customer" |
| `name` | String | Full name |
| `address` | String | Home/work address |
| `profileImage` | String | Firebase Storage URL (optional) |
| `createdAt` | Timestamp | Account creation date |

**Example Document:**
```
users/abc123xyz {
  uid: "abc123xyz",
  phone: "9876543210",
  userType: "customer",
  name: "Amit Kumar",
  address: "123 Main St, Bangalore",
  profileImage: "https://...",
  createdAt: Timestamp(Jan 15, 2025)
}
```

---

### Collection: workers/

**Purpose:** Store worker-specific data including profile, skills, availability

**Document ID:** Worker's UID (same as users collection)

**Fields:**

| Field | Type | Description |
|-------|------|-------------|
| `uid` | String | Worker's unique ID |
| `name` | String | Name |
| `skill` | String | Primary skill (Electrician, Plumber, etc.) |
| `experience` | Number | Years of experience |
| `dailyRate` | Number | Daily wage in ₹ |
| `phone` | String | Phone number |
| `address` | String | Work address |
| `location` | GeoPoint | Latitude & longitude |
| `profileImage` | String | Firebase Storage URL |
| `workImages` | Array | URLs of 3 work portfolio photos |
| `isAvailable` | Boolean | **KEY: Availability toggle** |
| `rating` | Number | Average rating (0-5) |
| `reviewCount` | Number | Total number of reviews |
| `createdAt` | Timestamp | Profile creation date |

**Example Document:**
```
workers/worker123 {
  uid: "worker123",
  name: "Raju Kumar",
  skill: "Electrician",
  experience: 5,
  dailyRate: 800,
  phone: "9876543210",
  address: "Bangalore, Karnataka",
  location: GeoPoint(12.9716, 77.5946),
  profileImage: "https://...",
  workImages: ["https://...", "https://...", "https://..."],
  isAvailable: true,  ⭐ CRITICAL FOR REAL-TIME
  rating: 4.8,
  reviewCount: 245,
  createdAt: Timestamp(Jan 1, 2025)
}
```

---

### Collection: bookings/

**Purpose:** Store booking requests between customers and workers

**Document ID:** Auto-generated unique ID

**Fields:**

| Field | Type | Description |
|-------|------|-------------|
| `id` | String | Booking ID |
| `workerId` | String | Reference to workers/ collection |
| `customerId` | String | Reference to users/ collection |
| `date` | Timestamp | Booking date |
| `startTime` | String | "09:00 AM" format |
| `endTime` | String | "05:00 PM" format |
| `address` | String | Job address |
| `notes` | String | Special instructions (optional) |
| `totalPrice` | Number | Total price in ₹ |
| `status` | String | "pending", "accepted", "completed", "cancelled" |
| `createdAt` | Timestamp | Booking creation date |

**Example Document:**
```
bookings/BK123456 {
  id: "BK123456",
  workerId: "worker123",
  customerId: "customer456",
  date: Timestamp(Jan 15, 2025),
  startTime: "09:00 AM",
  endTime: "05:00 PM",
  address: "123 Main St, Bangalore",
  notes: "Need ceiling fan installation",
  totalPrice: 6400,
  status: "pending",
  createdAt: Timestamp(Jan 10, 2025)
}
```

---

### Collection: reviews/

**Purpose:** Store customer reviews for workers

**Document ID:** Auto-generated unique ID

**Fields:**

| Field | Type | Description |
|-------|------|-------------|
| `id` | String | Review ID |
| `workerId` | String | Reference to workers/ |
| `customerId` | String | Reference to users/ |
| `rating` | Number | 1-5 stars |
| `comment` | String | Review text (optional) |
| `timestamp` | Timestamp | Review date |

**Example Document:**
```
reviews/REV789 {
  id: "REV789",
  workerId: "worker123",
  customerId: "customer456",
  rating: 5,
  comment: "Great work! Very professional. Will hire again.",
  timestamp: Timestamp(Jan 16, 2025)
}
```

---

### Collection: favorites/

**Purpose:** Store customer's favorite workers

**Document ID:** "favorite_[customerId]_[workerId]"

**Fields:**

| Field | Type | Description |
|-------|------|-------------|
| `userId` | String | Customer's UID |
| `workerId` | String | Worker's UID |
| `addedAt` | Timestamp | Date added |

**Example Document:**
```
favorites/favorite_customer456_worker123 {
  userId: "customer456",
  workerId: "worker123",
  addedAt: Timestamp(Jan 10, 2025)
}
```

---

### Collection: messages/ (Optional - for future chat feature)

**Purpose:** Store in-app messages between users

**Document ID:** "[uid1]_[uid2]" (sorted, e.g., "abc_xyz")

**Sub-collection:** messages/

**Fields per message:**

| Field | Type | Description |
|-------|------|-------------|
| `id` | String | Message ID |
| `senderId` | String | Sender's UID |
| `receiverId` | String | Receiver's UID |
| `message` | String | Message text |
| `timestamp` | Timestamp | Sent time |
| `isRead` | Boolean | Read status |

---

## 7. ANDROID IMPLEMENTATION STEPS

### Phase 1: Project Setup (Days 1-2)

#### Step 1.1: Create Android Project (1 hour)

1. Open Android Studio
2. File → New → New Android Project
3. Choose "Empty Activity" template
4. Configure:
   - Name: **NammaKelsa**
   - Package: **com.nammakelsa**
   - Save location: Choose a folder
   - Language: **Kotlin**
   - Minimum SDK: **API 28** (Android 9)
   - Build configuration language: **Kotlin DSL**
5. Finish

#### Step 1.2: Create Firebase Project (1 hour)

1. Go to [firebase.google.com](https://firebase.google.com)
2. Sign in with Google account
3. Click "Go to console"
4. Create new project:
   - Name: **namma-kelsa-prod**
   - Enable Google Analytics: Optional
5. Create project
6. Click "Android" icon to add Android app
7. Configure:
   - Package name: **com.nammakelsa**
   - App nickname: **Namma Kelsa**
8. Download **google-services.json**
9. Copy to: **app/** folder in Android Studio project

#### Step 1.3: Update build.gradle Files (30 mins)

**Project-level build.gradle:**
```gradle
plugins {
  id 'com.android.application' version '8.0.0' apply false
  id 'com.android.library' version '8.0.0' apply false
  id 'org.jetbrains.kotlin.android' version '1.9.0' apply false
}

buildscript {
  repositories {
    google()
    mavenCentral()
  }
  dependencies {
    classpath 'com.google.gms:google-services:4.3.15'
  }
}
```

**App-level build.gradle:**
```gradle
plugins {
  id 'com.android.application'
  id 'kotlin-android'
  id 'com.google.gms.google-services'
}

android {
  compileSdk 34
  
  defaultConfig {
    applicationId "com.nammakelsa"
    minSdk 28
    targetSdk 34
    versionCode 1
    versionName "1.0.0"
  }
  
  buildTypes {
    release {
      minifyEnabled true
      proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
    }
  }
  
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
  
  kotlinOptions {
    jvmTarget = '11'
  }
}

dependencies {
  // AndroidX
  implementation 'androidx.appcompat:appcompat:1.6.1'
  implementation 'androidx.constraintlayout:constraintlayout:2.1.4'
  implementation 'androidx.core:core-ktx:1.12.0'
  
  // Lifecycle & ViewModel
  implementation 'androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2'
  implementation 'androidx.lifecycle:lifecycle-livedata-ktx:2.6.2'
  implementation 'androidx.lifecycle:lifecycle-runtime-ktx:2.6.2'
  
  // Material Design 3
  implementation 'com.google.android.material:material:1.10.0'
  
  // Firebase BOM (Bill of Materials) - ensures compatible versions
  implementation platform('com.google.firebase:firebase-bom:32.3.1')
  implementation 'com.google.firebase:firebase-auth-ktx'
  implementation 'com.google.firebase:firebase-firestore-ktx'
  implementation 'com.google.firebase:firebase-storage-ktx'
  implementation 'com.google.firebase:firebase-messaging-ktx'
  
  // Google Maps & Location
  implementation 'com.google.android.gms:play-services-maps:18.2.0'
  implementation 'com.google.android.gms:play-services-location:21.0.1'
  
  // GeoFirestore for radius queries
  implementation 'org.imperiumlabs:geofirestore-android:1.2.0'
  
  // Image Loading (Glide)
  implementation 'com.github.bumptech.glide:glide:4.16.0'
  kapt 'com.github.bumptech.glide:compiler:4.16.0'
  
  // Coroutines
  implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.1'
  implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.1'
  
  // Testing
  testImplementation 'junit:junit:4.13.2'
  androidTestImplementation 'androidx.test.espresso:espresso-core:3.5.1'
}
```

---

### Phase 2: Add Permissions & Manifest (Day 2)

#### Step 2.1: Update AndroidManifest.xml

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

  <!-- Permissions -->
  <uses-permission android:name="android.permission.INTERNET" />
  <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
  <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
  <uses-permission android:name="android.permission.CAMERA" />
  <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
  <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
  <uses-permission android:name="android.permission.CALL_PHONE" />

  <application
    android:name=".NammaKelsaApp"
    android:allowBackup="false"
    android:usesCleartextTraffic="false"
    android:theme="@style/Theme.AppCompat.Light.DarkActionBar">

    <!-- Google Maps API Key -->
    <meta-data
      android:name="com.google.android.geo.API_KEY"
      android:value="YOUR_GOOGLE_MAPS_API_KEY" />

    <!-- Activities -->
    <activity
      android:name=".ui.auth.RoleSelectionActivity"
      android:exported="true">
      <intent-filter>
        <action android:name="android.intent.action.MAIN" />
        <category android:name="android.intent.category.LAUNCHER" />
      </intent-filter>
    </activity>

    <activity
      android:name=".ui.auth.OTPActivity"
      android:exported="false" />

    <activity
      android:name=".ui.worker.WorkerProfileActivity"
      android:exported="false" />

    <activity
      android:name=".ui.worker.WorkerDashboardActivity"
      android:exported="false" />

    <activity
      android:name=".ui.customer.CustomerSearchActivity"
      android:exported="false" />

    <activity
      android:name=".ui.customer.WorkerDetailsActivity"
      android:exported="false" />

    <activity
      android:name=".ui.customer.BookingActivity"
      android:exported="false" />

    <activity
      android:name=".ui.common.MapActivity"
      android:exported="false" />

  </application>

</manifest>
```

---

### Phase 3: Create Data Models (Day 3)

Create Kotlin data classes that match Firestore documents:

#### File: data/model/Worker.kt

```kotlin
data class Worker(
  val uid: String = "",
  val name: String = "",
  val skill: String = "",
  val experience: Int = 0,
  val dailyRate: Double = 0.0,
  val phone: String = "",
  val address: String = "",
  val profileImage: String = "",
  val workImages: List<String> = emptyList(),
  val isAvailable: Boolean = false,
  val rating: Double = 0.0,
  val reviewCount: Int = 0
)
```

#### File: data/model/User.kt

```kotlin
data class User(
  val uid: String = "",
  val phone: String = "",
  val userType: String = "",  // "worker" or "customer"
  val name: String = "",
  val address: String = "",
  val profileImage: String = ""
)
```

#### File: data/model/Booking.kt

```kotlin
data class Booking(
  val id: String = "",
  val workerId: String = "",
  val customerId: String = "",
  val date: Long = 0,
  val startTime: String = "",
  val endTime: String = "",
  val address: String = "",
  val notes: String = "",
  val totalPrice: Double = 0.0,
  val status: String = "pending"  // pending, accepted, completed, cancelled
)
```

#### File: data/model/Review.kt

```kotlin
data class Review(
  val id: String = "",
  val workerId: String = "",
  val customerId: String = "",
  val rating: Float = 0f,
  val comment: String = "",
  val timestamp: Long = 0
)
```

---

### Phase 4: Firebase Authentication (Day 3-4)

#### Step 4.1: Create OTPActivity.kt

```kotlin
package com.nammakelsa.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

class OTPActivity : AppCompatActivity() {
  
  private lateinit var auth: FirebaseAuth
  private lateinit var phoneInput: EditText
  private lateinit var sendOtpButton: Button
  private var verificationId: String = ""
  
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_otp)
    
    auth = FirebaseAuth.getInstance()
    phoneInput = findViewById(R.id.phoneInput)
    sendOtpButton = findViewById(R.id.sendOtpButton)
    
    sendOtpButton.setOnClickListener {
      val phone = phoneInput.text.toString().trim()
      if (phone.isEmpty() || phone.length != 10) {
        Toast.makeText(this, "Enter valid 10-digit phone number", Toast.LENGTH_SHORT).show()
        return@setOnClickListener
      }
      
      val fullPhone = "+91$phone"  // India country code
      sendOTP(fullPhone)
    }
  }
  
  private fun sendOTP(phoneNumber: String) {
    val options = PhoneAuthOptions.newBuilder(auth)
      .setPhoneNumber(phoneNumber)
      .setTimeout(60L, TimeUnit.SECONDS)
      .setActivity(this)
      .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        
        override fun onVerificationCompleted(credential: com.google.firebase.auth.PhoneAuthCredential) {
          signInWithCredential(credential)
        }
        
        override fun onVerificationFailed(e: com.google.firebase.FirebaseException) {
          Toast.makeText(this@OTPActivity, "Verification failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
        
        override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
          this@OTPActivity.verificationId = verificationId
          Toast.makeText(this@OTPActivity, "OTP sent to $phoneNumber", Toast.LENGTH_SHORT).show()
          // Transition to OTP input screen
          showOTPInput()
        }
      })
      .build()
    
    PhoneAuthProvider.verifyPhoneNumber(options)
  }
  
  private fun signInWithCredential(credential: com.google.firebase.auth.PhoneAuthCredential) {
    auth.signInWithCredential(credential)
      .addOnCompleteListener { task ->
        if (task.isSuccessful) {
          val user = task.result?.user
          Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()
          // Check if profile exists, route to appropriate screen
          checkUserProfileAndRoute()
        } else {
          Toast.makeText(this, "Login failed", Toast.LENGTH_SHORT).show()
        }
      }
  }
  
  private fun checkUserProfileAndRoute() {
    // TODO: Query Firestore to check if user exists
    // If not: Go to profile setup screen
    // If yes: Go to dashboard
  }
  
  private fun showOTPInput() {
    // TODO: Switch layout to show OTP input boxes
  }
}
```

---

### Phase 5: Real-Time Firestore Listener (Critical) (Day 5)

#### Step 5.1: WorkerRepository.kt - Real-time Worker Query

```kotlin
package com.nammakelsa.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.nammakelsa.data.model.Worker

class WorkerRepository(private val firestore: FirebaseFirestore) {
  
  // ⭐ CRITICAL: Real-time listener for workers by skill and availability
  fun searchWorkersBySkillRealtime(
    skill: String,
    callback: (List<Worker>) -> Unit,
    errorCallback: (Exception) -> Unit
  ) {
    firestore.collection("workers")
      .whereEqualTo("skill", skill)
      .whereEqualTo("isAvailable", true)  // ← Only available workers
      .addSnapshotListener { value, error ->  // ← REAL-TIME LISTENER
        if (error != null) {
          errorCallback(error)
          return@addSnapshotListener
        }
        
        val workers = value?.toObjects(Worker::class.java) ?: emptyList()
        callback(workers)  // ← UI updates instantly
      }
  }
  
  // Update worker's availability toggle
  fun updateAvailability(workerId: String, isAvailable: Boolean) {
    firestore.collection("workers")
      .document(workerId)
      .update("isAvailable", isAvailable)  // ← Single field update
      .addOnSuccessListener {
        // Show success toast
      }
      .addOnFailureListener { e ->
        // Show error
      }
  }
  
  // Get specific worker details
  fun getWorkerDetails(workerId: String, callback: (Worker?) -> Unit) {
    firestore.collection("workers")
      .document(workerId)
      .get()
      .addOnSuccessListener { doc ->
        val worker = doc.toObject(Worker::class.java)
        callback(worker)
      }
  }
  
  // Create booking
  fun createBooking(booking: Booking, callback: (Boolean) -> Unit) {
    firestore.collection("bookings")
      .add(booking)
      .addOnSuccessListener {
        callback(true)
      }
      .addOnFailureListener {
        callback(false)
      }
  }
  
  // Add review
  fun addReview(review: Review, callback: (Boolean) -> Unit) {
    firestore.collection("reviews")
      .add(review)
      .addOnSuccessListener {
        // Recalculate worker's average rating
        updateWorkerRating(review.workerId)
        callback(true)
      }
      .addOnFailureListener {
        callback(false)
      }
  }
  
  // Calculate and update worker's average rating
  private fun updateWorkerRating(workerId: String) {
    firestore.collection("reviews")
      .whereEqualTo("workerId", workerId)
      .get()
      .addOnSuccessListener { documents ->
        val ratings = documents.mapNotNull { (it["rating"] as? Number)?.toDouble() }
        val avgRating = if (ratings.isNotEmpty()) ratings.average() else 0.0
        
        firestore.collection("workers")
          .document(workerId)
          .update(
            "rating", avgRating,
            "reviewCount", documents.size()
          )
      }
  }
}
```

---

## 8. FEATURE-BY-FEATURE DEVELOPMENT GUIDE

### FEATURE 1: Real-Time Availability Sync ⭐ CRITICAL

**Why Important:** When worker toggles "Available Today", customers must see change instantly (< 1 second)

**Worker Side (Toggle):**
```kotlin
// In WorkerDashboardActivity
availabilitySwitch.setOnCheckedChangeListener { _, isChecked ->
  val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@setOnCheckedChangeListener
  
  // Update Firestore instantly
  firestore.collection("workers").document(uid)
    .update("isAvailable", isChecked)
    .addOnSuccessListener {
      Toast.makeText(
        this,
        if (isChecked) "You're now available" else "You're offline",
        Toast.LENGTH_SHORT
      ).show()
    }
    .addOnFailureListener { e ->
      Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}
```

**Customer Side (Search):**
```kotlin
// In CustomerSearchActivity
private fun setupSearchListener() {
  val searchQuery = "Electrician"  // Example
  
  firestore.collection("workers")
    .whereEqualTo("skill", searchQuery)
    .whereEqualTo("isAvailable", true)  // ← KEY
    .addSnapshotListener { value, error ->  // ← REAL-TIME
      if (error != null) {
        Log.w(TAG, "Error: $error")
        return@addSnapshotListener
      }
      
      val workers = value?.toObjects(Worker::class.java) ?: emptyList()
      // Update RecyclerView with fresh list
      workerAdapter.submitList(workers)
    }
}
```

**Testing:**
1. Open app on Worker device
2. Open app on Customer device
3. Customer searches for "Electrician"
4. Worker toggles "Available Today" OFF
5. ✅ Customer's list updates in < 1 second (worker disappears)
6. Worker toggles ON
7. ✅ Worker reappears in customer's list instantly

---

### FEATURE 2: Call Button

**Implementation:**
```kotlin
// In WorkerDetailsActivity
callButton.setOnClickListener {
  val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:+91${worker.phone}"))
  startActivity(intent)  // Opens native phone dialer
}
```

**Result:** Clicking the button opens the phone's dialer app with worker's number pre-filled. User can confirm to call.

**Note:** Uses `ACTION_DIAL` (safe, no permission needed) instead of `ACTION_CALL` (requires permission).

---

### FEATURE 3: Booking System

**Customer initiates booking (SCREEN 8):**
```kotlin
val booking = Booking(
  id = firestore.collection("bookings").document().id,
  workerId = selectedWorker.uid,
  customerId = currentUser.uid,
  date = selectedDate.time,
  startTime = startTime,
  endTime = endTime,
  address = selectedAddress,
  notes = specialNotes,
  totalPrice = calculatePrice(selectedWorker.dailyRate, hours),
  status = "pending"
)

firestore.collection("bookings")
  .document(booking.id)
  .set(booking)
  .addOnSuccessListener {
    // Send notification to worker (Firebase Cloud Messaging)
    sendNotificationToWorker(selectedWorker.uid, "New booking request!")
    // Go to confirmation screen
    startActivity(Intent(this, BookingConfirmationActivity::class.java))
  }
```

**Worker accepts/declines booking:**
```kotlin
// Accept
firestore.collection("bookings").document(bookingId)
  .update("status", "accepted")
  .addOnSuccessListener {
    sendNotificationToCustomer("Worker accepted your booking!")
  }

// Decline
firestore.collection("bookings").document(bookingId)
  .update("status", "cancelled")
  .addOnSuccessListener {
    sendNotificationToCustomer("Worker declined your booking")
  }
```

---

### FEATURE 4: Rating & Review System

**After booking completed, customer rates:**
```kotlin
val review = Review(
  id = firestore.collection("reviews").document().id,
  workerId = booking.workerId,
  customerId = currentUser.uid,
  rating = selectedStars.toFloat(),
  comment = reviewComment,
  timestamp = System.currentTimeMillis()
)

firestore.collection("reviews")
  .document(review.id)
  .set(review)
  .addOnSuccessListener {
    // Recalculate worker's average rating
    updateWorkerRating(booking.workerId)
    Toast.makeText(this, "Review submitted!", Toast.LENGTH_SHORT).show()
  }
```

**Recalculate average:**
```kotlin
fun updateWorkerRating(workerId: String) {
  firestore.collection("reviews")
    .whereEqualTo("workerId", workerId)
    .get()
    .addOnSuccessListener { result ->
      val ratings = result.documents.mapNotNull { doc ->
        (doc["rating"] as? Number)?.toDouble()
      }
      
      val avgRating = if (ratings.isNotEmpty()) {
        ratings.average()
      } else {
        0.0
      }
      
      firestore.collection("workers")
        .document(workerId)
        .update(
          "rating", avgRating,
          "reviewCount", result.size()
        )
    }
}
```

---

### FEATURE 5: Favorites Management

**Add to favorites:**
```kotlin
val favoriteId = "favorite_${currentUser.uid}_${worker.uid}"
val favorite = hashMapOf(
  "userId" to currentUser.uid,
  "workerId" to worker.uid,
  "addedAt" to FieldValue.serverTimestamp()
)

firestore.collection("favorites")
  .document(favoriteId)
  .set(favorite)
  .addOnSuccessListener {
    heartIcon.setImageResource(R.drawable.ic_heart_filled)  // Filled heart
  }
```

**Get favorites list:**
```kotlin
firestore.collection("favorites")
  .whereEqualTo("userId", currentUser.uid)
  .get()
  .addOnSuccessListener { docs ->
    val favWorkerIds = docs.documents.map { it["workerId"] as String }
    
    // Load full worker details for each ID
    favWorkerIds.forEach { workerId ->
      firestore.collection("workers")
        .document(workerId)
        .get()
        .addOnSuccessListener { doc ->
          val worker = doc.toObject(Worker::class.java)
          // Add to list
        }
    }
  }
```

---

## 9. TESTING & DEPLOYMENT CHECKLIST

### Manual Testing Checklist

- [ ] **Authentication:**
  - [ ] OTP login works with Firebase test phone number
  - [ ] OTP verification succeeds
  - [ ] First-time user routed to profile setup
  - [ ] Returning user routed to dashboard
  - [ ] Session persists after app restart
  - [ ] Logout clears session

- [ ] **Worker Features:**
  - [ ] Profile setup saves to Firestore
  - [ ] Work portfolio photos upload to Firebase Storage
  - [ ] Availability toggle updates Firestore
  - [ ] Availability appears/disappears in customer search instantly
  - [ ] Bookings received appear in Bookings tab
  - [ ] Can accept/decline bookings
  - [ ] Earnings calculated correctly
  - [ ] Profile edit updates Firestore

- [ ] **Customer Features:**
  - [ ] Search returns correct workers
  - [ ] Filters work (skill, distance, rating, availability)
  - [ ] Worker details screen displays correctly
  - [ ] Call button opens phone dialer
  - [ ] Booking form saves correctly
  - [ ] Bookings list shows history
  - [ ] Can rate & review after booking
  - [ ] Favorites add/remove works
  - [ ] Map shows worker locations

- [ ] **Data Integrity:**
  - [ ] Different phone numbers show different accounts
  - [ ] Reviews update worker's average rating
  - [ ] Images load with Glide
  - [ ] Real-time updates work across devices
  - [ ] Firestore offline mode works

### Device Testing

- [ ] Android 9 (API 28) phone
- [ ] Android 10 (API 29) phone
- [ ] Android 12+ (API 31+) phone
- [ ] Various screen sizes (4.5", 5.5", 6.5")
- [ ] Tablet (7"+)

### Firebase Configuration Checklist

- [ ] Firebase project created
- [ ] Android app registered
- [ ] google-services.json added to project
- [ ] Firestore Database created (test mode initially)
- [ ] Firebase Authentication enabled (Phone)
- [ ] Firebase Storage enabled with rules:
  ```
  rules_version = '2';
  service firebase.storage {
    match /b/{bucket}/o {
      match /workers/{uid}/{allPaths=**} {
        allow read: if true;
        allow write: if request.auth.uid == uid;
      }
    }
  }
  ```
- [ ] Firestore Security Rules:
  ```
  rules_version = '2';
  service cloud.firestore {
    match /databases/{database}/documents {
      
      // Users: readable by all, writable by self
      match /users/{uid} {
        allow read: if true;
        allow write: if request.auth.uid == uid;
      }
      
      // Workers: readable by all, writable by self
      match /workers/{uid} {
        allow read: if true;
        allow write: if request.auth.uid == uid;
      }
      
      // Bookings: readable by involved parties, writable by customer
      match /bookings/{bookingId} {
        allow read: if resource.data.workerId == request.auth.uid || 
                       resource.data.customerId == request.auth.uid;
        allow create: if request.auth.uid == request.resource.data.customerId;
        allow update: if resource.data.workerId == request.auth.uid;
      }
      
      // Reviews: readable by all, writable by customer
      match /reviews/{reviewId} {
        allow read: if true;
        allow create: if request.auth.uid == request.resource.data.customerId;
      }
      
      // Favorites: readable/writable by self
      match /favorites/{favoriteId} {
        allow read, write: if request.auth.uid == resource.data.userId;
      }
    }
  }
  ```

### Build & Release

1. **Configure signing key:**
   - Build → Generate Signed Bundle/APK
   - Select Release variant
   - Create new key if needed (save safely)
   - Generate APK

2. **Prepare for Play Store:**
   - Create app entry in Google Play Console
   - Fill app title, description, category
   - Upload screenshots (5+ per device type)
   - Set pricing (free recommended)
   - Create privacy policy
   - Create terms of service

3. **Upload & Submit:**
   - Upload signed APK/AAB
   - Fill out questionnaire
   - Submit for review
   - Wait for approval (24-48 hours typically)

---

## 10. APPENDIX: QUICK REFERENCE

### File Structure

```
app/src/main/
├── java/com/nammakelsa/
│   ├── ui/
│   │   ├── auth/
│   │   │   ├── RoleSelectionActivity.kt
│   │   │   └── OTPActivity.kt
│   │   ├── worker/
│   │   │   ├── WorkerProfileActivity.kt
│   │   │   ├── WorkerDashboardActivity.kt
│   │   │   └── WorkerBookingsFragment.kt
│   │   ├── customer/
│   │   │   ├── CustomerSearchActivity.kt
│   │   │   ├── WorkerDetailsActivity.kt
│   │   │   ├── BookingActivity.kt
│   │   │   └── ReviewActivity.kt
│   │   └── common/
│   │       ├── MapActivity.kt
│   │       └── FavoritesActivity.kt
│   ├── data/
│   │   ├── model/
│   │   │   ├── Worker.kt
│   │   │   ├── User.kt
│   │   │   ├── Booking.kt
│   │   │   └── Review.kt
│   │   └── repository/
│   │       └── WorkerRepository.kt
│   ├── viewmodel/
│   │   └── WorkerViewModel.kt
│   ├── adapter/
│   │   └── WorkerAdapter.kt
│   ├── NammaKelsaApp.kt
│   └── Constants.kt
└── res/
    ├── layout/
    │   ├── activity_role_selection.xml
    │   ├── activity_otp.xml
    │   ├── activity_worker_dashboard.xml
    │   ├── activity_customer_search.xml
    │   ├── activity_worker_details.xml
    │   ├── activity_booking.xml
    │   └── item_worker_card.xml
    ├── drawable/
    │   ├── ic_worker.xml
    │   ├── ic_customer.xml
    │   ├── ic_call.xml
    │   ├── ic_heart.xml
    │   └── ic_heart_filled.xml
    ├── values/
    │   ├── colors.xml
    │   ├── strings.xml
    │   ├── dimens.xml
    │   └── themes.xml
    └── menu/
        └── bottom_navigation.xml
```

### Common Firestore Queries (Kotlin)

```kotlin
// Get available electricians
firestore.collection("workers")
  .whereEqualTo("skill", "Electrician")
  .whereEqualTo("isAvailable", true)
  .get()

// Get worker's bookings
firestore.collection("bookings")
  .whereEqualTo("workerId", workerId)
  .orderBy("date", Query.Direction.DESCENDING)
  .get()

// Get reviews for a worker
firestore.collection("reviews")
  .whereEqualTo("workerId", workerId)
  .orderBy("timestamp", Query.Direction.DESCENDING)
  .get()

// Get user's favorites
firestore.collection("favorites")
  .whereEqualTo("userId", currentUser.uid)
  .get()
```

### Key Constants

```kotlin
object Constants {
  const val SKILL_ELECTRICIAN = "Electrician"
  const val SKILL_PLUMBER = "Plumber"
  const val SKILL_CARPENTER = "Carpenter"
  const val SKILL_PAINTER = "Painter"
  
  const val STATUS_PENDING = "pending"
  const val STATUS_ACCEPTED = "accepted"
  const val STATUS_COMPLETED = "completed"
  const val STATUS_CANCELLED = "cancelled"
  
  const val USER_TYPE_WORKER = "worker"
  const val USER_TYPE_CUSTOMER = "customer"
  
  const val PREF_USER_TYPE = "user_type"
  const val PREF_USER_ID = "user_id"
}
```

### Project Timeline Estimate

| Phase | Tasks | Duration |
|-------|-------|----------|
| 1 | Setup, Firebase, Dependencies | 2-3 days |
| 2 | Auth (OTP), Data Models | 2-3 days |
| 3 | Worker Profile, Availability Toggle | 3-4 days |
| 4 | Customer Search, Worker Details | 3-4 days |
| 5 | Booking, Reviews, Favorites | 2-3 days |
| 6 | Testing, Bug Fixes, Polish | 2-3 days |
| 7 | Play Store Submission, Deployment | 1-2 days |
| **TOTAL** | | **15-22 days** |

---

## SUMMARY

This document provides **complete specification** for building Namma-Kelsa. It includes:

✅ **Architecture**: MVVM pattern, Firestore database, real-time updates  
✅ **14 Screens**: Full UI/UX design for both workers and customers  
✅ **Database Schema**: Detailed Firestore collections and fields  
✅ **Implementation**: Step-by-step Android development guide  
✅ **Features**: Real-time availability, call integration, booking system, ratings  
✅ **Testing**: Manual testing checklist and deployment guide  

**Follow this SOP sequentially to build a production-ready app.**

For questions during development, refer back to the specific sections. All code snippets are copy-paste ready.

---

**Prepared for: Anti-Gravity Development Team**  
**Date: January 2025**  
**Status: Ready for Implementation**

