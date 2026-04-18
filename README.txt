# FitWell Studio 🏋️‍♂️

FitWell Studio is a comprehensive desktop application designed to manage fitness studio operations, schedules, trainees, and equipment inventory.

## 🚀 Features
- **Class Management**: Schedule, modify, and monitor training classes. Includes a robust Emergency Mode with suspended states and auto-resumption constraints.
- **Trainee & Plan Management**: Handle individual trainees along with Personal and Group training plans.
- **Equipment & Inventory**: Track equipment statuses and handle hardware collisions across concurrently overlapping classes.
- **SwiftFit Integration**: Parses automated monthly equipment JSON updates, extracting features dynamically via Image URL Machine Learning logic (with Low-confidence flagging).
- **Annual Reporting**: Generate XML inventory reports and PDF visualizations from the historical class database.

## 🛠️ Technology Stack
- **Language**: Java
- **UI**: Java Swing 
- **Database**: Access Database / JDBC (UCanAccess)
- **Reports**: JasperReports

## 📂 Project Structure
- `src/`: Java source code.
- `lib/`: Required external library references (.jar files).
- `dist/`: Ready-to-use compiled distribution (Runnable output).
- `META-INF/`: Application manifest and configuration.

## ⚙️ How to Run
1. Clone the repository to your local machine.
2. Ensure the JAR dependencies in the `lib/` directory are added to your build path.
3. To run directly, execute the file from the `dist/` directory or run the main `App` class from your IDE (e.g., Eclipse).

## 👨‍💻 Authors 
- Developed as a final software engineering project presentation.
