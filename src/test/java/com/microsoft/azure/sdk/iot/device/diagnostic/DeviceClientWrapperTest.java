package com.microsoft.azure.sdk.iot.device.diagnostic;

import com.microsoft.azure.sdk.iot.deps.serializer.Twin;
import com.microsoft.azure.sdk.iot.deps.serializer.TwinChangedCallback;
import com.microsoft.azure.sdk.iot.device.DeviceClient;
import com.microsoft.azure.sdk.iot.device.DeviceTwin.DeviceTwin;
import com.microsoft.azure.sdk.iot.device.DeviceTwin.PropertyCallBack;
import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.device.IotHubEventCallback;
import com.microsoft.azure.sdk.iot.device.transport.mqtt.MqttTransport;
import mockit.*;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;

import static org.junit.Assert.assertEquals;

/**
 * Created by zhqqi on 3/30/2017.
 */
// HostName=iothub.device.com;DeviceId=java;SharedAccessKey=NMNxZNArh
public class DeviceClientWrapperTest {

    @Mocked
    IDiagnosticProvider mockDiagnosticProvider;

    final String DEVICE_CONNECTION_STRING = "HostName=iothub.device.com;DeviceId=java;SharedAccessKey=NMNxZNArh";

    // Constructor of Wrapper should new instance of Client
    // Only mqtt is allow in Wrapper
    @Test
    public void WrapperConstructor()
            throws URISyntaxException
    {
        new DeviceClientWrapper(DEVICE_CONNECTION_STRING,mockDiagnosticProvider);
        new Verifications()
        {
            {
                new DeviceClient(DEVICE_CONNECTION_STRING,IotHubClientProtocol.MQTT);
            }
        };
    }

    @Test
    // When connection open in Server mode, start device twin automatically
    public void startDeviceTwinOnConnectionOpenInServerMode(
            @Mocked final MqttTransport mockTransport,
            @Mocked final Twin mockTwin
    )
            throws URISyntaxException, IOException
    {
        new NonStrictExpectations()
        {
            {
                mockTransport.isEmpty();
                result = true;
            }
        };
        DeviceClientWrapper wrapper = new DeviceClientWrapper(DEVICE_CONNECTION_STRING,new ContinuousDiagnosticProvider(IDiagnosticProvider.SamplingRateSource.Server,20));
        wrapper.open();
        new Verifications()
        {
            {
                new Twin((TwinChangedCallback)any,(TwinChangedCallback)any);
            }
        };
    }

    @Test
    // When connection open in Client/None mode, do not start device twin automatically
    public void doNotStartDeviceTwinOnConnectionOpenInClientOrNoneMode(
            @Mocked final MqttTransport mockTransport,
            @Mocked final Twin mockTwin
    )
            throws URISyntaxException, IOException
    {
        new NonStrictExpectations()
        {
            {
                mockTransport.isEmpty();
                result = true;
            }
        };
        DeviceClientWrapper wrapper = new DeviceClientWrapper(DEVICE_CONNECTION_STRING,new ContinuousDiagnosticProvider(IDiagnosticProvider.SamplingRateSource.Client,20));
        wrapper.open();
        DeviceClientWrapper wrapper2 = new DeviceClientWrapper(DEVICE_CONNECTION_STRING,new ContinuousDiagnosticProvider(IDiagnosticProvider.SamplingRateSource.None,20));
        wrapper2.open();
        new Verifications()
        {
            {
                new Twin((TwinChangedCallback)any,(TwinChangedCallback)any);
                times = 0;
            }
        };
    }

    @Test
    // User will receive custom desired twin update
    public void userWillReceiveCustomDesiredTwinUpdate(
            @Mocked final MqttTransport mockTransport,
            @Mocked final IotHubEventCallback mockIHCB,
            @Mocked final PropertyCallBack mockPCB
    ) throws URISyntaxException, IOException {
        new NonStrictExpectations()
        {
            {
                mockTransport.isEmpty();
                result = true;
            }
        };

        DeviceClientWrapper wrapper = new DeviceClientWrapper(DEVICE_CONNECTION_STRING,mockDiagnosticProvider);
        wrapper.open();
        wrapper.startDeviceTwin(mockIHCB,null,mockPCB,null);

        DeviceClient dc = Deencapsulation.getField(wrapper,"deviceClient");
        DeviceTwin dt = Deencapsulation.getField(dc,"deviceTwin");
        Twin t = Deencapsulation.getField(dt,"twinObject");
        t.updateTwin("{\"desired\":{\"custom\":\"value\"}}");
        new Verifications()
        {
            {
                mockPCB.PropertyCall("custom",(Object)"value",(Object)any);
            }
        };
    }

    @Test
    // User won't received diagnostic twin update
    public void userWillNotReceiveDiagnosticTwinUpdate(
            @Mocked final MqttTransport mockTransport,
            @Mocked final IotHubEventCallback mockIHCB,
            @Mocked final PropertyCallBack mockPCB
    ) throws URISyntaxException, IOException
    {
        new NonStrictExpectations()
        {
            {
                mockTransport.isEmpty();
                result = true;
            }
        };

        DeviceClientWrapper wrapper = new DeviceClientWrapper(DEVICE_CONNECTION_STRING,mockDiagnosticProvider);
        wrapper.open();
        wrapper.startDeviceTwin(mockIHCB,null,mockPCB,null);

        DeviceClient dc = Deencapsulation.getField(wrapper,"deviceClient");
        DeviceTwin dt = Deencapsulation.getField(dc,"deviceTwin");
        Twin t = Deencapsulation.getField(dt,"twinObject");
        t.updateTwin("{\"desired\":{\"diag_enable\":\"true\",\"diag_sample_rate\":50}}");
        new Verifications()
        {
            {
                mockPCB.PropertyCall(anyString,(Object)any,(Object)any);
                times=0;
            }
        };
    }

    @Test
    // Sampling rate will not be update when source is client
    public void samplingRateWillNotBeUpdateWhenSourceIsClient(
            @Mocked final MqttTransport mockTransport,
            @Mocked final IotHubEventCallback mockEventCallback,
            @Mocked final PropertyCallBack mockPropertyCallback)
            throws URISyntaxException, IOException
    {
        new NonStrictExpectations()
        {
            {
                mockTransport.isEmpty();
                result = true;
            }
        };


        DeviceClientWrapper wrapper = new DeviceClientWrapper(DEVICE_CONNECTION_STRING, new BaseDiagnosticProvider(IDiagnosticProvider.SamplingRateSource.Client,20) {
        });
        wrapper.open();
        wrapper.startDeviceTwin(mockEventCallback,null,mockPropertyCallback,null);


        DeviceClient dc = Deencapsulation.getField(wrapper,"deviceClient");
        DeviceTwin dt = Deencapsulation.getField(dc,"deviceTwin");
        Twin t = Deencapsulation.getField(dt,"twinObject");
        final IDiagnosticProvider p = Deencapsulation.getField(wrapper,"diagnosticProvider");
        t.updateTwin("{\"desired\":{\"diag_enable\":\"true\",\"diag_sample_rate\":50}}");
        assertEquals(p.getSamplingRatePercentage(),20);
    }

    @Test
    // Sampling rate will not be update when source is none
    public void samplingRateWillNotBeUpdateWhenSourceIsNone(
            @Mocked final MqttTransport mockTransport,
            @Mocked final IotHubEventCallback mockEventCallback,
            @Mocked final PropertyCallBack mockPropertyCallback)
            throws URISyntaxException, IOException
    {
        new NonStrictExpectations()
        {
            {
                mockTransport.isEmpty();
                result = true;
            }
        };


        DeviceClientWrapper wrapper = new DeviceClientWrapper(DEVICE_CONNECTION_STRING, new BaseDiagnosticProvider(IDiagnosticProvider.SamplingRateSource.None,20) {
        });
        wrapper.open();
        wrapper.startDeviceTwin(mockEventCallback,null,mockPropertyCallback,null);


        DeviceClient dc = Deencapsulation.getField(wrapper,"deviceClient");
        DeviceTwin dt = Deencapsulation.getField(dc,"deviceTwin");
        Twin t = Deencapsulation.getField(dt,"twinObject");
        final IDiagnosticProvider p = Deencapsulation.getField(wrapper,"diagnosticProvider");
        t.updateTwin("{\"desired\":{\"diag_enable\":\"true\",\"diag_sample_rate\":50}}");
        assertEquals(p.getSamplingRatePercentage(),20);
    }

    @Test
    // Sampling rate will be update when source is server
    public void samplingRateWillBeUpdateWhenSourceIsServer(
            @Mocked final MqttTransport mockTransport)
            throws URISyntaxException, IOException
    {
        new NonStrictExpectations()
        {
            {
                mockTransport.isEmpty();
                result = true;
            }
        };


        DeviceClientWrapper wrapper = new DeviceClientWrapper(DEVICE_CONNECTION_STRING, new BaseDiagnosticProvider(IDiagnosticProvider.SamplingRateSource.Server,20) {
        });
        wrapper.open();

        DeviceClient dc = Deencapsulation.getField(wrapper,"deviceClient");
        DeviceTwin dt = Deencapsulation.getField(dc,"deviceTwin");
        Twin t = Deencapsulation.getField(dt,"twinObject");
        final IDiagnosticProvider p = Deencapsulation.getField(wrapper,"diagnosticProvider");
        t.updateTwin("{\"desired\":{\"diag_enable\":\"true\",\"diag_sample_rate\":50}}");
        assertEquals(p.getSamplingRatePercentage(),50);
    }

    @Test
    // Sampling switch will not be changed when source is none
    public void samplingWillNotBeChangedWhenSourceIsNone(
            @Mocked final MqttTransport mockTransport,
            @Mocked final IotHubEventCallback mockEventCallback,
            @Mocked final PropertyCallBack mockPropertyCallback)
            throws URISyntaxException, IOException
    {
        new NonStrictExpectations()
        {
            {
                mockTransport.isEmpty();
                result = true;
            }
        };

        DeviceClientWrapper wrapper = new DeviceClientWrapper(DEVICE_CONNECTION_STRING, new BaseDiagnosticProvider(IDiagnosticProvider.SamplingRateSource.None,100) {
        });
        wrapper.open();
        wrapper.startDeviceTwin(mockEventCallback,null,mockPropertyCallback,null);

        DeviceClient dc = Deencapsulation.getField(wrapper,"deviceClient");
        DeviceTwin dt = Deencapsulation.getField(dc,"deviceTwin");
        Twin t = Deencapsulation.getField(dt,"twinObject");
        final IDiagnosticProvider p = Deencapsulation.getField(wrapper,"diagnosticProvider");
        t.updateTwin("{\"desired\":{\"diag_enable\":\"true\",\"diag_sample_rate\":100}}");
        assertEquals(p.NeedSampling(),false);
    }

    @Test
    // Sampling switch will not be changed when source is Client
    public void samplingSwitchWillNotBeChangedWhenSourceIsClient(
            @Mocked final MqttTransport mockTransport,
            @Mocked final IotHubEventCallback mockEventCallback,
            @Mocked final PropertyCallBack mockPropertyCallback)
            throws URISyntaxException, IOException
    {
        new NonStrictExpectations()
        {
            {
                mockTransport.isEmpty();
                result = true;
            }
        };

        DeviceClientWrapper wrapper = new DeviceClientWrapper(DEVICE_CONNECTION_STRING, new BaseDiagnosticProvider(IDiagnosticProvider.SamplingRateSource.Client,100) {
        });
        wrapper.open();
        wrapper.startDeviceTwin(mockEventCallback,null,mockPropertyCallback,null);

        DeviceClient dc = Deencapsulation.getField(wrapper,"deviceClient");
        DeviceTwin dt = Deencapsulation.getField(dc,"deviceTwin");
        Twin t = Deencapsulation.getField(dt,"twinObject");
        final IDiagnosticProvider p = Deencapsulation.getField(wrapper,"diagnosticProvider");
        t.updateTwin("{\"desired\":{\"diag_enable\":\"false\"}}");
        assertEquals(p.NeedSampling(),true);
    }

    @Test
    // Sampling switch will defaultly turned off when source is Server
    public void samplingSwitchWillBeDefaultlyTurnedOffWhenSourceIsServer(
            @Mocked final MqttTransport mockTransport)
            throws URISyntaxException, IOException
    {
        new NonStrictExpectations()
        {
            {
                mockTransport.isEmpty();
                result = true;
            }
        };

        DeviceClientWrapper wrapper = new DeviceClientWrapper(DEVICE_CONNECTION_STRING, new BaseDiagnosticProvider(IDiagnosticProvider.SamplingRateSource.Server,100) {
        });
        wrapper.open();
        final IDiagnosticProvider p = Deencapsulation.getField(wrapper,"diagnosticProvider");
        assertEquals(p.NeedSampling(),false);
    }

    @Test
    // Sampling switch will be changed when source is Server
    public void samplingSwitchWillBeChangedWhenSourceIsServer(
            @Mocked final MqttTransport mockTransport)
            throws URISyntaxException, IOException
    {
        new NonStrictExpectations()
        {
            {
                mockTransport.isEmpty();
                result = true;
            }
        };

        DeviceClientWrapper wrapper = new DeviceClientWrapper(DEVICE_CONNECTION_STRING, new BaseDiagnosticProvider(IDiagnosticProvider.SamplingRateSource.Server,0) {
        });
        wrapper.open();

        DeviceClient dc = Deencapsulation.getField(wrapper,"deviceClient");
        DeviceTwin dt = Deencapsulation.getField(dc,"deviceTwin");
        Twin t = Deencapsulation.getField(dt,"twinObject");
        final IDiagnosticProvider p = Deencapsulation.getField(wrapper,"diagnosticProvider");
        t.updateTwin("{\"desired\":{\"diag_enable\":\"true\",\"diag_sample_rate\":100}}");
        assertEquals(p.NeedSampling(),true);
    }

    @Test
    // Sampling switch will turn off when twin format invalid
    public void samplingWillTurnOffWhenTwinFormatInvalid(
            @Mocked final MqttTransport mockTransport)
            throws URISyntaxException, IOException
    {
        new NonStrictExpectations()
        {
            {
                mockTransport.isEmpty();
                result = true;
            }
        };

        DeviceClientWrapper wrapper = new DeviceClientWrapper(DEVICE_CONNECTION_STRING, new BaseDiagnosticProvider(IDiagnosticProvider.SamplingRateSource.Server,0) {
        });
        wrapper.open();

        DeviceClient dc = Deencapsulation.getField(wrapper,"deviceClient");
        DeviceTwin dt = Deencapsulation.getField(dc,"deviceTwin");
        Twin t = Deencapsulation.getField(dt,"twinObject");
        final IDiagnosticProvider p = Deencapsulation.getField(wrapper,"diagnosticProvider");
        t.updateTwin("{\"desired\":{\"diag_enable\":\"true\",\"diag_sample_rate\":100}}");
        t.updateTwin("{\"desired\":{\"diag_enable\":\"tru1e\",\"diag_sample_rate\":100}}");
        assertEquals(p.NeedSampling(),false);
    }

    @Test
    // Sampling rate will set to 0 when twin format invalid
    public void samplingRateWillSetToZeroWhenTwinFormatInvalid(
            @Mocked final MqttTransport mockTransport)
            throws URISyntaxException, IOException
    {
        new NonStrictExpectations()
        {
            {
                mockTransport.isEmpty();
                result = true;
            }
        };

        DeviceClientWrapper wrapper = new DeviceClientWrapper(DEVICE_CONNECTION_STRING, new BaseDiagnosticProvider(IDiagnosticProvider.SamplingRateSource.Server,0) {
        });
        wrapper.open();

        DeviceClient dc = Deencapsulation.getField(wrapper,"deviceClient");
        DeviceTwin dt = Deencapsulation.getField(dc,"deviceTwin");
        Twin t = Deencapsulation.getField(dt,"twinObject");
        final IDiagnosticProvider p = Deencapsulation.getField(wrapper,"diagnosticProvider");
        t.updateTwin("{\"desired\":{\"diag_enable\":\"true\",\"diag_sample_rate\":20}}");
        t.updateTwin("{\"desired\":{\"diag_enable\":\"true\",\"diag_sample_rate\":101}}");
        assertEquals(p.getSamplingRatePercentage(),0);
    }

    @Test
    // Set default diagnostic provider when user does not provide one
    public void setDefaultDiagnosticProviderWhenUserDoesNotProvideOne() throws URISyntaxException {
        DeviceClientWrapper wrapper = new DeviceClientWrapper(DEVICE_CONNECTION_STRING);
        final IDiagnosticProvider p = Deencapsulation.getField(wrapper,"diagnosticProvider");
        assertEquals(p.getSamplingRateSource(), IDiagnosticProvider.SamplingRateSource.None);
        assertEquals(p.getSamplingRatePercentage(),0);
    }
}
