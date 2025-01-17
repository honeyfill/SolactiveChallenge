package indexcalculator.dto.input;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record DividendAdjustment(@NotBlank String shareName, @Positive BigDecimal dividendValue) {
}
