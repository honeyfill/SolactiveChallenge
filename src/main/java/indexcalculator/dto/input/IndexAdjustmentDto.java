package indexcalculator.dto.input;

import com.fasterxml.jackson.annotation.JsonProperty;

public record IndexAdjustmentDto(
        @JsonProperty("additionOperation")
        AdditionAdjustment addition,
        @JsonProperty("deletionOperation")
        DeletionAdjustment deletion,
        @JsonProperty("dividendOperation")
        DividendAdjustment dividend
) {
    @Override
    public String toString() {
        if (addition != null) return addition.toString();
        if (deletion != null) return deletion.toString();
        return dividend.toString();
    }
}
