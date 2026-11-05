package tobyspring.splearn.application.course.required;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import jakarta.persistence.EntityManager;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import tobyspring.splearn.application.instructor.required.InstructorRepository;
import tobyspring.splearn.application.member.required.MemberRepository;
import tobyspring.splearn.domain.course.Course;
import tobyspring.splearn.domain.course.CourseFixture;
import tobyspring.splearn.domain.instructor.Instructor;
import tobyspring.splearn.domain.instructor.InstructorFixture;
import tobyspring.splearn.domain.member.Member;
import tobyspring.splearn.domain.member.MemberFixture;

@DataJpaTest
@RequiredArgsConstructor
class CourseRepositoryTest {

  final CourseRepository courseRepository;
  final EntityManager entityManager;
  final MemberRepository memberRepository;
  final InstructorRepository instructorRepository;

  Member member;
  Instructor instructor;

  @BeforeEach
  void setUp() {
    member = memberRepository.save(MemberFixture.createActiveMember());
    instructor = instructorRepository.save(InstructorFixture.createActiveInstructor(member));
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
        CourseFixture.createCourse(instructor, "Hello Spring"),
        CourseFixture.createCourse(instructor, "Clean Spring 2"),
        CourseFixture.createCourse(instructor, "Clean Code")
    ).map(course -> courseRepository.save(course).getId()).toList();

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
    var member2 = memberRepository.save(MemberFixture.createActiveMember());
    var instructor2 = instructorRepository.save(InstructorFixture.createActiveInstructor(member2));

    var course = courseRepository.save(CourseFixture.createCourse(instructor, "Title"));
    var course2 = courseRepository.save(CourseFixture.createCourse(instructor2, "Title2"));

    List<Course> courses = courseRepository.findByInstructorId(instructor.getId());
    assertThat(courses).singleElement().isEqualTo(course);

    List<Course> courses2 = courseRepository.findByInstructorId(instructor2.getId());
    assertThat(courses2).singleElement().isEqualTo(course2);

    List<Course> courses2_1 = courseRepository.findByInstructor(instructor2);
    assertThat(courses2_1).singleElement().isEqualTo(course2);
  }

  @Test
  void uniqueTitleAndInstructor() {
    courseRepository.save(CourseFixture.createCourse(instructor, "Title"));

    Assertions.assertThatThrownBy(() -> courseRepository.save(CourseFixture.createCourse(instructor, "Title")))
        .isInstanceOf(DataIntegrityViolationException.class);

  }
}