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
import mygame.config.GameConfig;
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
 * Estado principal del juego que maneja todas las entidades y la lógica del juego.
 * Integra el jugador, el núcleo central, enemigos y sistemas de juego.
 * 
 * @author tu_nombre
 */
public class GameState extends AbstractAppState implements CoreControl.CoreListener, EnemyManager.EnemyManagerListener {
    
    private SimpleApplication app;
    private Node rootNode;
    private Node gameNode;
    private InputManager inputManager;
    private Camera cam;
    
    // Configuración del juego
    private GameConfig config;
    
    // Entidades principales
    private Player player;
    private Core core;
    private CoreControl coreControl;
    
    // Managers
    private BulletPool bulletPool;
    private EnemyManager enemyManager;
    private Node bulletsNode; // Nodo para organizar las balas
    
    // Estado del juego
    private boolean gameOver = false;
    private float gameTime = 0f;
    
    // Variables para el manejo del input
    private boolean moveUp, moveDown, moveLeft, moveRight;
    private Vector3f moveDirection = new Vector3f();
    
    // Constantes para los nombres de los mappings
    private static final String MOVE_UP = "MoveUp";
    private static final String MOVE_DOWN = "MoveDown";
    private static final String MOVE_LEFT = "MoveLeft";
    private static final String MOVE_RIGHT = "MoveRight";
    private static final String SHOOT = "Shoot";
    
    /**
     * Constructor del estado del juego
     * 
     * @param config Configuración del juego
     */
    public GameState(GameConfig config) {
        this.config = config;
    }
    
    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        super.initialize(stateManager, app);
        
        this.app = (SimpleApplication) app;
        this.rootNode = this.app.getRootNode();
        this.inputManager = this.app.getInputManager();
        this.cam = this.app.getCamera();
        
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
    }
    
    /**
     * Inicializa el pool de balas
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
     * Inicializa el núcleo central
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
     * Inicializa el jugador
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
     * Configura la cámara para vista top-down
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
     * Configura los controles del juego
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
        if (!gameOver) {
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
        
        // TODO: Mostrar pantalla de Game Over (Fase 6)
        // TODO: Detener spawn de enemigos
        // TODO: Mostrar estadísticas finales
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
        
        // TODO: Añadir puntuación
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
}