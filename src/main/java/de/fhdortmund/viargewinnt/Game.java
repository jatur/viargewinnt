package de.fhdortmund.viargewinnt;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;


import static spark.Spark.halt;

class Game {
    GameState state = GameState.LOBBY;
    private long id;
    List<String> players = new ArrayList<>();
    String currentPlayer;
    private final int width = 7;
    private final int height = 6;
    private Column[] grid = new Column[width];

    class Column {
        private String[] pieces = new String[height];
    }

    Game(long id) {
        this.id = id;
        for (int x = 0; x < width; x++) {
            grid[x] = new Column();
        }
    }

    void start() {
        state = GameState.GAME;
        currentPlayer = players.get(0);
    }

    void move(int x) {
        if (x < 0 || x >= width) {
            halt(400, "invalid x position");
        }
        int y = findY(x);
        grid[x].pieces[y] = currentPlayer;

        TurnEvent event;
        if (isWon()) {
            event = new TurnEvent(this, currentPlayer, true, MessageType.GAMEUPDATE);
        } else {
            if (isDraw()) {
                event = new TurnEvent(this, "niemand", true, MessageType.GAMEUPDATE);
            } else {
                event = new TurnEvent(this, currentPlayer, false, MessageType.GAMEUPDATE);
            }
        }

        int playerIndex = players.indexOf(currentPlayer);
        playerIndex = (playerIndex + 1) % players.size();
        currentPlayer = players.get(playerIndex);

        UpdateStateSocket.sendEvent(id, event);
    }

    private int findY(int x) {
        for (int y = 5; y >= 0; y--) {
            if (grid[x].pieces[y] == null) {
                return y;
            }
        }

        halt(400, "column is full");
        return -1;
    }

    private boolean isDraw() {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                // still slots there to fill
                if (grid[x].pieces[y] == null) {
                    return false;
                }
            }
        }

        return true;
    }

    private boolean isWon() {
        // diagonals
        for (int x = 0; x < width - 3; x++) {
            for (int y = 0; y < height - 3; y++) {
                if (checkFour(x, y, (cx, i) -> cx + i, (cy, i) -> cy + i)) {
                    return true;
                }
                if (checkFour(x, y, (cx, i) -> cx + i, (cy, i) -> cy + 3 - i)) {
                    return true;
                }
            }
        }

        // horizontal
        for (int x = 0; x < width - 3; x++) {
            for (int y = 0; y < height; y++) {
                if (checkFour(x, y, (cx, i) -> cx + i, (cy, i) -> cy)) {
                    return true;
                }
            }
        }

        // vertical
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height - 3; y++) {
                if (checkFour(x, y, (cx, i) -> cx, (cy, i) -> cy + i)) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean checkFour(int x, int y, BiFunction<Integer, Integer, Integer> cx, BiFunction<Integer, Integer, Integer> cy) {
        String player = grid[cx.apply(x, 0)].pieces[cy.apply(y, 0)];

        if (player == null) {
            return false;
        }

        for (int i = 1; i < 4; i++) {
            if (!player.equals(grid[cx.apply(x, i)].pieces[cy.apply(y, i)])) {
                return false;
            }
        }

        return true;
    }
}
