# Aapanam - Inventory and Sales Management System

Aapanam is a modern, cross-platform inventory and sales management system built with Kotlin Multiplatform. It aims to provide a seamless experience for managing products, tracking sales, and handling credit accounts across various platforms.

## ✨ Features

*   **Inventory Management:**
    *   Add, edit, and delete products.
    *   Track product stock quantity.
    *   Categorize products for better organization.
*   **Sales Tracking:**
    *   Record sales transactions.
    *   Manage paid and credit sales.
*   **Credit Sales Grouping:**
    *   Automatically group credit sales by customer.
    *   Aggregate total sales amount, total paid amount, and remaining credit amount per customer.
*   **Customer Management (Implicit):**
    *   Link sales to customer IDs for comprehensive tracking.
*   **Modern UI:**
    *   Intuitive and responsive user interface powered by Compose Multiplatform.
*   **Reporting (Planned/Future):**
    *   View various reports related to sales, stock, and credit.

## 🚀 Technologies Used

*   **Kotlin Multiplatform:** For sharing business logic across different platforms.
*   **Compose Multiplatform:** For building native-like UIs on Android and other platforms.
*   **SQLDelight:** A multiplatform SQL database library for type-safe database interactions.
*   **Kotlin Coroutines & Flow:** For asynchronous programming and reactive data streams.
*   **MVVM Architecture:** Clean and testable architecture for maintainable code.

## 📁 Project Structure

The project is structured into two main modules:

*   `:composeApp`: This module contains the platform-specific UI implementations (e.g., Android application).
*   `:shared`: This is the core multiplatform module containing shared business logic, data models, database interactions, and view models.

```
Aapanam/
├── composeApp/                 # Platform-specific UI (Android)
│   └── src/
│       └── commonMain/kotlin/org/example/aapanam/ui/ # Compose Multiplatform UI
├── shared/                     # Shared Kotlin Multiplatform module
│   └── src/
│       └── commonMain/kotlin/org/example/aapanam/ # Shared logic, data, presentation
│           ├── data/           # Database, API, models
│           ├── presentation/   # ViewModels, state management
│           └── util/           # Utility functions
├── build.gradle.kts            # Root project Gradle configuration
└── README.md                   # Project documentation (this file)
```

## 🛠️ Getting Started

To get a local copy up and running, follow these steps.

### Prerequisites

*   Android Studio (Bumblebee or newer recommended)
*   JDK 11 or higher

### Installation

1.  **Clone the repository:**

    ```bash
    git clone https://github.com/your-username/Aapanam.git
    cd Aapanam
    ```

2.  **Open in Android Studio:**
    Open the `Aapanam` project in Android Studio. The IDE will automatically set up the Gradle project.

3.  **Run on Android:**
    *   Select the `composeApp` run configuration.
    *   Choose an Android emulator or a physical device.
    *   Click the 'Run' button (green triangle) in Android Studio.

## 🤝 Contributing

Contributions are what make the open-source community such an amazing place to learn, inspire, and create. Any contributions you make are **greatly appreciated**.

1.  Fork the Project
2.  Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3.  Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4.  Push to the Branch (`git push origin feature/AmazingFeature`)
5.  Open a Pull Request

## 📄 License

Distributed under the MIT License. See `LICENSE` for more information.

## 📞 Contact

Your Name/Organization - [your-email@example.com](mailto:your-email@example.com)
Project Link: [https://github.com/your-username/Aapanam](https://github.com/your-username/Aapanam)
