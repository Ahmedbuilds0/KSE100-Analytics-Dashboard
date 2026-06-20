<div align="center">

# KSE-100 Comparative Analytics Dashboard

**A real-time JavaFX analytics platform for Pakistan Stock Exchange index data**
**with nanosecond-precision algorithm speed comparison**

[![Java](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://www.oracle.com/java/)
[![JavaFX](https://img.shields.io/badge/JavaFX-21-007396?style=for-the-badge&logo=java&logoColor=white)](https://openjfx.io)
[![OOP](https://img.shields.io/badge/OOP-Abstraction%20%7C%20Inheritance%20%7C%20Encapsulation-6366f1?style=for-the-badge)]()
[![KSE-100](https://img.shields.io/badge/Data-KSE--100%20Index%20(PSX)-22c55e?style=for-the-badge)]()

<br/>

![Status](https://img.shields.io/badge/Status-Complete-22c55e?style=flat-square)
![Records](https://img.shields.io/badge/Dataset-863%20Trading%20Days%20(2023–2026)-3b82f6?style=flat-square)
![Algorithms](https://img.shields.io/badge/Algorithms-SMA%20%7C%20EMA%20%7C%20Linear%20Regression-a855f7?style=flat-square)

</div>

---

## What It Does

The KSE-100 Analytics Dashboard loads 3+ years of real Pakistan Stock Exchange index data from a local file, validates and repairs corrupt entries using a try-catch recovery system, and renders an interactive split-chart interface where two smoothing algorithms (SMA and EMA) can be run, compared visually, and benchmarked against each other down to the nanosecond using `System.nanoTime()`.

Every algorithm is a concrete subclass of an abstract `DataProcessor` parent — so adding a new algorithm requires only one new class, zero changes elsewhere.

---

## Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                     JavaFX Frontend                         │
│    LoginView · DashboardView · ChartCrosshair · CSS Theme   │
└──────────────────────────┬──────────────────────────────────┘
                           │
┌──────────────────────────▼──────────────────────────────────┐
│                   Service Layer (OOP)                       │
│                                                             │
│         DataProcessor  ← Abstract Parent Class              │
│         ┌──────────────┬──────────────────┐                 │
│         ▼              ▼                  ▼                 │
│    SMACalculator  EMACalculator  TrendLineCalculator        │
│    (Sliding Avg)  (Exp Smooth)   (Linear Regression)       │
│         │              │                  │                 │
│         └──────────────┴──────────────────┘                 │
│                  System.nanoTime() wraps each loop          │
└──────────────────────────┬──────────────────────────────────┘
                           │
┌──────────────────────────▼──────────────────────────────────┐
│                    Data Layer                               │
│    DataLoader → try-catch validation → DataRecord[]         │
│    [ kse100_project_dataset.txt ]  863 rows  2023–2026      │
└─────────────────────────────────────────────────────────────┘
```

---

## Features

| Category | Feature |
|---|---|
| 📊 Charts | Side-by-side split layout — original data left, processed output right |
| 🔵 SMA | Simple Moving Average with adjustable window size slider (3–15 pts) |
| 🟠 EMA | Exponential Moving Average with adjustable alpha slider (0.1–0.5) |
| ⚡ Run Both | Overlays SMA + EMA simultaneously on the right chart |
| 📈 Trend Line | Linear regression overlay showing overall market direction |
| 🎯 Crosshair | Synchronized hover tooltip across both charts (date + exact value) |
| ⏱ Speed Monitor | Dual `System.nanoTime()` timers — both visible simultaneously |
| 📉 Speed History | Bar chart tracking last 10 explicit algorithm runs |
| 🗓 Date Filter | Per-year filtering — all stats + charts recompute dynamically |
| 📋 Data Table | Popup TableView: Date · Original · SMA · EMA · Trend side by side |
| 📐 Statistics | Min · Max · Mean · Std Dev · Overall % Change (live, per filter) |
| 🔒 Login | Secure login + registration with file-based credential storage |
| 🛡 Validation | try-catch data recovery — corrupt rows replaced with last valid value |
| 🎨 UI Theme | Light professional theme — navy header, white cards, blue/coral accents |

---

## OOP Design

```
DataProcessor  (Abstract Class)
│
│  + process(List<DataRecord>): List<DataRecord>   ← abstract
│  + getExecutionTimeNs(): long                    ← abstract
│
├── SMACalculator
│       Centered sliding window average
│       Window size = 2 × radius + 1  (configurable)
│       Time complexity: O(n × window)
│
├── EMACalculator
│       Exponential smoothing: price × α + prevEMA × (1 - α)
│       Alpha configurable (0.1 – 0.5)
│       Time complexity: O(n)  ← consistently faster than SMA
│
└── TrendLineCalculator
        Ordinary Least Squares linear regression
        Finds best-fit straight line across all n points
        Time complexity: O(n)
```

---

## Algorithm Comparison

| Algorithm | Formula | Complexity | Speed (863 pts) | Best For |
|---|---|---|---|---|
| SMA | Σ(window) / size | O(n × w) | ~550,000 ns | Heavy smoothing |
| EMA | price×α + prev×(1-α) | O(n) | ~520,000 ns | Trend tracking |
| Linear Regression | OLS best-fit line | O(n) | ~80,000 ns | Direction overview |

> EMA is consistently faster than SMA because it has **no inner loop** — exactly 3 arithmetic operations per data point regardless of alpha.

---

## Tech Stack

[![Java 21](https://img.shields.io/badge/Java-21-ED8B00?style=flat-square&logo=openjdk&logoColor=white)](https://www.oracle.com/java/)
[![JavaFX](https://img.shields.io/badge/JavaFX-21-007396?style=flat-square&logo=java&logoColor=white)](https://openjfx.io)
[![CSS](https://img.shields.io/badge/JavaFX_CSS-Light_Theme-2563eb?style=flat-square)]()
[![File I/O](https://img.shields.io/badge/File_I%2FO-BufferedReader%20%2B%20try--catch-6366f1?style=flat-square)]()
[![System.nanoTime](https://img.shields.io/badge/Timing-System.nanoTime()-e8480a?style=flat-square)]()

---

## Project Structure

```
KSE100-Analytics-Dashboard/
├── src/com/kse100/
│   ├── Main.java                     # Entry point — scene switching (Login → Dashboard)
│   ├── model/
│   │   ├── DataRecord.java           # Encapsulated entity: date (String) + price (double)
│   │   └── Dataset.java              # Wrapper: records + metadata (total, repaired, dates)
│   ├── service/
│   │   ├── DataProcessor.java        # Abstract parent — declares process() + getExecutionTimeNs()
│   │   ├── SMACalculator.java        # Subclass: centered sliding window, configurable radius
│   │   ├── EMACalculator.java        # Subclass: exponential smoothing, configurable alpha
│   │   ├── TrendLineCalculator.java  # Subclass: ordinary least squares linear regression
│   │   └── DataLoader.java           # File I/O: parses .txt, try-catch repair, returns Dataset
│   └── ui/
│       ├── LoginView.java            # Login + registration screen with credential file storage
│       ├── DashboardView.java        # Main split dashboard — all charts, controls, stats
│       └── ChartCrosshair.java       # Synchronized hover crosshair overlay (both charts)
├── resources/
│   └── style.css                     # Full light theme — 200+ lines of JavaFX CSS
├── kse100_project_dataset.txt        # 863 real KSE-100 trading days (Jan 2023 – Jun 2026)
├── compile.ps1                       # One-command PowerShell compile script
├── run.ps1                           # One-command PowerShell run script
└── README.md
```

---

## How to Run

### Prerequisites
- **JDK 21** — [Download](https://www.oracle.com/java/technologies/downloads/#java21)
- **JavaFX SDK 21** — [Download](https://gluonhq.com/products/javafx/)

### Step 1 — Clone
```bash
git clone https://github.com/Ahmedbuilds0/KSE100-Analytics-Dashboard.git
cd KSE100-Analytics-Dashboard
```

### Step 2 — Compile
```powershell
mkdir bin

javac --module-path "PATH_TO_JAVAFX\lib" ^
      --add-modules javafx.controls,javafx.fxml ^
      -d bin ^
      src/com/kse100/Main.java ^
      src/com/kse100/model/*.java ^
      src/com/kse100/service/*.java ^
      src/com/kse100/ui/*.java

copy resources\style.css bin\style.css
```

### Step 3 — Run
```powershell
java --module-path "PATH_TO_JAVAFX\lib" ^
     --add-modules javafx.controls,javafx.fxml ^
     -cp bin com.kse100.Main
```

Replace `PATH_TO_JAVAFX` with your JavaFX SDK path, e.g.:
```
C:\Users\user\Downloads\openjfx-21.0.11_windows-x64_bin-sdk\javafx-sdk-21.0.11
```

### Default Login
```
Username: admin
Password: admin123
```
New accounts can be registered from the login screen.

---

<div align="center">

## Author

**Muhammad Ahmed**

[![GitHub](https://img.shields.io/badge/GitHub-Ahmedbuilds0-181717?style=for-the-badge&logo=github&logoColor=white)](https://github.com/Ahmedbuilds0)
[![LinkedIn](https://img.shields.io/badge/LinkedIn-Muhammad%20Ahmed-0A66C2?style=for-the-badge&logo=linkedin&logoColor=white)](https://www.linkedin.com/in/muhammad-ahmed-443592333/)

</div>
