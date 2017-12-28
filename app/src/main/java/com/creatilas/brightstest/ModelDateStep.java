package com.creatilas.brightstest;

/**
 * Created by rusci on 28-Dec-17.
 */

public class ModelDateStep {
   private String currentDate;
   private String steps;

    public ModelDateStep(String currentDate, String steps) {
        this.currentDate = currentDate;
        this.steps = steps;
    }

    public String getCurrentDate() {
        return currentDate;
    }

    public void setCurrentDate(String currentDate) {
        this.currentDate = currentDate;
    }

    public String getSteps() {
        return steps;
    }

    public void setSteps(String steps) {
        this.steps = steps;
    }
}
