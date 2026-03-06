# TestyBites - Food Delivery Application 🍔🛵

An Android-based food delivery application developed as part of the industrial internship program by **upskill Campus** and **UniConverge Technologies Pvt Ltd (UCT)**. 

This project features a complete real-time ecosystem with three distinct role-based modules: Admin, User, and Delivery Personnel. It relies on a centralized Firebase cloud backend for real-time data synchronization.

## 🚀 Key Features

### 1. User Module
* **Browse Menu:** Search and filter food items dynamically.
* **Cart Management:** Add items, adjust quantities, and calculate grand totals including taxes and delivery fees.
* **Checkout & Payments:** Supports Cash on Delivery (COD) and a simulated Mock Online Payment gateway.
* **Order Tracking:** Live map view using OSMDroid to visualize delivery locations.
* **Rewards System:** Automatically grants loyalty points upon successful order placement.

### 2. Admin Module (Access Code: `admin123`)
* **Menu Management:** Upload new food items with images, prices, and descriptions directly to Firestore.
* **Order Dashboard:** View active and historical orders.
* **Real-time Status Updates:** Change order states (Pending -> Accepted -> Out for Delivery), which instantly syncs to the User's device.
* **Revenue Tracking:** Aggregates and displays live administrative revenue.

### 3. Delivery Module (Access Code: `driver123`)
* **Task Management:** Filters and displays only active orders marked as "Accepted" or "Out for Delivery".
* **Delivery Confirmation:** Allows drivers to mark orders as "Delivered", automatically moving them to the admin's history tab.

## 🛠️ Technology Stack
* **Frontend:** Java, XML (Android Studio)
* **Backend Database:** Firebase Firestore (NoSQL Real-time DB)
* **Authentication:** Firebase Auth (Email/Password)
* **Storage:** Firebase Cloud Storage (Food images)
* **Push Notifications:** Firebase Cloud Messaging (FCM)
* **Map API:** OSMDroid 
* **Image Caching:** Glide Library

## 📁 Repository Contents
* `FoodDeliveryApplication.java` - The consolidated core Java source code for the application's major activities, adapters, and models.
* `FoodDeliveryApplication_Mayuri_USC_UCT.pdf` - The comprehensive 6-week industrial internship project report detailing system architecture, data flow diagrams, and testing outcomes.
