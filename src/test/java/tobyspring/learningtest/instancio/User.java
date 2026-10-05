package tobyspring.learningtest.instancio;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class User {

  private Long id;
  private String name;
  private String email;
  private UserStatus status;
}
