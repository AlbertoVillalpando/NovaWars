package mygame.controls;

import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import mygame.entities.Enemy;

/**
 * Control para enemigos con movimiento circular.
 * Los enemigos se mueven en órbitas circulares alrededor del área de juego,
 * manteniéndose a una distancia específica del centro.
 * 
 * @author Alberto Villalpando
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
     * Actualiza el movimiento circular en órbita
     * 
     * @param tpf Time per frame
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
     * Actualiza el movimiento de acercamiento al núcleo
     * 
     * @param tpf Time per frame
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
     * Inicia el movimiento de acercamiento al núcleo
     */
    private void startCoreApproach() {
        movingTowardCore = true;
        coreApproachTimer = 0f;
        currentOrbitTime = 0f;
        
        System.out.println("CircularEnemy iniciando acercamiento al núcleo");
    }
    
    /**
     * Retorna a la órbita circular
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
     * Reinicia la órbita para reutilización del enemigo
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