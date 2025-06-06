package mygame.states;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.RawInputListener;
import com.jme3.input.controls.*;
import com.jme3.math.Ray;
import com.jme3.math.Vector2f;
import com.jme3.renderer.Camera;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.scene.control.BillboardControl;
import mygame.config.GameConfig;
import mygame.states.GameOverState;
import mygame.controls.CoreControl;
import mygame.controls.PlayerControl;
import mygame.entities.Core;
import mygame.entities.Player;
import mygame.entities.Bullet;
import mygame.entities.Enemy;
import mygame.managers.BulletPool;
import mygame.managers.EnemyManager;
import java.util.List;

/**
 * Estado principal del juego NovaWars - Coordinador central de toda la lógica de gameplay.
 * 
 * <p>GameState es el núcleo del juego que coordina todas las entidades, sistemas y mecánicas.
 * Implementa el patrón Observer para recibir eventos del núcleo y enemigos, y gestiona
 * el flujo completo del juego desde la inicialización hasta la limpieza.</p>
 * 
 * <h3>Responsabilidades principales:</h3>
 * <ul>
 *   <li><strong>Gestión de entidades:</strong> Player, Core, Enemy, Bullet</li>
 *   <li><strong>Sistema de input:</strong> Twin-stick controls (WASD + mouse)</li>
 *   <li><strong>Detección de colisiones:</strong> Balas vs enemigos, enemigos vs núcleo</li>
 *   <li><strong>Coordinación de sistemas:</strong> BulletPool, EnemyManager</li>
 *   <li><strong>Estado del juego:</strong> Game over, tiempo, oleadas</li>
 *   <li><strong>Configuración de cámara:</strong> Vista top-down optimizada</li>
 * </ul>
 * 
 * <h3>Arquitectura de entrada:</h3>
 * <ul>
 *   <li><strong>WASD:</strong> Movimiento direccional del jugador</li>
 *   <li><strong>Mouse movement:</strong> Apuntado hacia cursor</li>
 *   <li><strong>Left click:</strong> Disparo de proyectiles</li>
 * </ul>
 * 
 * <h3>Sistemas integrados:</h3>
 * <ul>
 *   <li><strong>BulletPool:</strong> Object pooling para optimización de balas</li>
 *   <li><strong>EnemyManager:</strong> Spawning y gestión de oleadas</li>
 *   <li><strong>CoreControl:</strong> Manejo de vida y estado del núcleo</li>
 * </ul>
 * 
 * <h3>Eventos manejados:</h3>
 * <ul>
 *   <li><strong>CoreListener:</strong> Daño, destrucción y curación del núcleo</li>
 *   <li><strong>EnemyManagerListener:</strong> Oleadas, muertes y llegada al núcleo</li>
 * </ul>
 * 
 * @author Alberto Villalpando
 * @version 1.0
 * @see CoreControl.CoreListener
 * @see EnemyManager.EnemyManagerListener
 * @since 2024
 */
public class GameState extends AbstractAppState implements CoreControl.CoreListener, EnemyManager.EnemyManagerListener {
    
    // === Referencias de jMonkeyEngine ===
    /** Aplicación principal de jME3 que proporciona acceso a recursos y sistemas */
    private SimpleApplication app;
    
    /** Gestor de estados de la aplicación */
    private AppStateManager stateManager;
    
    /** Nodo raíz de la escena 3D donde se adjuntan todos los objetos */
    private Node rootNode;
    
    /** Nodo específico del juego que contiene todas las entidades de gameplay */
    private Node gameNode;
    
    /** Gestor de entrada para manejar teclado, mouse y otros dispositivos */
    private InputManager inputManager;
    
    /** Cámara del juego configurada en vista top-down */
    private Camera cam;
    
    // === Configuración del juego ===
    /** 
     * Configuración del juego cargada desde JSON.
     * Contiene parámetros de entidades, velocidades, tamaños y balanceo.
     */
    private GameConfig config;
    
    // === Entidades principales ===
    /** 
     * Entidad del jugador que puede moverse y disparar.
     * Controlada por input WASD + mouse.
     */
    private Player player;
    
    /** 
     * Núcleo central que debe ser defendido de los enemigos.
     * El objetivo principal del juego es mantenerlo con vida.
     */
    private Core core;
    
    /** 
     * Control que maneja la lógica del núcleo (vida, daño, destrucción).
     * Implementa CoreListener para notificar eventos.
     */
    private CoreControl coreControl;
    
    // === Sistemas de gestión ===
    /** 
     * Pool de objetos para balas que optimiza la creación/destrucción.
     * Reutiliza instancias para mejorar rendimiento.
     */
    private BulletPool bulletPool;
    
    /** 
     * Gestor de enemigos que maneja spawning, oleadas y AI.
     * Implementa EnemyManagerListener para eventos de enemigos.
     */
    private EnemyManager enemyManager;
    
    /** 
     * Nodo específico para organizar todas las balas en la escena.
     * Facilita la gestión y optimización del renderizado.
     */
    private Node bulletsNode;
    
    /** 
     * Nodo para la interfaz de usuario.
     * Contiene todos los elementos del HUD.
     */
    private Node guiNode;
    
    /** 
     * Texto de la puntuación mostrado en pantalla.
     */
    private BitmapText scoreText;
    
    // === Estado del juego ===
    /** 
     * Indica si el juego ha terminado (núcleo destruido).
     * Cuando es true, se detienen las actualizaciones principales.
     */
    private boolean gameOver = false;
    
    /** 
     * Tiempo total transcurrido desde el inicio del juego en segundos.
     * Usado para estadísticas, oleadas y eventos temporales.
     */
    private float gameTime = 0f;
    
    /** 
     * Puntuación del jugador.
     * Se incrementa en 10 puntos por cada enemigo eliminado.
     */
    private int score = 0;
    
    // === Variables de control de entrada ===
    /** Estados de las teclas de movimiento WASD */
    private boolean moveUp, moveDown, moveLeft, moveRight;
    
    /** 
     * Vector de dirección de movimiento calculado desde las teclas WASD.
     * Se normaliza automáticamente para movimiento diagonal consistente.
     */
    private Vector3f moveDirection = new Vector3f();
    
    // Constantes para los nombres de los mappings
    private static final String MOVE_UP = "MoveUp";
    private static final String MOVE_DOWN = "MoveDown";
    private static final String MOVE_LEFT = "MoveLeft";
    private static final String MOVE_RIGHT = "MoveRight";
    private static final String SHOOT = "Shoot";
    
    /**
     * Constructor del estado del juego.
     * 
     * <p>Inicializa el GameState con la configuración proporcionada.
     * La inicialización real de sistemas y entidades ocurre en initialize().</p>
     * 
     * @param config Configuración del juego cargada desde JSON con todos los parámetros
     * @see #initialize(AppStateManager, Application)
     */
    public GameState(GameConfig config) {
        this.config = config;
    }
    
    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        super.initialize(stateManager, app);
        
        this.stateManager = stateManager;
        this.app = (SimpleApplication) app;
        this.rootNode = this.app.getRootNode();
        this.inputManager = this.app.getInputManager();
        this.cam = this.app.getCamera();
        this.guiNode = this.app.getGuiNode();
        
        // Crear nodo principal del juego
        gameNode = new Node("GameNode");
        rootNode.attachChild(gameNode);
        
        // Crear nodo para proyectiles
        bulletsNode = new Node("BulletsNode");
        gameNode.attachChild(bulletsNode);
        
        // Inicializar sistemas
        initializeBulletPool();
        initializeCore();
        initializePlayer();
        initializeEnemyManager();
        
        // Configurar la cámara para vista top-down
        setupCamera();
        
        // Configurar los controles
        setupInput();
        
        // Configurar UI
        setupUI();
    }
    
    /**
     * Inicializa el sistema de pool de balas para optimización de rendimiento.
     * 
     * <p>El BulletPool reutiliza instancias de balas para evitar la creación/destrucción
     * constante de objetos, mejorando significativamente el rendimiento en combates intensos.</p>
     * 
     * <p>Configuración del pool:</p>
     * <ul>
     *   <li><strong>Tamaño máximo:</strong> Definido en configuración (típicamente 50)</li>
     *   <li><strong>Velocidad:</strong> Velocidad uniforme de todas las balas</li>
     *   <li><strong>Tiempo de vida:</strong> Duración antes de auto-destrucción</li>
     *   <li><strong>Tamaño visual:</strong> Radio de las balas para colisiones</li>
     * </ul>
     * 
     * @see BulletPool
     */
    private void initializeBulletPool() {
        System.out.println("Inicializando BulletPool...");
        
        bulletPool = new BulletPool(
            app, // Usar Application directamente (constructor correcto)
            config.getBulletMaxPool(),
            config.getBulletSpeed(),
            config.getBulletSize(),
            config.getBulletLifetime()
        );
        
        System.out.println("BulletPool inicializado: " + bulletPool.getDebugInfo());
    }
    
    /**
     * Inicializa el núcleo central que debe ser defendido.
     * 
     * <p>El núcleo es el objetivo principal del juego. Los enemigos intentarán
     * alcanzarlo para causar daño. Cuando su vida llega a cero, el juego termina.</p>
     * 
     * <p>Configuración del núcleo:</p>
     * <ul>
     *   <li><strong>Posición:</strong> Centro exacto del área de juego (0,0,0)</li>
     *   <li><strong>Vida:</strong> Definida en configuración (típicamente 100)</li>
     *   <li><strong>Tamaño:</strong> Radio visual y de colisión</li>
     *   <li><strong>Control:</strong> CoreControl maneja lógica de vida y eventos</li>
     * </ul>
     * 
     * <p>El CoreControl se configura como listener para recibir eventos de
     * daño, curación y destrucción del núcleo.</p>
     * 
     * @see Core
     * @see CoreControl
     */
    private void initializeCore() {
        // Crear el núcleo con la configuración
        core = new Core(
            app.getAssetManager(),
            config.getCoreHealth(),
            config.getCoreSize()
        );
        
        // Crear y configurar el control
        coreControl = new CoreControl(core);
        coreControl.setCoreListener(this);
        core.addControl(coreControl);
        
        // Añadir al nodo del juego
        gameNode.attachChild(core);
        
        System.out.println("Core inicializado - Salud: " + config.getCoreHealth() + 
                         ", Tamaño: " + config.getCoreSize());
    }
    
    /**
     * Inicializa el jugador controlable por el usuario.
     * 
     * <p>El jugador es la entidad principal que el usuario controla para defender
     * el núcleo. Tiene capacidad de movimiento libre y disparo direccional.</p>
     * 
     * <p>Configuración del jugador:</p>
     * <ul>
     *   <li><strong>Posición inicial:</strong> Offset del centro (5, 0, 5)</li>
     *   <li><strong>Velocidad:</strong> Definida en configuración</li>
     *   <li><strong>Tamaño:</strong> Radio para colisiones y visualización</li>
     *   <li><strong>Límites:</strong> Área de juego de 15x15 unidades</li>
     * </ul>
     * 
     * <p>El PlayerControl se crea automáticamente dentro de la entidad Player
     * y se configura con los límites del área de juego para evitar que el
     * jugador salga de los bordes.</p>
     * 
     * @see Player
     * @see PlayerControl
     */
    private void initializePlayer() {
        // Crear el jugador con los parámetros correctos
        player = new Player(
            app,
            config.getPlayerSize(),
            config.getPlayerSpeed()
        );
        
        // Posicionar el jugador (offset del centro)
        player.getNode().setLocalTranslation(5f, 0f, 5f);
        
        // El PlayerControl ya se crea dentro de Player, solo necesitamos configurar límites
        player.getControl().setPlayAreaBounds(15f, 15f);
        
        // Añadir al nodo del juego
        gameNode.attachChild(player.getNode());
        
        System.out.println("Jugador inicializado - Velocidad: " + config.getPlayerSpeed());
    }
    
    /**
     * Inicializa el manager de enemigos
     */
    private void initializeEnemyManager() {
        enemyManager = new EnemyManager(app.getAssetManager(), gameNode, config);
        enemyManager.setListener(this);
        enemyManager.setCorePosition(Vector3f.ZERO);
        
        System.out.println("EnemyManager inicializado");
    }
    
    /**
     * Configura la cámara para vista top-down óptima del área de juego.
     * 
     * <p>La cámara se posiciona directamente encima del área de juego para
     * proporcionar una vista completa de la acción. Esta configuración es
     * esencial para el gameplay twin-stick shooter.</p>
     * 
     * <p>Configuración de cámara:</p>
     * <ul>
     *   <li><strong>Posición:</strong> (0, 25, 0) - 25 unidades por encima</li>
     *   <li><strong>Dirección:</strong> Mirando hacia abajo al centro (0,0,0)</li>
     *   <li><strong>Up vector:</strong> Eje Z para orientación correcta</li>
     *   <li><strong>Proyección:</strong> Perspectiva con FOV de 45°</li>
     *   <li><strong>Frustum:</strong> Cerca: 1, Lejos: 1000 unidades</li>
     * </ul>
     * 
     * <p>La proyección en perspectiva se mantiene en lugar de ortogonal
     * para dar profundidad visual, aunque la jugabilidad es 2D.</p>
     */
    private void setupCamera() {
        // Posicionar cámara para vista top-down
        cam.setLocation(new Vector3f(0, 25, 0));
        cam.lookAt(Vector3f.ZERO, Vector3f.UNIT_Z);
        
        // Asegurar que la cámara esté en modo ortogonal si es necesario
        // Para una vista top-down más clara
        cam.setParallelProjection(false); // Usar perspectiva
        
        // Configurar frustum para mejor vista del área de juego
        cam.setFrustumPerspective(45f, (float) cam.getWidth() / cam.getHeight(), 1f, 1000f);
        
        System.out.println("Cámara configurada en modo top-down - Posición: " + cam.getLocation());
    }
    
    /**
     * Configura el sistema completo de controles twin-stick.
     * 
     * <p>Implementa el esquema de control clásico de twin-stick shooters:</p>
     * <ul>
     *   <li><strong>Stick izquierdo (WASD):</strong> Movimiento direccional</li>
     *   <li><strong>Stick derecho (Mouse):</strong> Apuntado y disparo</li>
     * </ul>
     * 
     * <p>Mappings de entrada configurados:</p>
     * <ul>
     *   <li><strong>W/S:</strong> Movimiento vertical (adelante/atrás)</li>
     *   <li><strong>A/D:</strong> Movimiento horizontal (izquierda/derecha)</li>
     *   <li><strong>Mouse movement:</strong> Apuntado hacia cursor</li>
     *   <li><strong>Left click:</strong> Disparo hacia dirección del cursor</li>
     * </ul>
     * 
     * <p>Se configuran tres tipos de listeners:</p>
     * <ul>
     *   <li><strong>ActionListener:</strong> Para teclas de movimiento y disparo</li>
     *   <li><strong>RawInputListener:</strong> Para movimiento preciso del mouse</li>
     * </ul>
     * 
     * @see #moveListener
     * @see #shootListener
     * @see #mouseListener
     */
    private void setupInput() {
        // Configurar teclas de movimiento
        inputManager.addMapping(MOVE_UP, new KeyTrigger(KeyInput.KEY_W));
        inputManager.addMapping(MOVE_DOWN, new KeyTrigger(KeyInput.KEY_S));
        inputManager.addMapping(MOVE_LEFT, new KeyTrigger(KeyInput.KEY_A));
        inputManager.addMapping(MOVE_RIGHT, new KeyTrigger(KeyInput.KEY_D));
        
        // Configurar disparo con click izquierdo del mouse
        inputManager.addMapping(SHOOT, new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
        
        // Agregar listeners
        inputManager.addListener(moveListener, MOVE_UP, MOVE_DOWN, MOVE_LEFT, MOVE_RIGHT);
        inputManager.addListener(shootListener, SHOOT);
        
        // Agregar listener para el movimiento del mouse (apuntado)
        inputManager.addRawInputListener(mouseListener);
        
        System.out.println("Input configurado - WASD para movimiento, CLICK IZQUIERDO para disparar, MOUSE para apuntar");
    }
    
    /**
     * Configura la interfaz de usuario del juego.
     * 
     * <p>Inicializa todos los elementos del HUD incluyendo:</p>
     * <ul>
     *   <li><strong>Puntuación:</strong> Contador de puntos del jugador</li>
     * </ul>
     */
    private void setupUI() {
        // Obtener la fuente por defecto
        BitmapFont font = app.getAssetManager().loadFont("Interface/Fonts/Default.fnt");
        
        // Crear texto de puntuación
        scoreText = new BitmapText(font, false);
        scoreText.setSize(font.getCharSet().getRenderedSize() * 2);
        scoreText.setColor(com.jme3.math.ColorRGBA.White);
        scoreText.setText("Score: 0");
        scoreText.setLocalTranslation(10, app.getCamera().getHeight() - 10, 0);
        
        // Añadir al nodo GUI
        guiNode.attachChild(scoreText);
        
        System.out.println("UI configurada");
    }
    
    /**
     * Listener para el movimiento del jugador
     */
    private ActionListener moveListener = new ActionListener() {
        @Override
        public void onAction(String name, boolean isPressed, float tpf) {
            switch (name) {
                case MOVE_UP:
                    moveUp = isPressed;
                    break;
                case MOVE_DOWN:
                    moveDown = isPressed;
                    break;
                case MOVE_LEFT:
                    moveLeft = isPressed;
                    break;
                case MOVE_RIGHT:
                    moveRight = isPressed;
                    break;
            }
            updateMoveDirection();
        }
    };
    
    /**
     * Actualiza la dirección de movimiento basada en las teclas presionadas
     */
    private void updateMoveDirection() {
        moveDirection.set(0, 0, 0);
        
        // En vista top-down: X es horizontal, Z es vertical
        if (moveUp) moveDirection.addLocal(0, 0, 1);      // W mueve hacia arriba
        if (moveDown) moveDirection.addLocal(0, 0, -1);   // S mueve hacia abajo
        if (moveLeft) moveDirection.addLocal(1, 0, 0);    // A mueve a la izquierda
        if (moveRight) moveDirection.addLocal(-1, 0, 0);  // D mueve a la derecha
        
        player.getControl().setMoveDirection(moveDirection);
    }
    
    /**
     * Listener para el disparo
     */
    private ActionListener shootListener = new ActionListener() {
        @Override
        public void onAction(String name, boolean isPressed, float tpf) {
            if (name.equals(SHOOT) && isPressed) {
                System.out.println("Disparando...");
                shoot();
            }
        }
    };
    
    /**
     * Listener para el movimiento del mouse (apuntado)
     */
    private RawInputListener mouseListener = new RawInputListener() {
        @Override
        public void onMouseMotionEvent(com.jme3.input.event.MouseMotionEvent evt) {
            // Obtener posición del mouse en pantalla
            Vector2f mousePos = new Vector2f(evt.getX(), evt.getY());
            
            // Convertir posición del mouse a coordenadas del mundo
            Vector3f worldCoords = getWorldCoordinates(mousePos);
            
            // Calcular dirección desde el jugador hacia el punto del mundo
            Vector3f playerPos = player.getPosition();
            Vector3f aimDirection = worldCoords.subtract(playerPos);
            aimDirection.y = 0; // Mantener en el plano horizontal
            
            // Actualizar dirección de apuntado del jugador
            player.getControl().setAimDirection(aimDirection);
        }
        
        // Métodos no utilizados pero requeridos por la interfaz
        @Override
        public void beginInput() {}
        @Override
        public void endInput() {}
        @Override
        public void onJoyAxisEvent(com.jme3.input.event.JoyAxisEvent evt) {}
        @Override
        public void onJoyButtonEvent(com.jme3.input.event.JoyButtonEvent evt) {}
        @Override
        public void onMouseButtonEvent(com.jme3.input.event.MouseButtonEvent evt) {}
        @Override
        public void onKeyEvent(com.jme3.input.event.KeyInputEvent evt) {}
        @Override
        public void onTouchEvent(com.jme3.input.event.TouchEvent evt) {}
    };
    
    /**
     * Convierte coordenadas de pantalla a coordenadas del mundo
     * 
     * @param screenPos Posición en pantalla (píxeles)
     * @return Vector3f con las coordenadas en el mundo 3D
     */
    private Vector3f getWorldCoordinates(Vector2f screenPos) {
        // Crear un rayo desde la cámara a través de la posición del mouse
        Vector3f origin = cam.getWorldCoordinates(screenPos, 0f);
        Vector3f direction = cam.getWorldCoordinates(screenPos, 1f).subtractLocal(origin).normalizeLocal();
        
        // Como estamos en vista top-down, intersectamos con el plano Y=0
        Ray ray = new Ray(origin, direction);
        float t = -ray.origin.y / ray.direction.y;
        Vector3f intersection = ray.origin.add(ray.direction.mult(t));
        
        return intersection;
    }
    
    /**
     * Dispara un proyectil desde la posición del jugador
     */
    private void shoot() {
        // Obtener posición y dirección del jugador
        Vector3f playerPos = player.getPosition().clone();
        Vector3f aimDir = player.getControl().getAimDirection();
        
        // Offset para que la bala aparezca delante del jugador
        Vector3f spawnPos = playerPos.add(aimDir.mult(1.5f));
        
        // Obtener bala del pool
        Bullet bullet = bulletPool.getBullet(spawnPos, aimDir);
        
        if (bullet != null) {
            // Añadir a la escena
            bulletsNode.attachChild(bullet.getNode());
            System.out.println("DISPARO! Bala creada en: " + spawnPos + " | " + bulletPool.getDebugInfo());
        } else {
            System.out.println("ERROR: No se pudo obtener bala del pool!");
        }
    }
    
    @Override
    public void update(float tpf) {
        if (!gameOver && isEnabled()) {
            gameTime += tpf;
            
            // Actualizar el pool de balas
            bulletPool.update(tpf);
            
            // Actualizar el manager de enemigos
            enemyManager.update(tpf);
            
            // Detectar colisiones entre balas y enemigos
            detectBulletEnemyCollisions();
            
            // Debug: mostrar información cada 5 segundos
            if ((int)(gameTime / 5) != (int)((gameTime - tpf) / 5)) {
                System.out.println("Estadísticas - Oleada: " + enemyManager.getCurrentWave() + 
                                 ", Enemigos activos: " + enemyManager.getActiveEnemyCount() +
                                 ", Balas activas: " + bulletPool.getActiveBulletCount());
            }
        }
    }
    
    /**
     * Detecta colisiones entre balas y enemigos
     */
    private void detectBulletEnemyCollisions() {
        List<Bullet> activeBullets = bulletPool.getActiveBullets();
        List<Enemy> activeEnemies = enemyManager.getActiveEnemies();
        
        for (Bullet bullet : activeBullets) {
            if (bullet.shouldDestroy()) continue; // Ignorar balas que ya van a ser destruidas
            
            Vector3f bulletPos = bullet.getPosition();
            float bulletRadius = bullet.getSize();
            
            for (Enemy enemy : activeEnemies) {
                if (enemy.shouldDestroy() || enemy.isDead()) continue; // Ignorar enemigos muertos
                
                Vector3f enemyPos = enemy.getLocalTranslation();
                float enemyRadius = enemy.getSize();
                
                // Verificar colisión (distancia entre centros < suma de radios)
                float distance = bulletPos.distance(enemyPos);
                if (distance <= bulletRadius + enemyRadius) {
                    // ¡Colisión detectada!
                    handleBulletEnemyCollision(bullet, enemy);
                    break; // Una bala solo puede golpear un enemigo
                }
            }
        }
    }
    
    /**
     * Maneja la colisión entre una bala y un enemigo
     * 
     * @param bullet Bala que colisionó
     * @param enemy Enemigo que fue golpeado
     */
    private void handleBulletEnemyCollision(Bullet bullet, Enemy enemy) {
        // Aplicar daño al enemigo (balas hacen 25 de daño)
        boolean enemyDied = enemy.takeDamage(25f);
        
        // Destruir la bala
        bullet.getNode().setUserData("destroy", true);
        
        System.out.println("¡Colisión! Bala golpeó enemigo. Enemigo " + 
                         (enemyDied ? "eliminado" : "dañado (Salud: " + enemy.getHealth() + ")"));
    }
    
    /**
     * Pausa el juego
     */
    public void pauseGame() {
        setEnabled(false);
    }
    
    /**
     * Reanuda el juego
     */
    public void resumeGame() {
        setEnabled(true);
    }
    
    /**
     * Reinicia el juego
     */
    public void resetGame() {
        gameOver = false;
        gameTime = 0f;
        score = 0;
        updateScoreDisplay();
        
        // Reiniciar el núcleo
        coreControl.reset();
        
        // Reiniciar el pool de balas
        bulletPool.reset();
        
        // Reiniciar el manager de enemigos
        enemyManager.reset();
        
        setEnabled(true);
    }
    
    // Implementación de CoreListener
    
    @Override
    public void onCoreDestroyed() {
        System.out.println("¡GAME OVER! El núcleo ha sido destruido.");
        gameOver = true;
        
        // Detener spawn de enemigos
        enemyManager.cleanup();
        
        // Crear y mostrar pantalla de Game Over
        showGameOverScreen();
    }
    
    @Override
    public void onCoreDamaged(float currentHealth, float maxHealth) {
        System.out.println("Núcleo dañado - Salud: " + currentHealth + "/" + maxHealth + 
                         " (" + (int)(currentHealth/maxHealth * 100) + "%)");
        
        // TODO: Actualizar HUD (Fase 6)
        // TODO: Reproducir efecto de sonido
        
        // Advertencia si la salud es baja
        if (currentHealth / maxHealth < 0.25f) {
            System.out.println("¡ADVERTENCIA! ¡Salud del núcleo crítica!");
        }
    }
    
    @Override
    public void onCoreHealed(float currentHealth, float maxHealth) {
        System.out.println("Núcleo curado - Salud: " + currentHealth + "/" + maxHealth);
        
        // TODO: Actualizar HUD (Fase 6)
        // TODO: Efecto visual de curación
    }
    
    // Implementación de EnemyManagerListener
    
    @Override
    public void onEnemyReachedCore(Enemy enemy, float damage) {
        System.out.println("¡Enemigo alcanzó el núcleo! Aplicando " + (damage * 100) + "% de daño");
        
        // Calcular daño como porcentaje de la salud máxima del núcleo
        float actualDamage = damage * config.getCoreHealth();
        coreControl.applyDamage(actualDamage);
    }
    
    @Override
    public void onEnemyDied(Enemy enemy) {
        System.out.println("Enemigo eliminado por disparo del jugador");
        
        // Añadir puntuación
        addScore(10);
        
        // TODO: Efectos visuales/sonoros
        // TODO: Posibles drops de power-ups
    }
    
    @Override
    public void onNewWave(int waveNumber, int enemyCount) {
        System.out.println("=== NUEVA OLEADA " + waveNumber + " ===");
        System.out.println("Enemigos en esta oleada: " + enemyCount);
        
        // TODO: Mostrar notificación en UI
        // TODO: Efectos sonoros
    }
    
    @Override
    public void cleanup() {
        super.cleanup();
        
        // Limpiar input mappings
        if (inputManager != null) {
            inputManager.deleteMapping(MOVE_UP);
            inputManager.deleteMapping(MOVE_DOWN);
            inputManager.deleteMapping(MOVE_LEFT);
            inputManager.deleteMapping(MOVE_RIGHT);
            inputManager.deleteMapping(SHOOT);
            inputManager.removeRawInputListener(mouseListener);
        }
        
        // Limpiar recursos
        if (gameNode != null) {
            gameNode.removeFromParent();
        }
        
        if (bulletPool != null) {
            bulletPool.cleanup();
        }
        
        if (enemyManager != null) {
            enemyManager.cleanup();
        }
        
        // Limpiar UI
        if (scoreText != null) {
            scoreText.removeFromParent();
        }
    }
    
    // Getters para acceso desde otros sistemas
    
    public Player getPlayer() {
        return player;
    }
    
    public Core getCore() {
        return core;
    }
    
    public boolean isGameOver() {
        return gameOver;
    }
    
    public float getGameTime() {
        return gameTime;
    }
    
    public EnemyManager getEnemyManager() {
        return enemyManager;
    }
    
    /**
     * Obtiene la puntuación actual del jugador
     * 
     * @return Puntuación actual
     */
    public int getScore() {
        return score;
    }
    
    /**
     * Añade puntos a la puntuación del jugador
     * 
     * @param points Puntos a añadir
     */
    private void addScore(int points) {
        score += points;
        updateScoreDisplay();
        System.out.println("Puntuación: +" + points + " = " + score);
    }
    
    /**
     * Actualiza la visualización de la puntuación en pantalla
     */
    private void updateScoreDisplay() {
        if (scoreText != null) {
            scoreText.setText("Score: " + score);
        }
    }
    
    /**
     * Muestra la pantalla de Game Over con las estadísticas finales
     */
    private void showGameOverScreen() {
        // Crear estado de Game Over con estadísticas finales
        GameOverState gameOverState = new GameOverState(
            config,
            score,
            enemyManager.getCurrentWave(),
            gameTime
        );
        
        // Remover este estado y agregar el de Game Over
        stateManager.detach(this);
        stateManager.attach(gameOverState);
        
        System.out.println("Transición a pantalla de Game Over");
    }
}