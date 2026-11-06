package tobyspring.splearn.application.course;

import lombok.RequiredArgsConstructor;
import tobyspring.splearn.application.course.provided.CourseCreateRequest;
import tobyspring.splearn.application.course.provided.CourseCreator;
import tobyspring.splearn.application.course.provided.CourseFinder;
import tobyspring.splearn.application.course.provided.CourseInfoUpdateRequest;
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

  @Override
  public Course create(CourseCreateRequest createRequest) {
    Instructor instructor = instructorFinder.find(createRequest.instructorId());

    // 2. validate
    // 3. course 생성

    return null;
  }

  @Override
  public Course updateInfo(Long courseId, CourseInfoUpdateRequest infoUpdateRequest) {
    return null;
  }
}
