package chess;

import java.lang.reflect.Array;
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
    private boolean hasMoved = false;


    public ChessPiece(ChessGame.TeamColor pieceColor, PieceType type) {
        this.pieceColor = pieceColor;
        this.type = type;
    }

    public boolean getMoveState() {
        return hasMoved;
    }

    public void setMoveState(boolean moveState) {
        hasMoved = moveState;
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
    
    private boolean isOnBoard(int row, int col) {
        return (row>=1 && row<=8 && col>=1 && col<=8);
    }

    private boolean canCapture(ChessBoard board, ChessPiece piece, ChessPosition moveTo) {
        ChessPiece target = board.getPiece(moveTo);

        return (target.getTeamColor() != piece.getTeamColor());
    }

    private ArrayList<ChessMove> slidingMoves(ChessBoard board, ChessPiece piece, ChessPosition moveFrom, int[][] dirArray) {
        ArrayList<ChessMove> validMoves = new ArrayList<>();
        for(int[] dir : dirArray) {
            int row = moveFrom.getRow();
            int col = moveFrom.getColumn();

            while(true) {
                row+=dir[0];
                col+=dir[1];

                if(!isOnBoard(row, col)) {
                    break;
                } else {
                    ChessPosition moveTo = new ChessPosition(row, col);
                    if(board.getPiece(moveTo) == null) {
                        ChessMove validMove = new ChessMove(moveFrom, moveTo, null);
                        validMoves.add(validMove);
                    } else if (canCapture(board, piece, moveTo)) {
                        ChessMove validMove = new ChessMove(moveFrom, moveTo, null);
                        validMoves.add(validMove);
                        break;
                    } else {
                        break;
                    }
                }
            }
        }

        return validMoves;
    }

    private ArrayList<ChessMove> steppingMoves(ChessBoard board, ChessPiece piece, ChessPosition moveFrom, int[][] dirArray) {
        ArrayList<ChessMove> validMoves = new ArrayList<>();

        for(int[] dir : dirArray) {
            int row = moveFrom.getRow() + dir[0];
            int col = moveFrom.getColumn() + dir[1];

            if(!isOnBoard(row,col)) {
                continue;
            } else {
                ChessPosition moveTo = new ChessPosition(row, col);
                if ((board.getPiece(moveTo) == null) || (canCapture(board, piece, moveTo))) {
                    ChessMove validMove = new ChessMove(moveFrom, moveTo, null);
                    validMoves.add(validMove);
                }
            }
        }

        return validMoves;
    }

    private ArrayList<ChessMove> pawnMoves(ChessBoard board, ChessPiece piece, ChessPosition moveFrom) {
        ArrayList<ChessMove> validMoves = new ArrayList<>();

        int startRow = moveFrom.getRow();
        int startCol = moveFrom.getColumn();

        int pawnDir;
        int promotionRow;
        int doubleMoveRow;
        if(piece.getTeamColor() == ChessGame.TeamColor.WHITE) {
            pawnDir = 1;
            promotionRow = 8;
            doubleMoveRow = 2;
        } else {
            pawnDir = -1;
            promotionRow = 1;
            doubleMoveRow = 7;
        }

        int[][] dirArray = {{pawnDir, 1}, {pawnDir, -1}};

        int rowPlus = startRow + pawnDir;

        // first square forward
        if(!isOnBoard(rowPlus, startCol)) {
            return validMoves;
        }
        ChessPosition moveTo = new ChessPosition(rowPlus, startCol);

        if(board.getPiece(moveTo) == null) {
            handlePawnMoves(validMoves, moveFrom, moveTo, promotionRow);

            // if pawn's first move (i.e. can double move)
            if(startRow == doubleMoveRow) {
                int rowPlusPlus = rowPlus + pawnDir;
                moveTo = new ChessPosition(rowPlusPlus, startCol);
                if(isOnBoard(rowPlusPlus, startCol)) {
                    if(board.getPiece(moveTo) == null) {
                        validMoves.add(new ChessMove(moveFrom, moveTo, null));
                    }
                }
            }
        }

        for(int[] dir : dirArray) {
            int row = startRow + dir[0];
            int col = startCol + dir[1];

            if(!isOnBoard(row, col)) {
                continue;
            } else {
                ChessPosition moveTo = new ChessPosition(row, col);
                if ((board.getPiece(moveTo) != null) && (canCapture(board, piece, moveTo))) {
                    handlePawnMoves(validMoves, moveFrom, moveTo, promotionRow);
                }
            }
        }

        return validMoves;
    }

    private void handlePawnMoves(ArrayList<ChessMove> validMoves, ChessPosition moveFrom, ChessPosition moveTo, int promotionRow) {
        if(moveTo.getRow() == promotionRow) {
            for(PieceType pieceType : PieceType.values()) {
                if((pieceType == PieceType.PAWN) || (pieceType == PieceType.KING)) {
                    continue;
                }
                validMoves.add(new ChessMove(moveFrom, moveTo, pieceType));
            }
        } else {
            validMoves.add(new ChessMove(moveFrom, moveTo, null));
        }
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
        ArrayList<ChessMove> validMoves = new ArrayList<>();
        ChessPosition moveToPos;

        int startRow = myPosition.getRow();
        int startCol = myPosition.getColumn();

        int[][] bishopDirs = {{1,1}, {1,-1}, {-1,1}, {-1,-1}};
        int[][] queenDirs = {{1,0}, {0,1}, {1,1}, {-1,0}, {0,-1}, {-1,-1}, {-1,1}, {1,-1}};
        int[][] rookDirs = {{1,0}, {0,1}, {-1,0}, {0,-1}};
        int[][] kingDirs = {{1,0}, {1,1}, {0,1}, {-1,0}, {-1,-1}, {0,-1}, {-1,1}, {1,-1}};
        int[][] knightDirs = {{2,1}, {-2,1}, {2,-1}, {-2,-1}, {1,2}, {-1,2}, {1,-2}, {-1,-2}};


        /*----------     Bishop     ----------*/
        if(piece.getPieceType() == PieceType.BISHOP) {

            return slidingMoves(board, piece, myPosition, bishopDirs);
        }
        /*----------     KING     ----------*/
        else if (piece.getPieceType() == PieceType.KING) {

            return steppingMoves(board, piece, myPosition, kingDirs);
        }
        /*----------     KNIGHT     ----------*/
        else if (piece.getPieceType() == PieceType.KNIGHT) {

            return steppingMoves(board, piece, myPosition, knightDirs);
        }
        /*----------     PAWN     ----------*/
        else if (piece.getPieceType() == PieceType.PAWN) {

            return pawnMoves(board, piece, myPosition);
        }
        /*----------     QUEEN     ----------*/
        else if (piece.getPieceType() == PieceType.QUEEN) {

            return slidingMoves(board, piece,myPosition, queenDirs);
        }
        /*----------     ROOK     ----------*/
        else if (piece.getPieceType() == PieceType.ROOK) {

            return slidingMoves(board, piece, myPosition, rookDirs);
        }

        return List.of();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessPiece that = (ChessPiece) o;
        return pieceColor == that.pieceColor && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pieceColor, type);
    }

}
