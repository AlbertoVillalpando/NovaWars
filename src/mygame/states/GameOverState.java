package mygame.states;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Node;
import mygame.config.GameConfig;

/**
 * Estado de Game Over - Pantalla que se muestra cuando el núcleo es destruido.
 * 
 * <p>GameOverState proporciona una interfaz simple para mostrar las estadísticas
 * finales del juego y permitir al jugador reiniciar o salir.</p>
 * 
 * <h3>Funcionalidades:</h3>
 * <ul>
 *   <li><strong>Mostrar estadísticas:</strong> Puntuación final, oleada alcanzada, tiempo</li>
 *   <li><strong>Opciones de navegación:</strong> Reiniciar con R, Salir con ESC</li>
 *   <li><strong>UI centrada:</strong> Diseño limpio y fácil de leer</li>
 * </ul>
 * 
 * @author Alberto Villalpando
 * @version 1.0
 * @since 2024
 */
public class GameOverState extends AbstractAppState {
    
    private SimpleApplication app;
    private Node guiNode;
    private InputManager inputManager;
    private AppStateManager stateManager;
    private GameConfig config;
    
    // Estadísticas del juego
    private int finalScore;
    private int finalWave;
    private float finalTime;
    
    // Elementos UI
    private BitmapText gameOverText;
    private BitmapText scoreText;
    private BitmapText waveText;
    private BitmapText timeText;
    private BitmapText instructionsText;
    
    // Constantes para input
    private static final String RESTART = "Restart";
    private static final String EXIT = "Exit";
    
    /**
     * Constructor del estado de Game Over
     * 
     * @param config Configuración del juego
     * @param score Puntuación final
     * @param wave Oleada final alcanzada
     * @param time Tiempo total de juego
     */
    public GameOverState(GameConfig config, int score, int wave, float time) {
        this.config = config;
        this.finalScore = score;
        this.finalWave = wave;
        this.finalTime = time;
    }
    
    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        super.initialize(stateManager, app);
        
        this.app = (SimpleApplication) app;
        this.guiNode = this.app.getGuiNode();
        this.inputManager = this.app.getInputManager();
        this.stateManager = stateManager;
        
        setupUI();
        setupInput();
        
        System.out.println("GameOverState inicializado");
    }
    
    /**
     * Configura la interfaz de usuario del Game Over
     */
    private void setupUI() {
        BitmapFont font = app.getAssetManager().loadFont("Interface/Fonts/Default.fnt");
        float screenWidth = app.getCamera().getWidth();
        float screenHeight = app.getCamera().getHeight();
        
        // Título "GAME OVER"
        gameOverText = new BitmapText(font, false);
        gameOverText.setSize(font.getCharSet().getRenderedSize() * 4);
        gameOverText.setColor(ColorRGBA.Red);
        gameOverText.setText("GAME OVER");
        
        // Centrar el título
        float titleWidth = gameOverText.getLineWidth();
        gameOverText.setLocalTranslation(
            (screenWidth - titleWidth) / 2,
            screenHeight * 0.7f,
            0
        );
        guiNode.attachChild(gameOverText);
        
        // Puntuación final
        scoreText = new BitmapText(font, false);
        scoreText.setSize(font.getCharSet().getRenderedSize() * 2);
        scoreText.setColor(ColorRGBA.White);
        scoreText.setText("Final Score: " + finalScore);
        
        float scoreWidth = scoreText.getLineWidth();
        scoreText.setLocalTranslation(
            (screenWidth - scoreWidth) / 2,
            screenHeight * 0.55f,
            0
        );
        guiNode.attachChild(scoreText);
        
        // Oleada alcanzada
        waveText = new BitmapText(font, false);
        waveText.setSize(font.getCharSet().getRenderedSize() * 1.5f);
        waveText.setColor(ColorRGBA.White);
        waveText.setText("Wave Reached: " + finalWave);
        
        float waveWidth = waveText.getLineWidth();
        waveText.setLocalTranslation(
            (screenWidth - waveWidth) / 2,
            screenHeight * 0.45f,
            0
        );
        guiNode.attachChild(waveText);
        
        // Tiempo de supervivencia
        timeText = new BitmapText(font, false);
        timeText.setSize(font.getCharSet().getRenderedSize() * 1.5f);
        timeText.setColor(ColorRGBA.White);
        
        int minutes = (int) (finalTime / 60);
        int seconds = (int) (finalTime % 60);
        timeText.setText("Survival Time: " + minutes + ":" + String.format("%02d", seconds));
        
        float timeWidth = timeText.getLineWidth();
        timeText.setLocalTranslation(
            (screenWidth - timeWidth) / 2,
            screenHeight * 0.35f,
            0
        );
        guiNode.attachChild(timeText);
        
        // Instrucciones
        instructionsText = new BitmapText(font, false);
        instructionsText.setSize(font.getCharSet().getRenderedSize() * 1.2f);
        instructionsText.setColor(ColorRGBA.Yellow);
        instructionsText.setText("Press R to Restart | Press ESC to Exit");
        
        float instructionsWidth = instructionsText.getLineWidth();
        instructionsText.setLocalTranslation(
            (screenWidth - instructionsWidth) / 2,
            screenHeight * 0.2f,
            0
        );
        guiNode.attachChild(instructionsText);
        
        System.out.println("UI de Game Over configurada");
    }
    
    /**
     * Configura los controles de entrada
     */
    private void setupInput() {
        // Mapear teclas
        inputManager.addMapping(RESTART, new KeyTrigger(KeyInput.KEY_R));
        inputManager.addMapping(EXIT, new KeyTrigger(KeyInput.KEY_ESCAPE));
        
        // Agregar listener
        inputManager.addListener(inputListener, RESTART, EXIT);
        
        System.out.println("Input de Game Over configurado - R para reiniciar, ESC para salir");
    }
    
    /**
     * Listener para manejar la entrada del usuario
     */
    private ActionListener inputListener = new ActionListener() {
        @Override
        public void onAction(String name, boolean isPressed, float tpf) {
            if (!isPressed) return; // Solo responder a key press, no release
            
            switch (name) {
                case RESTART:
                    System.out.println("Reiniciando juego...");
                    restartGame();
                    break;
                case EXIT:
                    System.out.println("Saliendo del juego...");
                    app.stop();
                    break;
            }
        }
    };
    
    /**
     * Reinicia el juego creando un nuevo GameState
     */
    private void restartGame() {
        // Remover este estado
        stateManager.detach(this);
        
        // Crear y agregar nuevo GameState
        GameState newGameState = new GameState(config);
        stateManager.attach(newGameState);
        
        System.out.println("Juego reiniciado");
    }
    
    @Override
    public void cleanup() {
        super.cleanup();
        
        // Limpiar input mappings
        if (inputManager != null) {
            inputManager.deleteMapping(RESTART);
            inputManager.deleteMapping(EXIT);
            inputManager.removeListener(inputListener);
        }
        
        // Limpiar elementos UI
        if (gameOverText != null) gameOverText.removeFromParent();
        if (scoreText != null) scoreText.removeFromParent();
        if (waveText != null) waveText.removeFromParent();
        if (timeText != null) timeText.removeFromParent();
        if (instructionsText != null) instructionsText.removeFromParent();
        
        System.out.println("GameOverState limpiado");
    }
}