package de.fhdortmund.viargewinnt;

@SuppressWarnings("FieldCanBeLocal")
class TurnEvent {
    private Game game;
    private String previousPlayer;
    private boolean win;
    MessageType type;
    TurnEvent(Game game, String previousPlayer, boolean win, MessageType type) {
        this.game = game;
        this.previousPlayer = previousPlayer;
        this.win = win;
        this.type=type;
    }
}
