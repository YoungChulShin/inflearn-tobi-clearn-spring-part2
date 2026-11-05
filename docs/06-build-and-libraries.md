# 빌드, 라이브러리와 실행 설정

> 패키지·업무 타입은 샘플 이름이다. 표기 규칙은 [README](README.md)를 따른다. 원본 구현을 설명하는 부분도 이 샘플 이름으로 표기한다.

## 원본에 선언된 버전

아래는 [build.gradle.kts](08-source-map-and-gaps.md), [Gradle wrapper](08-source-map-and-gaps.md), [.sdkmanrc](08-source-map-and-gaps.md)의 값이다. 최신성·지원 기간·호환성 전반을 외부 조사한 목록은 아니다.

| 항목 | 버전/설정 | 역할 |
| --- | --- | --- |
| Java toolchain | 21 | 컴파일·실행 기준 |
| SDKMAN Java | `21.0.8-librca` | 로컬 JDK 선택 |
| Gradle wrapper | 8.14.5 | 재현 가능한 빌드 실행 |
| Spring Boot plugin | 3.5.13 | 애플리케이션·의존성 기준 |
| Spring dependency-management plugin | 1.1.7 | BOM 의존성 관리 |
| SpotBugs Gradle plugin | 6.4.8 | 정적 분석 |
| JUnit Pioneer | 2.3.0 | `@StdIo`, `StdOut` 출력 테스트 |
| Mockito core 및 agent | 5.23.0 | mock·static mocking, JVM agent |
| ArchUnit JUnit5 | 1.4.1 | 구조 규칙 테스트 |
| Instancio core | 6.1.0 | 테스트 객체 생성 |

## 의존성 범위와 사용처

| Gradle 범위 | 의존성 | 사용 |
| --- | --- | --- |
| implementation | spring-boot-starter-data-jpa | Spring Data Repository, Hibernate, 트랜잭션 |
| implementation | spring-boot-starter-web | Spring MVC, JSON, ProblemDetail |
| implementation | spring-boot-starter-validation | Jakarta Validation 실행 |
| implementation | spring-security-core | 인증 관련 기반 의존성; 코드에서는 crypto BCrypt 사용 |
| compileOnly + annotationProcessor | lombok | Getter, 생성자, ToString 생성 |
| developmentOnly | spring-boot-docker-compose | 개발 MySQL compose 연동 |
| runtimeOnly | h2 | embedded DB 실행·테스트 경로 |
| runtimeOnly | mysql-connector-j | MySQL 접속 |
| testImplementation | spring-boot-starter-test | JUnit Jupiter, AssertJ, Spring Test, Mockito 등 |
| testRuntimeOnly | junit-platform-launcher | JUnit Platform 실행 |
| testImplementation | junit-pioneer, mockito-core, archunit-junit5, instancio-core | 테스트 기능 보강 |
| testCompileOnly + testAnnotationProcessor | lombok | 테스트 클래스 생성자 생성 |
| 별도 mockitoAgent Configuration | mockito-core, transitive=false | Test JVM의 javaagent |

버전을 지정하지 않은 의존성은 이 빌드의 Boot 의존성 관리와 전이 의존성 해결 결과를 따른다. 정확한 Hibernate·JUnit·AssertJ·Lombok 버전이 필요하면 `dependencyInsight`로 확인한다. 원본에 선언되지 않은 버전을 이 문서에서 추정하지 않는다.

실제 BCrypt 타입은 `org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder`다. security-core만 선언했다고 보안 필터 체인, 로그인 API, JWT, 인가가 자동 구현된 것은 아니다. 원본에는 security starter와 SecurityFilterChain 설정이 없다.

## 복사할 빌드 설정

기본은 원본 build.gradle.kts를 복사하고 group, version, 프로젝트명을 새 프로젝트에 맞춘다. 테스트 설정은 함께 가져간다.

```kotlin
val mockitoAgent: Configuration = configurations.create("mockitoAgent")

dependencies {
    testImplementation("org.mockito:mockito-core:5.23.0")
    mockitoAgent("org.mockito:mockito-core:5.23.0") { isTransitive = false }
}

tasks.withType<Test> {
    useJUnitPlatform()
    jvmArgs("-javaagent:${mockitoAgent.asPath}")
}
```

Mockito 라이브러리와 agent 버전을 맞춘다. 원본 `AppApplicationTest`는 SpringApplication 정적 호출을 mocking한다. 테스트를 복사할 때 실제 애플리케이션을 기동하는 통합 smoke test와 이 static 위임 테스트의 차이를 구분한다.

SpotBugs에는 [exclude filter](08-source-map-and-gaps.md)가 연결돼 있고 `EQ_CHECK_FOR_OPERAND_NOT_COMPATIBLE_WITH_THIS` 패턴을 제외한다. 새 프로젝트에서 제외가 필요한지 검토한다. 원본에는 별도 formatter, Checkstyle, JaCoCo 설정이 없다. SpotBugs task 설정이 있다는 사실은 현재 분석이 통과했다는 뜻이 아니다.

## 실행·테스트 리소스

| 파일 | 현재 내용 | 이식 시 처리 |
| --- | --- | --- |
| `settings.gradle.kts` | `rootProject.name = "sample-app"` | 프로젝트명 변경 |
| `application.yml` | 앱 이름, show-sql, ddl-auto=update, compose start_only | 환경별 DB·DDL 정책 결정 |
| `compose.yaml` | mysql:latest, 3306 포트, 개발 계정 | DB 이름·포트·개발 자격정보 변경, 이미지 버전 고정 여부 결정 |
| `META-INF/orm.xml` | 엔티티·값 객체 매핑 | 모든 class 경로 치환, 새 도메인 매핑 추가 |
| main의 `instancio.properties` | bean.validation.enabled=true | 새 프로젝트에서는 test resources로 이동 |
| test의 `junit-platform.properties` | 생성자 autowire=all | 테스트 생성자 주입 사용 시 유지 |
| `.sdkmanrc` | Java 21.0.8-librca | 팀 JDK 배포판·버전과 맞춤 |
| `.gitattributes` | gradlew LF, bat CRLF, jar binary | wrapper 사용 정책 유지 |
| `.gitignore` | build·IDE·도구 출력 제외 | 새 도구의 출력 경로 확인 |

원본 `compose.yaml`에는 개발용 비밀번호가 직접 적혀 있다. 이를 운영 자격정보로 가져가지 않는다. `lifecycle-management: start_only` 설정은 앱 종료 시 DB 컨테이너 종료까지 관리하는 설정이 아니다.

## 명령과 확인 범위

저장소 루트에서 실행한다. 사용자 환경에 RTK가 설치돼 있다면 각 명령 앞에 `rtk`를 붙인다. 분석 환경에서는 RTK 실행 파일을 찾지 못해 원래 명령을 사용했다.

```bash
./gradlew test --console=plain
./gradlew test --tests '*HexagonalArchitectureTest' --console=plain
./gradlew check --console=plain
./gradlew bootRun
./gradlew dependencyInsight --dependency hibernate-core --configuration runtimeClasspath
```

`test`는 테스트를, `check`는 빌드에 연결된 검증 작업을 실행한다. 네트워크 없는 환경에서 필요한 의존성이 이미 캐시돼 있으면 `--offline`을 사용할 수 있다. 현재 실제로 실행한 명령은 `./gradlew test --offline --console=plain`이며 테스트 컴파일에서 실패했다.

원본에 없는 도구: Flyway/Liquibase, Testcontainers, QueryDSL, MapStruct, Spring REST Docs/OpenAPI 설정, CI 워크플로, outbox, 분산 추적, 운영 프로파일. 이 도구들을 모두 필수 의존성으로 추가하지 말고 새 프로젝트 요구에 따라 결정한다. `기술아이템.md`에 언급된 Fixture Monkey는 실제 의존성으로 추가돼 있지 않다.
