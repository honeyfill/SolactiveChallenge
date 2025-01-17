package indexcalculator.dto.input;

import jakarta.validation.constraints.NotBlank;

public record DeletionAdjustment(@NotBlank String shareName, @NotBlank String indexName) {
}
