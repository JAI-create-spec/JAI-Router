package io.jai.router.llm;

public class Embedding {
    private final float[] vector;

    public Embedding(float[] vector) { this.vector = vector; }
    public float[] getVector() { return vector; }
}

