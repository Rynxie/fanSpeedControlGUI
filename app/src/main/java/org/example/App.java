package org.example;

import java.io.*;
import javax.swing.*;
import java.awt.*;

import org.eclipse.paho.client.mqttv3.*;

public class App {

    public static JLabel therm = new JLabel("Sıcaklık Buraya gelecek");
   
    public static chart thermChart = new chart();
    public static void main(String[] args) throws MqttException {
   

        MqttClient client = new MqttClient("tcp://tuna.sh:1884","JavaApp");
        client.connect();

        System.out.println("Connected to server");


        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.setSize(500,600);
        frame.setLayout(new GridLayout(3,1));
        


        JPanel topPanel = new JPanel();
        frame.add(topPanel);

        JPanel midPanel = new JPanel();
        midPanel.setLayout(new BorderLayout());
 //       therm.setFont(new Font("Arial", Font.BOLD, 48));

   //     midPanel.add(therm);
        midPanel.add(thermChart.chartPanel, BorderLayout.CENTER);
        frame.add(midPanel);
        
        
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new GridLayout(1,2));

        JButton acButton = new JButton("Fanı aç");
        JButton kapatButton = new JButton("Fanı kapat");
    

        acButton.setMargin(new Insets(30, 30, 30, 30));
        kapatButton.setMargin(new Insets(30, 30, 30, 30));

        acButton.setBackground(Color.decode("#42aa86"));
        acButton.setForeground(Color.WHITE);
        acButton.setFocusPainted(false);
        acButton.setBorderPainted(false);
        

        
        kapatButton.setBackground(Color.decode("#fc4f25"));
        kapatButton.setForeground(Color.WHITE);
        kapatButton.setFocusPainted(false);
        kapatButton.setBorderPainted(false);
        

        acButton.setContentAreaFilled(false);
        acButton.setOpaque(true);
        
        kapatButton.setContentAreaFilled(false);
        kapatButton.setOpaque(true);

        acButton.addActionListener(e -> {
            System.out.println("Fan acma sinyali gönderiliyor...");
            MqttMessage message = new MqttMessage("1".getBytes());
            message.setQos(1);
            try{
                
                client.publish("esp/signal",message);

            }catch(MqttException error){
                System.out.println("Sinyal gönderilemedi");
            }finally{
                System.out.println("Sinyal gönderildi");
            }
        });
        
        kapatButton.addActionListener(e -> {
            System.out.println("Fan kapat sinyali gönderiliyor...");
            MqttMessage message = new MqttMessage("0".getBytes());
            message.setQos(1);
            try{
                
                client.publish("esp/signal",message);

            }catch(MqttException error){
                System.out.println("Sinyal gönderilemedi");
            }finally{
                System.out.println("Sinyal gönderildi");
            }
        });
        bottomPanel.add(acButton);
        bottomPanel.add(kapatButton);

        frame.add(bottomPanel);
        
        frame.setVisible(true);

        readTherm thread = new readTherm();
        thread.start(); // Start the thread
    }
}
