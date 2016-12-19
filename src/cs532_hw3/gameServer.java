/*
 * Student Info: Name=Xinkai Li, ID=16271
 * Subject: CS532B_HW3_Fall_2016
 * Author: PeterLi
 * Filename: gameServer.java
 * Date and Time: 2016-11-10 19:13:32
 * Project Name: CS532_HW3
 */
package cs532_hw3;

/**
 *
 * @author PeterLi
 */
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListModel;

public class gameServer extends JFrame{
    
    DefaultListModel<Integer> sessions = new DefaultListModel<>();

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        gameServer frame = new gameServer();
    }
    
    public gameServer() {
        JTextArea jtaLog = new JTextArea();
        JScrollPane scrollPane = new JScrollPane(jtaLog);
        add(scrollPane, BorderLayout.CENTER);
        
        JPanel paneRight = new JPanel(new BorderLayout());
        JList<Integer> listBox = new JList<>(sessions);
        paneRight.add(listBox, BorderLayout.CENTER);
        JLabel rightText = new JLabel("Active Sessions");
        paneRight.add(rightText, BorderLayout.NORTH);
        add(paneRight, BorderLayout.EAST);
        
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(600, 320);
        setTitle("Game Server");
        
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        this.setLocation(dim.width/2-this.getSize().width/2, dim.height/2-this.getSize().height/2);
        
        setVisible(true);
        
        try {
            ServerSocket serverSocket = new ServerSocket(8000);
            jtaLog.append("Server started:\n");
            int sessionNo = 1;
            
            while (true) {
                jtaLog.append("Waiting for players to join session " + sessionNo + "\n");
                Socket player1 = serverSocket.accept();
                jtaLog.append("Player 1 joined session "
                        + sessionNo + '\n');

                new DataOutputStream(player1.getOutputStream()).writeInt(game.PLAYER1);
                
                Socket player2 = serverSocket.accept();

                jtaLog.append("Player 2 joined session " + sessionNo + '\n');

                sessions.addElement(sessionNo);
                new DataOutputStream(player2.getOutputStream()).writeInt(game.PLAYER2);
                
                HandleASession h = new HandleASession(this, player1, player2, sessionNo++);
                new Thread(h).start();
            }
            
        } catch (Exception e) {
        }
    }
    
    public synchronized void RemoveMe(int sessionId) {
        sessions.removeElement(sessionId);
    }
    
}

class HandleASession implements Runnable {

    private gameServer server = null;
    
    private Socket player1;
    private Socket player2;
    private int sessionId;
    public int getSessionId() {
        return sessionId;
    }
    

    
    public HandleASession(gameServer server, Socket player1, Socket player2, int sessionId) {
        this.server = server;
        this.player1 = player1;
        this.player2 = player2;
        this.sessionId = sessionId;
    }
    
    private int getMsg(DataInputStream is) throws IOException {
        return is.readInt();
    }
    private void sendMsg(DataOutputStream os, int msg) throws IOException {
        os.writeInt(msg);
    }
    
    @Override
    public void run() {
        try {
            DataInputStream fromPlayer1 = new DataInputStream(player1.getInputStream());
            DataOutputStream toPlayer1 = new DataOutputStream(player1.getOutputStream());
            DataInputStream fromPlayer2 = new DataInputStream(player2.getInputStream());
            DataOutputStream toPlayer2 = new DataOutputStream(player2.getOutputStream());
            
            int msgPlayer1 = game.QUIT;
            int msgPlayer2 = game.QUIT;
            
            int player1WonTimes = 0;
            int player2WonTimes = 0;
            
            int quitter = game.PLAYER1;
            
            while (true) {
                sendMsg(toPlayer1, game.READY);
                sendMsg(toPlayer2, game.READY);
                msgPlayer1 = getMsg(fromPlayer1);
                msgPlayer2 = getMsg(fromPlayer2);
                
                if(msgPlayer1 != game.READY || msgPlayer2 != game.READY) {
                    if(msgPlayer1 == game.QUIT) quitter = game.PLAYER1;
                    else quitter = game.PLAYER2;
                    break;
                }
                
                sendMsg(toPlayer1, game.THROW);
                sendMsg(toPlayer2, game.THROW);
                
                msgPlayer1 = getMsg(fromPlayer1);
                msgPlayer2 = getMsg(fromPlayer2);
                
                if(msgPlayer1 == game.QUIT || msgPlayer2 == game.QUIT) {
                    if(msgPlayer1 == game.QUIT) quitter = game.PLAYER1;
                    else quitter = game.PLAYER2;
                    break;
                }
                
                int winner = game.getWinner(msgPlayer1, msgPlayer2);
                if(winner == game.PLAYER1) player1WonTimes++;
                else if(winner == game.PLAYER2) player2WonTimes++;
                

                sendMsg(toPlayer1, winner);
                sendMsg(toPlayer2, winner);
                

                sendMsg(toPlayer2, msgPlayer1);
                sendMsg(toPlayer1, msgPlayer2);
                

                sendMsg(toPlayer1, player1WonTimes);
                sendMsg(toPlayer1, player2WonTimes);
                sendMsg(toPlayer2, player1WonTimes);
                sendMsg(toPlayer2, player2WonTimes);
                
            }
            

            if(quitter == game.PLAYER1)
                sendMsg(toPlayer2, game.QUIT);
            else if(quitter == game.PLAYER2)
                sendMsg(toPlayer1, game.QUIT);
            
            server.RemoveMe(sessionId);
        } catch (Exception e) {
            System.err.println(e.getStackTrace());
        }
    }
}
