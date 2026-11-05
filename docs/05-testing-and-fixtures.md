# 테스트, 픽스쳐, Instancio

> 패키지·업무 타입은 샘플 이름이다. 표기 규칙은 [README](README.md)를 따른다. 원본 구현을 설명하는 부분도 이 샘플 이름으로 표기한다.

## 테스트를 나누는 기준

| 대상 | 현재 구성 | 검증할 계약 |
| --- | --- | --- |
| 도메인 | JUnit 5 + AssertJ, Spring 미기동 | 초기 상태, 허용·금지 전이, 값 객체, 내부 엔티티 변경 |
| provided 포트 | `@ApplicationServiceTest`, 실제 서비스·저장소 | 유스케이스, validation 프록시, 중복·조회 실패 |
| required 저장소 | `@DataJpaTest`, EntityManager | ORM 매핑, 쿼리, DB 제약, flush/clear 후 재조회 |
| HTTP slice | `@WebMvcTest(AccountApi.class)` + `@MockitoBean` | JSON, DTO 검증, HTTP 코드, 포트 위임 |
| HTTP 통합 | `@SpringBootTest` + `@AutoConfigureMockMvc` + `@Transactional` | HTTP → 서비스 → DB 결과 |
| 기술 어댑터 | 일반 JUnit | BCrypt 인코딩·검증, 더미 메일 출력 |
| 아키텍처 | ArchUnit | 계층 의존, 업무 순환 참조, 다른 애그리거트 호출 |
| 학습 테스트 | `com.example.learningtest` | 라이브러리 동작 실험, 운영 기능과 분리 |

서비스 테스트는 구현체가 아닌 `AccountRegister`, `AuthorApplication` 같은 **포트 인터페이스를 주입**받는다. validation·트랜잭션이 중요한 테스트를 서비스 직접 생성만으로 대체하지 않는다.

현재 테스트에서 `@Test` 메서드는 62개, `@ArchTest` 규칙 메서드는 7개다. 미완성 테스트도 포함한 정적 집계이며 실행 성공 개수가 아니다.

## 공통 테스트 설정

현재 `ApplicationServiceTest`는 `@SpringBootTest`, `jakarta.transaction.Transactional`, `@Import(AppTestConfiguration.class)`를 조합한다. `WebApiAdapterTest`에는 추가로 `@AutoConfigureMockMvc`가 있지만 현재 API 테스트는 이 메타 애노테이션을 사용하지 않고 개별 애노테이션을 직접 선언한다.

`src/test/resources/junit-platform.properties`:

```properties
spring.test.constructor.autowire.mode=all
```

이 설정과 `@RequiredArgsConstructor` 또는 record 테스트 클래스를 조합해 생성자 주입한다. record는 변경 가능한 테스트 상태가 필요 없는 경우에만 선택한다. 원본에는 두 스타일이 공존한다.

`AppTestConfiguration`은 이메일 전송 lambda와 테스트용 PasswordEncoder를 등록한다. 이메일 어댑터는 `@Fallback`이라 일반 테스트 Bean이 우선할 수 있다. 비밀번호 어댑터에는 `@Fallback`이 없고, 테스트 Bean 이름·주입 지점 이름이 `passwordEncoder`다. 새 프로젝트에서는 이 이름 일치에만 기대지 말고 테스트 Bean의 `@Primary` 또는 명시적 Bean override로 선택을 확정한다. 아무 설정 없이 같은 타입 Bean 두 개를 등록하면 항상 자동 대체된다고 가정하지 않는다.

트랜잭션 테스트는 종료 후 rollback을 기대한다. 실제 commit 이후 이벤트, 별도 스레드·별도 트랜잭션의 동작은 이 테스트만으로 검증되지 않는다.

## 픽스쳐 설계

현재 픽스쳐는 각 `src/test/java/.../domain/<업무>` 패키지의 static 팩토리다.

- `AccountFixture`: 요청 생성, 계정 생성, ACTIVE 계정 생성, ID 강제 설정, 간단한 PasswordEncoder.
- `AuthorFixture`: 정상 계정으로 신청, 승인 상태 만들기, 저장된 계정 ID로 신청 요청 생성.
- `ContentFixture`: Instancio로 콘텐츠·상세 필드 생성, 주어진 작성자 또는 ACTIVE 작성자 삽입.

**새 프로젝트 규칙**:

1. 기본 픽스쳐는 유효하고 상태가 명확해야 한다. `createActiveXxx()`는 실제 전이 메서드를 호출한다.
2. 일반 도메인 객체는 public 생성 경로를 사용한다. 테스트하려는 생성·검증 로직을 reflection으로 우회하지 않는다.
3. 이메일·제목처럼 사례별로 중요한 값은 인자로 받거나 명시적으로 덮어쓴다.
4. 저장 테스트에서는 ID 없는 엔티티를 만들어 DB가 ID를 할당하게 한다.
5. API slice처럼 DB가 없는 테스트에 한해 `ReflectionTestUtils.setField(..., "id", id)`를 테스트 보조로 사용할 수 있다.
6. 서로 다른 애그리거트의 기존 참조가 필요하면 저장 순서를 픽스쳐 호출부에서 드러낸다.
7. 테스트 소스의 픽스쳐가 provided 요청을 만드는 것은 현재 허용돼 있다. production domain이 application에 의존해도 된다는 뜻은 아니다.

원본 PasswordEncoder fake는 대문자로 변환하고 비교한다. 도메인 테스트를 빠르게 만드는 목적이며 실제 보안 구현이 아니다. 실제 `SecurePasswordEncoder`는 별도 테스트에서 원문 일치·불일치를 검증한다. 새 fake는 테스트 대상의 대소문자 구별 등 필요한 계약을 훼손하지 않도록 만든다.

## Instancio 사용법과 경계

원본 의존성은 `org.instancio:instancio-core:6.1.0`이며 JUnit 전용 모듈은 선언하지 않는다. `src/main/resources/instancio.properties`에는 아래 값이 있다.

```properties
bean.validation.enabled=true
```

이를 이용해 `@Email`, `@Size` 등이 있는 record 요청을 생성한다. 새 프로젝트에서는 이 파일을 **`src/test/resources/instancio.properties`**에 두어 테스트 데이터 설정이 운영 리소스에 포함되지 않도록 한다. 제약 지원이 모든 업무 불변식을 보장하는 것은 아니다.

현재 `AccountFixture`의 핵심 형태:

```java
return Instancio.of(AccountRegisterRequest.class)
    .set(field(AccountRegisterRequest::email), email)
    .create();
```

`set`으로 고정한 값은 의도적으로 잘못된 이메일도 넣을 수 있으므로 검증 실패 테스트에 유용하다. 값 생성 API의 용도는 다음과 같다.

| API | 용도 |
| --- | --- |
| `set(field(...), value)` | 상태·참조·시험 대상 값을 고정 |
| `generate(field(...), gen -> ...)` | 형식·길이·범위 제한 |
| `ignore(field(...))` | 새 엔티티 ID 등 자동 생성에서 제외 |
| `toModel()` / `Instancio.of(model)` | 동일 생성 규칙을 반복 재사용 |
| `withSeed(long)` | 고정 seed로 생성 결과 재현; 새 테스트에 추가할 규칙 |

Model 예제는 [InstancioLearningTest](08-source-map-and-gaps.md)에 있다. 이 테스트는 ID 제외, 이메일 생성, PENDING 고정과 model 재사용을 보여준다. 원본에는 seed를 관리하는 공통 장치가 없다. 재현이 필요한 테스트는 seed를 명시하고 실패 보고에 기록한다.

랜덤 값은 중요하지 않은 데이터를 채우는 데 사용한다. 최소·최대 길이, null, 공백, 중복, 검색 포함 여부, 정렬 순서는 명시적인 사례로 검증한다. 랜덤 이메일이 언제나 고유하다고 전제하거나 랜덤 문자열이 특정 검색어를 절대 포함하지 않는다고 가정하지 않는다.

### ContentFixture를 그대로 이식하지 않을 이유

현재 픽스쳐는 `Content` 생성자를 우회해서 필드를 채운다. `ContentDetail.publishedAt`와 `archivedAt`를 제외하지 않아 DRAFT 객체에 랜덤 시각이 채워질 수 있다. description은 null 허용 생성이므로 검수 제출 성공 테스트는 이를 명시적으로 채워야 한다. 실제 ContentTest도 setUp에서 description을 덮어쓴다.

새 프로젝트의 정상 DRAFT 픽스쳐는 다음처럼 업무 생성자를 쓰는 것을 기본으로 한다.

```java
public static Content createContent(Author activeAuthor) {
    return new Content(activeAuthor, "테스트 콘텐츠", "검수 가능한 설명");
}
```

필드 직접 생성이 꼭 필요하면 ID, 상태, 모든 상태별 시각, 참조 객체를 통제하고 정상 생성 경로를 검증하는 테스트는 따로 둔다.

## 테스트별 작성 패턴

### 도메인

성공한 상태 전이뿐 아니라 잘못된 이전 상태·반복 호출도 검사한다. 변경 후 상태와 상세 시각을 함께 확인한다. 값 객체는 동등성·null·공백·경계·형식을 검사한다. 생성자를 테스트하는 사례에서는 테스트 대상 객체를 Instancio로 생성하지 않는다.

### 제공 포트

실제 Spring Bean을 호출해 정상 결과, 검증 실패, 미존재 참조, 중복, 금지된 상태 전이를 검증한다. 중요한 영속 변경은 flush/clear 후 읽는다. 이메일 전송을 검증해야 하면 출력만 하는 fake 대신 호출을 기록하는 fake 또는 mock을 사용한다. 현재 서비스 테스트는 실제 메일 발송을 검증하지 않는다.

### 저장소

`@DataJpaTest`에서 저장과 재조회, unique 제약, custom query 조건, 연관관계 cascade를 검증한다. 검색 테스트는 일치·불일치 값을 명시적으로 준비한다. MySQL 고유 동작이 중요하면 운영과 같은 DB 테스트를 별도 추가한다. 현재 H2 기반 구성만으로 모든 MySQL 동작이 검증됐다고 표현하지 않는다.

### HTTP

현재 `MockMvcTester`를 사용하며 `ObjectMapper`로 JSON을 만든다. slice에서는 `@MockitoBean AccountRegister`만 대체하고 상태 코드·body·위임 인자를 검사한다. 통합에서는 실제 저장소를 통해 결과도 읽는다. 새 입력 실패 테스트는 서비스가 호출되지 않았는지도 확인한다. 일반 서비스 테스트의 ConstraintViolationException과 MVC 입력 오류를 구분한다.

### ArchUnit

현재 [HexagonalArchitectureTest](08-source-map-and-gaps.md)는 production 클래스만 import한다. 계층 접근, domain 업무 순환, application 업무 순환, 다른 애그리거트 메서드 호출을 검사한다.

추가할 새 프로젝트 규칙:

- webapi는 application 구현 서비스와 required 저장소를 직접 의존하지 않는다.
- application 서비스는 자기 업무 provided 포트를 하나 이상 구현한다.
- 다른 업무의 required 포트 접근을 금지한다.
- `support`를 이용한 우회 의존을 막는다.

이 네 규칙은 원본에서 모두 자동 검사되고 있는 사항이 아니다. 이름 패턴 검사만으로 의미적 순수성을 증명할 수 없으므로 코드 리뷰와 함께 사용한다.

## 현재 실행 한계

참조 프로젝트의 전체 테스트는 작성 중인 저장소 테스트의 구문 오류로 컴파일 단계에서 실패했다. 실제 파일명·행 번호는 [원본 검증 기록](08-source-map-and-gaps.md)에 있다. `--tests`로 특정 테스트만 지정해도 전체 test source 컴파일이 선행하므로 이 구문 오류는 남는다. 작성 중인 코드를 임의로 삭제하거나 제외해서 전체 통과로 보고하지 않는다.
