/*
 * Student Info: Name=Xinkai Li, ID=16271
 * Subject: CS532B_HW3_Fall_2016
 * Author: PeterLi
 * Filename: gameClient.java
 * Date and Time: 2016-11-12 19:15:43
 * Project Name: CS532_HW3
 */
package cs532_hw3;

/**
 *
 * @author PeterLi
 */
import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;


public class gameClient extends JFrame implements Runnable{


    private JLabel jlblTitle = new JLabel();
    private JLabel jlblSubTitle = new JLabel();

    private JLabel jlblStatus = new JLabel();
    private JLabel jlblImageYou = new JLabel();
    private JLabel jlblImageOpponent = new JLabel();
    private ImageIcon[] imgs = new ImageIcon[4];
    

    private DataInputStream fromServer;
    private DataOutputStream toServer;
    
    private boolean continueToPlay = true;

    private String host = "localhost";
    
    private int lastWinner = 0;
    private int player1WonTimes = 0;
    private int player2WonTimes = 0;
    private int myRole = game.PLAYER1;
    
    private int clientStatus = game.START;
    
    private boolean waiting = true;
    
    private int myThrow = game.TIMEOUT;
    private int opponentsThrow = game.TIMEOUT;
    private boolean quitFlag = false;
    

    public static void main(String[] args) {
        gameClient client = new gameClient();
    }
    
    public gameClient() {
        

        imgs[game.ROCK - 1] = new ImageIcon("rock.png");
        imgs[game.PAPER - 1] = new ImageIcon("paper.png");
        imgs[game.SCISSORS - 1] = new ImageIcon("scissors.png");
        imgs[game.TIMEOUT - 1] = null;

        jlblTitle.setHorizontalAlignment(JLabel.CENTER);
        jlblTitle.setFont(new Font("SansSerif", Font.BOLD, 16));
        jlblSubTitle.setFont(new Font("SansSerif", Font.BOLD, 14));
        jlblTitle.setBorder(new LineBorder(Color.black, 1));
        JPanel pTitle = new JPanel(new GridLayout(0, 1));
        pTitle.add(jlblTitle);
        pTitle.add(jlblSubTitle);
        pTitle.setBorder(new LineBorder(Color.black, 1));
        add(pTitle, BorderLayout.NORTH);
        
        JPanel pBottom = new JPanel(new GridLayout(0, 1));

        JPanel pBtns = new JPanel();
        pBottom.add(pBtns);
        
        Button btnReady = new Button("Ready");
        Button btnRock = new Button("Rock");
        Button btnPaper = new Button("Paper");
        Button btnScissors = new Button("Scissors");
        Button btnQuit = new Button("Quit");
        
        btnReady.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(clientStatus == game.READY)
                    waiting = false;
            }
        });
        btnRock.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(clientStatus == game.THROW) {
                    myThrow = game.ROCK;
                    waiting = false;
                }
            }
        });
        btnPaper.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(clientStatus == game.THROW) {
                    myThrow = game.PAPER;
                    waiting = false;
                }
            }
        });
        btnScissors.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(clientStatus == game.THROW) {
                    myThrow = game.SCISSORS;
                    waiting = false;
                }
            }
        });
        btnQuit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(clientStatus == game.READY || clientStatus == game.THROW) {
                    quitFlag = true;
                    waiting = false;
                }
            }
        });
        
        pBtns.add(btnReady);
        pBtns.add(btnRock);
        pBtns.add(btnPaper);
        pBtns.add(btnScissors);
        pBtns.add(btnQuit);
        

        jlblStatus.setBorder(BorderFactory.createLoweredBevelBorder());
        pBottom.add(jlblStatus);
        add(pBottom, BorderLayout.SOUTH);
        
        Dimension d = new Dimension(imgs[0].getIconWidth(), imgs[0].getIconHeight()); 
        jlblImageYou.setMinimumSize(d);
        jlblImageYou.setHorizontalAlignment(JLabel.CENTER);
        jlblImageYou.setVerticalAlignment(JLabel.CENTER);
        jlblImageYou.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
        jlblImageOpponent.setMinimumSize(d);
        jlblImageOpponent.setHorizontalAlignment(JLabel.CENTER);
        jlblImageOpponent.setVerticalAlignment(JLabel.CENTER);
        jlblImageOpponent.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
        JPanel pImgPanel = new JPanel(new GridLayout(1, 0));
        pImgPanel.add(jlblImageYou);
        pImgPanel.add(jlblImageOpponent);
        add(pImgPanel, BorderLayout.CENTER);
        
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setTitle("Game Client");
        d = new Dimension(800, 400);
        setSize(d);
        setMinimumSize(d);

        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        this.setLocation(dim.width/2-this.getSize().width/2, dim.height/2-this.getSize().height/2);
        
        connectToServer();
        
        this.addWindowListener(new WindowAdapter(){
            public void windowClosing(WindowEvent e){
                if(clientStatus != game.QUIT) {
                    try {
                        sendMsg(toServer, game.QUIT);
                    } catch (IOException ex) {
                        Logger.getLogger(gameClient.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    finally {
                        System.exit(0);
                        return;
                    }
                }
                System.exit(0);
            }
        });
        
        setVisible(true);
    }
    
    private void connectToServer() {
        try {
            Socket socket;
            socket = new Socket(host, 8000);

            fromServer = new DataInputStream(socket.getInputStream());

            toServer = new DataOutputStream(socket.getOutputStream());
        } catch (Exception ex) {
            System.err.println(ex);
        }

        Thread thread = new Thread(this);
        thread.start();
    }
    
    private int getMsg(DataInputStream is) throws IOException {
        return is.readInt();
    }
    private void sendMsg(DataOutputStream os, int msg) throws IOException {
        os.writeInt(msg);
    }
    
    private void displayScores() {
        StringBuilder sb = new StringBuilder();
        if(lastWinner == game.DRAW)
            sb.append("Draw! ");
        else if(lastWinner == myRole)
            sb.append("You win! ");
        else if(lastWinner != 0)
            sb.append("You lose! ");
        sb.append("Player 1 won ");
        sb.append(player1WonTimes);
        sb.append(" times. Player 2 won ");
        sb.append(player2WonTimes);
        sb.append(" times");
        
        jlblImageYou.setIcon(imgs[myThrow - 1]);
        jlblImageOpponent.setIcon(imgs[opponentsThrow - 1]);
        jlblImageYou.invalidate();
        jlblImageOpponent.invalidate();
        
        jlblSubTitle.setText(sb.toString());
        
    }
    
    private void waitForReady() throws InterruptedException {
        while (waiting) {
            Thread.sleep(100);
        }

        waiting = true;
    }
    
    private boolean waitForThrow() throws InterruptedException {
        int counter = 0;
        while (waiting) {
            Thread.sleep(100);
            counter++;
            if(counter >= 51)
                return false;
        }
        waiting = true;
        return true;
    }

    @Override
    public void run() {
        try {
            myRole = getMsg(fromServer);
            if(myRole == game.PLAYER1) {
                jlblTitle.setText("Player 1");
                jlblStatus.setText("Waiting for player 2 to join.");
            } else if (myRole == game.PLAYER2) {
                jlblTitle.setText("Player 2");
            }
            

            while(continueToPlay) {
                displayScores();

                int sig = getMsg(fromServer);
                if(sig != game.READY) {
                    clientStatus = game.QUIT;
                    jlblStatus.setText("Game over");
                    break;
                }
                clientStatus = game.READY;
                jlblStatus.setText("Please click Ready.");
                waitForReady();
                if(quitFlag) {
                    clientStatus = game.QUIT;
                    sendMsg(toServer, game.QUIT);
                    jlblStatus.setText("Game over");
                    break;
                } else
                    sendMsg(toServer, game.READY);
                sig = getMsg(fromServer);
                if(sig != game.THROW) {
                    clientStatus = game.QUIT;
                    jlblStatus.setText("You opponent has quitted. Game ends");
                    break;
                }
                clientStatus = game.THROW;
                jlblStatus.setText("Please click in 5 seconds");
                if(!waitForThrow())
                    myThrow = game.TIMEOUT;


                sendMsg(toServer, myThrow);
                if(quitFlag) {
                    clientStatus = game.QUIT;
                    jlblStatus.setText("You quitted. Game ends");
                    break;
                }


                lastWinner = getMsg(fromServer);
                opponentsThrow = getMsg(fromServer);
                player1WonTimes = getMsg(fromServer);
                player2WonTimes = getMsg(fromServer);

                clientStatus = game.START;
                jlblStatus.setText("Click Ready to play again.");
            }
            
        } catch (IOException ex) {
            Logger.getLogger(gameClient.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(gameClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
}
