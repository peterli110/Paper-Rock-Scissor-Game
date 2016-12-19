/*
 * Student Info: Name=Xinkai Li, ID=16271
 * Subject: CS532B_HW3_Fall_2016
 * Author: PeterLi
 * Filename: game.java
 * Date and Time: 2016-11-11 23:34:55
 * Project Name: CS532_HW3
 */
package cs532_hw3;

/**
 *
 * @author PeterLi
 */
public class game {

    public static final int PLAYER1 = 1;
    public static final int PLAYER2 = 2;
    public static final int DRAW = 3;

    public static final int QUIT = -1;
    public static final int START = 0;
    public static final int READY = 1;
    public static final int THROW = 2;
    
    public static final int ROCK = 1;
    public static final int PAPER = 2;
    public static final int SCISSORS = 3;
    public static final int TIMEOUT = 4;
    
    public static int getWinner(int p1, int p2) {
        if(p1 == p2)
            return DRAW;
        
        if(p1 == TIMEOUT && p2 != TIMEOUT)
            return PLAYER2;
        else if(p2 == TIMEOUT && p1 != TIMEOUT)
            return PLAYER1;
        else if(p1 == TIMEOUT && p2 == TIMEOUT)
            return DRAW;
        
        
        if(p1 == p2 % 3 + 1)
            return PLAYER1;
        else
            return PLAYER2;
    }
}
