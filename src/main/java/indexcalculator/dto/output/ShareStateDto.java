package indexcalculator.dto.output;

import java.math.BigDecimal;
import java.math.RoundingMode;

public record ShareStateDto(
        String shareName,
        BigDecimal sharePrice,
        BigDecimal numberOfShares,
        BigDecimal indexWeightPct,
        BigDecimal indexValue
) {
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ShareStateDto other)) {
            return false;
        }
        if (!shareName.equals(other.shareName)) {
            return false;
        }
        if (!compareBigDecimalWithPrecision(sharePrice, other.sharePrice)) {
            return false;
        }
        if (!compareBigDecimalWithPrecision(numberOfShares, other.numberOfShares)) {
            return false;
        }
        if (!compareBigDecimalWithPrecision(indexWeightPct, other.indexWeightPct)) {
            return false;
        }
        return compareBigDecimalWithPrecision(indexValue, other.indexValue);
    }

    private boolean compareBigDecimalWithPrecision(BigDecimal lhs, BigDecimal rhs) {
        int scale = Math.max(lhs.scale(), rhs.scale()) - 1;
        BigDecimal scaledLhs = lhs.setScale(scale, RoundingMode.HALF_UP);
        BigDecimal scaledRhs = rhs.setScale(scale, RoundingMode.HALF_UP);
        return scaledRhs.equals(scaledLhs);
    }
}
