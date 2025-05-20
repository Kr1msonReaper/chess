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
    public boolean amChecking = false;
    public Collection<ChessMove> possibleMoves = new ArrayList<>();

    public ChessGame() {
        game = this;
        //board = ChessGame.game.getBoard();
        board = new ChessBoard();
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
        return Objects.equals(board, chessGame.board) && Objects.equals(currentTurn, chessGame.currentTurn);
    }

    @Override
    public int hashCode() {
        return Objects.hash(board, currentTurn);
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
        TeamColor teamc = ChessGame.game.getBoard().getPosition(startPosition.x, startPosition.y).occupyingPiece.team;
        isInCheck(teamc);
        getPossibleMoves(teamc);

        if(ChessGame.game.getBoard().getPosition(startPosition.x, startPosition.y).occupyingPiece != null){

            Collection<ChessMove> singularPieceMoves = ChessGame.game.getBoard().getPosition(startPosition.x,
                    startPosition.y).occupyingPiece.pieceMoves(board, startPosition);
            Collection<ChessMove> filteredMoves = new ArrayList<>();

            for(ChessMove mv : singularPieceMoves){
                if(possibleMoves.contains(mv)){
                    filteredMoves.add(mv);
                }
            }

            return filteredMoves;
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
        isInCheck(currentTurn);
        getPossibleMoves(currentTurn);

        if(!possibleMoves.contains(move)){
            throw new InvalidMoveException();
        }


        move.startPos.occupyingPiece = ChessGame.game.getBoard().getPosition(move.startPos.x,
                move.startPos.y).occupyingPiece;
        move.endPos.occupyingPiece = ChessGame.game.getBoard().getPosition(move.endPos.x, move.endPos.y).occupyingPiece;
        if(move.proPiece != null){
            ChessGame.game.getBoard().getPosition(move.endPos.x,
                    move.endPos.y).occupyingPiece = new ChessPiece(move.startPos.occupyingPiece.getTeamColor(),
                    move.proPiece);
            board.getPosition(move.endPos.x,
                    move.endPos.y).occupyingPiece = new ChessPiece(move.startPos.occupyingPiece.getTeamColor(),
                    move.proPiece);
        } else {
            ChessGame.game.getBoard().getPosition(move.endPos.x,
                    move.endPos.y).occupyingPiece = move.startPos.occupyingPiece;
            board.getPosition(move.endPos.x, move.endPos.y).occupyingPiece = move.startPos.occupyingPiece;
        }

        ChessGame.game.getBoard().getPosition(move.startPos.x, move.startPos.y).occupyingPiece = null;
        board.getPosition(move.startPos.x, move.startPos.y).occupyingPiece = null;
        if(currentTurn == TeamColor.BLACK){ blackInCheck = false; isInCheck(TeamColor.WHITE); }
        if(currentTurn == TeamColor.WHITE){ whiteInCheck = false; isInCheck(TeamColor.BLACK); }
        nextTurn();
    }

    public void makeMoveAStepAhead(ChessBoard nextBoard, ChessMove move) {
        nextBoard.getPosition(move.endPos.x, move.endPos.y).occupyingPiece = move.startPos.occupyingPiece;
        nextBoard.getPosition(move.startPos.x, move.startPos.y).occupyingPiece = null;
    }

    private boolean isThreateningKing(ChessPiece piece, ChessPosition from, TeamColor targetTeam, ChessBoard board) {
        for (ChessMove move : piece.pieceMoves(board, from)) {
            ChessPiece destinationPiece = move.endPos.occupyingPiece;
            if (destinationPiece != null &&
                    destinationPiece.pieceType == ChessPiece.PieceType.KING &&
                    destinationPiece.getTeamColor() == targetTeam) {
                return true;
            }
        }
        return false;
    }


    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        for (ChessPosition pos : ChessGame.game.getBoard().boardPositions) {
            ChessPiece piece = pos.occupyingPiece;
            if (piece == null) continue;

            if (piece.getTeamColor() != teamColor) {
                if (isThreateningKing(piece, pos, teamColor, ChessGame.game.getBoard())) {
                    if (teamColor == TeamColor.BLACK) {
                        blackInCheck = true;
                    } else {
                        whiteInCheck = true;
                    }
                    return true;
                }
            }
        }

        if (teamColor == TeamColor.BLACK) {
            blackInCheck = false;
        } else {
            whiteInCheck = false;
        }

        return false;
    }

    public boolean isInCheckAStepAhead(ChessBoard nextBoard, TeamColor teamColor) {
        for (ChessPosition pos : nextBoard.boardPositions) {
            ChessPiece piece = pos.occupyingPiece;
            if (piece == null) continue;

            if (piece.getTeamColor() != teamColor) {
                if (isThreateningKing(piece, pos, teamColor, nextBoard)) {
                    return true;
                }
            }
        }

        return false;
    }

    public void getPossibleMoves(TeamColor clr){
        possibleMoves.clear();
        for(ChessPosition pos : ChessGame.game.getBoard().boardPositions){
            if(pos.occupyingPiece != null){
                if(pos.occupyingPiece.getTeamColor() == clr){
                    Collection<ChessMove> mvs = pos.occupyingPiece.pieceMoves(ChessGame.game.getBoard(), pos);
                    possibleMoves.addAll(mvs);
                }
            }
        }

        if(!ChessGame.game.amChecking){
            ChessGame.game.amChecking = true;
            Collection<ChessMove> movesToRemove = new ArrayList<>();
            for(ChessMove mv : possibleMoves){
                ChessPosition savedStart = new ChessPosition(mv.startPos.x, mv.startPos.y);
                savedStart.occupyingPiece = mv.startPos.occupyingPiece;
                ChessPosition savedEnd = new ChessPosition(mv.endPos.x, mv.endPos.y);
                savedEnd.occupyingPiece = mv.endPos.occupyingPiece;

                ChessGame.game.makeMoveAStepAhead(ChessGame.game.getBoard(), mv);
                if(ChessGame.game.isInCheckAStepAhead(ChessGame.game.getBoard(), clr)){
                    movesToRemove.add(mv);
                }

                ChessGame.game.getBoard().getPosition(mv.startPos.x,
                        mv.startPos.y).occupyingPiece = savedStart.occupyingPiece;
                ChessGame.game.getBoard().getPosition(mv.endPos.x,
                        mv.endPos.y).occupyingPiece = savedEnd.occupyingPiece;
            }
            possibleMoves.removeAll(movesToRemove);
            ChessGame.game.amChecking = false;
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
        isInCheck(teamColor);
        getPossibleMoves(teamColor);

        if((teamColor == TeamColor.WHITE && whiteInCheck)
                || (teamColor == TeamColor.BLACK && blackInCheck)){ return false;}

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
        //ChessGame.game.getBoard() = board;
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
        return board;
    }
}
