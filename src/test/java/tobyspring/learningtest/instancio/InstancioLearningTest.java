package tobyspring.learningtest.instancio;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.instancio.Select.*;

import org.instancio.Instancio;
import org.instancio.Model;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.parameters.P;

public class InstancioLearningTest {

  @Test
  void user() {
    User user = Instancio.of(User.class)
        .ignore(field(User::getId))
        .generate(field(User::getEmail), gen -> gen.net().email())
        .set(field(User::getStatus), UserStatus.PENDING)
        .create();

    assertThat(user.getId()).isNull();
    assertThat(user.getEmail()).isNotEmpty();
    assertThat(user.getName()).isNotEmpty();
    assertThat(user.getStatus()).isEqualTo(UserStatus.PENDING);
  }

  @Test
  void userModel() {
    Model<User> model = Instancio.of(User.class)
        .ignore(field(User::getId))
        .generate(field(User::getEmail), gen -> gen.net().email())
        .set(field(User::getStatus), UserStatus.PENDING)
        .toModel();

    for (int i = 0; i < 100; i++) {
      User user = Instancio.of(model).create();

      assertThat(user.getId()).isNull();
      assertThat(user.getEmail()).isNotEmpty();
      assertThat(user.getName()).isNotEmpty();
      assertThat(user.getStatus()).isEqualTo(UserStatus.PENDING);
    }
  }

  @Test
  void annotation() {
    UserRegisterRequest userRegisterRequest = Instancio.of(UserRegisterRequest.class).create();

    System.out.println(userRegisterRequest);
  }
}
