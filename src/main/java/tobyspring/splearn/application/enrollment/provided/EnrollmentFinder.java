package tobyspring.splearn.application.enrollment.provided;

import java.util.List;
import java.util.Optional;
import tobyspring.splearn.domain.enrollment.Enrollment;

public interface EnrollmentFinder {

  Enrollment find(Long enrollmentId);

  List<Enrollment> findByMember(Long memberId);

  Optional<Enrollment> findByMemberAndCourse(Long memberId, Long courseId);

}
