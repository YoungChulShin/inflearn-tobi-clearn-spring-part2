package tobyspring.splearn.application.instructor.provided;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import lombok.RequiredArgsConstructor;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import tobyspring.splearn.application.instructor.required.InstructorRepository;
import tobyspring.splearn.domain.instructor.Instructor;
import tobyspring.splearn.domain.instructor.InstructorFixture;
import tobyspring.splearn.domain.instructor.InstructorStatus;
import tobyspring.splearn.support.stereotype.ApplicationServiceTest;
import tobyspring.splearn.support.test.BaseApplicationServiceTest;


@ApplicationServiceTest
@RequiredArgsConstructor
class InstructorApplicationTest extends BaseApplicationServiceTest {

  final InstructorApplication instructorApplication;
  final InstructorRepository instructorRepository;

  @Test
  void apply() {
    prepareActiveMember();

    Instructor instructor = instructorApplication.apply(InstructorFixture.createApplyRequest(member));

    assertThat(instructor.getId()).isNotNull();
    assertThat(instructor.getStatus()).isEqualTo(InstructorStatus.PENDING);

    instructorRepository.findById(instructor.getId()).orElseThrow();
  }

  @Test
  void duplicateApply() {
    prepareActiveMember();

    instructorApplication.apply(InstructorFixture.createApplyRequest(member));

    Assertions.assertThatThrownBy(() -> instructorApplication.apply(InstructorFixture.createApplyRequest(member)))
        .isInstanceOf(DuplicateInstructorApplicationException.class);
  }

  @Test
  void approve() {
    Instructor instructor = instructorApplication.approve(preparePendingInstructor().getId());

    assertThat(instructor.getStatus()).isEqualTo(InstructorStatus.ACTIVE);
  }

  @Test
  void reject() {
    Instructor instructor = instructorApplication.reject(preparePendingInstructor().getId());

    assertThat(instructor.getStatus()).isEqualTo(InstructorStatus.REJECTED);
  }

  private Instructor preparePendingInstructor() {
    prepareActiveMember();

    return instructorApplication.apply(InstructorFixture.createApplyRequest(member));
  }
}