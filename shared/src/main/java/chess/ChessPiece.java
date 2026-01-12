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

//    private Set<ChessMove> canCapture(ChessPiece target, ChessPiece attacker, ChessPosition moveFrom, ChessPosition moveTo, Set<ChessMove> validMoves) {
//        if(target.getTeamColor() != attacker.getTeamColor()) {
//            validMoves.add(new ChessMove(moveFrom, moveTo, null));
//        }
//        return validMoves;
//    }
//
//    private ArrayList<ChessMove> canCapture(ChessPiece target, ChessPiece attacker, ChessPosition moveFrom, ChessPosition moveTo, ArrayList<ChessMove> validMovesList) {
//        if (target.getTeamColor() != attacker.getTeamColor()) {
//            validMovesList.add(new ChessMove(moveFrom, moveTo, null));
//        }
//        return validMovesList;
//    }

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
                break;
            } else {
                ChessPosition moveTo = new ChessPosition(row, col);
                if (board.getPiece(moveTo) == null) {
                    ChessMove validMove = new ChessMove(moveFrom, moveTo, null);
                    validMoves.add(validMove);
                } else if (canCapture(board, piece, moveTo)) {
                    ChessMove validMove = new ChessMove(moveFrom, moveTo, null);
                    validMoves.add(validMove);
                } else {
                    break;
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
        //Set<ChessMove> validMoves = new LinkedHashSet<>();
        ArrayList<ChessMove> validMoves = new ArrayList<>();
        ChessMove newMove;
        ChessPosition moveToPos;

        int startRow = myPosition.getRow();
        int startCol = myPosition.getColumn();

        int[][] bishopDirs = {{1,1}, {1,-1}, {-1,1}, {-1,-1}};
        int[][] queenDirs = {{1,0}, {0,1}, {1,1}, {-1,0}, {0,-1}, {-1,-1}, {-1,1}, {1,-1}};
        int[][] rookDirs = {{1,0}, {0,1}, {-1,0}, {0,-1}};
        int[][] kingDirs = {{1,0}, {1,1}, {0,1}, {-1,0}, {-1,-1}, {0,-1}, {-1,1}, {1,-1}};
        if(piece.getTeamColor() == ChessGame.TeamColor.WHITE) {
            int[][] pawnDirs = {{1, 0}};
            int promotionRow = 8;
        } else {
            int[][] pawnDirs = {{-1,0}};
            int promotionRow = 1;
        }

        /*----------     Bishop     ----------*/
        if(piece.getPieceType() == PieceType.BISHOP) {

            return slidingMoves(board, piece, myPosition, bishopDirs);

//            for(int i = 1; i < 9; i++) {
//                if((startRow+i < 9) && (startCol+i < 9)) {
//                    moveToPos = new ChessPosition(startRow+i, startCol+i);
//                    ChessPiece target = board.getPiece(moveToPos);
//                    if (target != null) {
//                        validMoves = canCapture(target, piece, myPosition, moveToPos, validMoves);
//                        break;
//                    } else {
//                        newMove = new ChessMove(myPosition, moveToPos, null);
//                        validMoves.add(newMove);
//                    }
//                }
//            }
//            for(int i = 1; i < 9; i++) {
//                if((startRow-i > 0) && (startCol+i < 9)) {
//                    moveToPos = new ChessPosition(startRow-i, startCol+i);
//                    ChessPiece target = board.getPiece(moveToPos);
//                    if (target != null) {
//                        validMoves = canCapture(target, piece, myPosition, moveToPos, validMoves);
//                        break;
//                    } else {
//                        newMove = new ChessMove(myPosition, moveToPos, null);
//                        validMoves.add(newMove);
//                    }
//                }
//            }
//            for(int i = 1; i < 9; i++) {
//                if((startRow-i > 0) && (startCol-i > 0)) {
//                    moveToPos = new ChessPosition(startRow - i, startCol - i);
//                    ChessPiece target = board.getPiece(moveToPos);
//                    if (target != null) {
//                        validMoves = canCapture(target, piece, myPosition, moveToPos, validMoves);
//                        break;
//                    } else {
//                        newMove = new ChessMove(myPosition, moveToPos, null);
//                        validMoves.add(newMove);
//                    }
//                }
//            }
//            for(int i = 1; i < 9; i++) {
//                if((startRow+i < 9) && (startCol-i > 0)) {
//                    moveToPos = new ChessPosition(startRow+i, startCol-i);
//                    ChessPiece target = board.getPiece(moveToPos);
//                    if (target != null) {
//                        validMoves = canCapture(target, piece, myPosition, moveToPos, validMoves);
//                        break;
//                    } else {
//                        newMove = new ChessMove(myPosition, moveToPos, null);
//                        validMoves.add(newMove);
//                    }
//                }
//            }
//
//            return new ArrayList<>(validMoves);

        }
        /*----------     KING     ----------*/
        else if (piece.getPieceType() == PieceType.KING) {

            return steppingMoves(board, piece, myPosition, kingDirs);
//            int[][] potentialDirections = {
//                    {1,0}, {1,1}, {0,1}, {-1,0}, {-1,-1}, {0,-1}, {-1,1}, {1,-1}
//            };
//            for(int[] vectorPair : potentialDirections) {
//                int rowNext = myPosition.getRow() + vectorPair[0];
//                int colNext = myPosition.getColumn() + vectorPair[1];
//                if((rowNext > 8) || (rowNext < 1) || (colNext > 8) || (colNext < 1)) {
//                    continue;
//                }
//                moveToPos = new ChessPosition(rowNext, colNext);
//                ChessPiece target = board.getPiece(moveToPos);
//                if (target != null) {
//                    validMoves = canCapture(target, piece, myPosition, moveToPos, validMoves);
//                } else {
//                    newMove = new ChessMove(myPosition, moveToPos, null);
//                    validMoves.add(newMove);
//                }
//            }
//            return new ArrayList<>(validMoves);

        }
        /*----------     KNIGHT     ----------*/
        else if (piece.getPieceType() == PieceType.KNIGHT) {
//            int [][] potentialDirections = {
//                    {2,1}, {1,2}, {-2,1}, {2,-1}, {-2,-1}, {-1,2}, {-1,-2}, {1,-2}
//            };
//            for(int[] vectorPair : potentialDirections) {
//                int rowNext = myPosition.getRow() + vectorPair[0];
//                int colNext = myPosition.getColumn() + vectorPair[1];
//                if((rowNext > 8) || (rowNext < 1) || (colNext > 8) || (colNext < 1)) {
//                    continue;
//                }
//                moveToPos = new ChessPosition(rowNext, colNext);
//                ChessPiece target = board.getPiece(moveToPos);
//                if (target != null) {
//                    validMoves = canCapture(target, piece, myPosition, moveToPos, validMoves);
//                } else {
//                    newMove = new ChessMove(myPosition, moveToPos, null);
//                    validMoves.add(newMove);
//                }
//            }
//            return new ArrayList<>(validMoves);

        }
        /*----------     PAWN     ----------*/
        else if (piece.getPieceType() == PieceType.PAWN) {
//            ArrayList<ChessMove> validMovesList = new ArrayList<>(validMoves);
//            if(piece.getTeamColor() == ChessGame.TeamColor.WHITE) {
//                if(startRow == 2) {
//                    moveToPos = new ChessPosition(startRow+2, startCol);
//                    ChessPiece target = board.getPiece(moveToPos);
//                    if (target != null) {
//                        //Do nothing
//                    }
//                }
//                if(startRow+1 < 9) {
//                    moveToPos = new ChessPosition(startRow+1, startCol);
//                    ChessPiece target = board.getPiece(moveToPos);
//                    if (target != null) {
//                        //Do nothing
//                    } else {
//                        newMove = new ChessMove(myPosition, moveToPos, null);
//                        validMovesList.add(newMove);
//                        if(startRow+1 == 8) {
//                            validMovesList.add(newMove);
//                            validMovesList.add(newMove);
//                            validMovesList.add(newMove);
//                        }
//                    }
//                    if(startCol+1 < 9) {
//                        moveToPos = new ChessPosition(startRow+1, startCol+1);
//                        target = board.getPiece(moveToPos);
//                        if (target != null) {
//                            validMovesList = canCapture(target, piece, myPosition, moveToPos, validMovesList);
//                            newMove = new ChessMove(myPosition, moveToPos, null);
//                            if(startRow+1 == 8) {
//                                validMovesList.add(newMove);
//                                validMovesList.add(newMove);
//                                validMovesList.add(newMove);
//                            }
//                        }
//                    }
//                    if(startCol-1 > 0) {
//                        moveToPos = new ChessPosition(startRow+1, startCol-1);
//                        target = board.getPiece(moveToPos);
//                        if(target != null) {
//                            validMovesList = canCapture(target, piece, myPosition, moveToPos, validMovesList);
//                        }
//                    }
//
//                }
//            } else {
//                if(startRow == 7) {
//                    moveToPos = new ChessPosition(startRow-2, startCol);
//                    ChessPiece target = board.getPiece(moveToPos);
//                    if (target != null) {
//                        //Do nothing
//                    }
//                }
//                if(startRow-1 > 0) {
//                    moveToPos = new ChessPosition(startRow-1, startCol);
//                    ChessPiece target = board.getPiece(moveToPos);
//                    if (target != null) {
//                        // Do nothing
//                    } else {
//                        newMove = new ChessMove(myPosition, moveToPos, null);
//                        validMovesList.add(newMove);
//                        if(startRow-1 == 1) {
//                            validMovesList.add(newMove);
//                            validMovesList.add(newMove);
//                            validMovesList.add(newMove);
//                        }
//                    }
//                    if(startCol+1 < 9) {
//                        moveToPos = new ChessPosition(startRow-1, startCol+1);
//                        target = board.getPiece(moveToPos);
//                        if (target != null) {
//                            validMovesList = canCapture(target, piece, myPosition, moveToPos, validMovesList);
//                            if(startRow-1 == 1) {
//                                newMove = new ChessMove(myPosition, moveToPos, null);
//                                validMovesList.add(newMove);
//                                validMovesList.add(newMove);
//                                validMovesList.add(newMove);
//                            }
//                        }
//                    }
//                    if(startCol-1 > 0) {
//                        moveToPos = new ChessPosition(startRow-1, startCol-1);
//                        target = board.getPiece(moveToPos);
//                        if(target != null) {
//                            validMovesList = canCapture(target, piece, myPosition, moveToPos, validMovesList);
//                            if(startRow-1 == 1) {
//                                newMove = new ChessMove(myPosition, moveToPos, null);
//                                validMovesList.add(newMove);
//                                validMovesList.add(newMove);
//                                validMovesList.add(newMove);
//                            }
//                        }
//                    }
//                }
//            }
//
//
//
//
//            return validMovesList;
        }
        /*----------     QUEEN     ----------*/
        else if (piece.getPieceType() == PieceType.QUEEN) {

            return slidingMoves(board, piece,myPosition, queenDirs);
//            for(int i = 1; i < 9; i++) {
//                if((startRow+i < 9) && (startCol+i < 9)) {
//                    moveToPos = new ChessPosition(startRow+i, startCol+i);
//                    ChessPiece target = board.getPiece(moveToPos);
//                    if (target != null) {
//                        validMoves = canCapture(target, piece, myPosition, moveToPos, validMoves);
//                        break;
//                    } else {
//                        newMove = new ChessMove(myPosition, moveToPos, null);
//                        validMoves.add(newMove);
//                    }
//                }
//            }
//            for(int i = 1; i < 9; i++) {
//                if((startRow-i > 0) && (startCol+i < 9)) {
//                    moveToPos = new ChessPosition(startRow-i, startCol+i);
//                    ChessPiece target = board.getPiece(moveToPos);
//                    if (target != null) {
//                        validMoves = canCapture(target, piece, myPosition, moveToPos, validMoves);
//                        break;
//                    } else {
//                        newMove = new ChessMove(myPosition, moveToPos, null);
//                        validMoves.add(newMove);
//                    }
//                }
//            }
//            for(int i = 1; i < 9; i++) {
//                if((startRow-i > 0) && (startCol-i > 0)) {
//                    moveToPos = new ChessPosition(startRow - i, startCol - i);
//                    ChessPiece target = board.getPiece(moveToPos);
//                    if (target != null) {
//                        validMoves = canCapture(target, piece, myPosition, moveToPos, validMoves);
//                        break;
//                    } else {
//                        newMove = new ChessMove(myPosition, moveToPos, null);
//                        validMoves.add(newMove);
//                    }
//                }
//            }
//            for(int i = 1; i < 9; i++) {
//                if((startRow+i < 9) && (startCol-i > 0)) {
//                    moveToPos = new ChessPosition(startRow+i, startCol-i);
//                    ChessPiece target = board.getPiece(moveToPos);
//                    if (target != null) {
//                        validMoves = canCapture(target, piece, myPosition, moveToPos, validMoves);
//                        break;
//                    } else {
//                        newMove = new ChessMove(myPosition, moveToPos, null);
//                        validMoves.add(newMove);
//                    }
//                }
//            }
//            for(int i = 1; i < 9; i++) {
//                if(startRow+i < 9) {
//                    moveToPos = new ChessPosition(startRow+i, startCol);
//                    ChessPiece target = board.getPiece(moveToPos);
//                    if (target != null) {
//                        validMoves = canCapture(target, piece, myPosition, moveToPos, validMoves);
//                        break;
//                    } else {
//                        newMove = new ChessMove(myPosition, moveToPos, null);
//                        validMoves.add(newMove);
//                    }
//                }
//                if (startRow-i > 0) {
//                    moveToPos = new ChessPosition(startRow-i, startCol);
//                    ChessPiece target = board.getPiece(moveToPos);
//                    if (target != null) {
//                        validMoves = canCapture(target, piece, myPosition, moveToPos, validMoves);
//                        break;
//                    } else {
//                        newMove = new ChessMove(myPosition, moveToPos, null);
//                        validMoves.add(newMove);
//                    }
//                }
//            }
//            for(int i = 1; i < 9; i++) {
//                if(startCol+i < 9) {
//                    moveToPos = new ChessPosition(startRow, startCol+i);
//                    ChessPiece target = board.getPiece(moveToPos);
//                    if (target != null) {
//                        validMoves = canCapture(target, piece, myPosition, moveToPos, validMoves);
//                        break;
//                    } else {
//                        newMove = new ChessMove(myPosition, moveToPos, null);
//                        validMoves.add(newMove);
//                    }
//                }
//                if (startCol-i > 0) {
//                    moveToPos = new ChessPosition(startRow, startCol-i);
//                    ChessPiece target = board.getPiece(moveToPos);
//                    if (target != null) {
//                        validMoves = canCapture(target, piece, myPosition, moveToPos, validMoves);
//                        break;
//                    } else {
//                        newMove = new ChessMove(myPosition, moveToPos, null);
//                        validMoves.add(newMove);
//                    }
//                }
//            }
//
//
//
//            return new ArrayList<>(validMoves);
        }

        return List.of();
    }
}
