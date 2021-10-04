package io.jsrminer.evaluation;

public interface PrecisionRecallProvider {
     float getPrecision();
     float getRecall();
     int getTruePositivesCount();
     int getFalsePositivesCount();
     int getFalseNegativesCount();
     int getTrueNegativesCount();
}
