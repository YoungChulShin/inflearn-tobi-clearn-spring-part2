package tobyspring.splearn.application.course.provided;

import jakarta.validation.Valid;
import jakarta.validation.ValidationException;
import tobyspring.splearn.domain.course.Course;

/**
 * 강의를 준비하는 작업
 */
public interface CourseCreator {

  Course create(@Valid CourseCreateRequest createRequest) throws ValidationException;

  Course updateInfo(Long courseId, @Valid CourseInfoUpdateRequest infoUpdateRequest) throws ValidationException;
}
