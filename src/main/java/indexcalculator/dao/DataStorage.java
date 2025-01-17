package indexcalculator.dao;

import indexcalculator.exception.IndexRuntimeException;
import indexcalculator.model.Index;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Component
public class DataStorage {
    private final HashMap<String, Index> indexStorage;
    private final HashMap<String, List<Index>> shareName2Indices;

    public DataStorage() {
        this.indexStorage = new HashMap<>();
        this.shareName2Indices = new HashMap<>();
    }

    public boolean checkIndexNotExist(String indexName) {
        return !indexStorage.containsKey(indexName);
    }

    synchronized public void registerIndex(Index index) {
        if (indexStorage.containsKey(index.getIndexName())) {
            String msg = String.format("Index = %s already exists, cannot execute the request", index.getIndexName());
            throw new IndexRuntimeException(HttpStatus.CONFLICT, msg);
        }
        indexStorage.put(index.getIndexName(), index);
        index.getShares().forEach(share -> {
            List<Index> indices = shareName2Indices.getOrDefault(share, new ArrayList<>());
            indices.add(index);
            shareName2Indices.put(share, indices);
        });
    }

    public Index getIndexByName(String indexName) {
        return indexStorage.get(indexName);
    }

    public List<Index> getAllIndexStates() {
        return indexStorage.values().stream().toList();
    }

    public List<Index> getIndicesByShare(String shareName) {
        return shareName2Indices.get(shareName);
    }

}
