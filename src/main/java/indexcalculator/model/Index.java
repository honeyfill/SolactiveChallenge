package indexcalculator.model;

import indexcalculator.dto.output.IndexStateDto;
import indexcalculator.dto.output.ShareStateDto;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Index {
    private final String indexName;
    private final ConcurrentHashMap<String, SharePriceAndSize> share2PriceAndSize;
    private final BigDecimal indexValue;

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    private static final MathContext MATH_CONTEXT = new MathContext(15, RoundingMode.HALF_UP);

    public Index(String indexName, ConcurrentHashMap<String, SharePriceAndSize> share2PriceAndSize, BigDecimal indexValue) {
        this.indexName = indexName;
        this.share2PriceAndSize = share2PriceAndSize;
        this.indexValue = indexValue;
    }

    public String getIndexName() {
        return indexName;
    }

    public BigDecimal getIndexValue() {
        return indexValue;
    }

    synchronized public IndexStateDto makeIndexStateDto() {

        List<ShareStateDto> indexMembers = new ArrayList<>();
        for (Map.Entry<String, SharePriceAndSize> component : this.share2PriceAndSize.entrySet()) {

            String shareName = component.getKey();
            BigDecimal sharePrice = component.getValue().getSharePrice();
            BigDecimal numberOfShares = component.getValue().getShareSize();

            BigDecimal indexValue = numberOfShares.multiply(sharePrice, MATH_CONTEXT);
            BigDecimal indexWeightPct = indexValue.divide(this.getIndexValue(), MATH_CONTEXT).multiply(HUNDRED, MATH_CONTEXT);

            ShareStateDto dto = new ShareStateDto(shareName, sharePrice, numberOfShares, indexWeightPct, indexValue);
            indexMembers.add(dto);
        }
        indexMembers.sort((a, b) -> a.shareName().compareTo(b.shareName()));
        return new IndexStateDto(this.getIndexName(), this.getIndexValue(), indexMembers);
    }

    public List<String> getShares() {
        return share2PriceAndSize.keySet().stream().toList();
    }

    public boolean checkShareExistence(String shareName) {
        return share2PriceAndSize.containsKey(shareName);
    }

    public BigDecimal getSharePrice(String shareName) {
        return share2PriceAndSize.get(shareName).getSharePrice();
    }

    synchronized public void addShareAndAdjust(String shareName, SharePriceAndSize priceAndSize) {
        BigDecimal wrongIndexValue = indexValue.subtract(priceAndSize.getSharePrice().multiply(priceAndSize.getShareSize(), MATH_CONTEXT), MATH_CONTEXT);
        BigDecimal uniformAdjustFraction = wrongIndexValue.divide(indexValue, MATH_CONTEXT);

        for (SharePriceAndSize component : share2PriceAndSize.values()) {
            component.setShareSize(component.getShareSize().multiply(uniformAdjustFraction, MATH_CONTEXT));
        }
        share2PriceAndSize.put(shareName, priceAndSize);
    }

    synchronized public void deleteShareAndAdjust(String shareName) {
        SharePriceAndSize priceAndSize = share2PriceAndSize.get(shareName);
        BigDecimal wrongIndexValue = indexValue.subtract(priceAndSize.getSharePrice().multiply(priceAndSize.getShareSize(), MATH_CONTEXT), MATH_CONTEXT);
        BigDecimal uniformAdjustFraction = indexValue.divide(wrongIndexValue, MATH_CONTEXT);
        share2PriceAndSize.remove(shareName);
        for (SharePriceAndSize component : share2PriceAndSize.values()) {
            component.setShareSize(component.getShareSize().multiply(uniformAdjustFraction, MATH_CONTEXT));
        }
    }

    synchronized public void subtractDividendAndAdjust(String shareName, BigDecimal difference) {
        SharePriceAndSize priceAndSize = share2PriceAndSize.get(shareName);
        priceAndSize.setSharePrice(priceAndSize.getSharePrice().subtract(difference, MATH_CONTEXT));

        BigDecimal wrongIndexValue = indexValue.subtract(priceAndSize.getShareSize().multiply(difference, MATH_CONTEXT), MATH_CONTEXT);
        BigDecimal uniformAdjustFraction = indexValue.divide(wrongIndexValue, MATH_CONTEXT);

        for (SharePriceAndSize component : share2PriceAndSize.values()) {
            component.setShareSize(component.getShareSize().multiply(uniformAdjustFraction, MATH_CONTEXT));
        }
    }

}
