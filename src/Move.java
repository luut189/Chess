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
        8,  // down
        -1, // left
        1,  // right

        // bishop
        -9, // up left
        -7, // up right
        7,  // down left
        9   // down right
    };

    final static int[][] possibleKnightDir = {
        // up
        {-2, -1}, // up 2 left
        {-2, 1},  // up 2 right
        {-1, -2}, // up 1 left
        {-1, 2},  // up 1 right

        // down
        {1, -2}, // down 1 left
        {1, 2},  // down 1 right
        {2, -1}, // down 2 left
        {2, 1}   // down 2 right
    };
    
    final static int[][] possiblePawnDir = {
        // white
        {-1, -1}, // capture piece left
        {-1, 1},  // capture piece right
        {-1, 0},  // normal push
        {-2, 0},  // double push

        // black
        {1, -1}, // capture piece left
        {1, 1},  // capture piece right
        {1, 0},  // normal push
        {2, 0},  // double push
    };

    public static ArrayList<Move> generateMove(int[][] board, int piece, int currentRank, int currentFile) {
        ArrayList<Move> availableMove = new ArrayList<>();
        int pieceType = Piece.getPieceType(piece);
        if(pieceType != Piece.None) {
            boolean isSlidingPiece = pieceType == Piece.Q || pieceType == Piece.R || pieceType == Piece.B;

            if(isSlidingPiece) {
                availableMove = generateSlidingMove(board, piece, currentRank, currentFile);
            } else if(pieceType == Piece.K) {
                availableMove = generateKingMove(board, piece, currentRank, currentFile);
            } else if(pieceType == Piece.N) {
                availableMove = generateKnightMove(board, piece, currentRank, currentFile);
            } else if(pieceType == Piece.P) {
                availableMove = generatePawnMove(board, piece, currentRank, currentFile);
            }
        } else {
            availableMove = new ArrayList<>();
        }
        return availableMove;
    }

    public static ArrayList<Move> generateKingMove(int[][] board, int piece, int currentRank, int currentFile) {
        ArrayList<Move> availableMove = new ArrayList<>();

        int startSquare = currentRank * 8 + currentFile;

        for(int i = 0; i < 8; i++) {
            int targetSquare = startSquare + possibleSlidingDir[i];
            int targetFile = targetSquare%8;
            int targetRank = (targetSquare-targetFile)/8;

            if(Piece.isInRange(targetRank, targetFile)) {
                int pieceOnTarget = board[targetRank][targetFile];
                if(!Piece.isColor(piece, pieceOnTarget)) {
                    availableMove.add(new Move(targetRank, targetFile));
                } else continue;
            }
        }

        return availableMove;
    }

    public static ArrayList<Move> generateSlidingMove(int[][] board, int piece, int currentRank, int currentFile) {
        int startDirIndex = (Piece.getPieceType(piece) == Piece.B) ? 4 : 0;
        int endDirIndex = (Piece.getPieceType(piece) == Piece.R) ? 4 : 8;
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
                if(Piece.getPieceType(pieceOnTarget) != Piece.None) {
                    if(!Piece.isColor(piece, pieceOnTarget)) {
                        break;
                    }
                }
            }
        }
        return availableMove;
    }

    public static ArrayList<Move> generateKnightMove(int[][] board, int piece, int currentRank, int currentFile) {
        ArrayList<Move> availableMove = new ArrayList<>();
        
        for(int i = 0; i < possibleKnightDir.length; i++) {
            int targetRank = currentRank + possibleKnightDir[i][0];
            int targetFile = currentFile + possibleKnightDir[i][1];
            if(Piece.isInRange(targetRank, targetFile)) {
                int pieceOnTarget = board[targetRank][targetFile];
                if(Piece.isColor(piece, pieceOnTarget)) continue;
                availableMove.add(new Move(targetRank, targetFile));
            } else continue;
        }
        return availableMove;
    }

    public static ArrayList<Move> generatePawnMove(int[][] board, int piece, int currentRank, int currentFile) {
        ArrayList<Move> availableMove = new ArrayList<>();

        int pieceColor = Piece.getPieceColor(piece);
        int startRank = pieceColor == Piece.White ? 6 : 1;
        int doublePushIndex = pieceColor == Piece.White ? 2 : 6;
        int startDirIndex = pieceColor == Piece.White ? 0 : 4;
        
        for(int i = startDirIndex; i < startDirIndex+2; i++) {
            int targetRank = currentRank + possiblePawnDir[i][0];
            int targetFile = currentFile + possiblePawnDir[i][1];
            if(Piece.isInRange(targetRank, targetFile)) {
                int pieceOnTarget = board[targetRank][targetFile];
                if(Piece.getPieceType(pieceOnTarget) != Piece.None) {
                    if(!Piece.isColor(piece, pieceOnTarget)) {
                        availableMove.add(new Move(targetRank, targetFile));
                    } else continue;
                } else continue;
            }
        }

        if(currentRank == startRank) {
            for(int i = doublePushIndex; i < doublePushIndex+2; i++) {
                int targetRank = currentRank + possiblePawnDir[i][0];
                int targetFile = currentFile + possiblePawnDir[i][1];
                int pieceOnTarget = board[targetRank][targetFile];
                if(Piece.getPieceType(pieceOnTarget) == Piece.None) {
                    availableMove.add(new Move(targetRank, targetFile));
                } else break;
            }
        } else {
            int targetRank = currentRank + possiblePawnDir[doublePushIndex][0];
            int targetFile = currentFile + possiblePawnDir[doublePushIndex][1];
            if(Piece.isInRange(targetRank, targetFile)) {
                int pieceOnTarget = board[targetRank][targetFile];
                if(Piece.getPieceType(pieceOnTarget) == Piece.None) availableMove.add(new Move(targetRank, targetFile));
            }
        }

        return availableMove;
    }
}