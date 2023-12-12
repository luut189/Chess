import java.util.ArrayList;
import java.util.HashMap;

public class Search {
    
    public static final int pawnValue = 1;
    public static final int knightValue = 3;
    public static final int bishopValue = 3;
    public static final int rookValue = 5;
    public static final int queenValue = 9;

    private static HashMap<Integer, Integer> value = new HashMap<>();
    static {
        value.put(Piece.P, pawnValue);
        value.put(Piece.N, knightValue);
        value.put(Piece.B, bishopValue);
        value.put(Piece.R, rookValue);
        value.put(Piece.Q, queenValue);
    }
    
    public static int searchMove(int depth) {
        ArrayList<Move> moves = new ArrayList<>();

        for(int rank = 0; rank < 8; rank++) {
            for(int file = 0; file < 8; file++) {
                moves.addAll(0, Move.generateMove(Board.chessBoard, Board.chessBoard[rank][file], rank, file, Board.playerToMove));
            }
        }
        if(depth == 1) return moves.size();
        int sum = 0;

        for(Move move : moves) {
            int startRank = move.getStartRank();
            int startFile = move.getStartFile();
            int piece = Board.chessBoard[startRank][startFile];
            if(move.flag == Flag.PROMOTION) {
                for(int i = 2; i <= 5; i++) {
                    Board.playerToMove = Board.makeMove(piece, move, i);
                    sum += searchMove(depth-1);
                    Board.playerToMove = Board.unmakeMove(piece, move);
                }
            } else {
                Board.playerToMove = Board.makeMove(piece, move, 0);
                sum += searchMove(depth-1);
                Board.playerToMove = Board.unmakeMove(piece, move);
            }
        }

        return sum;
    }

    public static Move getMove() {
        ArrayList<Move> moves = new ArrayList<>();
        Move currentBestMove = null;

        for(int rank = 0; rank < 8; rank++) {
            for(int file = 0; file < 8; file++) {
                moves.addAll(0, Move.generateMove(Board.chessBoard, Board.chessBoard[rank][file], rank, file, Board.playerToMove));
            }
        }

        int bestScore = -Integer.MAX_VALUE;
        for(Move move : moves) {
            int startRank = move.getStartRank();
            int startFile = move.getStartFile();
            int piece = Board.chessBoard[startRank][startFile];
            Board.playerToMove = Board.makeMove(piece, move, 2);
            int score = minimax(2, false);
            Board.playerToMove = Board.unmakeMove(piece, move);
            if(score > bestScore) {
                bestScore = score;
                currentBestMove = move;
            }
        }

        return currentBestMove;
    }

    private static int calculateScore() {
        int score = 0;
        for(int rank = 0; rank < 8; rank++) {
            for(int file = 0; file < 8; file++) {
                int opponent = Board.playerToMove == Piece.White ? Piece.Black : Piece.White;
                int currentPiece = Board.chessBoard[rank][file];
                int color = Piece.getPieceColor(currentPiece);
                int pieceType = Piece.getPieceType(currentPiece);
                if(color == opponent && pieceType != Piece.K) {
                    score += value.get(pieceType);
                }
            }
        }
        return score;
    }

    public static int minimax(int depth, boolean isMax) {
        if(depth == 0) return calculateScore();

        ArrayList<Move> moves = new ArrayList<>();

        for(int rank = 0; rank < 8; rank++) {
            for(int file = 0; file < 8; file++) {
                moves.addAll(0, Move.generateMove(Board.chessBoard, Board.chessBoard[rank][file], rank, file, Board.playerToMove));
            }
        }

        if(isMax) {
            int bestScore = -Integer.MAX_VALUE;
            for(Move move : moves) {
                int startRank = move.getStartRank();
                int startFile = move.getStartFile();
                int piece = Board.chessBoard[startRank][startFile];
                Board.playerToMove = Board.makeMove(piece, move, 2);
                int score = minimax(depth-1, false);
                Board.playerToMove = Board.unmakeMove(piece, move);
                bestScore = Math.max(score, bestScore);
            }
            return bestScore;
        } else {
            int bestScore = Integer.MAX_VALUE;
            for(Move move : moves) {
                int startRank = move.getStartRank();
                int startFile = move.getStartFile();
                int piece = Board.chessBoard[startRank][startFile];
                Board.playerToMove = Board.makeMove(piece, move, 2);
                int score = minimax(depth-1, true);
                Board.playerToMove = Board.unmakeMove(piece, move);
                bestScore = Math.min(score, bestScore);
            }
            return bestScore;
        }
    }
    
}