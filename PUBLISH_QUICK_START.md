# Quick Start: Publish JAI Router

This is a quick reference for publishing JAI Router. For detailed steps, see [PUBLISHING.md](PUBLISHING.md).

## TL;DR (30 minutes)

### 1. Create Sonatype Account
- Register at https://issues.sonatype.org/
- Create OSSRH issue with group ID: `io.jai.router`
- Wait for approval (1-2 business days)

### 2. Generate GPG Keys
```bash
gpg --full-generate-key
# Key type: RSA, size: 4096, validity: 0

gpg --list-secret-keys --keyid-format=long
# Save the KEY_ID

gpg --keyserver keyserver.ubuntu.com --send-keys YOUR_KEY_ID
```

### 3. Configure Gradle
Create `~/.gradle/gradle.properties`:
```properties
ossrhUsername=your_username
ossrhPassword=your_password
signing.keyId=YOUR_KEY_ID
signing.password=your_gpg_passphrase
signing.secretKeyRingFile=/path/to/.gnupg/secring.gpg
```

### 4. Update Version (Remove SNAPSHOT)
```groovy
// build.gradle
version = '0.5.0'  // Remove -SNAPSHOT
```

### 5. Publish
```bash
./gradlew clean build
./gradlew publish
```

### 6. Release from Staging
Go to https://oss.sonatype.org/ and:
1. Click "Staging Repositories"
2. Find your repository
3. Click "Close" then "Release"

### 7. Verify (15-30 mins)
```bash
# Check Maven Central
curl -s "https://search.maven.org/solrsearch/select?q=g:io.jai.router" | jq .
```

Now users can use:
```xml
<dependency>
    <groupId>io.jai</groupId>
    <artifactId>jai-router-spring-boot-starter</artifactId>
    <version>0.5.0</version>
</dependency>
```

---

## For Each Release

1. Update version in `build.gradle` (remove -SNAPSHOT)
2. Create GitHub release/tag
3. Run `./gradlew publish`
4. Release from Sonatype Nexus
5. Wait 15-30 mins for Maven Central sync
6. Update version back to SNAPSHOT for next development

---

## Publish to GitHub Packages (Alternative)

If you want to publish to GitHub Packages instead:

```bash
export GITHUB_ACTOR=your_username
export GITHUB_TOKEN=your_github_token
./gradlew publish
```

Users add this to their `pom.xml`:
```xml
<repository>
    <id>github</id>
    <url>https://maven.pkg.github.com/JAI-create-spec/JAI-Router</url>
</repository>
```

---

## Troubleshooting

| Problem | Solution |
|---------|----------|
| "Invalid signature" | Restart GPG: `gpgconf --kill gpg-agent` |
| "401 Unauthorized" | Check credentials in `~/.gradle/gradle.properties` |
| "Artifact not found" | Wait 15-30 mins for Maven Central sync |
| "Group ID not authorized" | Verify your Sonatype JIRA issue was approved |

See [PUBLISHING.md](PUBLISHING.md) for detailed steps and troubleshooting.


