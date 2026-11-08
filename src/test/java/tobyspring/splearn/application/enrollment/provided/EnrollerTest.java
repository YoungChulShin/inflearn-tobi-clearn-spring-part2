package tobyspring.splearn.application.enrollment.provided;

import static org.assertj.core.api.Assertions.assertThat;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import tobyspring.splearn.domain.course.Course;
import tobyspring.splearn.domain.enrollment.Enrollment;
import tobyspring.splearn.domain.enrollment.EnrollmentStatus;
import tobyspring.splearn.domain.member.Member;
import tobyspring.splearn.support.stereotype.ApplicationServiceTest;
import tobyspring.splearn.support.test.BaseApplicationServiceTest;

@ApplicationServiceTest
@RequiredArgsConstructor
class EnrollerTest extends BaseApplicationServiceTest {

  final Enroller enroller;

  @Test
  void enroll() {
    Member member = prepareActiveMember();
    Course course = preparePublishedCourse();

    Enrollment enrollment = enroller.enroll(new EnrollRequest(member.getId(), course.getId()));

    assertThat(enrollment.getId()).isNotNull();
  }

  @Test
  void startStudying() {
    prepareEnrollment();

    Enrollment enrollmentStudying = enroller.startStudying(enrollment.getId());

    assertThat(enrollmentStudying.getStatus()).isEqualTo(EnrollmentStatus.STUDYING);
  }

  @Test
  void complete() {
    prepareEnrollment();
    enroller.startStudying(enrollment.getId());

    Enrollment enrollmentComplete = enroller.complete(enrollment.getId());

    assertThat(enrollmentComplete.getStatus()).isEqualTo(EnrollmentStatus.COMPLETED);
  }

}