package tobyspring.splearn.domain.curriculum;

import tobyspring.splearn.domain.course.CourseFixture;

public class CurriculumFixture {

  public static Curriculum createCurriculum() {
    return new Curriculum(CourseFixture.createCourse());
  }
}
