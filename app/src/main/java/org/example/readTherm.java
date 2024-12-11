package org.example;

import org.eclipse.paho.client.mqttv3.*;
import java.nio.charset.Charset;


public class readTherm extends Thread  {

    
    
    @Override
    public void run(){
        


        try {
            MqttClient client = new MqttClient("tcp://tuna.sh:1884","JavaApp1");
            client.connect();

            System.out.println("Therm mqtt connected");

            while (true) {
                client.setCallback(new MqttCallback() {
                    @Override
                    public void connectionLost(Throwable cause) {
                        System.out.println("Bağlantı kaybedildi: " + cause.getMessage());
                    }
    
                    @Override
                    public void messageArrived(String topic, MqttMessage message) throws Exception {
                        

 //                       org.example.App.therm.setText(new String(message.getPayload()) + "C°");
                        String payload = new String(message.getPayload(), Charset.forName("UTF-8"));
                        int value = Integer.parseInt(payload);
                        org.example.App.thermChart.updateData(value);
                    }
    
                    @Override
                    public void deliveryComplete(IMqttDeliveryToken token) {
                        
                    }
                });
    
                
                client.subscribe("esp/therm");
            }


        } catch (MqttException e) {
            
        }
    }
}
