package tobyspring.splearn.application.instructor.provided;

import jakarta.validation.constraints.NotNull;

public record InstructorApplyRequest(
    @NotNull Long memberId
) {

}
