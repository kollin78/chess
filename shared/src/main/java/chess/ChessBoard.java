package chess;

import java.util.Arrays;
import java.util.Objects;

/**
 * A chessboard that can hold and rearrange chess pieces.
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessBoard {

    ChessPiece[][] squares = new ChessPiece[8][8];
    public ChessBoard() {

    }

    /**
     * Adds a chess piece to the chessboard
     *
     * @param position where to add the piece to
     * @param piece    the piece to add
     */
    public void addPiece(ChessPosition position, ChessPiece piece) {
        squares[position.getRow() - 1][position.getColumn() - 1] = piece;
    }

    /**
     * Gets a chess piece on the chessboard
     *
     * @param position The position to get the piece from
     * @return Either the piece at the position, or null if no piece is at that
     * position
     */
    public ChessPiece getPiece(ChessPosition position) {
        return squares[position.getRow() - 1][position.getColumn() - 1];
    }


    private void addBackRow(int row, ChessGame.TeamColor teamColor) {
        addPiece(new ChessPosition(row, 1), new ChessPiece(teamColor, ChessPiece.PieceType.ROOK));
        addPiece(new ChessPosition(row, 2), new ChessPiece(teamColor, ChessPiece.PieceType.KNIGHT));
        addPiece(new ChessPosition(row, 3), new ChessPiece(teamColor, ChessPiece.PieceType.BISHOP));
        addPiece(new ChessPosition(row, 4), new ChessPiece(teamColor, ChessPiece.PieceType.QUEEN));
        addPiece(new ChessPosition(row, 5), new ChessPiece(teamColor, ChessPiece.PieceType.KING));
        addPiece(new ChessPosition(row, 6), new ChessPiece(teamColor, ChessPiece.PieceType.BISHOP));
        addPiece(new ChessPosition(row, 7), new ChessPiece(teamColor, ChessPiece.PieceType.KNIGHT));
        addPiece(new ChessPosition(row, 8), new ChessPiece(teamColor, ChessPiece.PieceType.ROOK));
    }

    private void addPawns(int row, ChessGame.TeamColor teamColor) {
        for(int i = 1; i < 9; i++) {
            addPiece(new ChessPosition(row, i), new ChessPiece(teamColor, ChessPiece.PieceType.PAWN));
        }
    }

    /**
     * Sets the board to the default starting board
     * (How the game of chess normally starts)
     */
    public void resetBoard() {
        squares = new ChessPiece[8][8];

        addPawns(2, ChessGame.TeamColor.WHITE);
        addPawns(7, ChessGame.TeamColor.BLACK);

        addBackRow(1, ChessGame.TeamColor.WHITE);
        addBackRow(8, ChessGame.TeamColor.BLACK);

    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessBoard that = (ChessBoard) o;
        return Objects.deepEquals(squares, that.squares);
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(squares);
    }
}
