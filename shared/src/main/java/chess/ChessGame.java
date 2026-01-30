package chess;

import java.util.Collection;
import java.util.Objects;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {

    private TeamColor teamTurn;
    private ChessBoard board = new ChessBoard();

    public ChessGame() {

    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return teamTurn;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        teamTurn = team;
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
        ChessPiece piece = board.getPiece(startPosition);

        return piece.pieceMoves(board, startPosition);
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        ChessPiece piece = board.getPiece(move.getEndPosition());

        if(piece == null) {
            throw new InvalidMoveException("No such piece");
        } else if (piece.getTeamColor() != teamTurn) {
            throw new InvalidMoveException("Incorrect turn");
        }

        Collection<ChessMove> legalMoves = validMoves(move.getStartPosition());
        if((legalMoves == null) || !legalMoves.contains(move)) {
            throw new InvalidMoveException("Illegal move");
        }



        setTeamTurn(teamTurn == TeamColor.BLACK ? TeamColor.WHITE : TeamColor.BLACK);

    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        ChessPiece king;
        ChessPosition kingPos = null;
        ChessPosition attackerPos;
        for(int i = 1; i < 9; i++) {
            for(int j = 1; j < 9; j++) {
                kingPos = new ChessPosition(i, j);
                if((board.getPiece(kingPos).getPieceType() == ChessPiece.PieceType.KING) && (board.getPiece(kingPos).getTeamColor() == teamColor)) {
                    king = board.getPiece(kingPos);
                    break;
                }
            }
        }

        for(int i = 1; i < 9; i++) {
            for(int j = 1; j < 9; j++) {
                attackerPos = new ChessPosition(i, j);
                if(board.getPiece(attackerPos).getTeamColor() != teamColor) {
                    for(ChessMove move : validMoves(attackerPos)) {
                        if(move.getEndPosition() == kingPos) {
                            return true;
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
        if(!isInCheck(teamColor)) {
            return false;
        }

        ChessPosition position;

        for(int i = 1; i < 9; i++) {
            for(int j = 1; j < 9; j++) {
                position = new ChessPosition(i, j);
                if(board.getPiece(position).getTeamColor() == teamColor) {
                    if(validMoves(position) != null) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves while not in check.
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        if(isInCheck(teamColor)) {
            return false;
        }

        ChessPosition position;

        for(int i = 1; i < 9; i++) {
            for(int j = 1; j < 9; j++) {
                position = new ChessPosition(i, j);
                if(board.getPiece(position).getTeamColor() == teamColor) {
                    if(validMoves(position) == null) {
                        return true;
                    }
                }
            }
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
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return this.board;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessGame chessGame = (ChessGame) o;
        return teamTurn == chessGame.teamTurn && Objects.equals(board, chessGame.board);
    }

    @Override
    public int hashCode() {
        return Objects.hash(teamTurn, board);
    }
}
