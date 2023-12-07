import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;

import java.util.ArrayList;
import java.util.Random;
import java.io.File;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.IOException;

public class Board extends JPanel {
    
    /*
     * When rendering, Rank is Y and File is X.
     * When getting piece from chessboard, Rank is first and File is second.
     */

    final static int MAX_MOVE = 100;

    int width, height, size;

    Color whiteColor = new Color(255, 237, 213);
    Color darkColor = new Color(115, 82, 71);

    int selectedRank = -1, selectedFile = -1;
    int movedRank = -1, movedFile = -1;
    int promotionRank = -10, promotionFile = -10;
    boolean hasSelected = false;

    boolean choosingPromotion = false;
    int promotionValue = 0;
    Move choosenMove = null;
    int choosenPiece = Piece.None;

    static int startRank = -1, startFile = -1;
    static int endRank = -1, endFile = -1;
    
    static int chessBoard[][];
    static int currentTurn, playerToMove;

    static boolean hasCaptured = false;
    static int capturedPiece = Piece.None;

    static boolean hasEnPassant = false;
    static int enPassantRank = -1;
    static int enPassantFile = -1;

    static int previousEPRank = -1;
    static int previousEPFile = -1;

    static int halfmoves;
    static int tempHalfmoves = 0;
    static int fullmoves;
    
    boolean isComputer = false;
    int delay = 0;
    
    boolean isPvP = true;
    boolean endGame = false;

    ArrayList<Move> currentAvailableMove = new ArrayList<>();
    ArrayList<Move> allPossibleMove = new ArrayList<>();
    
    String startFen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w - 0 1";
    String testFen = "8/7k/1N6/pP6/8/8/8/8 w a6 0 1";

    Random rand = new Random();

    static File output;
    static FileWriter fw;
    static BufferedWriter bw;

    static int numOfMoves = 0;
    int depth = 0;
    Board(int width, int height) {
        chessBoard = new int[8][8];
        this.width = width;
        this.height = height;
        this.size = width/8;

        this.setPreferredSize(new Dimension(width, height));
        this.addMouseListener(new mouseAdapter());

        Piece.inputFen(startFen, chessBoard);
        playerToMove = Piece.getPlayerToMove();
        currentTurn = playerToMove == Piece.White ? 1 : 0;

        try {
            new File(App.source + "/output").mkdirs();
            output = new File(App.source + "/output/match.txt");
            output.createNewFile();
            fw = new FileWriter(output, false);
            bw = new BufferedWriter(fw);
            bw.write("");
            bw.close();
        } catch(IOException e) {
            e.printStackTrace();
        }

        /* Code for searching legal move (Currently work with 2 ply)
        for(int i = 1; i <= 6; i++) {
            System.out.println(Search.searchMove(i));
        }
        */
        
        if(isComputer) {
            new Timer(delay, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    // getBoard();
                    // if(checkForEndgame()) {
                    //     getEndgame();
                    //     repaint();
                    //     ((Timer) e.getSource()).stop();
                    // } else {
                    //     randomMove();
                    // }
                    System.out.println(Search.searchMove(depth));
                    repaint();
                    depth++;
                }
            }).start();
        }
    }

    public void clearBoard() {
        for(int rank = 0; rank < 8; rank++) {
            for(int file = 0; file < 8; file++) {
                chessBoard[rank][file] = Piece.None;
            }
        }
    }

    // debugging stuff
    public static void getBoard() {
        StringBuilder sb = new StringBuilder();

        sb.append("Move No." + numOfMoves + "\n");
        sb.append("     ");
        for(int i = 0;i < 8; i++) {
            sb.append("= ");
        }
        sb.append("\n");
        for(int rank = 0; rank < 8; rank++) {
            sb.append(Math.abs(rank-8) + "  | ");
            for(int file = 0; file < 8; file++) {
                int pieceColor = Piece.getPieceColor(chessBoard[rank][file]);
                String piece = Piece.pieceTypes[Piece.getPieceType(chessBoard[rank][file])];
                boolean isWhite = pieceColor == Piece.White;
                sb.append((isWhite ? piece : piece.toLowerCase()) + " ");
            }
            sb.append("|\n");
        }
        sb.append("     ");
        for(int i = 0;i < 8; i++) {
            sb.append("= ");
        }
        sb.append("\n     ");
        for(int i = 0; i < 8; i++) {
            sb.append(Character.toString(i+97) + " ");
        }
        sb.append("\nFEN: " + Piece.outputFen(chessBoard, playerToMove) + "\n\n");

        try {
            fw = new FileWriter(output, true);
            bw = new BufferedWriter(fw);
            bw.write(sb.toString());
            bw.close();
            numOfMoves++;
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    public void drawBoard(Graphics g) {
        for(int rank = 0; rank < 8; rank++) {
            for(int file = 0; file < 8; file++) {
                boolean isLight = (file + rank) % 2 == 0;
                g.setColor((isLight) ? whiteColor : darkColor);
                g.fillRect(file*size, rank*size, size, size);
            }
        }
    }

    public void drawPieces(Graphics g) {
        for(int rank = 0; rank < 8; rank++) {
            for(int file = 0; file < 8; file++) {
                g.drawImage(Piece.getPiece(chessBoard[rank][file]), file*size, rank*size, size, size, null);
            }
        }
    }

    public void drawSelected(Graphics g) {
        Color rightColor = new Color(0, 0, 255, 75);
        Color wrongColor = new Color(255, 0, 0, 75);
        if(!(selectedRank == -1 || selectedFile == -1)) {
            g.setColor(Piece.isTurnToMove(chessBoard[selectedRank][selectedFile], playerToMove) ? rightColor : wrongColor);
            g.fillRect(selectedFile*size, selectedRank*size, size, size);
        }
    }

    public void drawMovable(Graphics g) {
        g.setColor(new Color(0, 255, 0, 50));
        for(int i = 0; i < currentAvailableMove.size(); i++) {
            int rank = currentAvailableMove.get(i).getEndRank();
            int file = currentAvailableMove.get(i).getEndFile();
            g.fillRect(file*size, rank*size, size, size);
        }
    }

    public void highlightMove(Graphics g) {
        if(hasSelected) {
            startRank = -1;
            startFile = -1;
            endRank = -1;
            endFile = -1;
        }
        for(int rank = 0; rank < 8; rank++) {
            for(int file = 0; file < 8; file++) {
                if(rank == startRank && file == startFile) {
                    g.setColor(new Color(66, 214, 204, 75));
                    g.fillRect(file*size, rank*size, size, size);
                } else if(rank == endRank && file == endFile) {
                    g.setColor(new Color(214, 66, 204, 75));
                    g.fillRect(file*size, rank*size, size, size);
                }
            }
        }
    }

    public void drawPromotion(Graphics g) {
        int side = playerToMove == Piece.White ? 1 : -1;
        int[] pieces = {playerToMove | Piece.Q, playerToMove | Piece.R, playerToMove | Piece.B, playerToMove | Piece.N};
        for(int i = 0; i <= 3; i++) {
            g.setColor(Color.white);
            g.fillRect(promotionFile*size, (promotionRank+i*side)*size, size, size);
            g.drawImage(Piece.getPiece(pieces[i]), promotionFile*size, (promotionRank+i*side)*size, size, size, null);
        }
        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(Color.black);
        g2d.setStroke(new BasicStroke(5));
        g2d.drawRoundRect(promotionFile*size, playerToMove == Piece.White? promotionRank*size : (promotionRank-3)*size, size, size*4, 5, 5);
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawBoard(g);
        drawSelected(g);
        drawMovable(g);
        highlightMove(g);
        drawPieces(g);
        if(choosingPromotion) drawPromotion(g);
    }

    public void getMoveToHighlight(Move move) {
        startRank = move.getStartRank();
        startFile = move.getStartFile();
        endRank = move.getEndRank();
        endFile = move.getEndFile();
    }

    public void deselectPiece() {
        hasSelected = false;
        movedFile = -1;
        movedRank = -1;
        selectedRank = -1;
        selectedFile = -1;
        if(endGame) {
            startRank = -1;
            startFile = -1;
            endRank = -1;
            endFile = -1;
        }
        
        currentAvailableMove = new ArrayList<>();
        repaint();
    }

    public static int makeMove(int currentPiece, Move move, int promotionValue) {
        int pieceColor = Piece.getPieceColor(currentPiece);
        fullmoves += pieceColor == Piece.Black ? 1 : 0;
        getEnPassantLocation(currentPiece, move);
        if(move.flag == Flag.PROMOTION) {
            currentPiece = pieceColor | promotionValue;
        }
        if(move.flag == Flag.EN_PASSANT) {
            int enPassantOffset = pieceColor == Piece.White ? 1 : -1;
            hasCaptured = true;
            capturedPiece = chessBoard[move.getEndRank()+enPassantOffset][move.getEndFile()];
            chessBoard[move.getEndRank()][move.getEndFile()] = currentPiece;
            chessBoard[move.getEndRank()+enPassantOffset][move.getEndFile()] = Piece.None;
            chessBoard[move.getStartRank()][move.getStartFile()] = Piece.None;
        } else {
            hasCaptured = chessBoard[move.getEndRank()][move.getEndFile()] != Piece.None;
            capturedPiece = hasCaptured ? chessBoard[move.getEndRank()][move.getEndFile()] : Piece.None;
            chessBoard[move.getEndRank()][move.getEndFile()] = currentPiece;
            chessBoard[move.getStartRank()][move.getStartFile()] = Piece.None;
        }
        if(hasCaptured || Piece.getPieceType(currentPiece) == Piece.P) {
            tempHalfmoves = halfmoves;
            halfmoves = 0;
        }
        halfmoves += hasCaptured || Piece.getPieceType(currentPiece) == Piece.P ? 0 : 1;
        currentTurn++;
        getCurrentTurn();
        return playerToMove;
    }

    public static int unmakeMove(int currentPiece, Move move) {
        int pieceColor = Piece.getPieceColor(currentPiece);
        fullmoves += pieceColor == Piece.Black ? -1 : 0;
        enPassantRank = previousEPRank;
        enPassantFile = previousEPFile;
        if(move.flag == Flag.PROMOTION) {
            currentPiece = pieceColor | Piece.P;
        }
        if(move.flag == Flag.EN_PASSANT) {
            int enPassantOffset = pieceColor == Piece.White ? 1 : -1;
            chessBoard[move.getEndRank()+enPassantOffset][move.getEndFile()] = capturedPiece;
            chessBoard[move.getStartRank()][move.getStartFile()] = currentPiece;
            chessBoard[move.getEndRank()][move.getEndFile()] = Piece.None;
        } else {
            chessBoard[move.getStartRank()][move.getStartFile()] = currentPiece;
            chessBoard[move.getEndRank()][move.getEndFile()] = hasCaptured ? capturedPiece : Piece.None;
        }
        if(hasCaptured || Piece.getPieceType(currentPiece) == Piece.P) {
            halfmoves = tempHalfmoves;
            tempHalfmoves = 0;
        } else {
            halfmoves--;
        }
        hasCaptured = false;
        currentTurn--;
        getCurrentTurn();
        return playerToMove;
    }

    public static void getEnPassantLocation(int currentPiece, Move move) {
        previousEPRank = enPassantRank;
        previousEPFile = enPassantFile;
        if(hasEnPassant) {
            enPassantRank = move.getEndRank() + (Piece.getPieceColor(currentPiece) == Piece.Black ? -1 : 1);
            enPassantFile = move.getEndFile();
        } else {
            enPassantRank = -1;
            enPassantFile = -1;
        }
    }

    public void randomMove() {
        if(!endGame) {
            int randRank = rand.nextInt(8);
            int randFile = rand.nextInt(8);
            while(chessBoard[randRank][randFile] == Piece.None || Piece.getPieceColor(chessBoard[randRank][randFile]) == Piece.White) {
                randRank = rand.nextInt(8);
                randFile = rand.nextInt(8);
            }
            getAllPossibleMove();
            currentAvailableMove = Move.generateMove(chessBoard, chessBoard[randRank][randFile], randRank, randFile, playerToMove);
            while(allPossibleMove.size() != 0 && currentAvailableMove.size() == 0) {
                randRank = rand.nextInt(8);
                randFile = rand.nextInt(8);
                currentAvailableMove = Move.generateMove(chessBoard, chessBoard[randRank][randFile], randRank, randFile, playerToMove);
            }
            if(checkForEndgame()) {
                getEndgame();
                repaint();
            } else {
                int randomSelect = rand.nextInt(currentAvailableMove.size());
                Move move = currentAvailableMove.get(randomSelect);
                int currentPiece = chessBoard[randRank][randFile];
                hasEnPassant = move.flag == Flag.DOUBLE_PUSH;
                makeMove(currentPiece, move, rand.nextInt(6-2) + 2);

                // to print when there is an en passant move made by the computer
                if(move.flag == Flag.EN_PASSANT) System.out.println("en passant at " + numOfMoves);

                currentAvailableMove = new ArrayList<>();
                getMoveToHighlight(move);
            }
        }
    }

    public boolean checkForEndgame() {
        getAllPossibleMove();
        return halfmoves == MAX_MOVE || allPossibleMove.size() == 0;
    }

    public void getEndgame() {
        System.out.println(halfmoves == MAX_MOVE ? "stalemate" : (playerToMove == Piece.White ? "black" : "white") + " checkmate");
        endGame = true;
        halfmoves = 0;
        tempHalfmoves = 0;
        deselectPiece();
    }

    public void getAllPossibleMove() {
        allPossibleMove = new ArrayList<>();
        for(int rank = 0; rank < 8; rank++) {
            for(int file = 0; file < 8; file++) {
                allPossibleMove.addAll(0, Move.generateMove(chessBoard, chessBoard[rank][file], rank, file, playerToMove));
            }
        }
    }

    public static void getCurrentTurn() {
        playerToMove = currentTurn % 2 == 0 ? Piece.Black : Piece.White;
    }

    class mouseAdapter extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent e) {
            if(SwingUtilities.isLeftMouseButton(e)) {
                if(choosingPromotion) {
                    int lowerBound = playerToMove == Piece.White ? 0 : 4;
                    int upperBound = playerToMove == Piece.White ? 3 : 7;
                    
                    if(e.getX()/size != choosenMove.getEndFile()) return;
                    if(e.getY()/size < lowerBound || e.getY()/size > upperBound) return;
                    if(playerToMove == Piece.White) {
                        promotionValue = e.getY()/size+2;
                    } else {
                        promotionValue = -(e.getY()/size)+9;
                        System.out.println(promotionValue);
                    }
                    choosingPromotion = false;
                    makeMove(choosenPiece, choosenMove, promotionValue);
                    getMoveToHighlight(choosenMove);
                    if(checkForEndgame()) {
                        getEndgame();
                        repaint();
                    } else {
                        if(!isPvP) randomMove();
                    }
                    choosenMove = null;
                    choosenPiece = Piece.None;
                }
                if(hasSelected && chessBoard[selectedRank][selectedFile] != Piece.None && Piece.isTurnToMove(chessBoard[selectedRank][selectedFile], playerToMove)) {
                    movedRank = e.getY()/size;
                    movedFile = e.getX()/size;
                    int currentPiece = chessBoard[selectedRank][selectedFile];
                    int targetPiece = chessBoard[movedRank][movedFile];
                    if(!Piece.isColor(currentPiece, targetPiece)){
                        for(Move move : currentAvailableMove) {
                            if(movedRank == move.getEndRank() && movedFile == move.getEndFile()) {
                                hasEnPassant = move.flag == Flag.DOUBLE_PUSH;
                                if(move.flag == Flag.PROMOTION) {
                                    promotionRank = movedRank;
                                    promotionFile = movedFile;
                                    choosingPromotion = true;
                                    choosenMove = move;
                                    choosenPiece = currentPiece;
                                    break;
                                } else {
                                    promotionRank = -1;
                                    promotionFile = -1;
                                    promotionValue = 0;
                                }
                                makeMove(currentPiece, move, 0);
                                getMoveToHighlight(move);
                                if(checkForEndgame()) {
                                    getEndgame();
                                    repaint();
                                } else {
                                    if(!isPvP) randomMove();
                                }
                                break;
                            }
                        }
                    }
                    deselectPiece();
                } else if(!endGame) {
                    selectedRank = e.getY()/size; // rank
                    selectedFile = e.getX()/size; // file
                    int currentPiece = chessBoard[selectedRank][selectedFile];
                    if(Piece.isTurnToMove(currentPiece, playerToMove)) {
                        currentAvailableMove = Move.generateMove(chessBoard, currentPiece, selectedRank, selectedFile, playerToMove);
                        getAllPossibleMove();
                        if(allPossibleMove.size() != 0 && currentAvailableMove.size() == 0) {
                            System.out.println("there is no legal move at the current piece");
                        }
                        hasSelected = true;
                    } else {
                        currentAvailableMove = new ArrayList<>();
                    }
                    repaint();
                }
            } else if(SwingUtilities.isRightMouseButton(e)) {
                deselectPiece();
            }
        }
    }
}