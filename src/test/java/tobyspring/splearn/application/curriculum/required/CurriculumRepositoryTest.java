package tobyspring.splearn.application.curriculum.required;

import static org.assertj.core.api.Assertions.assertThat;
import static tobyspring.splearn.domain.curriculum.LessonContent.lesson;
import static tobyspring.splearn.domain.curriculum.SectionContent.section;

import lombok.RequiredArgsConstructor;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import tobyspring.splearn.domain.curriculum.Curriculum;
import tobyspring.splearn.domain.curriculum.Lesson;
import tobyspring.splearn.domain.curriculum.Section;
import tobyspring.splearn.domain.curriculum.SectionContent;
import tobyspring.splearn.support.test.BaseRepositoryTest;

@DataJpaTest
@RequiredArgsConstructor
class CurriculumRepositoryTest extends BaseRepositoryTest {

  final CurriculumRepository curriculumRepository;
  final SectionRepository sectionRepository;
  final LessonRepository lessonRepository;

  @Test
  void saveAndFindById() {
    Long curriculumId = saveCurriculum();

    Curriculum found = curriculumRepository.findById(curriculumId).orElseThrow();

    assertThat(found.getId()).isEqualTo(curriculumId);
  }

  @Test
  void saveAndFindWithSectionsById() {
    Long curriculumId = saveCurriculum();

    Curriculum found = curriculumRepository.findWithSectionsById(curriculumId).orElseThrow();

    assertThat(found.getId()).isEqualTo(curriculumId);
  }

  @Test
  void removeLesson() {
    Long curriculumId = saveCurriculum();
    Curriculum curriculum = curriculumRepository.findWithSectionsById(curriculumId).orElseThrow();

    Lesson lesson = curriculum.removeLesson(0, 0);
    lessonRepository.delete(lesson);

    curriculumRepository.save(curriculum);

    entityManager.flush();
    entityManager.clear();


    Statistics prepareStatistics = prepareStatistics();
  }

  @Test
  void removeSection() {
    Long curriculumId = saveCurriculum();
    Curriculum curriculum = curriculumRepository.findWithSectionsById(curriculumId).orElseThrow();

    Section section = curriculum.removeSection(0);
    sectionRepository.delete(section);

    entityManager.flush();
    entityManager.clear();

    curriculum = curriculumRepository.findWithSectionsById(curriculumId).orElseThrow();
    assertThat(SectionContent.from(curriculum)).containsExactly(
        section("S2", lesson("L1"), lesson("L2"), lesson("L3")));

  }

  private Long saveCurriculum() {
    Curriculum curriculum = new Curriculum(prepareCourse());

    curriculum = curriculumRepository.save(curriculum);
    curriculum.addSection("S1");
    curriculum.addLesson(0, "L1");
    curriculum.addLesson(0, "L2");
    curriculum.addSection("S2");
    curriculum.addLesson(1, "L3");

    entityManager.flush();
    entityManager.clear();

    return curriculum.getId();
  }
}