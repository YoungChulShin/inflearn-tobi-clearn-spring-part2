package tobyspring.splearn.application.course.provided;


import static org.assertj.core.api.Assertions.assertThat;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import tobyspring.splearn.domain.course.Course;
import tobyspring.splearn.domain.course.CourseFixture;
import tobyspring.splearn.support.stereotype.ApplicationServiceTest;
import tobyspring.splearn.support.test.BaseApplicationServiceTest;

@ApplicationServiceTest
@RequiredArgsConstructor
class CourseCreatorTest extends BaseApplicationServiceTest {

  final CourseCreator courseCreator;

  @Test
  void create() {
    prepareInstructor();

    Course course = courseCreator.create(CourseFixture.createCourseCreateRequest(instructor.getId(), null));

    assertThat(course.getId()).isNotNull();
  }

  @Test
  void updateInfo() {
    prepareInstructor();
    Course course = courseCreator.create(CourseFixture.createCourseCreateRequest(instructor.getId(), null));

    Course updated = courseCreator.updateInfo(course.getId(),
        CourseFixture.createCourseInfoUpdateRequest("Updated"));

    assertThat(updated.getTitle()).isEqualTo("Updated");

  }

}