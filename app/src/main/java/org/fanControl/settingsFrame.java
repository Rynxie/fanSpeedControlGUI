package org.fanControl;


import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import javax.swing.*;
import java.awt.*;

public class settingsFrame extends JFrame{

        JLabel timerTitleLabel          = new JLabel("Zamanlama ayarları");
        
        JTextField minTempField         = new JTextField();
        JTextField autoTimeStartClock   = new JTextField();
        JTextField autoTimeStopClock    = new JTextField();
        JButton submitButton            = new JButton("Zaman emiri yolla");

        JLabel minTempFieldLabel        = new JLabel("Minimum sıcaklık:");
        JLabel autoTimeStartClockLabel  = new JLabel("Başlangıç saati:");
        JLabel autoTimeStopClockLabel   = new JLabel("Bitiş saati:");

        JLabel wifiTitleLabel           = new JLabel("Wifi ayarları");

        JTextField ssidField            = new JTextField(); 
        JTextField passworField         = new JTextField(); 
        JButton submitWifiButton        = new JButton("Wifi bilgilerini değiştir");

        JLabel ssidLabel                = new JLabel("SSID: ");
        JLabel passwordLabel            = new JLabel("Şifre: ");


        public settingsFrame(){

            this.setLayout(new GridBagLayout());
            this.setSize(300, 350);
            
            


            minTempField.setText("" + org.fanControl.MQTTClient.minTemp);
            submitButton.setHorizontalAlignment(SwingConstants.CENTER);
            submitButton.addActionListener(e->{
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
                try {
                    
                   
                    LocalTime.parse(autoTimeStartClock.getText(), formatter);
                    LocalTime.parse(autoTimeStopClock.getText(), formatter);

                } catch (DateTimeParseException error) {
                    JOptionPane.showMessageDialog(null, "Lütfen saatleri XX:XX şeklinde girniz. Örneğin 19:00 " + error,null,0);
                    return;
                }
                    
                MQTTClient.sendMqttPackage(org.fanControl.App.mqttThread.client, "esp/timing", minTempField.getText() + ";" + autoTimeStartClock.getText() + ";" + autoTimeStopClock.getText() + ";");
                JOptionPane.showMessageDialog(null, "Fan, " + autoTimeStartClock.getText() + "-" + autoTimeStopClock.getText() + " Saatleri arasında minimum " + minTempField.getText() + "C° sıcaklında çalışacak şekilde ayarlandı");
                
                 

                
            });

            submitWifiButton.addActionListener(e->{
                if(ssidField.getText().length() <= 10 && passworField.getText().length() <= 10){
                    MQTTClient.sendMqttPackage(org.fanControl.App.mqttThread.client, "esp/wifi", ssidField.getText() + ";" + passworField.getText());
                    JOptionPane.showMessageDialog(null,"ESP yeniden başladığında verilen bilgilere sahip WiFi ağına bağlanmaya çalışacak!");
                }else{
                    JOptionPane.showMessageDialog(null, "SSID ve Şifre 10 karakterden az olmalıdır", null, 0);
                }
            });
        
            timerTitleLabel.setHorizontalAlignment(SwingConstants.CENTER);
            wifiTitleLabel.setHorizontalAlignment(SwingConstants.CENTER);
            timerTitleLabel.setFont(new Font("Arial", Font.BOLD, 16));
            wifiTitleLabel.setFont(new Font("Arial", Font.BOLD, 16));

            
            
            
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.fill = GridBagConstraints.BOTH;
            gbc.insets = new Insets(5, 10, 5, 10);


            gbc.weightx = 1;
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.gridwidth = 2;
            gbc.gridheight = 2;

            this.add(timerTitleLabel, gbc);

            gbc.weightx = 0.3;
            gbc.gridx = 0;
            gbc.gridy = 3;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;

            this.add(minTempFieldLabel,gbc);

            gbc.weightx = 0.7;
            gbc.gridx = 1;
            gbc.gridy = 3;

            this.add(minTempField, gbc);

            gbc.weightx = 0.3;
            gbc.gridx = 0;
            gbc.gridy = 4;

            this.add(autoTimeStartClockLabel,gbc);

            gbc.weightx = 0.7;
            gbc.gridx = 1;
            gbc.gridy = 4;

            this.add(autoTimeStartClock, gbc);

            gbc.weightx = 0.3;
            gbc.gridx = 0;
            gbc.gridy = 5;

            this.add(autoTimeStopClockLabel,gbc);

            gbc.weightx = 0.7;
            gbc.gridx = 1;
            gbc.gridy = 5;

            this.add(autoTimeStopClock, gbc);

            gbc.gridx = 0;
            gbc.gridy = 6;
            gbc.gridwidth = 2;

        
            this.add(submitButton, gbc);

            gbc.gridwidth = 2;
            gbc.gridheight = 2;
            gbc.gridx = 0;
            gbc.gridy = 7;
            gbc.weightx = 1;
        
            this.add(wifiTitleLabel, gbc);

            gbc.gridheight = 1;
            gbc.gridwidth = 1;
            gbc.gridx = 0;
            gbc.gridy = 9;
            gbc.weightx = 0.3;

            this.add(ssidLabel, gbc);

            gbc.gridx = 1;
            gbc.gridy = 9;
            gbc.weightx = 0.7;

            this.add(ssidField, gbc);

            gbc.gridx = 0;
            gbc.gridy = 10;
            gbc.weightx = 0.3;

            this.add(passwordLabel, gbc);

            gbc.gridx = 1;
            gbc.gridy = 10;
            gbc.weightx = 0.7;

            this.add(passworField, gbc);

            gbc.gridx = 0;
            gbc.gridy = 11;
            gbc.gridwidth = 2;

        
            this.add(submitWifiButton, gbc);



            

            this.setVisible(true);


        }
}
