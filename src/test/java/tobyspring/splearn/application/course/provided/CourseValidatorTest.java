package tobyspring.splearn.application.course.provided;

import lombok.RequiredArgsConstructor;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import tobyspring.splearn.application.course.required.CourseRepository;
import tobyspring.splearn.domain.course.CourseFixture;
import tobyspring.splearn.domain.instructor.Instructor;
import tobyspring.splearn.domain.instructor.InstructorFixture;
import tobyspring.splearn.domain.member.MemberFixture;
import tobyspring.splearn.support.exception.ValidationException;
import tobyspring.splearn.support.stereotype.ApplicationServiceTest;
import tobyspring.splearn.support.test.BaseApplicationServiceTest;

@ApplicationServiceTest
@RequiredArgsConstructor
class CourseValidatorTest extends BaseApplicationServiceTest {

  final CourseValidator courseValidator;
  final CourseRepository courseRepository;

  @Test
  void titleDuplication() {
    var instructor1 = prepareInstructor();

    Instructor instructor2 = prepareInstructor();

    var course1 = courseRepository.save(CourseFixture.createCourse(instructor1, "Clean Spring"));
    var course2 = courseRepository.save(CourseFixture.createCourse(instructor2, "Clean Code"));

    courseValidator.validateForCreate(instructor1, new CourseCreateRequest(instructor1.getId(), "Spring 7", null));

    Assertions.assertThatThrownBy(
        () -> courseValidator.validateForCreate(instructor1, new CourseCreateRequest(instructor1.getId(), "Clean Spring", null))
        ).isInstanceOfSatisfying(ValidationException.class, e -> {
          Assertions.assertThat(e.getErrors()).hasSize(1);
    });

    courseValidator.validateForCreate(instructor2, new CourseCreateRequest(instructor2.getId(), "Clean Spring", null));
  }

}