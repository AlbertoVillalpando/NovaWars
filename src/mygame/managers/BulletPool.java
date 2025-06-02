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
 * Pool de objetos para manejar eficientemente las balas
 * Evita crear y destruir objetos constantemente, mejorando el rendimiento
 * 
 * @author Alberto Villalpando
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