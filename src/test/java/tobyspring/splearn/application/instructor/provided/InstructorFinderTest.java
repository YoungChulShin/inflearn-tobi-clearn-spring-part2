package tobyspring.splearn.application.instructor.provided;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import tobyspring.splearn.domain.instructor.Instructor;
import tobyspring.splearn.support.stereotype.ApplicationServiceTest;
import tobyspring.splearn.support.test.BaseApplicationServiceTest;

@ApplicationServiceTest
@RequiredArgsConstructor
class InstructorFinderTest extends BaseApplicationServiceTest {

  final InstructorFinder instructorFinder;
  final InstructorApplication instructorApplication;

  @Test
  void findByMember() {
    prepareMember();

    Instructor instructor = instructorApplication.apply(new InstructorApplyRequest(member.getId()));

    Instructor foundInstructor = instructorFinder.findByMember(member.getId()).orElseThrow();

    assertThat(instructor).isEqualTo(foundInstructor);

    assertThat(instructorFinder.findByMember(Long.MAX_VALUE).isPresent()).isFalse();
  }

}