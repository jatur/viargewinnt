package de.fhdortmund.viargewinnt;

@SuppressWarnings("FieldCanBeLocal")
class TurnEvent {
    private Game game;
    private String previousPlayer;
    private boolean win;

    TurnEvent(Game game, String previousPlayer, boolean win) {
        this.game = game;
        this.previousPlayer = previousPlayer;
        this.win = win;
    }
}
