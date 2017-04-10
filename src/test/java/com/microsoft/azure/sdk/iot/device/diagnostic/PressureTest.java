package com.microsoft.azure.sdk.iot.device.diagnostic;

import com.microsoft.azure.sdk.iot.device.DeviceTwin.Device;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhqqi on 4/10/2017.
 */
public class PressureTest {
    public static void main(String[] args) throws IOException, URISyntaxException {
        System.out.println("started");
        List<String> css = getDeviceConnectionStringsFromFile("C:\\Users\\v-zhq\\Desktop\\devicelist.txt");
        System.out.println("get css completed");
        int count = 0;
        for(String cs : css) {
            Thread t = new Thread(new DeviceThread(cs,count));
            t.start();
            System.out.println("Thread #"+count+" created");
            count++;
        }
        System.in.read();
    }

    public static List<String> getDeviceConnectionStringsFromFile(String path) throws IOException {
        List<String> result = new ArrayList<String>();
        int limit = 5;
        try(BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line = br.readLine();

            while (line != null) {
                result.add(line);
                limit -- ;
                if(limit == 0) return result;
                line = br.readLine();
            }
        }
        return result;
    }
}

class DeviceThread extends Thread {
    String cs;
    int index;
    DeviceClientWrapper d;
    public DeviceThread(String cs,int index){
        this.cs = cs;
        this.index = index;
    }
    public void run() {
        try {
            d = new DeviceClientWrapper(cs,new ContinuousDiagnosticProvider(IDiagnosticProvider.SamplingRateSource.Server,20));
            d.open();
            System.out.println("#"+index+" opened");
            d.close();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}