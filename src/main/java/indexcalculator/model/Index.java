package indexcalculator.model;

import indexcalculator.dto.output.ShareStateDto;
import indexcalculator.dto.output.IndexStateDto;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Index {
    private final String indexName;
    private final HashMap<String, SharePriceAndSize> share2PriceAndSize;
    private final BigDecimal indexValue;

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    public Index(String indexName, HashMap<String, SharePriceAndSize> share2PriceAndSize, BigDecimal indexValue) {
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

    public IndexStateDto makeIndexStateDto() {

        List<ShareStateDto> indexMembers = new ArrayList<>();
        for (Map.Entry<String, SharePriceAndSize> component : this.share2PriceAndSize.entrySet()) {

            String shareName = component.getKey();
            BigDecimal sharePrice = component.getValue().getSharePrice();
            BigDecimal numberOfShares = component.getValue().getShareSize();

            BigDecimal indexValue = numberOfShares.multiply(sharePrice);
            BigDecimal indexWeightPct = indexValue.divide(this.getIndexValue(), 8, RoundingMode.UP).multiply(HUNDRED);

            ShareStateDto dto = new ShareStateDto(shareName, sharePrice, numberOfShares, indexWeightPct, indexValue);
            indexMembers.add(dto);
        }

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
        BigDecimal wrongIndexValue = indexValue.subtract(priceAndSize.getSharePrice().multiply(priceAndSize.getShareSize()));
        BigDecimal uniformAdjustFraction = wrongIndexValue.divide(indexValue, 8, RoundingMode.UP);

        for (SharePriceAndSize component : share2PriceAndSize.values()) {
            component.setShareSize(component.getShareSize().multiply(uniformAdjustFraction));
        }
        share2PriceAndSize.put(shareName, priceAndSize);
    }

    synchronized public void deleteShareAndAdjust(String shareName) {
        SharePriceAndSize priceAndSize = share2PriceAndSize.get(shareName);
        BigDecimal wrongIndexValue = indexValue.subtract(priceAndSize.getSharePrice().multiply(priceAndSize.getShareSize()));
        BigDecimal uniformAdjustFraction = indexValue.divide(wrongIndexValue, 8, RoundingMode.UP);

        share2PriceAndSize.remove(shareName);
        for (SharePriceAndSize component : share2PriceAndSize.values()) {
            component.setShareSize(component.getShareSize().multiply(uniformAdjustFraction));
        }
    }

    public void subtractDividendAndAdjust(String shareName, BigDecimal difference) {
        SharePriceAndSize priceAndSize = share2PriceAndSize.get(shareName);
        priceAndSize.setSharePrice(priceAndSize.getSharePrice().subtract(difference));

        BigDecimal wrongIndexValue = indexValue.subtract(priceAndSize.getShareSize().multiply(difference));
        BigDecimal uniformAdjustFraction = indexValue.divide(wrongIndexValue, 8, RoundingMode.UP);

        for (SharePriceAndSize component : share2PriceAndSize.values()) {
            component.setShareSize(component.getShareSize().multiply(uniformAdjustFraction));
        }
    }

}
