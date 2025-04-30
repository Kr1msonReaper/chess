package chess;

import java.util.ArrayList;
import java.util.List;

/**
 * A chessboard that can hold and rearrange chess pieces.
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessBoard {
    List<ChessPosition> boardPositions = new ArrayList<>();

    public ChessBoard() {
        for(int x = 0; x < 8; x++){
            for(int y = 0; y < 8; y++){
                boardPositions.add(new ChessPosition(x, y));
            }
        }
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
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

        for(int x = 0; x < 8; x++){
            for(int y = 0; y < 8; y++){
                boardPositions.add(new ChessPosition(x, y));
            }
        }
        // Pawns
        for(int i = 0; i < 8; i++){
            getPosition(1, i).setPiece(new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN));
            getPosition(6, i).setPiece(new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.PAWN));
        }
        // Everything else
        getPosition(0, 0).setPiece(new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.ROOK));
        getPosition(0, 1).setPiece(new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.KNIGHT));
        getPosition(0, 2).setPiece(new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.BISHOP));
        getPosition(0, 3).setPiece(new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.QUEEN));
        getPosition(0, 4).setPiece(new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.KING));
        getPosition(0, 5).setPiece(new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.BISHOP));
        getPosition(0, 6).setPiece(new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.KNIGHT));
        getPosition(0, 7).setPiece(new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.ROOK));

        getPosition(7, 0).setPiece(new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.ROOK));
        getPosition(7, 1).setPiece(new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.KNIGHT));
        getPosition(7, 2).setPiece(new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.BISHOP));
        getPosition(7, 3).setPiece(new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.QUEEN));
        getPosition(7, 4).setPiece(new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.KING));
        getPosition(7, 5).setPiece(new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.BISHOP));
        getPosition(7, 6).setPiece(new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.KNIGHT));
        getPosition(7, 7).setPiece(new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.ROOK));

    }
}

