package tobyspring.splearn.domain.curriculum;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import tobyspring.splearn.domain.AbstractEntity;
import tobyspring.splearn.domain.course.Course;

@Entity
@Getter
@ToString(callSuper = true, exclude = {})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Curriculum extends AbstractEntity {

  @OneToOne(optional = false, fetch = FetchType.LAZY)
  private Course course;

  @OneToMany(mappedBy = "curriculum", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Section> sections = new ArrayList<>();

  public Curriculum(Course course) {
    this.course = Objects.requireNonNull(course);
  }

  public Section addSection(String title) {
    Section section = new Section(this, title);

    this.sections.add(section);

    return section;
  }

  public Section addSection(int index, String title) {
    Section section = new Section(this, title);

    this.sections.add(index, section);

    return section;
  }

  public Lesson addLesson(int sectionIndex, String title) {
    Lesson lesson = this.sections.get(sectionIndex).addLesson(title);

    return lesson;
  }

  public Section updateSectionTitle(int sectionIndex, String title) {
    Section section = this.sections.get(sectionIndex);

    section.updateTitle(title);

    return section;
  }

  public void updateLessonTitle(int sectionIndex, int lessonIndex, String title) {
    this.sections.get(sectionIndex).updateLessonTitle(lessonIndex, title);
  }
}
