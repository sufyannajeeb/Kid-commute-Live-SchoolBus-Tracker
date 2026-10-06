# 🚌 School Bus Live Tracking System

A complete **School Bus Live Tracking and Management System** designed to improve the safety, communication, and management of school transportation.

The system consists of an **Admin Web Application** and **Android Applications** for **Drivers, Ayahs, and Parents**.

Parents can track their child's school bus live based on the driver's current GPS location, while Ayahs and Parents can communicate through an in-app chat system. Administrators can manage students, buses, drivers, allocations, complaints, and monitor buses in real time.

---

## 📌 Project Overview

The School Bus Live Tracking System provides a centralized platform connecting:

* 👨‍💼 **Admin**
* 🚌 **Driver**
* 👩‍🏫 **Ayah**
* 👨‍👩‍👧 **Parent**

### 🎯 Main Goals

* Provide real-time school bus tracking
* Improve student transportation safety
* Allow parents to monitor their child's bus
* Enable communication between Parents and Ayahs
* Allow administrators to manage transportation efficiently
* Provide bus status updates
* Manage student and bus allocations
* Reduce dependency on manual communication

---

# ✨ Key Features

## 👨‍💼 Admin Web Application

The Admin Web Application provides centralized management of the school transportation system.

### Admin Features

* 🔐 Admin Login
* 👨‍🎓 Register and manage students
* 👨‍👩‍👧 Manage parent information
* 🚌 Register and manage buses
* 👨‍✈️ Manage drivers
* 👩‍🏫 Manage Ayahs
* 📋 Allocate students to buses
* 🚌 Allocate drivers to buses
* 👩‍🏫 Allocate Ayahs to buses
* 📍 View live bus locations
* 📊 Monitor bus status
* 📝 View driver-updated bus status
* ⚠️ View and manage parent complaints
* 🗂️ Manage transportation records

---

# 🚌 Driver Android Application

The Driver Android Application is used during the school bus trip.

### Driver Features

* 🔐 Driver Login
* 🚌 View assigned bus
* 🗺️ Share current GPS location
* 📍 Continuously provide location for live tracking
* 📝 Update bus status
* 📤 Send bus status updates
* 👨‍🎓 View assigned students
* 👩‍🏫 View assigned Ayah information

The driver's current location acts as the primary location source for live bus tracking.

---

# 👩‍🏫 Ayah Android Application

The Ayah application helps staff assist students during transportation.

### Ayah Features

* 🔐 Ayah Login
* 🚌 View assigned bus
* 👨‍🎓 View assigned students
* 📍 View relevant bus/trip information
* 💬 Chat with parents
* 📩 Send messages to parents
* 🔔 Receive messages from parents

---

# 👨‍👩‍👧 Parent Android Application

The Parent Application allows parents to monitor their child's transportation.

### Parent Features

* 🔐 Parent Login
* 👨‍🎓 View child/student information
* 🚌 View assigned bus
* 📍 Track school bus live
* 🗺️ View driver's current location
* 💬 Chat with Ayah
* 📩 Send and receive messages
* ⚠️ Submit complaints
* 📝 Report transportation issues
* 🚌 View bus status

Parents can check the current bus location through the application instead of depending on phone calls.

---

# 📍 Live Bus Tracking

One of the main features of the project is **real-time school bus tracking**.

### How It Works

```text
┌─────────────────────────┐
│    Driver Android App   │
│                         │
│      📍 GPS Location    │
└────────────┬────────────┘
             │
             │ Live Location
             ▼
┌─────────────────────────┐
│        Backend          │
│                         │
│ Store / Process         │
│ Bus Location            │
└────────────┬────────────┘
             │
             │ Latest Location
             ▼
┌─────────────────────────┐
│    Parent Android App   │
│                         │
│    🚌 Live Bus Map      │
└─────────────────────────┘
```

### Tracking Flow

1. Driver logs into the application.
2. Driver starts the bus trip.
3. The application obtains the driver's GPS location.
4. Location information is sent to the backend.
5. Backend updates the bus's current location.
6. Parent application receives the latest location.
7. Parent views the bus moving on the map.
8. Admin can also monitor the current bus location.

---

# 💬 Parent–Ayah Chat

The system provides direct communication between Parents and Ayahs.

### Chat Features

* 💬 Send messages
* 📩 Receive messages
* 👩‍🏫 Parent ↔ Ayah communication
* 🕐 Conversation history
* 🔔 Message notifications *(if implemented)*

### Communication Flow

```text
Parent App
     │
     │ Message
     ▼
┌─────────────┐
│   Backend   │
└──────┬──────┘
       │
       │ Message
       ▼
   Ayah App
```

---

# 📝 Bus Status Updates

Drivers can manually update the current bus status.

Example:

```text
Driver
   │
   │ "Bus delayed due to traffic"
   ▼
Backend
   │
   ▼
Admin
```

### Possible Bus Statuses

* 🟢 Bus Started
* 🚌 Bus On Route
* ⏸️ Bus Stopped
* 🚦 Delayed
* 🏁 Trip Completed
* ⚠️ Custom Status

---

# ⚠️ Complaint Management

Parents can submit transportation-related complaints through the Parent Application.

### Complaint Types

* 🚌 Bus-related problems
* 👨‍✈️ Driver-related concerns
* ⏱️ Delay complaints
* 👨‍🎓 Student transportation issues
* ⚠️ Other transportation-related issues

### Complaint Flow

```text
Parent
   │
   │ Submit Complaint
   ▼
Backend
   │
   ▼
Admin Web Application
   │
   ▼
Complaint Management
```

---

# 👨‍🎓 Student Allocation

The Admin can allocate students to their appropriate buses and transportation staff.

```text
                 Admin
                   │
        ┌──────────┼──────────┐
        ▼          ▼          ▼
     Student      Bus       Staff
                            │
                       ┌────┴────┐
                       ▼         ▼
                    Driver      Ayah
                       │         │
                       └────┬────┘
                            ▼
                       Allocation
```

The allocation information can then be accessed by the Parent, Driver, and Ayah applications.

---

# 🏗️ System Architecture

```text
                    SCHOOL BUS SYSTEM
                           │
             ┌─────────────┴─────────────┐
             │                           │
             ▼                           ▼
      ADMIN WEB APP                ANDROID APPS
             │                           │
             │              ┌────────────┼────────────┐
             │              │            │            │
             │              ▼            ▼            ▼
             │           Driver         Ayah        Parent
             │              │            │            │
             └──────────────┴────────────┴────────────┘
                            │
                            ▼
                       Backend / API
                            │
                            ▼
                         Database
```

---

# 🔄 Complete System Flow

```text
                         ADMIN
                           │
            ┌──────────────┼──────────────┐
            │              │              │
            ▼              ▼              ▼
        Students          Buses          Staff
                                         │
                                  ┌──────┴──────┐
                                  │             │
                                Driver         Ayah
                                  │             │
                                  └──────┬──────┘
                                         │
                                         ▼
                                     Allocation
                                         │
                                         ▼
                                  Driver Starts Trip
                                         │
                                         ▼
                                     Driver GPS
                                         │
                                         ▼
                                      Backend
                                         │
                              ┌──────────┴──────────┐
                              │                     │
                              ▼                     ▼
                            Admin                 Parent
                              │                     │
                              │                     ▼
                              │               Live Bus Map
                              │
                              ▼
                          Monitoring
```

---

# 📱 Applications

| User            | Platform | Main Purpose                       |
| --------------- | -------- | ---------------------------------- |
| 👨‍💼 Admin     | Web      | Complete transportation management |
| 🚌 Driver       | Android  | GPS location sharing & bus status  |
| 👩‍🏫 Ayah      | Android  | Student assistance & communication |
| 👨‍👩‍👧 Parent | Android  | Live bus tracking & communication  |

---

# 🛠️ Technologies Used

## 🌐 Web Application

* HTML
* CSS
* JavaScript
* Web-based Admin Dashboard

## 📱 Android Applications

* Dart
* Flutter

## ⚙️ Backend

* JavaScript
* API-based communication

## 🔧 Core Technologies

* GPS / Geolocation
* Real-time / Near-real-time location updates
* REST APIs
* Database
* Map Integration
* Authentication
* Chat / Communication System

> **Note:** Update the backend, database, and map technology names according to the final implementation.

---

# 📂 Project Structure

```text
School-Bus-Live-Tracking/
│
├── admin/
│   ├── dashboard/
│   ├── students/
│   ├── buses/
│   ├── drivers/
│   ├── ayahs/
│   ├── allocations/
│   ├── complaints/
│   └── tracking/
│
├── mobile/
│   ├── driver/
│   ├── ayah/
│   └── parent/
│
├── backend/
│   ├── api/
│   ├── controllers/
│   ├── routes/
│   └── services/
│
├── database/
│
├── assets/
│
├── README.md
└── .gitignore
```

---

# 🔐 Security

The system should ensure that transportation information is accessible only to authorized users.

### Security Considerations

* 🔐 Secure login
* 👥 Role-based access
* 🔑 Password protection
* 🛡️ API authentication
* 🔒 HTTPS communication
* 🗺️ Protected live-location data
* 🔐 Secure database access
* 🚫 Unauthorized access prevention

Parents should only be able to access information related to their registered child/student.

---

# 🎯 Objectives

1. Provide live school bus tracking.
2. Improve student transportation safety.
3. Allow parents to monitor their child's bus.
4. Provide direct communication between Parents and Ayahs.
5. Allow Admins to efficiently manage school transportation.
6. Allow Drivers to share their live location.
7. Provide a platform for reporting transportation complaints.
8. Provide real-time bus status information.
9. Maintain student, bus, driver, and Ayah allocations.
10. Reduce dependency on manual communication.

---

# 🌟 Advantages

* ✅ Real-time bus tracking
* ✅ Easy student allocation
* ✅ Centralized transportation management
* ✅ Parent–Ayah communication
* ✅ Driver location sharing
* ✅ Bus status updates
* ✅ Complaint management
* ✅ Admin live monitoring
* ✅ Improved transportation transparency
* ✅ Better parent awareness and safety

---

# 🔮 Future Enhancements

Possible future improvements include:

* 🔔 Push notifications
* ⏱️ Estimated Time of Arrival (ETA)
* 🗺️ Route visualization
* 📍 Geofencing
* 🚏 Bus-stop notifications
* 🚨 Emergency / SOS functionality
* 📊 Trip history and analytics
* 📈 Transportation reports
* 🔋 Driver device/battery monitoring
* 🤖 AI-based ETA prediction
* 📱 Improved mobile notifications
* 🚌 Multiple route management
* 🗺️ Historical bus route playback

---

# 👨‍💻 Project Information

### Project Name

**School Bus Live Tracking System**

### Project Type

**School Transportation Management & Live Tracking System**

### Platforms

* 🌐 Web — Admin
* 📱 Android — Driver
* 📱 Android — Ayah
* 📱 Android — Parent

### Technologies

* Dart / Flutter
* JavaScript
* HTML
* CSS
* Backend APIs
* Database
* GPS / Location Services
* Map Services

### Developed By

**Sufyan Najeeb**

---

# 📄 License

This project is developed for **educational purposes**.

If this project is intended for public distribution, an appropriate open-source license can be added.

---

# ⭐ Project Highlights

| Feature                             | Status |
| ----------------------------------- | ------ |
| 🚌 Live School Bus Tracking         | ✅      |
| 📍 Driver GPS-Based Location        | ✅      |
| 👨‍💼 Admin Web Management          | ✅      |
| 👨‍👩‍👧 Parent Android Application | ✅      |
| 👩‍🏫 Ayah Android Application      | ✅      |
| 🚌 Driver Android Application       | ✅      |
| 💬 Parent–Ayah Chat                 | ✅      |
| ⚠️ Complaint Management             | ✅      |
| 📝 Driver Bus Status Updates        | ✅      |
| 👨‍🎓 Student–Bus Allocation        | ✅      |

---

## 🚀 Project Vision

The goal of this project is to create a **safer, smarter, and more connected school transportation system** by combining live GPS tracking, centralized management, mobile applications, and real-time communication.

**🚌 Track. Communicate. Manage. Travel Safely.**
