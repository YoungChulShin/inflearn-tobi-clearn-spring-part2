package tobyspring.splearn.support.test;

import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import tobyspring.splearn.application.instructor.provided.InstructorApplication;
import tobyspring.splearn.application.member.provided.MemberRegister;
import tobyspring.splearn.domain.instructor.Instructor;
import tobyspring.splearn.domain.instructor.InstructorFixture;
import tobyspring.splearn.domain.member.Member;
import tobyspring.splearn.domain.member.MemberFixture;
import tobyspring.splearn.support.stereotype.ApplicationServiceTest;

@ApplicationServiceTest
public class BaseApplicationServiceTest {

  @Autowired
  MemberRegister memberRegister;

  @Autowired
  InstructorApplication instructorApplication;

  protected Member member;
  protected Instructor instructor;

  @NonNull
  protected Instructor prepareInstructor() {
    this.member = prepareMember();

    this.instructor = instructorApplication.apply(InstructorFixture.createApplyRequest(member));
    this.instructor.approve();

    return this.instructor;
  }

  @NonNull
  protected Member prepareMember() {
    this.member = memberRegister.register(MemberFixture.createMemberRegisterRequest());
    this.member.activate();

    return this.member;
  }
}
