package client;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessPosition;

import static ui.EscapeSequences.*;

public class DrawBoard {
    private static final int BOARD_SQUARES = 8;

    public static String draw(boolean isPlayerWhite) {
        var stringBuilder = new StringBuilder();
        ChessBoard board = new ChessBoard();
        drawColumns(stringBuilder, isPlayerWhite);

        if(isPlayerWhite) {
            for(int row = 8; row >= 1; row--) {
                drawRows(stringBuilder, row, board, true);
            }
        } else {
            for(int row = 1; row <= 8; row++) {
                drawRows(stringBuilder, row, board, false);
            }
        }

        drawColumns(stringBuilder, isPlayerWhite);

        return stringBuilder.toString();
    }

    private static void drawColumns(StringBuilder stringBuilder, boolean isPlayerWhite) {
        stringBuilder.append(SET_BG_COLOR_LIGHT_GREY).append(SET_TEXT_COLOR_BLUE).append("    ");
        String[] columnHeaders =
                {" a ", " b ", " c ",
                " d ", " e ", " f ",
                " g ", " h "};

        if(isPlayerWhite) {
            for(int i = 0; i < 8; i++) {
                stringBuilder.append(columnHeaders[i]);
            }
        } else {
            for(int i = 7; i >= 0; i--) {
                stringBuilder.append(columnHeaders[i]);
            }
        }

        stringBuilder.append("    ").append(RESET_TEXT_COLOR).append(RESET_BG_COLOR).append("\n");
    }

    private static void drawRows(StringBuilder stringBuilder, int row, ChessBoard board, boolean isPlayerWHite) {
        stringBuilder.append(SET_BG_COLOR_LIGHT_GREY).append(SET_TEXT_COLOR_BLUE).append("    ");

        for(int column = 1; column <= 8; column++) {
            int printedCol;

            if(isPlayerWHite) {
                printedCol = column;
            } else {
                printedCol = (9 - column);
            }

            boolean isDarkSquare = ((row + printedCol) % 2 == 0);

            if(isDarkSquare) {
                stringBuilder.append(SET_BG_COLOR_BLACK);
            } else {
                stringBuilder.append(SET_BG_COLOR_LIGHT_GREY);
            }

            ChessPiece piece = board.getPiece(new ChessPosition(row, printedCol));
            stringBuilder.append(getPieceString(piece));
        }

        stringBuilder.append(SET_BG_COLOR_LIGHT_GREY).append(SET_BG_COLOR_BLUE).append("    ");
        stringBuilder.append(RESET_TEXT_COLOR).append(RESET_BG_COLOR).append("\n");
    }

    private static String getPieceString(ChessPiece piece) {
        if(piece == null) {
            return "    ";
        }

        String color;
        String type;
        if(piece.getTeamColor() == ChessGame.TeamColor.WHITE) {
            color = SET_TEXT_COLOR_RED;
            type = switch(piece.getPieceType()) {
                case KING -> WHITE_KING;
                case QUEEN -> WHITE_QUEEN;
                case BISHOP -> WHITE_BISHOP;
                case KNIGHT -> WHITE_KNIGHT;
                case ROOK -> WHITE_ROOK;
                case PAWN -> WHITE_PAWN;
            };
        } else {
            color = SET_TEXT_COLOR_GREEN;
            type = switch(piece.getPieceType()) {
                case KING -> BLACK_KING;
                case QUEEN -> BLACK_QUEEN;
                case BISHOP -> BLACK_BISHOP;
                case KNIGHT -> BLACK_KNIGHT;
                case ROOK -> BLACK_ROOK;
                case PAWN -> BLACK_PAWN;
            };
        }

        return (color + type);
    }
}


