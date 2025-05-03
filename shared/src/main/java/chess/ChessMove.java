package chess;

import java.util.Objects;

/**
 * Represents moving a chess piece on a chessboard
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessMove {
    ChessPosition startPos;
    ChessPosition endPos;
    ChessPiece.PieceType proPiece;

    public ChessMove(ChessPosition startPosition, ChessPosition endPosition, ChessPiece.PieceType promotionPiece) {
        startPos = startPosition;
        endPos = endPosition;
        proPiece = promotionPiece;
    }

    public ChessMove(ChessMove move) {
        startPos = move.startPos;
        endPos = move.endPos;
        proPiece = move.proPiece;
    }

    /**
     * @return ChessPosition of starting location
     */
    public ChessPosition getStartPosition() {
        return startPos;
    }

    /**
     * @return ChessPosition of ending location
     */
    public ChessPosition getEndPosition() {
        return endPos;
    }

    /**
     * Gets the type of piece to promote a pawn to if pawn promotion is part of this
     * chess move
     *
     * @return Type of piece to promote a pawn to, or null if no promotion
     */
    public ChessPiece.PieceType getPromotionPiece() {
        return proPiece;
    }

    @Override
    public int hashCode() {
        return Objects.hash(startPos.toString() + endPos.toString());
    }

    @Override
    public boolean equals(Object obj) {
        try {
            ChessMove castedObj = (ChessMove)obj;

            return (obj instanceof ChessMove) && (Objects.hash(startPos.toString() + endPos.toString()) == Objects.hash(castedObj.startPos.toString() + castedObj.endPos.toString()));
        } catch(Exception e){
            return false;
        }
    }

}
