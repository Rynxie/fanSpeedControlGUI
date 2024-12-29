package org.fanControl;

import org.eclipse.paho.client.mqttv3.*;
import java.nio.charset.Charset;

import java.awt.Color;


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
                        

                        if(topic.equals("esp/therm")){
                            org.fanControl.App.therm.setText(new String(message.getPayload()) + "C°"); // sıcaklık buraya gel yazısını eğer sunucudan veri gelirse değiştiriyor 
                            
                            String payload = new String(message.getPayload(), Charset.forName("UTF-8")); // sunucudan gelen mesajı stringe çeviriyor
                            int value = Integer.parseInt(payload); // stringi integera çeviriyor 
                            org.fanControl.App.thermChart.updateData(value); // grafiğin değerlerini güncelliyor  
                            String colorCode = new String(); 
                            if(value < 20){
                            colorCode = "#25fccb";
                            }else if(value < 25){
                                colorCode = "#ffcd76";
                            }else{
                                colorCode = "#ff7676";
                            }

                            org.fanControl.App.therm.setForeground(Color.decode(colorCode));
                            org.fanControl.App.sendMqttPackage(client, "esp/statusCheck", 0);
                            
                        }
                        if(topic.equals("esp/statusInfo")){
                            String payload = new String(message.getPayload(), Charset.forName("UTF-8"));
                            String[] data = payload.split(";");
                            
                            if(Integer.parseInt(data[2]) == 1){
                                org.fanControl.App.acButton.setEnabled(false);
                                org.fanControl.App.kapatButton.setEnabled(false);
                                org.fanControl.App.slider.setEnabled(false);

                                org.fanControl.App.autoModCheckBox.setSelected(true);
                            }else{
                                org.fanControl.App.acButton.setEnabled(true);
                                org.fanControl.App.kapatButton.setEnabled(true);
                                org.fanControl.App.slider.setEnabled(true);
                                
                                org.fanControl.App.autoModCheckBox.setSelected(false);
                            }
                        }

                    }
    
                    @Override
                    public void deliveryComplete(IMqttDeliveryToken token) { // mesaj gönderilirse tetikleniyor ama biz kullanmıyoruz, kütüphane izin vermiyor silmemize 
                        
                    }
                });
    
                
                client.subscribe("esp/therm"); // mqtt'nin esp/therm kanalına abone oluyor- bu kanalan bir veri gelirse alıyor
                client.subscribe("esp/statusInfo");
            }


        } catch (MqttException e) { 
        // kütüphane try catch içinde yazılmazsa hata veriyor 
            
        }
    }
}
