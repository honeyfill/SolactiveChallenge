package indexcalculator.dto.input;


import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record IndexCreationDto(@NotBlank String indexName, List<ShareCreationDto> indexShares) {
}
