package org.example;

import org.eclipse.paho.client.mqttv3.*;

public class readTherm extends Thread  {

    
    
    @Override
    public void run(){
        
        org.example.App.therm.setText("Hello world");


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
                        

                        org.example.App.therm.setText(new String(message.getPayload()) + "C°");
                        
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
