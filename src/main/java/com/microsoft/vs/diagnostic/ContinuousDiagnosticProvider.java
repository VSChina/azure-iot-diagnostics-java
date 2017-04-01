package com.microsoft.vs.diagnostic;

/**
 * Created by zhqqi on 3/29/2017.
 */
public class ContinuousDiagnosticProvider extends BaseDiagnosticProvider {
    private int counter = 0;

    public ContinuousDiagnosticProvider(SamplingRateSource samplingRateSource , int samplingRatePercentage) {
        super(samplingRateSource,samplingRatePercentage);
    }

    @Override
    public boolean NeedSampling() {
        if(!super.NeedSampling()) {
            return false;
        }
        counter = counter + samplingRatePercentage;
        if(counter >= 100) {
            counter = 0;
            return true;
        }
        return false;
    }
}
