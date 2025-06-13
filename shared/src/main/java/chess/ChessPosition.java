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

    public void setPiece(ChessPiece piece){
        occupyingPiece = piece;
    }

    public ChessPiece getPiece(){
        return occupyingPiece;
    }

    public static ChessPiece.PieceType getPieceFromString(String pieceString) {
        if (pieceString == null || pieceString.equals(" ") || pieceString.trim().isEmpty()) {
            return null;
        }

        // Convert to uppercase for comparison
        String upperPiece = pieceString.toUpperCase();

        ChessPiece.PieceType pieceType = null;

        switch (upperPiece) {
            case "P":
                pieceType = ChessPiece.PieceType.PAWN;
                break;
            case "R":
                pieceType = ChessPiece.PieceType.ROOK;
                break;
            case "N":
                pieceType = ChessPiece.PieceType.KNIGHT;
                break;
            case "B":
                pieceType = ChessPiece.PieceType.BISHOP;
                break;
            case "Q":
                pieceType = ChessPiece.PieceType.QUEEN;
                break;
            case "K":
                pieceType = ChessPiece.PieceType.KING;
                break;
            default:
                return null;
        }

        // Create and return the chess piece with the specified type and team color
        return pieceType;
    }

    public String getPieceString(){
        if (occupyingPiece == null){
            return " ";
        }

        if (occupyingPiece.getPieceType() == ChessPiece.PieceType.PAWN){
            if (occupyingPiece.getTeamColor() == ChessGame.TeamColor.WHITE){
                return "P";
            } else {
                return "p";
            }
        }
        if (occupyingPiece.getPieceType() == ChessPiece.PieceType.ROOK){
            if (occupyingPiece.getTeamColor() == ChessGame.TeamColor.WHITE){
                return "R";
            } else {
                return "r";
            }
        }
        if (occupyingPiece.getPieceType() == ChessPiece.PieceType.KNIGHT){
            if (occupyingPiece.getTeamColor() == ChessGame.TeamColor.WHITE){
                return "N";
            } else {
                return "n";
            }
        }
        if (occupyingPiece.getPieceType() == ChessPiece.PieceType.BISHOP){
            if (occupyingPiece.getTeamColor() == ChessGame.TeamColor.WHITE){
                return "B";
            } else {
                return "b";
            }
        }
        if (occupyingPiece.getPieceType() == ChessPiece.PieceType.QUEEN){
            if (occupyingPiece.getTeamColor() == ChessGame.TeamColor.WHITE){
                return "Q";
            } else {
                return "q";
            }
        }
        if (occupyingPiece.getPieceType() == ChessPiece.PieceType.KING){
            if (occupyingPiece.getTeamColor() == ChessGame.TeamColor.WHITE){
                return "K";
            } else {
                return "k";
            }
        }
        return " ";
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
        return Objects.hash(Integer.toString(x) + Integer.toString(y) + getPieceString());
    }

    @Override
    public boolean equals(Object obj) {
        try {
            ChessPosition castedObj = (ChessPosition)obj;

            return (Objects.hash(Integer.toString(x) + Integer.toString(y) + getPieceString())
                    == Objects.hash(Integer.toString(castedObj.x) + Integer.toString(castedObj.y) + getPieceString()));
        } catch(Exception e){
            return false;
        }
    }
}
