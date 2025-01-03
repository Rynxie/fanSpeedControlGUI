package org.fanControl;
import javax.swing.JOptionPane;

import org.eclipse.paho.client.mqttv3.*;

public class MQTTClient {

    public static MqttClient client;

    public MQTTClient(){
        try {
            client = new MqttClient("tcp://tuna.sh:1884", "JavaApp");
            client.connect();

            client.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    JOptionPane.showMessageDialog(null, "Mqtt bağlantısı kaybedildi, Sebep: " + cause.getMessage(), null, 0);
                    System.out.println("Bağlantı kaybedildi: " + cause.getMessage()); // bağlantı koparsa 
                    
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                  
                }
            });
            
        } catch (MqttException e) {
            JOptionPane.showMessageDialog(null, "Mqtt bağlantısı kurulamadı ! İnternet bağlantınızın olduğuna ya da TCP 1884 portunun yasaklı olmadığına emin olunuz", null, 0);
            System.exit(0);
        }finally{
            System.out.println("MQTT bağlantısı sağlandı");
        }
    }
}
