package tobyspring.splearn.domain.curriculum;

import static org.springframework.util.Assert.isTrue;
import static org.springframework.util.Assert.state;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import tobyspring.splearn.domain.AbstractEntity;
import tobyspring.splearn.domain.course.Course;

@Entity
@Getter
@ToString(callSuper = true, exclude = {"sections"})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Curriculum extends AbstractEntity {

  @OneToOne(optional = false, fetch = FetchType.LAZY)
  private Course course;

  @OneToMany(mappedBy = "curriculum", cascade = CascadeType.ALL, orphanRemoval = true)
  @Getter(AccessLevel.NONE)
  private List<Section> sections = new ArrayList<>();

  public List<Section> getSections() {
    return Collections.unmodifiableList(this.sections);
  }

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

  public void remoteLesson(int sectionIndex, int lessonIndex) {
    this.sections.get(sectionIndex).removeLesson(lessonIndex);
  }

  public List<Lesson> allLessons() {
    return this.getSections().stream()
        .flatMap(section -> section.getLessons().stream())
        .toList();
  }

  public void removeSection(int sectionIndex) {
    state(this.sections.size() > 1, "마지막 세션은 삭제할 수 없습니다");

    Section removed = this.sections.remove(sectionIndex);

    if (sectionIndex == 0) {
      Section next = this.sections.get(0);
      removed.moveAllLessonsTo(next, 0);
    } else {
      Section previous = this.sections.get(sectionIndex - 1);
      removed.moveAllLessonsTo(previous, previous.getLessons().size());
    }
  }

  public void moveLesson(
      int fromSectionIndex,
      int fromLessonIndex,
      int toSectionIndex,
      int toLessonIndex) {
    Section fromSection = this.sections.get(fromSectionIndex);
    Section toSection = this.sections.get(toSectionIndex);

    toSection.addLesson(toLessonIndex, fromSection.removeLesson(fromLessonIndex));
  }

  public void validate() {
    if (this.sections.isEmpty()) {
      throw new InvalidCurriculumException("최소한 하나의 섹션이 필요합니다");
    }

    this.sections.forEach(section -> {
      if (section.getLessons().isEmpty()) {
        throw new InvalidCurriculumException("수업이 없는 섹션은 허용되지 않습니다");
      }
    });
  }

  public Optional<Lesson> firstLesson() {
    return this.allLessons().stream().findFirst();
  }

  public Optional<Lesson> nextLesson(Lesson lesson) {
    List<Lesson> lessons = allLessons();

    int index = lessons.indexOf(lesson);

    isTrue(index >= 0, "커리큘럼에 포함된 수업이 아닙니다");

    if (index + 1 >= lessons.size()) {
      return Optional.empty();
    }

    return Optional.of(lessons.get(index + 1));
  }

  public Optional<Lesson> nextLesson(Long lessonId) {
    Lesson lesson = allLessons().stream()
        .filter(candidate -> lessonId.equals(candidate.getId()))
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("레슨을 찾을 수 없습니다. ID: " + lessonId));

    return nextLesson(lesson);
  }
}
