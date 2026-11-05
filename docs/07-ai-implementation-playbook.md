# 다른 AI를 위한 구현 절차와 예제

> 패키지·업무 타입은 샘플 이름이다. 표기 규칙은 [README](README.md)를 따른다. 원본 구현을 설명하는 부분도 이 샘플 이름으로 표기한다.

## 새 프로젝트에 전달할 작업 지시문

아래 내용을 프로젝트 요구사항과 함께 구현 AI에게 전달한다.

```text
docs/README.md와 01~07 문서를 읽고 이 문서의 아키텍처를 적용하라.
08은 원본 대조가 필요할 때만 읽는 참고 기록이다.

먼저 제품 요구사항에서 업무 용어, 애그리거트, 상태 전이, 유스케이스를 도출하라.
com.example.app과 Account/Author/Content/Article은 샘플 이름이다.
패키지·클래스·변수·XML class 경로·테스트 경로를 새 업무에 맞춰 함께 치환하라.
샘플 업무 기능과 상태 전이는 제품 요구사항에 맞게 정하라.

기본은 단일 모듈 Java/Spring Boot와 domain/application/adapter/support 구조다.
application.<업무>.provided에 유스케이스 인터페이스를 먼저 만들고,
서비스가 이를 구현하게 하라. 호출자는 구체 서비스 대신 포트를 주입받아라.
상태 전이는 도메인 메서드에, 트랜잭션과 조회·저장 조정은 서비스에 두어라.
요청은 record와 Jakarta Validation으로 만들고 필수값·공백 정책을 명시하라.

JPA를 쓰면 도메인 엔티티 + META-INF/orm.xml + 필요한 메서드만 있는
Spring Data Repository 방식을 기본으로 한다. JPA 미사용이면 관련 의존과
ID·동등성 전략까지 함께 바꿔라. 불필요한 adapter.persistence를 만들지 마라.

도메인 → 매핑/required → provided/서비스 → API 순으로 기능 하나를 완성하라.
테스트는 도메인·저장소·제공 포트·HTTP·아키텍처로 나눠 검증하라.
픽스쳐는 유효한 상태를 만들고 중요한 값은 고정하며 Instancio seed 재현성을 확보하라.

문서의 '현재 구현'과 '새 프로젝트 규칙'을 구분하라.
원본 콘텐츠 서비스는 미구현이며 원본 테스트에는 미완성 구문이 있다.
없는 기능이나 실행하지 않은 검증을 완료됐다고 보고하지 마라.
새 프로젝트에서 추가한 선택은 근거와 함께 문서화하라.
```

## 구현 전 확정할 입력

| 결정 | 기본값/처리 |
| --- | --- |
| 루트 패키지·프로젝트명 | `com.example.app`/`App`을 실제 이름으로 치환 |
| 업무 용어 | 제품 요구사항에서 한국어·영어 명칭 고정 |
| 상태와 전이 | 각 행위의 이전 상태·이후 상태·금지 조건 작성 |
| 저장소 | JPA 사용 시 04 문서 적용, DB 제품·스키마 변경 절차 결정 |
| 고유성 | 어떤 필드 또는 필드 조합이 고유한지 명시 |
| 입력 제약 | null, 공백, 길이, 범위, 생략·삭제 의미 구분 |
| API | 경로·성공 코드·오류 코드·응답 필드 결정 |
| 외부 효과 | 이메일·결제 등이 있으면 트랜잭션과 실패 처리 결정 |
| 인증·인가 | 필요한 경우 별도 설계. 원본 로그인 확인으로 완료 처리하지 않음 |

요구사항으로 결정되지 않은 업무 규칙을 예제에서 추정하지 않는다. 구조·파일 위치 같은 가역적인 구현 선택은 위 기본값을 사용하고, 결과에 선택 내용을 기록한다.

## 구현 순서

1. Gradle wrapper, Java toolchain, 의존성, 기본 앱 클래스를 구성한다. 06 문서의 버전은 재현 기준이며 바꾼다면 호환성을 검증한다.
2. 네 개 조합 애노테이션과 테스트 설정을 작성한다. `@Adapter` 자체는 Component가 아님에 유의한다.
3. JPA 사용 시 공통 AbstractEntity와 orm.xml을 준비한다. [도메인 문서](02-domain-and-naming.md)의 프록시 대응 동등성 규칙을 구현한다.
4. 업무의 값 객체·상태 enum·루트·내부 엔티티를 구현하고 일반 JUnit 테스트로 전이를 검증한다.
5. required 저장소와 XML 매핑을 추가하고 `@DataJpaTest`로 flush/clear 후 저장값을 확인한다.
6. provided 포트·Request record·Info 변환·서비스 구현을 추가한다. 실제 Spring 포트 테스트로 validation까지 확인한다.
7. API·Response record·예외 변환을 추가하고 slice 테스트와 필요한 통합 흐름을 확인한다.
8. ArchUnit으로 의존 방향·순환·포트 경계 규칙을 검사한다.
9. 환경 설정과 실행 명령을 작성하고 `test`, `check` 결과를 기록한다.

## 수직 기능 예제: 게시글 생성·발행

이 예제는 **새 프로젝트용 설계 예시**이며 원본에 Article 기능은 없다. 업무 규칙은 제목 2~100자·공백 불가, DRAFT 생성, DRAFT에서만 발행이다. 생성 성공은 201, 발행 성공은 200, 미존재는 404, 상태 충돌은 409로 정했다.

각 코드 블록은 표기된 패키지의 별도 Java 파일이다. 공통 AbstractEntity, 조합 애노테이션, 앱 클래스, 의존성은 앞 단계에서 준비해야 한다. Lombok을 사용한다. XML은 04 문서의 Article 예제를 함께 추가한다.

### 도메인

```java
package com.example.app.domain.article;

public enum ArticleStatus { DRAFT, PUBLISHED }
```

```java
package com.example.app.domain.article;

public record ArticleCreateInfo(String title) { }
```

```java
package com.example.app.domain.article;

public class ArticleStateException extends RuntimeException {
    public ArticleStateException(String message) { super(message); }
}
```

```java
package com.example.app.domain.article;

import com.example.app.domain.AbstractEntity;
import jakarta.persistence.Entity;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Article extends AbstractEntity {
    private String title;
    private ArticleStatus status;

    public static Article create(ArticleCreateInfo info) {
        Objects.requireNonNull(info);
        String title = Objects.requireNonNull(info.title());
        if (title.isBlank() || title.length() < 2 || title.length() > 100) {
            throw new IllegalArgumentException("제목은 공백이 아닌 2~100자여야 합니다");
        }
        Article article = new Article();
        article.title = title;
        article.status = ArticleStatus.DRAFT;
        return article;
    }

    public void publish() {
        if (status != ArticleStatus.DRAFT) {
            throw new ArticleStateException("초안 상태에서만 발행할 수 있습니다");
        }
        status = ArticleStatus.PUBLISHED;
    }
}
```

제목 제약을 유스케이스 입력과 도메인에서 모두 지킨다. 직접 생성 테스트에서도 같은 불변식을 보장하기 위한 예제의 의도다. 복잡한 규칙으로 발전하면 제목 값 객체로 모을 수 있다.

### 제공 포트와 입력

```java
package com.example.app.application.article.provided;

import com.example.app.domain.article.ArticleCreateInfo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ArticleCreateRequest(
    @NotBlank @Size(min = 2, max = 100) String title
) {
    public ArticleCreateInfo toInfo() { return new ArticleCreateInfo(title); }
}
```

```java
package com.example.app.application.article.provided;

import com.example.app.domain.article.Article;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public interface ArticleCreator {
    Article create(@NotNull @Valid ArticleCreateRequest request);
}
```

```java
package com.example.app.application.article.provided;

import com.example.app.domain.article.Article;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public interface ArticlePublisher {
    Article publish(@NotNull @Positive Long articleId);
}
```

```java
package com.example.app.application.article.provided;

import com.example.app.domain.article.Article;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public interface ArticleFinder {
    Article find(@NotNull @Positive Long articleId);
}
```

```java
package com.example.app.application.article.provided;

public class ArticleNotFoundException extends RuntimeException {
    public ArticleNotFoundException(Long id) {
        super("게시글을 찾을 수 없습니다. ID: " + id);
    }
}
```

### 서비스

required의 `ArticleRepository`는 04 문서의 코드대로 작성한다. 조회 서비스도 인자 검증을 수행하도록 이 예제에서는 ValidatedApplicationService를 사용한다. 읽기 전용 트랜잭션도 원본에서 보완한 선택이다.

```java
package com.example.app.application.article;

import com.example.app.application.article.provided.ArticleFinder;
import com.example.app.application.article.provided.ArticleNotFoundException;
import com.example.app.application.article.required.ArticleRepository;
import com.example.app.domain.article.Article;
import com.example.app.support.stereotype.ValidatedApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@ValidatedApplicationService
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ArticleQueryService implements ArticleFinder {
    private final ArticleRepository articleRepository;

    @Override
    public Article find(Long articleId) {
        return articleRepository.findById(articleId)
            .orElseThrow(() -> new ArticleNotFoundException(articleId));
    }
}
```

```java
package com.example.app.application.article;

import com.example.app.application.article.provided.ArticleCreateRequest;
import com.example.app.application.article.provided.ArticleCreator;
import com.example.app.application.article.provided.ArticleFinder;
import com.example.app.application.article.provided.ArticlePublisher;
import com.example.app.application.article.required.ArticleRepository;
import com.example.app.domain.article.Article;
import com.example.app.support.stereotype.ValidatedApplicationService;
import lombok.RequiredArgsConstructor;

@ValidatedApplicationService
@RequiredArgsConstructor
public class ArticleModifyService implements ArticleCreator, ArticlePublisher {
    private final ArticleRepository articleRepository;
    private final ArticleFinder articleFinder;

    @Override
    public Article create(ArticleCreateRequest request) {
        return articleRepository.save(Article.create(request.toInfo()));
    }

    @Override
    public Article publish(Long articleId) {
        Article article = articleFinder.find(articleId);
        article.publish();
        return articleRepository.save(article);
    }
}
```

변경 서비스에서 조회 서비스를 호출하면 기본 REQUIRED 전파에 따라 바깥 변경 트랜잭션에 참여한다. `readOnly`를 데이터 변경을 금지하는 장치로 취급하지 않는다.

### HTTP 출력과 API

```java
package com.example.app.adapter.webapi.dto;

import com.example.app.domain.article.Article;

public record ArticleResponse(Long articleId, String title, String status) {
    public static ArticleResponse of(Article article) {
        return new ArticleResponse(
            article.getId(), article.getTitle(), article.getStatus().name());
    }
}
```

```java
package com.example.app.adapter.webapi;

import com.example.app.adapter.webapi.dto.ArticleResponse;
import com.example.app.application.article.provided.ArticleCreateRequest;
import com.example.app.application.article.provided.ArticleCreator;
import com.example.app.application.article.provided.ArticlePublisher;
import com.example.app.support.stereotype.WebApiAdapter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@WebApiAdapter
@RequiredArgsConstructor
public class ArticleApi {
    private final ArticleCreator articleCreator;
    private final ArticlePublisher articlePublisher;

    @PostMapping("/api/articles")
    @ResponseStatus(HttpStatus.CREATED)
    public ArticleResponse create(@RequestBody @Valid ArticleCreateRequest request) {
        return ArticleResponse.of(articleCreator.create(request));
    }

    @PostMapping("/api/articles/{articleId}/publish")
    public ArticleResponse publish(@PathVariable("articleId") Long articleId) {
        return ArticleResponse.of(articlePublisher.publish(articleId));
    }
}
```

```java
package com.example.app.adapter;

import com.example.app.application.article.provided.ArticleNotFoundException;
import com.example.app.domain.article.ArticleStateException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class ApiControllerAdvice extends ResponseEntityExceptionHandler {
    @ExceptionHandler(ArticleNotFoundException.class)
    ProblemDetail notFound(ArticleNotFoundException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, "게시글을 찾을 수 없습니다");
    }

    @ExceptionHandler(ArticleStateException.class)
    ProblemDetail conflict(ArticleStateException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, "현재 상태에서는 발행할 수 없습니다");
    }

    @ExceptionHandler(ConstraintViolationException.class)
    ProblemDetail invalidArgument(ConstraintViolationException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "입력값이 올바르지 않습니다");
    }
}
```

MVC의 요청 검증·타입 변환 오류는 상위 ResponseEntityExceptionHandler의 처리에 맡긴다. 위 코드는 모든 운영 오류 관측·로깅 정책까지 구현한 템플릿은 아니다. API 계약에 필요한 업무 오류의 최소 연결을 보여준다.

### 픽스쳐와 도메인 테스트

```java
package com.example.app.domain.article;

import com.example.app.application.article.provided.ArticleCreateRequest;
import org.instancio.Instancio;
import static org.instancio.Select.field;

public final class ArticleFixture {
    private ArticleFixture() { }

    public static ArticleCreateRequest createRequest(String title) {
        return Instancio.of(ArticleCreateRequest.class)
            .withSeed(20261015L)
            .set(field(ArticleCreateRequest::title), title)
            .create();
    }

    public static Article createDraft() {
        return Article.create(createRequest("테스트 게시글").toInfo());
    }
}
```

필드 하나인 record에는 `new ArticleCreateRequest(title)`만으로도 충분하다. 위 예제는 여러 필드를 가진 요청으로 확장할 때 중요한 값 고정·seed 사용 방식을 보여준다.

```java
package com.example.app.domain.article;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ArticleTest {
    @Test
    void publishOnlyOnce() {
        Article article = ArticleFixture.createDraft();
        assertThat(article.getStatus()).isEqualTo(ArticleStatus.DRAFT);

        article.publish();
        assertThat(article.getStatus()).isEqualTo(ArticleStatus.PUBLISHED);
        assertThatThrownBy(article::publish).isInstanceOf(ArticleStateException.class);
    }

    @Test
    void rejectBlankTitle() {
        assertThatThrownBy(() -> Article.create(new ArticleCreateInfo("  ")))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
```

### 기능 완료에 필요한 테스트

아래 테스트까지 구현해야 이 예제의 기능을 완료한 것으로 본다. 표는 테스트 명세이며 모두 실행됐다는 의미가 아니다.

| 테스트 대상 | 입력·동작 | 기대 결과 |
| --- | --- | --- |
| Article | 제목 null, 공백, 1자, 101자 | 생성 실패 |
| Article | 2자, 100자 | DRAFT 생성 |
| ArticleRepository | 저장→flush→clear→재조회 | ID, title, status 일치 |
| ArticleCreator | Spring Bean에 유효한 request | 저장된 DRAFT |
| ArticleCreator | null request, null/공백/너무 짧은 title | ConstraintViolationException |
| ArticlePublisher | 존재하는 DRAFT ID | PUBLISHED, 재조회 후에도 같은 상태 |
| ArticlePublisher | 존재하지 않는 양수 ID | ArticleNotFoundException |
| ArticlePublisher | 이미 발행된 ID | ArticleStateException |
| ArticleApi | POST create | 201과 제한된 응답 필드 |
| ArticleApi | 잘못된 JSON·title | 400, 생성 포트는 호출되지 않음 |
| ArticleApi | 발행 성공/미존재/재발행/음수 ID | 200/404/409/400 |
| ArchUnit | 새 article 패키지 | 의존·순환·포트 규칙 위반 없음 |

## 완료 체크리스트

- [ ] 업무 용어와 상태 전이 표가 있고 새 제품 요구사항과 일치한다.
- [ ] 서비스는 provided 포트를 구현하고 생성자로 인터페이스 의존성을 주입받는다.
- [ ] domain → application/adapter 의존, application → adapter 의존이 없다.
- [ ] 다른 업무의 변경은 provided 포트를 거치며 업무 간 순환 참조가 없다.
- [ ] HTTP 상태·응답 DTO·예외 변환이 코드와 테스트에서 일치한다.
- [ ] `@Valid`, `@Validated`, null·공백·범위 제약이 실제 호출 경로에서 작동한다.
- [ ] 직접 도메인 호출에도 상태 불변식이 유지된다.
- [ ] JPA 엔티티의 class 경로·필드·관계가 XML과 일치한다.
- [ ] unique·nullable·cascade·fetch 정책이 명시되고 중요한 저장 동작을 검증했다.
- [ ] 픽스쳐가 ID·상태·시각을 임의로 불일치시키지 않는다.
- [ ] 중요한 테스트 입력은 고정돼 있고 랜덤 실패를 재현할 수 있다.
- [ ] 도메인·포트·저장소·API·ArchUnit 테스트와 필요한 정적 검사가 통과한다.
- [ ] 실행한 명령, 성공·실패·미실행 범위를 결과에 기록했다.
- [ ] 운영에 필요한 DB·인증·외부 효과·관측 설정의 구현 범위를 명시했다.
- [ ] 코드에 반영한 설계 선택과 예제에서 달라진 부분을 프로젝트 문서에 반영했다.
