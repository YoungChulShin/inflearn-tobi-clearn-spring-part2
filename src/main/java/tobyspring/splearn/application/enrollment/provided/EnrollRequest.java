package tobyspring.splearn.application.enrollment.provided;

import jakarta.annotation.Nonnull;

public record EnrollRequest(
    @Nonnull Long memberId,
    @Nonnull Long courseId
) {

}
