package tobyspring.splearn.domain.course;

import static org.instancio.Select.*;

import java.time.LocalDateTime;
import org.instancio.Instancio;
import org.instancio.Select;
import tobyspring.splearn.domain.instructor.Instructor;
import tobyspring.splearn.domain.instructor.InstructorFixture;

public class CourseFixture {


  public static Course createCourse() {
    Instructor instructor = InstructorFixture.createActiveInstructor();

    CourseDetail detail = Instancio.of(CourseDetail.class)
        .generate(field(CourseDetail::getDescription), gen -> gen.string().maxLength(500).nullable())
        .set(field(CourseDetail::getCreatedAt), LocalDateTime.now())
        .create();

    return Instancio.of(Course.class)
        .ignore(field(Course::getId))
        .set(field(Course::getInstructor), instructor)
        .generate(field(Course::getTitle), gen -> gen.string().maxLength(100).minLength(2))
        .set(field(Course::getStatus), CourseStatus.DRAFT)
        .set(field(Course::getDetail), detail)
        .create();
  }
}
