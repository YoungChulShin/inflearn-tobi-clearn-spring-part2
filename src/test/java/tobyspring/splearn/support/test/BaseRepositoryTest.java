package tobyspring.splearn.support.test;

import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import tobyspring.splearn.application.course.required.CourseRepository;
import tobyspring.splearn.application.enrollment.required.EnrollmentRepository;
import tobyspring.splearn.application.instructor.required.InstructorRepository;
import tobyspring.splearn.application.member.required.MemberRepository;
import tobyspring.splearn.domain.course.Course;
import tobyspring.splearn.domain.course.CourseFixture;
import tobyspring.splearn.domain.enrollment.Enrollment;
import tobyspring.splearn.domain.enrollment.EnrollmentFixture;
import tobyspring.splearn.domain.instructor.Instructor;
import tobyspring.splearn.domain.instructor.InstructorFixture;
import tobyspring.splearn.domain.member.Member;
import tobyspring.splearn.domain.member.MemberFixture;

@DataJpaTest
public class BaseRepositoryTest {

  @Autowired
  protected EntityManager entityManager;

  @Autowired
  MemberRepository memberRepository;

  @Autowired
  InstructorRepository instructorRepository;

  @Autowired
  EnrollmentRepository enrollmentRepository;

  @Autowired
  CourseRepository courseRepository;

  protected Member member;

  protected Instructor instructor;

  protected Course course;

  protected Enrollment enrollment;

  protected Course preparePublishedCourse() {
    prepareActiveInstructor();

    this.course = courseRepository.save(CourseFixture.createCourse(this.instructor, null));
    this.course.updateInfo(CourseFixture.createCourseInfoUpdateRequest(null).toInfo());
    this.course.submitForReview();
    this.course.publish();

    return course;
  }

  protected Instructor prepareActiveInstructor() {
    prepareActiveMember();

    this.instructor = instructorRepository.save(InstructorFixture.createActiveInstructor(this.member));

    return instructor;
  }

  protected Member prepareActiveMember() {
    this.member = memberRepository.save(MemberFixture.createActiveMember());

    return member;
  }

  protected Enrollment prepareEnrollment(Member member, Course course) {
    this.enrollment = enrollmentRepository.save(EnrollmentFixture.createEnrollment(member, course));

    return this.enrollment;
  }
}
