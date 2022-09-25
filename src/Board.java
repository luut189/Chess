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
    int currentTurn, playerToMove;

    ArrayList<Move> currentAvailableMove = new ArrayList<>();
    
    String startFen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w";
    String testFen = "rbqnp3/8/8/8/8/8/8/3PNQBR b";

    Board(int width, int height) {
        chessBoard = new int[8][8];
        this.width = width;
        this.height = height;
        this.size = width/8;

        this.setPreferredSize(new Dimension(width, height));
        this.addMouseListener(new mouseAdapter());

        clearBoard();
        Piece.decryptFen(startFen, chessBoard);
        Piece.computePawnStartingPoint(chessBoard);
        playerToMove = Piece.getPlayerToMove();
        currentTurn = playerToMove == Piece.White ? 1 : 0;
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

    public void getCurrentTurn() {
        playerToMove = currentTurn % 2 == 0 ? Piece.Black : Piece.White;
    }

    class mouseAdapter extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent e) {
            if(SwingUtilities.isLeftMouseButton(e)) {
                if(hasSelected && chessBoard[selectedRank][selectedFile] != Piece.None && Piece.isTurnToMove(chessBoard[selectedRank][selectedFile], playerToMove)) {
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
                                currentTurn++;
                                getCurrentTurn();
                                /* to print out FEN string for every move
                                System.out.println(Piece.encryptFen(chessBoard, playerToMove));
                                */
                                break;
                            }
                        }
                    }
                    deselectPiece();
                } else {
                    selectedRank = e.getY()/size; // rank
                    selectedFile = e.getX()/size; // file
                    int currentPiece = chessBoard[selectedRank][selectedFile];
                    if(Piece.isTurnToMove(currentPiece, playerToMove)) {
                        currentAvailableMove = Move.generateMove(chessBoard, currentPiece, selectedRank, selectedFile);
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