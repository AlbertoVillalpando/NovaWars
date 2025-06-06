package mygame.managers;


import com.jme3.app.Application;
import com.jme3.math.Vector3f;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import mygame.controls.BulletControl;
import mygame.entities.Bullet;
import com.jme3.app.SimpleApplication;

/**
 * Sistema de pooling de objetos para proyectiles - Optimización de rendimiento crítica.
 * 
 * <p>BulletPool implementa el patrón Object Pool para gestionar eficientemente
 * los proyectiles del jugador. Evita la creación/destrucción constante de objetos
 * durante el combate intenso, manteniendo un framerate estable.</p>
 * 
 * <h3>Arquitectura del pool:</h3>
 * <ul>
 *   <li><strong>Pool disponible:</strong> Queue de balas reutilizables</li>
 *   <li><strong>Lista activa:</strong> Balas actualmente en movimiento</li>
 *   <li><strong>Tamaño dinámico:</strong> Expansión automática cuando se agota</li>
 *   <li><strong>Configuración uniforme:</strong> Parámetros consistentes para todas las balas</li>
 * </ul>
 * 
 * <h3>Ciclo de vida de las balas:</h3>
 * <ul>
 *   <li><strong>Pre-inicialización:</strong> Creación inicial de pool completo</li>
 *   <li><strong>Obtención:</strong> getBullet() configura y activa una bala</li>
 *   <li><strong>Actividad:</strong> Bala se mueve y puede colisionar</li>
 *   <li><strong>Marcado destrucción:</strong> BulletControl o GameState marcan para eliminar</li>
 *   <li><strong>Retorno:</strong> returnBullet() devuelve al pool para reutilización</li>
 * </ul>
 * 
 * <h3>Optimizaciones implementadas:</h3>
 * <ul>
 *   <li><strong>Lazy expansion:</strong> Creación bajo demanda si pool se agota</li>
 *   <li><strong>Reset eficiente:</strong> Reutilización de objetos sin new/delete</li>
 *   <li><strong>Batch processing:</strong> update() procesa todas las balas en lote</li>
 *   <li><strong>Memory cleanup:</strong> Gestión de referencias para GC</li>
 * </ul>
 * 
 * <h3>Monitoreo y debug:</h3>
 * <ul>
 *   <li><strong>Contadores en tiempo real:</strong> Disponibles vs activas</li>
 *   <li><strong>Debug logging:</strong> Información de reciclaje y estado</li>
 *   <li><strong>Métricas de rendimiento:</strong> Tracking de expansiones del pool</li>
 * </ul>
 * 
 * <h3>Integración con sistemas:</h3>
 * <ul>
 *   <li><strong>GameState:</strong> Obtención de balas para disparos</li>
 *   <li><strong>BulletControl:</strong> Auto-marcado para destrucción</li>
 *   <li><strong>GameConfig:</strong> Parámetros uniformes para todas las balas</li>
 * </ul>
 * 
 * <h3>Consideraciones de rendimiento:</h3>
 * <ul>
 *   <li><strong>Pool size inicial:</strong> Balance entre memoria y creaciones dinámicas</li>
 *   <li><strong>Cleanup automático:</strong> Retorno automático de balas expiradas</li>
 *   <li><strong>Zero allocation shooting:</strong> Disparos sin impacto en GC</li>
 * </ul>
 * 
 * @author Alberto Villalpando
 * @version 1.0
 * @see Bullet
 * @see BulletControl
 * @see GameState#shoot()
 * @since 2024
 */
public class BulletPool {
    
    private Application app;
    private Queue<Bullet> availableBullets;
    private List<Bullet> activeBullets;
    private int poolSize;
    
    // Configuración de las balas
    private float bulletSpeed;
    private float bulletSize;
    private float bulletLifetime;
    
    /**
     * Constructor del pool de balas
     * 
     * @param app Referencia a la aplicación
     * @param poolSize Tamaño inicial del pool
     * @param speed Velocidad de las balas
     * @param size Tamaño de las balas
     * @param lifetime Tiempo de vida de las balas
     */
    public BulletPool(Application app, int poolSize, float speed, float size, float lifetime) {
        this.app = app;
        this.poolSize = poolSize;
        this.bulletSpeed = speed;
        this.bulletSize = size;
        this.bulletLifetime = lifetime;
        
        availableBullets = new LinkedList<>();
        activeBullets = new ArrayList<>();
        
        // Pre-crear balas
        initializePool();
    }
    
    /**
     * Constructor alternativo usando AssetManager
     * 
     * @param assetManager AssetManager para crear materiales
     * @param poolSize Tamaño inicial del pool
     * @param size Tamaño de las balas
     * @param speed Velocidad de las balas  
     * @param lifetime Tiempo de vida de las balas
     */
    public BulletPool(com.jme3.asset.AssetManager assetManager, int poolSize, float size, float speed, float lifetime) {
        // Crear una aplicación temporal para mantener compatibilidad
        this.app = new SimpleApplication() {
            @Override
            public void simpleInitApp() {}
            @Override
            public com.jme3.asset.AssetManager getAssetManager() {
                return assetManager;
            }
        };
        
        this.poolSize = poolSize;
        this.bulletSpeed = speed;
        this.bulletSize = size;
        this.bulletLifetime = lifetime;
        
        availableBullets = new LinkedList<>();
        activeBullets = new ArrayList<>();
        
        // Pre-crear balas
        initializePool();
    }
    
    /**
     * Inicializa el pool creando las balas
     */
    private void initializePool() {
        for (int i = 0; i < poolSize; i++) {
            Bullet bullet = createBullet();
            availableBullets.offer(bullet);
        }
    }
    
    /**
     * Crea una nueva bala con configuración por defecto
     * 
     * @return Nueva instancia de Bullet
     */
    private Bullet createBullet() {
        // Crear con valores temporales, se actualizarán al disparar
        Bullet bullet = new Bullet(app, Vector3f.ZERO, Vector3f.UNIT_Z, 
                         bulletSpeed, bulletSize, bulletLifetime);
        
        return bullet;
    }
    
    /**
     * Obtiene una bala del pool y la configura
     * 
     * @param origin Posición inicial
     * @param direction Dirección de disparo
     * @return Bullet configurada o null si no hay disponibles
     */
    public Bullet getBullet(Vector3f origin, Vector3f direction) {
        Bullet bullet = availableBullets.poll();
        
        // Si no hay balas disponibles, crear una nueva
        if (bullet == null) {
            bullet = createBullet();
        }
        
        // Reconfigurar la bala
        resetBullet(bullet, origin, direction);
        activeBullets.add(bullet);
        
        return bullet;
    }
    
    /**
     * Reconfigura una bala para reutilizarla
     * 
     * @param bullet Bala a reconfigurar
     * @param origin Nueva posición inicial
     * @param direction Nueva dirección
     */
    private void resetBullet(Bullet bullet, Vector3f origin, Vector3f direction) {
        // Usar el método reset de la bala que maneja correctamente el reciclaje
        bullet.reset(origin, direction, bulletSpeed, bulletLifetime);
    }
    
    /**
     * Devuelve una bala al pool
     * 
     * @param bullet Bala a devolver
     */
    public void returnBullet(Bullet bullet) {
        if (activeBullets.remove(bullet)) {
            // Remover de la escena sin destruir el objeto
            bullet.getNode().removeFromParent();
            // Limpiar flag de destrucción
            bullet.getNode().setUserData("destroy", false);
            // Devolver al pool para reutilización
            availableBullets.offer(bullet);
        }
    }
    
    /**
     * Actualiza el pool, devolviendo balas marcadas para destrucción
     */
    public void update() {
        List<Bullet> toReturn = new ArrayList<>();
        
        // Debug: contador de balas marcadas para destrucción
        int destroyCount = 0;
        
        for (Bullet bullet : activeBullets) {
            if (bullet.shouldDestroy()) {
                toReturn.add(bullet);
                destroyCount++;
            }
        }
        
        // Debug logging
        if (destroyCount > 0) {
            System.out.println("BulletPool.update() - Balas a devolver: " + destroyCount + 
                             " | Activas antes: " + activeBullets.size());
        }
        
        for (Bullet bullet : toReturn) {
            returnBullet(bullet);
        }
        
        // Debug: información final si hubo cambios
        if (destroyCount > 0) {
            System.out.println("BulletPool.update() - Completado | " + getDebugInfo());
        }
    }
    
    /**
     * Obtiene la lista de balas activas
     * 
     * @return Lista de balas actualmente en uso
     */
    public List<Bullet> getActiveBullets() {
        return new ArrayList<>(activeBullets);
    }
    
    /**
     * Limpia todos los recursos del pool
     */
    public void cleanup() {
        for (Bullet bullet : activeBullets) {
            bullet.cleanup();
        }
        for (Bullet bullet : availableBullets) {
            bullet.cleanup();
        }
        activeBullets.clear();
        availableBullets.clear();
    }
    
    /**
     * Obtiene el número de balas disponibles en el pool
     * 
     * @return Número de balas disponibles
     */
    public int getAvailableCount() {
        return availableBullets.size();
    }
    
    /**
     * Obtiene el número de balas actualmente en uso
     * 
     * @return Número de balas activas
     */
    public int getActiveCount() {
        return activeBullets.size();
    }
    
    /**
     * Alias para getActiveCount() - obtiene el número de balas actualmente en uso
     * 
     * @return Número de balas activas
     */
    public int getActiveBulletCount() {
        return getActiveCount();
    }
    
    /**
     * Obtiene información de depuración del pool
     * 
     * @return String con información del estado del pool
     */
    public String getDebugInfo() {
        return String.format("BulletPool - Disponibles: %d, Activas: %d, Total: %d", 
                           getAvailableCount(), getActiveCount(), poolSize);
    }
    
    /**
     * Actualiza el pool con delta time (alternativa al método update())
     * 
     * @param tpf Time per frame
     */
    public void update(float tpf) {
        // Llamar al método update() sin parámetros
        update();
    }
    
    /**
     * Reinicia el pool devolviendo todas las balas activas al pool
     */
    public void reset() {
        // Devolver todas las balas activas al pool
        List<Bullet> allActive = new ArrayList<>(activeBullets);
        for (Bullet bullet : allActive) {
            returnBullet(bullet);
        }
        
        System.out.println("BulletPool reseteado - " + getDebugInfo());
    }
}