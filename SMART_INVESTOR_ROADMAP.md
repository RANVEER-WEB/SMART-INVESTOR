# Smart Investor — Product Roadmap & Competitor Benchmarking

## 1. Competitor Benchmark Matrix: Angel One vs Groww vs Smart Investor

| Feature Area | Angel One (Enterprise Retail) | Groww (Simplicity & Modern UX) | Smart Investor (Target: Luxury Glass AI) |
| :--- | :--- | :--- | :--- |
| **Visual Identity** | Dense data, high technical depth, traditional dark/light. | Clean minimalist cards, high whitespace, pastel accents. | **Luxury Glass 2.0**: Frosted glass surfaces, glow bloom, metallic accents, responsive Canvas charts. |
| **AI Experience** | Rule-based alerts and algorithmic screeners. | Basic curated investment baskets, news digests. | **Native AI Investing Copilot**: Gemini 2.0 Flash multi-turn reasoning, grounded company analysis, risk-adapted explanations. |
| **Market Discovery** | Multi-exchange (NSE/BSE/MCX), Option chains, F&O heatmaps. | Simplified search, top gainers/losers, thematic collections. | **Smart Movers & Instant Search**: Real-time quotes, sector tags, live suggestions, search history. |
| **Charts** | TradingView and ChartIQ with 100+ indicators. | Native line and candlestick charts with basic ranges. | **Interactive Canvas Hero Chart**: Touch scrub tooltips, multi-timeframe toggles, 3D gradient fills. |
| **Paper Trading** | Virtual trading primarily via 3rd party APIs. | Not built-in for retail. | **Built-in Paper Trading Simulator**: ₹10,00,000 virtual balance, simulated market/limit orders, live P&L. |
| **Wealth Planning** | Advanced tax reports, portfolio rebalancing tools. | Basic SIP and lumpsum calculators. | **Interactive Wealth Planner**: Goal calculator, SIP projections with visual growth breakdown, risk suitability test. |
| **Learning Academy**| Smart Money (blog/article format). | Groww Digest & Academy (educational guides). | **Interactive Academy**: Guided chapters, visual progress tracking, and interactive end-of-lesson quizzes. |

---

## 2. Execution Phases & Milestones

1. **Milestone 1: Design System & Theme Engine (Phase 2)**
   - Persistent theme store via Jetpack DataStore (`ThemeMode.LIGHT`, `ThemeMode.DARK`, `ThemeMode.SYSTEM`).
   - Unified theme provider in `MainActivity`.
   - Luxury Glass 2.0 interactive chart Canvas with gesture detection & value scrubbing.

2. **Milestone 2: Stock Discovery, Search & Stock Detail Screen (Phase 4 & 5)**
   - Replace placeholder `stock_detail/{symbol}` with comprehensive `StockDetailScreen`.
   - Implement real Finnhub Company Profile, financial statistics (52W range, Market Cap, P/E, High/Low).
   - Live Search dialog/screen with Room search history.

3. **Milestone 3: Reactive Watchlist with Room Database (Phase 5 & 7)**
   - Connect `WatchlistScreen` to `StockRepository.getWatchlist()` Flow.
   - Add/remove stock functionality with optimistic UI updates.

4. **Milestone 4: Paper Trading & Portfolio Engine (Phase 8)**
   - Room-backed paper trading holdings and transaction history.
   - Simulated buy/sell execution dialog with validation (sufficient cash, quantity > 0).
   - Live Portfolio screen displaying real-time calculation of invested vs current value and profit/loss.

5. **Milestone 5: AI Advisor Copilot Supercharging (Phase 6)**
   - Grounded financial prompts with structured outputs (sentiment, key drivers, bull/bear scenarios, risk warnings).
   - Quick-action suggestion chips ("Analyze AAPL", "Explain P/E Ratio", "Conservative Portfolio Tips").
   - Transparent SEBI-aligned educational disclaimers.

6. **Milestone 6: KYC Validation & Financial Planning (Phase 7 & 9)**
   - Indian regulatory format validation (PAN regex, Aadhaar 12-digit, IFSC).
   - Full Goal & SIP investment calculator with inflation/growth curves.
   - Interactive 4-question Risk Assessment survey with profile classification.

7. **Milestone 7: Learning Academy & Quiz Module (Phase 9)**
   - Interactive lesson view with readable content and completion check.
   - Knowledge check quiz with score calculation.

8. **Milestone 8: Automated Tests & Verification (Phase 12 & 13)**
   - Unit tests covering financial calculation formulas, KYC validators, and ViewModel state transitions.
   - Verification report.
