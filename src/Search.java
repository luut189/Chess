import java.util.ArrayList;

public class Search {
    
    public static int searchMove(int depth) {
        ArrayList<Move> moves = new ArrayList<>();
        if(depth == 0) return 1;

        for(int rank = 0; rank < 8; rank++) {
            for(int file = 0; file < 8; file++) {
                moves.addAll(0, Move.generateMove(Board.chessBoard, Board.chessBoard[rank][file], rank, file, Board.playerToMove));
            }
        }

        int sum = 0;

        for(Move move : moves) {
            int startRank = move.getStartRank();
            int startFile = move.getStartFile();
            int piece = Board.chessBoard[startRank][startFile];
            Board.playerToMove = Board.makeMove(piece, move);
            sum += searchMove(depth-1);
            Board.playerToMove = Board.unmakeMove(piece, move);
        }

        return sum;
    }
    
}