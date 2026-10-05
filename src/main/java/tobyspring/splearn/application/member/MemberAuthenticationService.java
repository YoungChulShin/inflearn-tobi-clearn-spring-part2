package tobyspring.splearn.application.member;

import lombok.RequiredArgsConstructor;
import tobyspring.splearn.application.member.provided.LoginFailedException;
import tobyspring.splearn.application.member.provided.MemberAuthenticator;
import tobyspring.splearn.application.member.provided.MemberLoginRequest;
import tobyspring.splearn.application.member.required.MemberRepository;
import tobyspring.splearn.domain.member.Member;
import tobyspring.splearn.domain.member.PasswordEncoder;
import tobyspring.splearn.domain.shared.Email;
import tobyspring.splearn.support.stereotype.ValidatedApplicationService;

@ValidatedApplicationService
@RequiredArgsConstructor
public class MemberAuthenticationService implements MemberAuthenticator {

  private final MemberRepository memberRepository;
  private final PasswordEncoder passwordEncoder;

  @Override
  public Member login(MemberLoginRequest loginRequest) throws LoginFailedException {
    Member member = memberRepository.findByEmail(
        new Email(loginRequest.email())).orElseThrow(LoginFailedException::new);

    if (!member.isActive()) {
      throw new LoginFailedException();
    }

    if (!member.verifyPassword(loginRequest.password(), passwordEncoder)) {
      throw new LoginFailedException();
    }

    return member;
  }
}
