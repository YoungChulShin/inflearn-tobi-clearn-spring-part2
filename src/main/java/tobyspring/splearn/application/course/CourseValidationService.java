package tobyspring.splearn.application.course;

import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import tobyspring.splearn.application.course.provided.CourseCreateRequest;
import tobyspring.splearn.application.course.provided.CourseValidator;
import tobyspring.splearn.application.course.required.CourseRepository;
import tobyspring.splearn.domain.instructor.Instructor;
import tobyspring.splearn.support.exception.ValidationException;
import tobyspring.splearn.support.stereotype.ApplicationService;

@ApplicationService
@RequiredArgsConstructor
public class CourseValidationService implements CourseValidator {

  private final CourseRepository courseRepository;

  @Override
  public void validateForCreate(Instructor instructor, CourseCreateRequest createRequest)
      throws ValidationException {
    instructor.ensureActive();

    List<String> errors = new ArrayList<>();

    checkTitleDuplication(instructor, createRequest.title(), errors);
    checkBannedWords(createRequest.title(), errors);
    checkBannedWords(createRequest.description(), errors);

    if (!errors.isEmpty()) {
      throw new ValidationException(errors);
    }
  }

  private void checkBannedWords(String text, List<String> errors) {
    // TODO
  }

  private void checkTitleDuplication(Instructor instructor, String title, List<String> errors) {
    if (courseRepository.findByInstructorAndTitle(instructor, title).isPresent()) {
      errors.add("이미 사용중인 강의 제목입니다. " + title);
    }
  }
}
