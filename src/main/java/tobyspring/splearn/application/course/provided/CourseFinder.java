package tobyspring.splearn.application.course.provided;

import java.util.List;
import tobyspring.splearn.domain.course.Course;

/**
 * 강의를 조회
 */
public interface CourseFinder {

  Course find(Long courseId);

  List<Course> findByTitle(String keyword);

  List<Course> findByInstructor(Long instructorId);
}
