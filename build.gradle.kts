plugins {
	java
	id("org.springframework.boot") version "4.0.6"
	id("io.spring.dependency-management") version "1.1.7"
	jacoco
	checkstyle
}

group = "com.jpmns"
version = "1.0.0"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

repositories {
	mavenCentral()
}

dependencies {
	// Persistence
	runtimeOnly("org.postgresql:postgresql")
	implementation("org.flywaydb:flyway-database-postgresql")
	implementation("org.springframework.boot:spring-boot-starter-flyway")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")

	// Web
	implementation("org.springframework.boot:spring-boot-starter-web")

	// Security
	implementation("org.springframework.boot:spring-boot-starter-security")

	// Validation
	implementation("org.springframework.boot:spring-boot-starter-validation")

	// Observability
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.springframework.boot:spring-boot-starter-opentelemetry")
	implementation("io.micrometer:micrometer-registry-otlp")

	// JWT
	runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.6")
	runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.6")
	implementation("io.jsonwebtoken:jjwt-api:0.12.6")

	// Swagger
	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.8")

	// Test
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
	testImplementation("org.springframework.boot:spring-boot-starter-data-jpa-test")
	testImplementation("org.springframework.boot:spring-boot-starter-security-test")
	testImplementation("org.springframework.boot:spring-boot-testcontainers")
	testImplementation("org.testcontainers:postgresql:1.20.1")
	testImplementation("com.h2database:h2")
}

tasks.withType<Test> {
	useJUnitPlatform()
	finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
	dependsOn(tasks.test)
	reports {
		xml.required.set(false)
		csv.required.set(false)
		html.required.set(true)
	}
	
	classDirectories.setFrom(
		files(classDirectories.files.map {
			fileTree(it) {
				exclude(
					"com/jpmns/task/configuration/**",
					"com/jpmns/task/core/application/usecase/task/dto/**",
					"com/jpmns/task/core/application/usecase/user/dto/**",
					"com/jpmns/task/core/application/port/security/dto/**",
					"com/jpmns/task/core/application/usecase/task/exception/**",
					"com/jpmns/task/core/application/usecase/user/exception/**",
					"com/jpmns/task/core/application/port/security/exception/**",
					"com/jpmns/task/TaskApplication.*"
				)
			}
		})
	)
}

tasks.jacocoTestCoverageVerification {
	dependsOn(tasks.jacocoTestReport)
	violationRules {
		rule {
			limit {
				minimum = "0.85".toBigDecimal()
			}
		}
	}
}

tasks.check {
	dependsOn(tasks.jacocoTestCoverageVerification)
}

checkstyle {
	toolVersion = "10.21.4"
	configFile = file("checkstyle.xml")
	isIgnoreFailures = false
	maxWarnings = 0
}

tasks.withType<Checkstyle> {
	exclude("**/documentation/**")
}
