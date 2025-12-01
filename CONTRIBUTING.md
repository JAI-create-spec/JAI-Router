# Contributing to JAI Router

Thank you for your interest in contributing to JAI Router! This document provides guidelines and instructions for contributing.

## Table of Contents

- [Code of Conduct](#code-of-conduct)
- [Getting Started](#getting-started)
- [Development Setup](#development-setup)
- [Making Changes](#making-changes)
- [Testing](#testing)
- [Code Style](#code-style)
- [Commit Guidelines](#commit-guidelines)
- [Pull Request Process](#pull-request-process)

## Code of Conduct

This project adheres to a code of conduct. By participating, you are expected to uphold this code. Please be respectful and constructive in all interactions.

## Getting Started

1. **Fork the repository** on GitHub
2. **Clone your fork** locally:
   ```bash
   git clone https://github.com/YOUR_USERNAME/JAI-Router.git
   cd JAI-Router
   ```
3. **Add upstream remote**:
   ```bash
   git remote add upstream https://github.com/JAI-create-spec/JAI-Router.git
   ```

## Development Setup

### Prerequisites

- **Java 17** or higher
- **Gradle 8.x** (wrapper included)
- **Git**
- **IDE**: IntelliJ IDEA (recommended), Eclipse, or VS Code

### Build the Project

```bash
# Build all modules
./gradlew clean build

# Run tests
./gradlew test

# Run specific module tests
./gradlew :jai-router-core:test
```

### IDE Setup

#### IntelliJ IDEA

1. Open the project as a Gradle project
2. Enable annotation processing: `Settings → Build → Compiler → Annotation Processors`
3. Import code style: `Settings → Editor → Code Style → Import Scheme`

#### Eclipse

1. Import as an existing Gradle project
2. Install Gradle Buildship plugin if not already installed

#### VS Code

1. Install Java Extension Pack
2. Install Gradle for Java extension
3. Open the project folder

## Making Changes

### Branch Naming

Use descriptive branch names following this pattern:
- `feature/description` - New features
- `fix/description` - Bug fixes
- `docs/description` - Documentation updates
- `refactor/description` - Code refactoring
- `test/description` - Test improvements

Example:
```bash
git checkout -b feature/add-redis-cache-support
```

### Keep Your Fork Updated

```bash
git fetch upstream
git checkout develop
git merge upstream/develop
```

## Testing

### Writing Tests

- Write unit tests for all new functionality
- Maintain or improve code coverage
- Use meaningful test names with `@DisplayName`
- Follow AAA pattern: Arrange, Act, Assert

Example:
```java
@Test
@DisplayName("Should route to auth service when input contains login keywords")
void shouldRouteToAuthService() {
    // Arrange
    Router router = new RouterEngine(llmClient);
    
    // Act
    RoutingResult result = router.route("User wants to login");
    
    // Assert
    assertThat(result.service()).isEqualTo("auth-service");
    assertThat(result.confidence()).isGreaterThan(0.7);
}
```

### Running Tests

```bash
# Run all tests
./gradlew test

# Run with coverage
./gradlew test jacocoTestReport

# Run specific test class
./gradlew test --tests "*RouterEngineTest"

# Run in continuous mode
./gradlew test --continuous
```

## Code Style

### Java Style Guide

We follow standard Java conventions with these specifics:

1. **Indentation**: 4 spaces (no tabs)
2. **Line length**: 120 characters maximum
3. **Braces**: Required for all control structures
4. **Naming**:
   - Classes: `PascalCase`
   - Methods/Variables: `camelCase`
   - Constants: `UPPER_SNAKE_CASE`
   - Packages: `lowercase`

### Documentation

- Add JavaDoc to all public classes and methods
- Include `@param`, `@return`, `@throws` where applicable
- Use `@author` and `@since` tags
- Keep comments concise and meaningful

Example:
```java
/**
 * Routes incoming requests to the appropriate service.
 * <p>
 * This method analyzes the input and returns a routing decision
 * with confidence score and explanation.
 * </p>
 *
 * @param input the request to route, must not be null
 * @return routing result containing service and confidence
 * @throws NullPointerException if input is null
 * @since 1.0.0
 */
@NotNull
RoutingResult route(@NotNull String input);
```

### Null Safety

- Use `@NotNull` and `@Nullable` annotations
- Validate inputs with `Objects.requireNonNull()`
- Return `Optional` instead of null where appropriate

### Error Handling

- Create custom exceptions for domain-specific errors
- Provide meaningful error messages
- Include context in exception messages

## Commit Guidelines

### Commit Message Format

```
<type>(<scope>): <subject>

<body>

<footer>
```

**Type**: Must be one of:
- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation changes
- `style`: Code style changes (formatting, etc.)
- `refactor`: Code refactoring
- `test`: Adding or updating tests
- `chore`: Maintenance tasks

**Scope**: The module affected (core, spring-boot, examples)

**Subject**: Short description (50 chars or less)

**Examples**:
```
feat(core): add Redis cache support for routing decisions

fix(spring-boot): resolve circular dependency in auto-configuration

docs(readme): update installation instructions for Java 17

test(core): add comprehensive tests for RouterEngine
```

### Atomic Commits

- Make small, focused commits
- Each commit should represent a single logical change
- Ensure code compiles after each commit

## Pull Request Process

### Before Submitting

1. **Update your branch** with latest changes from `develop`
2. **Run all tests** and ensure they pass
3. **Run build** and fix any warnings
4. **Update documentation** if needed
5. **Add/update tests** for your changes

### Submitting a PR

1. **Push your branch** to your fork
2. **Create a Pull Request** against `develop` branch
3. **Fill out the PR template** completely
4. **Link related issues** using keywords (Fixes #123)

### PR Title Format

```
<type>(<scope>): <description>
```

Example:
```
feat(core): Add support for custom routing strategies
```

### PR Description Template

```markdown
## Description
Brief description of the changes

## Motivation
Why is this change necessary?

## Changes
- List of specific changes
- Another change

## Testing
How was this tested?

## Checklist
- [ ] Tests added/updated
- [ ] Documentation updated
- [ ] Code follows style guidelines
- [ ] All tests passing
- [ ] No new warnings
```

### Review Process

- At least one maintainer approval required
- Address all review comments
- Keep discussions constructive and professional
- Be responsive to feedback

### After Approval

- Maintainers will merge your PR
- Delete your feature branch after merge
- Update your local repository

## Questions?

If you have questions about contributing:

- **Email**: rrezart.prebreza@gmail.com
- **Issues**: https://github.com/JAI-create-spec/JAI-Router/issues
- **Discussions**: https://github.com/JAI-create-spec/JAI-Router/discussions

Thank you for contributing to JAI Router!

