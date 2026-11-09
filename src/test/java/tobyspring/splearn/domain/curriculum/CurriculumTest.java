package tobyspring.splearn.domain.curriculum;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static tobyspring.splearn.domain.curriculum.LessonContent.*;
import static tobyspring.splearn.domain.curriculum.SectionContent.section;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
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

    List<Lesson> lessons = curriculum.allLessons();

    assertThat(lessons).extracting(Lesson::getTitle)
        .containsExactly("L0_0 Updated", "L0_1", "L1_0 Updated");

  }

  @Test
  void removeLesson() {
    Curriculum curriculum = CurriculumFixture.createCurriculum();
    Section s0 = curriculum.addSection("S0");
    Section s1 = curriculum.addSection("S1");
    Lesson l0_0 = curriculum.addLesson(0, "L0_0");
    Lesson l0_1 = curriculum.addLesson(0, "L0_1");
    Lesson l1_0 = curriculum.addLesson(1, "L1_0");
    Lesson l1_1 = curriculum.addLesson(1, "L1_1");

    assertThat(curriculum.allLessons()).extracting(Lesson::getTitle)
            .containsExactly("L0_0", "L0_1", "L1_0", "L1_1");

    curriculum.remoteLesson(0, 0);

    assertThat(curriculum.allLessons()).extracting(Lesson::getTitle)
        .containsExactly("L0_1", "L1_0", "L1_1");

    curriculum.remoteLesson(1, 1);

    assertThat(curriculum.allLessons()).extracting(Lesson::getTitle)
        .containsExactly("L0_1", "L1_0");
  }

  @Test
  void removeSection() {
    Curriculum curriculum = CurriculumFixture.createCurriculum();
    Section s0 = curriculum.addSection("S0");
    Section s1 = curriculum.addSection("S1");
    Section s2 = curriculum.addSection("S2");
    Lesson l0_0 = curriculum.addLesson(0, "L0_0");
    Lesson l0_1 = curriculum.addLesson(0, "L0_1");
    Lesson l1_0 = curriculum.addLesson(1, "L1_0");
    Lesson l1_1 = curriculum.addLesson(1, "L1_1");
    Lesson l2_0 = curriculum.addLesson(2, "L2_0");
    Lesson l2_1 = curriculum.addLesson(2, "L2_1");

    assertThat(SectionContent.from(curriculum)).containsExactly(
        section("S0", lesson("L0_0"), lesson("L0_1")),
        section("S1", lesson("L1_0"), lesson("L1_1")),
        section("S2", lesson("L2_0"), lesson("L2_1"))
    );

    curriculum.removeSection(2);

    assertThat(curriculum.getSections()).containsExactly(s0, s1);
    assertThat(SectionContent.from(curriculum)).containsExactly(
        section("S0", lesson("L0_0"), lesson("L0_1")),
        section("S1", lesson("L1_0"), lesson("L1_1"), lesson("L2_0"), lesson("L2_1")));

    curriculum.removeSection(0);

    assertThat(curriculum.getSections()).containsExactly(s1);
    assertThat(SectionContent.from(curriculum)).containsExactly(
        section("S1",
            lesson("L0_0"), lesson("L0_1"), lesson("L1_0"),
            lesson("L1_1"), lesson("L2_0"), lesson("L2_1")));

    assertThatThrownBy(() -> curriculum.removeSection(0))
        .isInstanceOf(IllegalStateException.class);
  }

  @Test
  void moveLesson() {
    Curriculum curriculum = CurriculumFixture.createCurriculum();
    Section s0 = curriculum.addSection("S0");
    Section s1 = curriculum.addSection("S1");
    Section s2 = curriculum.addSection("S2");
    Lesson l0_0 = curriculum.addLesson(0, "L0_0");
    Lesson l0_1 = curriculum.addLesson(0, "L0_1");
    Lesson l0_2 = curriculum.addLesson(0, "L0_2");
    Lesson l1_0 = curriculum.addLesson(1, "L1_0");
    Lesson l1_1 = curriculum.addLesson(1, "L1_1");
    Lesson l2_0 = curriculum.addLesson(2, "L2_0");
    Lesson l2_1 = curriculum.addLesson(2, "L2_1");

    assertThat(SectionContent.from(curriculum)).containsExactly(
        section("S0", lesson("L0_0"), lesson("L0_1"), lesson("L0_2")),
        section("S1", lesson("L1_0"), lesson("L1_1")),
        section("S2", lesson("L2_0"), lesson("L2_1"))
    );

    curriculum.moveLesson(0, 0, 0, 1);

    assertThat(SectionContent.from(curriculum)).containsExactly(
        section("S0", lesson("L0_1"), lesson("L0_0"), lesson("L0_2")),
        section("S1", lesson("L1_0"), lesson("L1_1")),
        section("S2", lesson("L2_0"), lesson("L2_1"))
    );

    curriculum.moveLesson(0, 2, 0, 0);

    assertThat(SectionContent.from(curriculum)).containsExactly(
        section("S0", lesson("L0_2"), lesson("L0_1"), lesson("L0_0")),
        section("S1", lesson("L1_0"), lesson("L1_1")),
        section("S2", lesson("L2_0"), lesson("L2_1"))
    );

    curriculum.moveLesson(0, 1, 1, 2);

    assertThat(SectionContent.from(curriculum)).containsExactly(
        section("S0", lesson("L0_2"), lesson("L0_0")),
        section("S1", lesson("L1_0"), lesson("L1_1"), lesson("L0_1")),
        section("S2", lesson("L2_0"), lesson("L2_1"))
    );
    assertThat(l0_1.getSection()).isEqualTo(s1);

    curriculum.moveLesson(2, 1, 0, 1);

    assertThat(SectionContent.from(curriculum)).containsExactly(
        section("S0", lesson("L0_2"), lesson("L2_1"), lesson("L0_0")),
        section("S1", lesson("L1_0"), lesson("L1_1"), lesson("L0_1")),
        section("S2", lesson("L2_0"))
    );
  }

  @Test
  void validate() {
    Curriculum curriculum = CurriculumFixture.createCurriculum();

    assertThatThrownBy(curriculum::validate)
        .isInstanceOf(InvalidCurriculumException.class);

    curriculum.addSection("S0");
    curriculum.addLesson(0, "L0_0");

    curriculum.validate();

    curriculum.addSection("S1");
    curriculum.addLesson(1, "L0_1");

    curriculum.validate();

    curriculum.remoteLesson(1, 0);
    assertThatThrownBy(curriculum::validate)
        .isInstanceOf(InvalidCurriculumException.class);
  }

  @Test
  void firstLesson() {
    Curriculum curriculum = CurriculumFixture.createCurriculum();
    Section s0 = curriculum.addSection("S0");
    Section s1 = curriculum.addSection("S1");
    Section s2 = curriculum.addSection("S2");

    assertThat(curriculum.firstLesson()).isEmpty();

    Lesson l0_0 = curriculum.addLesson(0, "L0_0");
    Lesson l0_1 = curriculum.addLesson(0, "L0_1");
    Lesson l0_2 = curriculum.addLesson(0, "L0_2");
    Lesson l1_0 = curriculum.addLesson(1, "L1_0");
    Lesson l1_1 = curriculum.addLesson(1, "L1_1");
    Lesson l2_0 = curriculum.addLesson(2, "L2_0");
    Lesson l2_1 = curriculum.addLesson(2, "L2_1");

    assertThat(curriculum.firstLesson().orElseThrow()).isEqualTo(l0_0);
  }

  @Test
  void nextLesson() {
    Curriculum curriculum = CurriculumFixture.createCurriculum();
    Section s0 = curriculum.addSection("S0");
    Section s1 = curriculum.addSection("S1");
    Lesson l0_0 = curriculum.addLesson(0, "L0_0");
    Lesson l0_1 = curriculum.addLesson(0, "L0_1");
    Lesson l1_0 = curriculum.addLesson(1, "L1_0");

    Lesson lesson = curriculum.firstLesson().orElseThrow();

    lesson = curriculum.nextLesson(lesson).orElseThrow();
    assertThat(lesson).isEqualTo(l0_1);

    lesson = curriculum.nextLesson(lesson).orElseThrow();
    assertThat(lesson).isEqualTo(l1_0);

    assertThat(curriculum.nextLesson(lesson)).isEmpty();
  }

  @Test
  void nextLessonWithId() {
    Curriculum curriculum = CurriculumFixture.createCurriculum();
    Section s0 = curriculum.addSection("S0");
    Section s1 = curriculum.addSection("S1");
    Lesson l0_0 = curriculum.addLesson(0, "L0_0");
    assignId(l0_0, 10L);
    Lesson l0_1 = curriculum.addLesson(0, "L0_1");
    assignId(l0_1, 11L);
    Lesson l1_0 = curriculum.addLesson(1, "L1_0");
    assignId(l1_0, 12L);

    // 13:07
    Lesson lesson = curriculum.nextLesson(10L).orElseThrow();
    System.out.println(lesson);
//    assertThat(lesson).isEqualTo(l0_0);
  }

  private void assignId(Lesson lesson, Long id) {
    ReflectionTestUtils.setField(lesson, "id", id);
  }
}