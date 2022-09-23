import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;

import java.util.ArrayList;

public class Board extends JPanel {
    
    /*
     * When rendering, Rank is Y and File is X.
     * When getting piece from chessboard, Rank is first and File is second.
     */

    int width, height, size;

    Color whiteColor = new Color(255, 237, 213);
    Color darkColor = new Color(115, 82, 71);

    int selectedRank = -1, selectedFile = -1;
    int movedRank = -1, movedFile = -1;
    boolean hasSelected = false;
    
    int chessBoard[][];

    ArrayList<Move> currentAvailableMove = new ArrayList<>();
    
    String startFen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR";

    Board(int width, int height) {
        chessBoard = new int[8][8];
        this.width = width;
        this.height = height;
        this.size = width/8;

        this.setPreferredSize(new Dimension(width, height));
        this.addMouseListener(new mouseAdapter());

        clearBoard();
        Piece.decryptFen(startFen, chessBoard);
        chessBoard[4][4] = Piece.White | Piece.Q;
    }

    public void clearBoard() {
        for(int rank = 0; rank < 8; rank++) {
            for(int file = 0; file < 8; file++) {
                chessBoard[rank][file] = Piece.None;
            }
        }
    }

    // debugging stuff
    public void getBoard() {
        for(int rank = 0; rank < 8; rank++) {
            for(int file = 0; file < 8; file++) {
                System.out.print(Piece.getPieceType(chessBoard[rank][file]) + " ");
            }
            System.out.println();
        }
        System.out.println();
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
        if(!(selectedRank == -1 || selectedFile == -1)) {
            g.setColor(new Color(0, 0, 255, 75));
            g.fillRect(selectedFile*size, selectedRank*size, size, size);
        }
    }

    public void drawMovable(Graphics g) {
        g.setColor(new Color(0, 255, 0, 50));
        for(int i = 0; i < currentAvailableMove.size(); i++) {
            int rank = currentAvailableMove.get(i).rank;
            int file = currentAvailableMove.get(i).file;
            g.fillRect(file*size, rank*size, size, size);
        }
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawBoard(g);
        drawPieces(g);
        drawSelected(g);
        drawMovable(g);
    }

    public void deselectPiece() {
        hasSelected = false;
        movedFile = -1;
        movedRank = -1;
        selectedRank = -1;
        selectedFile = -1;
        currentAvailableMove = new ArrayList<>();
        repaint();
    }

    class mouseAdapter extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent e) {
            if(SwingUtilities.isLeftMouseButton(e)) {
                if(hasSelected && chessBoard[selectedRank][selectedFile] != Piece.None) {
                    movedRank = e.getY()/size;
                    movedFile = e.getX()/size;
                    int currentPiece = chessBoard[selectedRank][selectedFile];
                    int targetPiece = chessBoard[movedRank][movedFile];
                    if(!Piece.isColor(currentPiece, targetPiece)) {
                        for(int i = 0; i < currentAvailableMove.size(); i++) {
                            int rank = currentAvailableMove.get(i).rank;
                            int file = currentAvailableMove.get(i).file;
                            if(movedRank == rank && movedFile == file) {
                                chessBoard[movedRank][movedFile] = currentPiece;
                                chessBoard[selectedRank][selectedFile] = Piece.None;
                            }
                        }
                    }
                    deselectPiece();
                } else {
                    selectedRank = e.getY()/size; // rank
                    selectedFile = e.getX()/size; // file
                    int currentPiece = Piece.getPieceType(chessBoard[selectedRank][selectedFile]);
                    if(!(currentPiece == Piece.None)) {
                        boolean isSlidingPiece = currentPiece == Piece.R || currentPiece == Piece.B || currentPiece == Piece.Q;
                        boolean isKing = currentPiece == Piece.K;
                        boolean isKnight = currentPiece == Piece.N;
                        boolean isPawn = currentPiece == Piece.P;

                        if(isSlidingPiece) {
                            currentAvailableMove = Move.generateSlidingMove(chessBoard, chessBoard[selectedRank][selectedFile], selectedRank, selectedFile);
                        } else if(isKing) {

                        } else if(isKnight) {

                        } else if(isPawn) {

                        }
                        hasSelected = true;
                    }
                    repaint();
                }
            } else if(SwingUtilities.isRightMouseButton(e)) {
                deselectPiece();
            }
        }
    }
}
