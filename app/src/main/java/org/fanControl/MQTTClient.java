package org.fanControl;


import org.eclipse.paho.client.mqttv3.*;
import java.nio.charset.Charset;

import javax.swing.JOptionPane;

import java.awt.Color;


public class MQTTClient extends Thread  {

    public static int minTemp = 0;
    boolean isFirstMessage = true;
    public MqttClient client;
    
    
    public static void sendMqttPackage(MqttClient client, String topic, Object data) {
        

        String payload = String.valueOf(data);
        MqttMessage message = new MqttMessage(payload.getBytes());
        message.setQos(1);
        try {
            client.publish(topic, message);
        } catch (MqttException error) {
            System.out.println("Sinyal gönderilemedi");
            return;
        } finally {
            System.out.println("Sinyal gönderildi topic --> " + topic + " mesaj --> " + payload);
        }
    }
    

    @Override
    public void run(){
        


        try {
            client = new MqttClient("tcp://tuna.sh:1884","JavaApp1"); // sunucuya bağlanılıyor 
            client.connect(); 

            System.out.println("Therm mqtt connected"); // bağlandıysa 

            while (true) {
                client.setCallback(new MqttCallback() { // interrupt gibi çalışıyor 
                    @Override
                    public void connectionLost(Throwable cause) { 
                        JOptionPane.showMessageDialog(null, "Mqtt bağlantısı kaybedildi, Sebep: " + cause.getMessage(), null, 0);
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
                            
                          
                                
                            sendMqttPackage(client, "esp/statusCheck", 0);
                            
                            org.fanControl.App.therm.setForeground(Color.decode(colorCode));
                            
                            
                        }
                        if(topic.equals("esp/statusInfo")){
                            String payload = new String(message.getPayload(), Charset.forName("UTF-8"));
                            String[] data = payload.split(";");
                            
                            if(Integer.parseInt(data[2]) == 1){
                                org.fanControl.App.changeInteractionStatus(false, false, true, true);
                                
                                org.fanControl.App.autoModCheckBox.removeActionListener(org.fanControl.App.checkBoxListener);
                                org.fanControl.App.autoModCheckBox.setSelected(true);
                                org.fanControl.App.autoModCheckBox.addActionListener(org.fanControl.App.checkBoxListener);
                               
                                


                            }else{
                                org.fanControl.App.changeInteractionStatus(false, false, false, false);

                                
                                
                            
                                
                                if (Integer.parseInt(data[0]) == 1) {
                                    org.fanControl.App.changeInteractionStatus(true, false, false, false);
                                }else{
                                    org.fanControl.App.changeInteractionStatus(false, true, false, false);
                                } 
                                    
                                

                                org.fanControl.App.slider.setValue(Integer.parseInt(data[1]));
                                minTemp = Integer.parseInt(data[3]);
                                
                                org.fanControl.App.autoModCheckBox.removeActionListener(org.fanControl.App.checkBoxListener);
                                org.fanControl.App.autoModCheckBox.setSelected(false);
                                org.fanControl.App.autoModCheckBox.addActionListener(org.fanControl.App.checkBoxListener);
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
            JOptionPane.showMessageDialog(null, "Mqtt bağlantısı kurulamadı ! İnternet bağlantınızın olduğuna ya da TCP 1884 portunun yasaklı olmadığına emin olunuz", null, 0);
            System.exit(0);
            
        }finally{
            System.out.println("MQTT bağlantısı sağlandı");
        }
    }
}
