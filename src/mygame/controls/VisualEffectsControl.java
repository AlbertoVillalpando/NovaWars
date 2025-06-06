package mygame.controls;

import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import java.util.ArrayList;
import java.util.List;

/**
 * Control para efectos visuales dinámicos en entidades del juego.
 * 
 * <p>VisualEffectsControl maneja animaciones visuales como rotación, pulsación,
 * cambios de color y otros efectos que dan vida a las entidades sin afectar
 * la lógica de gameplay.</p>
 * 
 * <h3>Efectos disponibles:</h3>
 * <ul>
 *   <li><strong>Rotación continua:</strong> Rotación automática en cualquier eje</li>
 *   <li><strong>Pulsación de brillo:</strong> Variación sinusoidal de intensidad</li>
 *   <li><strong>Cambio de color:</strong> Transición entre colores</li>
 *   <li><strong>Escalado dinámico:</strong> Pulsación de tamaño</li>
 *   <li><strong>Efectos combinados:</strong> Múltiples efectos simultáneos</li>
 * </ul>
 * 
 * @author Alberto Villalpando
 * @version 1.0
 * @since 2024
 */
public class VisualEffectsControl extends AbstractControl {
    
    // Configuración de efectos
    private boolean enableRotation = false;
    private boolean enablePulsing = false;
    private boolean enableColorShift = false;
    private boolean enableScaling = false;
    
    // Parámetros de rotación
    private float rotationSpeedX = 0f;
    private float rotationSpeedY = 0f;
    private float rotationSpeedZ = 0f;
    
    // Parámetros de pulsación
    private float pulseSpeed = 2f;
    private float pulseIntensity = 0.3f;
    private float baseBrightness = 1f;
    
    // Parámetros de cambio de color
    private ColorRGBA baseColor;
    private ColorRGBA targetColor;
    private float colorShiftSpeed = 1f;
    
    // Parámetros de escalado
    private float scaleSpeed = 1.5f;
    private float scaleRange = 0.1f;
    private float baseScale = 1f;
    
    // Variables de tiempo
    private float timeAccumulator = 0f;
    
    // Lista de materiales afectados
    private List<Material> affectedMaterials = new ArrayList<>();
    
    /**
     * Constructor básico del control de efectos visuales
     */
    public VisualEffectsControl() {
        // Constructor vacío - los efectos se configuran mediante métodos
    }
    
    /**
     * Activa rotación continua en los ejes especificados
     * 
     * @param speedX Velocidad de rotación en X (radianes/segundo)
     * @param speedY Velocidad de rotación en Y (radianes/segundo)
     * @param speedZ Velocidad de rotación en Z (radianes/segundo)
     */
    public void enableRotation(float speedX, float speedY, float speedZ) {
        this.enableRotation = true;
        this.rotationSpeedX = speedX;
        this.rotationSpeedY = speedY;
        this.rotationSpeedZ = speedZ;
    }
    
    /**
     * Activa pulsación de brillo
     * 
     * @param speed Velocidad de pulsación
     * @param intensity Intensidad del efecto (0.0 - 1.0)
     * @param baseBrightness Brillo base
     */
    public void enablePulsing(float speed, float intensity, float baseBrightness) {
        this.enablePulsing = true;
        this.pulseSpeed = speed;
        this.pulseIntensity = intensity;
        this.baseBrightness = baseBrightness;
    }
    
    /**
     * Activa cambio gradual de color
     * 
     * @param baseColor Color inicial
     * @param targetColor Color objetivo
     * @param speed Velocidad de transición
     */
    public void enableColorShift(ColorRGBA baseColor, ColorRGBA targetColor, float speed) {
        this.enableColorShift = true;
        this.baseColor = baseColor.clone();
        this.targetColor = targetColor.clone();
        this.colorShiftSpeed = speed;
    }
    
    /**
     * Activa escalado dinámico (pulsación de tamaño)
     * 
     * @param speed Velocidad de pulsación
     * @param range Rango de escalado (0.1 = ±10%)
     */
    public void enableScaling(float speed, float range) {
        this.enableScaling = true;
        this.scaleSpeed = speed;
        this.scaleRange = range;
        this.baseScale = spatial != null ? spatial.getLocalScale().x : 1f;
    }
    
    @Override
    protected void controlUpdate(float tpf) {
        if (spatial == null) return;
        
        timeAccumulator += tpf;
        
        // Actualizar rotación
        if (enableRotation) {
            updateRotation(tpf);
        }
        
        // Actualizar pulsación de brillo
        if (enablePulsing) {
            updatePulsing();
        }
        
        // Actualizar cambio de color
        if (enableColorShift) {
            updateColorShift();
        }
        
        // Actualizar escalado
        if (enableScaling) {
            updateScaling();
        }
    }
    
    /**
     * Actualiza la rotación del spatial
     */
    private void updateRotation(float tpf) {
        if (rotationSpeedX != 0) {
            spatial.rotate(rotationSpeedX * tpf, 0, 0);
        }
        if (rotationSpeedY != 0) {
            spatial.rotate(0, rotationSpeedY * tpf, 0);
        }
        if (rotationSpeedZ != 0) {
            spatial.rotate(0, 0, rotationSpeedZ * tpf);
        }
    }
    
    /**
     * Actualiza la pulsación de brillo
     */
    private void updatePulsing() {
        float pulseValue = FastMath.sin(timeAccumulator * pulseSpeed) * pulseIntensity;
        float currentBrightness = baseBrightness + pulseValue;
        
        // Aplicar a todos los materiales afectados
        for (Material material : affectedMaterials) {
            ColorRGBA currentColor = (ColorRGBA) material.getParam("Color").getValue();
            ColorRGBA glowColor = currentColor.mult(currentBrightness);
            material.setColor("GlowColor", glowColor);
        }
    }
    
    /**
     * Actualiza el cambio gradual de color
     */
    private void updateColorShift() {
        float t = (FastMath.sin(timeAccumulator * colorShiftSpeed) + 1f) / 2f; // 0-1
        ColorRGBA currentColor = baseColor.interpolateLocal(targetColor, t);
        
        // Aplicar a todos los materiales afectados
        for (Material material : affectedMaterials) {
            material.setColor("Color", currentColor);
            material.setColor("GlowColor", currentColor);
        }
    }
    
    /**
     * Actualiza el escalado dinámico
     */
    private void updateScaling() {
        float scaleValue = FastMath.sin(timeAccumulator * scaleSpeed) * scaleRange;
        float currentScale = baseScale + scaleValue;
        spatial.setLocalScale(currentScale);
    }
    
    /**
     * Recolecta automáticamente todos los materiales del spatial y sus hijos
     */
    public void collectMaterials() {
        affectedMaterials.clear();
        collectMaterialsRecursive(spatial);
    }
    
    /**
     * Recolecta materiales recursivamente
     */
    private void collectMaterialsRecursive(Spatial spatial) {
        if (spatial instanceof Geometry) {
            Geometry geom = (Geometry) spatial;
            if (geom.getMaterial() != null) {
                affectedMaterials.add(geom.getMaterial());
            }
        } else if (spatial instanceof Node) {
            Node node = (Node) spatial;
            for (Spatial child : node.getChildren()) {
                collectMaterialsRecursive(child);
            }
        }
    }
    
    /**
     * Añade un material específico a la lista de afectados
     */
    public void addAffectedMaterial(Material material) {
        if (!affectedMaterials.contains(material)) {
            affectedMaterials.add(material);
        }
    }
    
    /**
     * Configura un preset de efectos para enemigo agresivo
     */
    public void setupAggressivePreset() {
        enableRotation(0, 1f, 0); // Rotación lenta en Y
        enablePulsing(3f, 0.4f, 1.2f); // Pulsación rápida e intensa
        enableScaling(2f, 0.05f); // Escalado sutil
    }
    
    /**
     * Configura un preset de efectos para enemigo elegante
     */
    public void setupElegantPreset() {
        enableRotation(0, 0.5f, 0); // Rotación suave
        enablePulsing(1.5f, 0.2f, 1f); // Pulsación suave
        ColorRGBA blue = new ColorRGBA(0.1f, 0.8f, 1f, 1f);
        ColorRGBA cyan = new ColorRGBA(0.4f, 1f, 1f, 1f);
        enableColorShift(blue, cyan, 0.8f); // Cambio de color sutil
    }
    
    /**
     * Configura un preset de efectos para enemigo evasivo
     */
    public void setupEvasivePreset() {
        enableRotation(0.5f, 2f, 0.3f); // Rotación en múltiples ejes
        enablePulsing(4f, 0.6f, 1.1f); // Pulsación muy rápida
        enableScaling(3f, 0.08f); // Escalado nervioso
    }
    
    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
        // No necesita lógica de renderizado especial
    }
    
    @Override
    public void setSpatial(Spatial spatial) {
        super.setSpatial(spatial);
        if (spatial != null) {
            // Recolectar materiales automáticamente cuando se asigna el spatial
            collectMaterials();
        }
    }
}