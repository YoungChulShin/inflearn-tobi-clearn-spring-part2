package tobyspring.splearn.application.course.required;

import tobyspring.splearn.domain.curriculum.InvalidCurriculumException;

public interface CurriculumValidator {

  void validate(Long courseId) throws InvalidCurriculumException;

}
