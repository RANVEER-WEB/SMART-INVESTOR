# Smart Investor — Project Audit & Baseline Report

**Date:** 2026-10-27  
**Platform:** Native Android (Kotlin, Jetpack Compose, Material 3, Hilt, Room, DataStore, Ktor, OkHttp)  
**Lead Developer:** Ranveer Mandal (ranveermandal183@gmail.com, +91 8340320664)  
**Branch:** `feat/fintech-upgrade` (Baseline Checkpoint Commit: `4e5307c`)

---

## 1. Executive Summary

Smart Investor is designed as a futuristic fintech investment platform blending Angel One and Groww ergonomics with an AI-native "Luxury Glass" design system. The initial foundation includes a modern Gradle build setup with Kotlin 2.0.21, Jetpack Compose (2024.12 BOM), Hilt 2.52, Room 2.6.1 offline caching, DataStore preferences, Supabase 3.0.2 authentication and database clients, Finnhub market data integration, and a direct REST Gemini AI client.

However, a thorough audit reveals that while the core architectural skeleton compiles cleanly (`compileDebugKotlin` passed in 33s), many core user journeys are currently static or UI placeholders:
- Watchlist and Portfolio are disconnected from Room DB and user holdings (using hardcoded in-memory state).
- Stock details and company fundamentals are routed to a generic `PlaceholderScreen`.
- Indian market indices (NIFTY 50 / SENSEX) and multi-timeframe charts with touch scrubbing are missing.
- KYC is purely client-side with no input validation or persistence to Supabase.
- No unit tests or automated test suites existed in `app/src/test`.
- Paper trading / order execution engine is absent.

---

## 2. Platform & Technology Stack Inventory

| Component | Technology | Version | Status |
| :--- | :--- | :--- | :--- |
| **Language & Runtime** | Kotlin | 2.0.21 | Verified Working |
| **Build System** | Gradle & AGP | 8.9 / 8.5.2 | Verified Working (Java 21 JBR) |
| **UI Framework** | Jetpack Compose + M3 | BOM 2024.12.01 | Verified Working |
| **Dependency Injection** | Dagger Hilt | 2.52 + KSP | Verified Working |
| **Local Database** | Room Database | 2.6.1 + KSP | 4 Entities (stocks, watchlist, notifications, search) |
| **Preferences** | Jetpack DataStore | 1.1.1 | Configured in DI, pending UI link |
| **Authentication & DB** | Supabase Kotlin SDK | 3.0.2 (Auth + Postgrest) | Configured |
| **AI Integration** | Google Gemini (REST via OkHttp) | `gemini-2.0-flash` | Functional with error handling |
| **Market Data** | Finnhub REST API (Ktor) | Ktor 3.0.1 | Verified |
| **Charts** | Compose Canvas + Vico | Canvas custom + Vico 2.0.0-beta.3 | Canvas active; needs touch/timeframes |
| **Animations** | Compose Animation + Lottie | Lottie 6.6.2 | Configured |

---

## 3. Comprehensive Feature & Module Classification

| Module / Feature | Route | Status | Notes & Current Deficiencies |
| :--- | :--- | :--- | :--- |
| **Animated Splash Screen** | `splash` | **WORKING** | Animated diamond logo (scale 0.8→1.0), glow bloom, trust statement. |
| **Auth: The Vault (Login)** | `login` | **PARTIAL** | Real Supabase sign-in, loading state, error card. Lacks password reset and real biometric prompt hookup. |
| **Auth: Register** | `register` | **PARTIAL** | Supabase sign-up. Needs client-side password strength and email regex validation. |
| **MFA Verification** | `mfa_verify` | **MISSING** | Routed to `PlaceholderScreen`. |
| **Biometric Setup** | `biometric_setup` | **MISSING** | Biometric prompt helper exists in AndroidX but no settings/setup screen. |
| **KYC Stepper** | `kyc` | **PARTIAL** | 4-step wizard (PAN, Aadhaar, Bank, Demat). Missing format validation regex, bank IFSC lookup, and Supabase sync. |
| **Dashboard** | `dashboard` | **PARTIAL** | Real Finnhub stock movers and market news. Canvas chart has no interactive scrubbing/timeframes. Hardcoded portfolio balance. |
| **Stock Details & Fundamentals**| `stock_detail/{symbol}` | **MISSING** | Currently routed to `PlaceholderScreen`. Needs company profile, key metrics, technical chart, and buy/sell actions. |
| **Watchlist** | `watchlist` | **PARTIAL** | Has beautiful Luxury Glass UI, but uses local static state list instead of observing Room DB / user watchlist! |
| **Portfolio & Holdings** | `portfolio` | **UI-ONLY** | Hardcoded list of holdings. No live P&L calculation from real stock quotes or paper trading ledger. |
| **AI Advisor (Copilot)** | `ai_advisor` | **WORKING** | Real Gemini 2.0 Flash REST connection with multi-turn chat and animated thinking orb. Needs structured investment insights & risk profile context. |
| **SIP & Goal Calculator** | `financial_tools` | **PARTIAL** | SIP formula works accurately. Lacks goal-based target calculations, PDF export, and compounding options. |
| **Risk Assessment** | `risk_assessment` | **MISSING** | Placeholder. Crucial for financial suitability. |
| **Learning Academy** | `learning` | **UI-ONLY** | 6 lesson cards with hardcoded progress bars. Clicking does not open lesson details or quizzes. |
| **Lesson Detail & Quiz** | `lesson_detail`, `quiz` | **MISSING** | Routed to `PlaceholderScreen`. |
| **Notifications Center** | `notifications` | **PARTIAL** | Displays priority badges (Purple AI, Green Profit, Blue News). Needs link to Room `NotificationDao`. |
| **Settings & Theme** | `settings` | **PARTIAL** | Theme selector toggles local state; must persist to DataStore so app theme stays chosen across restarts. |
| **Admin Suite** | `admin` | **UI-ONLY** | Mock statistics and action cards. No role checks or backend user list. |
| **Paper Trading / Order Engine**| `orders` | **MISSING** | No simulated trading balance or buy/sell execution flow. |
| **Offline Protocol** | Room Cache | **WORKING** | `StockRepository` caches stocks in Room and displays offline banner when network drops. |
| **Automated Tests** | Unit & Instrumentation | **MISSING** | 0 tests existed in `app/src/test`. |

---

## 4. Architectural & Security Risk Assessment

1. **State Persistence**: Theme choice in `MainActivity` was not bound to DataStore Flow, resetting to DARK on app restart.
2. **Data Consistency**: Watchlist and Portfolio UI operated on static in-memory lists rather than Room reactive queries.
3. **Chart Usability**: Dashboard chart did not support touch point inspection, value scrub tooltips, or 1D/1W/1M/1Y range switches.
4. **Input Sanitization**: KYC forms allowed arbitrary text without regex validation (e.g. invalid PAN/Aadhaar accepted).
5. **Secrets Handling**: API keys are read from `local.properties` via `BuildConfig` and not committed into git.
6. **Regulatory Framing**: All investment outputs and calculations must explicitly convey disclaimers (non-advisory educational platform under Indian SEBI guidelines).

---

## 5. Prioritized Implementation Roadmap

- **Phase A — Design System & Theme Engine**:
  - Connect DataStore to app-wide ThemeMode (Light, Dark, System).
  - Standardize Luxury Glass 2.0 components: interactive chart Canvas with touch scrubber, AI pulse orb, and elevation strokes.
- **Phase B — Stock Details, Discovery & Interactive Charts**:
  - Build full-featured `StockDetailScreen` with Finnhub company profile, quote, key ratios, and interactive multi-range candle/line chart.
  - Implement dynamic Search with search history and instant suggestions.
- **Phase C — Reactive Watchlist & Real Database Binding**:
  - Connect `WatchlistScreen` to `StockRepository` and Room `WatchlistDao`.
  - Enable swipe-to-remove and add-to-watchlist from search and stock details.
- **Phase D — Paper Trading & Portfolio Engine**:
  - Add virtual portfolio ledger with starting balance (e.g. ₹10,00,000 virtual cash).
  - Implement Buy/Sell order dialog with market/limit pricing, position tracking, and real-time P&L against live Finnhub quotes.
- **Phase E — AI Advisor Enhancement**:
  - Add structured prompt engineering with risk profile conditioning, bullish/bearish scenario breakdowns, and quick question chips.
- **Phase F — Wealth Planning & Risk Assessment**:
  - Implement comprehensive Goal & SIP Calculator and an interactive Risk Assessment questionnaire that updates user's risk profile.
- **Phase G — Learning Academy Interactive Lessons**:
  - Build interactive lesson reader and knowledge quiz modules with progress saving in Room.
- **Phase H — Automated Testing & Verification**:
  - Author robust unit tests for financial math (SIP, Portfolio P&L), validation rules (PAN, Aadhaar), and state machines.
