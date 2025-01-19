package indexcalculator.dto.input;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record IndexCreationDto(@NotBlank String indexName, @NotNull List<ShareCreationDto> indexShares) {
}
