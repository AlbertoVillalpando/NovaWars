package mygame.controls;


import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;

/**
 * Control para manejar el comportamiento de los proyectiles
 * Los proyectiles viajan en línea recta y se destruyen después de cierto tiempo
 * 
 * @author Alberto Villalpando
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