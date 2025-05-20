package chess;

import java.util.*;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {
    public ChessGame.TeamColor team;
    public ChessPiece.PieceType pieceType;

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        team = pieceColor;
        pieceType = type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(team.name() + pieceType.name());
    }

    @Override
    public boolean equals(Object obj) {
        try {
            ChessPiece castedObj = (ChessPiece)obj;

            return (obj instanceof ChessPiece)
                    && (Objects.hash(team.name() + pieceType.name())
                    == Objects.hash(castedObj.team.name() + castedObj.pieceType.name()));
        } catch(Exception e){
            return false;
        }
    }

    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return team;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return pieceType;
    }

    public Collection<ChessMove> validateMoves(Collection<ChessMove> moves){
        Collection<ChessMove> validatedMoves = new ArrayList<>();

        for (ChessMove move : moves) {
            // Only allow moves to stay on board
            if ((move.endPos.x >= 1 && move.endPos.x <= 8) &&
                    (move.endPos.y >= 1 && move.endPos.y <= 8)) {

                // If it's a pawn move reaching the promotion row
                if (pieceType == PieceType.PAWN) {
                    if ((team == ChessGame.TeamColor.WHITE && move.endPos.x == 8) ||
                            (team == ChessGame.TeamColor.BLACK && move.endPos.x == 1)) {

                        // If it's already a promotion move, just accept it
                        if (move.proPiece != null) {
                            validatedMoves.add(move);
                            continue;
                        }

                        // Otherwise, create 4 promotion moves
                        for (PieceType promotion : List.of(PieceType.QUEEN, PieceType.ROOK, PieceType.BISHOP, PieceType.KNIGHT)) {
                            validatedMoves.add(new ChessMove(move.startPos, move.endPos, promotion));
                        }
                        continue;
                    }
                }

                validatedMoves.add(move); // normal move
            }
        }

        return validatedMoves;
    }


    public Collection<ChessMove> addMoves(ChessBoard board, ChessPosition myPosition, int xDir, int yDir){
        Collection<ChessMove> potentialMoves = new ArrayList<>();

        int potentialX = myPosition.x;
        int potentialY = myPosition.y;
        while(true){
            if(potentialX == 1 && xDir < 0){ break; } else if(potentialX == 8 && xDir > 0){ break; }
            if(potentialY == 1 && yDir < 0){ break; } else if(potentialY == 8 && yDir > 0){ break; }

            potentialX += xDir;
            potentialY += yDir;

            ChessPosition nextPos = new ChessPosition(potentialX, potentialY);
            ChessPiece pc = board.getPiece(nextPos);
            if (pc == null){
                ChessMove nextMove = new ChessMove(myPosition, nextPos, null);
                potentialMoves.add(nextMove);
                continue;
            }
            if(pc.getTeamColor() != team){
                ChessPosition targetPos = new ChessPosition(nextPos.x, nextPos.y);
                targetPos.occupyingPiece = pc;
                ChessMove nextMove = new ChessMove(myPosition, targetPos, null);
                potentialMoves.add(nextMove);
            }
            break;
        }

        return validateMoves(potentialMoves);
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> potentialMoves = new ArrayList<>();
        myPosition.occupyingPiece = this;

        switch (pieceType) {
            case PAWN -> potentialMoves.addAll(generatePawnMoves(board, myPosition));
            case KNIGHT, KING -> potentialMoves.addAll(generateJumpingMoves(board, myPosition));
            case ROOK, BISHOP, QUEEN -> potentialMoves.addAll(generateSlidingMoves(board, myPosition));
        }

        return validateMoves(potentialMoves);
    }

    private Collection<ChessMove> generatePawnMoves(ChessBoard board, ChessPosition pos) {
        List<ChessMove> moves = new ArrayList<>();
        int direction = (team == ChessGame.TeamColor.WHITE) ? 1 : -1;
        int startRow = (team == ChessGame.TeamColor.WHITE) ? 2 : 7;
        int promotionRow = (team == ChessGame.TeamColor.WHITE) ? 8 : 1;

        // Forward one
        ChessPosition oneForward = board.getPosition(pos.x + direction, pos.y);
        if (oneForward != null && oneForward.occupyingPiece == null) {
            addPawnMove(pos, oneForward, moves, oneForward.x == promotionRow);

            // Forward two
            ChessPosition twoForward = board.getPosition(pos.x + 2 * direction, pos.y);
            if (pos.x == startRow && twoForward != null && twoForward.occupyingPiece == null) {
                moves.add(new ChessMove(pos, twoForward, null));
            }
        }

        // Diagonal captures
        for (int dy : new int[]{-1, 1}) {
            ChessPosition diag = board.getPosition(pos.x + direction, pos.y + dy);
            if (diag != null && diag.occupyingPiece != null &&
                    diag.occupyingPiece.getTeamColor() != team) {
                addPawnMove(pos, diag, moves, diag.x == promotionRow);
            }
        }

        return moves;
    }

    private void addPawnMove(ChessPosition from, ChessPosition to, List<ChessMove> moves, boolean isPromotion) {
        if (isPromotion) {
            for (PieceType promotionType : List.of(PieceType.QUEEN, PieceType.ROOK, PieceType.BISHOP, PieceType.KNIGHT)) {
                moves.add(new ChessMove(from, to, promotionType));
            }
        } else {
            moves.add(new ChessMove(from, to, null));
        }
    }


    private Collection<ChessMove> generateJumpingMoves(ChessBoard board, ChessPosition pos) {
        List<ChessMove> moves = new ArrayList<>();
        int[][] offsets = (pieceType == PieceType.KNIGHT)
                ? new int[][]{{1,-2},{-1,-2},{2,-1},{2,1},{1,2},{-1,2},{-2,-1},{-2,1}}
                : new int[][]{{1,-1},{-1,-1},{-1,0},{1,0},{0,-1},{0,1},{1,1},{-1,1}};

        for (int[] offset : offsets) {
            int dx = offset[0], dy = offset[1];
            ChessPosition target = board.getPosition(pos.x + dx, pos.y + dy);
            if (target != null && (target.occupyingPiece == null ||
                    target.occupyingPiece.getTeamColor() != team)) {
                moves.add(new ChessMove(pos, target, null));
            }
        }

        return moves;
    }

    private Collection<ChessMove> generateSlidingMoves(ChessBoard board, ChessPosition pos) {
        List<ChessMove> moves = new ArrayList<>();

        if (pieceType == PieceType.ROOK || pieceType == PieceType.QUEEN) {
            moves.addAll(addMoves(board, pos, 1, 0));
            moves.addAll(addMoves(board, pos, -1, 0));
            moves.addAll(addMoves(board, pos, 0, -1));
            moves.addAll(addMoves(board, pos, 0, 1));
        }

        if (pieceType == PieceType.BISHOP || pieceType == PieceType.QUEEN) {
            moves.addAll(addMoves(board, pos, 1, -1));
            moves.addAll(addMoves(board, pos, 1, 1));
            moves.addAll(addMoves(board, pos, -1, -1));
            moves.addAll(addMoves(board, pos, -1, 1));
        }

        return moves;
    }
}
