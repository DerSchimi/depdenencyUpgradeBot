# Dependency Upgrade Bot

A modular bot to upgrade dependencies in different project types.

## Supported Project Types

- **Gradle**: Updates dependencies in `build.gradle` files
- **Maven**: Updates dependencies in `pom.xml` files

## Features

- Auto-detects project types in the current directory
- Updates dependencies to newer minor versions while maintaining the same major version
- Supports both single-project and multi-module projects
- Uses Maven Central API to find the latest versions
- Creates `.updated` files with the changes (preserves original files)

## Usage

### Auto-detect and update all project types:
```bash
gradle run
```

### Update specific project type:
```bash
# For Gradle projects only
gradle run --args="gradle"

# For Maven projects only
gradle run --args="maven"
```

### Backwards compatibility:
The original `GradleMinorUpdater` class is still available for backwards compatibility:
```bash
gradle run -DmainClass="de.schimi.gradleupdater.GradleMinorUpdater"
```

## Architecture

The application is now modularized with the following structure:

- `DependencyUpdateBot`: Main entry point with auto-detection
- `DependencyUpdateService`: Interface for different project types
- `VersionService`: Handles version checking via Maven Central API
- `GradleUpdateService`: Handles Gradle build.gradle files
- `MavenUpdateService`: Handles Maven pom.xml files

## Output

The bot creates `.updated` files containing the modified build configurations with updated dependency versions. Review these files before applying the changes to ensure they meet your requirements.