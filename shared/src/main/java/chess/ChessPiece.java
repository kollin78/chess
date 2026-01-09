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

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        ChessPiece piece = board.getPiece(myPosition);
        List<ChessMove> validMoves = new ArrayList<>();
        ChessMove newMove;
        ChessPosition moveToPos;

        if(piece.getPieceType() == PieceType.BISHOP) {
            int startRow = myPosition.getRow();
            int startCol = myPosition.getColumn();

            Set<ChessMove> setMoves = new LinkedHashSet<>(validMoves);


            for(int i = 1; i < 8; i++) {
                if((startRow+i < 8) && (startCol+i < 8)) {
                    moveToPos = new ChessPosition(startRow+i, startCol+i);
                    newMove = new ChessMove(myPosition, moveToPos, null);
                    setMoves.add(newMove);
                }
            }
            for(int i = 1; i < 8; i++) {
                if((startRow-i >= 0) && (startCol+i < 8)) {
                    moveToPos = new ChessPosition(startRow-i, startCol+i);
                    newMove = new ChessMove(myPosition, moveToPos, null);
                    setMoves.add(newMove);
                }
            }
            for(int i = 1; i < 8; i++) {
                if((startRow-i >= 0) && (startCol-i >= 0)) {
                    moveToPos = new ChessPosition(startRow-i, startCol-i);
                    newMove = new ChessMove(myPosition, moveToPos, null);
                    setMoves.add(newMove);
                }
            }
            for(int i = 1; i < 8; i++) {
                if((startRow+i < 8) && (startCol-i >= 0)) {
                    moveToPos = new ChessPosition(startRow+i, startCol-i);
                    newMove = new ChessMove(myPosition, moveToPos, null);
                    setMoves.add(newMove);
                }
            }

            return new ArrayList<>(setMoves);
            //return List.of(new ChessMove(new ChessPosition(5,4), new ChessPosition(1,8), null));
        }

        return List.of();
    }
}
