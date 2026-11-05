package tobyspring.splearn.application.course.provided;

import tobyspring.splearn.domain.course.Course;

public interface CoursePublisher {

  Course submitForReview(Long courseId);

  Course publish(Long courseId);

  Course archive(Long courseId);

}
