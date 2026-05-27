package gamegl.state;

/**
 * Enumerates the possible high-level states of the game loop.
 */
public enum GameStateType {
    /**
     * The game is displaying the main menu screen.
     */
    MAIN_MENU,
    /**
     * The game is actively being played.
     */
    PLAYING,
    /**
     * The game is paused.
     */
    PAUSED,
}
