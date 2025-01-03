package org.fanControl;

import java.io.*;

import javax.print.DocFlavor.STRING;
import javax.swing.*; //arayüzü yaptığımız kütüphane 
import java.awt.*;
import java.awt.event.ActionListener;
import com.formdev.flatlaf.*;
import com.formdev.flatlaf.extras.FlatAnimatedLafChange;
import org.eclipse.paho.client.mqttv3.*;

public class App {

    public static JLabel therm = new JLabel("ESP offline...");// espyi takmadan önceki yazı 
    public static chart thermChart = new chart(); // grafik 
    public static JButton acButton;
    public static JButton kapatButton;
    public static JSlider slider;
    public static JCheckBox autoModCheckBox;
    public static ActionListener checkBoxListener;
    public static MqttClient client;
 

    public static void main(String[] args){

        // tema ayarlama
        FlatDarkLaf.setup();
        FlatAnimatedLafChange.showSnapshot(); // butonların renklerini vs değiştiriyor, sliderin şeklini değiştiriyor

        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
        } catch (Exception e) {
            System.err.println("Tema yuklenemedi " + e.getMessage()); // eğer eklenemezse hata veriyor 
        }

        // mqtt sunucusuna bağlantı ayarı
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

        // pencere ayarları
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // pencere kapatıldığında programı tamamen kapat, bu koyulmazsa arka planda program çalışmaya devam ediyor
        frame.setSize(500, 600); // pencerenin boyutu
        frame.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;

        // ızgara hücrelerine eklenecek panellerin ayarları her biri için ayrı fonksiyon tanımlandı
        JPanel topPanel = createTopPanel(client);
        JPanel midPanel = createMidPanel();
        JPanel bottomPanel = createBottomPanel(client);

        // panelleri pencereye ekliyoruz
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weighty = 0.1;
        frame.add(topPanel, gbc);

        gbc.gridy = 1;
        gbc.weighty = 0.7;
        frame.add(midPanel, gbc);

        gbc.gridy = 2;
        gbc.weighty = 0.2;
        frame.add(bottomPanel, gbc); // pencereyi görünür yapıyor
        changeInteractionStatus(false,false,false,true);
        frame.setVisible(true);
        // mqtt "esp/therm" kanalından sıcaklık okuması alınıp grafiğe ve anlık sıcaklık göstergesine yükleyen fonksiyon
        readTherm thread = new readTherm(); // thread'in açılma sebebi aynı anda mqtt üzerinden gelen sıcaklık verisinin okunması gerek hem de pencereyi açıyor iki işi aynı anda yapması için thread kullanılıyor 
        thread.start();
    }
    public static void changeInteractionStatus(boolean enableAcButton, boolean enableKapatButton, boolean enableAutoBox, boolean disableAll){

        acButton.setEnabled(!disableAll);
        kapatButton.setEnabled(!disableAll);
        slider.setEnabled(!disableAll);
        autoModCheckBox.setEnabled(!disableAll);
        
        acButton.setBackground(Color.decode("#c9b5b5"));
        kapatButton.setBackground(Color.decode("#c9b5b5"));

       if(enableAcButton){
            acButton.setBackground(Color.decode("#c9b5b5"));
            kapatButton.setBackground(Color.decode("#fc4f25"));
            kapatButton.setEnabled(true);
            acButton.setEnabled(false);
       }
       if (enableKapatButton) {
            
            acButton.setBackground(Color.decode("#42aa86"));
            kapatButton.setBackground(Color.decode("#c9b5b5"));
            kapatButton.setEnabled(false);
            acButton.setEnabled(true);

       }
       if(enableAutoBox){
        autoModCheckBox.setEnabled(true);
       }

/* 
        acButton.setEnabled(status);
        kapatButton.setEnabled(status);
        slider.setEnabled(status);
        autoModCheckBox.setEnabled(status);
        
         
        if(status){
            acButton.setBackground(Color.decode("#42aa86"));
            kapatButton.setBackground(Color.decode("#fc4f25"));
        }else{
            acButton.setBackground(Color.decode("#c9b5b5"));
            kapatButton.setBackground(Color.decode("#c9b5b5"));
        } */
    }
    public static void sendMqttPackage(MqttClient client, String topic, int data){
        
        String payload = "" + data;
        MqttMessage message = new MqttMessage(payload.getBytes());
        message.setQos(1);
        try { 
            client.publish(topic, message);
        } catch (MqttException error) { 
            System.out.println("Sinyal gönderilemedi");
            return;
        } finally {
            System.out.println("Sinyal gönderildi topic --> "+ topic + " mesaj --> " + payload);
        }
        return;
    }


    public static void sendMqttPackage(MqttClient client, String topic, String data){
        
        String payload = data;
        MqttMessage message = new MqttMessage(payload.getBytes());
        message.setQos(1);
        try { 
            client.publish(topic, message);
        } catch (MqttException error) { 
            System.out.println("Sinyal gönderilemedi");
            return;
        } finally {
            System.out.println("Sinyal gönderildi topic --> "+ topic + " mesaj --> " + payload);
        }
        return;
    }
    private static JPanel createTopPanel(MqttClient client) {
        JPanel topPanel = new JPanel(new GridLayout(1, 2)); 
    
        JPanel topLeftPanel = new JPanel();
        topLeftPanel.setLayout(new GridBagLayout()); 
    
        GridBagConstraints gbcTop = new GridBagConstraints();
        gbcTop.fill = GridBagConstraints.BOTH;
        
    
        slider = new JSlider(0, 255, 127);
        JLabel speedLabel = new JLabel("Hız");
        JLabel autoModLabel = new JLabel("Otomatik mod");
        autoModCheckBox = new JCheckBox();

        
    
        speedLabel.setFont(new Font("Arial", Font.BOLD, 16)); 
        autoModLabel.setFont(new Font("Arial", Font.BOLD, 16)); 
    
        slider.addChangeListener(e -> { 
            System.out.println("Fan hızı ayarlandı"); 
            int pwmDuty = slider.getValue();
            sendMqttPackage(client,"esp/speed",pwmDuty);
        });
        
        autoModCheckBox.setHorizontalAlignment(SwingConstants.RIGHT);
        checkBoxListener = e -> {
            System.out.println("Auto mod değişti"); 
            boolean statusBool = autoModCheckBox.isSelected();
            
            if (statusBool) {
                changeInteractionStatus(false, false, true, true);
            }else{
                changeInteractionStatus(false, false, true, false);
            }

            
            sendMqttPackage(client, "esp/auto", statusBool ? 1 : 0);

            
        };
        autoModCheckBox.addActionListener(checkBoxListener);
    
        JButton timeFrameSummoner = new JButton("Ayarlar");

        timeFrameSummoner.addActionListener(e -> {
            settingsFrame settings = new settingsFrame();
        });
    
        gbcTop.weighty = 0.4;
        gbcTop.weightx = 0.1;
        gbcTop.gridx = 0;
        gbcTop.gridy = 0;
        topLeftPanel.add(speedLabel, gbcTop);
    
        gbcTop.weightx = 2;
        gbcTop.gridx = 1;
        gbcTop.gridy = 0;
        topLeftPanel.add(slider, gbcTop);
    
        gbcTop.weightx = 0.2;
        gbcTop.gridx = 0;
        gbcTop.gridy = 1;
        topLeftPanel.add(autoModLabel, gbcTop);
    
        gbcTop.weightx = 0.2;
        gbcTop.gridx = 1;
        gbcTop.gridy = 1;
        topLeftPanel.add(autoModCheckBox, gbcTop);
    
        gbcTop.weightx = 1.0;
        gbcTop.weighty = 0.2;
        gbcTop.gridx = 0;
        gbcTop.gridy = 2;
        gbcTop.gridwidth = 2;
        topLeftPanel.add(timeFrameSummoner, gbcTop);
    
        JPanel topRightPanel = new JPanel(new GridLayout(1, 1)); 
      
        therm.setFont(new Font("Arial", Font.BOLD, 24)); 
        therm.setHorizontalAlignment(SwingConstants.CENTER); 
        therm.setForeground(Color.WHITE);
        topRightPanel.add(therm); 
    
        topPanel.add(topLeftPanel); 
        topPanel.add(topRightPanel);
        
        return topPanel;
    }
    
    private static JPanel createMidPanel() { // midPaneli oluşturan fonksiyon 
        JPanel midPanel = new JPanel(new BorderLayout()); // midpanelin objesi oluşturulur 
        midPanel.add(thermChart.chartPanel, BorderLayout.CENTER); // grafiği orta panele ekleyip ortalıyor 
        return midPanel;
    }

    private static JPanel createBottomPanel(MqttClient client) { // bottomPaneli oluşturan fonksiyon 
        JPanel bottomPanel = new JPanel(new GridLayout(1, 2)); // bottomPanel'e 1 satır, 2 sütun ekliyor 
        
        // esp'nin kodu mqtt'nin esp/signal kanalından gelen 1 ve 0 a göre fan açıp kapatılıyor
        acButton = createButton("Fanı aç", "#42aa86", 1, client); 
        kapatButton = createButton("Fanı kapat", "#fc4f25", 0, client);
        
        // aç kapat butonlarını bottomPanele ekliyor 
        bottomPanel.add(acButton); 
        bottomPanel.add(kapatButton);

        return bottomPanel; 
    }

    private static JButton createButton(String text, String color, int signal, MqttClient client) { 
        JButton button = new JButton(text); // textin yazılacağı buton objesi oluşturuluyor 
        button.setBackground(Color.decode(color)); // butonun arka plan rengi 
        button.setForeground(Color.WHITE); // butonun üstündeki yazının rengi
        button.setFocusPainted(false); // üstüne tıklandığında rengi değişmemesi için
        button.setBorderPainted(false); // çerçevesi olmaması için
        button.setUI(new javax.swing.plaf.basic.BasicButtonUI()); // oluşturulan butonun temadan etkilenmemesi için 

        button.addActionListener(e -> { // butona tıklandığında yapılacak şeyler
            
            sendMqttPackage(client,"esp/signal",signal);

            switch (signal) {
                case 1:
                    changeInteractionStatus(false,true,false,false);
                    break;
                case 0:
                    changeInteractionStatus(true,false,false,false);
                default:
                    break;
            }
        });

        return button; // oluşturulan buton döndürülür
    }
}
