import java.util.ArrayList;

public class Move {

    int rank;
    int file;

    Move(int x, int y) {
        this.rank = x;
        this.file = y;
    }
    
    final static int[] possibleSlidingDir = {
        // rook
        -8, // up
        8, // down
        -1, // left
        1, // right

        // bishop
        -9, // up left
        -7, // up right
        7, // down left
        9 // down right
    };

    public static ArrayList<Move> generateSlidingMove(int[][] board, int piece, int currentRank, int currentFile) {
        int startDirIndex = (Piece.getPieceType(piece).equals("B")) ? 4 : 0;
        int endDirIndex = (Piece.getPieceType(piece).equals("R")) ? 4 : 8;
        int startSquare = currentRank * 8 + currentFile;

        ArrayList<Move> availableMove = new ArrayList<>();

        for(int i = startDirIndex; i < endDirIndex; i++) {
            for(int n = 0; n < Piece.numSquaresToEdge[startSquare][i]; n++) {
                int targetSquare = startSquare + possibleSlidingDir[i] * (n + 1);
                int targetFile = targetSquare%8;
                int targetRank = (targetSquare-targetFile)/8;
                
                int pieceOnTarget = board[targetRank][targetFile];
                
                if(Piece.isColor(piece, pieceOnTarget)) break;
                availableMove.add(new Move(targetRank, targetFile));
                if(!Piece.getPieceType(pieceOnTarget).equals("-")) {
                    if(!Piece.isColor(piece, pieceOnTarget)) {
                        break;
                    }
                }
            }
        }
        return availableMove;
    }

}
