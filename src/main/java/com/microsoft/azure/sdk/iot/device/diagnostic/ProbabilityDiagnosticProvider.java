package com.microsoft.azure.sdk.iot.device.diagnostic;

import java.util.Random;

/**
 * Created by zhqqi on 3/31/2017.
 */
public class ProbabilityDiagnosticProvider extends BaseDiagnosticProvider {
    private Random rand;

    public ProbabilityDiagnosticProvider() {
        super();
        rand = new Random();
    }

    public ProbabilityDiagnosticProvider(IDiagnosticProvider.SamplingRateSource samplingRateSource) {
        super(samplingRateSource);
        rand = new Random();
    }

    public ProbabilityDiagnosticProvider(IDiagnosticProvider.SamplingRateSource samplingRateSource , int samplingRatePercentage) {
        super(samplingRateSource,samplingRatePercentage);
        rand = new Random();
    }

    @Override
    public boolean ShouldAddDiagnosticProperties() {
        if(!super.ShouldAddDiagnosticProperties()) {
            return false;
        }
        int n = rand.nextInt(100) + 1; //1-100
        return n<=samplingRatePercentage;
    }
}
