package indexcalculator.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import indexcalculator.exception.IndexRuntimeException;
import indexcalculator.dao.DataStorage;
import indexcalculator.dto.input.AdditionAdjustment;
import indexcalculator.dto.input.DeletionAdjustment;
import indexcalculator.dto.input.DividendAdjustment;
import indexcalculator.dto.input.IndexCreationDto;
import indexcalculator.dto.output.IndexDetailsDto;
import indexcalculator.dto.input.ShareCreationDto;
import indexcalculator.model.Index;
import indexcalculator.model.SharePriceAndSize;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;

@Component
public class IndexService {

    private final DataStorage dataStorage;

    private static final Logger logger = LogManager.getLogger(IndexService.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static final int MINIMAL_SHARES_IN_INDEX = 2;

    public IndexService(@Autowired DataStorage dataStorage) {
        this.dataStorage = dataStorage;
    }

    public void createIndex(IndexCreationDto indexCreationDto) {

        String indexName = indexCreationDto.indexName();
        BigDecimal indexValue = BigDecimal.ZERO;
        HashMap<String, SharePriceAndSize> indexComponents = new HashMap<>();

        if (indexCreationDto.indexShares().size() < MINIMAL_SHARES_IN_INDEX) {
            String msg = String.format("Index `%s` contains less than two members, cannot create such an index", indexName);
            throw new IndexRuntimeException(HttpStatus.BAD_REQUEST, msg);
        }

        for (ShareCreationDto shareCreationDto : indexCreationDto.indexShares()) {
            SharePriceAndSize priceAndSize = new SharePriceAndSize(shareCreationDto.sharePrice(), shareCreationDto.numberOfShares());
            indexComponents.put(shareCreationDto.shareName(), priceAndSize);
            indexValue = indexValue.add(shareCreationDto.sharePrice().multiply(shareCreationDto.numberOfShares()));
        }

        Index index = new Index(indexName, indexComponents, indexValue);
        dataStorage.registerIndex(index);
        logger.info("Index `{}` was successfully created!", index.getIndexName());
    }

    public void adjustIndexByAddition(AdditionAdjustment additionAdjustment) {
        String indexName = additionAdjustment.indexName();
        if (dataStorage.checkIndexNotExist(indexName)) {
            String msg = String.format("Index `%s` does not exist, cannot add share to the index", indexName);
            throw new IndexRuntimeException(HttpStatus.NOT_FOUND, msg);
        }
        Index index = dataStorage.getIndexByName(indexName);
        String shareName = additionAdjustment.shareName();
        SharePriceAndSize priceAndSize = new SharePriceAndSize(additionAdjustment.sharePrice(), additionAdjustment.numberOfShares());
        if (index.checkShareExistence(shareName)) {
            String msg = String.format(
                    "Share `%s` already exists in Index `%s`, cannot add share to the index",
                    shareName, indexName
            );
            throw new IndexRuntimeException(HttpStatus.ACCEPTED, msg);
        }
        index.addShareAndAdjust(shareName, priceAndSize);
        logger.info("Index `{}` was successfully adjust after Share `{}` addition", indexName, shareName);
    }

    public void adjustIndexByDeletion(DeletionAdjustment deletionAdjustment) {
        String indexName = deletionAdjustment.indexName();
        String shareName = deletionAdjustment.shareName();

        if (dataStorage.checkIndexNotExist(indexName)) {
            String msg = String.format("Index `%s` does not exist, cannot delete `%s` from the index", indexName, shareName);
            throw new IndexRuntimeException(HttpStatus.NOT_FOUND, msg);
        }

        Index index = dataStorage.getIndexByName(indexName);

        if (!index.checkShareExistence(shareName)) {
            String msg = String.format("Index `%s` does not contain `%s`, cannot delete from the index", indexName, shareName);
            throw new IndexRuntimeException(HttpStatus.UNAUTHORIZED, msg);
        }

        if (index.getShares().size() == MINIMAL_SHARES_IN_INDEX) {
            String msg = String.format("Index `%s` contains only two members, cannot delete one of them", indexName);
            throw new IndexRuntimeException(HttpStatus.METHOD_NOT_ALLOWED, msg);
        }
        index.deleteShareAndAdjust(shareName);
        logger.info("Index `{}` was successfully adjust after Share `{}` deletion", indexName, shareName);
    }

    public void adjustIndexByDividend(DividendAdjustment dividendAdjustment) {
        String shareName = dividendAdjustment.shareName();
        BigDecimal difference = dividendAdjustment.dividendValue();

        List<Index> indexStates = dataStorage.getAllIndexStates();

        if (indexStates.isEmpty()) {
            String msg = String.format("There is no Index that contains share `%s`, cannot adjust share price", shareName);
            throw new IndexRuntimeException(HttpStatus.UNAUTHORIZED, msg);
        }

        for (Index index : dataStorage.getIndicesByShare(shareName)) {
            if (index.getSharePrice(shareName).compareTo(difference) < 1) {
                String msg = String.format(
                        "Index `%s` contains a share `%s` with a price less than a dividend value, cannot adjust share price (%s <= %s)",
                        index.getIndexName(), shareName, index.getSharePrice(shareName), difference
                );
                throw new IndexRuntimeException(HttpStatus.BAD_REQUEST, msg);
            }
        }
        for (Index index : dataStorage.getIndicesByShare(shareName)) {
            logger.info("Index `{}` was successfully adjust after Share {} dividend subtraction", index.getIndexName(), shareName);
            index.subtractDividendAndAdjust(shareName, difference);
        }
    }

    public String getIndexState() {
        try {
            IndexDetailsDto result = new IndexDetailsDto(
                    dataStorage.getAllIndexStates()
                            .stream()
                            .map(Index::makeIndexStateDto)
                            .toList()
            );
            return objectMapper.writeValueAsString(result);
        } catch (JsonProcessingException e) {
            throw new IndexRuntimeException(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error, call an IT support");
        }
    }
}
