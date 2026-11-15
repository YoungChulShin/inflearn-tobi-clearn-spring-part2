package tobyspring.splearn.application.curriculum.provided;

import tobyspring.splearn.application.course.required.CurriculmCreator;
import tobyspring.splearn.application.course.required.CurriculumValidator;
import tobyspring.splearn.domain.curriculum.Curriculum;

public interface CurriculumCoordinator extends CurriculmCreator, CurriculumValidator {

  Curriculum addSection(Long curriculumId, String title);

  Curriculum addSection(Long curriculumId, int sectionIndex, String title);

  Curriculum addLesson(Long curriculumId, int sectionIndex, String title);

  Curriculum updateSectionTitle(Long curriculumId, int sectionIndex, String title);

  Curriculum updateLessonTitle(Long curriculumId, int sectionIndex, int lessonIndex, String title);

  Curriculum removeLesson(Long curriculumId, int sectionIndex, int lessonIndex);

  Curriculum removeSection(Long curriculumId, int sectionIndex);

  Curriculum moveLesson(Long curriculumId, int fromSectionIndex, int fromLessonIndex, int toSectionIndex, int toLessonIndex);

}
