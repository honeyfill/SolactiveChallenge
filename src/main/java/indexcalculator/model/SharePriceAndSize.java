package indexcalculator.model;

import java.math.BigDecimal;

public class SharePriceAndSize {
    private BigDecimal sharePrice;
    private BigDecimal shareSize;

    public SharePriceAndSize(BigDecimal sharePrice, BigDecimal shareSize) {
        this.sharePrice = sharePrice;
        this.shareSize = shareSize;
    }

    public BigDecimal getSharePrice() {
        return sharePrice;
    }

    public BigDecimal getShareSize() {
        return shareSize;
    }

    public void setSharePrice(BigDecimal sharePrice) {
        this.sharePrice = sharePrice;
    }

    public void setShareSize(BigDecimal shareSize) {
        this.shareSize = shareSize;
    }
}
