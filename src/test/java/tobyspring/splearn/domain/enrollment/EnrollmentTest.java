package tobyspring.splearn.domain.enrollment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import tobyspring.splearn.domain.course.Course;
import tobyspring.splearn.domain.course.CourseFixture;
import tobyspring.splearn.domain.member.Member;
import tobyspring.splearn.domain.member.MemberFixture;

class EnrollmentTest {

  @Test
  void enroll() {
    Member member = MemberFixture.createActiveMember();
    Course course = CourseFixture.createPublishedCourse();

    Enrollment enrollment = Enrollment.enroll(member, course);

    assertThat(enrollment.getStatus()).isEqualTo(EnrollmentStatus.ENROLLED);
    assertThat(enrollment.getEnrolledAt()).isNotNull();
  }

  @Test
  void enrollFailNotPublishedCourse() {
    Member member = MemberFixture.createActiveMember();
    Course course = CourseFixture.createCourse();

    assertThatThrownBy(() -> Enrollment.enroll(member, course))
        .isInstanceOf(IllegalStateException.class);
  }

  @Test
  void startStudying() {
    Enrollment enrollment = EnrollmentFixture.createEnrollment();

    enrollment.startStudying();

    assertThat(enrollment.getStatus()).isEqualTo(EnrollmentStatus.STUDYING);

    assertThatThrownBy(enrollment::startStudying).isInstanceOf(IllegalStateException.class);
  }

  @Test
  void complete() {
    Enrollment enrollment = EnrollmentFixture.createEnrollment();
    enrollment.startStudying();

    enrollment.complete();

    assertThat(enrollment.getStatus()).isEqualTo(EnrollmentStatus.COMPLETED);
    assertThat(enrollment.getCompletedAt()).isNotNull();

    assertThatThrownBy(enrollment::complete).isInstanceOf(IllegalStateException.class);
  }

}