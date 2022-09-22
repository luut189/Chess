import java.awt.*;
import java.util.HashMap;

import javax.swing.*;

public class Piece {
    /*
     * 00 000 0
     * 00 001 1
     * 00 010 2
     * 00 011 3
     * 00 100 4
     * 00 101 5
     * 00 110 6
     * 
     * 01 000 8
     * 10 000 16
     */

    final static int None = 0;
    final static int K = 1;
    final static int Q = 2;
    final static int R = 3;
    final static int B = 4;
    final static int N = 5;
    final static int P = 6;
    
    private final static String[] pieceTypes = {"None", "K", "Q", "R", "B", "N", "P"};

    final static int White = 8;
    final static int Black = 16;

    public static Image getPiece(int piece) {
        if(piece == 0) {
            return new ImageIcon("./Pieces/None.png").getImage();
        } else {
            String binaryPiece = Integer.toBinaryString(piece);
            String pieceColor = (binaryPiece.length() == 4) ? "W" : "B";
            int pieceIndex = Integer.parseInt(binaryPiece.substring(1,binaryPiece.length()), 2);
            return new ImageIcon("./Pieces/" + pieceColor + pieceTypes[pieceIndex] + ".png").getImage();
        }
    }

    public static void decryptFen(String fen, int[][] board) {
        HashMap<Character, Integer> symbolToPiece = new HashMap<>();
        symbolToPiece.put('k', Piece.K);
        symbolToPiece.put('q', Piece.Q);
        symbolToPiece.put('r', Piece.R);
        symbolToPiece.put('b', Piece.B);
        symbolToPiece.put('n', Piece.N);
        symbolToPiece.put('p', Piece.P);

        int file = 0, rank = 7;

        for(char symbol : fen.toCharArray()) {
            if(symbol == '/') {
                file = 0;
                rank--;
            } else {
                if(Character.isDigit(symbol)) {
                    file += Character.getNumericValue(symbol);
                } else {
                    int pieceColor = !Character.isUpperCase(symbol) ? Piece.White : Piece.Black;
                    int pieceType = symbolToPiece.get(Character.toLowerCase(symbol));
                    board[rank][file] = pieceColor | pieceType;
                    file++;
                }
            }
        }
    }
}