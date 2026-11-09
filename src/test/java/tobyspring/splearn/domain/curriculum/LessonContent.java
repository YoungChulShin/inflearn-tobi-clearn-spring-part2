package tobyspring.splearn.domain.curriculum;

public record LessonContent(String title) {

  public static LessonContent lesson(String title) {
    return new LessonContent(title);
  }
}
