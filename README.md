# Malawi Assemblies of God University Student Management Information System - MAGU SMIS
This is a comprehensive mobile application designed to manage the core aspects of a small university's student information system (SIS). Developed natively using Android Studio, the application provides a secure and user-friendly interface for students to manage their personal details, course registrations, and academic records.
## Features
- **Student Authentication**: Secure registration and login functionalities using hashed passwords.
- **Course Management**: Allows students to view available courses and register for new ones.
- **Academic Records**: Functionality for viewing grades and academic history (Grade Management).
- **Persistent Storage**: Data is securely stored and managed on the local device.
## Technology Stack
|Component|Technology|Description|
|---|---|---|
|Platform|Android (Java)|Native mobile application development platform.|
|User Interface|XML Layouts & Android Views|Standard UI components within Activities for navigation and interaction.|
|Database|Room Persistence Library|Abstracts the SQLite database layer for robust, local data management.|
|Data Access|Repository Pattern & DAO|Utilizes best practices for separating business logic from data access logic.|
|Security|SHA-256 Hashing|Used for secure storage and validation of user passwords.|
## Setup and Installation
### Prerequisites
- Android Studio (Latest Version)
- An Android device or emulator running API Level 24+ (Nougat or newer).
### Installation Steps
1. **Open in Android Studio**: Navigate to `File > Open` and select the cloned project directory.
2. **Sync Gradle**: Allow Android Studio to download dependencies and sync the Gradle files.
3. **Run**: Select an emulator or connected device and click the Run button.
