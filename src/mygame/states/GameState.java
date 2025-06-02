package mygame.states;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.RawInputListener;
import com.jme3.input.controls.*;
import com.jme3.math.Ray;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Node;
import mygame.config.GameConfig;
import mygame.entities.Player;
import mygame.entities.Bullet;
import mygame.managers.BulletPool;

/**
 * Estado principal del juego que maneja la lógica del gameplay
 * Controla el input, las entidades y el flujo del juego
 * 
 * @author Alberto Villalpando
 */
public class GameState extends BaseAppState {
    
    private SimpleApplication app;
    private Node rootNode;
    private InputManager inputManager;
    private Camera cam;
    
    private GameConfig config;
    private Player player;
    private BulletPool bulletPool;
    
    // Nodo para organizar las entidades del juego
    private Node gameNode;
    private Node bulletsNode; // Nodo separado para proyectiles
    
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
     * @param config Configuración del juego cargada desde JSON
     */
    public GameState(GameConfig config) {
        this.config = config;
    }
    
    /**
     * Inicializa el pool de balas
     */
    private void initBulletPool() {
        // Crear pool con 50 balas iniciales
        bulletPool = new BulletPool(
            app,
            50, 
            config.bullet.speed, 
            config.bullet.size, 
            3f
        );
    }
    
    /**
     * Dispara un proyectil desde la posición del jugador
     */
    private void shoot() {
        // Obtener posición y dirección del jugador
        Vector3f playerPos = player.getPosition().clone();
        Vector3f aimDir = player.getControl().getAimDirection();
        
        // Offset para que la bala aparezca delante del jugador
        Vector3f spawnPos = playerPos.add(aimDir.mult(player.getNode().getWorldBound().getVolume()));
        
        // Obtener bala del pool
        Bullet bullet = bulletPool.getBullet(spawnPos, aimDir);
        
        if (bullet != null) {
            // Añadir a la escena
            bulletsNode.attachChild(bullet.getNode());
            // Debug: Mostrar estado del pool
            System.out.println(bulletPool.getDebugInfo());
        } else {
            System.out.println("¡No hay balas disponibles en el pool!");
        }
    }
    
    @Override
    protected void initialize(Application app) {
        this.app = (SimpleApplication) app;
        this.rootNode = this.app.getRootNode();
        this.inputManager = this.app.getInputManager();
        this.cam = this.app.getCamera();
        
        // Crear nodo contenedor para el juego
        gameNode = new Node("GameNode");
        rootNode.attachChild(gameNode);
        
        // Crear nodo para proyectiles
        bulletsNode = new Node("BulletsNode");
        gameNode.attachChild(bulletsNode);
        
        // Inicializar el jugador
        initPlayer();
        
        // Inicializar el pool de balas
        initBulletPool();
        
        // Configurar los controles
        setupInput();
        
        // TODO: Inicializar el núcleo central
        // TODO: Inicializar el HUD
    }
    
    /**
     * Inicializa la entidad del jugador
     */
    private void initPlayer() {
        player = new Player(app, config.player.size, config.player.speed);
        gameNode.attachChild(player.getNode());
        
        // Configurar los límites del área de juego basados en la vista de la cámara
        float playAreaSize = 15f; // Ajustar según necesidad
        player.getControl().setPlayAreaBounds(playAreaSize, playAreaSize);
    }
    
    /**
     * Configura los mappings de input y listeners
     */
    private void setupInput() {
        // Limpiar mappings existentes si los hay
        clearInputMappings();
        
        // Configurar teclas de movimiento
        inputManager.addMapping(MOVE_UP, new KeyTrigger(KeyInput.KEY_W));
        inputManager.addMapping(MOVE_DOWN, new KeyTrigger(KeyInput.KEY_S));
        inputManager.addMapping(MOVE_LEFT, new KeyTrigger(KeyInput.KEY_A));
        inputManager.addMapping(MOVE_RIGHT, new KeyTrigger(KeyInput.KEY_D));
        
        // Configurar disparo con click izquierdo
        inputManager.addMapping(SHOOT, new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
        
        // Agregar listeners
        inputManager.addListener(moveListener, MOVE_UP, MOVE_DOWN, MOVE_LEFT, MOVE_RIGHT);
        inputManager.addListener(shootListener, SHOOT);
        
        // Agregar listener para el movimiento del mouse (apuntado)
        inputManager.addRawInputListener(mouseListener);
    }
    
    /**
     * Limpia los mappings de input existentes
     */
    private void clearInputMappings() {
        inputManager.deleteMapping(MOVE_UP);
        inputManager.deleteMapping(MOVE_DOWN);
        inputManager.deleteMapping(MOVE_LEFT);
        inputManager.deleteMapping(MOVE_RIGHT);
        inputManager.deleteMapping(SHOOT);
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
     * Corregido para que coincida con la vista top-down
     */
    private void updateMoveDirection() {
        moveDirection.set(0, 0, 0);
        
        // En vista top-down: X es horizontal, Z es vertical
        if (moveUp) moveDirection.addLocal(0, 0, 1);     // W mueve hacia arriba (Z negativo)
        if (moveDown) moveDirection.addLocal(0, 0, -1);    // S mueve hacia abajo (Z positivo)
        if (moveLeft) moveDirection.addLocal(1, 0, 0);   // A mueve a la izquierda (X negativo)
        if (moveRight) moveDirection.addLocal(-1, 0, 0);   // D mueve a la derecha (X positivo)
        
        player.getControl().setMoveDirection(moveDirection);
    }
    
    /**
     * Listener para el disparo
     */
    private ActionListener shootListener = new ActionListener() {
        @Override
        public void onAction(String name, boolean isPressed, float tpf) {
            if (name.equals(SHOOT) && isPressed) {
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
    
    @Override
    public void update(float tpf) {
        // Actualizar el pool de balas
        if (bulletPool != null) {
            bulletPool.update();
        }
    }
    
    @Override
    protected void cleanup(Application app) {
        // Limpiar recursos
        clearInputMappings();
        if (bulletPool != null) {
            bulletPool.cleanup();
        }
        rootNode.detachChild(gameNode);
    }
    
    @Override
    protected void onEnable() {
        // Estado habilitado
    }
    
    @Override
    protected void onDisable() {
        // Estado deshabilitado
    }
}