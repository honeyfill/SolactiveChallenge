package indexcalculator.dto.output;

import java.math.BigDecimal;
import java.util.List;

public record IndexStateDto(
        String indexName,
        BigDecimal indexValue,
        List<ShareStateDto> indexMembers) {
}
