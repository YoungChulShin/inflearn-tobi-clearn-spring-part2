package tobyspring.splearn.application.course;

import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import tobyspring.splearn.application.course.provided.CourseCreateRequest;
import tobyspring.splearn.application.course.provided.CourseCreator;
import tobyspring.splearn.application.course.provided.CourseFinder;
import tobyspring.splearn.application.course.provided.CourseInfoUpdateRequest;
import tobyspring.splearn.application.course.provided.CourseValidator;
import tobyspring.splearn.application.course.required.CourseRepository;
import tobyspring.splearn.application.instructor.provided.InstructorFinder;
import tobyspring.splearn.domain.course.Course;
import tobyspring.splearn.domain.instructor.Instructor;
import tobyspring.splearn.support.stereotype.ValidatedApplicationService;

@ValidatedApplicationService
@RequiredArgsConstructor
public class CourseModifyService implements CourseCreator {

  private final CourseRepository courseRepository;
  private final CourseFinder courseFinder;
  private final InstructorFinder instructorFinder;
  private final CourseValidator courseValidator;

  @Override
  public Course create(CourseCreateRequest createRequest) throws ValidationException {
    Instructor instructor = instructorFinder.find(createRequest.instructorId());

    courseValidator.validateForCreate(instructor, createRequest);

    Course course = new Course(instructor, createRequest.title(), createRequest.description());

    return courseRepository.save(course);
  }

  @Override
  public Course updateInfo(Long courseId, CourseInfoUpdateRequest infoUpdateRequest) throws ValidationException {
    Course course = courseFinder.find(courseId);

    courseValidator.validateForUpdate(course, infoUpdateRequest);

    course.updateInfo(infoUpdateRequest.toInfo());

    return courseRepository.save(course);
  }
}
