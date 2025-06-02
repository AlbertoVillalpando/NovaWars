package mygame.controls;

import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;

/**
 * Control para manejar el movimiento y rotación del jugador
 * Implementa el sistema twin-stick donde:
 * - WASD controla el movimiento
 * - Mouse controla la dirección de apuntado
 * 
 * @author Alberto Villalpando
 */
public class PlayerControl extends AbstractControl {
    
    private float speed;
    private Vector3f moveDirection = new Vector3f();
    private Vector3f aimDirection = new Vector3f(0, 0, -1); // Dirección inicial
    private Vector3f velocity = new Vector3f();
    
    // Límites del área de juego (se calcularán basados en la vista de la cámara)
    private float maxX = 20f;
    private float maxZ = 20f;
    
    /**
     * Constructor del control del jugador
     * 
     * @param speed Velocidad de movimiento del jugador
     */
    public PlayerControl(float speed) {
        this.speed = speed;
    }
    
    /**
     * Actualiza la posición y rotación del jugador cada frame
     * 
     * @param tpf Time per frame (delta time)
     */
    @Override
    protected void controlUpdate(float tpf) {
        if (spatial == null) return;
        
        // Calcular velocidad basada en la dirección de movimiento
        velocity.set(moveDirection).multLocal(speed * tpf);
        
        // Aplicar movimiento
        Vector3f newPos = spatial.getLocalTranslation().add(velocity);
        
        // Mantener al jugador dentro de los límites del área de juego
        newPos.x = FastMath.clamp(newPos.x, -maxX, maxX);
        newPos.z = FastMath.clamp(newPos.z, -maxZ, maxZ);
        newPos.y = 0; // Mantener en el plano Y=0
        
        spatial.setLocalTranslation(newPos);
        
        // Rotar hacia la dirección de apuntado si hay una dirección válida
        if (aimDirection.lengthSquared() > 0.01f) {
            rotateTowards(aimDirection);
        }
    }
    
    /**
     * Rota el spatial hacia la dirección especificada
     * 
     * @param direction Dirección hacia la cual rotar
     */
    private void rotateTowards(Vector3f direction) {
        // Normalizar la dirección
        Vector3f normalizedDir = direction.normalize();
        
        // Calcular el ángulo de rotación en el plano XZ
        // Corregido: removemos las negaciones para que la rotación sea correcta
        float angle = FastMath.atan2(normalizedDir.x, normalizedDir.z);
        
        // Crear quaternion de rotación alrededor del eje Y
        Quaternion rotation = new Quaternion();
        rotation.fromAngleAxis(angle, Vector3f.UNIT_Y);
        
        spatial.setLocalRotation(rotation);
    }
    
    /**
     * Establece la dirección de movimiento basada en el input
     * 
     * @param direction Vector normalizado de dirección de movimiento
     */
    public void setMoveDirection(Vector3f direction) {
        this.moveDirection.set(direction);
        
        // Normalizar si el vector no es cero
        if (moveDirection.lengthSquared() > 0.01f) {
            moveDirection.normalizeLocal();
        }
    }
    
    /**
     * Establece la dirección de apuntado (hacia donde mira el jugador)
     * 
     * @param direction Vector de dirección de apuntado
     */
    public void setAimDirection(Vector3f direction) {
        this.aimDirection.set(direction);
    }
    
    /**
     * Obtiene la dirección actual de apuntado
     * Útil para el sistema de disparo
     * 
     * @return Vector3f dirección de apuntado normalizada
     */
    public Vector3f getAimDirection() {
        return aimDirection.normalize();
    }
    
    /**
     * Obtiene la posición actual del jugador
     * 
     * @return Vector3f posición del jugador
     */
    public Vector3f getPosition() {
        return spatial != null ? spatial.getLocalTranslation() : Vector3f.ZERO;
    }
    
    /**
     * Establece los límites del área de juego
     * 
     * @param maxX Límite máximo en el eje X
     * @param maxZ Límite máximo en el eje Z
     */
    public void setPlayAreaBounds(float maxX, float maxZ) {
        this.maxX = maxX;
        this.maxZ = maxZ;
    }
    
    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
        // No se necesita lógica de renderizado especial
    }
}