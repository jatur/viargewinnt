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
    private String[][] grid = new String[width][height];

    Game(long id) {
        this.id = id;
    }

    void start() {
        state = GameState.GAME;
        currentPlayer = players.get(0);
    }

    void move(int x) {
        int y = findY(x);

        grid[x][y] = currentPlayer;

        TurnEvent event = new TurnEvent(this, currentPlayer, isWon(),MessageType.GAMEUPDATE);

        int playerIndex = players.indexOf(currentPlayer);
        playerIndex = (playerIndex + 1) % players.size();
        currentPlayer = players.get(playerIndex);

        UpdateStateSocket.sendEvent(id, event);
    }

    private int findY(int x) {
        for (int y = 5; y >= 0; y--) {
            if (grid[x][y] == null) {
                return y;
            }
        }

        halt(400, "column is full");
        return -1;
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
        String player = grid[cx.apply(x, 0)][cy.apply(y, 0)];

        if (player == null) {
            return false;
        }

        for (int i = 1; i < 4; i++) {
            if (!player.equals(grid[cx.apply(x, i)][cy.apply(y, i)])) {
                return false;
            }
        }

        return true;
    }
}
