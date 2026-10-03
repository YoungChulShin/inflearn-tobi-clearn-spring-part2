package tobyspring.splearn.application.member.provided;

import jakarta.transaction.Transactional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import tobyspring.splearn.SplearnTestConfiguration;
import tobyspring.splearn.domain.member.MemberFixture;

@SpringBootTest
@Transactional
@Import(SplearnTestConfiguration.class)
public class MemberAuthenticatorTest {

  @Autowired
  private MemberAuthenticator memberAuthenticator;

  @Autowired
  private MemberRegister memberRegister;

  @Test
  void login() {
    var registerRequest = MemberFixture.createMemberRegisterRequest();
    memberRegister.register(registerRequest).activate();

    var member = memberAuthenticator.login(new MemberLoginRequest(registerRequest.email(), registerRequest.password()));
  }

  @Test
  void loginFailedNotActive() {
    var registerRequest = MemberFixture.createMemberRegisterRequest();
    memberRegister.register(registerRequest);

    Assertions.assertThatThrownBy(() ->
        memberAuthenticator.login(new MemberLoginRequest(registerRequest.email(), registerRequest.password())))
        .isInstanceOf(LoginFailedException.class);
  }

  @Test
  void loginFailedEmailNotExist() {
    var registerRequest = MemberFixture.createMemberRegisterRequest();
    memberRegister.register(registerRequest).activate();

    Assertions.assertThatThrownBy(() ->
            memberAuthenticator.login(new MemberLoginRequest("notexist@email.com", registerRequest.password())))
        .isInstanceOf(LoginFailedException.class);
  }

  @Test
  void loginFailedWrongPassword() {
    var registerRequest = MemberFixture.createMemberRegisterRequest();
    memberRegister.register(registerRequest).activate();

    Assertions.assertThatThrownBy(() ->
            memberAuthenticator.login(new MemberLoginRequest(registerRequest.email(), "wrongpassword")))
        .isInstanceOf(LoginFailedException.class);
  }
}
