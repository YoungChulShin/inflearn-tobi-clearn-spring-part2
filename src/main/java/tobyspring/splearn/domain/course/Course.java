package tobyspring.splearn.domain.course;

import static java.util.Objects.*;
import static org.springframework.util.Assert.state;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;
import tobyspring.splearn.domain.AbstractEntity;
import tobyspring.splearn.domain.instructor.Instructor;

@Entity
@Getter
@ToString(callSuper = true, exclude = {})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Course extends AbstractEntity {

  @ManyToOne
  private Instructor instructor;

  private String title;

  private CourseStatus status;

  @OneToOne
  private CourseDetail detail;

  public Course(Instructor instructor, String title, @Nullable String description) {
    instructor.ensureActive();

    this.instructor = requireNonNull(instructor);
    this.title = requireNonNull(title);
    this.status = CourseStatus.DRAFT;

    this.detail = new CourseDetail(description);
  }

  public void submitForReview() {
    state(status == CourseStatus.DRAFT, "DRAFT 상태가 아닙니다");
    state(StringUtils.hasText(detail.getDescription()), "강의 소개가 등록되지 않았습니다");

    this.status = CourseStatus.IN_REVIEW;
  }

  public void publish() {
    state(status == CourseStatus.IN_REVIEW, "IN_REVIEW 상태가 아닙니다");

    this.status = CourseStatus.PUBLISHED;
    this.detail.publish();
  }

  public void archive() {
    state(status == CourseStatus.PUBLISHED, "PUBLISHED 상태가 아닙니다");

    this.status = CourseStatus.ARCHIVED;
    this.detail.archive();
  }

  public boolean isPublished() {
    return this.status == CourseStatus.PUBLISHED;
  }

  public void ensurePublished() {
    state(isPublished(), "PUBLISHED 상태가 아닙니다");
  }

  public void updateInfo(CourseUpdateInfo updateInfo) {
    this.title = Objects.requireNonNull(updateInfo.title());
    this.detail.updateInfo(updateInfo);
  }
}
