package tobyspring.splearn.domain.curriculum;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.junit.jupiter.api.Test;
import tobyspring.splearn.domain.course.CourseFixture;

class CurriculumTest {

  @Test
  void create() {
    var course = CourseFixture.createCourse();

    Curriculum curriculum = new Curriculum(course);

    assertThat(curriculum.getCourse()).isEqualTo(course);

    assertThatThrownBy(() -> new Curriculum(null))
        .isInstanceOf(NullPointerException.class);
  }

  @Test
  void addSection() {
    Curriculum curriculum = CurriculumFixture.createCurriculum();

    Section section1 = curriculum.addSection("Section 1");

    assertThat(curriculum.getSections()).containsExactly(section1);
    assertThat(curriculum.getSections()).extracting(Section::getTitle).containsExactly("Section 1");

    assertThatThrownBy(() -> curriculum.addSection(null))
        .isInstanceOf(NullPointerException.class);
  }

  @Test
  void addSectionWithIndex() {
    Curriculum curriculum = CurriculumFixture.createCurriculum();

    Section s1 = curriculum.addSection("S1");
    Section s2 = curriculum.addSection("S2");

    assertThat(curriculum.getSections()).containsExactly(s1, s2);

    Section s1_1 = curriculum.addSection(1, "S1_1");

    assertThat(curriculum.getSections()).containsExactly(s1, s1_1, s2);

    Section s3 = curriculum.addSection(3, "S3");

    assertThat(curriculum.getSections()).containsExactly(s1, s1_1, s2, s3);

    assertThatThrownBy(() -> curriculum.addSection(5, "Fail"))
        .isInstanceOf(IndexOutOfBoundsException.class);
  }

  @Test
  void addLesson() {
    Curriculum curriculum = CurriculumFixture.createCurriculum();
    Section s0 = curriculum.addSection("S0");
    Section s1 = curriculum.addSection("S1");

    Lesson l0 = curriculum.addLesson(0, "L0");
    Lesson l1 = curriculum.addLesson(0, "L1");

    assertThat(s0.getLessons()).containsExactly(l0, l1);

    Lesson l2 = curriculum.addLesson(1, "L2");
    assertThat(s1.getLessons()).containsExactly(l2);
  }

  @Test
  void updateSectionTitle() {
    Curriculum curriculum = CurriculumFixture.createCurriculum();
    Section s0 = curriculum.addSection("S0");
    Section s1 = curriculum.addSection("S1");

    curriculum.updateSectionTitle(0, "S0 Updated");

    assertThat(s0.getTitle()).isEqualTo("S0 Updated");

    curriculum.updateSectionTitle(1, "S1 Updated");

    assertThat(curriculum.getSections())
        .extracting(Section::getTitle)
        .containsExactly("S0 Updated", "S1 Updated");
  }

  @Test
  void updateLessonTitle() {
    Curriculum curriculum = CurriculumFixture.createCurriculum();
    Section s0 = curriculum.addSection("S0");
    Section s1 = curriculum.addSection("S1");
    Lesson l0_0 = curriculum.addLesson(0, "L0_0");
    Lesson l0_1 = curriculum.addLesson(0, "L0_1");
    Lesson l1_0 = curriculum.addLesson(1, "L1_0");

    curriculum.updateLessonTitle(0, 0, "L0_0 Updated");

    assertThat(l0_0.getTitle()).isEqualTo("L0_0 Updated");

    curriculum.updateLessonTitle(1, 0, "L1_0 Updated");

    assertThat(l1_0.getTitle()).isEqualTo("L1_0 Updated");

    List<Lesson> lessons = curriculum.getSections().stream()
        .flatMap(section -> section.getLessons().stream())
        .toList();

    assertThat(lessons).extracting(Lesson::getTitle)
        .containsExactly("L0_0 Updated", "L0_1", "L1_0 Updated");

  }
}