import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.time.LocalTime;


// change color
// change text to retry
// mistake count
public class FrameTyping extends JFrame implements ActionListener, KeyListener {
    String passage=""; //Passage we get
    String typedPass=""; //Passage the user types
    String message=""; //Message to display at the end of the TypingTest

    int typed=0; //typed stores till which character the user has typed
    int count=0;
    int mistake = 0;
    int WPM;

    double start;
    double end;
    double elapsed;
    double seconds;

    boolean running; //If the person is typing
    boolean ended; //Whether the typing test has ended or not

    final int SCREEN_WIDTH;
    final int SCREEN_HEIGHT;
    final int DELAY=100;

    JButton button;
    Timer timer;
    JLabel label;
    public static void main(String[] args) {
        new FrameTyping();
    }

     FrameTyping() {
        this.setLayout(new BorderLayout());
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        SCREEN_WIDTH=720;
        SCREEN_HEIGHT=400;
        this.setSize(SCREEN_WIDTH,SCREEN_HEIGHT);
        this.setVisible(true);
        this.setLocationRelativeTo(null);

        button=new JButton("Start");
        button.setFont(new Font("Pearly Gates",Font.BOLD,30));
        button.setForeground(Color.black);
        button.setFocusPainted(false);
        button.setVisible(true);
        button.addActionListener(this);
        button.setFocusable(false);

        label=new JLabel();
        label.setText("Click the Start Button");
        label.setFont(new Font("Pearly Gates",Font.BOLD,30));
        label.setVisible(true);
        label.setOpaque(true);
        label.setHorizontalAlignment(JLabel.CENTER);
        label.setBackground(Color.lightGray);

        this.add(button, BorderLayout.SOUTH);
        this.add(label, BorderLayout.NORTH);
        this.getContentPane().setBackground(Color.WHITE);
        this.addKeyListener(this);
        this.setFocusable(true);
        this.setResizable(false);
        this.setTitle("Typing Test");
        this.revalidate();
    }
    private void drawWrappedText(Graphics g, String text, int xPos, int yPos, int maxCharactersPerLine) {
        int lineHeight = g.getFont().getSize() * 2;
        int charactersDrawn = 0;
        int currentLine = 0;

        while (charactersDrawn < text.length()) {
            int remainingCharacters = text.length() - charactersDrawn;
            int charactersToDraw = Math.min(maxCharactersPerLine, remainingCharacters);
            String line = text.substring(charactersDrawn, charactersDrawn + charactersToDraw);
            g.drawString(line, xPos, yPos + currentLine * lineHeight);
            charactersDrawn += charactersToDraw;
            currentLine++;
        }
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        draw(g);
    }

    public void draw(Graphics g) {
        g.setFont(new Font("MV Boli", Font.BOLD, 25));


        if (running) {
            // Display the black passage text
            g.setColor(Color.BLACK);
            drawWrappedText(g, passage, g.getFont().getSize(), g.getFont().getSize() * 5, 50);

            // Display the correctly typed passage in green
            g.setColor(Color.GREEN);
            drawWrappedText(g, typedPass, g.getFont().getSize(), g.getFont().getSize() * 5, 50);



            running = false;

        }



        if(ended)
        {
            if(WPM<=40)
                message="You are an Average Typist";
            else if(WPM>40 && WPM<=60)
                message="You are a Good Typist";
            else if(WPM>60 && WPM<=100)
                message="You are an Excellent Typist";
            else
                message="You are an Elite Typist";



            //set background
            g.setColor(Color.white);
            g.fillRect(0,0,getWidth(),getHeight());

            FontMetrics metrics=g.getFontMetrics();

            g.setColor(Color.BLUE);
            g.drawString("Typing Test Completed!", (getWidth()-metrics.stringWidth("Typing Test Completed!"))/2, g.getFont().getSize()*3);

            g.setColor(Color.BLACK);
            g.drawString("Typing Speed: "+WPM+" Words Per Minute", (getWidth()-metrics.stringWidth("Typing Speed: "+WPM+" Words Per Minute"))/2, g.getFont().getSize()*6);

            g.drawString("mistake: "+mistake, (getWidth()-metrics.stringWidth("mistake: "+mistake))/2, g.getFont().getSize()*9);
            g.drawString(message, (getWidth()-metrics.stringWidth(message))/2, g.getFont().getSize()*11);

            timer.stop();
            ended=false;
        }
    }


    @Override
    public void keyTyped(KeyEvent e) //keyTyped uses the key Character which can identify capital and lowercase difference in keyPressed it takes unicode so it also considers shift which creates a problem
    {
        if(passage.length()>1)
        {
            if(count==0)
                start=LocalTime.now().toNanoOfDay();
            else if(count==passage.length()) { //Once all 200 characters are typed we will end the time and calculate time elapsed

                end=LocalTime.now().toNanoOfDay();
                elapsed=end-start;
                seconds=elapsed/1000000000.0; //nano/1000000000.0 is seconds
                String[] words = passage.split("\\s+");
                int numWords = words.length;
                WPM=(int)((numWords/seconds)*60); //number of character by 5 is one word by seconds is words per second * 60 WPM
                ended=true;
                running=false;
                count++;
            }
            char[] pass = passage.toCharArray();
            if(typed < pass.length) {
                running=true;
                if(e.getKeyChar()==pass[typed]) {
                    typedPass = typedPass + pass[typed]; //To the typed Passage we are adding what is currently typed
                    typed++;
                    count++;
                } //If the typed character is not equal to the current position it will not add it to the typedPassage, so the user needs to type the right thing

                else if (e.getKeyChar() != pass[typed]) {
                    mistake++;
                }

            }

        }
    }

    @Override
    public void keyPressed(KeyEvent e) {}

    @Override
    public void keyReleased(KeyEvent e) {}

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource()==button)
        {
            passage=getPassage();
            timer=new Timer(DELAY,this);
            timer.start();
            running=true;
            ended=false;

            typedPass="";
            message="";

            typed=0;
            count=0;
            mistake=0;
        }
        if(running)
            repaint();
        if(ended)
            repaint();
    }
    public static String getPassage() {
        String API_ENDPOINT = "https://api.api-ninjas.com/v1/facts?limit=1";
        String API_KEY = "LrUv2QPMTRW0T8sucAEmDA==KXlVo7UPpo9Fumh9";
        try {
            HttpClient httpclient = HttpClientBuilder.create().build();
            HttpGet request = new HttpGet(API_ENDPOINT);
            request.addHeader("X-Api-Key", API_KEY);

            HttpResponse respone = httpclient.execute(request);
            HttpEntity entity = respone.getEntity();

            if (entity != null) {
                String jsonResponse = EntityUtils.toString(entity);
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(jsonResponse);
                JsonNode factNode = root.get(0).get("fact");
                String fact = factNode.asText();
                return fact;
            }

            return null;
        }
        catch (RuntimeException e) {throw new RuntimeException(e);}
        catch (IOException e) {throw new RuntimeException(e);

        }
    }
}