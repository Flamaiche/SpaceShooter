package gamegl.state;

import gamegl.SpaceShooter;
import gamegl.gestion.donnees.GameData;
import gamegl.gestion.donnees.SaveClassPatron;
import gamegl.utils.GetDonnee;
import learngl.commandes.Commande;

import java.util.List;

/**
 * Manages the current game state and transitions between states
 * (main menu, playing, paused). Handles score persistence and
 * best-score tracking when switching states.
 */
public class GameStateManager {
    private GameState currentState;
    private final Commande commande;
    private int width;
    private int height;
    private GameState playing;
    private final GameData gameData;

    /**
     * Enumeration of possible game states.
     */
    public enum GameStateEnum {
        MAIN,
        PLAY,
        NEWPLAY,
        PAUSE
    }

    /**
     * Constructs a GameStateManager and loads saved scores from persistent storage.
     *
     * @param commande the command handler for input
     * @param gameData the shared game data object
     * @param width    the initial window width
     * @param height   the initial window height
     */
    public GameStateManager(Commande commande, GameData gameData, int width, int height) {
        this.commande = commande;
        this.width = width;
        this.height = height;
        this.gameData = gameData;
        playing = null;
        List<Object> saveObject = GetDonnee.readJson(SpaceShooter.filenameSaveScore);
        if (saveObject != null) {
            for (Object obj : saveObject) {
                if (obj instanceof SaveClassPatron) {
                    if (((SaveClassPatron) obj).getScore() > gameData.getBestScore()) {
                        gameData.setBestScore(((SaveClassPatron) obj).getScore());
                    }
                }
            }
        }
    }

    /**
     * Switches to the specified game state.
     *
     * @param gState the target game state enum value
     */
    public void setState(GameStateEnum gState) {
        if (currentState != null && currentState != playing) currentState.cleanup();
        choiceGameState(gState);

    }

    private void choiceGameState(GameStateEnum gState) {
        switch (gState) {
            case PLAY: currentState = playing;
                break;
            case MAIN:
                if (playing != null) {
                    sauvegarde();
                    gameData.setTotalScore(gameData.getTotalScore() + gameData.getScore());
                    if (gameData.getScore() > gameData.getBestScore()) {
                        gameData.setBestScore(gameData.getScore());
                    }
                    playing.cleanup();
                    playing = null;
                }
                currentState = new MainMenuState(commande, gameData, width, height);
                break;
            case NEWPLAY:
                if (playing != null) playing.cleanup();
                playing = new PlayingState(commande, gameData, width, height);
                currentState = playing;
                break;
            case PAUSE: currentState = new PausedState(commande, gameData, width, height);
                break;
        }
        currentState.init(commande, width, height);
    }

    /**
     * Updates the current state with the given delta time and window dimensions.
     *
     * @param deltaTime time elapsed since the last update
     * @param width     the current window width
     * @param height    the current window height
     */
    public void update(float deltaTime, int width, int height) {
        this.width = width;
        this.height = height;
        currentState.setWidthHeight(width, height);
        if (currentState != null) currentState.update(deltaTime);
    }

    /** Renders the current state. */
    public void render() {
        if (currentState != null) currentState.render();
    }

    /** Saves the current game data to persistent storage. */
    public void sauvegarde() {
        SaveClassPatron SCP = new SaveClassPatron(gameData);
        SCP.saveDonnees();
    }
}
