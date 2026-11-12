package tobyspring.splearn.application.curriculum.required;

import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.Repository;
import tobyspring.splearn.domain.curriculum.Curriculum;

public interface CurriculumRepository extends Repository<Curriculum, Long> {

  Curriculum save(Curriculum curriculum);
  
  Optional<Curriculum> findById(Long curriculumId);

  @EntityGraph(attributePaths = {"sections", "sections.lessons"})
  Optional<Curriculum> findWithSectionsById(Long curriculumId);

  Optional<Curriculum> findByCourseId(Long courseId);
}
