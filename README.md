# KSE-100 Comparative Analytics Dashboard & Speed Analyzer

A JavaFX desktop application that loads 3+ years of real **Pakistan Stock Exchange (KSE-100)** index data, visualizes it across side-by-side interactive charts, and runs two smoothing algorithms (SMA & EMA) with **nanosecond-precision execution timing** for direct performance comparison.

## Features

- Secure Login & Registration system with file-based credential storage
- Side-by-side split chart layout — original data on the left, processed output on the right
- **Simple Moving Average (SMA)** with adjustable window size slider (3–15 points)
- **Exponential Moving Average (EMA)** with adjustable alpha slider (0.1–0.5)
- **Run Both** mode — overlays SMA and EMA simultaneously on the right chart
- **Linear Regression Trend Line** overlay on the original chart
- Synchronized crosshair hover tooltip across both charts (date + exact index value)
- Nanosecond execution timing using `System.nanoTime()` with dual speed monitor display
- Speed History bar chart tracking the last 10 explicit algorithm runs
- Live descriptive statistics: Min, Max, Mean, Std Dev, Overall % Change
- Date range filter (by year) that recomputes all stats and charts dynamically
- Data Table popup showing raw + SMA + EMA + Trend values side by side
- Data Validation Layer with try-catch recovery for corrupt file entries
- Light professional theme: navy header, white cards, blue/coral algorithm accents

---

## Tech Stack

| Technology | Usage |
|---|---|
| Java 21 | Core language |
| JavaFX 21 | UI framework (Charts, TableView, CSS) |
| OOP (Abstract classes, Inheritance) | DataProcessor → SMA / EMA / TrendLine |
| File I/O | Dataset loading + credential storage |
| System.nanoTime() | Algorithm execution timing |

---

## Project Structure

```
KSE100-Analytics-Dashboard/
├── src/
│   └── com/kse100/
│       ├── Main.java                     # App entry point, scene switching
│       ├── model/
│       │   ├── DataRecord.java           # Encapsulated data entity (date + price)
│       │   └── Dataset.java              # Dataset wrapper with metadata
│       ├── service/
│       │   ├── DataProcessor.java        # Abstract parent class
│       │   ├── SMACalculator.java        # SMA subclass (configurable window)
│       │   ├── EMACalculator.java        # EMA subclass (configurable alpha)
│       │   ├── TrendLineCalculator.java  # Linear regression subclass
│       │   └── DataLoader.java           # File I/O + validation layer
│       └── ui/
│           ├── LoginView.java            # Login & registration screen
│           ├── DashboardView.java        # Main split dashboard
│           └── ChartCrosshair.java       # Synchronized hover crosshair
├── resources/
│   └── style.css                         # Full light theme stylesheet
├── kse100_project_dataset.txt            # 863 records of real KSE-100 data (2023–2026)
├── compile.ps1                           # PowerShell compile script
├── run.ps1                               # PowerShell run script
└── README.md
```

---

## Prerequisites

- **JDK 21** — [Download here](https://www.oracle.com/java/technologies/downloads/#java21)
- **JavaFX SDK 21** — [Download here](https://gluonhq.com/products/javafx/)

---

## How to Run

### Step 1 — Clone the repository
```bash
git clone https://github.com/YOUR_USERNAME/KSE100-Analytics-Dashboard.git
cd KSE100-Analytics-Dashboard
```

### Step 2 — Download JavaFX SDK 21
Download from [gluonhq.com/products/javafx](https://gluonhq.com/products/javafx/) and extract it somewhere on your machine. Note the path to the `lib` folder inside it.

### Step 3 — Compile
Open PowerShell or Command Prompt in the project root and run:

```powershell
# Create bin folder if it doesn't exist
mkdir bin

# Compile all Java files
javac --module-path "PATH_TO_JAVAFX\lib" --add-modules javafx.controls,javafx.fxml -d bin src/com/kse100/Main.java src/com/kse100/model/*.java src/com/kse100/service/*.java src/com/kse100/ui/*.java

# Copy CSS to bin
copy resources\style.css bin\style.css
```

Replace `PATH_TO_JAVAFX` with your actual JavaFX SDK path, for example:
```
C:\Users\user\Downloads\openjfx-21.0.11_windows-x64_bin-sdk\javafx-sdk-21.0.11
```

### Step 4 — Run
```powershell
java --module-path "PATH_TO_JAVAFX\lib" --add-modules javafx.controls,javafx.fxml -cp bin com.kse100.Main
```

### Default Login Credentials
```
Username: admin
Password: admin123
```
> You can also register a new account from the login screen.

---

## OOP Design

```
DataProcessor  (Abstract Class)
├── SMACalculator       — Centered sliding window average (configurable radius)
├── EMACalculator       — Exponential smoothing (configurable alpha)
└── TrendLineCalculator — Ordinary least squares linear regression
```

All subclasses override `process()` and `getExecutionTimeNs()`, which are declared abstract in the parent — making timing a guaranteed part of the contract for every algorithm.

---

## Algorithm Overview

| Algorithm | Formula | Time Complexity | Best For |
|---|---|---|---|
| SMA | Average of surrounding window | O(n × window) | Heavy smoothing, noise removal |
| EMA | `price × α + prevEMA × (1-α)` | O(n) | Tracking trends with less lag |
| Linear Regression | Ordinary Least Squares | O(n) | Overall market direction |

---

*Author*

[![LinkedIn](https://img.shields.io/badge/LinkedIn-Connect-blue)](https://linkedin.com/in/YOUR_LINKEDIN)
[![GitHub](https://img.shields.io/badge/GitHub-Follow-black)](https://github.com/YOUR_USERNAME)
