package org.example;

import org.eclipse.paho.client.mqttv3.*;
import java.nio.charset.Charset;


public class readTherm extends Thread  {

    
    
    @Override
    public void run(){
        


        try {
            MqttClient client = new MqttClient("tcp://tuna.sh:1884","JavaApp1"); // sunucuya bağlanılıyor 
            client.connect(); 

            System.out.println("Therm mqtt connected"); // bağlandıysa 

            while (true) {
                client.setCallback(new MqttCallback() { // interrupt gibi çalışıyor 
                    @Override
                    public void connectionLost(Throwable cause) { 
                        System.out.println("Bağlantı kaybedildi: " + cause.getMessage()); // bağlantı koparsa 
                    }
    
                    @Override
                    public void messageArrived(String topic, MqttMessage message) throws Exception { // sunucudan mesaj gelirse 
                        

                        org.example.App.therm.setText(new String(message.getPayload()) + "C°"); // sıcaklık buraya gel yazısını eğer sunucudan veri gelirse değiştiriyor 
                        String payload = new String(message.getPayload(), Charset.forName("UTF-8")); // sunucudan gelen mesajı stringe çeviriyor
                        int value = Integer.parseInt(payload); // stringi integera çeviriyor 
                        org.example.App.thermChart.updateData(value); // grafiğin değerlerini güncelliyor  
                    }
    
                    @Override
                    public void deliveryComplete(IMqttDeliveryToken token) { // mesaj gönderilirse tetikleniyor ama biz kullanmıyoruz, kütüphane izin vermiyor silmemize 
                        
                    }
                });
    
                
                client.subscribe("esp/therm"); // mqtt'nin esp/therm kanalına abone oluyor- bu kanalan bir veri gelirse alıyor
            }


        } catch (MqttException e) { 
        // kütüphane try catch içinde yazılmazsa hata veriyor 
            
        }
    }
}
