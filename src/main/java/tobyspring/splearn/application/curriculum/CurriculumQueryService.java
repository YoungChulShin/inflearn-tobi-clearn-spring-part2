package tobyspring.splearn.application.curriculum;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import tobyspring.splearn.application.curriculum.provided.CurriculumFinder;
import tobyspring.splearn.application.curriculum.required.CurriculumRepository;
import tobyspring.splearn.domain.curriculum.Curriculum;
import tobyspring.splearn.domain.curriculum.Lesson;
import tobyspring.splearn.support.stereotype.ApplicationService;

@ApplicationService
@RequiredArgsConstructor
public class CurriculumQueryService implements CurriculumFinder {

  private final CurriculumRepository curriculumRepository;

  @Override
  public Curriculum find(Long curriculumId) {
    return curriculumRepository.findById(curriculumId)
        .orElseThrow(() -> new IllegalArgumentException("커리큘럼을 찾을 수 없습니다. ID" + curriculumId));
  }

  @Override
  public Curriculum findWithSections(Long curriculumId) {
    return curriculumRepository.findWithSectionsById(curriculumId)
        .orElseThrow(() -> new IllegalArgumentException("커리큘럼을 찾을 수 없습니다. ID" + curriculumId));
  }

  @Override
  public Curriculum findByCourse(Long courseId) {
    return curriculumRepository.findByCourseId(courseId)
        .orElseThrow(() -> new IllegalArgumentException("커리큘럼을 찾을 수 없습니다. Course ID" + courseId));
  }

  @Override
  public Optional<Lesson> firstLesson(Long curriculumId) {
    return this.findWithSections(curriculumId).firstLesson();
  }

  @Override
  public Optional<Lesson> nextLesson(Long curriculumId, Long lessonId) {
    return this.findWithSections(curriculumId).nextLesson(lessonId);
  }
}
