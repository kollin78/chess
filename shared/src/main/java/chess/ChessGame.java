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

    private TeamColor teamTurn;
    private ChessBoard board = new ChessBoard();

    private ChessMove previousMove;

    public ChessGame() {
        this.teamTurn = TeamColor.WHITE;
        this.board.resetBoard();
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

        Collection<ChessMove> testMoves = piece.pieceMoves(board, startPosition);
        Collection<ChessMove> validatedMoves = new ArrayList<>();

        if(piece.getPieceType() == ChessPiece.PieceType.PAWN) {
            enPassant(startPosition, piece, testMoves);
        } else if (piece.getPieceType() == ChessPiece.PieceType.KING) {
            castling(startPosition, piece, testMoves);
        }

        for(ChessMove move : testMoves) {
            ChessPiece tmpTarget = board.getPiece(move.getEndPosition());
            board.addPiece(move.getEndPosition(), piece);
            board.addPiece(move.getStartPosition(), null);

            if(!isInCheck(piece.getTeamColor())) {
                validatedMoves.add(move);
            }

            board.addPiece(move.getStartPosition(), piece);
            board.addPiece(move.getEndPosition(), tmpTarget);
        }

        return validatedMoves;
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        ChessPiece piece = board.getPiece(move.getStartPosition());

        if(piece == null) {
            throw new InvalidMoveException("No such piece");
        } else if (piece.getTeamColor() != teamTurn) {
            throw new InvalidMoveException("Incorrect turn");
        }

        Collection<ChessMove> legalMoves = validMoves(move.getStartPosition());
        if((legalMoves == null) || !legalMoves.contains(move)) {
            throw new InvalidMoveException("Illegal move");
        }

        if(piece.getPieceType() == ChessPiece.PieceType.PAWN) {
            boolean moveDiagonal = move.getStartPosition().getColumn() != move.getEndPosition().getColumn();
            boolean isDestEmpty = board.getPiece(move.getEndPosition()) == null;

            if(moveDiagonal && isDestEmpty) {
                ChessPosition capturedPosition = new ChessPosition(move.getStartPosition().getRow(), move.getEndPosition().getColumn());
                board.addPiece(capturedPosition, null);
            }
        }

        if (piece.getPieceType() == ChessPiece.PieceType.KING) {
            int colDist = move.getEndPosition().getColumn() - move.getStartPosition().getColumn();
            if (Math.abs(colDist) == 2) {
                int row = move.getStartPosition().getRow();
                int rookStartCol = (colDist == 2) ? 8 : 1;
                int rookEndCol = (colDist == 2) ? 6 : 4;

                ChessPiece rook = board.getPiece(new ChessPosition(row, rookStartCol));

                // Move the Rook to the other side of the King
                board.addPiece(new ChessPosition(row, rookEndCol), rook);
                board.addPiece(new ChessPosition(row, rookStartCol), null);

                // MARK THE ROOK AS MOVED
                if (rook != null) {
                    rook.setMoveState(true);
                }
            }
        }

        ChessPiece finalPiece = piece;
        if(move.getPromotionPiece() != null) {
            finalPiece = new ChessPiece(piece.getTeamColor(), move.getPromotionPiece());
        }

        board.addPiece(move.getEndPosition(), finalPiece);
        board.addPiece(move.getStartPosition(), null);

        this.previousMove = move;
        finalPiece.setMoveState(true);

        setTeamTurn(teamTurn == TeamColor.BLACK ? TeamColor.WHITE : TeamColor.BLACK);
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        ChessPiece king = null;
        ChessPosition kingPos = null;
        ChessPosition attackerPos;

        outerLoop:
        for(int i = 1; i < 9; i++) {
            for(int j = 1; j < 9; j++) {
                kingPos = new ChessPosition(i, j);
                if(board.getPiece(kingPos) == null) continue;
                if((board.getPiece(kingPos).getPieceType() == ChessPiece.PieceType.KING) && (board.getPiece(kingPos).getTeamColor() == teamColor)) {
                    king = board.getPiece(kingPos);
                    break outerLoop;
                }
            }
        }

        if (king == null) return false;

        for(int i = 1; i < 9; i++) {
            for(int j = 1; j < 9; j++) {
                attackerPos = new ChessPosition(i, j);
                ChessPiece attacker = board.getPiece(attackerPos);
                if(attacker == null) continue;
                if(attacker.getTeamColor() != teamColor) {
                    for(ChessMove move : attacker.pieceMoves(board, attackerPos)) {
                        if(move.getEndPosition().equals(kingPos)) {
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
                ChessPiece piece = board.getPiece(position);
                if(piece == null) continue;
                if(piece.getTeamColor() == teamColor) {
                    if(!validMoves(position).isEmpty()) {
                        return false;
                    }
                }
            }
        }

        return true;
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
                ChessPiece piece = board.getPiece(position);
                if(piece == null) continue;
                if(piece.getTeamColor() == teamColor) {
                    if(!validMoves(position).isEmpty()) {
                        return false;
                    }
                }
            }
        }

        return true;
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

    private void enPassant(ChessPosition position, ChessPiece piece, Collection<ChessMove> validMoves) {

        if(previousMove == null) return;

        ChessPiece previousPiece = board.getPiece(previousMove.getEndPosition());
        if((previousPiece == null) || (previousPiece.getPieceType() != ChessPiece.PieceType.PAWN)) return;
        else if (Math.abs(previousMove.getStartPosition().getRow() - previousMove.getEndPosition().getRow()) != 2) return;

        if(previousMove.getEndPosition().getRow() == position.getRow()) {
            if(Math.abs(previousMove.getEndPosition().getColumn() - position.getColumn()) == 1) {
                int moveToRow;
                if(piece.getTeamColor() == TeamColor.WHITE) moveToRow = position.getRow() + 1;
                else moveToRow = position.getRow() - 1;
                validMoves.add(new ChessMove(position, new ChessPosition(moveToRow, previousMove.getEndPosition().getColumn()), null));
            }
        }
    }

    private void castling(ChessPosition position, ChessPiece king, Collection<ChessMove> validMoves) {

        if(isInCheck(king.getTeamColor())) return;

        int row;
        if(king.getTeamColor() == TeamColor.WHITE) row = 1;
        else row = 8;

        if(canCastle(row, 8, new int[]{6, 7}, king.getTeamColor())) {
            validMoves.add(new ChessMove(position, new ChessPosition(row, 7), null));
        }
        if(canCastle(row, 1, new int[]{2, 3, 4}, king.getTeamColor())) {
            validMoves.add(new ChessMove(position, new ChessPosition(row, 3), null));
        }
    }

    private boolean canCastle(int row, int col, int[] pathColumns, TeamColor teamColor) {
        ChessPiece rook = board.getPiece(new ChessPosition(row, col));
        ChessPiece king = board.getPiece(new ChessPosition(row, 5));

        if((king == null) || (rook == null) || (king.getMoveState()) || rook.getMoveState()) return false;

        for(int column : pathColumns) {
            if(board.getPiece(new ChessPosition(row, column)) != null) return false;
        }

        ChessPosition startingPosKing = new ChessPosition(row, 5);
        int[] checkSquares;
        if(col == 8) checkSquares = new int[]{6, 7};
        else checkSquares = new int[]{4, 3};

        for (int column : checkSquares) {
            ChessPosition tempPos = new ChessPosition(row, column);
            board.addPiece(tempPos, king);
            board.addPiece(startingPosKing, null);

            boolean badMove = isInCheck(teamColor);

            board.addPiece(startingPosKing, king);
            board.addPiece(tempPos, null);

            if(badMove) return false;
        }

        return true;
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
