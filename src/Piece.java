import java.awt.*;
import java.util.ArrayList;
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
    
    private final static String[] pieceTypes = {"-", "K", "Q", "R", "B", "N", "P"};

    final static int White = 8;
    final static int Black = 16;

    final static int typeMask = 0b00111;
    final static int blackMask = 0b10000;
    final static int whiteMask = 0b01000;
    final static int colorMask = whiteMask | blackMask;

    static int[][] numSquaresToEdge = new int[64][8];

    static ArrayList<Move> pawnStartingPoint = new ArrayList<>();

    static int playerToMove;

    public static Image getPiece(int piece) {
        if(piece == 0) {
            return new ImageIcon("./Pieces/None.png").getImage();
        } else {
            int pieceType = getPieceType(piece);
            String pieceColor = (piece & colorMask) == Piece.White ? "W" : "B";
            return new ImageIcon("./Pieces/" + pieceColor + pieceTypes[pieceType] + ".png").getImage();
        }
    }

    public static boolean isColor(int piece, int targetPiece) {
        return (piece & colorMask) == (targetPiece & colorMask);
    }

    public static int getPieceType(int piece) {
        return piece & typeMask;
    }

    public static int getPieceColor(int piece) {
        return piece & colorMask;
    }

    public static boolean isTurnToMove(int selectedPiece, int playerToMove) {
        return getPieceColor(selectedPiece) == playerToMove;
    }

    public static int getPlayerToMove() {
        return playerToMove;
    }

    public static boolean isInRange(int rank, int file) {
        return (rank >= 0 && rank < 8) && (file >= 0 && file < 8);
    }

    public static boolean isOnStartingPoint(int currentRank, int currentFile) {
        for(int i = 0; i < pawnStartingPoint.size(); i++) {
            if(currentRank == pawnStartingPoint.get(i).rank && currentFile == pawnStartingPoint.get(i).file) return true;
        }
        return false;
    }

    public static void decryptFen(String fen, int[][] board) {
        HashMap<Character, Integer> symbolToPiece = new HashMap<>();
        symbolToPiece.put('k', Piece.K);
        symbolToPiece.put('q', Piece.Q);
        symbolToPiece.put('r', Piece.R);
        symbolToPiece.put('b', Piece.B);
        symbolToPiece.put('n', Piece.N);
        symbolToPiece.put('p', Piece.P);

        int file = 0, rank = 0;

        for(char symbol : fen.toCharArray()) {
            if(symbol != ' ') {
                if(symbol == '/') {
                    file = 0;
                    rank++;
                } else {
                    if(rank == 7 && file == 8) {
                        playerToMove = symbol == 'w' ? Piece.White : Piece.Black;
                    } else {
                        if(Character.isDigit(symbol)) {
                            file += Character.getNumericValue(symbol);
                        } else {
                            int pieceColor = Character.isUpperCase(symbol) ? Piece.White : Piece.Black;
                            int pieceType = symbolToPiece.get(Character.toLowerCase(symbol));
                            board[rank][file] = pieceColor | pieceType;
                            file++;
                        }
                    }
                }
            }
        }
    }

    public static String encryptFen(int[][] board, int playerToMove) {
        String fen = "";
        HashMap<Integer, Character> pieceToSymbol = new HashMap<>();
        pieceToSymbol.put(Piece.K, 'k');
        pieceToSymbol.put(Piece.Q, 'q');
        pieceToSymbol.put(Piece.R, 'r');
        pieceToSymbol.put(Piece.B, 'b');
        pieceToSymbol.put(Piece.N, 'n');
        pieceToSymbol.put(Piece.P, 'p');

        int currentEmpty = 0;
        for(int rank = 0; rank < 8; rank++) {
            for(int file = 0; file < 8; file++) {
                if(getPieceType(board[rank][file]) != Piece.None) {
                    if(currentEmpty != 0) fen += currentEmpty;
                    currentEmpty = 0;
                    char currentPiece = pieceToSymbol.get(getPieceType(board[rank][file]));
                    fen += (Piece.getPieceColor(board[rank][file]) == Piece.White ? Character.toUpperCase(currentPiece) : currentPiece);
                } else {
                    currentEmpty++;
                }
            }
            if(rank != 7) {
                if(currentEmpty != 0) {
                    fen += currentEmpty + "/";
                    currentEmpty = 0;
                } else {
                    fen += "/";
                }
            }
        }
        fen += " ";
        fen += playerToMove == Piece.White ? "w" : "b";

        return fen;
    }

    public static void computePawnStartingPoint(int[][] board) {
        for(int rank = 0; rank < 8; rank++) {
            for(int file = 0; file < 8; file++) {
                if(getPieceType(board[rank][file]) == Piece.P) {
                    pawnStartingPoint.add(new Move(rank, file));
                }
            }
        }
    }

    public static void computeData() {
        for(int rank = 0; rank < 8; rank++) {
            for(int file = 0; file < 8; file++) {
                int top = rank;
                int bottom = 7 - rank;
                int left = file;
                int right = 7 - file;

                int[] data = {
                    top,
                    bottom,
                    left,
                    right,
                    Math.min(top, left),
                    Math.min(top, right),
                    Math.min(bottom, left),
                    Math.min(bottom, right)
                };

                numSquaresToEdge[rank * 8 + file] = data;
            }
        }
    }
}