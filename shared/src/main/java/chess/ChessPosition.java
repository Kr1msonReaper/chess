package chess;

import java.util.Objects;

/**
 * Represents a single square position on a chess board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPosition {
    public int x;
    public int y;
    ChessPiece occupyingPiece = null;

    public ChessPosition(int row, int col) {
        x = row;
        y = col;
    }

    /**
     * @return which row this position is in
     * 1 codes for the bottom row
     */
    public int getRow() {
        return x;
    }

    public void setPiece(ChessPiece _piece){
        occupyingPiece = _piece;
    }

    public ChessPiece getPiece(){
        return occupyingPiece;
    }

    public String getPieceString(ChessPiece piece){
        if (piece.getPieceType() == ChessPiece.PieceType.PAWN){
            if (piece.getTeamColor() == ChessGame.TeamColor.WHITE){
                return "P";
            } else {
                return "p";
            }
        }
        if (piece.getPieceType() == ChessPiece.PieceType.ROOK){
            if (piece.getTeamColor() == ChessGame.TeamColor.WHITE){
                return "R";
            } else {
                return "r";
            }
        }
        if (piece.getPieceType() == ChessPiece.PieceType.KNIGHT){
            if (piece.getTeamColor() == ChessGame.TeamColor.WHITE){
                return "N";
            } else {
                return "n";
            }
        }
        if (piece.getPieceType() == ChessPiece.PieceType.BISHOP){
            if (piece.getTeamColor() == ChessGame.TeamColor.WHITE){
                return "B";
            } else {
                return "b";
            }
        }
        if (piece.getPieceType() == ChessPiece.PieceType.QUEEN){
            if (piece.getTeamColor() == ChessGame.TeamColor.WHITE){
                return "Q";
            } else {
                return "q";
            }
        }
        if (piece.getPieceType() == ChessPiece.PieceType.KING){
            if (piece.getTeamColor() == ChessGame.TeamColor.WHITE){
                return "K";
            } else {
                return "k";
            }
        }
        return null;
    }

    /**
     * @return which column this position is in
     * 1 codes for the left row
     */
    public int getColumn() {
        return y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(Integer.toString(x) + Integer.toString(y));
    }

    @Override
    public boolean equals(Object obj) {
        try {
            ChessPosition castedObj = (ChessPosition)obj;

            return (obj instanceof ChessPosition) && (Objects.hash(Integer.toString(x) + Integer.toString(y)) == Objects.hash(Integer.toString(castedObj.x) + Integer.toString(castedObj.y)));
        } catch(Exception e){
            return false;
        }
    }
}
