package tobyspring.splearn.application.member.provided;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import tobyspring.splearn.domain.member.MemberRegisterInfo;

public record MemberRegisterRequest(
        @Email String email,
        @Size(min = 5, max = 20) String nickname,
        @Size(min = 8, max = 100) String password) {

  public MemberRegisterInfo toInfo() {
    return new MemberRegisterInfo(this.email, this.nickname, this.password);
  }
}
