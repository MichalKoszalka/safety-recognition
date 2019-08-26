package com.safety.recognition.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import org.nd4j.evaluation.regression.RegressionEvaluation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EvaluationMetricsManager {

    private final MeterRegistry meterRegistry;

    @Autowired
    public EvaluationMetricsManager(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public void updateEvaluationMetrics(RegressionEvaluation evaluation) {
        meterRegistry.gauge("prediction.MSE", Double.valueOf(evaluation.scoreForMetric(RegressionEvaluation.Metric.MSE)));
        meterRegistry.gauge("prediction.MAE", Double.valueOf(evaluation.scoreForMetric(RegressionEvaluation.Metric.MAE)));
        meterRegistry.gauge("prediction.RMSE", Double.valueOf(evaluation.scoreForMetric(RegressionEvaluation.Metric.RMSE)));
        meterRegistry.gauge("prediction.RSE", Double.valueOf(evaluation.scoreForMetric(RegressionEvaluation.Metric.RSE)));
        meterRegistry.gauge("prediction.PC", Double.valueOf(evaluation.scoreForMetric(RegressionEvaluation.Metric.PC)));
        meterRegistry.gauge("prediction.R^2", Double.valueOf(evaluation.scoreForMetric(RegressionEvaluation.Metric.R2)));
    }

}
