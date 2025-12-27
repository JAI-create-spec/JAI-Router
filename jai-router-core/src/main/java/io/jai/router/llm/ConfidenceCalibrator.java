package io.jai.router.llm;

public class ConfidenceCalibrator {

    public double calibrate(double rawConfidence, String provider, String serviceId) {
        // No calibration data: return raw
        return rawConfidence;
    }
}

