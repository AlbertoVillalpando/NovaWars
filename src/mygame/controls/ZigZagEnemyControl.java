package mygame.controls;

import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import mygame.entities.Enemy;

/**
 * Control para enemigos con movimiento zig-zag.
 * Los enemigos se mueven hacia el núcleo pero con desviación horizontal
 * sinusoidal que crea un patrón de zig-zag.
 * 
 * @author Alberto Villalpando
 */
public class ZigZagEnemyControl extends EnemyControl {
    
    private float zigZagAmplitude;
    private float zigZagFrequency;
    private float timeAccumulator = 0f;
    
    // Dirección base hacia el núcleo (calculada al inicio)
    private Vector3f baseDirection = new Vector3f();
    private Vector3f perpendicular = new Vector3f();
    private Vector3f startPosition = new Vector3f();
    
    /**
     * Constructor del control zig-zag
     * 
     * @param enemy Referencia al enemigo
     * @param zigZagAmplitude Amplitud del movimiento horizontal
     * @param zigZagFrequency Frecuencia del zig-zag
     */
    public ZigZagEnemyControl(Enemy enemy, float zigZagAmplitude, float zigZagFrequency) {
        super(enemy);
        this.zigZagAmplitude = zigZagAmplitude;
        this.zigZagFrequency = zigZagFrequency;
        
        System.out.println("ZigZagEnemyControl creado - Amplitud: " + zigZagAmplitude + ", Frecuencia: " + zigZagFrequency);
    }
    
    @Override
    protected void updateMovement(float tpf) {
        timeAccumulator += tpf;
        
        // Debug cada 2 segundos para verificar que se está ejecutando
        if ((int)(timeAccumulator) % 2 == 0 && (int)(timeAccumulator - tpf) % 2 != 0) {
            System.out.println("ZigZagEnemy actualizando movimiento - Tiempo: " + timeAccumulator);
        }
        
        // Actualizar dirección base si es necesario
        updateBaseDirection();
        
        // Calcular desplazamiento horizontal sinusoidal
        float zigZagOffset = FastMath.sin(timeAccumulator * zigZagFrequency) * zigZagAmplitude;
        
        // Calcular dirección final combinando movimiento hacia núcleo y zig-zag
        Vector3f finalDirection = new Vector3f();
        finalDirection.addLocal(baseDirection); // Movimiento principal hacia núcleo
        finalDirection.addLocal(perpendicular.mult(zigZagOffset * 0.3f)); // Componente zig-zag
        
        finalDirection.normalizeLocal();
        
        // Aplicar movimiento
        moveInDirection(finalDirection, enemy.getSpeed(), tpf);
    }
    
    /**
     * Actualiza la dirección base hacia el núcleo y calcula la perpendicular
     */
    private void updateBaseDirection() {
        Vector3f currentPos = spatial.getLocalTranslation();
        
        // Dirección principal hacia el núcleo
        baseDirection = corePosition.subtract(currentPos);
        baseDirection.y = 0; // Mantener en plano horizontal
        
        if (baseDirection.length() > 0.1f) {
            baseDirection.normalizeLocal();
            
            // Calcular vector perpendicular para el movimiento zig-zag
            // Rotar 90 grados en el plano XZ
            perpendicular.set(-baseDirection.z, 0, baseDirection.x);
            perpendicular.normalizeLocal();
        }
    }
    
    /**
     * Reinicia el zig-zag para reutilización del enemigo
     */
    public void resetZigZag() {
        timeAccumulator = 0f;
        
        // Guardar posición inicial para referencia
        if (spatial != null) {
            startPosition.set(spatial.getLocalTranslation());
        }
        
        // Recalcular dirección base
        updateBaseDirection();
    }
    
    @Override
    public void reset() {
        super.reset();
        resetZigZag();
    }
    
    /**
     * Establece la posición del núcleo y recalcula direcciones
     */
    @Override
    public void setCorePosition(Vector3f corePosition) {
        super.setCorePosition(corePosition);
        updateBaseDirection();
    }
} 