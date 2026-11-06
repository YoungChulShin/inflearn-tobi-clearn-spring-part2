package tobyspring.splearn.application.course.provided;

import static org.assertj.core.api.Assertions.assertThat;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tobyspring.splearn.domain.course.CourseStatus;
import tobyspring.splearn.support.stereotype.ApplicationServiceTest;
import tobyspring.splearn.support.test.BaseApplicationServiceTest;

@ApplicationServiceTest
@RequiredArgsConstructor
class CoursePublisherTest extends BaseApplicationServiceTest {

  final CoursePublisher coursePublisher;


  @BeforeEach
  void setup() {
    prepareCourse();
  }

  @Test
  void submitForReview() {
    var courseForReview = coursePublisher.submitForReview(course.getId());

    assertThat(courseForReview.getStatus()).isEqualTo(CourseStatus.IN_REVIEW);

  }

  @Test
  void publish() {
    var courseForPublish = coursePublisher.submitForReview(course.getId());
    courseForPublish.publish();

    assertThat(courseForPublish.getStatus()).isEqualTo(CourseStatus.PUBLISHED);
  }

  @Test
  void archive() {
    var courseForArchive = coursePublisher.submitForReview(course.getId());
    courseForArchive.publish();
    courseForArchive.archive();

    assertThat(courseForArchive.getStatus()).isEqualTo(CourseStatus.ARCHIVED);
  }


}