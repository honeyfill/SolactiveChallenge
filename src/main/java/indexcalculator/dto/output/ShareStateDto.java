package indexcalculator.dto.output;

import java.math.BigDecimal;

public record ShareStateDto(
        String shareName,
        BigDecimal sharePrice,
        BigDecimal numberOfShares,
        BigDecimal indexWeightPct,
        BigDecimal indexValue
) {
}
