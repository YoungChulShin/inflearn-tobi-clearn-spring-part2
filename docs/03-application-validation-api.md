# 서비스, 검증, API

> 패키지·업무 타입은 샘플 이름이다. 표기 규칙은 [README](README.md)를 따른다. 원본 구현을 설명하는 부분도 이 샘플 이름으로 표기한다.

## 서비스 구현 계약

현재 모든 애플리케이션 서비스 구현체는 provided 포트를 구현하고, `@RequiredArgsConstructor`와 final 필드로 생성자 주입한다.

```java
// 현재 AccountModifyService의 구조를 요약한 예시
@ValidatedApplicationService
@RequiredArgsConstructor
public class AccountModifyService implements AccountRegister {
    private final AccountFinder accountFinder;
    private final AccountRepository accountRepository;
    private final EmailSender emailSender;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Account register(AccountRegisterRequest request) {
        checkDuplicateEmail(request);
        Account account = Account.register(request.toInfo(), passwordEncoder);
        accountRepository.save(account);
        sendWelcomeEmail(account);
        return account;
    }
    // 보조 메서드와 다른 포트 메서드는 원본 참조
}
```

유스케이스는 보통 `필요한 객체 조회 → 전체 데이터가 필요한 규칙 확인 → 도메인 행위 → 저장 → 필요한 외부 효과 → 결과 반환` 순서다. 상태 변경 규칙을 서비스의 if 문으로 재구현하지 않는다.

`AccountQueryService.find()`는 저장소의 Optional을 꺼내거나 `IllegalArgumentException`을 던진다. `AccountAuthenticationService`는 계정 존재, ACTIVE 상태, 비밀번호 일치를 확인하고 실패를 `LoginFailedException`으로 통일한다. 이 로그인은 계정 확인 기능이며 세션·JWT 발급이나 접근 제어는 구현돼 있지 않다.

## 조합 애노테이션

| 애노테이션 | 현재 구성 | 의미 |
| --- | --- | --- |
| `@ApplicationService` | `@Service` + Spring `@Transactional` | Spring 서비스 등록 및 트랜잭션 경계 |
| `@ValidatedApplicationService` | `@ApplicationService` + `@Validated` | 위 기능과 메서드 검증 |
| `@Adapter` | TYPE, RUNTIME, Documented | 의미 표식. 단독으로는 Bean 등록하지 않음 |
| `@WebApiAdapter` | `@Adapter` + `@RestController` | HTTP 입력 어댑터 등록 |

모두 클래스 대상으로 런타임 유지한다. 새 프로젝트로 복사할 때 패키지를 치환한다. `DummyEmailSender`와 `SecurePasswordEncoder`는 현재 일반 `@Component`를 사용하므로 모든 어댑터에 `@Adapter`가 붙어 있다고 가정하지 않는다.

원본 조회 서비스도 `@ApplicationService`를 사용해 기본 read-write 트랜잭션이다. 새 프로젝트에서는 조회 전용 서비스의 `@Transactional(readOnly = true)`를 명시할 수 있지만 원본 구현 사실과 구분한다. 메서드 단위 설정은 실제 Spring 프록시를 통한 테스트로 확인한다.

## Jakarta Validation 실행 지점

```text
HTTP JSON
  → @RequestBody @Valid: HTTP 입력 검증
  → provided 메서드의 @Valid + 서비스의 @Validated: 유스케이스 입력 검증
  → 엔티티 생성자/메서드: 도메인 불변식
  → DB 제약: 저장 데이터의 최종 무결성
```

현재 `AccountRegister.register(@Valid AccountRegisterRequest)`에 검증 계약을 선언하고 구현체는 같은 제약을 반복 선언하지 않는다. MVC를 통하지 않는 다른 어댑터도 Spring 서비스 프록시를 통해 호출하면 유스케이스 검증을 받을 수 있다. `new AccountModifyService(...)`로 직접 만든 객체나 자기 자신 내부 호출에는 동일한 프록시 검증·트랜잭션을 기대하지 않는다.

**새 프로젝트 규칙**:

- 요청 필드가 필수면 `@NotNull` 또는 `@NotBlank`를 명시한다.
- `@Size`, `@Email`은 null 차단을 대신하지 않는다. 공백 문자열도 별도 의미로 검토한다.
- 요청 객체 자체가 null이면 안 되는 포트는 `@NotNull @Valid`를 함께 선언한다.
- 숫자 ID는 필수 여부와 양수 조건을 별도로 정한다. 원본은 일부 ID에 `@NotNull`만 사용하며 양수 검증이 없다.
- 중첩 객체는 `@Valid` 전파, 컬렉션은 컨테이너·원소 제약을 명시한다.
- 검증 계약은 포트에 두고 구현체에 메서드 인자 제약을 중복 추가하지 않는다.
- private 메서드 인자에 붙인 애노테이션만으로 검증됐다고 판단하지 않는다. 원본 `AccountDetail.convertToProfile()`이 그 예다.
- 필수 도메인 규칙은 직접 호출되는 생성자·행위에서도 확인한다. DTO validation에만 의존하지 않는다.

```java
// 새 프로젝트 예시. 원본보다 null·공백 계약을 명확히 한 형태
public record AccountRegisterRequest(
    @NotBlank @Email String email,
    @NotBlank @Size(min = 5, max = 20) String nickname,
    @NotBlank @Size(min = 8, max = 100) String password
) { }

public interface AccountRegister {
    Account register(@NotNull @Valid AccountRegisterRequest request);
}
```

필요 import는 `jakarta.validation.Valid`, `jakarta.validation.constraints.*`다. 위 제한 숫자는 계정 예제일 뿐이며 새 업무의 요구사항에 맞춘다.

## 중복 검사와 트랜잭션

현재 계정 이메일·프로필 중복은 서비스 조회로 검사하고 XML의 unique 제약으로도 보호한다. 프로필은 제거 요청과 자기 자신의 기존 값은 중복 조회에서 제외한다. 작성자 신청도 `findByAccountId()`로 선조회한다.

선조회는 동시 요청 사이의 중복까지 막지 못한다. 새 프로젝트에서는 업무 고유성에 DB unique 제약을 두고, 경쟁 요청의 제약 위반을 의도한 API 오류로 변환하는 테스트까지 작성한다. 작성자 계정 관계의 실제 제약은 원본 XML의 `one-to-one` 설정과 생성 DDL을 확인해야 하며, 명시적인 업무 unique 제약을 새 매핑에 기록하는 편이 명확하다.

원본 계정 등록은 DB 트랜잭션 내부에서 이메일을 동기 호출한다. 실제 메일 전송은 DB rollback으로 취소되지 않는다. 새 프로젝트에서 전송 신뢰성이 필요하다면 커밋 이후 실행 또는 outbox를 별도 설계한다. 원본에 이벤트·outbox·재시도는 없다. JPA 트랜잭션 안의 관리 엔티티는 변경 감지가 가능하지만, 원본 서비스는 수정 뒤에도 명시적으로 `save()`를 호출한다.

## HTTP 어댑터와 오류

현재 HTTP API는 `POST /api/accounts` 하나다. `@WebApiAdapter`, 포트 생성자 주입, `@RequestBody @Valid`, Response record를 사용한다. 등록 성공은 현재 **200 OK**이며 201이나 Location 헤더를 반환하지 않는다.

`ApiControllerAdvice`는 `ResponseEntityExceptionHandler`를 상속한다.

| 상황 | 현재 처리 |
| --- | --- |
| 이메일·프로필 중복 | 409 + ProblemDetail |
| MVC 요청 DTO 검증 실패 | 프레임워크의 400 처리 경로; WebMvc 테스트에 이메일 오류 사례 있음 |
| 나머지 예외 | 일반 핸들러에서 500 + ProblemDetail |

직접 만든 ProblemDetail에는 메시지, `timestamp`, 예외 단순 클래스명이 들어간다. 프레임워크가 만드는 응답 전체에 동일한 확장 필드가 붙는 구성은 아니다. 로그인 실패, 조회 실패, 작성자 중복, 서비스 메서드 검증 예외 각각에 대한 HTTP 변환은 현재 없다.

**새 프로젝트 규칙**: 필요한 API마다 성공·검증 실패·미존재·중복·인증 실패를 명시하고 테스트한다. 예를 들어 생성 201, 미존재 404, 중복 409는 선택 가능한 새 계약이다. HTTP 응답에 내부 예외 메시지나 클래스명을 그대로 노출하는 동작은 표준화하지 않는다. 엔티티와 비밀번호 해시는 응답 DTO로 내보내지 않는다.

원본 소스와 실제 이름은 [원본 분석 기록](08-source-map-and-gaps.md)에서 확인한다.
