package org.fanControl;

import java.io.*;
import javax.swing.*; //arayüzü yaptığımız kütüphane 
import java.awt.*;
import com.formdev.flatlaf.*;
import com.formdev.flatlaf.extras.FlatAnimatedLafChange;
import org.eclipse.paho.client.mqttv3.*;

public class App {

    public static JLabel therm = new JLabel("Baglanti YOK!");// espyi takmadan önceki yazı 
    public static chart thermChart = new chart(); // grafik 
    public static JButton acButton;
    public static JButton kapatButton;
    public static JSlider slider;
    public static JCheckBox autoModCheckBox;

    public static void main(String[] args) throws MqttException {

        // tema ayarlama
        FlatDarkLaf.setup();
        FlatAnimatedLafChange.showSnapshot(); // butonların renklerini vs değiştiriyor, sliderin şeklini değiştiriyor

        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
        } catch (Exception e) {
            System.err.println("Tema yuklenemedi " + e.getMessage()); // eğer eklenemezse hata veriyor 
        }

        // mqtt sunucusuna bağlantı ayarı
        MqttClient client = new MqttClient("tcp://tuna.sh:1884", "JavaApp");
        client.connect();
        System.out.println("Connected to server");

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
        frame.setVisible(true);
        // mqtt "esp/therm" kanalından sıcaklık okuması alınıp grafiğe ve anlık sıcaklık göstergesine yükleyen fonksiyon
        readTherm thread = new readTherm(); // thread'in açılma sebebi aynı anda mqtt üzerinden gelen sıcaklık verisinin okunması gerek hem de pencereyi açıyor iki işi aynı anda yapması için thread kullanılıyor 
        thread.start();
    }
    public static void sendMqttPackage(MqttClient client, String topic, int data){
        
        String payload = "" + data;
        MqttMessage message = new MqttMessage(payload.getBytes());
        message.setQos(1);
        try { 
            client.publish(topic, message);
        } catch (MqttException error) { 
            System.out.println("Sinyal gönderilemedi");
        } finally {
            System.out.println("Sinyal gönderildi --> " + payload);
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
        autoModCheckBox.addActionListener(e -> {
            System.out.println("Auto mod değişti"); 
            boolean statusBool = autoModCheckBox.isSelected();
            int status =  statusBool ? 1 : 0;

            
            acButton.setEnabled(!statusBool);
            kapatButton.setEnabled(!statusBool);
            slider.setEnabled(!statusBool);
            

            sendMqttPackage(client,"esp/auto",status);
        });
    
        JButton timeFrameSummoner = new JButton("Zaman ayarları");
    
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
        });

        return button; // oluşturulan buton döndürülür
    }
}
