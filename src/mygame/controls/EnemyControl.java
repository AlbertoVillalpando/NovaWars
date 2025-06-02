package mygame.controls;

import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.control.AbstractControl;
import mygame.entities.Enemy;

/**
 * Control base para todos los enemigos del juego.
 * Proporciona funcionalidad común como detección de límites,
 * cálculo de distancia al núcleo, etc.
 * 
 * @author Alberto Villalpando
 */
public abstract class EnemyControl extends AbstractControl {
    
    protected Enemy enemy;
    protected Vector3f corePosition = Vector3f.ZERO; // Posición del núcleo
    protected float playAreaRadius = 20f; // Radio del área de juego
    protected float coreRadius = 2f; // Radio del núcleo para detección de colisión
    
    // Estado del control
    protected boolean isActive = true;
    
    /**
     * Constructor del control de enemigo
     * 
     * @param enemy Referencia al enemigo que controla
     */
    public EnemyControl(Enemy enemy) {
        this.enemy = enemy;
    }
    
    /**
     * Actualización principal del control
     * 
     * @param tpf Time per frame
     */
    @Override
    protected void controlUpdate(float tpf) {
        if (!isActive || spatial == null || enemy == null) return;
        
        // Verificar si el enemigo está muerto
        if (enemy.isDead()) {
            setEnabled(false);
            return;
        }
        
        // Actualizar movimiento (implementado por subclases)
        updateMovement(tpf);
        
        // Verificar colisión con el núcleo
        checkCoreCollision();
        
        // Verificar si está fuera del área de juego
        checkBounds();
    }
    
    /**
     * Método abstracto para actualizar el movimiento específico del enemigo
     * Debe ser implementado por cada subclase
     * 
     * @param tpf Time per frame
     */
    protected abstract void updateMovement(float tpf);
    
    /**
     * Verifica si el enemigo ha colisionado con el núcleo
     */
    protected void checkCoreCollision() {
        Vector3f enemyPos = spatial.getLocalTranslation();
        float distanceToCore = enemyPos.distance(corePosition);
        
        // Si está lo suficientemente cerca del núcleo
        if (distanceToCore <= coreRadius + enemy.getSize()) {
            // El enemigo alcanzó el núcleo
            enemy.reachCore();
            setEnabled(false);
        }
    }
    
    /**
     * Verifica si el enemigo está fuera del área de juego
     */
    protected void checkBounds() {
        Vector3f enemyPos = spatial.getLocalTranslation();
        float distanceFromCenter = enemyPos.length();
        
        // Si está muy lejos del centro, eliminarlo
        if (distanceFromCenter > playAreaRadius * 2) {
            enemy.setUserData("destroy", true);
            setEnabled(false);
        }
    }
    
    /**
     * Calcula la dirección normalizada hacia el núcleo
     * 
     * @return Vector3f dirección hacia el núcleo
     */
    protected Vector3f getDirectionToCore() {
        Vector3f enemyPos = spatial.getLocalTranslation();
        Vector3f direction = corePosition.subtract(enemyPos);
        direction.y = 0; // Mantener en el plano horizontal
        return direction.normalizeLocal();
    }
    
    /**
     * Calcula la distancia al núcleo
     * 
     * @return float distancia al núcleo
     */
    protected float getDistanceToCore() {
        Vector3f enemyPos = spatial.getLocalTranslation();
        return enemyPos.distance(corePosition);
    }
    
    /**
     * Mueve el enemigo en una dirección específica
     * 
     * @param direction Dirección de movimiento (normalizada)
     * @param speed Velocidad de movimiento
     * @param tpf Time per frame
     */
    protected void moveInDirection(Vector3f direction, float speed, float tpf) {
        Vector3f velocity = direction.mult(speed * tpf);
        spatial.move(velocity);
    }
    
    /**
     * Establece la posición del núcleo para navegación
     * 
     * @param corePosition Posición del núcleo
     */
    public void setCorePosition(Vector3f corePosition) {
        this.corePosition = corePosition.clone();
    }
    
    /**
     * Establece el radio del área de juego
     * 
     * @param radius Radio del área de juego
     */
    public void setPlayAreaRadius(float radius) {
        this.playAreaRadius = radius;
    }
    
    /**
     * Establece el radio del núcleo para detección de colisión
     * 
     * @param radius Radio del núcleo
     */
    public void setCoreRadius(float radius) {
        this.coreRadius = radius;
    }
    
    /**
     * Reinicia el control para reutilización
     */
    public void reset() {
        isActive = true;
        setEnabled(true);
    }
    
    /**
     * Activa/desactiva el control
     * 
     * @param active true para activar, false para desactivar
     */
    public void setActive(boolean active) {
        this.isActive = active;
    }
    
    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
        // No se necesita renderizado especial
    }
} 