import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;

public class Board extends JPanel {
    
    int width, height, size;

    Color whiteColor = new Color(255, 237, 213);
    Color darkColor = new Color(115, 82, 71);

    int selectedRank = -1, selectedFile = -1;
    int movedRank = -1, movedFile = -1;
    boolean hasSelected = false;
    
    int chessBoard[][];
    
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
    }

    public void clearBoard() {
        for(int rank = 0; rank < 8; rank++) {
            for(int file = 0; file < 8; file++) {
                chessBoard[file][rank] = Piece.None;
            }
        }
    }

    public void drawBoard(Graphics g) {
        for(int rank = 0; rank < 8; rank++) {
            for(int file = 0; file < 8; file++) {
                boolean isLight = (file + rank) % 2 == 0;
                g.setColor((isLight) ? whiteColor : darkColor);
                g.fillRect(rank*size, file*size, size, size);
            }
        }
    }

    public void drawPieces(Graphics g) {
        for(int file = 0; file < 8; file++) {
            for(int rank = 0; rank < 8; rank++) {
                g.drawImage(Piece.getPiece(chessBoard[file][rank]), rank*size, file*size, size, size, null);
            }
        }
    }

    public void drawSelected(Graphics g) {
        if(!(selectedRank == -1 || selectedFile == -1)) {
            g.setColor(new Color(0, 255, 0, 75));
            g.fillRect(selectedRank*size, selectedFile*size, size, size);
        }
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawBoard(g);
        drawPieces(g);
        drawSelected(g);
    }

    class mouseAdapter extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent e) {
            if(SwingUtilities.isLeftMouseButton(e)) {
                if(hasSelected && chessBoard[selectedFile][selectedRank] != Piece.None) {
                    movedRank = e.getX()/size;
                    movedFile = e.getY()/size;
                    if(!(selectedFile == movedFile && selectedRank == movedRank)) {
                        chessBoard[movedFile][movedRank] = chessBoard[selectedFile][selectedRank];
                        chessBoard[selectedFile][selectedRank] = Piece.None;
                    }
    
                    movedFile = -1;
                    movedRank = -1;
                    selectedRank = -1;
                    selectedFile = -1;
    
                    hasSelected = false;
                    
                    repaint();
                } else {
                    selectedRank = e.getX()/size; // rank
                    selectedFile = e.getY()/size; // file
                    hasSelected = true;
                    repaint();
                }
            } else if(SwingUtilities.isRightMouseButton(e)) {
                selectedRank = -1;
                selectedFile = -1;
                hasSelected = false;
                repaint();
            }
        }
    }
}
