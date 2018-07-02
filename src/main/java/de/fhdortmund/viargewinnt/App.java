package de.fhdortmund.viargewinnt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import static spark.Spark.*;

public class App {
    private static Map<Long, Game> games = new HashMap<>();
    private static AtomicLong nextGameId = new AtomicLong(0);


    public static void main(String[] args) {

        final Logger log = LoggerFactory.getLogger(App.class);
        webSocket("/state", UpdateStateSocket.class);

        post("/create", (req, res) -> {
            long id = nextGameId.incrementAndGet();
            games.put(id, new Game(id));
            log.info("game created with id {}", id);
            return id;
        });

        post("/join/:id/:name", (req, res) -> {
            long id = Long.parseLong(req.params("id"));
            String name = req.params("name");
            Game game = getGame(id);

            if (game.state != GameState.LOBBY) {
                halt(400, "game is already active");
            }
            if (game.players.contains(name)) {
                halt(400, "name is taken");
            }
            game.players.add(name);
            UpdateStateSocket.sendEvent(id,new TurnEvent(game,"noone",false,MessageType.PLAYERUPDTAE));

            log.info("player {} joined the game", name);
            return "ok";
        });

        post("/start/:id", (req, res) -> {
            long id = Integer.parseInt(req.params("id"));
            Game game = getGame(id);

            if (game.state != GameState.LOBBY) {
                halt(400, "game is already active");
            }
            if (game.players.isEmpty()) {
                halt(400, "no players");
            }

            game.start();
            UpdateStateSocket.sendEvent(id,new TurnEvent(game,"noone",false,MessageType.GAMESTART));
            log.info("game with id {} started ", id);
            return "ok";
        });

        post("/turn/:id/:player/:position", (req, res) -> {
            long id = Long.parseLong(req.params("id"));
            String player = req.params("player");
            int position = Integer.parseInt(req.params("position"));
            Game game = getGame(id);

            if (game.state != GameState.GAME) {
                halt(400, "game not started");
            }

            if (!game.currentPlayer.equals(player)) {
                halt(403, "not your turn");
            }

            game.move(position);
            log.info("player {} set position {} in Game {}", player, position, id);
            return "ok";
        });
    }

    private static Game getGame(long id) {
        if (!games.containsKey(id)) {
            halt(404, "games does not exist");
        }
        return games.get(id);
    }
}
