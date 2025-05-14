package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {
    ChessBoard board;
    TeamColor currentTurn = TeamColor.WHITE;
    public static ChessGame game;
    public boolean blackInCheck = false;
    public boolean whiteInCheck = false;
    public boolean blackInCheckmate = false;
    public boolean whiteInCheckmate = false;
    public Collection<ChessMove> possibleMoves = new ArrayList<>();

    public ChessGame() {
        game = this;
        board = ChessBoard.existingBoard;
        board.resetBoard();
        getPossibleMoves(currentTurn);
        isInCheck(TeamColor.BLACK);
        isInCheck(TeamColor.WHITE);
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
        getPossibleMoves(currentTurn);
    }

    public void nextTurn(){
        if(currentTurn == TeamColor.WHITE){
            currentTurn = TeamColor.BLACK;
        } else {
            currentTurn = TeamColor.WHITE;
        }
        getPossibleMoves(currentTurn);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessGame chessGame = (ChessGame) o;
        return blackInCheck == chessGame.blackInCheck && whiteInCheck == chessGame.whiteInCheck && blackInCheckmate == chessGame.blackInCheckmate && whiteInCheckmate == chessGame.whiteInCheckmate && Objects.equals(board, chessGame.board) && currentTurn == chessGame.currentTurn;
    }

    @Override
    public int hashCode() {
        return Objects.hash(board, currentTurn, blackInCheck, whiteInCheck, blackInCheckmate, whiteInCheckmate);
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

        if(!possibleMoves.contains(move)){
            throw new InvalidMoveException();
        }


        move.startPos.occupyingPiece = ChessBoard.existingBoard.getPosition(move.startPos.x, move.startPos.y).occupyingPiece;
        move.endPos.occupyingPiece = ChessBoard.existingBoard.getPosition(move.endPos.x, move.endPos.y).occupyingPiece;
        if(move.proPiece != null){
            ChessBoard.existingBoard.getPosition(move.endPos.x, move.endPos.y).occupyingPiece = new ChessPiece(move.startPos.occupyingPiece.getTeamColor(), move.proPiece);
            board.getPosition(move.endPos.x, move.endPos.y).occupyingPiece = new ChessPiece(move.startPos.occupyingPiece.getTeamColor(), move.proPiece);
        } else {
            ChessBoard.existingBoard.getPosition(move.endPos.x, move.endPos.y).occupyingPiece = move.startPos.occupyingPiece;
            board.getPosition(move.endPos.x, move.endPos.y).occupyingPiece = move.startPos.occupyingPiece;
        }

        ChessBoard.existingBoard.getPosition(move.startPos.x, move.startPos.y).occupyingPiece = null;
        board.getPosition(move.startPos.x, move.startPos.y).occupyingPiece = null;
        if(currentTurn == TeamColor.BLACK){ blackInCheck = false; isInCheck(TeamColor.WHITE); }
        if(currentTurn == TeamColor.WHITE){ whiteInCheck = false; isInCheck(TeamColor.BLACK); }
        nextTurn();
    }

    public void makeMoveAStepAhead(ChessBoard nextBoard, ChessMove move) {
        nextBoard.getPosition(move.endPos.x, move.endPos.y).occupyingPiece = move.startPos.occupyingPiece;
        nextBoard.getPosition(move.startPos.x, move.startPos.y).occupyingPiece = null;
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
                                    blackInCheck = true;
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
                                    whiteInCheck = true;
                                    return true;
                                }
                            }
                        }
                    }
                }
            }
        }

        if(teamColor == TeamColor.BLACK){
            blackInCheck = false;
        } else { whiteInCheck = false; }

        return false;
    }

    public boolean isInCheckAStepAhead(ChessBoard nextBoard, TeamColor teamColor) {
        for(ChessPosition pos : nextBoard.boardPositions){
            if(teamColor == TeamColor.BLACK){
                if(pos.occupyingPiece != null){
                    if(pos.occupyingPiece.getTeamColor() == TeamColor.WHITE){
                        for(ChessMove mv : pos.occupyingPiece.pieceMoves(nextBoard, pos)){
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
                    if(pos.x == 5 && pos.y == 4){
                        int test = 1;
                    }
                    if(pos.occupyingPiece.getTeamColor() == TeamColor.BLACK){
                        for(ChessMove mv : pos.occupyingPiece.pieceMoves(nextBoard, pos)){
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

    public void getPossibleMoves(TeamColor clr){
        possibleMoves.clear();
        for(ChessPosition pos : ChessBoard.existingBoard.boardPositions){
            if(pos.occupyingPiece != null){
                if(pos.occupyingPiece.getTeamColor() == clr){
                    Collection<ChessMove> mvs = pos.occupyingPiece.pieceMoves(ChessBoard.existingBoard, pos);
                    possibleMoves.addAll(mvs);
                }
            }
        }
    }
    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        isInCheck(teamColor);
        getPossibleMoves(teamColor);
        System.out.println("Possible moves: " + possibleMoves.size());

        if(possibleMoves.size() == 0){
            return true;
        }
        return false;
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        if((teamColor == TeamColor.WHITE && whiteInCheck) || (teamColor == TeamColor.BLACK && blackInCheck)){ return false;}

        if(possibleMoves.size() == 0){
            return true;
        }
        return false;
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.board = board;
        ChessBoard.existingBoard = board;
        blackInCheck = false;
        blackInCheckmate = false;
        whiteInCheck = false;
        whiteInCheckmate = false;
        possibleMoves.clear();
        getPossibleMoves(currentTurn);
        isInCheck(TeamColor.BLACK);
        isInCheck(TeamColor.WHITE);
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
