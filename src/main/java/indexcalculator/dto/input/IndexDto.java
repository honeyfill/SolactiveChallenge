package indexcalculator.dto.input;

import jakarta.validation.constraints.NotNull;

public record IndexDto(@NotNull IndexCreationDto index) {
}
