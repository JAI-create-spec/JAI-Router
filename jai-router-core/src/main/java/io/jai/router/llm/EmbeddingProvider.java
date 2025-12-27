package io.jai.router.llm;

public interface EmbeddingProvider {
    Embedding embed(String text);
}

