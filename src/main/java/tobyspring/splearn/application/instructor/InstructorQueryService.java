package tobyspring.splearn.application.instructor;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tobyspring.splearn.application.instructor.provided.InstructorFinder;
import tobyspring.splearn.application.instructor.required.InstructorRepository;
import tobyspring.splearn.domain.instructor.Instructor;

@Service
@Transactional
@RequiredArgsConstructor
public class InstructorQueryService implements InstructorFinder {

  private final InstructorRepository instructorRepository;

  @Override
  public Instructor find(Long instructorId) {
    return instructorRepository.findById(instructorId)
        .orElseThrow(() -> new IllegalArgumentException("강사를 찾을 수 없습니다. ID: " + instructorId));
  }

  @Override
  public Optional<Instructor> findByMember(Long memberId) {
    return instructorRepository.findByMemberId(memberId);
  }
}
