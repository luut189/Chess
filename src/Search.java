import java.util.ArrayList;

public class Search {
    
    public static int searchMove(int depth) {
        ArrayList<Move> moves = new ArrayList<>();
        if(depth == 0) return 1;

        moves = Move.generateAllPossibleMove(Board.chessBoard, Board.playerToMove);
        if(moves.size() == 0) return 0;

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