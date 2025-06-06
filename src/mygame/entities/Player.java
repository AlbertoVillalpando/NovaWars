package mygame.entities;

import com.jme3.app.Application;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Sphere;
import com.jme3.scene.shape.Cylinder;
import mygame.controls.PlayerControl;
import mygame.controls.VisualEffectsControl;
import mygame.utils.MaterialFactory;

/**
 * Entidad principal controlable por el usuario en NovaWars.
 * 
 * <p>El Player representa la nave espacial que el usuario controla para defender
 * el núcleo central. Encapsula toda la representación visual, física y lógica
 * de movimiento del jugador en una entidad cohesiva.</p>
 * 
 * <h3>Características visuales:</h3>
 * <ul>
 *   <li><strong>Cuerpo principal:</strong> Cubo rectangular que simula una nave</li>
 *   <li><strong>Marcador frontal:</strong> Indicador visual de dirección</li>
 *   <li><strong>Color neón:</strong> Cyan brillante con efecto de brillo</li>
 *   <li><strong>Escala adaptiva:</strong> Tamaño configurable desde GameConfig</li>
 * </ul>
 * 
 * <h3>Componentes integrados:</h3>
 * <ul>
 *   <li><strong>Node:</strong> Contenedor para toda la geometría del jugador</li>
 *   <li><strong>Geometry:</strong> Representación visual de la nave</li>
 *   <li><strong>PlayerControl:</strong> Lógica de movimiento y comportamiento</li>
 *   <li><strong>Material:</strong> Shader neón con efectos de brillo</li>
 * </ul>
 * 
 * <h3>Funcionalidades principales:</h3>
 * <ul>
 *   <li><strong>Movimiento fluido:</strong> Respuesta directa a input WASD</li>
 *   <li><strong>Límites de área:</strong> Confinado al área de juego</li>
 *   <li><strong>Orientación visual:</strong> Marcador que indica dirección frontal</li>
 *   <li><strong>Efectos visuales:</strong> Cambio de color para efectos especiales</li>
 * </ul>
 * 
 * <h3>Integración con sistemas:</h3>
 * <ul>
 *   <li><strong>GameState:</strong> Recibe comandos de movimiento y apuntado</li>
 *   <li><strong>BulletPool:</strong> Punto de origen para disparos</li>
 *   <li><strong>CollisionSystem:</strong> Detección de colisiones con enemigos</li>
 * </ul>
 * 
 * @author Alberto Villalpando
 * @version 1.0
 * @see PlayerControl
 * @see GameState
 * @since 2024
 */
public class Player {
    
    /** 
     * Nodo contenedor principal que agrupa toda la geometría del jugador.
     * Se adjunta directamente al gameNode en GameState.
     */
    private Node playerNode;
    
    /** 
     * Geometría principal que representa el cuerpo de la nave del jugador.
     * Cubo rectangular con proporciones que simulan una nave espacial.
     */
    private Geometry playerGeometry;
    
    /** 
     * Control que maneja toda la lógica de movimiento, límites y comportamiento.
     * Responde a comandos de GameState y actualiza la posición del jugador.
     */
    private PlayerControl playerControl;
    
    /** 
     * Referencia a la aplicación jME3 para acceso al AssetManager.
     * Necesario para crear materiales y cargar recursos visuales.
     */
    private Application app;
    
    /**
     * Constructor del jugador que inicializa todos los componentes visuales y lógicos.
     * 
     * <p>Crea una entidad jugador completamente funcional con:</p>
     * <ul>
     *   <li>Geometría visual de nave espacial con marcador direccional</li>
     *   <li>Material neón cyan con efectos de brillo</li>
     *   <li>Control de movimiento con límites de área configurados</li>
     *   <li>Posicionamiento inicial en el centro del área de juego</li>
     * </ul>
     * 
     * @param app Referencia a la aplicación jME3 para acceso al AssetManager
     * @param size Tamaño del jugador (radio de la nave en unidades del mundo)
     * @param speed Velocidad máxima de movimiento en unidades/segundo
     * @see #createPlayerGeometry(float)
     */
    public Player(Application app, float size, float speed) {
        this.app = app;
        
        // Crear nodo contenedor para el jugador
        playerNode = new Node("Player");
        
        // Crear geometría del jugador (por ahora un cubo, luego será una nave)
        createPlayerGeometry(size);
        
        // Crear y adjuntar el control de movimiento
        playerControl = new PlayerControl(speed);
        playerNode.addControl(playerControl);
        
        // Añadir efectos visuales heroicos
        setupHeroicVisualEffects();
        
        // Posición inicial en el centro
        playerNode.setLocalTranslation(0, 0, 0);
    }
    
    /**
     * Crea la representación visual épica del jugador - Nave heroica de defensa.
     * 
     * <p>Construye una nave espacial avanzada con múltiples componentes:</p>
     * <ul>
     *   <li><strong>Fuselaje principal:</strong> Cuerpo aerodinámico de la nave</li>
     *   <li><strong>Cabina del piloto:</strong> Cúpula central con efectos especiales</li>
     *   <li><strong>Alas de combate:</strong> Extensiones laterales con armamento</li>
     *   <li><strong>Motores traseros:</strong> Propulsores con efectos visuales</li>
     *   <li><strong>Cañones frontales:</strong> Sistemas de armas visibles</li>
     *   <li><strong>Efectos neón:</strong> Iluminación heroica cyan con detalles dorados</li>
     * </ul>
     * 
     * @param size Tamaño base del jugador en unidades del mundo
     */
    private void createPlayerGeometry(float size) {
        // Crear el fuselaje principal de la nave
        createMainFuselage(size);
        
        // Crear la cabina del piloto
        createPilotCockpit(size);
        
        // Crear las alas de combate
        createCombatWings(size);
        
        // Crear los motores traseros
        createEngines(size);
        
        // Crear los sistemas de armas frontales
        createWeaponSystems(size);
        
        // Crear detalles decorativos
        createDetailAccents(size);
    }
    
    /**
     * Crea el fuselaje principal de la nave heroica
     */
    private void createMainFuselage(float size) {
        // Cuerpo principal - forma aerodinámica
        Box mainBody = new Box(size * 0.4f, size * 0.15f, size * 0.6f);
        playerGeometry = new Geometry("PlayerMainBody", mainBody);
        
        // Material principal heroico
        Material mainMaterial = MaterialFactory.createNeonMaterial(
            app.getAssetManager(), getHeroicColor(), 1.2f);
        playerGeometry.setMaterial(mainMaterial);
        playerNode.attachChild(playerGeometry);
        
        // Sección frontal puntiaguda
        Box noseCone = new Box(size * 0.25f, size * 0.1f, size * 0.3f);
        Geometry noseConeGeom = new Geometry("NoseCone", noseCone);
        Material noseMateria = MaterialFactory.createNeonMaterial(
            app.getAssetManager(), getHeroicColor().mult(1.1f), 1.3f);
        noseConeGeom.setMaterial(noseMateria);
        noseConeGeom.setLocalTranslation(0, 0, size * 0.8f);
        playerNode.attachChild(noseConeGeom);
    }
    
    /**
     * Crea la cabina del piloto con efectos especiales
     */
    private void createPilotCockpit(float size) {
        // Cúpula de la cabina
        Sphere cockpit = new Sphere(16, 16, size * 0.2f);
        Geometry cockpitGeom = new Geometry("PilotCockpit", cockpit);
        
        // Material de cabina translúcido con tinte dorado
        ColorRGBA cockpitColor = new ColorRGBA(0.3f, 0.8f, 1.0f, 0.8f);
        Material cockpitMaterial = MaterialFactory.createTransparentNeonMaterial(
            app.getAssetManager(), cockpitColor, 0.8f, 1.5f);
        cockpitGeom.setMaterial(cockpitMaterial);
        MaterialFactory.setupTransparency(cockpitGeom);
        
        // Posicionar encima del fuselaje
        cockpitGeom.setLocalTranslation(0, size * 0.25f, size * 0.1f);
        playerNode.attachChild(cockpitGeom);
    }
    
    /**
     * Crea las alas de combate con detalles de armamento
     */
    private void createCombatWings(float size) {
        for (int i = -1; i <= 1; i += 2) {
            // Ala principal
            Box wing = new Box(size * 0.3f, size * 0.05f, size * 0.4f);
            Geometry wingGeom = new Geometry("CombatWing" + i, wing);
            
            Material wingMaterial = MaterialFactory.createNeonMaterial(
                app.getAssetManager(), getHeroicColor().mult(0.9f), 1.1f);
            wingGeom.setMaterial(wingMaterial);
            wingGeom.setLocalTranslation(i * size * 0.6f, 0, 0);
            playerNode.attachChild(wingGeom);
            
            // Punta del ala con efecto especial
            Box wingTip = new Box(size * 0.08f, size * 0.03f, size * 0.15f);
            Geometry wingTipGeom = new Geometry("WingTip" + i, wingTip);
            
            ColorRGBA accentColor = new ColorRGBA(1.0f, 0.8f, 0.2f, 1.0f); // Dorado
            Material tipMaterial = MaterialFactory.createNeonMaterial(
                app.getAssetManager(), accentColor, 2.0f);
            wingTipGeom.setMaterial(tipMaterial);
            wingTipGeom.setLocalTranslation(i * size * 0.85f, 0, size * 0.2f);
            playerNode.attachChild(wingTipGeom);
            
            // Hardpoint de armas
            createWingWeapon(size, i);
        }
    }
    
    /**
     * Crea sistemas de armas en las alas
     */
    private void createWingWeapon(float size, int side) {
        Cylinder weapon = new Cylinder(8, 8, size * 0.15f, size * 0.04f, true);
        Geometry weaponGeom = new Geometry("WingWeapon" + side, weapon);
        
        ColorRGBA weaponColor = new ColorRGBA(0.8f, 0.8f, 0.8f, 1.0f); // Metálico
        Material weaponMaterial = MaterialFactory.createNeonMaterial(
            app.getAssetManager(), weaponColor, 0.8f);
        weaponGeom.setMaterial(weaponMaterial);
        
        // Rotar para que apunte hacia adelante
        weaponGeom.rotate(com.jme3.math.FastMath.HALF_PI, 0, 0);
        weaponGeom.setLocalTranslation(side * size * 0.6f, -size * 0.1f, size * 0.3f);
        playerNode.attachChild(weaponGeom);
    }
    
    /**
     * Crea los motores traseros con efectos de propulsión
     */
    private void createEngines(float size) {
        // Motores principales
        for (int i = -1; i <= 1; i += 2) {
            // Carcasa del motor
            Cylinder engineHousing = new Cylinder(12, 12, size * 0.25f, size * 0.12f, true);
            Geometry engineGeom = new Geometry("Engine" + i, engineHousing);
            
            Material engineMaterial = MaterialFactory.createNeonMaterial(
                app.getAssetManager(), getHeroicColor().mult(0.7f), 1.0f);
            engineGeom.setMaterial(engineMaterial);
            
            // Rotar para que apunte hacia atrás
            engineGeom.rotate(com.jme3.math.FastMath.HALF_PI, 0, 0);
            engineGeom.setLocalTranslation(i * size * 0.3f, 0, -size * 0.6f);
            playerNode.attachChild(engineGeom);
            
            // Núcleo del motor con efecto especial
            createEngineCore(size, i);
            
            // Crear estela de propulsión
            createEngineTrail(size, i);
        }
    }
    
    /**
     * Crea el núcleo luminoso de los motores
     */
    private void createEngineCore(float size, int side) {
        Sphere engineCore = new Sphere(12, 12, size * 0.08f);
        Geometry coreGeom = new Geometry("EngineCore" + side, engineCore);
        
        ColorRGBA coreColor = new ColorRGBA(0.2f, 0.6f, 1.0f, 1.0f); // Azul energético
        Material coreMaterial = MaterialFactory.createNeonMaterial(
            app.getAssetManager(), coreColor, 3.0f);
        coreGeom.setMaterial(coreMaterial);
        
        coreGeom.setLocalTranslation(side * size * 0.3f, 0, -size * 0.6f);
        playerNode.attachChild(coreGeom);
    }
    
    /**
     * Crea estelas de propulsión dinámicas detrás de los motores
     */
    private void createEngineTrail(float size, int side) {
        // Crear múltiples segmentos de estela con transparencia gradual
        for (int i = 0; i < 4; i++) {
            Box trailSegment = new Box(size * 0.04f, size * 0.02f, size * 0.1f);
            Geometry trailGeom = new Geometry("EngineTrail" + side + "_" + i, trailSegment);
            
            // Calcular transparencia gradual (más transparente hacia atrás)
            float alpha = 0.8f - (i * 0.15f);
            float fadeLevel = (float) i / 4f;
            
            ColorRGBA trailColor = new ColorRGBA(0.3f, 0.7f, 1.0f, alpha);
            Material trailMaterial = MaterialFactory.createStreakMaterial(
                app.getAssetManager(), trailColor, fadeLevel);
            trailGeom.setMaterial(trailMaterial);
            MaterialFactory.setupTransparency(trailGeom);
            
            // Posicionar cada segmento más atrás que el anterior
            float zOffset = -size * 0.7f - (i * size * 0.15f);
            trailGeom.setLocalTranslation(side * size * 0.3f, 0, zOffset);
            playerNode.attachChild(trailGeom);
        }
        
        // Crear partículas de energía en la estela
        createTrailParticles(size, side);
    }
    
    /**
     * Crea pequeñas partículas de energía en las estelas
     */
    private void createTrailParticles(float size, int side) {
        for (int i = 0; i < 3; i++) {
            Sphere particle = new Sphere(6, 6, size * 0.02f);
            Geometry particleGeom = new Geometry("TrailParticle" + side + "_" + i, particle);
            
            ColorRGBA particleColor = new ColorRGBA(0.8f, 0.9f, 1.0f, 0.7f);
            Material particleMaterial = MaterialFactory.createNeonMaterial(
                app.getAssetManager(), particleColor, 2.0f);
            particleGeom.setMaterial(particleMaterial);
            
            // Distribuir partículas aleatoriamente en la estela
            float randomX = (float) (side * size * 0.3f + (Math.random() - 0.5) * size * 0.1f);
            float randomY = (float) ((Math.random() - 0.5) * size * 0.1f);
            float randomZ;
            randomZ = (float) (-size * 0.8f - (Math.random() * size * 0.4f));
            
            particleGeom.setLocalTranslation((float)randomX, (float)randomY, (float)randomZ);
            playerNode.attachChild(particleGeom);
        }
    }
    
    /**
     * Crea los sistemas de armas frontales
     */
    private void createWeaponSystems(float size) {
        // Cañones frontales principales
        for (int i = -1; i <= 1; i += 2) {
            // Cuerpo del cañón
            Cylinder cannon = new Cylinder(8, 8, size * 0.2f, size * 0.03f, true);
            Geometry cannonGeom = new Geometry("FrontCannon" + i, cannon);
            
            ColorRGBA cannonColor = new ColorRGBA(1.0f, 0.8f, 0.2f, 1.0f); // Dorado
            Material cannonMaterial = MaterialFactory.createNeonMaterial(
                app.getAssetManager(), cannonColor, 1.5f);
            cannonGeom.setMaterial(cannonMaterial);
            
            // Rotar para que apunte hacia adelante
            cannonGeom.rotate(com.jme3.math.FastMath.HALF_PI, 0, 0);
            cannonGeom.setLocalTranslation(i * size * 0.2f, 0, size * 0.7f);
            playerNode.attachChild(cannonGeom);
            
            // Añadir efectos de energía en la boca del cañón
            createWeaponMuzzle(size, i);
            
            // Crear líneas de energía en el cañón
            createWeaponEnergyLines(size, i);
        }
    }
    
    /**
     * Crea efectos de energía en la boca del cañón
     */
    private void createWeaponMuzzle(float size, int side) {
        // Anillo de energía en la boca del cañón
        Cylinder muzzleRing = new Cylinder(12, 12, size * 0.04f, 0.01f, size * 0.02f, true, false);
        Geometry muzzleGeom = new Geometry("WeaponMuzzle" + side, muzzleRing);
        
        ColorRGBA muzzleColor = new ColorRGBA(0.8f, 1.0f, 0.3f, 0.9f); // Verde energético
        Material muzzleMaterial = MaterialFactory.createTransparentNeonMaterial(
            app.getAssetManager(), muzzleColor, 0.9f, 2.5f);
        muzzleGeom.setMaterial(muzzleMaterial);
        MaterialFactory.setupTransparency(muzzleGeom);
        
        // Posicionar al frente del cañón
        muzzleGeom.setLocalTranslation(side * size * 0.2f, 0, size * 0.9f);
        playerNode.attachChild(muzzleGeom);
        
        // Núcleo de energía central
        Sphere energyCore = new Sphere(8, 8, size * 0.015f);
        Geometry coreGeom = new Geometry("WeaponCore" + side, energyCore);
        
        ColorRGBA coreColor = new ColorRGBA(1.0f, 1.0f, 0.8f, 1.0f); // Blanco energético
        Material coreMaterial = MaterialFactory.createNeonMaterial(
            app.getAssetManager(), coreColor, 3.0f);
        coreGeom.setMaterial(coreMaterial);
        coreGeom.setLocalTranslation(side * size * 0.2f, 0, size * 0.9f);
        playerNode.attachChild(coreGeom);
    }
    
    /**
     * Crea líneas de energía decorativas en los cañones
     */
    private void createWeaponEnergyLines(float size, int side) {
        for (int j = 0; j < 2; j++) {
            Box energyLine = new Box(size * 0.008f, size * 0.008f, size * 0.15f);
            Geometry lineGeom = new Geometry("WeaponEnergyLine" + side + "_" + j, energyLine);
            
            ColorRGBA lineColor = new ColorRGBA(0.9f, 1.0f, 0.4f, 1.0f); // Amarillo energético
            Material lineMaterial = MaterialFactory.createNeonMaterial(
                app.getAssetManager(), lineColor, 2.0f);
            lineGeom.setMaterial(lineMaterial);
            
            // Posicionar las líneas alrededor del cañón
            float angleOffset = j * com.jme3.math.FastMath.PI;
            float offsetX = side * size * 0.2f + (float)(Math.cos(angleOffset) * size * 0.05f);
            float offsetY = (float)(Math.sin(angleOffset) * size * 0.05f);
            
            lineGeom.setLocalTranslation(offsetX, offsetY, size * 0.75f);
            playerNode.attachChild(lineGeom);
        }
    }
    
    /**
     * Crea detalles decorativos heroicos
     */
    private void createDetailAccents(float size) {
        // Líneas de energía en el fuselaje
        for (int i = 0; i < 3; i++) {
            Box energyLine = new Box(size * 0.02f, size * 0.01f, size * 0.4f);
            Geometry lineGeom = new Geometry("EnergyLine" + i, energyLine);
            
            ColorRGBA lineColor = new ColorRGBA(1.0f, 0.9f, 0.3f, 1.0f); // Dorado brillante
            Material lineMaterial = MaterialFactory.createNeonMaterial(
                app.getAssetManager(), lineColor, 2.5f);
            lineGeom.setMaterial(lineMaterial);
            
            float offsetX = (i - 1) * size * 0.15f;
            lineGeom.setLocalTranslation(offsetX, size * 0.16f, 0);
            playerNode.attachChild(lineGeom);
        }
        
        // Escudo de energía sutil
        createEnergyShield(size);
    }
    
    /**
     * Crea un escudo de energía sutil alrededor de la nave
     */
    private void createEnergyShield(float size) {
        Sphere shield = new Sphere(20, 20, size * 1.1f);
        Geometry shieldGeom = new Geometry("EnergyShield", shield);
        
        ColorRGBA shieldColor = new ColorRGBA(0.3f, 0.9f, 1.0f, 0.1f); // Muy transparente
        Material shieldMaterial = MaterialFactory.createTransparentNeonMaterial(
            app.getAssetManager(), shieldColor, 0.1f, 1.0f);
        shieldGeom.setMaterial(shieldMaterial);
        MaterialFactory.setupTransparency(shieldGeom);
        
        playerNode.attachChild(shieldGeom);
    }
    
    /**
     * Obtiene el color heroico principal de la nave
     */
    private ColorRGBA getHeroicColor() {
        return new ColorRGBA(0.2f, 0.9f, 1.0f, 1.0f); // Cyan heroico
    }
    
    /**
     * Configura efectos visuales heroicos específicos para la nave del jugador
     */
    private void setupHeroicVisualEffects() {
        // Efectos principales de la nave
        VisualEffectsControl mainEffects = new VisualEffectsControl();
        setupMainShipEffects(mainEffects);
        playerNode.addControl(mainEffects);
        
        // Efectos especiales para los motores
        setupEngineEffects();
        
        // Efectos para las líneas de energía
        setupEnergyLineEffects();
        
        // Efectos para el escudo
        setupShieldEffects();
    }
    
    /**
     * Configura los efectos principales de la nave
     */
    private void setupMainShipEffects(VisualEffectsControl effects) {
        // Pulsación suave heroica
        effects.enablePulsing(1.2f, 0.15f, 1.0f);
        
        // Ligero cambio de color entre cyan y azul
        ColorRGBA baseColor = getHeroicColor();
        ColorRGBA targetColor = new ColorRGBA(0.1f, 0.7f, 1.0f, 1.0f);
        effects.enableColorShift(baseColor, targetColor, 0.6f);
        
        // Rotación muy sutil para dar vida
        effects.enableRotation(0, 0.2f, 0);
    }
    
    /**
     * Configura efectos especiales para los núcleos de los motores
     */
    private void setupEngineEffects() {
        // Buscar y añadir efectos a los núcleos de los motores
        for (int i = -1; i <= 1; i += 2) {
            // Buscar el núcleo del motor por nombre
            com.jme3.scene.Spatial coreSpatial = findSpatialByName("EngineCore" + i);
            if (coreSpatial instanceof Geometry) {
                Geometry coreGeom = (Geometry) coreSpatial;
                VisualEffectsControl engineEffects = new VisualEffectsControl();
                
                // Pulsación intensa para simular propulsión
                engineEffects.enablePulsing(4.0f, 0.8f, 1.5f);
                
                // Escalado rápido para simular energía
                engineEffects.enableScaling(3.0f, 0.15f);
                
                // Color dinámico entre azul y blanco
                ColorRGBA engineBlue = new ColorRGBA(0.2f, 0.6f, 1.0f, 1.0f);
                ColorRGBA engineWhite = new ColorRGBA(0.8f, 0.9f, 1.0f, 1.0f);
                engineEffects.enableColorShift(engineBlue, engineWhite, 2.0f);
                
                coreGeom.addControl(engineEffects);
            }
        }
    }
    
    /**
     * Configura efectos para las líneas de energía
     */
    private void setupEnergyLineEffects() {
        for (int i = 0; i < 3; i++) {
            com.jme3.scene.Spatial lineSpatial = findSpatialByName("EnergyLine" + i);
            if (lineSpatial instanceof Geometry) {
                Geometry lineGeom = (Geometry) lineSpatial;
                VisualEffectsControl lineEffects = new VisualEffectsControl();
                
                // Pulsación secuencial para crear efecto de flujo de energía
                float phaseOffset = i * 0.8f; // Desfase para crear onda
                lineEffects.enablePulsing(2.5f + phaseOffset, 0.6f, 1.2f);
                
                lineGeom.addControl(lineEffects);
            }
        }
    }
    
    /**
     * Configura efectos para el escudo de energía
     */
    private void setupShieldEffects() {
        com.jme3.scene.Spatial shieldSpatial = findSpatialByName("EnergyShield");
        if (shieldSpatial instanceof Geometry) {
            Geometry shieldGeom = (Geometry) shieldSpatial;
            VisualEffectsControl shieldEffects = new VisualEffectsControl();
            
            // Pulsación muy sutil para el escudo
            shieldEffects.enablePulsing(0.8f, 0.3f, 0.8f);
            
            // Rotación lenta para dar movimiento al escudo
            shieldEffects.enableRotation(0.1f, 0.2f, 0.15f);
            
            shieldGeom.addControl(shieldEffects);
        }
    }
    
    /**
     * Busca un spatial hijo por nombre recursivamente
     */
    private com.jme3.scene.Spatial findSpatialByName(String name) {
        return findSpatialByNameRecursive(playerNode, name);
    }
    
    /**
     * Búsqueda recursiva de spatials por nombre
     */
    private com.jme3.scene.Spatial findSpatialByNameRecursive(Node parent, String name) {
        for (com.jme3.scene.Spatial child : parent.getChildren()) {
            if (child.getName() != null && child.getName().equals(name)) {
                return child;
            }
            if (child instanceof Node) {
                com.jme3.scene.Spatial result = findSpatialByNameRecursive((Node) child, name);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }
    
    /**
     * Obtiene el nodo contenedor principal del jugador.
     * 
     * <p>Este nodo contiene toda la geometría visual del jugador y debe
     * ser adjuntado al gameNode en GameState para que sea visible en la escena.</p>
     * 
     * @return Node principal que contiene toda la representación del jugador
     * @see GameState#initializePlayer()
     */
    public Node getNode() {
        return playerNode;
    }
    
    /**
     * Obtiene el control que maneja la lógica de movimiento del jugador.
     * 
     * <p>El PlayerControl es utilizado por GameState para:</p>
     * <ul>
     *   <li>Establecer dirección de movimiento desde input WASD</li>
     *   <li>Configurar dirección de apuntado desde posición del mouse</li>
     *   <li>Aplicar límites del área de juego</li>
     *   <li>Actualizar posición cada frame</li>
     * </ul>
     * 
     * @return PlayerControl que maneja comportamiento y movimiento
     * @see PlayerControl
     * @see GameState#updateMoveDirection()
     */
    public PlayerControl getControl() {
        return playerControl;
    }
    
    /**
     * Obtiene la posición actual del jugador
     * 
     * @return Vector3f con la posición
     */
    public Vector3f getPosition() {
        return playerNode.getLocalTranslation();
    }
    
    /**
     * Establece la posición del jugador
     * 
     * @param position Nueva posición
     */
    public void setPosition(Vector3f position) {
        playerNode.setLocalTranslation(position);
    }
    
    /**
     * Actualiza el color del jugador (útil para efectos visuales)
     * 
     * @param color Nuevo color
     */
    public void setColor(ColorRGBA color) {
        Material mat = playerGeometry.getMaterial();
        mat.setColor("Color", color);
        mat.setColor("GlowColor", color);
    }
}