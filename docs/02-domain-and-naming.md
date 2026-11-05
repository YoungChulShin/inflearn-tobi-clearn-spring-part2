# 도메인 모델과 네이밍

> 패키지·업무 타입은 샘플 이름이다. 표기 규칙은 [README](README.md)를 따른다. 원본 구현을 설명하는 부분도 이 샘플 이름으로 표기한다.

## 클래스·메서드 이름

| 역할 | 현재 예 | 새 프로젝트 규칙 |
| --- | --- | --- |
| 애그리거트 루트 | `Account`, `Author`, `Content` | 업무 명사. `Entity` 접미사는 기본적으로 생략 |
| 내부 엔티티 | `AccountDetail`, `ContentDetail` | 실제 분리할 생명주기·데이터가 있을 때 `<업무>Detail` |
| 상태 | `AccountStatus`, `ContentStatus` | `<업무>Status` enum, 상수는 대문자 스네이크 |
| 값 객체 | `Email`, `Profile` | 의미를 가진 명사, 가능하면 record |
| 제공 포트 | `AccountRegister`, `AccountFinder`, `ContentPublisher` | `<업무><역할>`. 조회 `Finder`, 기능에 따라 `Creator`, `Authenticator` 등 |
| 변경 서비스 | `AccountModifyService` | `<업무>ModifyService`, 기능이 충분히 크면 별도 역할명 |
| 조회 서비스 | `AccountQueryService` | `<업무>QueryService` |
| 특화 서비스 | `AccountAuthenticationService` | `<업무><기능>Service` |
| 요구 포트 | `AccountRepository`, `EmailSender` | 저장소 `Repository`, 외부 행위는 `Sender`, `Client` 등 구체 역할 |
| 어댑터 | `AccountApi`, `SecurePasswordEncoder`, `DummyEmailSender` | HTTP는 `<업무>Api`, 외부 기술 구현은 성격을 드러내는 이름 |
| 유스케이스 입력 | `AccountRegisterRequest`, `AuthorApplyRequest` | `<업무><행위>Request`, provided 소속 |
| 도메인 입력 | `AccountRegisterInfo`, `ContentUpdateInfo` | `<업무><행위>Info`, domain 소속 |
| HTTP 출력 | `AccountRegisterResponse` | `<업무><행위>Response`, webapi.dto 소속 |
| 업무 예외 | `DuplicateEmailException`, `LoginFailedException` | 실패 의미 + `Exception` |
| 테스트 데이터 | `AccountFixture`, `ContentFixture` | `<업무>Fixture`, 상태가 드러나는 `createActiveAccount()` 등 |
| 테스트 | `AccountTest`, `AccountRegisterTest`, `AccountApiWebMvcTest` | 검증 대상의 역할 기준. 포트 테스트는 구현체명보다 포트명을 사용 |

원본의 `AuthorApplication`은 작성자 신청 포트의 이름이다. 모든 업무 포트를 `Application`으로 이름 지으라는 규칙은 아니다. `AccountInfoUpdateRequest`는 현재 domain에 있는 명명 예외다. 새 코드에서는 domain 입력은 `Info`, application 입력은 `Request`로 정리한다.

메서드 이름은 `register`, `apply`, `activate`, `deactivate`, `approve`, `reject`, `submitForReview`, `publish`, `archive`, `updateInfo`처럼 업무 행위를 사용한다. `setStatus()` 같은 범용 setter로 상태 전이를 노출하지 않는다. `find()`는 없으면 예외를 던지는 포트, `Optional` 반환은 부재가 정상인 조회임을 계약에 명시한다.

## 객체 생성과 변경

현재 `Account.register()`와 `Author.apply()`는 정적 팩토리이며, `Content`는 업무 인자를 받는 public 생성자를 사용한다. 생성 방식은 둘 다 가능하다. 핵심은 유효한 초기 상태를 한 경로에서 보장하는 것이다.

**새 프로젝트 규칙**:

- 엔티티 필드는 private을 기본으로 하고 setter를 제공하지 않는다. 현재 `Author`의 package-private 필드는 그대로 일반화하지 않는다.
- JPA용 무인자 생성자는 protected로 둔다. Lombok을 쓰면 `@NoArgsConstructor(access = AccessLevel.PROTECTED)`를 사용한다.
- 최초 상태·내부 엔티티·생성 시각은 팩토리 또는 생성자에서 초기화한다.
- 상태 전이 전제는 메서드 내부에서 확인한다. 원본의 `Assert.state()`는 실패 시 `IllegalStateException`을 던진다.
- 필수 값은 생성 경로에서 확인한다. 원본처럼 `requireNonNull()`을 사용할 수 있으며, 업무상 금지된 공백·범위도 필요한 곳에서 직접 검사한다.
- 원본은 `@Getter`와 `@ToString`을 사용한다. 새 프로젝트에서는 연관관계와 비밀번호·토큰 등 민감 필드를 `toString`에서 제외한다. `@Data`로 setter와 동등성을 일괄 생성하지 않는다.

원본 `Account`의 `toString`은 `detail`만 제외해 `passwordHash`가 포함될 수 있다. `Content`의 `exclude = {}`는 연관 객체를 제외하지 않는다. 두 설정을 새 프로젝트 표준으로 복사하지 않는다.

## 현재 상태 전이

| 도메인 | 생성 및 전이 | 추가 조건 |
| --- | --- | --- |
| Account | 생성 `PENDING` → `activate` → `ACTIVE` → `deactivate` → `DEACTIVATED` | `updateInfo`는 ACTIVE에서만 가능 |
| Author | 신청 `PENDING` → `approve` → `ACTIVE`, 또는 `reject` → `REJECTED` | 신청 계정이 ACTIVE여야 함 |
| Content | 생성 `DRAFT` → `submitForReview` → `IN_REVIEW` → `publish` → `PUBLISHED` → `archive` → `ARCHIVED` | 생성 작성자가 ACTIVE, 검수 제출 시 description에 텍스트가 있어야 함 |

현재 `Content.updateInfo()`에는 상태 제한이 없다. 수정 가능 상태를 새 요구사항 없이 추정해서 제한하지 않는다. `AccountDetail`은 활성·탈퇴 시각 중복 설정을 검사하지만 `ContentDetail`은 루트의 상태 전이 검사를 통해 변경을 통제한다.

현재 시간은 `LocalDateTime.now()`로 얻는다. 시간대·시각 경계 테스트가 중요해지면 `Clock` 또는 명시적 시각 전달을 별도 설계한다. 원본에 주입형 Clock이나 UTC 정책은 없다.

## 값 객체와 입력 DTO

`Email`과 `Profile`은 record다. 값 동등성이 필요하고 자체 형식 검증을 수행한다.

- `Profile`: 소문자·숫자, 최대 15자, null 불가. 빈 문자열은 객체 생성이 가능하지만 계정 상세 갱신에서는 null로 저장해 프로필 제거를 표현한다.
- `Email`: 정규식 검사. 현재 정규식은 마지막 도메인 길이를 2~7자로 제한하며 null을 별도 처리하지 않는다. 새 프로젝트의 이메일 정책으로 자동 승계하지 않는다.
- `AccountRegisterRequest.toInfo()`: 유스케이스 입력을 도메인 입력으로 바꾼다. domain이 application 요청 타입을 import하지 않도록 한다.
- `AccountRegisterResponse.of(account)`: 도메인 값을 HTTP 출력으로 바꾼다. 엔티티 전체를 직렬화하지 않는다.

DTO의 Jakarta Validation 애노테이션은 객체를 `new`로 생성하는 순간 실행되지 않는다. domain 단위 테스트에서 `AccountInfoUpdateRequest("Leo", ...)`가 사용되는 것은 이 차이를 보여준다. 닉네임 5자 제한을 단위 객체 호출 자체에서도 보장하려면 도메인에 별도 검증이 필요하다.

## 엔티티 동등성

현재 `AbstractEntity`는 다음 규칙을 가진다.

1. 같은 인스턴스는 같다.
2. Hibernate 프록시를 고려한 실제 엔티티 클래스가 같아야 한다.
3. ID가 null이 아니고 같아야 서로 다른 인스턴스를 같다고 판단한다.
4. hashCode는 실제 클래스 기반이어서 영속화로 ID가 생겨도 바뀌지 않는다.

ID가 없는 새 엔티티 두 개를 같다고 취급하지 않는 것이 핵심이다. 새 프로젝트에서 이 공통 기반을 채택하면 proxy·저장 전후·서로 다른 타입의 동등성을 검증한다. 엔티티 필드 전체를 이용한 equals/hashCode나 record 엔티티로 바꾸지 않는다.

## 예외 위치와 null 정책

원본에는 domain의 `DuplicateEmailException`/`DuplicateProfileException`과 provided의 `LoginFailedException`/`DuplicateAuthorApplicationException`이 공존한다. 새 프로젝트에서는 도메인 자체의 실패는 domain, 유스케이스 수행 실패는 provided에 두는 것을 기본으로 한다. HTTP 예외 타입을 안쪽 계층에 두지 않는다.

현재 `domain/package-info.java`와 `application/package-info.java`에는 `@NonNullApi`가 있지만 **하위 패키지까지 재귀 적용되지 않는다**. 또한 런타임 Bean Validation을 대신하지 않는다. ID는 저장 전 null이고, 프로필·상태별 시각·콘텐츠 설명도 null이 가능하다. 새 프로젝트에서는 실제 필수값과 nullable 필드를 명시하고 nullability 애노테이션 체계를 일관되게 사용한다. 원본 테스트에는 Spring과 JSpecify의 `@Nullable`이 혼재한다.

원본 소스와 실제 이름은 [원본 분석 기록](08-source-map-and-gaps.md)에서 확인한다.
