package tobyspring.splearn.application.member.provided;

import jakarta.validation.Valid;
import tobyspring.splearn.domain.member.Member;

/**
 * 회원 인증
 */
public interface MemberAuthenticator {

  Member login(@Valid MemberLoginRequest memberLoginRequest) throws LoginFailedException;
}
