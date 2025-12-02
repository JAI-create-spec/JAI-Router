# Publishing JAI Router to Maven Central

This guide explains how to publish JAI Router to Maven Central Repository so others can use it as a Maven/Gradle dependency.

## Table of Contents

1. [Prerequisites](#prerequisites)
2. [Step 1: Prepare for Publishing](#step-1-prepare-for-publishing)
3. [Step 2: Create Sonatype Account](#step-2-create-sonatype-account)
4. [Step 3: Generate GPG Keys](#step-3-generate-gpg-keys)
5. [Step 4: Configure Gradle](#step-4-configure-gradle)
6. [Step 5: Publish Release](#step-5-publish-release)
7. [Step 6: Verify Publication](#step-6-verify-publication)
8. [Alternative: GitHub Packages](#alternative-github-packages)
9. [Troubleshooting](#troubleshooting)

---

## Prerequisites

- Java 17+
- Gradle 8.x
- GPG command-line tool (`brew install gnupg` on macOS)
- Sonatype JIRA account (for Maven Central)
- GitHub account (for GitHub Packages alternative)

---

## Step 1: Prepare for Publishing

### 1.1 Ensure Project has Proper Metadata

The `build.gradle` should include POM information:

```groovy
// build.gradle (root)

allprojects {
    group = 'io.jai.router'
    version = '0.5.0'  // Remove -SNAPSHOT for releases
    
    ext {
        // ... dependencies
    }
}
```

### 1.2 Add Maven Publishing Plugin

Update your root `build.gradle`:

```groovy
plugins {
    id 'maven-publish'
    id 'signing'
}

publishing {
    publications {
        mavenJava(MavenPublication) {
            from components.java
            
            pom {
                name = 'JAI Router'
                description = 'Intelligent AI-powered microservice routing engine'
                url = 'https://github.com/JAI-create-spec/JAI-Router'
                
                licenses {
                    license {
                        name = 'MIT License'
                        url = 'https://opensource.org/licenses/MIT'
                    }
                }
                
                developers {
                    developer {
                        id = 'rrezart'
                        name = 'Rrezart Prebreza'
                        email = 'rrezart.prebreza@gmail.com'
                    }
                }
                
                scm {
                    connection = 'scm:git:https://github.com/JAI-create-spec/JAI-Router.git'
                    developerConnection = 'scm:git:https://github.com/JAI-create-spec/JAI-Router.git'
                    url = 'https://github.com/JAI-create-spec/JAI-Router'
                }
            }
        }
    }
    
    repositories {
        maven {
            name = 'sonatype'
            url = uri('https://oss.sonatype.org/service/local/staging/deploy/maven2/')
            credentials {
                username = project.findProperty('ossrhUsername') ?: System.getenv('OSSRH_USERNAME')
                password = project.findProperty('ossrhPassword') ?: System.getenv('OSSRH_PASSWORD')
            }
        }
    }
}

signing {
    sign publishing.publications.mavenJava
}
```

---

## Step 2: Create Sonatype Account

### 2.1 Register at Sonatype

1. Go to [Sonatype JIRA](https://issues.sonatype.org/secure/Signup!default.jspa)
2. Create an account
3. Create a new issue:
   - **Project**: Community Developers (OSSRH)
   - **Issue Type**: New Project
   - **Summary**: `io.jai.router - Intelligent microservice routing`
   - **Description**: Describe JAI Router
   - **Group ID**: `io.jai.router`
   - **Project URL**: `https://github.com/JAI-create-spec/JAI-Router`
   - **SCM URL**: `https://github.com/JAI-create-spec/JAI-Router.git`

### 2.2 Wait for Approval

Sonatype will verify your account and approve within 1-2 business days. You'll get an email when approved.

---

## Step 3: Generate GPG Keys

GPG keys are required to sign artifacts for Maven Central.

### 3.1 Generate Key Pair

```bash
gpg --full-generate-key
```

Follow the prompts:
- Key type: RSA
- Key size: 4096
- Validity: 0 (never expires)
- Name: Your name
- Email: Your email
- Comment: JAI Router Publisher
- Passphrase: Create a strong passphrase

### 3.2 List Your Keys

```bash
gpg --list-secret-keys --keyid-format=long
```

Output example:
```
/Users/rrezart/.gnupg/pubring.gpg
-----------------------------------
sec   rsa4096/1234567890ABCDEF 2025-12-02 [SC]
      uid                 [ultimate] Your Name <your.email@example.com>
```

**Save the key ID**: `1234567890ABCDEF`

### 3.3 Export Public Key

```bash
gpg --armor --export 1234567890ABCDEF > public.key
```

### 3.4 Upload to Key Servers

```bash
gpg --keyserver keyserver.ubuntu.com --send-keys 1234567890ABCDEF
```

---

## Step 4: Configure Gradle

### 4.1 Create `~/.gradle/gradle.properties`

Add your credentials:

```properties
# Sonatype OSSRH credentials
ossrhUsername=your_sonatype_username
ossrhPassword=your_sonatype_password

# GPG signing credentials
signing.keyId=1234567890ABCDEF
signing.password=your_gpg_passphrase
signing.secretKeyRingFile=/Users/rrezart/.gnupg/secring.gpg
```

**⚠️ IMPORTANT**: Never commit `gradle.properties` to Git. Add to `.gitignore`.

### 4.2 Alternative: Use Environment Variables

```bash
export OSSRH_USERNAME="your_username"
export OSSRH_PASSWORD="your_password"
export GPG_KEY_ID="1234567890ABCDEF"
export GPG_PASSPHRASE="your_passphrase"
```

---

## Step 5: Publish Release

### 5.1 Update Version (Remove -SNAPSHOT)

```groovy
// build.gradle
version = '0.5.0'  // Was 0.5.0-SNAPSHOT
```

### 5.2 Build and Publish

```bash
# Build all modules
./gradlew clean build

# Publish to Sonatype (staging)
./gradlew publish
```

### 5.3 Release from Staging

After successful publishing, release from staging repository:

```bash
# Close the staging repository
./gradlew closeRepository

# Release the repository
./gradlew releaseRepository
```

Or do it manually in [Sonatype Nexus UI](https://oss.sonatype.org/):
1. Login to Nexus
2. Go to "Staging Repositories"
3. Find your repository
4. Click "Close" then "Release"

---

## Step 6: Verify Publication

### 6.1 Check Maven Central

After release (can take 15-30 minutes to sync):

```bash
# Search Maven Central
curl -s "https://search.maven.org/solrsearch/select?q=g:io.jai.router&core=gav" | jq .
```

Or visit: https://search.maven.org/search?q=io.jai.router

### 6.2 Download and Test

```bash
# Create test project
mkdir test-jai-router
cd test-jai-router

# Create build.gradle
cat > build.gradle << 'EOF'
dependencies {
    implementation 'io.jai:jai-router-spring-boot-starter:0.5.0'
}
EOF

# Download dependency
./gradlew dependencies
```

---

## Alternative: GitHub Packages

If you want to publish to GitHub Packages instead of Maven Central:

### A.1 Create Personal Access Token

1. Go to [GitHub Settings → Developer settings → Personal access tokens](https://github.com/settings/tokens)
2. Click "Generate new token"
3. Select scopes: `write:packages`, `read:packages`
4. Copy the token

### A.2 Configure GitHub Packages

Update `build.gradle`:

```groovy
publishing {
    repositories {
        maven {
            name = 'github'
            url = uri('https://maven.pkg.github.com/JAI-create-spec/JAI-Router')
            credentials {
                username = System.getenv('GITHUB_ACTOR') ?: project.findProperty('gpr.user')
                password = System.getenv('GITHUB_TOKEN') ?: project.findProperty('gpr.key')
            }
        }
    }
}
```

### A.3 Publish

```bash
export GITHUB_ACTOR=your_username
export GITHUB_TOKEN=your_token

./gradlew publish
```

### A.4 Use in Another Project

```xml
<!-- pom.xml -->
<repository>
    <id>github</id>
    <name>GitHub Packages</name>
    <url>https://maven.pkg.github.com/JAI-create-spec/JAI-Router</url>
</repository>

<dependency>
    <groupId>io.jai</groupId>
    <artifactId>jai-router-spring-boot-starter</artifactId>
    <version>0.5.0</version>
</dependency>
```

---

## Continuous Integration (GitHub Actions)

### Automated Publishing on Release

Add to `.github/workflows/publish.yml`:

```yaml
name: Publish to Maven Central

on:
  release:
    types: [created]

jobs:
  publish:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      
      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          distribution: 'temurin'
          java-version: '17'
      
      - name: Publish to Maven Central
        env:
          OSSRH_USERNAME: ${{ secrets.OSSRH_USERNAME }}
          OSSRH_PASSWORD: ${{ secrets.OSSRH_PASSWORD }}
          GPG_KEY_ID: ${{ secrets.GPG_KEY_ID }}
          GPG_PASSPHRASE: ${{ secrets.GPG_PASSPHRASE }}
        run: ./gradlew publish
```

---

## Troubleshooting

### Issue: "Invalid signature"

**Solution**: Make sure GPG keys are properly configured and GPG agent is running.

```bash
# Restart GPG agent
gpgconf --kill gpg-agent
```

### Issue: "Could not find artifact in repository"

**Solution**: Artifact may still be syncing. Maven Central syncs every few hours. Check Sonatype Nexus UI.

### Issue: "401 Unauthorized"

**Solution**: Verify Sonatype credentials are correct in `~/.gradle/gradle.properties`.

### Issue: "Group ID not authorized"

**Solution**: Make sure your Sonatype JIRA issue was approved for the group ID `io.jai.router`.

---

## Summary

**Publishing Checklist:**

- ✅ Sonatype account created and approved
- ✅ GPG keys generated and uploaded
- ✅ `build.gradle` configured with POM metadata
- ✅ Version updated (remove -SNAPSHOT)
- ✅ Credentials set in `~/.gradle/gradle.properties`
- ✅ Build and publish: `./gradlew publish`
- ✅ Release from staging repository
- ✅ Verify on Maven Central (15-30 mins)
- ✅ Announce release to users

---

## Next Steps

1. **Version 0.5.0**: First release to Maven Central
2. **Version 0.6.0**: Add features and publish update
3. **Maintain**: Keep dependencies updated, fix bugs, release patches

**Congratulations! Your library is now available for everyone to use!** 🎉


