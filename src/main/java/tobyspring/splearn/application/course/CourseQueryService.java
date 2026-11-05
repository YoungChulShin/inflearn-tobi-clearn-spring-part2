package tobyspring.splearn.application.course;

import java.util.List;
import lombok.RequiredArgsConstructor;
import tobyspring.splearn.application.course.provided.CourseFinder;
import tobyspring.splearn.application.course.required.CourseRepository;
import tobyspring.splearn.domain.course.Course;
import tobyspring.splearn.support.stereotype.ApplicationService;

@ApplicationService
@RequiredArgsConstructor
public class CourseQueryService implements CourseFinder {

  private final CourseRepository courseRepository;

  @Override
  public Course find(Long courseId) {
    return courseRepository.findById(courseId).orElseThrow(
        () -> new IllegalArgumentException("강의를 찾을 수 없습니다. ID: " + courseId));
  }

  @Override
  public List<Course> findByTitle(String keyword) {
    return courseRepository.findByTitleContaining(keyword);
  }

  @Override
  public List<Course> findByInstructor(Long instructorId) {
    return courseRepository.findByInstructorId(instructorId);
  }
}
