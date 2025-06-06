package mygame.controls;

import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import mygame.entities.Enemy;

/**
 * Control de movimiento para enemigos con patrón de zig-zag evasivo.
 * 
 * <p>ZigZagEnemyControl implementa un algoritmo de movimiento que combina
 * desplazamiento directo hacia el núcleo con oscilación horizontal sinusoidal,
 * creando un patrón evasivo que dificulta el targeting del jugador.</p>
 * 
 * <h3>Algoritmo de movimiento:</h3>
 * <ul>
 *   <li><strong>Componente principal:</strong> Vector directo hacia el núcleo central</li>
 *   <li><strong>Componente evasivo:</strong> Oscilación sinusoidal perpendicular</li>
 *   <li><strong>Resultado:</strong> Trayectoria en zig-zag hacia el objetivo</li>
 * </ul>
 * 
 * <h3>Parámetros configurables:</h3>
 * <ul>
 *   <li><strong>Amplitud:</strong> Intensidad de la desviación horizontal</li>
 *   <li><strong>Frecuencia:</strong> Velocidad de cambio de dirección</li>
 *   <li><strong>Velocidad base:</strong> Heredada del enemigo base</li>
 * </ul>
 * 
 * <h3>Características técnicas:</h3>
 * <ul>
 *   <li><strong>Recálculo continuo:</strong> Dirección base actualizada cada frame</li>
 *   <li><strong>Navegación 2D:</strong> Movimiento confinado al plano horizontal (Y=0)</li>
 *   <li><strong>Vector perpendicular:</strong> Calculado dinámicamente para zig-zag</li>
 *   <li><strong>Normalización:</strong> Velocidad constante independiente de dirección</li>
 * </ul>
 * 
 * @author Alberto Villalpando
 * @version 1.0
 * @see EnemyControl
 * @see ZigZagEnemy
 * @since 2024
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
    
    /**
     * Actualiza el movimiento zig-zag del enemigo cada frame.
     * 
     * <p>Este método implementa el núcleo del algoritmo de movimiento evasivo,
     * combinando progreso hacia el núcleo con oscilación perpendicular para
     * crear un patrón de zig-zag impredecible.</p>
     * 
     * <h3>Proceso de cálculo por frame:</h3>
     * <ol>
     *   <li><strong>Acumulación temporal:</strong> Incrementa el tiempo para funciones sinusoidales</li>
     *   <li><strong>Recálculo direccional:</strong> Actualiza vector base hacia núcleo</li>
     *   <li><strong>Oscilación sinusoidal:</strong> Calcula desplazamiento horizontal</li>
     *   <li><strong>Combinación vectorial:</strong> Fusiona componente principal y evasivo</li>
     *   <li><strong>Normalización:</strong> Asegura velocidad constante</li>
     *   <li><strong>Aplicación:</strong> Ejecuta movimiento resultante</li>
     * </ol>
     * 
     * <h3>Fórmula matemática:</h3>
     * <pre>
     * finalDirection = baseDirection + (perpendicular * sin(time * frequency) * amplitude * 0.3)
     * zigZagOffset = sin(timeAccumulator * zigZagFrequency) * zigZagAmplitude
     * </pre>
     * 
     * <h3>Parámetros de ajuste:</h3>
     * <ul>
     *   <li><strong>Factor 0.3f:</strong> Reduce intensidad del zig-zag para evitar movimiento errático</li>
     *   <li><strong>Normalización:</strong> Mantiene velocidad uniforme en curvas</li>
     *   <li><strong>Recálculo continuo:</strong> Adapta a posición cambiante del enemigo</li>
     * </ul>
     * 
     * @param tpf Time per frame - Delta de tiempo desde la última actualización
     */
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
     * Recalcula la dirección base hacia el núcleo y el vector perpendicular para zig-zag.
     * 
     * <p>Este método es fundamental para mantener la precisión del movimiento evasivo,
     * ya que actualiza continuamente la referencia direccional conforme el enemigo
     * se desplaza por el campo de batalla.</p>
     * 
     * <h3>Proceso de cálculo direccional:</h3>
     * <ol>
     *   <li><strong>Vector base:</strong> Calcula dirección actual → núcleo central</li>
     *   <li><strong>Proyección 2D:</strong> Elimina componente Y para movimiento horizontal</li>
     *   <li><strong>Normalización:</strong> Convierte a vector unitario para velocidad uniforme</li>
     *   <li><strong>Vector perpendicular:</strong> Calcula dirección 90° para oscilación</li>
     * </ol>
     * 
     * <h3>Matemática del vector perpendicular:</h3>
     * <pre>
     * Si baseDirection = (x, 0, z), entonces:
     * perpendicular = (-z, 0, x)
     * 
     * Esto rota el vector base 90° en el plano horizontal,
     * creando la dirección de oscilación zig-zag.
     * </pre>
     * 
     * <h3>Validaciones:</h3>
     * <ul>
     *   <li><strong>Distancia mínima:</strong> Solo calcula si distance > 0.1f</li>
     *   <li><strong>Prevención de división por cero:</strong> Evita normalización de vector nulo</li>
     *   <li><strong>Estabilidad numérica:</strong> Mantiene precisión en distancias cortas</li>
     * </ul>
     * 
     * <h3>Llamadas automáticas:</h3>
     * <ul>
     *   <li><strong>updateMovement():</strong> Cada frame durante movimiento activo</li>
     *   <li><strong>setCorePosition():</strong> Cuando cambia la posición del núcleo</li>
     *   <li><strong>resetZigZag():</strong> Durante reinicio de enemigo reciclado</li>
     * </ul>
     */
    private void updateBaseDirection() {
        Vector3f currentPos = spatial.getLocalTranslation();
        
        // Dirección principal hacia el núcleo
        baseDirection = corePosition.subtract(currentPos);
        baseDirection.y = 0; // Mantener en plano horizontal
        
        if (baseDirection.length() > 0.1f) {
            baseDirection.normalizeLocal();
            
            // Calcular vector perpendicular para el movimiento zig-zag
            perpendicular.set(-baseDirection.z, 0, baseDirection.x);
            perpendicular.normalizeLocal();
        }
    }
    
    /**
     * Reinicia completamente el estado del patrón zig-zag para reutilización del enemigo.
     * 
     * <p>Este método es crítico para el sistema de object pooling, permitiendo
     * que enemigos reciclados comiencen con un estado de movimiento limpio
     * desde cualquier posición de spawn.</p>
     * 
     * <h3>Proceso de reinicio:</h3>
     * <ol>
     *   <li><strong>Reset temporal:</strong> timeAccumulator = 0 (reinicia ciclo sinusoidal)</li>
     *   <li><strong>Captura de spawn:</strong> Guarda posición inicial para referencia</li>
     *   <li><strong>Recálculo direccional:</strong> Actualiza vectores base y perpendicular</li>
     * </ol>
     * 
     * <h3>Importancia del reset temporal:</h3>
     * <ul>
     *   <li><strong>Consistencia:</strong> Todos los enemigos inician en misma fase sinusoidal</li>
     *   <li><strong>Predictibilidad:</strong> Evita patrones aleatorios por tiempo acumulado</li>
     *   <li><strong>Sincronización:</strong> Permite coordinación de oleadas de enemigos</li>
     * </ul>
     * 
     * <h3>Uso típico:</h3>
     * <ul>
     *   <li><strong>EnemyManager:</strong> Al reciclar enemigo de pool</li>
     *   <li><strong>Enemy.reset():</strong> Durante reinicio general de entidad</li>
     *   <li><strong>GameState:</strong> Al iniciar nueva oleada o nivel</li>
     * </ul>
     * 
     * @see Enemy#reset(Vector3f)
     * @see EnemyManager
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