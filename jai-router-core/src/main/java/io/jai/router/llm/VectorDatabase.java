package io.jai.router.llm;

import java.util.List;

public interface VectorDatabase {
    void store(String id, Embedding embedding, Object payload);
    List<VectorSearchResult> searchSimilar(Embedding embedding, int topK);
}

