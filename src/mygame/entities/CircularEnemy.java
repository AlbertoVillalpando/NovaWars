package mygame.entities;

import com.jme3.asset.AssetManager;
import com.jme3.math.ColorRGBA;
import mygame.config.GameConfig;
import mygame.controls.CircularEnemyControl;
import mygame.controls.VisualEffectsControl;
import mygame.utils.MaterialFactory;

/**
 * Enemigo que se mueve en patrones circulares alrededor del área de juego.
 * Se mantiene en una órbita circular mientras ocasionalmente se acerca al núcleo.
 * 
 * @author Alberto Villalpando
 */
public class CircularEnemy extends Enemy {
    
    private float orbitRadius;
    private float orbitSpeed;
    
    /**
     * Constructor del CircularEnemy
     * 
     * @param assetManager AssetManager para crear materiales
     * @param config Configuración del juego
     */
    public CircularEnemy(AssetManager assetManager, GameConfig config) {
        super(
            assetManager,
            50f, // Salud base
            config.getCircularEnemySpeed(),
            config.getEnemySize(),
            config.getEnemyCoreDamage()
        );
        
        this.orbitRadius = config.getCircularEnemyRadius();
        this.orbitSpeed = config.getCircularEnemyOrbitSpeed();
        
        System.out.println("CircularEnemy constructor - orbitRadius: " + this.orbitRadius + ", orbitSpeed: " + this.orbitSpeed);
        
        // Ahora crear el control con los valores correctamente inicializados
        createEnemyControl();
    }
    
    @Override
    protected void createEnemyGeometry(AssetManager assetManager) {
        // Crear forma orbital elegante - anillos y núcleo central
        createOrbitalGeometry(assetManager);
    }
    
    /**
     * Crea una geometría elegante que refleja el movimiento orbital del CircularEnemy
     */
    private void createOrbitalGeometry(AssetManager assetManager) {
        // Núcleo central - esfera más pequeña
        com.jme3.scene.shape.Sphere coreSphere = new com.jme3.scene.shape.Sphere(16, 16, size * 0.3f);
        enemyGeometry = new com.jme3.scene.Geometry("CircularEnemyCore", coreSphere);
        
        // Material principal con efectos neón orbitales
        enemyMaterial = MaterialFactory.createNeonMaterial(assetManager, getEnemyColor(), 1.4f);
        enemyGeometry.setMaterial(enemyMaterial);
        this.attachChild(enemyGeometry);
        
        // Crear anillos orbitales
        createOrbitalRings(assetManager);
        
        // Crear puntos de energía orbitales
        createOrbitalPoints(assetManager);
        
        // Crear púas triangulares defensivas
        createDefensiveSpikes(assetManager);
    }
    
    /**
     * Crea anillos que representan las órbitas del enemigo
     */
    private void createOrbitalRings(AssetManager assetManager) {
        // Anillo principal horizontal
        com.jme3.scene.shape.Cylinder ring1 = new com.jme3.scene.shape.Cylinder(24, 24, size * 0.8f, 0.02f, size * 0.05f, true, false);
        com.jme3.scene.Geometry ring1Geom = new com.jme3.scene.Geometry("OrbitalRing1", ring1);
        
        com.jme3.material.Material ringMaterial = MaterialFactory.createTransparentNeonMaterial(
            assetManager, getEnemyColor().mult(0.7f), 0.8f, 1.0f);
        ring1Geom.setMaterial(ringMaterial);
        ring1Geom.setQueueBucket(com.jme3.renderer.queue.RenderQueue.Bucket.Transparent);
        
        // Rotar para que esté horizontal
        ring1Geom.rotate(com.jme3.math.FastMath.HALF_PI, 0, 0);
        this.attachChild(ring1Geom);
        
        // Anillo secundario inclinado
        com.jme3.scene.shape.Cylinder ring2 = new com.jme3.scene.shape.Cylinder(20, 20, size * 0.6f, 0.015f, size * 0.03f, true, false);
        com.jme3.scene.Geometry ring2Geom = new com.jme3.scene.Geometry("OrbitalRing2", ring2);
        ring2Geom.setMaterial(ringMaterial);
        ring2Geom.setQueueBucket(com.jme3.renderer.queue.RenderQueue.Bucket.Transparent);
        
        // Rotar e inclinar
        ring2Geom.rotate(com.jme3.math.FastMath.HALF_PI, 0, com.jme3.math.FastMath.QUARTER_PI);
        this.attachChild(ring2Geom);
    }
    
    /**
     * Crea pequeños puntos de energía que orbitan alrededor del núcleo
     */
    private void createOrbitalPoints(AssetManager assetManager) {
        int numPoints = 4;
        for (int i = 0; i < numPoints; i++) {
            com.jme3.scene.shape.Sphere point = new com.jme3.scene.shape.Sphere(8, 8, size * 0.08f);
            com.jme3.scene.Geometry pointGeom = new com.jme3.scene.Geometry("OrbitalPoint" + i, point);
            
            com.jme3.material.Material pointMaterial = MaterialFactory.createNeonMaterial(
                assetManager, getEnemyColor().mult(1.5f), 2.0f);
            pointGeom.setMaterial(pointMaterial);
            
            // Posicionar en círculo alrededor del núcleo
            float angle = (float) (i * 2 * Math.PI / numPoints);
            float radius = size * 0.5f;
            float x = (float) (Math.cos(angle) * radius);
            float z = (float) (Math.sin(angle) * radius);
            pointGeom.setLocalTranslation(x, 0, z);
            
            this.attachChild(pointGeom);
        }
    }
    
    /**
     * Crea púas triangulares defensivas que sobresalen del núcleo
     */
    private void createDefensiveSpikes(AssetManager assetManager) {
        int numSpikes = 8; // 8 púas distribuidas uniformemente
        
        for (int i = 0; i < numSpikes; i++) {
            // Calcular ángulo para distribución uniforme
            float angle = (float) (i * 2 * Math.PI / numSpikes);
            
            // Crear púa triangular principal
            createTriangularSpike(assetManager, angle, i, size * 0.4f, false);
            
            // Crear púa secundaria más pequeña (entre las principales)
            if (i < numSpikes) {
                float secondaryAngle = angle + (float) (Math.PI / numSpikes);
                createTriangularSpike(assetManager, secondaryAngle, i + numSpikes, size * 0.25f, true);
            }
        }
    }
    
    /**
     * Crea una púa triangular individual
     * 
     * @param assetManager AssetManager para materiales
     * @param angle Ángulo de posicionamiento en radianes
     * @param index Índice único para naming
     * @param spikeLength Longitud de la púa
     * @param isSecondary Si es una púa secundaria (más pequeña)
     */
    private void createTriangularSpike(AssetManager assetManager, float angle, int index, 
                                     float spikeLength, boolean isSecondary) {
        
        // Crear la forma triangular usando una pirámide aplastada
        com.jme3.scene.shape.Box spikeBase = new com.jme3.scene.shape.Box(
            size * 0.08f, size * 0.02f, spikeLength * 0.5f);
        com.jme3.scene.Geometry spikeGeom = new com.jme3.scene.Geometry(
            "DefensiveSpike" + index, spikeBase);
        
        // Material de la púa con intensidad según tipo
        float intensity = isSecondary ? 1.2f : 1.5f;
        ColorRGBA spikeColor = getEnemyColor().mult(intensity);
        com.jme3.material.Material spikeMaterial = MaterialFactory.createNeonMaterial(
            assetManager, spikeColor, intensity + 0.5f);
        spikeGeom.setMaterial(spikeMaterial);
        
        // Posicionamiento radial desde el centro
        float radius = size * 0.45f; // Justo fuera del núcleo principal
        float x = (float) (Math.cos(angle) * radius);
        float z = (float) (Math.sin(angle) * radius);
        spikeGeom.setLocalTranslation(x, 0, z);
        
        // Rotar la púa para que apunte hacia afuera
        spikeGeom.rotate(0, angle, 0);
        
        this.attachChild(spikeGeom);
        
        // Crear punta más brillante para mayor definición
        createSpikeTip(assetManager, angle, index, spikeLength, isSecondary);
        
        // Crear efecto de energía en la base de la púa
        createSpikeEnergyBase(assetManager, angle, index, isSecondary);
    }
    
    /**
     * Crea la punta brillante de la púa triangular
     */
    private void createSpikeTip(AssetManager assetManager, float angle, int index, 
                               float spikeLength, boolean isSecondary) {
        
        // Punta como esfera pequeña muy brillante
        float tipSize = isSecondary ? size * 0.03f : size * 0.05f;
        com.jme3.scene.shape.Sphere tipSphere = new com.jme3.scene.shape.Sphere(8, 8, tipSize);
        com.jme3.scene.Geometry tipGeom = new com.jme3.scene.Geometry(
            "SpikeTip" + index, tipSphere);
        
        // Material súper brillante para la punta
        ColorRGBA tipColor = new ColorRGBA(1.0f, 1.0f, 0.8f, 1.0f); // Blanco energético
        com.jme3.material.Material tipMaterial = MaterialFactory.createNeonMaterial(
            assetManager, tipColor, 3.0f);
        tipGeom.setMaterial(tipMaterial);
        
        // Posicionar en la punta de la púa
        float totalRadius = size * 0.45f + spikeLength;
        float x = (float) (Math.cos(angle) * totalRadius);
        float z = (float) (Math.sin(angle) * totalRadius);
        tipGeom.setLocalTranslation(x, 0, z);
        
        this.attachChild(tipGeom);
    }
    
    /**
     * Crea un efecto de energía en la base de cada púa
     */
    private void createSpikeEnergyBase(AssetManager assetManager, float angle, int index, 
                                     boolean isSecondary) {
        
        // Anillo de energía en la base de la púa
        float ringRadius = isSecondary ? size * 0.06f : size * 0.08f;
        com.jme3.scene.shape.Cylinder energyRing = new com.jme3.scene.shape.Cylinder(
            12, 12, ringRadius, 0.01f, size * 0.015f, true, false);
        com.jme3.scene.Geometry ringGeom = new com.jme3.scene.Geometry(
            "SpikeEnergyBase" + index, energyRing);
        
        // Material translúcido con pulsación
        ColorRGBA ringColor = getEnemyColor().mult(0.8f);
        ringColor.a = 0.7f;
        com.jme3.material.Material ringMaterial = MaterialFactory.createTransparentNeonMaterial(
            assetManager, ringColor, 0.7f, 1.8f);
        ringGeom.setMaterial(ringMaterial);
        MaterialFactory.setupTransparency(ringGeom);
        
        // Posicionar en la base de la púa
        float baseRadius = size * 0.35f;
        float x = (float) (Math.cos(angle) * baseRadius);
        float z = (float) (Math.sin(angle) * baseRadius);
        ringGeom.setLocalTranslation(x, 0, z);
        
        // Rotar para que esté horizontal
        ringGeom.rotate(com.jme3.math.FastMath.HALF_PI, 0, 0);
        
        this.attachChild(ringGeom);
    }
    
    @Override
    protected void createEnemyControl() {
        enemyControl = new CircularEnemyControl(this, orbitRadius, orbitSpeed);
        this.addControl(enemyControl);
        
        // Añadir efectos visuales elegantes
        VisualEffectsControl visualEffects = new VisualEffectsControl();
        visualEffects.setupElegantPreset();
        this.addControl(visualEffects);
        
        // Añadir efectos especiales a las púas
        setupSpikeEffects();
    }
    
    @Override
    protected ColorRGBA getEnemyColor() {
        // Color azul orbital elegante con tinte cyan
        return new ColorRGBA(0.1f, 0.8f, 1.0f, 1.0f);
    }
    
    /**
     * Configura efectos visuales especiales para las púas defensivas
     */
    private void setupSpikeEffects() {
        int totalSpikes = 16; // 8 principales + 8 secundarias
        
        for (int i = 0; i < totalSpikes; i++) {
            // Buscar las púas por nombre
            com.jme3.scene.Spatial spikeSpatial = findSpatialByName("DefensiveSpike" + i);
            if (spikeSpatial instanceof com.jme3.scene.Geometry) {
                com.jme3.scene.Geometry spikeGeom = (com.jme3.scene.Geometry) spikeSpatial;
                VisualEffectsControl spikeEffects = new VisualEffectsControl();
                
                // Pulsación individual para cada púa con desfase
                float phaseOffset = i * 0.2f;
                spikeEffects.enablePulsing(2.0f + phaseOffset, 0.5f, 1.3f);
                
                // Ligera rotación para dar vida a las púas
                spikeEffects.enableRotation(0, 0, 0.3f + (i * 0.1f));
                
                spikeGeom.addControl(spikeEffects);
            }
            
            // Efectos para las puntas de las púas
            com.jme3.scene.Spatial tipSpatial = findSpatialByName("SpikeTip" + i);
            if (tipSpatial instanceof com.jme3.scene.Geometry) {
                com.jme3.scene.Geometry tipGeom = (com.jme3.scene.Geometry) tipSpatial;
                VisualEffectsControl tipEffects = new VisualEffectsControl();
                
                // Pulsación intensa para las puntas
                tipEffects.enablePulsing(4.0f + (i * 0.3f), 0.8f, 2.0f);
                
                // Escalado sutil para efecto de energía
                tipEffects.enableScaling(3.0f + (i * 0.2f), 0.1f);
                
                tipGeom.addControl(tipEffects);
            }
            
            // Efectos para las bases de energía
            com.jme3.scene.Spatial baseSpatial = findSpatialByName("SpikeEnergyBase" + i);
            if (baseSpatial instanceof com.jme3.scene.Geometry) {
                com.jme3.scene.Geometry baseGeom = (com.jme3.scene.Geometry) baseSpatial;
                VisualEffectsControl baseEffects = new VisualEffectsControl();
                
                // Rotación para los anillos de energía
                baseEffects.enableRotation(0, 1.5f + (i * 0.1f), 0);
                
                // Pulsación de transparencia
                baseEffects.enablePulsing(1.8f + (i * 0.15f), 0.4f, 1.0f);
                
                baseGeom.addControl(baseEffects);
            }
        }
    }
    
    /**
     * Busca un spatial hijo por nombre recursivamente
     */
    private com.jme3.scene.Spatial findSpatialByName(String name) {
        return findSpatialByNameRecursive(this, name);
    }
    
    /**
     * Búsqueda recursiva de spatials por nombre
     */
    private com.jme3.scene.Spatial findSpatialByNameRecursive(com.jme3.scene.Node parent, String name) {
        for (com.jme3.scene.Spatial child : parent.getChildren()) {
            if (child.getName() != null && child.getName().equals(name)) {
                return child;
            }
            if (child instanceof com.jme3.scene.Node) {
                com.jme3.scene.Spatial result = findSpatialByNameRecursive((com.jme3.scene.Node) child, name);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }
    
    /**
     * Obtiene el radio de órbita del enemigo
     * 
     * @return Radio de órbita
     */
    public float getOrbitRadius() {
        return orbitRadius;
    }
    
    /**
     * Obtiene la velocidad de órbita del enemigo
     * 
     * @return Velocidad de órbita
     */
    public float getOrbitSpeed() {
        return orbitSpeed;
    }
    
    @Override
    public void reset(com.jme3.math.Vector3f position) {
        super.reset(position);
        
        // Reiniciar parámetros específicos si es necesario
        if (enemyControl instanceof CircularEnemyControl) {
            CircularEnemyControl control = (CircularEnemyControl) enemyControl;
            control.resetOrbit();
        }
    }
} 