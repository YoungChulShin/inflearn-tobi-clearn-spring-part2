package tobyspring.splearn.application.curriculum.provided;

import tobyspring.splearn.domain.curriculum.Curriculum;
import tobyspring.splearn.domain.curriculum.Lesson;

public interface CurriculumFinder {

  Curriculum find(Long curriculumId);

  Curriculum findByCourse(Long courseId);

  Lesson firstLesson(Long curriculumId);

  Lesson nextLesson(Long curriculumId, Long lessonId);
}
