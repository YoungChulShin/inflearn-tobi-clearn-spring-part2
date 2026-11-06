package tobyspring.splearn.application.course;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import tobyspring.splearn.application.course.provided.CourseCreateRequest;
import tobyspring.splearn.application.course.provided.CourseInfoUpdateRequest;
import tobyspring.splearn.application.course.provided.CourseValidator;
import tobyspring.splearn.application.course.required.CourseRepository;
import tobyspring.splearn.domain.course.Course;
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

    checkTitleDuplicationForCreate(instructor, createRequest.title(), errors);
    checkBannedWords(createRequest.title(), errors);
    checkBannedWords(createRequest.description(), errors);

    if (!errors.isEmpty()) {
      throw new ValidationException(errors);
    }
  }

  @Override
  public void validateForUpdate(Course course, CourseInfoUpdateRequest infoUpdateRequest) {
    List<String> errors = new ArrayList<>();

    checkTitleDuplicationForUpdate(course, course.getInstructor(), infoUpdateRequest.title(), errors);
    checkBannedWords(infoUpdateRequest.title(), errors);
    checkBannedWords(infoUpdateRequest.description(), errors);

    if (!errors.isEmpty()) {
      throw new ValidationException(errors);
    }
  }

  private void checkBannedWords(String text, List<String> errors) {
    // TODO
  }

  private void checkTitleDuplicationForCreate(Instructor instructor, String title, List<String> errors) {
    if (courseRepository.findByInstructorAndTitle(instructor, title).isPresent()) {
      errors.add("이미 사용중인 강의 제목입니다. " + title);
    }
  }

  private void checkTitleDuplicationForUpdate(
      Course course,
      Instructor instructor,
      String title,
      List<String> errors) {
    courseRepository.findByInstructorAndTitle(instructor, title).ifPresent(found -> {
      if (!found.equals(course)) {
        errors.add("이미 사용중인 강의 제목입니다. " + title);
      }
    });
  }
}
