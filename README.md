# azure-iot-diagnostics-java
Azure IoT Hub Java Device SDK with End-to-end Diagnostic library provides a convenient way to send diagnostic messages for IoT devices.

### Usage of End-to-End diagnostic Java SDK

```java
        // Random Diagnostic Sampling: sampling rate is based on user settings.
        ProbabilityDiagnosticProvider diagnosticProvider = new ProbabilityDiagnosticProvider(IDiagnosticProvider.SamplingRateSource.Client, 50);
        DeviceClientWrapper deviceClient = new DeviceClientWrapper(deviceConnectionString, diagnosticProvider);

        // Periodic Diagnostic Sampling
        ContinuousDiagnosticProvider diagnosticProvider = new ContinuousDiagnosticProvider(IDiagnosticProvider.SamplingRateSource.Client, 50);
        DeviceClientWrapper deviceClient = new DeviceClientWrapper(deviceConnectionString, diagnosticProvider);

        // You can also set SamplingRateSource.Server to obtain settings from device twin
        ProbabilityDiagnosticProvider diagnosticProvider = new ProbabilityDiagnosticProvider(IDiagnosticProvider.SamplingRateSource.Server);
        DeviceClientWrapper deviceClient = new DeviceClientWrapper(deviceConnectionString, diagnosticProvider);

        // Disable diagnostic
        DeviceClientWrapper deviceClient = new DeviceClientWrapper(deviceConnectionString);

```

### A quick guide to use End-to-End diagnostic Java SDK
1. Ensure we already have an IoT project and it is a maven project.Here we provide a simple example which will send 10 messages to IoT Hub. ( Replace `YOUR DEVICE CONNECTION STRING` with correct device Connection String, the  Connection String could be found from Azure IoT Hub -> Devices -> select `your device name ` -> Connection string in right panel )

```java
    protected static class EventCallback implements IotHubEventCallback{
        public void execute(IotHubStatusCode status, Object context){
            Integer i = (Integer) context;
            System.out.println("IoT Hub responded to message "+i.toString()
                    + " with status " + status.name());
        }
    }

    public static void main(String[] args)
            throws IOException, URISyntaxException
    {
        String connString = "<YOUR DEVICE CONNECTION STRING>";
        DeviceClient client = new DeviceClient(connString,IotHubClientProtocol.MQTT);

        long time = 2400;
        client.setOption("SetSASTokenExpiryTime", time);

        client.open();

        for (int i = 0; i < 10; ++i)
        {
            String msgStr = "Event Message " + Integer.toString(i);
            try
            {
                Message msg = new Message(msgStr);
                msg.setMessageId(java.util.UUID.randomUUID().toString());
                msg.setExpiryTime(5000);
                System.out.println(msgStr);
                EventCallback cb = new EventCallback();
                client.sendEventAsync(msg, cb, i);
            } catch (Exception e)
            {
                e.printStackTrace(); // Trace the exception
            }
            try
            {
                Thread.sleep(2000);
            } catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }

        System.out.println("Press any key to exit...");

        Scanner scanner = new Scanner(System.in);
        scanner.nextLine();

        client.close();
    }
```
2. Edit your pom.xml file, add following dependencies. Here replace the `version` with the latest version number, you can find the number in [Myget Repository](https://www.myget.org/feed/azure-iot-device-diagnostic/package/maven/com.microsoft.azure.sdk.iot.device.diagnostic/azure-iot-diagnostics-java)
```xml
<dependencies> 
  <dependency> 
    <groupId>com.microsoft.azure.sdk.iot.device.diagnostic</groupId> 
    <artifactId>azure-iot-diagnostics-java</artifactId> 
    <version>1.0.5</version> 
  </dependency> 
</dependencies> 
<repositories> 
    <repository> 
      <id>MyGet</id> 
      <url>https://www.myget.org/F/azure-iot-device-diagnostic/maven</url> 
    </repository> 
</repositories> 
```

3. Run `mvn package` to fix maven dependencies.

4. In your existing project, find the places where DeviceClient is constructed. Add import at the beginning. 

Replace the construct of DeviceClient to DeviceClientWrapper:
```java
DeviceClient client = new DeviceClient(connString, protocol); // Original

DeviceClientWrapper client = new DeviceClientWrapper(connString,diagnosticProvider); // Replace constructor of DeviceClient with DeviceClientWrapper

ContinuousDiagnosticProvider diagnosticProvider = new ContinuousDiagnosticProvider(IDiagnosticProvider.SamplingRateSource.Client, 50); // Periodic Diagnostic Sampling: sampling rate is based on user settings.
ProbabilityDiagnosticProvider diagnosticProvider = new ProbabilityDiagnosticProvider(IDiagnosticProvider.SamplingRateSource.Client, 50); // Random Diagnostic Sampling
ProbabilityDiagnosticProvider diagnosticProvider = new ProbabilityDiagnosticProvider(IDiagnosticProvider.SamplingRateSource.Server); // Obtain settings from cloud (device twin)
```

Import at the beginning
```java
import com.microsoft.azure.sdk.iot.device.diagnostic.*;
```
5. Build and run your project.




### API Reference

### DeviceClientWrapper
----
This class is used to create diagnostic device client which has similar interface with [Microsoft.Azure.Devices.Client SDK](https://azure.github.io/azure-iot-sdk-java/device/) 

```java
public DeviceClientWrapper (string connectionString)
public DeviceClientWrapper (string connectionString, IDiagnosticProvider diagnosticProvider)
```

`DeviceClientWrapper` object which has similar interface with Microsoft.Azure.Devices.Client SDK

`connectionString` is the connection string which can be obtain from Azure IoT Hub -> Devices -> select `your device name` -> Connection string in right panel

`diagnosticProvider` is the DiagnosticProvider object which used to set diagnostic configurations


### ContinuousDiagnosticProvider
----
This class provide a settings for continuous interval sampling, which means the diagnostic messages appear periodic and are uniform distributed in user's messages

```java
public ContinuousDiagnosticProvider() // source:SamplingRateSource.None , samplingRate:0
public ContinuousDiagnosticProvider(SamplingRateSource source) // samplingRate:0
public ContinuousDiagnosticProvider(SamplingRateSource source , int samplingRate)
```

`SamplingRateSource` can be set to SamplingRateSource.None/SamplingRateSource.Client/SamplingRateSource.Server, SamplingRateSource.None means do not send diagnostic message, SamplingRateSource.Client means sampling rate is based on local user settings, SamplingRateSource.Server means sampling rate is based on IoTHub device Twin settings

`samplingRate` can be set from 0 to 100, which means the sampling percentage of user message, 0 means do not insert diagnostic information to user's messages


### ProbabilityDiagnosticProvider
----
This class provide a settings for random sampling, which means the diagnostic message appear randomly and are random distributed in user's message

```java
public ProbabilityDiagnosticProvider() // source:SamplingRateSource.None , samplingRate:0
public ProbabilityDiagnosticProvider(SamplingRateSource source) // samplingRate:0
public ProbabilityDiagnosticProvider(SamplingRateSource source , int samplingRate)
```

`SamplingRateSource` can be set to SamplingRateSource.None/SamplingRateSource.Client/SamplingRateSource.Server, SamplingRateSource.None means do not send diagnostic message, SamplingRateSource.Client means sampling rate is based on local user settings, SamplingRateSource.Server means sampling rate is based on IoTHub device Twin settings

`samplingRate` can be set from 0 to 100, which means the sampling percentage of user message, 0 means do not insert diagnostic information to user's messages



### Related project
#### C# Diagnostic SDK UWP Demos
https://github.com/VSChina/win10-iot-core-diagnostic-app

#### C# Diagnostic SDK
https://github.com/VSChina/azure-iot-diagnostics-csharp
