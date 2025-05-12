package chess;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * A chessboard that can hold and rearrange chess pieces.
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessBoard {
    public List<ChessPosition> boardPositions = new ArrayList<>();
    String testStringRep = "";
    public static ChessBoard existingBoard = new ChessBoard();

    public ChessBoard() {

        for(int x = 1; x < 9; x++){
            for(int y = 1; y < 9; y++){
                boardPositions.add(new ChessPosition(x, y));
                //boardPositions.hashCode()
            }
        }
        ChessBoard.existingBoard = this;
    }

    @Override
    public int hashCode() {
        return Objects.hash(boardPositions);
    }

    @Override
    public String toString(){
        String stringRep = "";

        for(int x = 8; x > 0; x--){
            for(int y = 1; y < 9; y++){
                ChessPosition pos = getPosition(x, y);

                String middle = " ";
                String pieceChar = pos.getPieceString();
                if(pieceChar != null){
                    middle = pieceChar;
                }

                if (y == 1){
                    stringRep += "|";
                }
                stringRep += middle;
                stringRep += "|";
                if (y == 8){
                    stringRep += "\n";
                }
            }
        }
        testStringRep = stringRep;
        return stringRep;
    }

    @Override
    public boolean equals(Object obj) {
        int test = 0;
        try {

            String otherObj = obj.toString();
            String stringRepresentation = toString();

            System.out.println("Real class:\n");
            System.out.println(stringRepresentation + "\n ---------------------------");
            System.out.println("Compared object:\n");
            System.out.println(otherObj + "\n ---------------------------");

            return (stringRepresentation.equals(otherObj));
        } catch(Exception e){
            return false;
        }
    }

    /**
     * Adds a chess piece to the chessboard
     *
     * @param position where to add the piece to
     * @param piece    the piece to add
     */
    public void addPiece(ChessPosition position, ChessPiece piece) {
        for(ChessPosition pos : boardPositions){
            if(pos.getRow() == position.getRow() && pos.getColumn() == position.getColumn()){
                pos.setPiece(piece);
            }
        }
    }

    /**
     * Gets a chess piece on the chessboard
     *
     * @param position The position to get the piece from
     * @return Either the piece at the position, or null if no piece is at that
     * position
     */
    public ChessPiece getPiece(ChessPosition position) {
        for(ChessPosition pos : boardPositions){
            if(pos.getRow() == position.getRow() && pos.getColumn() == position.getColumn()){
                return pos.getPiece();
            }
        }
        return null;
    }

    public ChessPosition getPosition(int x, int y){
        for(ChessPosition pos : boardPositions){
            if(pos.getRow() == x && pos.getColumn() == y){
                return pos;
            }
        }
        return null;
    }

    /**
     * Sets the board to the default starting board
     * (How the game of chess normally starts)
     */
    public void resetBoard() {
        boardPositions.clear();

        for(int x = 1; x < 9; x++){
            for(int y = 1; y < 9; y++){
                boardPositions.add(new ChessPosition(x, y));
            }
        }
        // Pawns
        for(int i = 1; i < 9; i++){
            getPosition(2, i).setPiece(new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN));
            getPosition(7, i).setPiece(new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.PAWN));
        }
        // Everything else
        getPosition(1, 1).setPiece(new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.ROOK));
        getPosition(1, 2).setPiece(new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.KNIGHT));
        getPosition(1, 3).setPiece(new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.BISHOP));
        getPosition(1, 4).setPiece(new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.QUEEN));
        getPosition(1, 5).setPiece(new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.KING));
        getPosition(1, 6).setPiece(new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.BISHOP));
        getPosition(1, 7).setPiece(new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.KNIGHT));
        getPosition(1, 8).setPiece(new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.ROOK));

        getPosition(8, 1).setPiece(new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.ROOK));
        getPosition(8, 2).setPiece(new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.KNIGHT));
        getPosition(8, 3).setPiece(new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.BISHOP));
        getPosition(8, 4).setPiece(new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.QUEEN));
        getPosition(8, 5).setPiece(new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.KING));
        getPosition(8, 6).setPiece(new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.BISHOP));
        getPosition(8, 7).setPiece(new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.KNIGHT));
        getPosition(8, 8).setPiece(new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.ROOK));
        toString();
    }
}

