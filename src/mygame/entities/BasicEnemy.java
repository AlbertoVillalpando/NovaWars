package mygame.entities;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import mygame.config.GameConfig;
import mygame.controls.EnemyControl;
import mygame.controls.VisualEffectsControl;
import mygame.utils.MaterialFactory;

/**
 * Enemigo básico con movimiento directo - Implementación simple para testing.
 * 
 * <p>BasicEnemy es la implementación más simple de la clase Enemy, diseñada
 * para probar el sistema de enemigos y proporcionar una amenaza directa
 * sin patrones complejos de movimiento.</p>
 * 
 * <h3>Características específicas:</h3>
 * <ul>
 *   <li><strong>Movimiento:</strong> Línea recta directa hacia el núcleo</li>
 *   <li><strong>Color:</strong> Verde neón brillante para identificación</li>
 *   <li><strong>Salud:</strong> 40 puntos (2 disparos para destruir)</li>
 *   <li><strong>Comportamiento:</strong> Agresivo y predecible</li>
 * </ul>
 * 
 * <h3>Patrón de movimiento:</h3>
 * <ul>
 *   <li><strong>Algoritmo:</strong> Vector directo hacia posición del núcleo</li>
 *   <li><strong>Velocidad:</strong> Constante definida en GameConfig</li>
 *   <li><strong>Navegación:</strong> Sin evasión de obstáculos</li>
 *   <li><strong>Objetivo:</strong> Siempre el núcleo central (0,0,0)</li>
 * </ul>
 * 
 * <h3>Control implementado:</h3>
 * <ul>
 *   <li><strong>BasicEnemyControl:</strong> Clase interna que extiende EnemyControl</li>
 *   <li><strong>updateMovement():</strong> Cálculo de dirección hacia núcleo</li>
 *   <li><strong>Sin IA compleja:</strong> Comportamiento completamente predecible</li>
 * </ul>
 * 
 * <h3>Propósito en gameplay:</h3>
 * <ul>
 *   <li><strong>Oleadas iniciales:</strong> Enemigos de introducción</li>
 *   <li><strong>Relleno de oleadas:</strong> Amenaza constante entre enemigos complejos</li>
 *   <li><strong>Testing:</strong> Validación de sistemas de colisión y daño</li>
 *   <li><strong>Referencia:</strong> Base para implementar enemigos más complejos</li>
 * </ul>
 * 
 * @author Alberto Villalpando
 * @version 1.0
 * @see Enemy
 * @see EnemyControl
 * @see EnemyManager
 * @since 2024
 */
public class BasicEnemy extends Enemy {
    
    /**
     * Constructor del BasicEnemy
     * 
     * @param assetManager AssetManager para crear materiales
     * @param config Configuración del juego
     */
    public BasicEnemy(AssetManager assetManager, GameConfig config) {
        super(
            assetManager,
            40f, // Salud base
            config.getEnemySpeed(),
            config.getEnemySize(),
            config.getEnemyCoreDamage()
        );
        
        // BasicEnemy no tiene parámetros adicionales, crear control directamente
        createEnemyControl();
    }
    
    @Override
    protected void createEnemyGeometry(AssetManager assetManager) {
        // Crear forma agresiva - cubo puntiagudo con extensiones
        createAggressiveCubeGeometry(assetManager);
    }
    
    /**
     * Crea una geometría agresiva que refleja el comportamiento directo del BasicEnemy
     */
    private void createAggressiveCubeGeometry(AssetManager assetManager) {
        // Cuerpo principal - cubo central más pequeño
        Box mainBody = new Box(size * 0.4f, size * 0.3f, size * 0.4f);
        enemyGeometry = new Geometry("BasicEnemyBody", mainBody);
        
        // Material principal con efectos neón mejorados
        enemyMaterial = MaterialFactory.createNeonMaterial(assetManager, getEnemyColor(), 1.3f);
        enemyGeometry.setMaterial(enemyMaterial);
        this.attachChild(enemyGeometry);
        
        // Crear "pinchos" frontales que indican agresividad
        createFrontSpikes(assetManager);
        
        // Crear alas laterales pequeñas
        createSideWings(assetManager);
    }
    
    /**
     * Crea pinchos frontales que dan apariencia agresiva
     */
    private void createFrontSpikes(AssetManager assetManager) {
        // Pincho central frontal - más largo
        Box frontSpike = new Box(size * 0.1f, size * 0.1f, size * 0.6f);
        Geometry frontSpikeGeom = new Geometry("FrontSpike", frontSpike);
        
        Material spikeMaterial = MaterialFactory.createNeonMaterial(assetManager, getEnemyColor().mult(1.2f), 1.5f);
        frontSpikeGeom.setMaterial(spikeMaterial);
        
        // Posicionar hacia adelante
        frontSpikeGeom.setLocalTranslation(0, 0, size * 0.8f);
        this.attachChild(frontSpikeGeom);
        
        // Pinchos laterales frontales
        for (int i = -1; i <= 1; i += 2) {
            Box sideSpike = new Box(size * 0.08f, size * 0.08f, size * 0.4f);
            Geometry sideSpikeGeom = new Geometry("SideSpike" + i, sideSpike);
            sideSpikeGeom.setMaterial(spikeMaterial);
            sideSpikeGeom.setLocalTranslation(i * size * 0.3f, 0, size * 0.6f);
            this.attachChild(sideSpikeGeom);
        }
    }
    
    /**
     * Crea pequeñas alas laterales para dar más presencia visual
     */
    private void createSideWings(AssetManager assetManager) {
        for (int i = -1; i <= 1; i += 2) {
            Box wing = new Box(size * 0.2f, size * 0.05f, size * 0.3f);
            Geometry wingGeom = new Geometry("Wing" + i, wing);
            
            Material wingMaterial = MaterialFactory.createNeonMaterial(assetManager, getEnemyColor().mult(0.8f), 1.0f);
            wingGeom.setMaterial(wingMaterial);
            
            // Posicionar a los lados
            wingGeom.setLocalTranslation(i * size * 0.6f, 0, 0);
            this.attachChild(wingGeom);
        }
    }
    
    @Override
    protected void createEnemyControl() {
        // Crear un control básico que solo se mueva hacia el núcleo
        enemyControl = new BasicEnemyControl(this);
        this.addControl(enemyControl);
        
        // Añadir efectos visuales agresivos
        VisualEffectsControl visualEffects = new VisualEffectsControl();
        visualEffects.setupAggressivePreset();
        this.addControl(visualEffects);
    }
    
    @Override
    protected ColorRGBA getEnemyColor() {
        // Color verde agresivo con tinte rojo para mayor amenaza
        return new ColorRGBA(0.6f, 1.0f, 0.2f, 1.0f);
    }
    
    /**
     * Control básico que solo se mueve hacia el núcleo
     */
    private static class BasicEnemyControl extends EnemyControl {
        
        public BasicEnemyControl(Enemy enemy) {
            super(enemy);
        }
        
        @Override
        protected void updateMovement(float tpf) {
            // Simplemente moverse hacia el núcleo
            Vector3f directionToCore = getDirectionToCore();
            moveInDirection(directionToCore, enemy.getSpeed(), tpf);
        }
    }
} 