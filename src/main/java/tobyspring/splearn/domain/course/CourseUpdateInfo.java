package tobyspring.splearn.domain.course;

import jakarta.validation.constraints.Size;
import org.springframework.lang.Nullable;

public record CourseUpdateInfo(
    @Size(min = 2, max = 100) String title,
    @Nullable String description
) {

}
