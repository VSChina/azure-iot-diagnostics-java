package com.microsoft.vs.diagnostic;

import com.microsoft.azure.sdk.iot.device.Message;

/**
 * Created by zhqqi on 3/29/2017.
 */
public interface IDiagnosticProvider {
    public enum SamplingRateSource
    {
        None,
        Client,
        Server
    };
    static String DIAGNOSTIC_VERSION = "0.1.0";
    static String KEY_CORRELATION_ID = "x-correlation-id";
    static String KEY_BEFORE_SEND_REQUEST = "x-before-send-request";
    static String KEY_VERSION = "x-version";
    static String KEY_TWIN_DIAG_SAMPLE_RATE = "diag_sample_rate";
    static String KEY_TWIN_DIAG_ENABLE = "diag_enable";

    public Message Process(Message message);
    public boolean NeedSampling();
    public SamplingRateSource getSamplingRateSource();
    public void setSamplingRateSource(BaseDiagnosticProvider.SamplingRateSource samplingRateSource);
    public int getSamplingRatePercentage();
    public void setSamplingRatePercentage(int samplingRatePercentage);
    public void setServerSamplingTurnedOn(boolean serverSamplingTurnedOn);
    public boolean isServerSamplingTurnedOn();
}
