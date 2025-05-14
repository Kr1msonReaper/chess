package chess;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;

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

            return (obj instanceof ChessPiece) && (Objects.hash(team.name() + pieceType.name()) == Objects.hash(castedObj.team.name() + castedObj.pieceType.name()));
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

        for(ChessMove move : moves){
            if ((move.endPos.x >= 1 && move.endPos.x <= 8) && (move.endPos.y >= 1 && move.endPos.y <= 8)){

                if(pieceType == PieceType.PAWN){
                    if(move.endPos.x == 8 && team == ChessGame.TeamColor.WHITE){
                        move.proPiece = PieceType.QUEEN;
                        ChessMove move1 = new ChessMove(move);
                        move1.proPiece = PieceType.KNIGHT;
                        ChessMove move2 = new ChessMove(move);
                        move2.proPiece = PieceType.BISHOP;
                        ChessMove move3 = new ChessMove(move);
                        move3.proPiece = PieceType.ROOK;
                        validatedMoves.add(move);
                        validatedMoves.add(move1);
                        validatedMoves.add(move2);
                        validatedMoves.add(move3);
                        continue;
                    }
                    if(move.endPos.x == 1 && team == ChessGame.TeamColor.BLACK){
                        move.proPiece = PieceType.QUEEN;
                        ChessMove move1 = new ChessMove(move);
                        move1.proPiece = PieceType.KNIGHT;
                        ChessMove move2 = new ChessMove(move);
                        move2.proPiece = PieceType.BISHOP;
                        ChessMove move3 = new ChessMove(move);
                        move3.proPiece = PieceType.ROOK;
                        validatedMoves.add(move);
                        validatedMoves.add(move1);
                        validatedMoves.add(move2);
                        validatedMoves.add(move3);
                        continue;
                    }
                }

                if(pieceType == PieceType.KING && getTeamColor() == ChessGame.TeamColor.WHITE){
                    int test = 1;
                }

                validatedMoves.add(move);
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
                break;
            } else {
                break;
            }
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

        if (pieceType == PieceType.PAWN && team == ChessGame.TeamColor.WHITE){

            ChessPosition forwardPos = board.getPosition(myPosition.x + 1, myPosition.y);
            if(forwardPos != null){
                ChessMove forwardMove = new ChessMove(myPosition, forwardPos, null);
                if (forwardPos.occupyingPiece == null){ potentialMoves.add(forwardMove); }
            }

            ChessPosition diagLeftPos = board.getPosition(myPosition.x + 1, myPosition.y - 1);
            if(diagLeftPos != null){
                ChessMove diagLeftMove = new ChessMove(myPosition, diagLeftPos, null);
                if (diagLeftPos.occupyingPiece != null && diagLeftPos.occupyingPiece.getTeamColor() == ChessGame.TeamColor.BLACK){ potentialMoves.add(diagLeftMove); }
            }

            ChessPosition diagRightPos = board.getPosition(myPosition.x + 1, myPosition.y + 1);
            if(diagRightPos != null){
                ChessMove diagRightMove = new ChessMove(myPosition, diagRightPos, null);
                if (diagRightPos.occupyingPiece != null && diagRightPos.occupyingPiece.getTeamColor() == ChessGame.TeamColor.BLACK){ potentialMoves.add(diagRightMove); }
            }


            ChessPosition doubleForwardPos = board.getPosition(myPosition.x + 2, myPosition.y);
            if(doubleForwardPos != null){
                ChessMove doubleForwardMove = new ChessMove(myPosition, doubleForwardPos, null);
                if (doubleForwardPos.occupyingPiece == null && forwardPos.occupyingPiece == null && (myPosition.x == 2 && team == ChessGame.TeamColor.WHITE)){ potentialMoves.add(doubleForwardMove); }
            }

        } else if(pieceType == PieceType.PAWN && team == ChessGame.TeamColor.BLACK){

            ChessPosition forwardPos = board.getPosition(myPosition.x - 1, myPosition.y);
            if(forwardPos != null){
                ChessMove forwardMove = new ChessMove(myPosition, forwardPos, null);
                if (forwardPos.occupyingPiece == null){ potentialMoves.add(forwardMove); }
            }

            ChessPosition diagLeftPos = board.getPosition(myPosition.x - 1, myPosition.y - 1);
            if(diagLeftPos != null){
                ChessMove diagLeftMove = new ChessMove(myPosition, diagLeftPos, null);
                if (diagLeftPos.occupyingPiece != null && diagLeftPos.occupyingPiece.getTeamColor() == ChessGame.TeamColor.WHITE){ potentialMoves.add(diagLeftMove); }
            }

            ChessPosition diagRightPos = board.getPosition(myPosition.x - 1, myPosition.y + 1);
            if(diagRightPos != null){
                ChessMove diagRightMove = new ChessMove(myPosition, diagRightPos, null);
                if (diagRightPos.occupyingPiece != null && diagRightPos.occupyingPiece.getTeamColor() == ChessGame.TeamColor.WHITE){ potentialMoves.add(diagRightMove); }
            }

            ChessPosition doubleForwardPos = board.getPosition(myPosition.x - 2, myPosition.y);
            if(doubleForwardPos != null){
                ChessMove doubleForwardMove = new ChessMove(myPosition, doubleForwardPos, null);
                if (doubleForwardPos.occupyingPiece == null && forwardPos.occupyingPiece == null && (myPosition.x == 7 && team == ChessGame.TeamColor.BLACK)){ potentialMoves.add(doubleForwardMove); }
            }
        }

        if (pieceType == PieceType.KNIGHT){
            ChessPosition leftUp = board.getPosition(myPosition.x + 1, myPosition.y - 2);
            if(leftUp != null){
                ChessMove moveLeftUp = new ChessMove(myPosition, leftUp, null);
                if (leftUp.occupyingPiece == null || leftUp.occupyingPiece.getTeamColor() != team){ potentialMoves.add(moveLeftUp); }
            }

            ChessPosition leftDown = board.getPosition(myPosition.x - 1, myPosition.y - 2);
            if(leftDown != null){
                ChessMove moveLeftDown = new ChessMove(myPosition, leftDown, null);
                if (leftDown.occupyingPiece == null || leftDown.occupyingPiece.getTeamColor() != team){ potentialMoves.add(moveLeftDown); }
            }


            ChessPosition upLeft = board.getPosition(myPosition.x + 2, myPosition.y - 1);
            if(upLeft != null){
                ChessMove moveUpLeft = new ChessMove(myPosition, upLeft, null);
                if (upLeft.occupyingPiece == null || upLeft.occupyingPiece.getTeamColor() != team){ potentialMoves.add(moveUpLeft); }
            }


            ChessPosition upRight = board.getPosition(myPosition.x + 2, myPosition.y + 1);
            if(upRight != null){
                ChessMove moveUpRight = new ChessMove(myPosition, upRight, null);
                if (upRight.occupyingPiece == null || upRight.occupyingPiece.getTeamColor() != team){ potentialMoves.add(moveUpRight); }
            }


            ChessPosition rightUp = board.getPosition(myPosition.x + 1, myPosition.y + 2);
            if(rightUp != null){
                ChessMove moveRightUp = new ChessMove(myPosition, rightUp, null);
                if (rightUp.occupyingPiece == null || rightUp.occupyingPiece.getTeamColor() != team){ potentialMoves.add(moveRightUp); }
            }



            ChessPosition rightDown = board.getPosition(myPosition.x - 1, myPosition.y + 2);
            if(rightDown != null){
                ChessMove moveRightDown = new ChessMove(myPosition, rightDown, null);
                if (rightDown.occupyingPiece == null || rightDown.occupyingPiece.getTeamColor() != team){ potentialMoves.add(moveRightDown); }
            }



            ChessPosition downLeft = board.getPosition(myPosition.x - 2, myPosition.y - 1);
            if(downLeft != null){
                ChessMove moveDownLeft = new ChessMove(myPosition, downLeft, null);
                if (downLeft.occupyingPiece == null || downLeft.occupyingPiece.getTeamColor() != team){ potentialMoves.add(moveDownLeft); }
            }

            ChessPosition downRight = board.getPosition(myPosition.x - 2, myPosition.y + 1);
            if(downRight != null){
                ChessMove moveDownRight = new ChessMove(myPosition, downRight, null);
                if (downRight.occupyingPiece == null || downRight.occupyingPiece.getTeamColor() != team){ potentialMoves.add(moveDownRight); }
            }

        }

        if (pieceType == PieceType.KING){
            ChessPosition leftUp = board.getPosition(myPosition.x + 1, myPosition.y - 1);
            if(leftUp != null){
                ChessMove moveLeftUp = new ChessMove(myPosition, leftUp, null);
                if (leftUp.occupyingPiece == null || leftUp.occupyingPiece.getTeamColor() != team){ potentialMoves.add(moveLeftUp); }
            }


            ChessPosition leftDown = board.getPosition(myPosition.x - 1, myPosition.y - 1);
            if(leftDown != null){
                ChessMove moveLeftDown = new ChessMove(myPosition, leftDown, null);
                if (leftDown.occupyingPiece == null || leftDown.occupyingPiece.getTeamColor() != team){ potentialMoves.add(moveLeftDown); }
            }


            ChessPosition upLeft = board.getPosition(myPosition.x - 1, myPosition.y);
            if(upLeft != null){
                ChessMove moveUpLeft = new ChessMove(myPosition, upLeft, null);
                if (upLeft.occupyingPiece == null || upLeft.occupyingPiece.getTeamColor() != team){ potentialMoves.add(moveUpLeft); }
            }


            ChessPosition upRight = board.getPosition(myPosition.x + 1, myPosition.y);
            if(upRight != null){
                ChessMove moveUpRight = new ChessMove(myPosition, upRight, null);
                if (upRight.occupyingPiece == null || upRight.occupyingPiece.getTeamColor() != team){ potentialMoves.add(moveUpRight); }
            }


            ChessPosition rightUp = board.getPosition(myPosition.x, myPosition.y - 1);
            if(rightUp != null){
                ChessMove moveRightUp = new ChessMove(myPosition, rightUp, null);
                if (rightUp.occupyingPiece == null || rightUp.occupyingPiece.getTeamColor() != team){ potentialMoves.add(moveRightUp); }
            }


            ChessPosition rightDown = board.getPosition(myPosition.x, myPosition.y + 1);
            if(rightDown != null){
                ChessMove moveRightDown = new ChessMove(myPosition, rightDown, null);
                if (rightDown.occupyingPiece == null || rightDown.occupyingPiece.getTeamColor() != team){ potentialMoves.add(moveRightDown); }
            }


            ChessPosition downLeft = board.getPosition(myPosition.x + 1, myPosition.y + 1);
            if(downLeft != null){
                ChessMove moveDownLeft = new ChessMove(myPosition, downLeft, null);
                if (downLeft.occupyingPiece == null || downLeft.occupyingPiece.getTeamColor() != team){ potentialMoves.add(moveDownLeft); }
            }


            ChessPosition downRight = board.getPosition(myPosition.x - 1, myPosition.y + 1);
            if(downRight != null) {
                ChessMove moveDownRight = new ChessMove(myPosition, downRight, null);
                if (downRight.occupyingPiece == null || downRight.occupyingPiece.getTeamColor() != team) {
                    potentialMoves.add(moveDownRight);
                }
            }
        }

        if(pieceType == PieceType.ROOK){
            Collection<ChessMove> up = addMoves(board, myPosition, 1, 0);
            Collection<ChessMove> down = addMoves(board, myPosition, -1, 0);
            Collection<ChessMove> left = addMoves(board, myPosition, 0, -1);
            Collection<ChessMove> right = addMoves(board, myPosition, 0, 1);
            potentialMoves.addAll(up);
            potentialMoves.addAll(down);
            potentialMoves.addAll(left);
            potentialMoves.addAll(right);
        }

        if(pieceType == PieceType.QUEEN){
            Collection<ChessMove> up = addMoves(board, myPosition, 1, 0);
            Collection<ChessMove> upLeft = addMoves(board, myPosition, 1, -1);
            Collection<ChessMove> upRight = addMoves(board, myPosition, 1, 1);
            Collection<ChessMove> down = addMoves(board, myPosition, -1, 0);
            Collection<ChessMove> downLeft = addMoves(board, myPosition, -1, -1);
            Collection<ChessMove> downRight = addMoves(board, myPosition, -1, 1);
            Collection<ChessMove> left = addMoves(board, myPosition, 0, -1);
            Collection<ChessMove> right = addMoves(board, myPosition, 0, 1);
            potentialMoves.addAll(up);
            potentialMoves.addAll(upLeft);
            potentialMoves.addAll(upRight);
            potentialMoves.addAll(down);
            potentialMoves.addAll(downLeft);
            potentialMoves.addAll(downRight);
            potentialMoves.addAll(left);
            potentialMoves.addAll(right);
        }

        if(pieceType == PieceType.BISHOP){
            Collection<ChessMove> upLeft = addMoves(board, myPosition, 1, -1);
            Collection<ChessMove> upRight = addMoves(board, myPosition, 1, 1);
            Collection<ChessMove> downLeft = addMoves(board, myPosition, -1, -1);
            Collection<ChessMove> downRight = addMoves(board, myPosition, -1, 1);
            potentialMoves.addAll(upLeft);
            potentialMoves.addAll(upRight);
            potentialMoves.addAll(downLeft);
            potentialMoves.addAll(downRight);
        }

        return validateMoves(potentialMoves);
    }
}
