package tobyspring.splearn.application.enrollment.provided;

import tobyspring.splearn.domain.enrollment.Enrollment;

/**
 * 수강 신청과 관리를 담당
 */
public interface Enroller {

  Enrollment enroll(EnrollRequest enrollRequest);

  Enrollment startStudying(Long enrollmentId);

  Enrollment complete(Long enrollmentId);
}
