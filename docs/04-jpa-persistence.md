# JPA와 ORM 매핑

> 패키지·업무 타입은 샘플 이름이다. 표기 규칙은 [README](README.md)를 따른다. 원본 구현을 설명하는 부분도 이 샘플 이름으로 표기한다.

## 채택한 방식

현재 프로젝트는 도메인 객체 자체를 JPA 엔티티로 사용한다. 별도 `AccountJpaEntity`나 영속 모델 변환기는 없다. Java에는 `@Entity`, `@OneToOne`, `@ManyToOne`, `@MappedSuperclass`, ID 설정 등 일부 표식을 남기고, 테이블·컬럼·enum·fetch·cascade·embedded 상세를 [META-INF/orm.xml](08-source-map-and-gaps.md)에 둔다.

이는 **애노테이션과 XML을 함께 쓰는 방식**이다. XML만 사용하거나 JPA 의존을 제거한 도메인이 아니다. XML은 FIELD access를 선언하고 Hibernate mapping namespace와 `mapping-3.1.0.xsd`를 참조한다. `metadata-complete`는 선언하지 않는다. 새 엔티티에서 양쪽 설정이 충돌하지 않도록 XML을 상세 매핑의 기준으로 유지하고 저장 테스트로 실제 적용을 확인한다.

`META-INF/orm.xml` 경로를 유지하면 이 프로젝트처럼 별도 mapping location 설정 없이 사용한다. 파일 이름·경로를 바꾼다면 해당 설정까지 명시해야 한다. 패키지를 바꿀 때 XML의 모든 class 속성도 반드시 변경한다.

## 현재 매핑 전체

| 타입 | 테이블/매핑 | 핵심 설정 |
| --- | --- | --- |
| `AbstractEntity` | mapped-superclass | `Long id`, IDENTITY, Java에도 동일 ID 표식 존재 |
| `Account` | `account` | nickname NOT NULL/100, password_hash NOT NULL/200, status STRING/50, email embedded |
| `Account.detail` | one-to-one | LAZY, cascade ALL, detail_id unique 명시 |
| `Account.email` | Email embedded | email_address unique 명시, Java `@NaturalId` |
| `AccountDetail` | `account_detail` | introduction TEXT, registered_at NOT NULL, activated_at·deactivated_at nullable |
| `AccountDetail.profile` | Profile embedded | profile_address unique 명시 |
| `Author` | `author` | status STRING/20, account one-to-one LAZY; XML에 status NOT NULL은 없음 |
| `Content` | `content` | title NOT NULL/100, status NOT NULL/20 |
| `Content.author` | many-to-one | optional=false, LAZY, cascade 없음 |
| `Content.detail` | one-to-one | optional=false, LAZY, cascade ALL |
| `ContentDetail` | `content_detail` | description 길이 500; 시각 필드는 XML에서 개별 선언하지 않음 |
| `Profile` | embeddable record | address → profile_address, 길이 20 |
| `Email` | embeddable record | address → email_address, NOT NULL, 길이 150 |

매핑에 생략된 join column 이름과 기본 필드 매핑은 provider의 규칙에도 의존한다. `ContentDetail.createdAt/publishedAt/archivedAt`은 Java 필드로 존재하며 XML에서 없다고 비영속 필드가 되는 것은 아니다. 새 프로젝트에서는 중요한 컬럼명·nullable·길이·인덱스·unique를 명시해 기본값 의존을 줄인다.

검증과 DDL은 목적이 다르다. 원본 닉네임 DTO는 5~20자지만 DB는 100자, 프로필은 최대 15자지만 DB는 20자다. 새 프로젝트에서 이 차이를 의도적으로 유지할지 맞출지 결정한다.

## 새 엔티티의 XML 예제

다음은 [AI 구현 예제](07-ai-implementation-playbook.md)의 Article용이다. 새로운 파일의 골격으로 사용할 수 있다. 이미 있는 orm.xml에 추가할 때는 `<entity>`만 합치고 공통 mapped-superclass를 중복 선언하지 않는다.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<entity-mappings xmlns="http://www.hibernate.org/xsd/orm/mapping"
                 xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                 xsi:schemaLocation="http://www.hibernate.org/xsd/orm/mapping
                                     https://hibernate.org/xsd/orm/mapping/mapping-3.1.0.xsd">
    <access>FIELD</access>
    <mapped-superclass class="com.example.app.domain.AbstractEntity">
        <attributes>
            <id name="id">
                <column name="id"/>
                <generated-value strategy="IDENTITY"/>
            </id>
        </attributes>
    </mapped-superclass>
    <entity class="com.example.app.domain.article.Article">
        <table name="article"/>
        <attributes>
            <basic name="title">
                <column name="title" nullable="false" length="100"/>
            </basic>
            <basic name="status">
                <column name="status" nullable="false" length="20"/>
                <enumerated>STRING</enumerated>
            </basic>
        </attributes>
    </entity>
</entity-mappings>
```

이 예제는 현재 Hibernate 기반을 그대로 사용하는 선택이다. 다른 JPA provider로 옮길 때는 XML schema, record embeddable 지원, 프록시 기반 동등성까지 다시 검증한다.

## required 저장소

```java
package com.example.app.application.article.required;

import java.util.Optional;
import org.springframework.data.repository.Repository;
import com.example.app.domain.article.Article;

public interface ArticleRepository extends Repository<Article, Long> {
    Article save(Article article);
    Optional<Article> findById(Long id);
}
```

현재 `AccountRepository`는 `save`, `findById`, `findByEmail`, `findByProfile`만 공개한다. 프로필 조회만 JPQL `select m from Account m where m.detail.profile = :profile`을 사용한다. `ContentRepository`는 `findByTitleContaining`, `findByAuthorId`, 그리고 엔티티 인자를 ID로 바꾸는 default 메서드를 제공한다. 페이지네이션은 현재 없다.

새 프로젝트에서도 필요한 쿼리만 추가한다. 검색 결과가 커질 수 있으면 페이징·정렬 계약을 먼저 정한다. 스프링 데이터 타입을 포트에 노출할지 역시 의도적으로 선택한다.

## 관계와 저장 순서

- `Account → AccountDetail`, `Content → ContentDetail`은 루트 저장으로 상세가 같이 저장되는 구조다.
- `Content → Author`, `Author → Account`는 다른 애그리거트 참조이며 cascade를 설정하지 않는다.
- 콘텐츠 저장 테스트에서는 계정 저장 → 작성자 저장 → 콘텐츠 저장 순서로 준비한다.
- `LAZY`는 설정 의도다. 실제 SQL·프록시 동작은 provider와 접근 방식으로 확인한다. LAZY만 붙였다고 N+1이나 세션 밖 접근 문제가 해결된 것으로 보지 않는다.
- 원본은 orphanRemoval, `@Version`, lock, fetch join/entity graph, migration 도구를 사용하지 않는다. 필요해지면 해당 변경의 테스트와 함께 도입한다.

## 저장 검증과 실행 환경

저장소 테스트는 `@DataJpaTest`와 실제 EntityManager를 사용한다. H2가 의존성에 있으므로 기본 테스트의 embedded DB 경로를 사용할 수 있다. 원본에는 Testcontainers나 실제 MySQL을 강제하는 테스트 설정이 없다.

저장 검증은 `save → flush → clear → find → 필드와 관계 검증`으로 작성한다. `clear` 없이 같은 엔티티를 다시 읽으면 1차 캐시만 검증할 수 있다. IDENTITY에서는 `save()` 시점에 SQL이 실행될 수 있지만 다른 제약은 flush에서 드러날 수 있으므로 새 테스트는 검증할 SQL 실행 시점을 명확히 한다.

원본 `ContentRepositoryTest.saveAndFindId()`는 재조회 후 equals만 확인한다. ID 기반 equals만으로 title·status·detail 매핑의 정확성을 증명할 수 없으므로 새 테스트에서는 해당 필드도 확인한다.

현재 application.yml은 `ddl-auto: update`, `show-sql: true`다. 개발 예제 설정으로 취급한다. 새 프로젝트에서 운영 DB를 사용한다면 스키마 변경 절차와 별도 프로파일을 정하고 migration 또는 관리 DDL과 일치하는지 검증한다. 이 프로젝트가 이미 운영 migration을 제공한다고 가정하지 않는다.

## JPA를 선택하지 않을 때

포트·서비스·도메인 행위 구조는 유지할 수 있다. 그 경우 `AbstractEntity`의 Hibernate 프록시 의존, JPA 애노테이션, Spring Data 상속, orm.xml, `@DataJpaTest`를 함께 재설계한다. XML만 제거하고 같은 저장·ID·동등성 동작을 기대하지 않는다.

원본 소스와 실제 이름은 [원본 분석 기록](08-source-map-and-gaps.md)에서 확인한다.
