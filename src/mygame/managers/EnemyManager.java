package mygame.managers;

import com.jme3.asset.AssetManager;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import java.util.ArrayList;
import java.util.List;
import mygame.config.GameConfig;
import mygame.entities.Enemy;
// Reactivando los enemigos complejos
import mygame.entities.CircularEnemy;
import mygame.entities.ZigZagEnemy;
import mygame.entities.BasicEnemy;

/**
 * Sistema de gestión de enemigos y oleadas - Motor de dificultad progresiva.
 * 
 * <p>EnemyManager es el cerebro del sistema de enemigos que controla el spawning,
 * la progresión de oleadas, la escalada de dificultad y la coordinación de
 * diferentes tipos de enemigos para crear una experiencia de juego balanceada.</p>
 * 
 * <h3>Sistema de oleadas:</h3>
 * <ul>
 *   <li><strong>Progresión automática:</strong> Oleadas se generan continuamente</li>
 *   <li><strong>Dificultad escalante:</strong> Más enemigos y tipos variados por oleada</li>
 *   <li><strong>Timing controlado:</strong> Intervalos entre spawns y oleadas</li>
 *   <li><strong>Notificaciones:</strong> Eventos para UI y feedback al jugador</li>
 * </ul>
 * 
 * <h3>Tipos de enemigos gestionados:</h3>
 * <ul>
 *   <li><strong>BasicEnemy:</strong> Movimiento directo, oleadas iniciales</li>
 *   <li><strong>CircularEnemy:</strong> Patrón orbital, dificultad media</li>
 *   <li><strong>ZigZagEnemy:</strong> Movimiento evasivo, dificultad alta</li>
 *   <li><strong>Mix dinámico:</strong> Combinaciones variables según oleada</li>
 * </ul>
 * 
 * <h3>Algoritmo de spawning:</h3>
 * <ul>
 *   <li><strong>Posicionamiento circular:</strong> Aparición en perímetro del área</li>
 *   <li><strong>Distribución aleatoria:</strong> Ángulos variables para impredecibilidad</li>
 *   <li><strong>Distancia segura:</strong> Spawning fuera del alcance inmediato</li>
 *   <li><strong>Targeting automático:</strong> Todos los enemigos conocen posición del núcleo</li>
 * </ul>
 * 
 * <h3>Escalada de dificultad:</h3>
 * <ul>
 *   <li><strong>Cantidad:</strong> Más enemigos por oleada con el tiempo</li>
 *   <li><strong>Variedad:</strong> Introducción gradual de enemigos complejos</li>
 *   <li><strong>Velocidad:</strong> Intervalos de spawn más cortos</li>
 *   <li><strong>Composición:</strong> Balance dinámico de tipos de enemigos</li>
 * </ul>
 * 
 * <h3>Gestión de ciclo de vida:</h3>
 * <ul>
 *   <li><strong>Lista activa:</strong> Tracking de todos los enemigos en pantalla</li>
 *   <li><strong>Cleanup automático:</strong> Eliminación de enemigos muertos</li>
 *   <li><strong>Eventos de llegada:</strong> Manejo cuando enemigos alcanzan núcleo</li>
 *   <li><strong>Reset completo:</strong> Limpieza para reinicio de juego</li>
 * </ul>
 * 
 * <h3>Patrón Observer implementado:</h3>
 * <ul>
 *   <li><strong>EnemyManagerListener:</strong> Interface para eventos</li>
 *   <li><strong>onEnemyReachedCore():</strong> Notificación de daño al núcleo</li>
 *   <li><strong>onEnemyDied():</strong> Notificación de eliminación por jugador</li>
 *   <li><strong>onNewWave():</strong> Notificación de inicio de oleada</li>
 * </ul>
 * 
 * <h3>Integración con sistemas:</h3>
 * <ul>
 *   <li><strong>GameState:</strong> Listener principal para eventos</li>
 *   <li><strong>GameConfig:</strong> Parámetros de enemigos y dificultad</li>
 *   <li><strong>CoreControl:</strong> Aplicación de daño cuando enemigos llegan</li>
 * </ul>
 * 
 * @author Alberto Villalpando
 * @version 1.0
 * @see Enemy
 * @see EnemyManagerListener
 * @see GameState
 * @since 2024
 */
public class EnemyManager {
    
    private AssetManager assetManager;
    private Node gameNode;
    private Node enemiesNode;
    private GameConfig config;
    
    // Lista de enemigos activos
    private List<Enemy> activeEnemies;
    
    // Sistema de oleadas
    private int currentWave = 0;
    private float waveTimer = 0f;
    private float timeSinceLastSpawn = 0f;
    private boolean waveInProgress = false;
    private int enemiesToSpawnInCurrentWave = 0;
    private int enemiesSpawnedInCurrentWave = 0;
    
    // Configuración del área de spawn
    private float spawnRadius = 18f; // Radio donde aparecen los enemigos
    private Vector3f corePosition = Vector3f.ZERO;
    
    // Listeners para eventos
    private EnemyManagerListener listener;
    
    /**
     * Interface para eventos del EnemyManager
     */
    public interface EnemyManagerListener {
        /**
         * Se llama cuando un enemigo alcanza el núcleo
         * 
         * @param enemy Enemigo que alcanzó el núcleo
         * @param damage Daño que hará al núcleo
         */
        void onEnemyReachedCore(Enemy enemy, float damage);
        
        /**
         * Se llama cuando un enemigo muere
         * 
         * @param enemy Enemigo que murió
         */
        void onEnemyDied(Enemy enemy);
        
        /**
         * Se llama cuando comienza una nueva oleada
         * 
         * @param waveNumber Número de oleada
         * @param enemyCount Cantidad de enemigos en la oleada
         */
        void onNewWave(int waveNumber, int enemyCount);
    }
    
    /**
     * Constructor del EnemyManager
     * 
     * @param assetManager AssetManager para crear enemigos
     * @param gameNode Nodo principal del juego
     * @param config Configuración del juego
     */
    public EnemyManager(AssetManager assetManager, Node gameNode, GameConfig config) {
        this.assetManager = assetManager;
        this.gameNode = gameNode;
        this.config = config;
        
        // Crear nodo para organizar enemigos
        enemiesNode = new Node("EnemiesNode");
        gameNode.attachChild(enemiesNode);
        
        // Inicializar lista de enemigos activos
        activeEnemies = new ArrayList<>();
        
        System.out.println("EnemyManager inicializado");
    }
    
    /**
     * Actualiza el manager de enemigos
     * 
     * @param tpf Time per frame
     */
    public void update(float tpf) {
        // Actualizar timer de oleada
        waveTimer += tpf;
        timeSinceLastSpawn += tpf;
        
        // Verificar si es hora de iniciar una nueva oleada
        if (!waveInProgress && waveTimer >= config.getWaveInterval()) {
            startNewWave();
        }
        
        // Spawn enemigos durante la oleada
        if (waveInProgress) {
            updateWaveSpawning(tpf);
        }
        
        // Actualizar enemigos activos
        updateActiveEnemies();
    }
    
    /**
     * Inicia una nueva oleada de enemigos
     */
    private void startNewWave() {
        currentWave++;
        waveInProgress = true;
        waveTimer = 0f;
        timeSinceLastSpawn = 0f;
        
        // Calcular cantidad de enemigos para esta oleada
        enemiesToSpawnInCurrentWave = calculateWaveEnemyCount();
        enemiesSpawnedInCurrentWave = 0;
        
        System.out.println("¡Oleada " + currentWave + " iniciada! Enemigos: " + enemiesToSpawnInCurrentWave);
        
        // Notificar al listener
        if (listener != null) {
            listener.onNewWave(currentWave, enemiesToSpawnInCurrentWave);
        }
    }
    
    /**
     * Calcula la cantidad de enemigos para la oleada actual
     * 
     * @return Cantidad de enemigos
     */
    private int calculateWaveEnemyCount() {
        float baseCount = config.getBaseWaveSize();
        float scaling = config.getWaveScaling();
        
        // Fórmula: baseCount + (wave - 1) * scaling
        return Math.round(baseCount + (currentWave - 1) * scaling);
    }
    
    /**
     * Actualiza el spawn de enemigos durante una oleada
     * 
     * @param tpf Time per frame
     */
    private void updateWaveSpawning(float tpf) {
        // Verificar si podemos hacer spawn de más enemigos
        if (enemiesSpawnedInCurrentWave < enemiesToSpawnInCurrentWave &&
            timeSinceLastSpawn >= config.getEnemySpawnInterval() &&
            activeEnemies.size() < config.getEnemyMaxOnScreen()) {
            
            spawnRandomEnemy();
            timeSinceLastSpawn = 0f;
            enemiesSpawnedInCurrentWave++;
        }
        
        // Terminar oleada si hemos spawneado todos los enemigos y no quedan activos
        if (enemiesSpawnedInCurrentWave >= enemiesToSpawnInCurrentWave && activeEnemies.isEmpty()) {
            endCurrentWave();
        }
    }
    
    /**
     * Termina la oleada actual
     */
    private void endCurrentWave() {
        waveInProgress = false;
        waveTimer = 0f;
        System.out.println("Oleada " + currentWave + " completada");
    }
    
    /**
     * Hace spawn de un enemigo aleatorio
     */
    private void spawnRandomEnemy() {
        Vector3f spawnPos = getRandomSpawnPosition();
        
        // Ahora usar los tres tipos de enemigos
        Enemy enemy;
        float rand = FastMath.nextRandomFloat();
        
        if (rand < 0.33f) {
            // 33% probabilidad de CircularEnemy
            enemy = new CircularEnemy(assetManager, config);
            System.out.println("Spawn CircularEnemy en: " + spawnPos);
        } else if (rand < 0.66f) {
            // 33% probabilidad de ZigZagEnemy
            enemy = new ZigZagEnemy(assetManager, config);
            System.out.println("Spawn ZigZagEnemy en: " + spawnPos);
        } else {
            // 33% probabilidad de BasicEnemy
            enemy = new BasicEnemy(assetManager, config);
            System.out.println("Spawn BasicEnemy en: " + spawnPos);
        }
        
        // Configurar posición y control
        enemy.setLocalTranslation(spawnPos);
        if (enemy.getControl() != null) {
            enemy.getControl().setCorePosition(corePosition);
            enemy.getControl().setPlayAreaRadius(spawnRadius);
            enemy.getControl().setCoreRadius(config.getCoreSize());
        }
        
        // Añadir a la escena y lista activa
        enemiesNode.attachChild(enemy);
        activeEnemies.add(enemy);
    }
    
    /**
     * Obtiene una posición aleatoria en el borde del área de juego
     * 
     * @return Vector3f posición de spawn
     */
    private Vector3f getRandomSpawnPosition() {
        // Ángulo aleatorio
        float angle = FastMath.nextRandomFloat() * FastMath.TWO_PI;
        
        // Posición en el borde del círculo
        float x = FastMath.cos(angle) * spawnRadius;
        float z = FastMath.sin(angle) * spawnRadius;
        
        return new Vector3f(x, 0, z);
    }
    
    /**
     * Actualiza los enemigos activos y limpia los que deben ser destruidos
     */
    private void updateActiveEnemies() {
        // Usar iterador para evitar ConcurrentModificationException
        java.util.Iterator<Enemy> iterator = activeEnemies.iterator();
        
        while (iterator.hasNext()) {
            Enemy enemy = iterator.next();
            
            // Verificar si el enemigo debe ser destruido
            if (enemy.shouldDestroy()) {
                // Verificar por qué se destruye
                if (enemy.hasReachedCore()) {
                    // El enemigo alcanzó el núcleo
                    if (listener != null) {
                        listener.onEnemyReachedCore(enemy, enemy.getCoreDamage());
                    }
                } else if (enemy.isDead()) {
                    // El enemigo murió por disparo
                    if (listener != null) {
                        listener.onEnemyDied(enemy);
                    }
                }
                
                // Remover de la escena
                enemy.removeFromParent();
                
                // Remover de la lista usando el iterador
                iterator.remove();
            }
        }
    }
    
    /**
     * Establece la posición del núcleo para navegación de enemigos
     * 
     * @param corePosition Posición del núcleo
     */
    public void setCorePosition(Vector3f corePosition) {
        this.corePosition = corePosition.clone();
        
        // Actualizar posición en todos los enemigos activos
        for (Enemy enemy : activeEnemies) {
            if (enemy.getControl() != null) {
                enemy.getControl().setCorePosition(corePosition);
            }
        }
    }
    
    /**
     * Establece el listener para eventos del manager
     * 
     * @param listener Listener que recibirá las notificaciones
     */
    public void setListener(EnemyManagerListener listener) {
        this.listener = listener;
    }
    
    /**
     * Obtiene la lista de enemigos activos
     * 
     * @return Lista de enemigos activos
     */
    public List<Enemy> getActiveEnemies() {
        return new ArrayList<>(activeEnemies);
    }
    
    /**
     * Obtiene el número de oleada actual
     * 
     * @return Número de oleada
     */
    public int getCurrentWave() {
        return currentWave;
    }
    
    /**
     * Obtiene la cantidad de enemigos activos
     * 
     * @return Cantidad de enemigos activos
     */
    public int getActiveEnemyCount() {
        return activeEnemies.size();
    }
    
    /**
     * Limpia todos los recursos del manager
     */
    public void cleanup() {
        // Limpiar todos los enemigos activos
        for (Enemy enemy : activeEnemies) {
            enemy.removeFromParent();
        }
        activeEnemies.clear();
        
        // Remover nodo de enemigos
        if (enemiesNode != null) {
            enemiesNode.removeFromParent();
        }
        
        System.out.println("EnemyManager limpiado");
    }
    
    /**
     * Reinicia el manager para un nuevo juego
     */
    public void reset() {
        cleanup();
        
        // Reinicializar estado
        currentWave = 0;
        waveTimer = 0f;
        timeSinceLastSpawn = 0f;
        waveInProgress = false;
        enemiesToSpawnInCurrentWave = 0;
        enemiesSpawnedInCurrentWave = 0;
        
        // Recrear nodo de enemigos
        enemiesNode = new Node("EnemiesNode");
        gameNode.attachChild(enemiesNode);
        
        // Reinicializar lista
        activeEnemies = new ArrayList<>();
        
        System.out.println("EnemyManager reiniciado");
    }
} 