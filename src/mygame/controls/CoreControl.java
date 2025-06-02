package mygame.controls;


import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.control.AbstractControl;
import mygame.entities.Core;

/**
 * Control que maneja la lógica de actualización del núcleo central.
 * Este control se encarga de actualizar el estado del núcleo cada frame,
 * incluyendo efectos visuales, invulnerabilidad y detección de destrucción.
 * 
 * @author tu_nombre
 */
public class CoreControl extends AbstractControl {
    
    private Core core;
    private CoreListener listener;
    
    /**
     * Interface para notificar eventos del núcleo
     */
    public interface CoreListener {
        /**
         * Se llama cuando el núcleo es destruido
         */
        void onCoreDestroyed();
        
        /**
         * Se llama cuando el núcleo recibe daño
         * 
         * @param currentHealth Salud actual después del daño
         * @param maxHealth Salud máxima
         */
        void onCoreDamaged(float currentHealth, float maxHealth);
        
        /**
         * Se llama cuando el núcleo es curado
         * 
         * @param currentHealth Salud actual después de la curación
         * @param maxHealth Salud máxima
         */
        void onCoreHealed(float currentHealth, float maxHealth);
    }
    
    /**
     * Constructor del control
     * 
     * @param core Referencia al núcleo que controla
     */
    public CoreControl(Core core) {
        this.core = core;
    }
    
    /**
     * Establece el listener para eventos del núcleo
     * 
     * @param listener Listener que recibirá las notificaciones
     */
    public void setCoreListener(CoreListener listener) {
        this.listener = listener;
    }
    
    /**
     * Actualización principal del control
     * 
     * @param tpf Time per frame
     */
    @Override
    protected void controlUpdate(float tpf) {
        if (core != null) {
            // Actualizar el núcleo
            core.update(tpf);
            
            // Verificar si fue destruido
            if (core.isDestroyed() && listener != null) {
                listener.onCoreDestroyed();
                // Desactivar el control después de notificar
                setEnabled(false);
            }
        }
    }
    
    /**
     * Aplica daño al núcleo y notifica al listener
     * 
     * @param damage Cantidad de daño a aplicar
     */
    public void applyDamage(float damage) {
        if (core != null && isEnabled()) {
            boolean destroyed = core.takeDamage(damage);
            
            if (listener != null) {
                if (destroyed) {
                    listener.onCoreDestroyed();
                    setEnabled(false);
                } else {
                    listener.onCoreDamaged(core.getCurrentHealth(), core.getMaxHealth());
                }
            }
        }
    }
    
    /**
     * Cura el núcleo y notifica al listener
     * 
     * @param amount Cantidad de salud a restaurar
     */
    public void heal(float amount) {
        if (core != null && isEnabled() && !core.isDestroyed()) {
            core.heal(amount);
            
            if (listener != null) {
                listener.onCoreHealed(core.getCurrentHealth(), core.getMaxHealth());
            }
        }
    }
    
    /**
     * Obtiene el estado de salud actual
     * 
     * @return Array con [salud actual, salud máxima]
     */
    public float[] getHealthStatus() {
        if (core != null) {
            return new float[] { core.getCurrentHealth(), core.getMaxHealth() };
        }
        return new float[] { 0f, 0f };
    }
    
    /**
     * Obtiene el porcentaje de salud
     * 
     * @return Porcentaje de salud (0.0 - 1.0)
     */
    public float getHealthPercentage() {
        if (core != null) {
            return core.getHealthPercentage();
        }
        return 0f;
    }
    
    /**
     * Verifica si el núcleo está destruido
     * 
     * @return true si está destruido, false en caso contrario
     */
    public boolean isCoreDestroyed() {
        return core != null && core.isDestroyed();
    }
    
    /**
     * Reinicia el núcleo a su estado inicial
     */
    public void reset() {
        if (core != null) {
            core.reset();
            setEnabled(true);
        }
    }
    
    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
        // No se necesita renderizado especial
    }
}