package ru.itmo.anya.mark.interpreter;

public class Environment {
    private final MeasurementParamManager paramManager;
    private final QCCheckManager checkManager;
    private final QCLimitManager limitManager;
    private final QCPlanManager planManager;
    private final SampleManager sampleManager;

    public  Environment(MeasurementParamManager paramManager,
                        QCCheckManager checkManager,
                        QCLimitManager limitManager,
                        QCPlanManager planManager,
                        SampleManager sampleManager) {
        this.paramManager = paramManager;
        this.checkManager = checkManager;
        this.limitManager = limitManager;
        this.planManager = planManager;
        this.sampleManager = sampleManager;
    }
    public MeasurementParamManager getParamManager() {
        return paramManager;
    }
    public QCCheckManager getCheckManager() {
        return checkManager;
    }
    public QCLimitManager getLimitManager() {
        return limitManager;
    }
    public QCPlanManager getPlanManager() {
        return planManager;
    }
    public SampleManager getSampleManager() {
        return sampleManager;
    }
}