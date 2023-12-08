import java.util.ArrayList;

public class Search {
    
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
    
}