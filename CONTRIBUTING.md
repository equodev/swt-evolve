# Contributing to SWT Evolve

We welcome contributions to SWT Evolve! This is a modern implementation of the Standard Widget Toolkit (SWT). Please follow these guidelines to ensure a smooth and effective contribution process.

## Prerequisites

Before contributing, make sure you have the following installed:

### Required Software
- **Java JDK 17+** - for building and running Java components
- **Flutter SDK** - [Install Flutter](https://docs.flutter.dev/get-started/install) (Select OS and Desktop)
- **Dart SDK** - comes with Flutter
- **Git** - for version control
- **Gradle** - for building
- **Eclipse IDE or IntelliJ IDEA**

### Platform-Specific Requirements

#### Windows
- Install Visual Studio Build Tools:
  ```bash
  winget install Microsoft.VisualStudio.2022.BuildTools --silent --override "--wait --quiet --add ProductLang En-us --add Microsoft.VisualStudio.Workload.VCTools --includeRecommended"
  ```

## Development Environment Setup

1. **Clone the repository**:
   ```bash
   git clone git@github.com:equodev/swt-evolve.git
   cd swt-evolve
   ```

2. **Import project in your favorite IDE**:

3. **Install Flutter dependencies**:
   ```bash
   cd flutter-lib
   flutter pub get
   ```

4. **Generate Dart JSON serialization code inside the flutter-lib project**:
   ```bash
   dart run build_runner build
   ```

5. **Build native SWT implementation**:
   ```bash
   ./gradlew :swt_native:assemble
   ```

## Project Architecture

The project consists of a few interconnected modules:

- **flutter-lib/**: Flutter application implementing SWT widgets and communication layer
- **swt_native/**: Platform-specific SWT implementations (Java) with native bindings
- **examples/**: Examples and snippets for testing and demonstration

## How to Contribute

1. **Fork the repository** and create a new branch for your feature or bug fix:
   ```bash
   git checkout -b feature/your-feature-name
   ```

2. **Make your changes** following the project conventions and architecture patterns

3. **Add tests** in `swt_native`

4. **Run code quality checks**:
   ```bash
   # Format Dart code
   cd flutter-lib
   flutter analyze
   ```

5. **Submit a pull request** with:
    - Clear description of your changes
    - Screenshots or examples if UI-related
    - Test results demonstrating compatibility
    - Any relevant documentation updates

## Development Workflows

### Building for Different Platforms

```bash
# Flutter builds
cd flutter-lib
flutter build macos             # macOS native
# or
flutter build windows           # macOS native
# or
flutter build linux             # Linux native (coming soon)

# Build all platform JARs
./gradlew buildAllPlatforms

# Or build platform-specific JARs, like:
./gradlew <platform>Jar
# where <platform> could be windows-x86_64, windows-aarch64, macos-x86_64, or macos-aarch64. Support for Linux is coming soon!
```

### Key Guidelines

- Always look at existing components to understand patterns before implementing new ones
- Use the existing communication layer between Java and Flutter
- Add examples in the `examples` folder when contributing new widgets
- Add tests in `swt_native`

## Key Directories

- `flutter-lib/lib/src/swt/`: SWT API classes (.dart and .g.dart files)
- `flutter-lib/lib/src/impl/`: Flutter widget implementations
- `flutter-lib/lib/src/comm/`: Communication layer between Java and Flutter
- `swt_native/src/main/java/`: Core Java SWT implementation
- `swt_native/src/{os}/java/`: Platform-specific native implementations

## Issues and Support

If you find a bug or have a feature request:

1. Check existing issues first
2. Open a new issue on GitHub with:
    - Clear description of the problem
    - Steps to reproduce
    - Expected vs actual behavior
    - Relevant logs, screenshots, or code snippets
    - Platform and version information

## Testing Requirements

Before submitting a PR, ensure:

- [ ] All existing tests pass
- [ ] New functionality is covered by tests
- [ ] Code follows established patterns
- [ ] Examples are provided for new widgets
- [ ] Documentation is updated if needed
- [ ] Code is properly formatted

Thank you for contributing to SWT Evolve!