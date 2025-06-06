package mygame.controls;


import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;

/**
 * Control de comportamiento para proyectiles - Movimiento lineal y auto-destrucción.
 * 
 * <p>BulletControl maneja toda la lógica de vida de un proyectil desde su creación
 * hasta su destrucción. Implementa movimiento lineal constante con múltiples
 * condiciones de terminación para optimizar rendimiento.</p>
 * 
 * <h3>Comportamiento de movimiento:</h3>
 * <ul>
 *   <li><strong>Trayectoria lineal:</strong> Movimiento en línea recta hacia objetivo</li>
 *   <li><strong>Velocidad constante:</strong> Sin aceleración ni deceleración</li>
 *   <li><strong>Dirección fija:</strong> Sin cambios de rumbo tras disparo</li>
 *   <li><strong>Movimiento basado en tiempo:</strong> Consistencia independiente de FPS</li>
 * </ul>
 * 
 * <h3>Condiciones de auto-destrucción:</h3>
 * <ul>
 *   <li><strong>Tiempo de vida:</strong> Destrucción automática tras lifetime segundos</li>
 *   <li><strong>Límite de distancia:</strong> Eliminación al salir del área válida</li>
 *   <li><strong>Colisión externa:</strong> Marcado por GameState al impactar enemigos</li>
 *   <li><strong>Optimización de memoria:</strong> Retorno automático al pool</li>
 * </ul>
 * 
 * <h3>Sistema de marcado para destrucción:</h3>
 * <ul>
 *   <li><strong>UserData "destroy":</strong> Flag booleano para eliminación</li>
 *   <li><strong>Verificación por sistemas:</strong> GameState y BulletPool monitoran</li>
 *   <li><strong>Limpieza automática:</strong> BulletPool maneja reutilización</li>
 * </ul>
 * 
 * <h3>Métricas de rendimiento:</h3>
 * <ul>
 *   <li><strong>Age tracking:</strong> Tiempo transcurrido desde creación</li>
 *   <li><strong>Life percentage:</strong> Porcentaje de vida útil consumido</li>
 *   <li><strong>Distance checking:</strong> Validación eficiente de límites</li>
 *   <li><strong>Pooling optimized:</strong> Diseñado para reutilización de objetos</li>
 * </ul>
 * 
 * <h3>Integración con sistemas:</h3>
 * <ul>
 *   <li><strong>BulletPool:</strong> Gestión de ciclo de vida y reutilización</li>
 *   <li><strong>GameState:</strong> Detección de colisiones y marcado para destrucción</li>
 *   <li><strong>Player:</strong> Origen de dirección y parámetros iniciales</li>
 * </ul>
 * 
 * @author Alberto Villalpando
 * @version 1.0
 * @see Bullet
 * @see BulletPool
 * @see GameState#detectBulletEnemyCollisions()
 * @since 2024
 */
public class BulletControl extends AbstractControl {
    
    private Vector3f direction;
    private float speed;
    private float lifetime;
    private float age = 0f;
    
    // Límites del área de juego
    private float maxDistance = 40f;
    
    /**
     * Constructor del control de bala
     * 
     * @param direction Dirección normalizada de movimiento
     * @param speed Velocidad de la bala
     * @param lifetime Tiempo de vida en segundos
     */
    public BulletControl(Vector3f direction, float speed, float lifetime) {
        this.direction = direction.normalize();
        this.speed = speed;
        this.lifetime = lifetime;
    }
    
    /**
     * Actualiza la posición de la bala cada frame
     * 
     * @param tpf Time per frame
     */
    @Override
    protected void controlUpdate(float tpf) {
        if (spatial == null) return;
        
        // Mover la bala en su dirección
        Vector3f velocity = direction.mult(speed * tpf);
        spatial.move(velocity);
        
        // Incrementar edad
        age += tpf;
        
        // Verificar si debe destruirse por tiempo o distancia
        if (shouldDestroy()) {
            // Marcar para destrucción
            spatial.setUserData("destroy", true);
        }
    }
    
    /**
     * Verifica si la bala debe ser destruida
     * 
     * @return true si debe destruirse
     */
    private boolean shouldDestroy() {
        // Destruir por tiempo de vida
        if (age >= lifetime) {
            return true;
        }
        
        // Destruir si sale del área de juego
        Vector3f pos = spatial.getLocalTranslation();
        float distance = pos.length();
        
        return distance > maxDistance;
    }
    
    /**
     * Obtiene la edad actual de la bala
     * Útil para efectos visuales basados en tiempo
     * 
     * @return edad en segundos
     */
    public float getAge() {
        return age;
    }
    
    /**
     * Obtiene el porcentaje de vida transcurrido
     * 
     * @return valor entre 0 y 1
     */
    public float getLifePercent() {
        return age / lifetime;
    }
    
    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
        // No se necesita lógica de renderizado especial
    }
}