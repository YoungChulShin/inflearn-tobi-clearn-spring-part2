package tobyspring.splearn.application.instructor.provided;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import tobyspring.splearn.application.member.provided.MemberRegister;
import tobyspring.splearn.domain.instructor.Instructor;
import tobyspring.splearn.domain.member.Member;
import tobyspring.splearn.domain.member.MemberFixture;
import tobyspring.splearn.support.stereotype.ApplicationServiceTest;

@ApplicationServiceTest
@RequiredArgsConstructor
class InstructorFinderTest {

  final InstructorFinder instructorFinder;
  final InstructorApplication instructorApplication;
  final MemberRegister memberRegister;

  @Test
  void findByMember() {
    Member member = memberRegister.register(MemberFixture.createMemberRegisterRequest());
    member = memberRegister.activate(member.getId());

    Instructor instructor = instructorApplication.apply(new InstructorApplyRequest(member.getId()));

    Instructor foundInstructor = instructorFinder.findByMember(member.getId()).orElseThrow();

    assertThat(instructor).isEqualTo(foundInstructor);

    assertThat(instructorFinder.findByMember(Long.MAX_VALUE).isPresent()).isFalse();
  }

}