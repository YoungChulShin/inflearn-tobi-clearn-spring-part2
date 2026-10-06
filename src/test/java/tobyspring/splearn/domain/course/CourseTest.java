package tobyspring.splearn.domain.course;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tobyspring.splearn.domain.instructor.Instructor;
import tobyspring.splearn.domain.instructor.InstructorFixture;

class CourseTest {

  Course course;

  @BeforeEach
  void setUp() {
    this.course = CourseFixture.createCourse();
    this.course.updateInfo(new CourseUpdateInfo(course.getTitle(), "Description"));
  }

  @Test
  void create() {
    Instructor instructor = InstructorFixture.createActiveInstructor();

    Course course = new Course(instructor, "Clean Spring 2", "Description");

    assertThat(course.getInstructor()).isEqualTo(instructor);
    assertThat(course.getTitle()).isEqualTo("Clean Spring 2");
    assertThat(course.getStatus()).isEqualTo(CourseStatus.DRAFT);
    assertThat(course.getDetail().getDescription()).isEqualTo("Description");
    assertThat(course.getDetail().getCreatedAt()).isNotNull();
  }

  @Test
  void createFailNotActiveInstructor() {
    Instructor instructor = InstructorFixture.createInstructor();

    Assertions.assertThatThrownBy(() -> new Course(instructor, "Clean Spring 2", "Description"))
        .isInstanceOf(IllegalStateException.class);
  }

  @Test
  void submitForReview() {
    course.submitForReview();

    assertThat(course.getStatus()).isEqualTo(CourseStatus.IN_REVIEW);

    assertThatThrownBy(() -> course.submitForReview())
        .isInstanceOf(IllegalStateException.class);
  }

  @Test
  void submitForReviewFail() {
    Instructor instructor = InstructorFixture.createActiveInstructor();
    Course course = new Course(instructor, "Clean Spring 2", null);

    assertThatThrownBy(course::submitForReview)
        .isInstanceOf(IllegalStateException.class);
  }

  @Test
  void publish() {
    course.submitForReview();

    course.publish();

    assertThat(course.getStatus()).isEqualTo(CourseStatus.PUBLISHED);
    assertThat(course.getDetail().getPublishedAt()).isNotNull();

    assertThatThrownBy(() -> course.publish())
        .isInstanceOf(IllegalStateException.class);
  }

  @Test
  void archive() {
    course.submitForReview();
    course.publish();

    course.archive();

    assertThat(course.getStatus()).isEqualTo(CourseStatus.ARCHIVED);
    assertThat(course.getDetail().getArchivedAt()).isNotNull();

    assertThatThrownBy(() -> course.archive())
        .isInstanceOf(IllegalStateException.class);
  }

  @Test
  void updateInfo() {
    course.updateInfo(new CourseUpdateInfo("Clean Spring 3", "Updated Description"));

    assertThat(course.getTitle()).isEqualTo("Clean Spring 3");
    assertThat(course.getDetail().getDescription()).isEqualTo("Updated Description");
  }
}