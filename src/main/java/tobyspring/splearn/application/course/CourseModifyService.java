package tobyspring.splearn.application.course;

import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import tobyspring.splearn.application.course.provided.CourseCreateRequest;
import tobyspring.splearn.application.course.provided.CourseCreator;
import tobyspring.splearn.application.course.provided.CourseFinder;
import tobyspring.splearn.application.course.provided.CourseInfoUpdateRequest;
import tobyspring.splearn.application.course.provided.CoursePublisher;
import tobyspring.splearn.application.course.provided.CourseValidator;
import tobyspring.splearn.application.course.required.CourseRepository;
import tobyspring.splearn.application.instructor.provided.InstructorFinder;
import tobyspring.splearn.domain.course.Course;
import tobyspring.splearn.domain.instructor.Instructor;
import tobyspring.splearn.support.stereotype.ValidatedApplicationService;

@ValidatedApplicationService
@RequiredArgsConstructor
public class CourseModifyService implements CourseCreator, CoursePublisher {

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

  @Override
  public Course submitForReview(Long courseId) {
    Course course = courseFinder.find(courseId);

    courseValidator.validateForReview(course);

    course.submitForReview();

    return courseRepository.save(course);
  }

  @Override
  public Course publish(Long courseId) {
    Course course = courseFinder.find(courseId);

    courseValidator.validateForPublish(course);

    course.publish();

    return courseRepository.save(course);
  }

  @Override
  public Course archive(Long courseId) {
    Course course = courseFinder.find(courseId);

    courseValidator.validateForArchive(course);

    course.archive();

    return courseRepository.save(course);
  }
}
