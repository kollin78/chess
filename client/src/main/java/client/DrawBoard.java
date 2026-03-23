package client;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessPosition;

import static ui.EscapeSequences.*;

public class DrawBoard {
    private static final int BOARD_SQUARES = 8;
    private static final String TRIPLE_SPACE = "   ";
    private static final String DOUBLE_SPACE = "  ";
    private static final String SINGLE_SPACE = " ";
    private static final String EM_SPACE = "\u2003";
    private static final String PUNC_SPACE = "\u2008";

    public static String draw(boolean isPlayerWhite) {
        var stringBuilder = new StringBuilder();
        ChessBoard board = new ChessBoard();
        board.resetBoard();
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
        stringBuilder.append(SET_BG_COLOR_DARK_GREY).append(SET_TEXT_COLOR_BLUE).append(TRIPLE_SPACE);
        String[] columnHeaders =
                {"a", "b", "c",
                "d", "e", "f",
                "g", "h"};

        if(isPlayerWhite) {
            for(int i = 0; i < 8; i++) {
                stringBuilder.append(EM_SPACE).append(columnHeaders[i]).append(SINGLE_SPACE);
            }
        } else {
            for(int i = 7; i >= 0; i--) {
                stringBuilder.append(EM_SPACE).append(columnHeaders[i]).append(SINGLE_SPACE);
            }
        }

        stringBuilder.append(TRIPLE_SPACE).append(PUNC_SPACE).append(PUNC_SPACE).append(RESET_TEXT_COLOR).append(RESET_BG_COLOR).append("\n");
    }

    private static void drawRows(StringBuilder stringBuilder, int row, ChessBoard board, boolean isPlayerWhite) {
        stringBuilder.append(SET_BG_COLOR_DARK_GREY).append(SET_TEXT_COLOR_MAGENTA).append(SINGLE_SPACE).append(row).append(EM_SPACE);

        for(int column = 1; column <= 8; column++) {
            int printedCol;

            if(isPlayerWhite) {
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

        stringBuilder.append(SET_BG_COLOR_DARK_GREY).append(SET_TEXT_COLOR_MAGENTA).append(SINGLE_SPACE).append(row).append(SINGLE_SPACE);
        stringBuilder.append(RESET_TEXT_COLOR).append(RESET_BG_COLOR).append("\n");
    }

    private static String getPieceString(ChessPiece piece) {
        if(piece == null) {
            return SINGLE_SPACE + EM_SPACE + SINGLE_SPACE;
        }

        String color;
        String type;
        if(piece.getTeamColor() == ChessGame.TeamColor.WHITE) {
            color = SET_TEXT_COLOR_WHITE;
            type = switch(piece.getPieceType()) {
                case KING -> WHITE_KING;
                case QUEEN -> WHITE_QUEEN;
                case BISHOP -> WHITE_BISHOP;
                case KNIGHT -> WHITE_KNIGHT;
                case ROOK -> WHITE_ROOK;
                case PAWN -> WHITE_PAWN;
            };
        } else {
            color = SET_TEXT_COLOR_BLUE;
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


