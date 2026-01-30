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
            // Check if move is on board and if space is open
            if((isOnBoard(startRow + pawnDir, startCol)) && (board.getPiece(new ChessPosition(startRow + pawnDir, startCol)) == null)) {
                moveToPos = new ChessPosition(startRow + pawnDir, startCol);
                if (startRow+pawnDir == promotionRow) { // Pawn should be promoted
                    for(PieceType type : PieceType.values()) {
                        if(type == PieceType.PAWN || type == PieceType.KING) continue;
                        validMoves.add(new ChessMove(myPosition, moveToPos, type));
                    }
                } else { // Normal moves
                    validMoves.add(new ChessMove(myPosition, moveToPos, null));
                    if(startRow == doubleMoveRow) {
                        moveToPos = new ChessPosition(startRow + pawnDir + pawnDir, startCol);
                        if(isOnBoard(startRow + pawnDir + pawnDir, startCol) && (board.getPiece(new ChessPosition(startRow + pawnDir + pawnDir, startCol)) == null)) {
                            validMoves.add(new ChessMove(myPosition, moveToPos, null));
                        }
                    }
                }
            }
            // Checking sides for capturable pieces
            if(isOnBoard(startRow + pawnDir, startCol + 1) && (board.getPiece(new ChessPosition(startRow + pawnDir, startCol + 1)) != null)) {
                moveToPos = new ChessPosition(startRow + pawnDir, startCol + 1);
                if(canCapture(board, piece, moveToPos)) {
                    if(startRow + pawnDir == promotionRow) {
                        for(PieceType type : PieceType.values()) {
                            if(type == PieceType.PAWN || type == PieceType.KING) continue;
                            validMoves.add(new ChessMove(myPosition, moveToPos, type));
                        }
                    } else {
                        validMoves.add(new ChessMove(myPosition, moveToPos, null));
                    }
                }
            }
            if(isOnBoard(startRow + pawnDir, startCol - 1) && (board.getPiece(new ChessPosition(startRow + pawnDir, startCol - 1)) != null)) {
                moveToPos = new ChessPosition(startRow + pawnDir, startCol - 1);
                if(canCapture(board, piece, moveToPos)) {
                    if(startRow + pawnDir == promotionRow) {
                        for(PieceType type : PieceType.values()) {
                            if(type == PieceType.PAWN || type == PieceType.KING) continue;
                            validMoves.add(new ChessMove(myPosition, moveToPos, type));
                        }
                    } else {
                        validMoves.add(new ChessMove(myPosition, moveToPos, null));
                    }
                }
            }


            return validMoves;
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
