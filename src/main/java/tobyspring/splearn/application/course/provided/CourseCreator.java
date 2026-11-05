package tobyspring.splearn.application.course.provided;

import jakarta.validation.Valid;
import tobyspring.splearn.domain.course.Course;

/**
 * 강의를 준비하는 작업
 */
public interface CourseCreator {

  Course create(@Valid CourseCreateRequest createRequest);

  Course updateInfo(Long courseId, @Valid CourseInfoUpdateRequest infoUpdateRequest);
}
