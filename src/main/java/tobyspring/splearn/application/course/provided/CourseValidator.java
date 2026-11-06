package tobyspring.splearn.application.course.provided;

import tobyspring.splearn.domain.course.Course;
import tobyspring.splearn.domain.instructor.Instructor;
import tobyspring.splearn.support.exception.ValidationException;

public interface CourseValidator {

  void validateForCreate(Instructor instructor, CourseCreateRequest createRequest) throws ValidationException;

  void validateForUpdate(Course course, CourseInfoUpdateRequest infoUpdateRequest);
}
