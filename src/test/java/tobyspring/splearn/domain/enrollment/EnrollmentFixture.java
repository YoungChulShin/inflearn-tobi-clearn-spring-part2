package tobyspring.splearn.domain.enrollment;

import static tobyspring.splearn.domain.course.CourseFixture.createPublishedCourse;
import static tobyspring.splearn.domain.member.MemberFixture.createActiveMember;

import org.springframework.lang.Nullable;
import tobyspring.splearn.domain.course.Course;
import tobyspring.splearn.domain.member.Member;

public class EnrollmentFixture {

  public static Enrollment createEnrollment(@Nullable Member member, @Nullable Course course) {
    return Enrollment.enroll(
        member == null ? createActiveMember() : member,
        course == null ? createPublishedCourse() : course
    );
  }

  public static Enrollment createEnrollment() {
    return createEnrollment(null, null);
  }
}
