# FitWell — Fitness Center Management System

A full-featured desktop application for managing fitness training classes, equipment inventory, training plans, and consultant workflows.

Built with **Java Swing**, **MS Access (UCanAccess)**, and **JasperReports**.

---

## Features

### Roles
| Role | Capabilities |
|------|-------------|
| **Manager** | Approve/reject consultants, full system access |
| **Trainer** | Manage classes, equipment, attendance, reports |
| **Dietitian** | Manage dietary restrictions on personal plans |
| **Trainee** | View classes, register, track plans |

### Core Modules
- **Training Classes** — Create, schedule, manage status (Start / Pause / Complete / Cancel) with DB persistence
- **Training Plans** — Personal & group plans, member management, class assignment
- **Equipment Management** — SwiftFit JSON import, AI-assisted extraction (stub + Claude API skeleton), inspection workflow, flagging
- **Reports** — XML inventory report (export to file / SwiftFit), Low Attendance report (JTable + CSV export), JasperReports PDF
- **Emergency Alerts** — Activate/deactivate alerts, suspend/resume classes
- **Dietary Restrictions** — Structured enum-based selection (Vegan, Gluten Free, Halal, etc.)

---

## Project Structure

```
Druzelegends/
├── src/fitwell/
│   ├── app/                    # Entry point (App.java) & DI container (ApplicationContext)
│   ├── domain/                 # Domain models & enums
│   │   ├── user/               #   Consultant, Trainee, ConsultantRole
│   │   ├── training/           #   TrainingClass, Plan, PlanStatus, ...
│   │   ├── registration/       #   ClassRegistration, AttendanceStatus
│   │   ├── equipment/          #   Equipment, EquipmentInspection, ...
│   │   ├── emergency/          #   EmergencyAlert
│   │   ├── reports/            #   LowAttendanceRecord, ExtractionResult
│   │   └── shared/             #   DietaryRestriction, PreferredUpdateMethod
│   ├── persistence/            # Data access layer
│   │   ├── api/                #   Repository interfaces
│   │   ├── jdbc/               #   JDBC / UCanAccess implementations
│   │   └── db/                 #   Db connection & DbMigration
│   ├── service/                # Business logic
│   │   ├── auth/               #   AuthenticationService
│   │   ├── training/           #   TrainingClassService, TrainingPlanService, ...
│   │   ├── attendance/         #   AttendanceService, LowAttendanceReportService
│   │   ├── equipment/          #   EquipmentReviewService, EquipmentImportService, ...
│   │   └── emergency/          #   EmergencyAlertService
│   ├── controller/             # UI-facing controllers
│   ├── integration/            # External system integrations
│   │   ├── ai/                 #   ImageExtractionService, ClaudeExtractionService
│   │   ├── swiftfit/           #   SwiftFitGateway
│   │   └── notification/       #   NotificationGateway
│   ├── ui/                     # Swing UI
│   │   ├── shell/              #   AppShellFrame, LoginDialog, SignUpDialog, RoleSelectionPanel
│   │   ├── consultant/         #   ConsultantDashboardPanel
│   │   ├── consultant/panels/  #   All consultant sub-panels & dialogs
│   │   ├── trainee/            #   TraineeDashboardPanel
│   │   ├── trainee/panels/     #   All trainee sub-panels
│   │   ├── components/         #   StatCard, SidebarButton, TopBarPanel, ProTextBlocks
│   │   └── theme/              #   FWTheme, FWUi
│   ├── util/                   # JSON parser, XML builder, file utilities
│   └── config/                 # App paths configuration
├── lib/                        # External JARs (JasperReports, UCanAccess, etc.)
├── dist/
│   ├── db/                     # fitwell_2.accdb — MS Access database
│   ├── reports/                # JRXML templates
│   └── RunFitWell.bat          # Windows launcher
├── sample-data/                # Sample JSON & XML files for import/testing
└── README.md
```

---

## Architecture

The project follows a clean layered architecture:

```
app → ui → controller → service → persistence → domain
```

Each layer depends only on the layer below it. Dependency injection is handled manually via `ApplicationContext`, which wires all repositories and services at startup.

---

## Requirements

- **Java 21** (compiled with `--release 21`)
- **Eclipse IDE** or any Java IDE with classpath support
- **Windows** recommended (MS Access via UCanAccess; `.bat` launcher)

---

## Running the Application

### From Eclipse
1. Import as existing Java project
2. Set JRE to Java 21
3. Run `fitwell.app.App` as Java Application

### From command line
```bat
cd dist
RunFitWell.bat
```

### Manual compile
```bash
find src -name "*.java" > /tmp/sources.txt
javac --release 21 -cp "lib/*" -d bin @/tmp/sources.txt
```

---

## AI Equipment Extraction

The `ImageExtractionService` runs as a smart stub by default — it derives equipment name and category from the image URL path segments.

To activate **real Claude API extraction**:
1. Set environment variable: `CLAUDE_API_KEY=<your-anthropic-key>`
2. See `src/fitwell/integration/ai/ClaudeExtractionService.java` for the ready-to-use skeleton

---

## Sample Data

Import SwiftFit equipment updates via `Equipment → Import JSON` using files in `sample-data/`:
- `swiftfit_import_batch_A.json` — sample equipment batch

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| UI | Java Swing (dark theme) |
| Database | MS Access via UCanAccess 5.x |
| Reports | JasperReports 6.21 |
| Build | Manual javac (`--release 21`) |
| AI Integration | Claude API skeleton (Anthropic) |
