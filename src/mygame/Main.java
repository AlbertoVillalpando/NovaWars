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
 * Clase principal del juego NovaWars - Twin-stick Shooter de defensa de núcleo.
 * 
 * <p>NovaWars es un juego de disparos donde el jugador controla una nave que debe defender
 * un núcleo central de oleadas continuas de enemigos. El juego utiliza el motor jMonkeyEngine
 * para renderizado 3D y física.</p>
 * 
 * <h3>Características principales:</h3>
 * <ul>
 *   <li>Control twin-stick: movimiento con WASD, disparo con mouse</li>
 *   <li>Sistema de oleadas progresivas de enemigos</li>
 *   <li>Núcleo central con vida que debe ser protegido</li>
 *   <li>Diferentes tipos de enemigos con patrones de movimiento únicos</li>
 *   <li>Sistema de configuración JSON para ajustar parámetros del juego</li>
 * </ul>
 * 
 * <h3>Arquitectura del juego:</h3>
 * <p>La aplicación extiende SimpleApplication de jME3 y utiliza un patrón de estados
 * donde GameState maneja toda la lógica del juego. El sistema se basa en:</p>
 * <ul>
 *   <li>Entidades (Player, Core, Enemy, Bullet) con componentes Control</li>
 *   <li>Managers para pooling de objetos y spawning de enemigos</li>
 *   <li>Sistema de configuración centralizado</li>
 * </ul>
 * 
 * <h3>Configuración técnica:</h3>
 * <ul>
 *   <li>Resolución: 1920x1080 (configurable)</li>
 *   <li>Frame rate objetivo: 60 FPS con VSync</li>
 *   <li>Renderizado: OpenGL a través de jME3-LWJGL</li>
 * </ul>
 * 
 * @author Alberto Villalpando
 * @version 1.0
 * @since 2024
 */
public class Main extends SimpleApplication {
    
    /**
     * Configuración del juego cargada desde archivo JSON.
     * Contiene todos los parámetros ajustables como velocidades, tamaños, vida, etc.
     */
    private GameConfig gameConfig;
    
    /**
     * Estado principal del juego que maneja todas las entidades y la lógica de gameplay.
     * Se encarga de la actualización de jugador, enemigos, colisiones y sistemas del juego.
     */
    private GameState gameState;
    
    /**
     * Punto de entrada principal de la aplicación NovaWars.
     * 
     * <p>Configura la ventana del juego, las opciones de renderizado y inicia
     * la aplicación jMonkeyEngine. La configuración incluye:</p>
     * <ul>
     *   <li>Resolución de pantalla y modo ventana</li>
     *   <li>Sincronización vertical y limitación de FPS</li>
     *   <li>Título de la ventana</li>
     *   <li>Desactivación del diálogo de configuración inicial</li>
     * </ul>
     * 
     * @param args Argumentos de línea de comandos (no utilizados)
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
     * Carga la configuración del juego desde el archivo JSON.
     * 
     * <p>Utiliza ConfigLoader para leer el archivo de configuración y mapear
     * los valores a un objeto GameConfig. Si ocurre algún error durante la carga,
     * se utilizan valores por defecto para asegurar que el juego pueda ejecutarse.</p>
     * 
     * <p>La configuración incluye parámetros para:</p>
     * <ul>
     *   <li>Entidades del juego (velocidad, tamaño, vida)</li>
     *   <li>Configuración del núcleo central</li>
     *   <li>Parámetros de balas y enemigos</li>
     *   <li>Resolución de pantalla</li>
     * </ul>
     * 
     * @see ConfigLoader#load()
     * @see GameConfig
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
     * Configura el renderizado básico y la apariencia visual del juego.
     * 
     * <p>Establece la configuración visual fundamental:</p>
     * <ul>
     *   <li>Color de fondo azul oscuro para crear ambiente espacial</li>
     *   <li>Desactivación de estadísticas de debug por defecto</li>
     *   <li>Activación del contador de FPS para monitoreo de rendimiento</li>
     *   <li>Eliminación del estado de estadísticas de jME3 si existe</li>
     * </ul>
     * 
     * <p>El color de fondo (0.05, 0.05, 0.1) crea un ambiente espacial
     * que complementa el estilo visual neón del juego.</p>
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
     * Inicializa los estados del juego y configura el flujo de la aplicación.
     * 
     * <p>Crea e inicializa el estado principal GameState que maneja toda la lógica
     * del juego. El GameState se encarga de:</p>
     * <ul>
     *   <li>Gestión de entidades (jugador, núcleo, enemigos, balas)</li>
     *   <li>Procesamiento de input del usuario</li>
     *   <li>Detección de colisiones</li>
     *   <li>Lógica de oleadas de enemigos</li>
     *   <li>Gestión del estado del juego</li>
     * </ul>
     * 
     * <p>Estados futuros a implementar:</p>
     * <ul>
     *   <li>HUDState: Interfaz de usuario y información de juego</li>
     *   <li>MenuState: Menú principal y opciones</li>
     * </ul>
     * 
     * @see GameState
     */
    private void initializeGameStates() {
        // Crear y añadir el estado principal del juego
        gameState = new GameState(gameConfig);
        stateManager.attach(gameState);
        
        // TODO: Añadir HUDState cuando se implemente (Fase 6)
        // TODO: Añadir MenuState cuando se implemente (Fase 6)
    }
    
    /**
     * Configura el sistema de input y controles del juego.
     * 
     * <p>Prepara el sistema de entrada para el control twin-stick:</p>
     * <ul>
     *   <li>Desactiva FlyByCamera de jME3 para evitar conflictos</li>
     *   <li>Elimina el mapping por defecto de ESC para salir</li>
     *   <li>Delega el manejo de input específico a GameState</li>
     * </ul>
     * 
     * <p>El control del juego se maneja completamente en GameState:</p>
     * <ul>
     *   <li>WASD para movimiento del jugador</li>
     *   <li>Mouse para apuntado y disparo</li>
     *   <li>Controles futuros: pausa, reinicio, menú</li>
     * </ul>
     * 
     * <p>La desactivación de FlyByCamera es crucial para que el sistema de
     * cámaras personalizado funcione correctamente.</p>
     * 
     * @see GameState configuración de input específico
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