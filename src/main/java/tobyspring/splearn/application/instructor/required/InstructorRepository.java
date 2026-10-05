package tobyspring.splearn.application.instructor.required;

import java.util.Optional;
import org.springframework.data.repository.Repository;
import tobyspring.splearn.domain.instructor.Instructor;

public interface InstructorRepository extends Repository<Instructor, Long> {

  Instructor save(Instructor instructor);
  
  Optional<Instructor> findById(Long instructorId);

  Optional<Instructor> findByMemberId(Long memberId);
}
