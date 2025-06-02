package mygame;

import com.jme3.app.SimpleApplication;
import com.jme3.app.StatsAppState;
import com.jme3.math.ColorRGBA;
import com.jme3.renderer.RenderManager;
import com.jme3.system.AppSettings;
import mygame.config.ConfigLoader;
import mygame.config.GameConfig;
import mygame.states.GameState;

/**
 * Clase principal del juego NovaWars.
 * Un twin-stick shooter donde el jugador defiende un núcleo central de oleadas de enemigos.
 * 
 * @author tu_nombre
 */
public class Main extends SimpleApplication {
    
    private GameConfig gameConfig;
    private GameState gameState;
    
    /**
     * Punto de entrada principal
     */
    public static void main(String[] args) {
        Main app = new Main();
        
        // Configurar ventana
        AppSettings settings = new AppSettings(true);
        settings.setTitle("NovaWars - Defend the Core!");
        settings.setResolution(1920, 1080);
        settings.setFullscreen(false);
        settings.setVSync(true);
        settings.setFrameRate(60);
        
        app.setSettings(settings);
        app.setShowSettings(false); // No mostrar diálogo de configuración
        app.setPauseOnLostFocus(false); // No pausar si pierde el foco
        
        app.start();
    }
    
    @Override
    public void simpleInitApp() {
        // Cargar configuración del juego
        loadGameConfig();
        
        // Configurar render básico
        setupRender();
        
        // Inicializar estados del juego
        initializeGameStates();
        
        // Configurar input
        setupInput();
        
        System.out.println("NovaWars iniciado - Fase 3: Núcleo Central implementado");
    }
    
    /**
     * Carga la configuración del juego desde el archivo properties
     */
    private void loadGameConfig() {
        try {
            gameConfig = ConfigLoader.load();
            System.out.println("Configuración cargada exitosamente");
        } catch (Exception e) {
            System.err.println("Error al cargar configuración, usando valores por defecto: " + e.getMessage());
            gameConfig = new GameConfig(); // Usar valores por defecto
        }
    }
    
    /**
     * Configura el renderizado básico
     */
    private void setupRender() {
        // Color de fondo negro para estilo neón
        viewPort.setBackgroundColor(new ColorRGBA(0.05f, 0.05f, 0.1f, 1f));
        
        // Desactivar estadísticas por defecto
        setDisplayStatView(false);
        setDisplayFps(true);
        
        // Remover el estado de estadísticas si existe
        StatsAppState statsState = stateManager.getState(StatsAppState.class);
        if (statsState != null) {
            stateManager.detach(statsState);
        }
    }
    
    /**
     * Inicializa los estados del juego
     */
    private void initializeGameStates() {
        // Crear y añadir el estado principal del juego
        gameState = new GameState(gameConfig);
        stateManager.attach(gameState);
        
        // TODO: Añadir HUDState cuando se implemente (Fase 6)
        // TODO: Añadir MenuState cuando se implemente (Fase 6)
    }
    
    /**
     * Configura el sistema de input
     */
    private void setupInput() {
        // Deshabilitar FlyByCamera para que no interfiera con nuestros controles
        flyCam.setEnabled(false);
        
        // Limpiar mappings por defecto
        inputManager.deleteMapping(SimpleApplication.INPUT_MAPPING_EXIT);
        
        // El input específico del jugador se maneja en GameState
        
        // TODO: Añadir tecla de pausa (Fase 6)
        // TODO: Añadir tecla de reinicio para debug
        
        System.out.println("FlyByCamera deshabilitada - controles del juego activos");
    }
    
    @Override
    public void simpleUpdate(float tpf) {
        // La lógica principal se maneja en GameState
    }
    
    @Override
    public void simpleRender(RenderManager rm) {
        // Render adicional si es necesario
    }
    
    @Override
    public void destroy() {
        super.destroy();
        System.out.println("NovaWars cerrado");
    }
}