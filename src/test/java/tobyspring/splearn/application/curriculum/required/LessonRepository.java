package tobyspring.splearn.application.curriculum.required;

import org.springframework.data.repository.Repository;
import tobyspring.splearn.domain.curriculum.Lesson;

public interface LessonRepository extends Repository<Lesson, Long> {

  void delete(Lesson lesson);

}
