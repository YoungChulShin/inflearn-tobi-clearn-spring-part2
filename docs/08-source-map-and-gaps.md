# 분석 범위, 소스 인덱스와 이식 주의점

> 이 파일은 원본 대조용 선택 참고 문서다. 실제 패키지·클래스 이름과 소스 링크를 보존한다. 새 프로젝트 구현에는 README와 01~07 문서의 샘플 이름을 사용한다.

## 원본과 샘플 이름의 대응

| 원본 표기 | 구현 가이드의 샘플 표기 |
| --- | --- |
| `tobyspring.splearn`, `tobyspring/splearn` | `com.example.app`, `com/example/app` |
| `tobyspring.learningtest` | `com.example.learningtest` |
| `splearn`, `SplearnApplication`, `SplearnTestConfiguration` | `sample-app`, `AppApplication`, `AppTestConfiguration` |
| `Member*`, `member*`, 회원 | `Account*`, `account*`, 계정 |
| `Instructor*`, `instructor*`, 강사 | `Author*`, `author*`, 작성자 |
| `Course*`, `course*`, 강의 | `Content*`, `content*`, 콘텐츠 |

클래스 이름 내부의 `Member` 등에도 같은 치환을 적용한다. 예를 들어 `findByMemberId`는 `findByAccountId`, `createActiveInstructor`는 `createActiveAuthor`로 설명한다. `Article`은 처음부터 별도로 만든 구현 예제이며 원본 클래스와 일대일 대응하지 않는다. 이 이름 변경은 문서에만 적용했다.

## 분석 범위

2026-10-15의 HEAD `90514c1` 및 작업 트리를 읽었다. 메인 Java 53개(1,122줄), 테스트 Java 34개(1,441줄), 빌드·리소스·루트 도메인 문서·drawio 모델의 개념과 관계를 확인했다. 기존 build 산출물은 현재 소스의 검증 결과로 대체하지 않았다.

현재 사용자 작업에는 강의 provided 5개, required 저장소, 저장소 테스트, 강의·강사 픽스쳐 변경이 있다. 이 파일들을 수정하거나 staging 상태를 바꾸지 않고 문서를 추가했다. 인덱스는 아래에 각 production·test Java 파일을 모두 나열한다.

## 구현 범위

| 업무 | 도메인 | 서비스/포트 | HTTP | 비고 |
| --- | --- | --- | --- | --- |
| 회원 | 루트·상세·값 객체·상태 | 등록/활성화/탈퇴/수정/조회/로그인 구현 | 회원 등록만 구현 | 로그인은 회원 확인까지 |
| 강사 | 신청·승인·거절·상태 | 신청/승인/거절/조회 구현 | 없음 | 회원 조회는 MemberFinder 사용 |
| 강의 | 생성·검수 제출·발행·보관·정보 수정 | provided 인터페이스와 required 저장소 존재, 서비스 없음 | 없음 | 저장소 테스트 작성 중 |
| 수업·섹션·수강·진도 | 개념 문서·drawio에 등장 | 없음 | 없음 | 구현 대상으로 자동 포함하지 않음 |

## 이식 시 보완할 항목

| 항목 | 현재 관찰 | 새 프로젝트에서 할 일 |
| --- | --- | --- |
| 테스트 컴파일 | CourseRepositoryTest 55행에 미완성 `CourseFixture` 식 | 작성 완료 후 전체 테스트 재실행; 이 분석에서는 원본 미수정 |
| 강의 유스케이스 | 인터페이스만 존재 | 서비스·검증·API 구현이 완료된 예로 오인하지 않기 |
| 필수 입력 | 많은 String 필드가 Size/Email만 사용 | NotNull/NotBlank와 공백 정책 추가 |
| 포트 요청 자체 | Valid만으로 null 요청을 차단하지 않음 | 필수 요청이면 NotNull 추가 |
| 도메인 검증 | DTO 제약이 직접 객체 호출에서 실행되지 않음 | 직접 호출에도 필요한 불변식을 명시적으로 검사 |
| nullability | 부모 패키지의 NonNullApi, 일부 nullable 표식 혼재 | 실제 하위 패키지·필드 계약과 일치시키기 |
| 조회 트랜잭션 | QueryService도 기본 read-write | 읽기 전용 여부를 명시적으로 결정 |
| 고유성 경쟁 | 서비스 선조회; 회원 DB unique 존재 | 동시성 제약 위반의 API 변환·테스트 추가 |
| HTTP 예외 | 일부 중복 외 나머지 일반 예외는 500 경로 | 미존재·인증·상태·입력 오류를 업무별로 매핑 |
| 오류 상세 | 내부 예외 메시지와 클래스명 노출 | 외부 응답 메시지와 내부 진단을 분리 |
| 외부 효과 | 트랜잭션 중 동기 더미 이메일 호출 | 실제 전송 시 실패·중복·커밋 시점 정책 설계 |
| 보안 | BCrypt와 회원 비밀번호 확인 | 필요 시 세션/토큰·인증·인가를 별도 구현 |
| 로그 문자열 | Member의 passwordHash, Course의 관계가 ToString에서 제외되지 않음 | 민감 필드·연관관계 제외 |
| CourseFixture | 필드 직접 채움, 상태별 미래 시각 통제 없음 | 정상 생성 경로 또는 완전히 통제된 모델 사용 |
| 랜덤 재현성 | 공통 seed 정책 없음 | 중요한 값 고정, 실패 seed 재현 |
| 테스트 Bean | passwordEncoder 이름에 따른 선택 가능 | 명시적 Primary/override로 선택 보장 |
| 저장소 검증 | Course 저장 후 ID equals 중심 | title/status/detail·쿼리 조건까지 검증 |
| XML/DB 제약 | 일부 nullable·시간·join column은 기본값 의존 | 필요한 매핑을 명시하고 대상 DB에서 확인 |
| 아키텍처 검사 | 포트만 접근, 서비스 implements 등은 모두 자동 강제되지 않음 | 원하는 규칙을 ArchUnit에 추가 |
| 운영 설정 | ddl-auto update, show-sql, mysql latest, 개발 비밀번호 | 운영 프로파일·스키마 변경·버전·비밀값 관리 결정 |

이 표의 보완 사항은 코드 변경 요청이 아니라 새 프로젝트 구현 시의 판단 자료다. 프레임워크 분리·멀티모듈·CQRS 인프라·outbox를 모두 선제 추가할 이유는 없다.

## 실행 검증

```text
명령: ./gradlew test --offline --console=plain
환경: OpenJDK 21.0.8
compileJava: UP-TO-DATE
processResources: UP-TO-DATE
compileTestJava: FAILED
원인: CourseRepositoryTest.java:55, not a statement / ';' expected
테스트 실행: 도달하지 못함
```

SpotBugs, 실제 MySQL 구동, HTTP 서버 기동, 동시성 테스트는 실행하지 않았다. 정적 분석에서 확인한 동작과 실행 성공을 구분한다.

### 문서 예제의 별도 실행 검증

04·07 문서에서 Java/XML 코드 블록을 추출하고, 원본 AbstractEntity·조합 애노테이션의 패키지를 치환해 `/private/tmp` 아래 임시 Gradle 프로젝트를 만들었다. 원본 의존성을 사용하고 H2 및 테스트용 Spring Boot 진입점으로 실행했다. 테스트 보조 코드는 임시 경로에만 작성했다.

```text
명령: ./gradlew -p <임시 예제 프로젝트> test --offline --console=plain
Java 예제: 컴파일 성공
ArticleTest: 2 tests, 0 failures, 0 errors
ExampleIntegrationTest: 6 tests, 0 failures, 0 errors
결과: BUILD SUCCESSFUL
```

검증 범위는 도메인 발행·재발행 금지·공백 거부, XML 매핑에 따른 저장과 flush/clear 후 필드 재조회, Spring 포트의 null·공백·음수 검증, 미존재 예외, MockMvc의 생성 201·입력 오류 400·발행 200·중복 발행 409·미존재 404다. 별도 HTTP 서버를 띄운 테스트는 아니다. 07 문서의 전체 완료 체크리스트나 모든 경계값·ArchUnit 추가 규칙까지 검증한 것은 아니다.

문서의 상대 링크, 코드 블록 쌍, production 53개와 test 34개 파일의 소스 인덱스 누락 여부도 확인했다.

## 전체 Java 소스 인덱스

아래 경로는 원본 저장소 기준 상대 링크다. 이 문서 묶음을 다른 프로젝트에 복사할 경우 원본 증거 확인용으로 사용한다.

### Production: 53개

| 파일 | 역할 |
| --- | --- |
| [tobyspring/splearn/SplearnApplication.java](../src/main/java/tobyspring/splearn/SplearnApplication.java) | Spring Boot 진입점 |
| [tobyspring/splearn/adapter/ApiControllerAdvice.java](../src/main/java/tobyspring/splearn/adapter/ApiControllerAdvice.java) | ProblemDetail 예외 변환 |
| [tobyspring/splearn/adapter/integration/DummyEmailSender.java](../src/main/java/tobyspring/splearn/adapter/integration/DummyEmailSender.java) | Fallback 메일 어댑터·표준 출력 |
| [tobyspring/splearn/adapter/security/SecurePasswordEncoder.java](../src/main/java/tobyspring/splearn/adapter/security/SecurePasswordEncoder.java) | BCrypt 도메인 인터페이스 구현 |
| [tobyspring/splearn/adapter/webapi/MemberApi.java](../src/main/java/tobyspring/splearn/adapter/webapi/MemberApi.java) | POST /api/members |
| [tobyspring/splearn/adapter/webapi/dto/MemberRegisterResponse.java](../src/main/java/tobyspring/splearn/adapter/webapi/dto/MemberRegisterResponse.java) | HTTP 응답 record·of 변환 |
| [tobyspring/splearn/application/course/provided/CourseCreateRequest.java](../src/main/java/tobyspring/splearn/application/course/provided/CourseCreateRequest.java) | 강의 생성 입력 record |
| [tobyspring/splearn/application/course/provided/CourseCreator.java](../src/main/java/tobyspring/splearn/application/course/provided/CourseCreator.java) | 강의 생성·수정 제공 포트, 구현체 없음 |
| [tobyspring/splearn/application/course/provided/CourseFinder.java](../src/main/java/tobyspring/splearn/application/course/provided/CourseFinder.java) | 강의 조회 제공 포트, 구현체 없음 |
| [tobyspring/splearn/application/course/provided/CourseInfoUpdateRequest.java](../src/main/java/tobyspring/splearn/application/course/provided/CourseInfoUpdateRequest.java) | 강의 수정 입력 record |
| [tobyspring/splearn/application/course/provided/CoursePublisher.java](../src/main/java/tobyspring/splearn/application/course/provided/CoursePublisher.java) | 강의 검수·발행·보관 제공 포트, 구현체 없음 |
| [tobyspring/splearn/application/course/required/CourseRepository.java](../src/main/java/tobyspring/splearn/application/course/required/CourseRepository.java) | 강의 저장·제목·강사별 조회 required 포트 |
| [tobyspring/splearn/application/instructor/InstructorModifyService.java](../src/main/java/tobyspring/splearn/application/instructor/InstructorModifyService.java) | 강사 신청·승인·거절 구현 |
| [tobyspring/splearn/application/instructor/InstructorQueryService.java](../src/main/java/tobyspring/splearn/application/instructor/InstructorQueryService.java) | 강사 조회 구현 |
| [tobyspring/splearn/application/instructor/provided/DuplicateInstructorApplicationException.java](../src/main/java/tobyspring/splearn/application/instructor/provided/DuplicateInstructorApplicationException.java) | 중복 신청 실패 |
| [tobyspring/splearn/application/instructor/provided/InstructorApplication.java](../src/main/java/tobyspring/splearn/application/instructor/provided/InstructorApplication.java) | 강사 신청·승인·거절 제공 포트 |
| [tobyspring/splearn/application/instructor/provided/InstructorApplyRequest.java](../src/main/java/tobyspring/splearn/application/instructor/provided/InstructorApplyRequest.java) | 강사 신청 입력 |
| [tobyspring/splearn/application/instructor/provided/InstructorFinder.java](../src/main/java/tobyspring/splearn/application/instructor/provided/InstructorFinder.java) | 강사 ID·회원별 조회 제공 포트 |
| [tobyspring/splearn/application/instructor/required/InstructorRepository.java](../src/main/java/tobyspring/splearn/application/instructor/required/InstructorRepository.java) | 강사 저장·조회 required 포트 |
| [tobyspring/splearn/application/member/MemberAuthenticationService.java](../src/main/java/tobyspring/splearn/application/member/MemberAuthenticationService.java) | 로그인 포트 구현 |
| [tobyspring/splearn/application/member/MemberModifyService.java](../src/main/java/tobyspring/splearn/application/member/MemberModifyService.java) | 회원 변경 포트 구현·중복 검사·저장·메일 |
| [tobyspring/splearn/application/member/MemberQueryService.java](../src/main/java/tobyspring/splearn/application/member/MemberQueryService.java) | 회원 조회 포트 구현 |
| [tobyspring/splearn/application/member/provided/LoginFailedException.java](../src/main/java/tobyspring/splearn/application/member/provided/LoginFailedException.java) | 로그인 유스케이스 실패 |
| [tobyspring/splearn/application/member/provided/MemberAuthenticator.java](../src/main/java/tobyspring/splearn/application/member/provided/MemberAuthenticator.java) | 회원 로그인 제공 포트 |
| [tobyspring/splearn/application/member/provided/MemberFinder.java](../src/main/java/tobyspring/splearn/application/member/provided/MemberFinder.java) | 회원 조회 제공 포트 |
| [tobyspring/splearn/application/member/provided/MemberLoginRequest.java](../src/main/java/tobyspring/splearn/application/member/provided/MemberLoginRequest.java) | 로그인 입력 record |
| [tobyspring/splearn/application/member/provided/MemberRegister.java](../src/main/java/tobyspring/splearn/application/member/provided/MemberRegister.java) | 회원 등록·활성화·탈퇴·수정 제공 포트 |
| [tobyspring/splearn/application/member/provided/MemberRegisterRequest.java](../src/main/java/tobyspring/splearn/application/member/provided/MemberRegisterRequest.java) | 회원 등록 입력·toInfo 변환 |
| [tobyspring/splearn/application/member/required/EmailSender.java](../src/main/java/tobyspring/splearn/application/member/required/EmailSender.java) | 이메일 발송 required 포트 |
| [tobyspring/splearn/application/member/required/MemberRepository.java](../src/main/java/tobyspring/splearn/application/member/required/MemberRepository.java) | 회원 저장·이메일·프로필 조회 required 포트 |
| [tobyspring/splearn/application/package-info.java](../src/main/java/tobyspring/splearn/application/package-info.java) | 해당 패키지 NonNullApi 선언 |
| [tobyspring/splearn/domain/AbstractEntity.java](../src/main/java/tobyspring/splearn/domain/AbstractEntity.java) | ID 및 프록시 대응 엔티티 동등성 |
| [tobyspring/splearn/domain/course/Course.java](../src/main/java/tobyspring/splearn/domain/course/Course.java) | 강의 루트·검수·발행·보관 |
| [tobyspring/splearn/domain/course/CourseDetail.java](../src/main/java/tobyspring/splearn/domain/course/CourseDetail.java) | 강의 설명·생성/발행/보관 시각 |
| [tobyspring/splearn/domain/course/CourseStatus.java](../src/main/java/tobyspring/splearn/domain/course/CourseStatus.java) | 강의 상태 enum |
| [tobyspring/splearn/domain/course/CourseUpdateInfo.java](../src/main/java/tobyspring/splearn/domain/course/CourseUpdateInfo.java) | 도메인 강의 수정 입력 |
| [tobyspring/splearn/domain/instructor/Instructor.java](../src/main/java/tobyspring/splearn/domain/instructor/Instructor.java) | 강사 애그리거트 루트 |
| [tobyspring/splearn/domain/instructor/InstructorStatus.java](../src/main/java/tobyspring/splearn/domain/instructor/InstructorStatus.java) | 강사 상태 enum |
| [tobyspring/splearn/domain/member/DuplicateEmailException.java](../src/main/java/tobyspring/splearn/domain/member/DuplicateEmailException.java) | 회원 이메일 중복 실패 |
| [tobyspring/splearn/domain/member/DuplicateProfileException.java](../src/main/java/tobyspring/splearn/domain/member/DuplicateProfileException.java) | 회원 프로필 중복 실패 |
| [tobyspring/splearn/domain/member/Member.java](../src/main/java/tobyspring/splearn/domain/member/Member.java) | 회원 애그리거트 루트와 상태 전이 |
| [tobyspring/splearn/domain/member/MemberDetail.java](../src/main/java/tobyspring/splearn/domain/member/MemberDetail.java) | 회원 내부 상세·시각·프로필 변경 |
| [tobyspring/splearn/domain/member/MemberInfoUpdateRequest.java](../src/main/java/tobyspring/splearn/domain/member/MemberInfoUpdateRequest.java) | domain에 위치한 정보 수정 입력 record |
| [tobyspring/splearn/domain/member/MemberRegisterInfo.java](../src/main/java/tobyspring/splearn/domain/member/MemberRegisterInfo.java) | 도메인 등록 입력 record |
| [tobyspring/splearn/domain/member/MemberStatus.java](../src/main/java/tobyspring/splearn/domain/member/MemberStatus.java) | 회원 상태 enum |
| [tobyspring/splearn/domain/member/PasswordEncoder.java](../src/main/java/tobyspring/splearn/domain/member/PasswordEncoder.java) | 도메인이 요구하는 비밀번호 인코딩 추상화 |
| [tobyspring/splearn/domain/member/Profile.java](../src/main/java/tobyspring/splearn/domain/member/Profile.java) | 프로필 주소 값 객체 |
| [tobyspring/splearn/domain/package-info.java](../src/main/java/tobyspring/splearn/domain/package-info.java) | 해당 패키지 NonNullApi 선언 |
| [tobyspring/splearn/domain/shared/Email.java](../src/main/java/tobyspring/splearn/domain/shared/Email.java) | 공유 이메일 값 객체 |
| [tobyspring/splearn/support/stereotype/Adapter.java](../src/main/java/tobyspring/splearn/support/stereotype/Adapter.java) | 어댑터 의미 표식 |
| [tobyspring/splearn/support/stereotype/ApplicationService.java](../src/main/java/tobyspring/splearn/support/stereotype/ApplicationService.java) | Service + Transactional 조합 |
| [tobyspring/splearn/support/stereotype/ValidatedApplicationService.java](../src/main/java/tobyspring/splearn/support/stereotype/ValidatedApplicationService.java) | 서비스 + Validated 조합 |
| [tobyspring/splearn/support/stereotype/WebApiAdapter.java](../src/main/java/tobyspring/splearn/support/stereotype/WebApiAdapter.java) | Adapter + RestController 조합 |

### Test: 34개

| 파일 | 역할 |
| --- | --- |
| [tobyspring/learningtest/archunit/ArchUnitLearningTest.java](../src/test/java/tobyspring/learningtest/archunit/ArchUnitLearningTest.java) | 3개 학습용 구조 규칙 |
| [tobyspring/learningtest/archunit/adapter/MyAdapter.java](../src/test/java/tobyspring/learningtest/archunit/adapter/MyAdapter.java) | ArchUnit 학습용 어댑터 |
| [tobyspring/learningtest/archunit/application/MyService.java](../src/test/java/tobyspring/learningtest/archunit/application/MyService.java) | ArchUnit 학습용 서비스 |
| [tobyspring/learningtest/archunit/application/MyService2.java](../src/test/java/tobyspring/learningtest/archunit/application/MyService2.java) | ArchUnit 학습용 서비스 의존 |
| [tobyspring/learningtest/archunit/domain/MyMember.java](../src/test/java/tobyspring/learningtest/archunit/domain/MyMember.java) | ArchUnit 학습용 도메인 |
| [tobyspring/learningtest/instancio/InstancioLearningTest.java](../src/test/java/tobyspring/learningtest/instancio/InstancioLearningTest.java) | 생성·selector·Model·validation 실험 |
| [tobyspring/learningtest/instancio/User.java](../src/test/java/tobyspring/learningtest/instancio/User.java) | Instancio 학습용 POJO |
| [tobyspring/learningtest/instancio/UserRegisterRequest.java](../src/test/java/tobyspring/learningtest/instancio/UserRegisterRequest.java) | Instancio 검증 제약 학습용 record |
| [tobyspring/learningtest/instancio/UserStatus.java](../src/test/java/tobyspring/learningtest/instancio/UserStatus.java) | Instancio 학습용 enum |
| [tobyspring/splearn/AssertThatUtils.java](../src/test/java/tobyspring/splearn/AssertThatUtils.java) | MockMvc JSON assertion 보조 |
| [tobyspring/splearn/HexagonalArchitectureTest.java](../src/test/java/tobyspring/splearn/HexagonalArchitectureTest.java) | 4개 production 구조 규칙 |
| [tobyspring/splearn/SplearnApplicationTest.java](../src/test/java/tobyspring/splearn/SplearnApplicationTest.java) | main의 SpringApplication.run 정적 호출 검증 |
| [tobyspring/splearn/SplearnTestConfiguration.java](../src/test/java/tobyspring/splearn/SplearnTestConfiguration.java) | 메일·PasswordEncoder 테스트 Bean |
| [tobyspring/splearn/adapter/integration/DummyEmailSenderTest.java](../src/test/java/tobyspring/splearn/adapter/integration/DummyEmailSenderTest.java) | JUnit Pioneer 표준 출력 검증 |
| [tobyspring/splearn/adapter/security/SecurePasswordEncoderTest.java](../src/test/java/tobyspring/splearn/adapter/security/SecurePasswordEncoderTest.java) | BCrypt 원문 일치·불일치 |
| [tobyspring/splearn/adapter/webapi/MemberApiTest.java](../src/test/java/tobyspring/splearn/adapter/webapi/MemberApiTest.java) | HTTP 통합·저장 결과·409 |
| [tobyspring/splearn/adapter/webapi/MemberApiWebMvcTest.java](../src/test/java/tobyspring/splearn/adapter/webapi/MemberApiWebMvcTest.java) | API slice, MockitoBean, 성공·400 |
| [tobyspring/splearn/application/course/required/CourseRepositoryTest.java](../src/test/java/tobyspring/splearn/application/course/required/CourseRepositoryTest.java) | 강의 저장·재조회, 제목 조회 테스트 미완성 |
| [tobyspring/splearn/application/instructor/provided/InstructorApplicationTest.java](../src/test/java/tobyspring/splearn/application/instructor/provided/InstructorApplicationTest.java) | 강사 신청·중복·승인·거절 통합 |
| [tobyspring/splearn/application/instructor/provided/InstructorFinderTest.java](../src/test/java/tobyspring/splearn/application/instructor/provided/InstructorFinderTest.java) | 회원별 강사 조회와 부재 |
| [tobyspring/splearn/application/member/provided/MemberAuthenticatorTest.java](../src/test/java/tobyspring/splearn/application/member/provided/MemberAuthenticatorTest.java) | 로그인 성공과 세 가지 실패 조건 |
| [tobyspring/splearn/application/member/provided/MemberFinderTest.java](../src/test/java/tobyspring/splearn/application/member/provided/MemberFinderTest.java) | 조회 포트·flush/clear·미존재 |
| [tobyspring/splearn/application/member/provided/MemberRegisterTest.java](../src/test/java/tobyspring/splearn/application/member/provided/MemberRegisterTest.java) | 회원 변경 제공 포트 통합·중복·validation |
| [tobyspring/splearn/application/member/required/MemberRepositoryTest.java](../src/test/java/tobyspring/splearn/application/member/required/MemberRepositoryTest.java) | 회원 저장·상세 cascade·이메일 unique |
| [tobyspring/splearn/domain/course/CourseFixture.java](../src/test/java/tobyspring/splearn/domain/course/CourseFixture.java) | Instancio 강의·상세 직접 필드 생성 |
| [tobyspring/splearn/domain/course/CourseTest.java](../src/test/java/tobyspring/splearn/domain/course/CourseTest.java) | 강의 생성·상태 전이·수정 테스트 |
| [tobyspring/splearn/domain/instructor/InstructorFixture.java](../src/test/java/tobyspring/splearn/domain/instructor/InstructorFixture.java) | 정상 강사 상태·신청 요청 생성 |
| [tobyspring/splearn/domain/instructor/InstructorTest.java](../src/test/java/tobyspring/splearn/domain/instructor/InstructorTest.java) | 강사 신청·승인·거절·상태 테스트 |
| [tobyspring/splearn/domain/member/MemberFixture.java](../src/test/java/tobyspring/splearn/domain/member/MemberFixture.java) | Instancio 요청·정상 회원·fake encoder·reflection ID |
| [tobyspring/splearn/domain/member/MemberTest.java](../src/test/java/tobyspring/splearn/domain/member/MemberTest.java) | 회원 생성·상태·수정·비밀번호 도메인 테스트 |
| [tobyspring/splearn/domain/member/ProfileTest.java](../src/test/java/tobyspring/splearn/domain/member/ProfileTest.java) | 주소 형식·길이·URL 테스트 |
| [tobyspring/splearn/domain/shared/EmailTest.java](../src/test/java/tobyspring/splearn/domain/shared/EmailTest.java) | 값 동등성 테스트 |
| [tobyspring/splearn/support/stereotype/ApplicationServiceTest.java](../src/test/java/tobyspring/splearn/support/stereotype/ApplicationServiceTest.java) | 서비스 통합 테스트용 조합 애노테이션 |
| [tobyspring/splearn/support/stereotype/WebApiAdapterTest.java](../src/test/java/tobyspring/splearn/support/stereotype/WebApiAdapterTest.java) | API 통합 테스트용 조합 애노테이션, 현재 미사용 |

### 설정·기존 문서 인덱스

| 파일 | 분석한 내용 |
| --- | --- |
| [build.gradle.kts](../build.gradle.kts) | 플러그인·의존성·Test agent·SpotBugs |
| [settings.gradle.kts](../settings.gradle.kts) | 단일 프로젝트 이름 |
| [compose.yaml](../compose.yaml) | 개발 MySQL |
| [spotbugs-exclude-filter.xml](../spotbugs-exclude-filter.xml) | 동등성 검사 제외 패턴 |
| [gradle/wrapper/gradle-wrapper.properties](../gradle/wrapper/gradle-wrapper.properties) | Gradle 배포 버전 |
| [.sdkmanrc](../.sdkmanrc) | JDK 선택 |
| [.gitignore](../.gitignore) | 생성물 제외 |
| [.gitattributes](../.gitattributes) | wrapper 줄바꿈·binary |
| [src/main/resources/application.yml](../src/main/resources/application.yml) | JPA·Compose 설정 |
| [src/main/resources/META-INF/orm.xml](../src/main/resources/META-INF/orm.xml) | 전체 영속 매핑 |
| [src/main/resources/instancio.properties](../src/main/resources/instancio.properties) | Bean Validation 기반 데이터 생성 |
| [src/test/resources/junit-platform.properties](../src/test/resources/junit-platform.properties) | 테스트 생성자 주입 |
| [README.md](../README.md) | 기존 프로젝트·도메인·용어·기술 설명 |
| [개발가이드.md](../개발가이드.md) | 기존 프로젝트·도메인·용어·기술 설명 |
| [도메인모델.md](../도메인모델.md) | 기존 프로젝트·도메인·용어·기술 설명 |
| [용어사전.md](../용어사전.md) | 기존 프로젝트·도메인·용어·기술 설명 |
| [기술아이템.md](../기술아이템.md) | 기존 프로젝트·도메인·용어·기술 설명 |
| [도메인모델.drawio](../도메인모델.drawio) | 회원·강사·강의·수강 등 개념 관계도 |
