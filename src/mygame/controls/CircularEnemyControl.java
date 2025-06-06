package mygame.controls;

import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import mygame.entities.Enemy;

/**
 * Control de movimiento orbital avanzado para enemigos con comportamiento circular.
 * 
 * <p>CircularEnemyControl implementa un sistema de navegación de dos fases
 * que simula un comportamiento táctico inteligente: patrullaje orbital
 * seguido de ataques directos calculados.</p>
 * 
 * <h3>Fases del comportamiento orbital:</h3>
 * <ul>
 *   <li><strong>Fase 1 - Patrullaje:</strong> Movimiento circular estable a distancia segura</li>
 *   <li><strong>Fase 2 - Asalto:</strong> Acercamiento directo y agresivo al núcleo</li>
 *   <li><strong>Transición:</strong> Retorno a órbita después de ataque fallido</li>
 * </ul>
 * 
 * <h3>Algoritmo de órbita circular:</h3>
 * <ul>
 *   <li><strong>Posicionamiento:</strong> Cálculo trigonométrico de puntos orbitales</li>
 *   <li><strong>Trayectoria:</strong> Interpolación suave hacia posición objetivo</li>
 *   <li><strong>Rotación:</strong> Incremento angular constante para movimiento circular</li>
 * </ul>
 * 
 * <h3>Sistema de timing táctico:</h3>
 * <ul>
 *   <li><strong>orbitTime:</strong> Duración de patrullaje antes de atacar (8s)</li>
 *   <li><strong>coreApproachDuration:</strong> Tiempo máximo de asalto (3s)</li>
 *   <li><strong>Velocidad adaptativa:</strong> 1.5x más rápido durante ataques</li>
 * </ul>
 * 
 * <h3>Características técnicas:</h3>
 * <ul>
 *   <li><strong>Inicialización dinámica:</strong> Ángulo inicial basado en spawn position</li>
 *   <li><strong>Detección de proximidad:</strong> Activación automática de daño al núcleo</li>
 *   <li><strong>Navegación 2D:</strong> Movimiento confinado al plano horizontal</li>
 *   <li><strong>Estado persistente:</strong> Mantiene progreso orbital entre ciclos</li>
 * </ul>
 * 
 * @author Alberto Villalpando
 * @version 1.0
 * @see EnemyControl
 * @see CircularEnemy
 * @since 2024
 */
public class CircularEnemyControl extends EnemyControl {
    
    private float orbitRadius;
    private float orbitSpeed;
    private float currentAngle = 0f;
    
    // Estado del movimiento
    private boolean movingTowardCore = false;
    private float coreApproachTimer = 0f;
    private float coreApproachDuration = 3f; // Segundos acercándose al núcleo
    private float orbitTime = 8f; // Segundos en órbita antes de acercarse
    private float currentOrbitTime = 0f;
    
    /**
     * Constructor del control circular
     * 
     * @param enemy Referencia al enemigo
     * @param orbitRadius Radio de la órbita circular
     * @param orbitSpeed Velocidad de rotación en la órbita
     */
    public CircularEnemyControl(Enemy enemy, float orbitRadius, float orbitSpeed) {
        super(enemy);
        this.orbitRadius = orbitRadius;
        this.orbitSpeed = orbitSpeed;
        
        // El ángulo inicial se calculará en el primer update cuando spatial esté disponible
        this.currentAngle = 0f;
        
        System.out.println("CircularEnemyControl creado - Radio: " + orbitRadius + ", Velocidad: " + orbitSpeed);
    }
    
    /**
     * Método principal de actualización que coordina las fases de movimiento orbital.
     * 
     * <p>Funciona como dispatcher central que selecciona el algoritmo de movimiento
     * apropiado basado en el estado táctico actual del enemigo.</p>
     * 
     * <h3>Flujo de control por estado:</h3>
     * <pre>
     * if (movingTowardCore) {
     *     → updateCoreApproach() // Fase de asalto directo
     * } else {
     *     → updateCircularOrbit() // Fase de patrullaje orbital
     * }
     * </pre>
     * 
     * <h3>Inicialización dinámica del ángulo:</h3>
     * <ul>
     *   <li><strong>Primera ejecución:</strong> Calcula ángulo inicial desde spawn position</li>
     *   <li><strong>Fórmula:</strong> atan2(z, x) convierte coordenadas cartesianas a polares</li>
     *   <li><strong>Ventaja:</strong> Permite spawn en cualquier posición de la órbita</li>
     * </ul>
     * 
     * <h3>Machine State Pattern:</h3>
     * <ul>
     *   <li><strong>Estado inicial:</strong> Patrullaje orbital (movingTowardCore = false)</li>
     *   <li><strong>Transición 1:</strong> Patrullaje → Asalto (timer alcanza orbitTime)</li>
     *   <li><strong>Transición 2:</strong> Asalto → Patrullaje (timer alcanza approachDuration)</li>
     *   <li><strong>Transición 3:</strong> Asalto → Destrucción (impacto con núcleo)</li>
     * </ul>
     * 
     * @param tpf Time per frame - Delta temporal para cálculos de movimiento
     */
    @Override
    protected void updateMovement(float tpf) {
        // Inicializar ángulo si es la primera vez y spatial está disponible
        if (currentAngle == 0f && spatial != null) {
            Vector3f spawnPos = spatial.getLocalTranslation();
            this.currentAngle = FastMath.atan2(spawnPos.z, spawnPos.x);
            System.out.println("CircularEnemy inicializado en ángulo: " + currentAngle);
        }
        
        if (movingTowardCore) {
            updateCoreApproach(tpf);
        } else {
            updateCircularOrbit(tpf);
        }
    }
    
    /**
     * Ejecuta el algoritmo de movimiento circular orbital durante la fase de patrullaje.
     * 
     * <p>Implementa navegación trigonométrica para mantener órbita estable
     * alrededor del núcleo central, combinando cálculo de posición objetivo
     * con interpolación suave de movimiento.</p>
     * 
     * <h3>Algoritmo de órbita circular:</h3>
     * <ol>
     *   <li><strong>Incremento angular:</strong> currentAngle += orbitSpeed * tpf</li>
     *   <li><strong>Normalización:</strong> Mantiene ángulo en rango [0, 2π]</li>
     *   <li><strong>Conversión polar → cartesiana:</strong> (r, θ) → (x, z)</li>
     *   <li><strong>Interpolación:</strong> Movimiento suave hacia posición calculada</li>
     * </ol>
     * 
     * <h3>Fórmulas matemáticas:</h3>
     * <pre>
     * x = cos(θ) * radio    // Componente horizontal X
     * z = sin(θ) * radio    // Componente horizontal Z
     * y = 0                // Movimiento confinado al plano
     * 
     * dirección = posiciónObjetivo - posiciónActual
     * </pre>
     * 
     * <h3>Sistema de navegación híbrido:</h3>
     * <ul>
     *   <li><strong>Lejos de órbita:</strong> Movimiento direccional interpolado</li>
     *   <li><strong>En órbita:</strong> Posicionamiento directo para precisión</li>
     *   <li><strong>Umbral:</strong> 0.1f unidades para cambio de modo</li>
     * </ul>
     * 
     * <h3>Gestión de timing táctico:</h3>
     * <ul>
     *   <li><strong>currentOrbitTime:</strong> Acumula tiempo en patrullaje</li>
     *   <li><strong>Trigger de asalto:</strong> Transición cuando time >= orbitTime</li>
     *   <li><strong>Predictibilidad:</strong> 8 segundos de patrullaje garantizados</li>
     * </ul>
     * 
     * @param tpf Time per frame - Delta temporal para cálculos de incremento angular
     */
    private void updateCircularOrbit(float tpf) {
        currentOrbitTime += tpf;
        
        // Actualizar ángulo de órbita
        currentAngle += orbitSpeed * tpf;
        if (currentAngle > FastMath.TWO_PI) {
            currentAngle -= FastMath.TWO_PI;
        }
        
        // Calcular nueva posición en la órbita
        float x = FastMath.cos(currentAngle) * orbitRadius;
        float z = FastMath.sin(currentAngle) * orbitRadius;
        Vector3f orbitPosition = new Vector3f(x, 0, z);
        
        // Mover hacia la posición de órbita
        Vector3f currentPos = spatial.getLocalTranslation();
        Vector3f direction = orbitPosition.subtract(currentPos);
        direction.y = 0; // Mantener en plano horizontal
        
        if (direction.length() > 0.1f) {
            direction.normalizeLocal();
            moveInDirection(direction, enemy.getSpeed(), tpf);
        } else {
            // Ya estamos en la órbita, solo rotar
            spatial.setLocalTranslation(orbitPosition);
        }
        
        // Verificar si es hora de acercarse al núcleo
        if (currentOrbitTime >= orbitTime) {
            startCoreApproach();
        }
    }
    
    /**
     * Ejecuta la fase de asalto directo al núcleo con detección de impacto.
     * 
     * <p>Durante esta fase crítica, el enemigo abandona la órbita segura
     * y se lanza en ataque directo, incrementando velocidad y evaluando
     * continuamente la proximidad para activar daño al núcleo.</p>
     * 
     * <h3>Mecánicas de asalto:</h3>
     * <ol>
     *   <li><strong>Aceleración:</strong> Velocidad x1.5 para ataque decisivo</li>
     *   <li><strong>Navegación directa:</strong> Vector recto hacia núcleo sin desviaciones</li>
     *   <li><strong>Timer de asalto:</strong> Limite de 3 segundos para evitar bucles</li>
     *   <li><strong>Detección de impacto:</strong> Evaluación continua de proximidad</li>
     * </ol>
     * 
     * <h3>Condiciones de terminación:</h3>
     * <pre>
     * 1. TIMEOUT: coreApproachTimer >= coreApproachDuration
     *    → returnToOrbit() // Regresa a patrullaje
     * 
     * 2. IMPACTO: distanceToCore <= (coreRadius + enemySize)
     *    → enemy.reachCore() + setEnabled(false) // Daño y destrucción
     * </pre>
     * 
     * <h3>Gestión de estado:</h3>
     * <ul>
     *   <li><strong>Timer de asalto:</strong> Previene ataques eternos</li>
     *   <li><strong>Retorno automático:</strong> Garantiza regreso a patrullaje</li>
     *   <li><strong>Detección precisa:</strong> Cálculo de colisión con radios</li>
     * </ul>
     * 
     * <h3>Optimizaciones de rendimiento:</h3>
     * <ul>
     *   <li><strong>getDirectionToCore():</strong> Heredado de EnemyControl</li>
     *   <li><strong>getDistanceToCore():</strong> Cálculo optimizado de distancia</li>
     *   <li><strong>setEnabled(false):</strong> Desactiva control al completar misión</li>
     * </ul>
     * 
     * @param tpf Time per frame - Delta temporal para timer y movimiento
     */
    private void updateCoreApproach(float tpf) {
        coreApproachTimer += tpf;
        
        // Moverse directamente hacia el núcleo
        Vector3f directionToCore = getDirectionToCore();
        moveInDirection(directionToCore, enemy.getSpeed() * 1.5f, tpf); // Más rápido cuando se acerca
        
        // Verificar si debe volver a la órbita
        if (coreApproachTimer >= coreApproachDuration) {
            returnToOrbit();
        }
        
        // Si está muy cerca del núcleo, forzar impacto
        float distanceToCore = getDistanceToCore();
        if (distanceToCore <= coreRadius + enemy.getSize()) {
            enemy.reachCore();
            setEnabled(false);
        }
    }
    
    /**
     * Inicia la transición desde patrullaje orbital hacia fase de asalto al núcleo.
     * 
     * <p>Este método ejecuta el cambio de estado crítico que transforma
     * al enemigo de observador pasivo a atacante activo, reseteando
     * todos los timers relevantes para asegurar comportamiento consistente.</p>
     * 
     * <h3>Cambios de estado ejecutados:</h3>
     * <ul>
     *   <li><strong>movingTowardCore = true:</strong> Activa modo de asalto</li>
     *   <li><strong>coreApproachTimer = 0:</strong> Reinicia contador de ataque</li>
     *   <li><strong>currentOrbitTime = 0:</strong> Prepara próximo ciclo orbital</li>
     * </ul>
     * 
     * <h3>Efecto en updateMovement():</h3>
     * <pre>
     * Antes: updateMovement() → updateCircularOrbit()
     * Después: updateMovement() → updateCoreApproach()
     * </pre>
     * 
     * <h3>Timing del trigger:</h3>
     * <ul>
     *   <li><strong>Llamada desde:</strong> updateCircularOrbit()</li>
     *   <li><strong>Condición:</strong> currentOrbitTime >= orbitTime (8 segundos)</li>
     *   <li><strong>Frecuencia:</strong> Una vez por ciclo de patrullaje</li>
     * </ul>
     * 
     * <h3>Debugging y monitoreo:</h3>
     * <ul>
     *   <li><strong>Log output:</strong> Confirma transición para debugging</li>
     *   <li><strong>Utilidad:</strong> Tracking de comportamiento enemy AI</li>
     * </ul>
     */
    private void startCoreApproach() {
        movingTowardCore = true;
        coreApproachTimer = 0f;
        currentOrbitTime = 0f;
        
        System.out.println("CircularEnemy iniciando acercamiento al núcleo");
    }
    
    /**
     * Ejecuta transición de regreso desde asalto fallido hacia patrullaje orbital.
     * 
     * <p>Este método maneja la recuperación del enemigo cuando un ataque
     * al núcleo no resulta en impacto dentro del tiempo límite,
     * reestableciendo comportamiento orbital desde posición actual.</p>
     * 
     * <h3>Proceso de recuperación:</h3>
     * <ol>
     *   <li><strong>Cambio de estado:</strong> movingTowardCore = false</li>
     *   <li><strong>Reset de timers:</strong> Limpia contadores de ambas fases</li>
     *   <li><strong>Recálculo angular:</strong> Sincroniza ángulo con posición actual</li>
     *   <li><strong>Reanudación orbital:</strong> Retorna a updateCircularOrbit()</li>
     * </ol>
     * 
     * <h3>Recálculo de ángulo crítico:</h3>
     * <pre>
     * currentAngle = atan2(currentPos.z, currentPos.x)
     * 
     * Problema: Enemigo puede estar en posición no-orbital
     * Solución: Calcula ángulo más cercano en órbita
     * Resultado: Reintegración suave sin saltos visuales
     * </pre>
     * 
     * <h3>Casos de activación:</h3>
     * <ul>
     *   <li><strong>Timeout de asalto:</strong> coreApproachTimer >= 3s</li>
     *   <li><strong>Asalto fallido:</strong> No impactó núcleo en tiempo límite</li>
     *   <li><strong>Recuperación automática:</strong> Parte del diseño táctico</li>
     * </ul>
     * 
     * <h3>Ventajas del sistema:</h3>
     * <ul>
     *   <li><strong>Ciclos repetibles:</strong> Enemigo permanece amenaza activa</li>
     *   <li><strong>Variación táctica:</strong> Alterna entre defensa y ataque</li>
     *   <li><strong>Continuidad visual:</strong> Sin teleporting o glitches</li>
     * </ul>
     */
    private void returnToOrbit() {
        movingTowardCore = false;
        coreApproachTimer = 0f;
        currentOrbitTime = 0f;
        
        // Recalcular ángulo basado en posición actual
        Vector3f currentPos = spatial.getLocalTranslation();
        currentAngle = FastMath.atan2(currentPos.z, currentPos.x);
        
        System.out.println("CircularEnemy retornando a órbita");
    }
    
    /**
     * Reinicio completo del sistema orbital para reutilización de enemigo reciclado.
     * 
     * <p>Método crítico del object pooling que garantiza que enemigos
     * reutilizados comiencen con estado orbital limpio y consistente,
     * independientemente de su estado previo de destrucción.</p>
     * 
     * <h3>Proceso de reinicio orbital:</h3>
     * <ol>
     *   <li><strong>Estado táctico:</strong> Fuerza modo patrullaje (no asalto)</li>
     *   <li><strong>Limpieza temporal:</strong> Resetea todos los timers acumulados</li>
     *   <li><strong>Recalibración angular:</strong> Sincroniza con nueva spawn position</li>
     * </ol>
     * 
     * <h3>Diferencias con returnToOrbit():</h3>
     * <pre>
     * returnToOrbit():  Transición durante gameplay activo
     * resetOrbit():     Inicialización para enemigo reciclado
     * 
     * returnToOrbit():  Mantiene progreso de timers
     * resetOrbit():     Limpia completamente todos los timers
     * </pre>
     * 
     * <h3>Integración con pooling:</h3>
     * <ul>
     *   <li><strong>EnemyManager.getEnemy():</strong> Llama reset() antes de reutilizar</li>
     *   <li><strong>CircularEnemy.reset():</strong> Propaga llamada a resetOrbit()</li>
     *   <li><strong>Control.reset():</strong> Asegura estado limpio del control</li>
     * </ul>
     * 
     * <h3>Ventajas del reset completo:</h3>
     * <ul>
     *   <li><strong>Comportamiento predecible:</strong> Todos los enemigos inician igual</li>
     *   <li><strong>Sin residuos:</strong> Elimina estado de instancia anterior</li>
     *   <li><strong>Facilita debugging:</strong> Comportamiento consistente y reproducible</li>
     * </ul>
     * 
     * @see Enemy#reset(Vector3f)
     * @see EnemyManager
     * @see CircularEnemy#reset(Vector3f)
     */
    public void resetOrbit() {
        movingTowardCore = false;
        coreApproachTimer = 0f;
        currentOrbitTime = 0f;
        
        // Calcular ángulo inicial basado en posición actual
        if (spatial != null) {
            Vector3f spawnPos = spatial.getLocalTranslation();
            this.currentAngle = FastMath.atan2(spawnPos.z, spawnPos.x);
        }
    }
    
    @Override
    public void reset() {
        super.reset();
        resetOrbit();
    }
} 