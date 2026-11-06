package tobyspring.splearn.domain.course;

import static org.instancio.Instancio.gen;
import static org.instancio.Select.*;

import java.time.LocalDateTime;
import org.instancio.Instancio;
import org.springframework.lang.Nullable;
import tobyspring.splearn.application.course.provided.CourseCreateRequest;
import tobyspring.splearn.application.course.provided.CourseInfoUpdateRequest;
import tobyspring.splearn.domain.instructor.Instructor;
import tobyspring.splearn.domain.instructor.InstructorFixture;

public class CourseFixture {


  public static Course createCourse(
      @Nullable Instructor instructor,
      @Nullable String title) {
    CourseDetail detail = Instancio.of(CourseDetail.class)
        .ignore(field(CourseDetail::getId))
        .generate(field(CourseDetail::getDescription), gen -> gen.string().maxLength(500).nullable())
        .set(field(CourseDetail::getCreatedAt), LocalDateTime.now())
        .create();

    return Instancio.of(Course.class)
        .ignore(field(Course::getId))
        .set(field(Course::getInstructor),
            instructor == null
                ? InstructorFixture.createActiveInstructor()
                : instructor)
        .set(field(Course::getTitle), title == null ? gen().string().maxLength(100).minLength(2).get() : title)
        .set(field(Course::getStatus), CourseStatus.DRAFT)
        .set(field(Course::getDetail), detail)
        .create();
  }

  public static Course createCourse() {
    return createCourse(null, null);
  }

  public static CourseCreateRequest createCourseCreateRequest(Long instructorId, @Nullable String title) {
    return Instancio.of(CourseCreateRequest.class)
        .set(field(CourseCreateRequest::instructorId), instructorId)
        .set(field(CourseCreateRequest::title), title == null ? gen().string().maxLength(100).minLength(2).get() : title)
        .generate(field(CourseCreateRequest::description), gen -> gen.string().maxLength(500).nullable())
        .create();
  }

  public static CourseInfoUpdateRequest createCourseInfoUpdateRequest(@Nullable String title) {
    return Instancio.of(CourseInfoUpdateRequest.class)
        .set(field(CourseInfoUpdateRequest::title), title == null ? gen().string().maxLength(100).minLength(2).get() : title)
        .generate(field(CourseInfoUpdateRequest::description), gen -> gen.string().maxLength(500).nullable())
        .create();
  }
}
