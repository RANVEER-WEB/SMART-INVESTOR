# Smart Investor — System Architecture & Data Flow

## 1. High-Level Architecture

Smart Investor follows modern Android Clean Architecture principles with MVVM and unidirectional data flow (UDF):

```
┌────────────────────────────────────────────────────────┐
│                   Presentation Layer                   │
│   Composable Screens & Luxury Glass Design System      │
│   (Dashboard, StockDetail, Watchlist, Portfolio, AI)  │
└──────────────────────────▲─────────────────────────────┘
                           │ (StateFlow / UiState)
┌──────────────────────────┴─────────────────────────────┐
│                     ViewModel Layer                    │
│   (DashboardViewModel, StockDetailViewModel, etc.)     │
└──────────────────────────▲─────────────────────────────┘
                           │ (Coroutines / Flows)
┌──────────────────────────┴─────────────────────────────┐
│                    Repository Layer                    │
│   (StockRepository, PortfolioRepository, AuthRepo)     │
└─────────────────▲────────────────────▲─────────────────┘
                  │                    │
┌─────────────────▼─────────┐   ┌──────▼─────────────────┐
│     Local Data Layer      │   │   Remote Data Layer    │
│  - Room Database (Cache)  │   │  - Finnhub API (Ktor)  │
│  - DataStore Preferences  │   │  - Gemini REST (OkHttp)│
│                           │   │  - Supabase (Auth/Post)│
└───────────────────────────┘   └────────────────────────┘
```

## 2. Data Synchronization & Offline Protocol
1. **Network-First with Local Cache Fallback**:
   - Quotes and company profiles are fetched from Finnhub API.
   - Successful responses are immediately cached in Room (`cached_stocks`).
   - If network request fails or times out, cached stock entities are returned seamlessly.
   - The UI displays a "Showing cached data" alert banner with a "Retry Sync" action.
2. **Local Persistence**:
   - Watchlist items stored in Room `watchlist` table.
   - Paper trading transactions and portfolio holdings stored in Room `portfolio_holdings` and `order_history`.
   - Notifications and user search history stored in Room.
   - User theme preferences persisted in Jetpack DataStore.

## 3. Security & Regulatory Compliance
- API keys are injected via Gradle `BuildConfig` from local properties.
- Biometric authentication guards the local session.
- AI Advisor explicitly identifies itself as an educational tool and recommends consulting SEBI-registered professionals for actual financial advice.
- Financial numbers formatting conforms to Indian numbering system (Lakhs & Crores / ₹) and standard 2-decimal accuracy.
