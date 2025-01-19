package indexcalculator.dto.output;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public record IndexStateDto(
        String indexName,
        BigDecimal indexValue,
        List<ShareStateDto> indexMembers) {

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof IndexStateDto other)) {
            return false;
        }
        if (!indexName.equals(other.indexName)) {
            return false;
        }
        if (!compareBigDecimalWithPrecision(indexValue, other.indexValue)) {
            return false;
        }
        return indexMembers.equals(other.indexMembers);
    }

    private boolean compareBigDecimalWithPrecision(BigDecimal lhs, BigDecimal rhs) {
        int scale = Math.max(lhs.scale(), rhs.scale()) - 1;
        BigDecimal scaledLhs = lhs.setScale(scale, RoundingMode.HALF_UP);
        BigDecimal scaledRhs = rhs.setScale(scale, RoundingMode.HALF_UP);
        return scaledRhs.equals(scaledLhs);
    }
}
