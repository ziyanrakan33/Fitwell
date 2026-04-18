# FitWell — Refactoring Plan

מסמך זה מגדיר את תהליך ה-refactoring המלא למבנה חדש מבוסס ארכיטקטורה שכבתית.
הוא מיועד להרצה עם Claude Code, שלב אחרי שלב.

---

## מטרה

מעבר מהמבנה הנוכחי (`entity/`, `repo/`, `control/`, `integration/`, `ui/pro/`, `db/`) לארכיטקטורה שכבתית נקייה:

```
app → ui → controller → service → persistence → domain
```

כל שכבה תלויה רק בשכבה שמתחתיה. אין תלות הפוכה.

---

## הכנות לפני תחילת העבודה

1. **וודא שהפרויקט מתקמפל ועובד** במצבו הנוכחי.
2. **צור git branch חדש**: `git checkout -b refactor/layered-architecture`
3. **וודא שיש בדיקות ידניות מוכנות** — אחרי כל שלב נוודא שהאפליקציה עולה, login עובד, dashboard עולה.
4. **קרא את `ARCHITECTURE.md`** כדי להבין לאן אנחנו מכוונים.

---

## חוקים חוצי-שלבים (חשוב!)

- **כל שלב = קומיט נפרד.** אחרי כל שלב מוצלח: `git add -A && git commit -m "refactor: step N - <description>"`.
- **אחרי כל שלב — וודא קומפילציה מלאה.** אם לא מתקמפל, אל תמשיך לשלב הבא.
- **אל תשנה לוגיקה עסקית.** זה refactor מבני בלבד — תזוזות, שינויי imports, הפיכה של classes ל-interfaces.
- **שמור על שמות מחלקות ומתודות קיימים**, אלא אם כן צוין אחרת.
- **עדכן את כל ה-imports בכל הפרויקט** אחרי כל תזוזה.
- **אל תיצור DTOs** בשלב זה.
- **אל תמחק קוד** אלא אם כן נאמר במפורש (למשל `FitWellServiceRegistry` בשלב 4).

---

## שלב 1: חלוקת `entity/` ל-`domain/` עם sub-packages

### מטרה
לפצל את 30 המחלקות השטוחות לחבילות לפי תחום עסקי.

### פעולות

צור את החבילה `com.fitwell.domain` עם תתי-חבילות, והעבר אליהן את המחלקות הבאות:

**`domain/user/`**
- `Trainee`
- `Consultant`
- `ConsultantRole` (enum)

**`domain/training/`**
- `TrainingClass`
- `TrainingClassStatus` (enum)
- `Plan` (abstract)
- `PersonalPlan`
- `GroupPlan`
- `PlanStatus` (enum)
- `TrainingPlanMember`
- `PlanClassAssignment`

**`domain/registration/`**
- `ClassRegistration`
- `AttendanceStatus` (enum)

**`domain/equipment/`**
- `Equipment`
- `EquipmentCategory` (enum)
- `EquipmentStatus` (enum)
- `EquipmentLocation`
- `EquipmentAssignment`
- `ClassEquipmentAssignment`
- `EquipmentInspection`
- `InspectionSeverity` (enum)
- `EquipmentUpdate`
- `EquipmentBatch`

**`domain/emergency/`**
- `EmergencyAlert`

**`domain/reports/`**
- `LowAttendanceRecord`
- `ExtractionResult`

**`domain/shared/`**
- `DietaryRestriction` (enum)
- `PreferredUpdateMethod` (enum)

### וידוא
- מחק את חבילת `entity/` הישנה (אחרי שכל המחלקות הועברו).
- עדכן את כל ה-imports בכל השכבות האחרות.
- הפרויקט מתקמפל.
- הרץ את האפליקציה — עולה ועובדת.

### קומיט
`refactor: step 1 - reorganize entity/ into domain/ subpackages`

---

## שלב 2: איחוד `repo/` + `db/` ל-`persistence/`

### מטרה
להפוך את שכבת ה-persistence לאחידה, ולהפריד בין חוזה (interface) למימוש (JDBC).

### פעולות

**2.1** צור את חבילת `com.fitwell.persistence` עם תתי-חבילות: `api`, `jdbc`, `db`.

**2.2** העבר את `Db.java` ו-`DbMigration.java` מ-`db/` ל-`persistence/db/`. מחק את חבילת `db/` הישנה.

**2.3** עבור כל אחד מ-13 ה-repositories הקיימים (`TrainingClassRepository`, `TraineeRepository`, `ConsultantRepository`, `EquipmentRepository`, `EquipmentInspectionRepository`, `EquipmentAssignmentRepository`, `ClassEquipmentAssignmentRepository`, `TrainingPlanRepository`, `PlanMemberRepository`, `PlanClassRepository`, `RegistrationRepository`, `AttendanceRepository`, `EmergencyAlertRepository`):

  **א.** צור interface ב-`persistence/api/` עם אותו שם (לדוגמה `TraineeRepository`). ה-interface מכיל רק את חתימות המתודות הציבוריות.

  **ב.** צור מחלקת מימוש ב-`persistence/jdbc/` בשם `Jdbc<Name>` (לדוגמה `JdbcTraineeRepository implements TraineeRepository`). העבר את כל הקוד הקיים לתוך המימוש.

  **ג.** כל מקום בפרויקט שהשתמש במחלקה הישנה — יעדכן את ה-import ל-interface מ-`persistence/api/`.

### וידוא
- `persistence/api/` מכיל רק interfaces.
- `persistence/jdbc/` מכיל את המימושים.
- חבילת `repo/` הישנה נמחקה.
- הפרויקט מתקמפל.
- האפליקציה עולה ועובדת.

### קומיט
`refactor: step 2 - unify repo/ and db/ into persistence/ with interfaces`

---

## שלב 3: פיצול `control/` ל-`service/` ו-`controller/`

### מטרה
הפרדה ברורה בין Services (לוגיקה עסקית) ל-Controllers (מגשרים ל-UI).

### פעולות

**3.1** צור את חבילת `com.fitwell.service` עם תתי-חבילות: `auth`, `training`, `attendance`, `equipment`, `emergency`.

**3.2** העבר את ה-Services לתתי-החבילות המתאימות:

- `service/auth/` → `AuthenticationService`
- `service/training/` → `TrainingClassService`, `TrainingClassQueryService`, `TrainingPlanService`
- `service/attendance/` → `AttendanceService`, `LowAttendanceReportService`
- `service/equipment/` → `EquipmentReviewService`, `EquipmentImportService`, `InspectionWorkflowService`
- `service/emergency/` → `EmergencyAlertService`

**3.3** צור את חבילת `com.fitwell.controller` והעבר אליה:

- `EquipmentManagementController`
- `EquipmentAssignmentController`
- `InventoryReportController`
- `JasperReportController`

**3.4** `FitWellServiceRegistry` — **אל תיגע בו בשלב זה.** הוא יטופל בשלב 4. אם הוא בחבילת `control/`, השאר אותו שם זמנית.

### וידוא
- חבילת `control/` ריקה (או מכילה רק את `FitWellServiceRegistry` באופן זמני).
- הפרויקט מתקמפל.
- האפליקציה עולה ועובדת.

### קומיט
`refactor: step 3 - split control/ into service/ and controller/`

---

## שלב 4: החלפת `FitWellServiceRegistry` ב-`ApplicationContext`

### מטרה
להחליף את ה-Service Locator (אנטי-דפוס) ב-Dependency Injection ידני — קוד נקי יותר, ניתן לבדיקה.

### פעולות

**4.1** צור את חבילת `com.fitwell.app` עם שתי מחלקות:
- `App.java` (העבר את המחלקה הקיימת עם `main()`)
- `ApplicationContext.java` (חדש)

**4.2** `ApplicationContext` יבנה בקונסטרקטור שלו את כל ה-repositories וה-services, בסדר הנכון של תלויות:

```java
public class ApplicationContext {
    // repositories
    private final TraineeRepository traineeRepository;
    private final ConsultantRepository consultantRepository;
    // ... שאר ה-repositories

    // services
    private final AuthenticationService authenticationService;
    private final TrainingClassService trainingClassService;
    // ... שאר ה-services

    public ApplicationContext() {
        // persistence
        this.traineeRepository = new JdbcTraineeRepository();
        this.consultantRepository = new JdbcConsultantRepository();
        // ...

        // services — מקבלים את ה-repositories/services שהם תלויים בהם
        this.authenticationService = new AuthenticationService(consultantRepository, traineeRepository);
        this.trainingClassService = new TrainingClassService(trainingClassRepository, ...);
        // ...
    }

    // getters לכל service/repository
    public AuthenticationService authenticationService() { return authenticationService; }
    // ...
}
```

**4.3** הסר את דפוס ה-Singleton מ-`AuthenticationService` (מחק את `getInstance()` ואת ה-static field). הוא יווצר פעם אחת ב-`ApplicationContext`.

**4.4** עדכן את `App.main()`:
```java
public static void main(String[] args) {
    // db init + migration
    DbMigration.migrate();

    // bootstrap
    ApplicationContext context = new ApplicationContext();

    // launch UI
    SwingUtilities.invokeLater(() -> new AppShellFrame(context).setVisible(true));
}
```

**4.5** כל מחלקה שקראה ל-`FitWellServiceRegistry.getInstance().getX()` — תשתנה כך שתקבל את השירות הרלוונטי דרך ה-constructor. העבר את `ApplicationContext` (או את השירותים הספציפיים) ל-UI panels דרך קונסטרקטורים.

**4.6** מחק את `FitWellServiceRegistry.java`.

**4.7** אם `control/` ריקה עכשיו — מחק אותה.

### וידוא
- אין יותר שימוש ב-Singleton ב-services (חוץ מ-`ApplicationContext` עצמו שנוצר פעם אחת ב-`main`).
- `FitWellServiceRegistry` נמחק לגמרי.
- הפרויקט מתקמפל.
- האפליקציה עולה, login עובד, dashboard עולה, ניתן לנווט בין הפאנלים.

### קומיט
`refactor: step 4 - replace FitWellServiceRegistry with ApplicationContext (DI)`

---

## שלב 5: חלוקת `integration/` ל-sub-packages

### מטרה
ארגון של ה-integrations החיצוניים לפי ספק/סוג.

### פעולות

צור תתי-חבילות בתוך `integration/` והעבר את הקבצים:

- `integration/ai/` → `ClaudeExtractionService`, `ImageExtractionService`
- `integration/swiftfit/` → `SwiftFitGateway`
- `integration/notification/` → `NotificationGateway`

### וידוא
- הפרויקט מתקמפל.
- האפליקציה עולה ועובדת.

### קומיט
`refactor: step 5 - organize integration/ by external system`

---

## שלב 6: חלוקת `ui/pro/` ל-`ui/` עם sub-packages

### מטרה
הפרדת ה-UI לפי role ולפי תפקיד טכני (shell, components, theme).

### פעולות

**6.1** צור את תתי-החבילות:
- `ui/shell/`
- `ui/consultant/`
- `ui/consultant/panels/`
- `ui/trainee/`
- `ui/trainee/panels/`
- `ui/components/`
- `ui/theme/`

**6.2** העבר לפי הסיווג הבא:

**`ui/shell/`:**
- `AppShellFrame`
- `LoginDialog`
- `SignUpDialog`
- `RoleSelectionPanel`

**`ui/consultant/`:**
- `ConsultantDashboardPanel`

**`ui/consultant/panels/`:**
- 11 הפאנלים של consultant (שיעורים, מתאמנים, תוכניות, ציוד, דוחות, חירום וכו')

**`ui/trainee/`:**
- `TraineeDashboardPanel`

**`ui/trainee/panels/`:**
- 4 הפאנלים של trainee (רישום לשיעורים, תוכניות, פרופיל)

**`ui/components/`:**
- `StatCard`, `SidebarButton`, `TopBarPanel`, `ProTextBlocks`

**`ui/theme/`:**
- `FWTheme`, `FWUi`

**6.3** מחק את `ui/pro/` הישנה.

### וידוא
- הפרויקט מתקמפל.
- האפליקציה עולה, login עובד לכל role, dashboards עולים, כל הפאנלים ניתנים לניווט.

### קומיט
`refactor: step 6 - restructure ui/pro/ by role and concern`

---

## שלב 7 (אופציונלי): הוספת `common/exception/`

### מטרה
Exception hierarchy מרכזי לשימוש עקבי ברחבי הפרויקט.

### פעולות

**7.1** צור חבילה `com.fitwell.common.exception` עם:

- `FitWellException` — abstract, extends RuntimeException.
- `NotFoundException` extends FitWellException — לזריקה מ-repositories כשישות לא נמצאה.
- `ValidationException` extends FitWellException — לזריקה מ-services כשולידציה נכשלת.

**7.2** אתר מקומות בקוד שבהם:
- Repository מחזיר `null` / `Optional.empty()` ואז ה-service זורק שגיאה גנרית → החלף בזריקת `NotFoundException` מה-repository.
- Service בודק קלט וזורק `IllegalArgumentException` → החלף ב-`ValidationException`.

**7.3** אל תחליף את כל ה-exceptions בפרויקט — רק את הברורים. שמרני.

### וידוא
- הפרויקט מתקמפל.
- האפליקציה עובדת — כולל מסלולי שגיאה (login שגוי, רישום כפול וכו').

### קומיט
`refactor: step 7 - introduce common/exception hierarchy`

---

## סיום

אחרי שלב 6 (או 7):
1. `git checkout main`
2. `git merge refactor/layered-architecture`
3. הרץ את האפליקציה end-to-end — login לשני ה-roles, יצירת שיעור, רישום מתאמן, בדיקת ציוד, דוח.
4. אם משהו שבור — חזור ל-branch, תקן, merge מחדש.

---

## Troubleshooting

- **קומפילציה נשברה אחרי תזוזה** → כמעט תמיד imports. הרץ organize imports על כל הפרויקט.
- **NullPointerException ב-runtime אחרי שלב 4** → סביר שפאנל/service עדיין מנסה לקרוא ל-`FitWellServiceRegistry` או מצפה ל-Singleton. חפש שימושים ב-`getInstance()`.
- **הפאנל לא נטען** → בדוק שה-constructor של הפאנל מקבל את ה-`ApplicationContext` / service מ-`AppShellFrame`.
