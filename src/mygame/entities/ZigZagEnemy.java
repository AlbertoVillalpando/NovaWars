package mygame.entities;

import com.jme3.asset.AssetManager;
import com.jme3.math.ColorRGBA;
import mygame.config.GameConfig;
import mygame.controls.ZigZagEnemyControl;
import mygame.controls.VisualEffectsControl;
import mygame.utils.MaterialFactory;

/**
 * Enemigo que se mueve en patrón zig-zag hacia el núcleo.
 * Se mueve directamente hacia el núcleo pero con movimiento horizontal
 * sinusoidal que crea un patrón de zig-zag.
 * 
 * @author Alberto Villalpando
 */
public class ZigZagEnemy extends Enemy {
    
    private float zigZagAmplitude;
    private float zigZagFrequency;
    
    /**
     * Constructor del ZigZagEnemy
     * 
     * @param assetManager AssetManager para crear materiales
     * @param config Configuración del juego
     */
    public ZigZagEnemy(AssetManager assetManager, GameConfig config) {
        super(
            assetManager,
            30f, // Salud base (menor que CircularEnemy, más agresivo)
            config.getZigZagEnemySpeed(),
            config.getEnemySize(),
            config.getEnemyCoreDamage()
        );
        
        this.zigZagAmplitude = config.getZigZagAmplitude();
        this.zigZagFrequency = config.getZigZagFrequency();
        
        System.out.println("ZigZagEnemy constructor - zigZagAmplitude: " + this.zigZagAmplitude + ", zigZagFrequency: " + this.zigZagFrequency);
        
        // Ahora crear el control con los valores correctamente inicializados
        createEnemyControl();
    }
    
    @Override
    protected void createEnemyGeometry(AssetManager assetManager) {
        // Crear forma evasiva y ágil - diamante con aletas
        createEvasiveGeometry(assetManager);
    }
    
    /**
     * Crea una geometría ágil que refleja el movimiento evasivo del ZigZagEnemy
     */
    private void createEvasiveGeometry(AssetManager assetManager) {
        // Cuerpo principal - forma de diamante (cubo rotado)
        com.jme3.scene.shape.Box diamondBody = new com.jme3.scene.shape.Box(size * 0.3f, size * 0.2f, size * 0.4f);
        enemyGeometry = new com.jme3.scene.Geometry("ZigZagEnemyBody", diamondBody);
        
        // Material principal con efectos neón evasivos
        enemyMaterial = MaterialFactory.createNeonMaterial(assetManager, getEnemyColor(), 1.5f);
        enemyGeometry.setMaterial(enemyMaterial);
        
        // Rotar para darle forma de diamante
        enemyGeometry.rotate(0, com.jme3.math.FastMath.QUARTER_PI, 0);
        this.attachChild(enemyGeometry);
        
        // Crear aletas laterales dinámicas
        createDynamicFins(assetManager);
        
        // Crear elementos de velocidad (estelas)
        createSpeedStreaks(assetManager);
        
        // Crear sensor frontal
        createFrontSensor(assetManager);
    }
    
    /**
     * Crea aletas laterales que sugieren movimiento rápido y evasivo
     */
    private void createDynamicFins(AssetManager assetManager) {
        for (int i = -1; i <= 1; i += 2) {
            // Aletas principales
            com.jme3.scene.shape.Box fin = new com.jme3.scene.shape.Box(size * 0.15f, size * 0.02f, size * 0.25f);
            com.jme3.scene.Geometry finGeom = new com.jme3.scene.Geometry("Fin" + i, fin);
            
            com.jme3.material.Material finMaterial = MaterialFactory.createTransparentNeonMaterial(
                assetManager, getEnemyColor().mult(0.8f), 0.9f, 1.2f);
            finGeom.setMaterial(finMaterial);
            finGeom.setQueueBucket(com.jme3.renderer.queue.RenderQueue.Bucket.Transparent);
            
            // Posicionar con ligera inclinación para sugerir movimiento
            finGeom.setLocalTranslation(i * size * 0.4f, 0, size * 0.1f);
            finGeom.rotate(0, 0, i * com.jme3.math.FastMath.QUARTER_PI * 0.3f);
            this.attachChild(finGeom);
            
            // Aletas secundarias más pequeñas
            com.jme3.scene.shape.Box secondaryFin = new com.jme3.scene.shape.Box(size * 0.08f, size * 0.01f, size * 0.15f);
            com.jme3.scene.Geometry secondaryFinGeom = new com.jme3.scene.Geometry("SecondaryFin" + i, secondaryFin);
            secondaryFinGeom.setMaterial(finMaterial);
            secondaryFinGeom.setQueueBucket(com.jme3.renderer.queue.RenderQueue.Bucket.Transparent);
            
            secondaryFinGeom.setLocalTranslation(i * size * 0.3f, size * 0.1f, -size * 0.1f);
            secondaryFinGeom.rotate(0, 0, i * com.jme3.math.FastMath.QUARTER_PI * 0.5f);
            this.attachChild(secondaryFinGeom);
        }
    }
    
    /**
     * Crea elementos que sugieren alta velocidad y movimiento
     */
    private void createSpeedStreaks(AssetManager assetManager) {
        // Estelas traseras que indican velocidad
        for (int i = 0; i < 3; i++) {
            com.jme3.scene.shape.Box streak = new com.jme3.scene.shape.Box(size * 0.03f, size * 0.02f, size * 0.2f);
            com.jme3.scene.Geometry streakGeom = new com.jme3.scene.Geometry("SpeedStreak" + i, streak);
            
            float fadeLevel = (float) i / 3f; // 0.0 a 0.66
            com.jme3.material.Material streakMaterial = MaterialFactory.createStreakMaterial(
                assetManager, getEnemyColor(), fadeLevel);
            streakGeom.setMaterial(streakMaterial);
            streakGeom.setQueueBucket(com.jme3.renderer.queue.RenderQueue.Bucket.Transparent);
            
            // Posicionar detrás del enemigo con separación
            float offsetX = (i - 1) * size * 0.15f;
            streakGeom.setLocalTranslation(offsetX, 0, -size * 0.4f - (i * size * 0.15f));
            this.attachChild(streakGeom);
        }
    }
    
    /**
     * Crea un sensor frontal que sugiere detección y evasión
     */
    private void createFrontSensor(AssetManager assetManager) {
        com.jme3.scene.shape.Sphere sensor = new com.jme3.scene.shape.Sphere(12, 12, size * 0.12f);
        com.jme3.scene.Geometry sensorGeom = new com.jme3.scene.Geometry("FrontSensor", sensor);
        
        ColorRGBA sensorColor = new ColorRGBA(1.0f, 1.0f, 0.2f, 1.0f); // Amarillo brillante
        com.jme3.material.Material sensorMaterial = MaterialFactory.createNeonMaterial(
            assetManager, sensorColor, 2.5f);
        sensorGeom.setMaterial(sensorMaterial);
        
        // Posicionar al frente
        sensorGeom.setLocalTranslation(0, 0, size * 0.6f);
        this.attachChild(sensorGeom);
    }
    
    @Override
    protected void createEnemyControl() {
        enemyControl = new ZigZagEnemyControl(this, zigZagAmplitude, zigZagFrequency);
        this.addControl(enemyControl);
        
        // Añadir efectos visuales evasivos
        VisualEffectsControl visualEffects = new VisualEffectsControl();
        visualEffects.setupEvasivePreset();
        this.addControl(visualEffects);
    }
    
    @Override
    protected ColorRGBA getEnemyColor() {
        // Color rojo intenso con tinte magenta para movimiento evasivo
        return new ColorRGBA(1.0f, 0.2f, 0.6f, 1.0f);
    }
    
    /**
     * Obtiene la amplitud del zig-zag
     * 
     * @return Amplitud del zig-zag
     */
    public float getZigZagAmplitude() {
        return zigZagAmplitude;
    }
    
    /**
     * Obtiene la frecuencia del zig-zag
     * 
     * @return Frecuencia del zig-zag
     */
    public float getZigZagFrequency() {
        return zigZagFrequency;
    }
    
    @Override
    public void reset(com.jme3.math.Vector3f position) {
        super.reset(position);
        
        // Reiniciar parámetros específicos si es necesario
        if (enemyControl instanceof ZigZagEnemyControl) {
            ZigZagEnemyControl control = (ZigZagEnemyControl) enemyControl;
            control.resetZigZag();
        }
    }
} 