package org.example;

import java.io.*;
import javax.swing.*;
import java.awt.*;
import com.formdev.flatlaf.*;
import com.formdev.flatlaf.extras.FlatAnimatedLafChange;
import org.eclipse.paho.client.mqttv3.*;

public class App {

   
    public static JLabel therm = new JLabel("Sıcaklık Buraya gelecek");
    public static chart thermChart = new chart();

    public static void main(String[] args) throws MqttException {

        // tema ayarlama
        FlatDarkLaf.setup();
        FlatAnimatedLafChange.showSnapshot();

        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception e) {
            System.err.println("Cannot load the theme: " + e.getMessage());
        }

        // mqtt sunucusuna bağlantı ayarı
        MqttClient client = new MqttClient("tcp://tuna.sh:1884", "JavaApp");
        client.connect();
        System.out.println("Connected to server");

        // pencere ayarları
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 600);
        frame.setLayout(new GridLayout(3, 1));
        frame.setUndecorated(true);

        // ızgarra hücrelerine eklenecek panellerin ayarları her biri için aytı fonksiyon tanımalndı
        JPanel topPanel = createTopPanel(client);
        JPanel midPanel = createMidPanel();
        JPanel bottomPanel = createBottomPanel(client);

        // paneller pencereye ekleniyoor
        frame.add(topPanel);
        frame.add(midPanel);
        frame.add(bottomPanel);

        frame.setVisible(true);

        // mqtt "esp/therm" kanalından sıcaklık okuması alınıp grafiğe ve anlık sıcaklık göstergesine yükleyen fonksiyon
        readTherm thread = new readTherm();
        thread.start();
    }

    private static JPanel createTopPanel(MqttClient client) {
        JPanel topPanel = new JPanel(new GridLayout(1, 2));
        
        // Sol kısım fan hızı ayarlama
        JPanel topLeftPanel = new JPanel();
        topLeftPanel.setLayout(new BoxLayout(topLeftPanel, BoxLayout.Y_AXIS));

        JSlider slider = new JSlider(0, 100, 50);
        JLabel speedLabel = new JLabel("Max Hız");
        JButton setSpeedButton = new JButton("Onayla");

        speedLabel.setFont(new Font("Arial", Font.BOLD, 24));
        speedLabel.setHorizontalAlignment(SwingConstants.CENTER);
        speedLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        slider.setAlignmentX(Component.CENTER_ALIGNMENT);

        setSpeedButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        setSpeedButton.addActionListener(e -> {
            System.out.println("Fan hızı set edildi");
            int pwmDuty = slider.getValue();
            pwmDuty = (pwmDuty * 255) / 100;
            String payload = "" + pwmDuty;
            MqttMessage message = new MqttMessage(payload.getBytes());
            message.setQos(1);
            try {
                client.publish("esp/speed", message);
            } catch (MqttException error) {
                System.out.println("Sinyal gönderilemedi");
            } finally {
                System.out.println("Sinyal gönderildi");
            }
        });

        topLeftPanel.add(Box.createRigidArea(new Dimension(0, 50)));
        topLeftPanel.add(speedLabel);
        topLeftPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        topLeftPanel.add(slider);
        topLeftPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        topLeftPanel.add(setSpeedButton);

        // Sağ kısım sıcaklık ayarlama
        JPanel topRightPanel = new JPanel(new GridLayout(1, 1));
        therm.setFont(new Font("Arial", Font.BOLD, 24));
        therm.setHorizontalAlignment(SwingConstants.CENTER);
        topRightPanel.add(therm);

        topPanel.add(topLeftPanel);
        topPanel.add(topRightPanel);
        
        return topPanel;
    }

    private static JPanel createMidPanel() {
        JPanel midPanel = new JPanel(new BorderLayout());
        midPanel.add(thermChart.chartPanel, BorderLayout.CENTER);
        return midPanel;
    }

    private static JPanel createBottomPanel(MqttClient client) {
        JPanel bottomPanel = new JPanel(new GridLayout(1, 2));

        JButton acButton = createButton("Fanı aç", "#42aa86", "1", client);
        JButton kapatButton = createButton("Fanı kapat", "#fc4f25", "0", client);

        bottomPanel.add(acButton);
        bottomPanel.add(kapatButton);

        return bottomPanel;
    }

    private static JButton createButton(String text, String color, String signal, MqttClient client) {
        JButton button = new JButton(text);
        button.setMargin(new Insets(30, 30, 30, 30));
        button.setBackground(Color.decode(color));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(true);
        button.setUI(new javax.swing.plaf.basic.BasicButtonUI());

        button.addActionListener(e -> {
            System.out.println(text + " sinyali gönderiliyor...");
            MqttMessage message = new MqttMessage(signal.getBytes());
            message.setQos(1);
            try {
                client.publish("esp/signal", message);
            } catch (MqttException error) {
                System.out.println("Sinyal gönderilemedi");
            } finally {
                System.out.println("Sinyal gönderildi");
            }
        });

        return button;
    }
}
