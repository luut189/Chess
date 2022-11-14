import java.awt.*;
import java.util.HashMap;

import javax.swing.*;

public class Piece {

    /* Example of pieces representations:
     * 00 000 - 0 - None
     * 00 001 - 1 - King
     * 00 010 - 2 - Queen
     * 00 011 - 3 - Rook
     * 00 100 - 4 - Bishop
     * 00 101 - 5 - Knight
     * 00 110 - 6 - Pawn
     * 
     * 01 000 - 8 - White
     * 10 000 - 16 - Black
     */

    final static int None = 0;
    final static int K = 1;
    final static int Q = 2;
    final static int R = 3;
    final static int B = 4;
    final static int N = 5;
    final static int P = 6;
    
    public final static String[] pieceTypes = {"-", "K", "Q", "R", "B", "N", "P"};

    final static int White = 8;
    final static int Black = 16;

    // Binary mask used to decode the piece
    final static int typeMask = 0b00111;
    final static int colorMask = 0b11000;

    // 2D array to store the pre-calculated data
    static int[][] numSquaresToEdge = new int[64][8];

    // Store the current player to move
    static int playerToMove;

    // Take in a piece and return an Image based on the piece
    public static Image getPiece(int piece) {
        if(piece == 0) {
            return new ImageIcon(App.source + "/Pieces/None.png").getImage();
        } else {
            int pieceType = getPieceType(piece);
            String pieceColor = getPieceColor(piece) == Piece.White ? "W" : "B";
            return new ImageIcon(App.source + "/Pieces/" + pieceColor + pieceTypes[pieceType] + ".png").getImage();
        }
    }

    // Take in two pieces and return whether they are the same color or not
    public static boolean isColor(int piece, int targetPiece) {
        return (piece & colorMask) == (targetPiece & colorMask);
    }

    // Take in a piece and return which piece that is (King, Queen, Knight,...)
    public static int getPieceType(int piece) {
        return piece & typeMask;
    }

    // Take in a piece and return the color of that piece (Black, White)
    public static int getPieceColor(int piece) {
        return piece & colorMask;
    }

    // Take in a piece and return if it is that piece's turn to move
    public static boolean isTurnToMove(int selectedPiece, int playerToMove) {
        return getPieceColor(selectedPiece) == playerToMove;
    }

    // Return the current player to move
    public static int getPlayerToMove() {
        return playerToMove;
    }

    // Take in two numbers for rank and file and return if they are in range
    public static boolean isInRange(int rank, int file) {
        return (rank >= 0 && rank < 8) && (file >= 0 && file < 8);
    }

    /*
     * Take in a FEN string and a 2D array to represent a chess board
     * Decode the FEN string and put the information into the 2D array
     */
    public static void inputFen(String fen, int[][] board) {

        // A look up table for symbols to chess pieces
        HashMap<Character, Integer> symbolToPiece = new HashMap<>();
        symbolToPiece.put('k', Piece.K);
        symbolToPiece.put('q', Piece.Q);
        symbolToPiece.put('r', Piece.R);
        symbolToPiece.put('b', Piece.B);
        symbolToPiece.put('n', Piece.N);
        symbolToPiece.put('p', Piece.P);

        int file = 0, rank = 0;

        // Split the string into segments
        String splittedFen[] = fen.split(" ");

        // Put the pieces into the board
        for(char symbol : splittedFen[0].toCharArray()) {
            if(symbol == '/') {
                file = 0;
                rank++;
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
        
        // Get the player to move
        playerToMove = splittedFen[1].equals("w") ? Piece.White : Piece.Black;

        // Get the En passant position if there is any
        if(!splittedFen[2].equals("-")) {
            Board.hasEnPassant = true;
            Board.enPassantRank = Math.abs(8 - Integer.parseInt(String.valueOf(splittedFen[2].charAt(1))));
            Board.enPassantFile = (int) splittedFen[2].charAt(0) - 97;
        }

        // Get the halfmoves and fullmoves clock
        Board.halfmoves = Integer.parseInt(splittedFen[3]);
        Board.fullmoves = Integer.parseInt(splittedFen[4]);
    }

    // Take in the board information and encode into a FEN string
    public static String outputFen(int[][] board, int playerToMove) {
        String fen = "";

        // A look up table for pieces to symbols
        HashMap<Integer, Character> pieceToSymbol = new HashMap<>();
        pieceToSymbol.put(Piece.K, 'k');
        pieceToSymbol.put(Piece.Q, 'q');
        pieceToSymbol.put(Piece.R, 'r');
        pieceToSymbol.put(Piece.B, 'b');
        pieceToSymbol.put(Piece.N, 'n');
        pieceToSymbol.put(Piece.P, 'p');

        // Encode the pieces information of the board into FEN string
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

        // Encode the current player to move
        fen += " ";
        fen += playerToMove == Piece.White ? "w" : "b";

        // Encode the En passant position if there is any
        fen += " ";
        if(Board.hasEnPassant) {
            fen += Character.toString((char) Board.enPassantFile+97) + Math.abs(Board.enPassantRank-8);
        } else {
            fen += "-";
        }

        // Encode the halfmoves and fullmoves clock
        fen += " " + Board.halfmoves + " " + Board.fullmoves;

        return fen;
    }

    // Calculate the amount of squares from each square to the edges
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