package ru.itmo.anya.mark.model;

public class DilutionLink {
    public enum SourceType {
        SAMPLE, SOLUTION
    }

    private SourceType sourceType;
    private int sourceId;

    public DilutionLink(SourceType sourceType, int sourceId) {
        this.sourceType = sourceType;
        this.sourceId = sourceId;
    }

    public SourceType getSourceType() {
        return sourceType;
    }

    public void setSourceType(SourceType sourceType) {
        this.sourceType = sourceType;
    }

    public int getSourceId() {
        return sourceId;
    }

    public void setSourceId(int sourceId) {
        this.sourceId = sourceId;
    }

    @Override
    public String toString() {
        return "Source: " + sourceType + " #" + sourceId;
    }
}
