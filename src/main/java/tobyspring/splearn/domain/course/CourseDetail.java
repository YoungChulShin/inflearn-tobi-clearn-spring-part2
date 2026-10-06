package tobyspring.splearn.domain.course;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import tobyspring.splearn.domain.AbstractEntity;

@Entity
@Getter
@ToString(callSuper = true, exclude = {})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CourseDetail extends AbstractEntity {

  @Column(length = 500)
  String description;

  LocalDateTime createdAt;

  LocalDateTime publishedAt;

  LocalDateTime archivedAt;

  public CourseDetail(String description) {
    this.description = description;
    this.createdAt = LocalDateTime.now();
  }

  public void publish() {
    this.publishedAt = LocalDateTime.now();
  }

  public void archive() {
    this.archivedAt = LocalDateTime.now();
  }
}
