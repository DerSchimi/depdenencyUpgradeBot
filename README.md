# depdenencyUpgradeBot
A modular dependency upgrade bot that supports multiple build systems (Gradle and Maven)

## Features

- **Multi-Build System Support**: Automatically detects and updates dependencies in both Gradle (`build.gradle`) and Maven (`pom.xml`) projects
- **Modular Architecture**: Clean separation of concerns with pluggable build file processors
- **Minor Version Updates**: Safely updates dependencies to newer minor versions within the same major version
- **Maven Central Integration**: Uses Maven Central repository to find latest versions
- **Logging**: Comprehensive logging of all update operations

## Supported Build Systems

- **Gradle**: Processes `build.gradle` files and updates dependencies in the format `'group:artifact:version'`
- **Maven**: Processes `pom.xml` files and updates dependencies in `<dependency>` sections

## Usage

Run the dependency upgrade bot:

```bash
gradle run
```

The bot will:
1. Scan the current directory and subdirectories for supported build files
2. Parse dependency declarations
3. Check Maven Central for newer minor versions
4. Update dependencies and save modified files with `.updated` extension

## Architecture

The codebase is organized into modular packages:

- `core/`: Core interfaces and shared implementations
- `gradle/`: Gradle-specific build file processing
- `maven/`: Maven-specific build file processing  
- `version/`: Version checking logic using Maven Central API
- `gradleupdater/`: Legacy classes (maintained for compatibility)

## Output

Updated files are saved with a `.updated` extension:
- `build.gradle.updated` for Gradle projects
- `pom.xml.updated` for Maven projects
