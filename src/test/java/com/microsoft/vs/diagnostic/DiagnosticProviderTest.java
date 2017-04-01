package com.microsoft.vs.diagnostic;

import com.microsoft.azure.sdk.iot.device.Message;

import mockit.*;
import org.bouncycastle.jcajce.provider.symmetric.ARC4;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by zhqqi on 3/31/2017.
 */
public class DiagnosticProviderTest {
    @Test
    // Properties added rightly in process message
    public void propertiesAddedRightlyInProcessMessage(

            )
    {
        BaseDiagnosticProvider b = new BaseDiagnosticProvider() {
            @Override
            public boolean NeedSampling() {
                return true;
            }
        };
        Message m = new Message("body");
        m = b.Process(m);
        assertNotEquals(m.getProperty(IDiagnosticProvider.KEY_BEFORE_SEND_REQUEST),null);
        assertNotEquals(m.getProperty(IDiagnosticProvider.KEY_CORRELATION_ID),null);
        assertNotEquals(m.getProperty(IDiagnosticProvider.KEY_VERSION),null);
    }

    @Test (expected = IllegalArgumentException.class)
    // Do not allow user to set retained diagnostic properties
    public void doNotAllowUserToSetDiagnosticProperties()
    {
        BaseDiagnosticProvider b = new BaseDiagnosticProvider() {
            @Override
            public boolean NeedSampling() {
                return true;
            }
        };
        Message m = new Message("body");
        m.setProperty(IDiagnosticProvider.KEY_BEFORE_SEND_REQUEST,"any");
        m = b.Process(m);
    }

    @Test (expected = IllegalArgumentException.class)
    // Sampling percentage cannot out of range 0-100
    public void samplingPercentageCanNotOutOfRange()
    {
        BaseDiagnosticProvider b = new BaseDiagnosticProvider(IDiagnosticProvider.SamplingRateSource.Client,-1) {
            @Override
            public boolean NeedSampling() {
                return false;
            }
        };
    }

    @Test
    // Do not sampling when source is none
    public void doNotSamplingWhenSourceIsNone()
    {
        ContinuousDiagnosticProvider c = new ContinuousDiagnosticProvider(IDiagnosticProvider.SamplingRateSource.None,50);
        ProbabilityDiagnosticProvider p = new ProbabilityDiagnosticProvider(IDiagnosticProvider.SamplingRateSource.None,50);
        int count = 0;
        for(int i = 0;i<100;i++) {
            if(c.NeedSampling()) count++;
            if(p.NeedSampling()) count++;
        }
        assertEquals(count,0);
    }

    @Test
    // Do not sampling when needsample is false
    public void doNotSamplingWhenNeedSampleIsFalse()
    {
        BaseDiagnosticProvider b = new BaseDiagnosticProvider() {
            @Override
            public boolean NeedSampling() {
                return false;
            }
        };
        Message m = new Message("body");
        m = b.Process(m);
        assertEquals(m.getProperty(IDiagnosticProvider.KEY_BEFORE_SEND_REQUEST),null);
        assertEquals(m.getProperty(IDiagnosticProvider.KEY_CORRELATION_ID),null);
        assertEquals(m.getProperty(IDiagnosticProvider.KEY_VERSION),null);
    }

    @Test
    // Test continuous sampling rate 0
    public void continuousSamplingRate0()
    {
        ContinuousDiagnosticProvider c = new ContinuousDiagnosticProvider(IDiagnosticProvider.SamplingRateSource.Client,0);
        Message[] ms = new Message[100];
        int count = 0;
        for(Message m : ms)
        {
            m = new Message();
            m = c.Process(m);
            if(m.getProperty(IDiagnosticProvider.KEY_BEFORE_SEND_REQUEST)!= null) {
                count++;
            }
        }
        assertEquals(count,0);
    }

    @Test
    // Test continuous sampling rate 100
    public void continuousSamplingRate100()
    {
        ContinuousDiagnosticProvider c = new ContinuousDiagnosticProvider(IDiagnosticProvider.SamplingRateSource.Client,100);
        Message[] ms = new Message[100];
        int count = 0;
        for(Message m : ms)
        {
            m = new Message();
            m = c.Process(m);
            if(m.getProperty(IDiagnosticProvider.KEY_BEFORE_SEND_REQUEST)!= null) {
                count++;
            }
        }
        assertEquals(count,100);
    }

    @Test
    // Test continuous sampling rate 20,25,50
    public void continuousSamplingRateOther()
    {
        int[] rates = {20,25,50};
        for(int rate : rates)
        {
            ContinuousDiagnosticProvider c = new ContinuousDiagnosticProvider(IDiagnosticProvider.SamplingRateSource.Client,rate);
            Message[] ms = new Message[100];
            int count = 0;
            for(Message m : ms)
            {
                m = new Message();
                m = c.Process(m);
                if(m.getProperty(IDiagnosticProvider.KEY_BEFORE_SEND_REQUEST)!= null) {
                    count++;
                }
            }
            assertEquals(count,rate);

        }
    }

    @Test
    // Test probability sampling rate 0
    public void probabilitySamplingRate0()
    {
        ProbabilityDiagnosticProvider c = new ProbabilityDiagnosticProvider(IDiagnosticProvider.SamplingRateSource.Client,0);
        Message[] ms = new Message[100];
        int count = 0;
        for(Message m : ms)
        {
            m = new Message();
            m = c.Process(m);
            if(m.getProperty(IDiagnosticProvider.KEY_BEFORE_SEND_REQUEST)!= null) {
                count++;
            }
        }
        assertEquals(count,0);
    }

    @Test
    // Test probability sampling rate 100
    public void probabilitySamplingRate100()
    {
        ProbabilityDiagnosticProvider c = new ProbabilityDiagnosticProvider(IDiagnosticProvider.SamplingRateSource.Client,100);
        Message[] ms = new Message[100];
        int count = 0;
        for(Message m : ms)
        {
            m = new Message();
            m = c.Process(m);
            if(m.getProperty(IDiagnosticProvider.KEY_BEFORE_SEND_REQUEST)!= null) {
                count++;
            }
        }
        assertEquals(count,100);
    }

    @Test
    // Test probability sampling rate 20,25,50
    public void probabilitySamplingRateOther()
    {
        int[] rates = {20,25,50};
        for(int rate: rates) {
            ProbabilityDiagnosticProvider c = new ProbabilityDiagnosticProvider(IDiagnosticProvider.SamplingRateSource.Client, rate);
            final int num = 1000;
            int count = 0;
            for(int i = 0;i<num*100;i++) {
                if(c.NeedSampling()) count++;
            }
            int high = ((Double)(1.1*rate*num)).intValue();
            int low = ((Double)(0.9*rate*num)).intValue();
            System.out.println(String.format("count:%d in [%d,%d],%f%%",count,low,high,(double)count*100/(rate*num)));
            assertTrue(count<=high && count>=low);
        }
    }

    @Test
    // Do not sampling when source is service and no twin received
    public void doNotSamplingWhenSourceIsServiceAndNoTwinReceived()
    {
        ContinuousDiagnosticProvider c = new ContinuousDiagnosticProvider(IDiagnosticProvider.SamplingRateSource.Server,50);
        ProbabilityDiagnosticProvider p = new ProbabilityDiagnosticProvider(IDiagnosticProvider.SamplingRateSource.Server,50);
        int count = 0;
        for(int i = 0;i<100;i++) {
            if(c.NeedSampling()) count++;
            if(p.NeedSampling()) count++;
        }
        assertEquals(count,0);
    }
}
