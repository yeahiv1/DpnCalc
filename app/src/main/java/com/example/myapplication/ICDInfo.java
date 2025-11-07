package com.example.myapplication;

import java.util.HashMap;
import java.util.Map;

public class ICDInfo {
    String condition;
    float severity;
    String category;

    public ICDInfo(String condition, float severity, String category) {
        this.condition = condition;
        this.severity = severity;
        this.category = category;
    }

    public String getCondition() {
        return condition;
    }

    public float getSeverity() {
        return severity;
    }

    public String getCategory() {
        return category;
    }

}




