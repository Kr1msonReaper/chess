package chess;

/**
 * Represents a single square position on a chess board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPosition {
    int x;
    int y;
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

    /**
     * @return which column this position is in
     * 1 codes for the left row
     */
    public int getColumn() {
        return y;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }
}
