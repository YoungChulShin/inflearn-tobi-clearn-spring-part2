package tobyspring.splearn.application.curriculum.provided;

import java.util.Optional;
import tobyspring.splearn.domain.curriculum.Curriculum;
import tobyspring.splearn.domain.curriculum.Lesson;

public interface CurriculumFinder {

  Curriculum find(Long curriculumId);

  Curriculum findWithSections(Long curriculumId);

  Curriculum findByCourse(Long courseId);

  Optional<Lesson> firstLesson(Long curriculumId);

  Optional<Lesson> nextLesson(Long curriculumId, Long lessonId);
}
