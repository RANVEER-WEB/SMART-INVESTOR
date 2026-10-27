---
description: Startup-grade system instructions for Smart Investor. High-performance modular architecture with 25+ functional modules and professional error/success handling.
---

# Smart Investor - Executive System Instructions

## 1. Professional Modular Architecture
The app MUST manage 25+ Composable modules through a centralized NavHost:
1.  **Intro Hub:** Animated Splash Screen + Logo Intro + Trust Statement.
2.  **The Vault (Entry):** 3D Login portal with MFA & Biometrics (Face/Fingerprint).
3.  **KYC Stepper:** PAN, Aadhaar, Bank & Demat linking with 3D verification status.
4.  **Dashboard:** Primary command center with the **Interactive 3D Stock Graph**.
5.  **Learning Academy:** Video hub with Search, Filters, and "Lesson Progress" tracking.
6.  **Admin Suite:** Advanced control for User/Payment/Ticket management.
7.  **Financial Tools:** Tax Reports (PDF), SIP Calculators, and Risk Assessment surveys.

## 2. Advanced System Logic
- **Theme Strategy:** Support for Dark/Light mode and System-auto. Persistence via DataStore.
- **Offline Protocol:** Use Room Database for caching. Display "Cached Data" indicator and "Retry Sync" when offline.
- **Search Experience:** Implement Live Suggestions, Trending Searches, and Voice-to-Text.
- **Notification Center:** Priority-based grouping with "Read/Unread" tracking and actionable deep links.

## 3. Support & Founder Credentials
- **Lead Developer/Support:** ranveermandal183@gmail.com
- **Contact:** +91 8340320664
- **Accessibility:** Mandatory support for Screen Readers and Touch Target optimization (min 48dp).

## 4. Technical Rules
- **State Management:** Use `Loading`, `Success`, and `Error` states for every API call.
- **Performance:** Complex animations must use `Lottie` or `Canvas` to prevent frame drops.