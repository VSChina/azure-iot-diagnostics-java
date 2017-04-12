package com.microsoft.azure.sdk.iot.device.diagnostic;

/**
 * Created by zhqqi on 3/29/2017.
 */
public class ContinuousDiagnosticProvider extends BaseDiagnosticProvider {
    public ContinuousDiagnosticProvider(SamplingRateSource samplingRateSource , int samplingRatePercentage) {
        super(samplingRateSource,samplingRatePercentage);
    }

    @Override
    public boolean ShouldAddDiagnosticProperties() {
        if(!super.ShouldAddDiagnosticProperties()) {
            return false;
        }
        return Math.floor((messageCounter - 2) * samplingRatePercentage / 100.0) < Math.floor((messageCounter - 1) * samplingRatePercentage / 100.0);
    }

}
