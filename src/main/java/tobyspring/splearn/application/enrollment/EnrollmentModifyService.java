package tobyspring.splearn.application.enrollment;

import lombok.RequiredArgsConstructor;
import tobyspring.splearn.application.course.provided.CourseFinder;
import tobyspring.splearn.application.enrollment.provided.EnrollRequest;
import tobyspring.splearn.application.enrollment.provided.Enroller;
import tobyspring.splearn.application.enrollment.provided.EnrollmentFinder;
import tobyspring.splearn.application.enrollment.required.EnrollmentRepository;
import tobyspring.splearn.application.member.provided.MemberFinder;
import tobyspring.splearn.domain.course.Course;
import tobyspring.splearn.domain.enrollment.Enrollment;
import tobyspring.splearn.domain.member.Member;
import tobyspring.splearn.support.stereotype.ValidatedApplicationService;

@ValidatedApplicationService
@RequiredArgsConstructor
public class EnrollmentModifyService implements Enroller {

  private final EnrollmentRepository enrollmentRepository;
  private final EnrollmentFinder enrollmentFinder;
  private final MemberFinder memberFinder;
  private final CourseFinder courseFinder;

  @Override
  public Enrollment enroll(EnrollRequest enrollRequest) {
    Member member = memberFinder.find(enrollRequest.memberId());
    Course course = courseFinder.find(enrollRequest.courseId());

    checkDuplication(member, course);

    Enrollment enrollment = Enrollment.enroll(member, course);

    return enrollmentRepository.save(enrollment);
  }

  private void checkDuplication(Member member, Course course) {
    if (enrollmentRepository.findByMemberIdAndCourseId(member.getId(), course.getId()).isPresent()) {
      throw new IllegalArgumentException("이미 수강중인 강의입니다");
    }
  }

  @Override
  public Enrollment startStudying(Long enrollmentId) {
    Enrollment enrollment = enrollmentFinder.find(enrollmentId);

    enrollment.startStudying();

    return enrollmentRepository.save(enrollment);
  }

  @Override
  public Enrollment complete(Long enrollmentId) {
    Enrollment enrollment = enrollmentFinder.find(enrollmentId);

    enrollment.complete();

    return enrollmentRepository.save(enrollment);
  }
}
