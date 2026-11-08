package tobyspring.splearn.application.course.required;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import tobyspring.splearn.domain.course.Course;
import tobyspring.splearn.domain.course.CourseFixture;
import tobyspring.splearn.support.test.BaseRepositoryTest;

@DataJpaTest
@RequiredArgsConstructor
class CourseRepositoryTest extends BaseRepositoryTest {

  final CourseRepository courseRepository;

  @BeforeEach
  void setUp() {
    member = prepareActiveMember();
    instructor = prepareActiveInstructor(member);
  }

  @Test
  void saveAndFindId() {
    Course course = CourseFixture.createCourse(instructor, null);
    course = courseRepository.save(course);

    AssertionsForClassTypes.assertThat(course.getId()).isNotNull();

    entityManager.flush();
    entityManager.clear();

    var found = courseRepository.findById(course.getId()).orElseThrow();

    AssertionsForClassTypes.assertThat(found).isEqualTo(course);
  }

  @Test
  void findByTitleContaining() {
    List<Long> ids = Stream.of(
        prepareCourse(instructor, "Hello Spring"),
        prepareCourse(instructor, "Clean Spring 2"),
        prepareCourse(instructor, "Clean Code")
    ).map(Course::getId).toList();

    AssertionsForClassTypes.assertThat(courseRepository.findByTitleContaining("Spring")
        .stream()
        .map(Course::getId).toList())
        .isEqualTo(List.of(ids.get(0), ids.get(1)));

    AssertionsForClassTypes.assertThat(courseRepository.findByTitleContaining("Clean")
        .stream()
        .map(Course::getId).toList())
        .isEqualTo(List.of(ids.get(1), ids.get(2)));

    AssertionsForClassTypes.assertThat(courseRepository.findByTitleContaining("Code")
        .stream()
        .map(Course::getId).toList())
        .isEqualTo(List.of(ids.get(2)));

    AssertionsForClassTypes.assertThat(courseRepository.findByTitleContaining("JPA")
        .stream()
        .map(Course::getId).toList())
        .isEqualTo(Collections.emptyList());
  }

  @Test
  void findByInstructor() {
    var instructor1 = prepareActiveInstructor();
    var instructor2 = prepareActiveInstructor();

    var course = prepareCourse(instructor1, null);
    var course2 = prepareCourse(instructor2, null);

    List<Course> courses = courseRepository.findByInstructorId(instructor1.getId());
    assertThat(courses).singleElement().isEqualTo(course);

    List<Course> courses2 = courseRepository.findByInstructorId(instructor2.getId());
    assertThat(courses2).singleElement().isEqualTo(course2);

    List<Course> courses2_1 = courseRepository.findByInstructor(instructor2);
    assertThat(courses2_1).singleElement().isEqualTo(course2);
  }

  @Test
  void uniqueTitleAndInstructor() {
    prepareCourse(instructor, "Title");

    Assertions.assertThatThrownBy(() -> courseRepository.save(CourseFixture.createCourse(instructor, "Title")))
        .isInstanceOf(DataIntegrityViolationException.class);

  }
}