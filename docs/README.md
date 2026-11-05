# 새 프로젝트를 위한 아키텍처 구현 가이드

이 문서 묶음은 참조 프로젝트의 전체 Java 소스, 테스트, 빌드·실행 설정과 기존 도메인 문서를 분석해 작성했다. 다른 AI가 새 프로젝트에 적용할 수 있도록 **본문과 예제에는 샘플 패키지·업무 클래스 이름**을 사용한다.

분석 기준과 실제 소스 이름·경로·검증 기록은 선택 참고 문서인 [08-source-map-and-gaps.md](08-source-map-and-gaps.md)에 보관한다. 라이브러리 버전은 참조 프로젝트의 선언 값이며 최신 버전 추천을 의미하지 않는다.

## 샘플 이름과 치환 규칙

| 항목 | 샘플 값 | 새 프로젝트 적용 |
| --- | --- | --- |
| 루트 패키지 | `com.example.app` | 조직·프로젝트에 맞는 루트 패키지로 치환 |
| 소스 경로 | `com/example/app` | 패키지와 함께 치환 |
| 프로젝트명 | `sample-app` | Gradle·애플리케이션·DB 이름에 적용 |
| 진입점 | `AppApplication` | 새 애플리케이션 이름에 맞게 변경 |
| 계정 예제 | `Account`, `account` | 루트·서비스·포트·DTO·테스트의 이름을 함께 변경 |
| 작성자 예제 | `Author`, `author` | 계정에 연결된 다른 애그리거트의 예시 |
| 콘텐츠 예제 | `Content`, `content` | 작성자를 참조하는 애그리거트의 예시 |
| 독립 구현 예제 | `Article`, `article` | 04·07 문서의 작은 게시글 생성·발행 기능 |

`AccountModifyService`, `AccountRegisterRequest`, `AccountFixture`처럼 파생 이름에도 같은 샘플 명칭을 사용한다. `AbstractEntity`, `ApplicationService`, `Email` 같은 공통 역할명과 Spring·JPA·Jakarta 등 라이브러리의 실제 타입 이름은 유지한다. 업무 예시와 상태 규칙은 새 제품 요구사항에 맞게 교체한다.

**다른 AI에는 이 README와 01~07 문서를 기본으로 전달한다.** 08 문서는 실제 코드 대조가 필요할 때만 전달한다. 샘플 이름은 원본 소스의 실제 파일명으로 검색하기 위한 이름이 아니다.

## 읽는 순서

| 문서 | 목적 |
| --- | --- |
| [01-architecture.md](01-architecture.md) | 헥사고날 패키지, 의존 방향, 포트와 서비스, 애그리거트 경계 |
| [02-domain-and-naming.md](02-domain-and-naming.md) | 클래스·메서드 네이밍, 엔티티·값 객체·상태 전이 |
| [03-application-validation-api.md](03-application-validation-api.md) | 서비스 구현, 트랜잭션, Jakarta Validation, HTTP와 예외 |
| [04-jpa-persistence.md](04-jpa-persistence.md) | Spring Data, `orm.xml`, 연관관계, 동등성과 저장 테스트 |
| [05-testing-and-fixtures.md](05-testing-and-fixtures.md) | 테스트 계층, 픽스쳐, Instancio, Mockito, ArchUnit |
| [06-build-and-libraries.md](06-build-and-libraries.md) | 라이브러리·버전·설정 파일과 실행 명령 |
| [07-ai-implementation-playbook.md](07-ai-implementation-playbook.md) | AI 구현 지시문, 수직 기능 구현 예제, 완료 체크리스트 |
| [08-source-map-and-gaps.md](08-source-map-and-gaps.md) | 선택 참고: 실제 이름을 보존한 소스 인덱스·구현 범위·검증 결과 |

## 문서를 해석하는 규칙

- **현재 구현**: 참조 프로젝트에서 확인한 사실을 샘플 명칭으로 설명한 것이다. 원본과 샘플 이름의 대응은 08 문서에 있다.
- **새 프로젝트 규칙**: 이 구조를 이식하기 위한 기본 구현 계약이다. 원본의 불일치를 정리한 선택도 포함한다.
- **선택 사항**: 제품 요구사항이 생겼을 때 도입한다. 원본에 구현되어 있다는 뜻이 아니다.
- 구현 가이드는 다른 저장소에서도 읽을 수 있다. 실제 소스 파일로 향하는 링크는 08 문서에 모아 두었다.
- 원본 코드와 새 프로젝트 규칙이 다르면 원본을 무조건 복제하지 말고 해당 차이를 확인한다. 제품 요구사항이 우선한다.

## 채택할 기본 구조

**단일 Gradle 모듈, Java 21, Spring Boot, 도메인 모델 패턴, 포트와 어댑터를 사용하는 헥사고날 아키텍처**를 기본으로 한다.

1. `domain`, `application`, `adapter`, `support`를 최상위 패키지로 둔다.
2. `domain`과 `application` 아래는 업무별로 묶고, `application.<업무>.provided/required`에 포트를 둔다.
3. 모든 애플리케이션 서비스는 `provided` 인터페이스를 구현한다. 호출자는 구현 서비스 대신 포트를 주입받는다.
4. 상태 전이와 불변식은 도메인이, 조회·중복 확인·저장·외부 호출의 순서는 서비스가 담당한다.
5. 생성자 주입, 불변 `record` DTO, 역할을 드러내는 클래스명, 조합 애노테이션을 사용한다.
6. 입력 검증은 HTTP와 애플리케이션 포트 경계에서 수행하고, 도메인 불변식은 도메인 메서드 안에서도 지킨다.
7. JPA를 선택하면 원본처럼 도메인 엔티티 + `orm.xml` + 최소 Spring Data 저장소 방식을 기본으로 한다.
8. 도메인 단위 테스트, 포트 통합 테스트, 저장소 테스트, API 테스트, 아키텍처 테스트를 구분한다.

이 구조는 프레임워크 의존을 모두 제거하지 않는다. 현재 도메인은 JPA·Hibernate·Spring 유틸리티를 사용하고, required 저장소는 Spring Data를 상속한다. 프레임워크 독립 도메인이나 멀티모듈 분리는 별도 설계 변경이다.

## 확인된 구현 범위와 검증 상태

샘플 명칭으로 설명하면, 계정은 등록·활성화·탈퇴·수정·로그인 서비스와 등록 HTTP API까지 구현돼 있다. 작성자는 신청·승인·거절·조회 서비스까지 있으며 HTTP API는 없다. 콘텐츠는 도메인·ORM·포트·저장소가 있고 **애플리케이션 서비스와 HTTP API는 아직 없다**.

참조 프로젝트의 전체 테스트는 작성 중인 저장소 테스트의 구문 오류로 컴파일 단계에서 실패했다. 실제 실패 파일과 명령은 08 문서에 기록했다. 문서 작성 과정에서 애플리케이션·테스트 소스는 수정하지 않았다.

별도로 04·07 문서의 Java/XML 예제를 임시 프로젝트로 추출해 컴파일하고 **테스트 8개를 통과**했다. 도메인 테스트 2개와 H2·Spring·MockMvc 통합 검증 6개이며, 원본 프로젝트 전체 테스트 결과와 구분한다. 자세한 범위는 [검증 기록](08-source-map-and-gaps.md)을 참고한다.
