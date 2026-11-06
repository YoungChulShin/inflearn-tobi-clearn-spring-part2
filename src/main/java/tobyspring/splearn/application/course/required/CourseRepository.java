package tobyspring.splearn.application.course.required;

import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.Repository;
import tobyspring.splearn.domain.course.Course;
import tobyspring.splearn.domain.instructor.Instructor;

public interface CourseRepository extends Repository<Course, Long> {

  Course save(Course course);

  Optional<Course> findById(Long id);

  List<Course> findByTitleContaining(String keyword);

  default List<Course> findByInstructor(Instructor instructor) {
    return findByInstructorId(instructor.getId());
  }

  List<Course> findByInstructorId(Long instructorId);

  Optional<Course> findByInstructorAndTitle(Instructor instructor, String title);
}
