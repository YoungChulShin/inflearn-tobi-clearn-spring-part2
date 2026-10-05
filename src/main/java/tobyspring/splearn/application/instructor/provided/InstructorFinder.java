package tobyspring.splearn.application.instructor.provided;

import java.util.Optional;
import tobyspring.splearn.domain.instructor.Instructor;
import tobyspring.splearn.domain.member.Member;

/**
 * 강사 조회
 */
public interface InstructorFinder {

  Instructor find(Long instructorId);

  Optional<Instructor> findByMember(Long memberId);

  default Optional<Instructor> findByMember(Member member) {
    return findByMember(member.getId());
  }
}
