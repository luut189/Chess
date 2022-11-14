import java.util.ArrayList;

public class Move {

    // Properties of a Move
    int startPosition;
    int targetPosition;
    Flag flag;

    // Binary mask for decoding move's position (Rank and File)
    final int rankMask = 0b11110000;
    final int fileMask = 0b00001111;
    
    // Constructor for a Move (X is Rank and Y is File)
    Move(int startX, int startY, int endX, int endY, Flag flag) {
        
        /*
         * Shifting X by 4 bits and do bitwise OR with Y
         * A crappy example:
         *      X is 7 - 0b0111
         *      Y is 2 - 0b0010
         * 
         *      X << 4 = 0b01110000
         *      (X << 4) | Y = 0b01110010
         */
        startPosition = (startX << 4) | startY;
        targetPosition = (endX << 4) | endY;
        this.flag = flag;
    }

    // Getter for start position's rank
    public int getStartRank() {
        return (startPosition & rankMask) >> 4;
    }

    // Getter for start position's file
    public int getStartFile() {
        return startPosition & fileMask;
    }

    // Getter for target position's rank
    public int getEndRank() {
        return (targetPosition & rankMask) >> 4;
    }

    // Getter for target position's file
    public int getEndFile() {
        return targetPosition & fileMask;
    }
    
    // Constant for sliding pieces' movement
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

    // Constant for king's movement
    final static int[][] possibleKingDir = {
        {-1, 0},
        {1, 0},
        {0, -1},
        {0, 1},

        {-1, -1},
        {-1, 1},
        {1, -1},
        {1, 1}
    };

    // Constant for knight's movement
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
    
    // Constant for pawn's movement
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

    /*
     * Take in the chess board and the current player
     * Return the position of the current player's king
     */
    public static int[] getKingSquare(int[][] board, int currentPlayer) {
        int[] kingSquare = new int[2];

        for(int rank = 0; rank < 8; rank++) {
            for(int file = 0; file < 8; file++) {
                if(board[rank][file] == (currentPlayer | Piece.K)) {
                    kingSquare[0] = rank;
                    kingSquare[1] = file;
                    return kingSquare;
                }
            }
        }
        return kingSquare;
    }

    /*
     * Take in the moves list and the king's position
     * Return if there is any move that can capture the king
     */
    public static boolean checkIllegal(ArrayList<Move> response, int[] kingSquare) {
        for(Move move : response) {
            if(move.getEndRank() == kingSquare[0] && move.getEndFile() == kingSquare[1]) {
                return true;
            }
        }
        return false;
    }

    public static ArrayList<Move> generateMove(int[][] board, int piece, int currentRank, int currentFile, int currentPlayer) {
        ArrayList<Move> pseudoLegalMove = generatePseudoLegalMove(board, piece, currentRank, currentFile, currentPlayer);
        ArrayList<Move> legalMove = new ArrayList<>();

        int[] kingSquare = getKingSquare(board, currentPlayer);
        for(Move move : pseudoLegalMove) {
            if(currentRank == kingSquare[0] && currentFile == kingSquare[1]) {
                Board.makeMove(piece, move);
                kingSquare = getKingSquare(board, currentPlayer);
                Board.unmakeMove(piece, move);
                currentPlayer = Board.makeMove(piece, move);
                ArrayList<Move> opponentResponse = generateAllPossibleMove(board, currentPlayer);
                if(!checkIllegal(opponentResponse, kingSquare)) {   
                    legalMove.add(move);
                }
                currentPlayer = Board.unmakeMove(board[move.getEndRank()][move.getEndFile()], move);
                kingSquare = getKingSquare(board, currentPlayer);
            } else {
                currentPlayer = Board.makeMove(piece, move);
                ArrayList<Move> opponentResponse = generateAllPossibleMove(board, currentPlayer);
                if(!checkIllegal(opponentResponse, kingSquare)) {   
                    legalMove.add(move);
                }
                currentPlayer = Board.unmakeMove(board[move.getEndRank()][move.getEndFile()], move);
            }
        }
        return legalMove;
    }

    public static ArrayList<Move> generateAllPossibleMove(int board[][], int currentPlayer) {
        ArrayList<Move> availableMove = new ArrayList<>();

        for(int rank = 0; rank < 8; rank++) {
            for(int file = 0; file < 8; file++) {
                int piece = board[rank][file];
                if(Piece.isTurnToMove(piece, currentPlayer)) {
                    int pieceType = Piece.getPieceType(piece);
                    if(pieceType != Piece.None) {
                        boolean isSlidingPiece = pieceType == Piece.Q || pieceType == Piece.R || pieceType == Piece.B;
        
                        if(isSlidingPiece) {
                            availableMove.addAll(0, generateSlidingMove(board, piece, rank, file));
                        } else if(pieceType == Piece.K) {
                            availableMove.addAll(0, generateKingMove(board, piece, rank, file));
                        } else if(pieceType == Piece.N) {
                            availableMove.addAll(0, generateKnightMove(board, piece, rank, file));
                        } else if(pieceType == Piece.P) {
                            availableMove.addAll(0, generatePawnMove(board, piece, rank, file));
                        }
                    }
                }
            }
        }
        return availableMove;
    }

    public static ArrayList<Move> generatePseudoLegalMove(int[][] board, int piece, int currentRank, int currentFile, int currentPlayer) {
        ArrayList<Move> availableMove = new ArrayList<>();
        if(Piece.isTurnToMove(piece, currentPlayer)) {
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
            }
        }
        return availableMove;
    }

    public static ArrayList<Move> generateKingMove(int[][] board, int piece, int currentRank, int currentFile) {
        ArrayList<Move> availableMove = new ArrayList<>();
        for(int i = 0; i < 8; i++) {
            int targetRank = currentRank + possibleKingDir[i][0];
            int targetFile = currentFile + possibleKingDir[i][1];

            if(Piece.isInRange(targetRank, targetFile)) {
                int pieceOnTarget = board[targetRank][targetFile];
                if(!Piece.isColor(piece, pieceOnTarget)) {
                    availableMove.add(new Move(currentRank, currentFile, targetRank, targetFile, Flag.NONE));
                }
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
                availableMove.add(new Move(currentRank, currentFile, targetRank, targetFile, Flag.NONE));
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
                availableMove.add(new Move(currentRank, currentFile, targetRank, targetFile, Flag.NONE));
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

        int promotionRank = pieceColor == Piece.White ? 0 : 7;
        
        for(int i = startDirIndex; i < startDirIndex+2; i++) {
            int targetRank = currentRank + possiblePawnDir[i][0];
            int targetFile = currentFile + possiblePawnDir[i][1];
            if(Piece.isInRange(targetRank, targetFile)) {
                int pieceOnTarget = board[targetRank][targetFile];
                if(Board.hasEnPassant) {
                    if(targetRank == Board.enPassantRank && targetFile == Board.enPassantFile) {
                        availableMove.add(new Move(currentRank, currentFile, targetRank, targetFile, Flag.EN_PASSANT));
                    }
                }
                if(Piece.getPieceType(pieceOnTarget) != Piece.None) {
                    if(!Piece.isColor(piece, pieceOnTarget)) {
                        availableMove.add(new Move(currentRank, currentFile, targetRank, targetFile, targetRank == promotionRank ? Flag.PROMOTION : Flag.NONE));
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
                    availableMove.add(new Move(currentRank, currentFile, targetRank, targetFile, (i == 3 || i == 7) ? Flag.DOUBLE_PUSH : Flag.NONE));
                } else break;
            }
        } else {
            int targetRank = currentRank + possiblePawnDir[doublePushIndex][0];
            int targetFile = currentFile + possiblePawnDir[doublePushIndex][1];
            if(Piece.isInRange(targetRank, targetFile)) {
                int pieceOnTarget = board[targetRank][targetFile];
                if(Piece.getPieceType(pieceOnTarget) == Piece.None) availableMove.add(new Move(currentRank, currentFile, targetRank, targetFile, targetRank == promotionRank ? Flag.PROMOTION : Flag.NONE));
            }
        }

        return availableMove;
    }
}