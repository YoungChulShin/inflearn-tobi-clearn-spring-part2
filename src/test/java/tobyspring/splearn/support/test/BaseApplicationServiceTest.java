package tobyspring.splearn.support.test;

import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import tobyspring.splearn.application.course.provided.CourseCreator;
import tobyspring.splearn.application.instructor.provided.InstructorApplication;
import tobyspring.splearn.application.member.provided.MemberRegister;
import tobyspring.splearn.domain.course.Course;
import tobyspring.splearn.domain.course.CourseFixture;
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

  @Autowired
  CourseCreator courseCreator;

  protected Member member;
  protected Instructor instructor;
  protected Course course;

  @NonNull
  protected Instructor prepareInstructor() {
    prepareMember();

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

  @NonNull
  protected Course prepareCourse() {
    prepareInstructor();

    this.course = courseCreator.create(CourseFixture.createCourseCreateRequest(instructor.getId(), null));
    this.course.updateInfo(CourseFixture.createCourseInfoUpdateRequest(null).toInfo());

    return this.course;
  }
}
