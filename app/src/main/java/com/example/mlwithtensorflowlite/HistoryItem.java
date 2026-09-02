package com.example.mlwithtensorflowlite;

public class HistoryItem {
    public String imageUri;
    public String label;
    public String confidenceInfo;
    public String doSummary;
    public String dontSummary;
    public String proTip;

    public HistoryItem() {} // Needed for Firestore

    public HistoryItem(String imageUri, String label, String confidenceInfo,
                       String doSummary, String dontSummary, String proTip) {
        this.imageUri = imageUri;
        this.label = label;
        this.confidenceInfo = confidenceInfo;
        this.doSummary = doSummary;
        this.dontSummary = dontSummary;
        this.proTip = proTip;
    }
}
