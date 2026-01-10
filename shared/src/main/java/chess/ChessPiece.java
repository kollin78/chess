package chess;

import java.util.*;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {

    private final ChessGame.TeamColor pieceColor;
    private final PieceType type;

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.pieceColor = pieceColor;
        this.type = type;
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
        return pieceColor;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return type;
    }

    private Set<ChessMove> canCapture(ChessPiece target, ChessPiece attacker, ChessPosition moveFrom, ChessPosition moveTo, Set<ChessMove> validMoves) {
        if(target.getTeamColor() != attacker.getTeamColor()) {
            validMoves.add(new ChessMove(moveFrom, moveTo, null));
        }
        return validMoves;
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        ChessPiece piece = board.getPiece(myPosition);
        Set<ChessMove> validMoves = new LinkedHashSet<>();
        ChessMove newMove;
        ChessPosition moveToPos;

        int startRow = myPosition.getRow();
        int startCol = myPosition.getColumn();

        if(piece.getPieceType() == PieceType.BISHOP) {

            for(int i = 1; i < 9; i++) {
                if((startRow+i < 9) && (startCol+i < 9)) {
                    moveToPos = new ChessPosition(startRow+i, startCol+i);
                    ChessPiece target = board.getPiece(moveToPos);
                    if (target != null) {
                        validMoves = canCapture(target, piece, myPosition, moveToPos, validMoves);
                        break;
                    } else {
                        newMove = new ChessMove(myPosition, moveToPos, null);
                        validMoves.add(newMove);
                    }
                }
            }
            for(int i = 1; i < 9; i++) {
                if((startRow-i > 0) && (startCol+i < 9)) {
                    moveToPos = new ChessPosition(startRow-i, startCol+i);
                    ChessPiece target = board.getPiece(moveToPos);
                    if (target != null) {
                        validMoves = canCapture(target, piece, myPosition, moveToPos, validMoves);
                        break;
                    } else {
                        newMove = new ChessMove(myPosition, moveToPos, null);
                        validMoves.add(newMove);
                    }
                }
            }
            for(int i = 1; i < 9; i++) {
                if((startRow-i > 0) && (startCol-i > 0)) {
                    moveToPos = new ChessPosition(startRow - i, startCol - i);
                    ChessPiece target = board.getPiece(moveToPos);
                    if (target != null) {
                        validMoves = canCapture(target, piece, myPosition, moveToPos, validMoves);
                        break;
                    } else {
                        newMove = new ChessMove(myPosition, moveToPos, null);
                        validMoves.add(newMove);
                    }
                }
            }
            for(int i = 1; i < 9; i++) {
                if((startRow+i < 9) && (startCol-i > 0)) {
                    moveToPos = new ChessPosition(startRow+i, startCol-i);
                    ChessPiece target = board.getPiece(moveToPos);
                    if (target != null) {
                        validMoves = canCapture(target, piece, myPosition, moveToPos, validMoves);
                        break;
                    } else {
                        newMove = new ChessMove(myPosition, moveToPos, null);
                        validMoves.add(newMove);
                    }
                }
            }

            return new ArrayList<>(validMoves);
        } else if (piece.getPieceType() == PieceType.KING) {

            int[][] potentialDirections = {
                    {1,0}, {1,1}, {0,1}, {-1,0}, {-1,-1}, {0,-1}, {-1,1}, {1,-1}
            };
            for(int[] vectorPair : potentialDirections) {
                int rowNext = myPosition.getRow() + vectorPair[0];
                int colNext = myPosition.getColumn() + vectorPair[1];
                if((rowNext > 8) || (rowNext < 1) || (colNext > 8) || (colNext < 1)) {
                    continue;
                }
                moveToPos = new ChessPosition(rowNext, colNext);
                ChessPiece target = board.getPiece(moveToPos);
                if (target != null) {
                    validMoves = canCapture(target, piece, myPosition, moveToPos, validMoves);
                } else {
                    newMove = new ChessMove(myPosition, moveToPos, null);
                    validMoves.add(newMove);
                }
            }
            return new ArrayList<>(validMoves);
        } else if (piece.getPieceType() == PieceType.KNIGHT) {
            int [][] potentialDirections = {
                    {2,1}, {1,2}, {-2,1}, {2,-1}, {-2,-1}, {-1,2}, {-1,-2}, {1,-2}
            };
            for(int[] vectorPair : potentialDirections) {
                int rowNext = myPosition.getRow() + vectorPair[0];
                int colNext = myPosition.getColumn() + vectorPair[1];
                if((rowNext > 8) || (rowNext < 1) || (colNext > 8) || (colNext < 1)) {
                    continue;
                }
                moveToPos = new ChessPosition(rowNext, colNext);
                ChessPiece target = board.getPiece(moveToPos);
                if (target != null) {
                    validMoves = canCapture(target, piece, myPosition, moveToPos, validMoves);
                } else {
                    newMove = new ChessMove(myPosition, moveToPos, null);
                    validMoves.add(newMove);
                }
            }
            return new ArrayList<>(validMoves);
        }

        return List.of();
    }
}
