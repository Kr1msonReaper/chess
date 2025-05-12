package chess;

import java.util.Collection;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {
    ChessBoard board;
    TeamColor currentTurn = TeamColor.WHITE;

    public ChessGame() {
        board = ChessBoard.existingBoard;
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return currentTurn;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        currentTurn = team;
    }

    public void nextTurn(){
        if(currentTurn == TeamColor.WHITE){
            currentTurn = TeamColor.BLACK;
        } else {
            currentTurn = TeamColor.WHITE;
        }
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        if(ChessBoard.existingBoard.getPosition(startPosition.x, startPosition.y).occupyingPiece != null){
            return ChessBoard.existingBoard.getPosition(startPosition.x, startPosition.y).occupyingPiece.pieceMoves(board, startPosition);
        } else {
            return null;
        }
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        ChessBoard.existingBoard.getPosition(move.endPos.x, move.endPos.y).occupyingPiece = move.startPos.occupyingPiece;
        ChessBoard.existingBoard.getPosition(move.startPos.x, move.startPos.y).occupyingPiece = null;
        nextTurn();
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        for(ChessPosition pos : ChessBoard.existingBoard.boardPositions){
            if(teamColor == TeamColor.BLACK){
                if(pos.occupyingPiece != null){
                    if(pos.occupyingPiece.getTeamColor() == TeamColor.WHITE){
                        for(ChessMove mv : pos.occupyingPiece.pieceMoves(ChessBoard.existingBoard, pos)){
                            if(mv.endPos.occupyingPiece != null){
                                if(mv.endPos.occupyingPiece.pieceType == ChessPiece.PieceType.KING && mv.endPos.occupyingPiece.getTeamColor() == TeamColor.BLACK){
                                    return true;
                                }
                            }
                        }
                    }
                }
            } else {
                if(pos.occupyingPiece != null){
                    if(pos.occupyingPiece.getTeamColor() == TeamColor.BLACK){
                        for(ChessMove mv : pos.occupyingPiece.pieceMoves(ChessBoard.existingBoard, pos)){
                            if(mv.endPos.occupyingPiece != null){
                                if(mv.endPos.occupyingPiece.pieceType == ChessPiece.PieceType.KING && mv.endPos.occupyingPiece.getTeamColor() == TeamColor.WHITE){
                                    return true;
                                }
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.board = board;
        ChessBoard.existingBoard = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return ChessBoard.existingBoard;
    }
}
