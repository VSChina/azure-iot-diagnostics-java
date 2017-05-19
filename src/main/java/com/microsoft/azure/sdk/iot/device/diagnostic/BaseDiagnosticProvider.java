package com.microsoft.azure.sdk.iot.device.diagnostic;

import com.microsoft.azure.sdk.iot.device.Message;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.UUID;

/**
 * Created by zhqqi on 3/23/2017.
 */
public abstract class BaseDiagnosticProvider implements IDiagnosticProvider {
    protected IDiagnosticProvider.SamplingRateSource samplingRateSource;
    protected int samplingRatePercentage;
    protected int messageNumber;
    private SimpleDateFormat simpleDateFormat;

    public boolean isServerSamplingTurnedOn() {
        return serverSamplingTurnedOn;
    }

    public void setServerSamplingTurnedOn(boolean serverSamplingTurnedOn) {
        this.serverSamplingTurnedOn = serverSamplingTurnedOn;
    }

    protected boolean serverSamplingTurnedOn;

    public IDiagnosticProvider.SamplingRateSource getSamplingRateSource() {
        return samplingRateSource;
    }

    public void setSamplingRateSource(SamplingRateSource samplingRateSource) {
        this.samplingRateSource = samplingRateSource;
    }

    public int getSamplingRatePercentage() {
        return samplingRatePercentage;
    }

    public void setSamplingRatePercentage(int samplingRatePercentage) {
        this.samplingRatePercentage = samplingRatePercentage;
    }

    public BaseDiagnosticProvider() {
        this(SamplingRateSource.None,0);
    }

    public BaseDiagnosticProvider(SamplingRateSource samplingRateSource)
    {
        this(samplingRateSource,0);
    }

    public BaseDiagnosticProvider(SamplingRateSource samplingRateSource , int samplingRatePercentage) throws IllegalArgumentException
    {
        if(samplingRatePercentage > 100 || samplingRatePercentage < 0) {
            throw new IllegalArgumentException("Invalid percentage value");
        }
        this.samplingRateSource = samplingRateSource;
        this.samplingRatePercentage = samplingRatePercentage;
        this.serverSamplingTurnedOn = false;
        this.messageNumber = 0;
        simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    public boolean ShouldAddDiagnosticProperties() {
        if(samplingRateSource == SamplingRateSource.None) {
            return false;
        }
        if(samplingRateSource == SamplingRateSource.Server && !serverSamplingTurnedOn) {
            return false;
        }
        return true;
    }

    @Override
    public final Message Process(Message message) {
        messageNumber++;
        if(!ShouldAddDiagnosticProperties()) {
            return message;
        }

        if(message.getProperty(KEY_CORRELATION_ID) != null) {
            throw new IllegalArgumentException("Property " + KEY_CORRELATION_ID+" is reserved.");
        }

        if(message.getProperty(KEY_BEFORE_SEND_REQUEST) != null) {
            throw new IllegalArgumentException("Property " + KEY_BEFORE_SEND_REQUEST+" is reserved.");
        }

        if(message.getProperty(KEY_VERSION) != null) {
            throw new IllegalArgumentException("Property " + KEY_VERSION+" is reserved.");
        }

        // add condition
        message.setProperty(KEY_CORRELATION_ID, UUID.randomUUID().toString());

        message.setProperty(KEY_BEFORE_SEND_REQUEST, simpleDateFormat.format(new Date()));
        message.setProperty(KEY_VERSION, DIAGNOSTIC_VERSION);
        return message;
    }

}
