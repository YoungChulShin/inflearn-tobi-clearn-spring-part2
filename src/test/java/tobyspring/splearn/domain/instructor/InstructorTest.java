package tobyspring.splearn.domain.instructor;

import static org.assertj.core.api.Assertions.*;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import tobyspring.splearn.domain.member.Member;
import tobyspring.splearn.domain.member.MemberFixture;

class InstructorTest {

  @Test
  void apply() {
    Member member = MemberFixture.createActiveMember();

    Instructor instructor = Instructor.apply(member);

    assertThat(instructor.member).isEqualTo(member);
    assertThat(instructor.status).isEqualTo(InstructorStatus.PENDING);
  }

  @Test
  void applyFailedMemberNotActive() {
    Member member = MemberFixture.createMember();

    Assertions.assertThatThrownBy(() -> Instructor.apply(member))
        .isInstanceOf(IllegalStateException.class);
  }

  @Test
  void approve() {
    Instructor instructor = InstructorFixture.createInstructor();

    instructor.approve();

    assertThat(instructor.status).isEqualTo(InstructorStatus.ACTIVE);
  }

  @Test
  void approveFailed() {
    Instructor instructor = InstructorFixture.createActiveInstructor();

    Assertions.assertThatThrownBy(instructor::approve)
        .isInstanceOf(IllegalStateException.class);
  }

  @Test
  void reject() {
    Instructor instructor = InstructorFixture.createInstructor();

    instructor.reject();

    assertThat(instructor.status).isEqualTo(InstructorStatus.REJECTED);
  }

  @Test
  void rejectFailed() {
    Instructor instructor = InstructorFixture.createInstructor();
    instructor.reject();

    Assertions.assertThatThrownBy(instructor::reject)
        .isInstanceOf(IllegalStateException.class);
  }

  @Test
  void isActive() {
    Member member = MemberFixture.createActiveMember();
    Instructor instructor = Instructor.apply(member);

    assertThat(instructor.isActive()).isFalse();

    instructor.approve();
    assertThat(instructor.isActive()).isTrue();
  }

  @Test
  void ensureActive() {
    Member member = MemberFixture.createActiveMember();
    Instructor instructor = Instructor.apply(member);

    Assertions.assertThatThrownBy(instructor::ensureActive)
        .isInstanceOf(IllegalStateException.class);

    instructor.approve();

    instructor.ensureActive();
  }


}