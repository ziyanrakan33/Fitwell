# FitWell — Architecture Reference

מסמך reference קצר של המבנה החדש. יש לקרוא אותו לפני כל שלב ב-`REFACTORING_PLAN.md`.

---

## עקרון מרכזי: Layered Architecture

ארכיטקטורה שכבתית עם כיוון תלות **אחד בלבד** — מלמעלה למטה.
אף שכבה לא תלויה בשכבה שמעליה.

```
┌─────────────────────────────────────────┐
│  app/        ← נקודת כניסה + DI wiring  │
├─────────────────────────────────────────┤
│  ui/         ← Swing panels (View)      │
├─────────────────────────────────────────┤
│  controller/ ← מגשרים דקים UI↔Service   │
├─────────────────────────────────────────┤
│  service/    ← לוגיקה עסקית             │
├─────────────────────────────────────────┤
│  persistence/← JDBC + interfaces        │
├─────────────────────────────────────────┤
│  domain/     ← entities + enums טהורים  │
└─────────────────────────────────────────┘

integration/ ← גישה חיצונית (AI, SwiftFit, Notifications)
common/      ← exceptions, utils (ניטרלי)
```

### חוקי תלות
- `domain/` לא תלוי באף אחד. POJOs טהורים.
- `persistence/` תלוי רק ב-`domain/`.
- `service/` תלוי ב-`persistence/` (דרך interfaces) וב-`domain/`.
- `controller/` תלוי ב-`service/` וב-`domain/`.
- `ui/` תלוי ב-`controller/`, `service/`, `domain/`.
- `app/` תלוי בכולם — זה המקום היחיד שיודע לחבר את הכל.
- `integration/` תלוי ב-`domain/` בלבד, ו-services קוראים לו.
- `common/` לא תלוי באף אחד; כולם יכולים להשתמש בו.

---

## מבנה החבילות המלא

```
com.fitwell/
│
├── app/
│   ├── App.java                       # main()
│   └── ApplicationContext.java        # Dependency Injection ידני
│
├── domain/
│   ├── user/           Trainee, Consultant, ConsultantRole
│   ├── training/       TrainingClass, Plan(+Personal/Group), PlanMember,
│   │                   PlanClassAssignment, status enums
│   ├── registration/   ClassRegistration, AttendanceStatus
│   ├── equipment/      Equipment, EquipmentLocation, Assignment,
│   │                   Inspection, EquipmentUpdate/Batch, enums
│   ├── emergency/      EmergencyAlert
│   ├── reports/        LowAttendanceRecord, ExtractionResult
│   └── shared/         DietaryRestriction, PreferredUpdateMethod
│
├── persistence/
│   ├── api/            13 interfaces (TraineeRepository, ...)
│   ├── jdbc/           13 מימושים (JdbcTraineeRepository, ...)
│   └── db/             Db, DbMigration
│
├── service/
│   ├── auth/           AuthenticationService
│   ├── training/       TrainingClassService, QueryService, PlanService
│   ├── attendance/     AttendanceService, LowAttendanceReportService
│   ├── equipment/      ReviewService, ImportService, InspectionWorkflowService
│   └── emergency/      EmergencyAlertService
│
├── controller/
│   ├── EquipmentManagementController
│   ├── EquipmentAssignmentController
│   ├── InventoryReportController
│   └── JasperReportController
│
├── integration/
│   ├── ai/             ClaudeExtractionService, ImageExtractionService
│   ├── swiftfit/       SwiftFitGateway
│   └── notification/   NotificationGateway
│
├── ui/
│   ├── shell/          AppShellFrame, Login/SignUp Dialogs, RoleSelection
│   ├── consultant/
│   │   ├── ConsultantDashboardPanel
│   │   └── panels/     11 פאנלים
│   ├── trainee/
│   │   ├── TraineeDashboardPanel
│   │   └── panels/     4 פאנלים
│   ├── components/     StatCard, SidebarButton, TopBarPanel, ProTextBlocks
│   └── theme/          FWTheme, FWUi
│
└── common/
    └── exception/      FitWellException, NotFoundException, ValidationException
```

---

## החלטות ארכיטקטוניות מרכזיות

### 1. אין Service Locator, יש Dependency Injection ידני
- `FitWellServiceRegistry` (Singleton + Service Locator) מבוטל.
- במקומו: `ApplicationContext` יחיד שנבנה ב-`main()` ומחבר את כל התלויות דרך קונסטרקטורים.
- **למה**: תלויות מפורשות, בדיקות קלות יותר, אין "קסם" גלובלי.

### 2. Repository = interface + JDBC implementation
- כל repository הוא interface ב-`persistence/api/`.
- המימוש ב-`persistence/jdbc/`.
- **למה**: ניתן להחליף MS Access ב-DB אחר, ניתן ל-mock בבדיקות, מפריד חוזה ממימוש.

### 3. Service vs Controller — הפרדה אמיתית
- **Service** = לוגיקה עסקית, transaction boundaries, ולידציות.
- **Controller** = מגשר דק בין UI ל-Service. מכין נתונים לתצוגה, מקבל קלט מהמשתמש.
- **למה**: היום `control/` מכיל את שניהם מעורבבים. הפרדה מקלה על תחזוקה.

### 4. Domain ללא enums בחבילה נפרדת
- Enums יושבים ליד ה-entity הרלוונטי (לדוגמה `AttendanceStatus` ב-`domain/registration/`).
- **למה**: 10 enums בחבילה נפרדת מאלצים navigation מיותר. Cohesion לפי תחום עסקי.

### 5. UI מחולק לפי role, לא לפי טכנולוגיה
- `ui/consultant/` ו-`ui/trainee/` נפרדים.
- **למה**: כשמוסיפים feature ל-consultant, כל הקבצים הרלוונטיים באותו מקום.

### 6. Exceptions ב-`common/`
- Hierarchy של exceptions מותאמים לדומיין.
- **למה**: הודעות שגיאה אחידות, טיפול מרוכז ב-UI.

---

## Design Patterns שנשמרים

| Pattern | איפה |
|---------|------|
| Repository / DAO | `persistence/api/` + `persistence/jdbc/` |
| Dependency Injection (ידני) | `app/ApplicationContext.java` |
| MVC | UI=View, Service=Model, Controller=Controller |
| Abstract Base Class | `domain/training/Plan.java` → Personal/Group |
| CardLayout State Machine | `ui/shell/AppShellFrame.java` |
| Gateway | `integration/*` (SwiftFit, Claude, Notification) |

## Design Patterns שמוסרים

| Pattern | למה |
|---------|-----|
| Service Locator (`FitWellServiceRegistry`) | מסתיר תלויות, אנטי-דפוס |
| Singleton ב-`AuthenticationService` | לא נחוץ — `ApplicationContext` מחזיק instance אחד |

---

## Stack טכנולוגי (ללא שינוי)

- Java + Swing (GUI)
- MS Access (UcanAccess JDBC)
- Jasper Reports (PDF)
