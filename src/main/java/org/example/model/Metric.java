package org.example.model;

public class Metric {
    private final String label;
    private final long count;
    public Metric(String label, long count) { this.label = label; this.count = count; }
    public String getLabel() { return label; }
    public long getCount() { return count; }
}
