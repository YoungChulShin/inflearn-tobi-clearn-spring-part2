package tobyspring.splearn.application.curriculum.required;

import org.springframework.data.repository.Repository;
import tobyspring.splearn.domain.curriculum.Section;

public interface SectionRepository extends Repository<Section, Long> {

  void delete(Section section);

}
