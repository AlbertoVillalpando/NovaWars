package mygame.utils;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;

/**
 * Factory para crear materiales especializados con efectos neón únicos.
 * 
 * <p>MaterialFactory centraliza la creación de materiales con efectos visuales
 * avanzados, proporcionando consistencia visual y fácil personalización de
 * la apariencia neón del juego NovaWars.</p>
 * 
 * <h3>Tipos de materiales disponibles:</h3>
 * <ul>
 *   <li><strong>Neón básico:</strong> Material emisivo estándar</li>
 *   <li><strong>Neón pulsante:</strong> Material con variación de brillo</li>
 *   <li><strong>Neón transparente:</strong> Material con alpha blending</li>
 *   <li><strong>Neón iridiscente:</strong> Material con cambio de color gradual</li>
 * </ul>
 * 
 * @author Alberto Villalpando
 * @version 1.0
 * @since 2024
 */
public class MaterialFactory {
    
    /**
     * Crea un material neón básico con efecto de brillo
     * 
     * @param assetManager AssetManager para cargar shaders
     * @param color Color base del material
     * @param glowIntensity Intensidad del brillo (1.0f = normal, 2.0f = muy brillante)
     * @return Material configurado con efecto neón
     */
    public static Material createNeonMaterial(AssetManager assetManager, ColorRGBA color, float glowIntensity) {
        Material material = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        
        // Color base
        material.setColor("Color", color);
        
        // Efecto de brillo (glow)
        ColorRGBA glowColor = color.mult(glowIntensity);
        material.setColor("GlowColor", glowColor);
        
        return material;
    }
    
    /**
     * Crea un material neón transparente para efectos especiales
     * 
     * @param assetManager AssetManager para cargar shaders
     * @param color Color base del material
     * @param alpha Nivel de transparencia (0.0f = invisible, 1.0f = opaco)
     * @param glowIntensity Intensidad del brillo
     * @return Material transparente configurado
     */
    public static Material createTransparentNeonMaterial(AssetManager assetManager, ColorRGBA color, 
                                                        float alpha, float glowIntensity) {
        Material material = createNeonMaterial(assetManager, color, glowIntensity);
        
        // Configurar transparencia
        ColorRGBA transparentColor = color.clone();
        transparentColor.a = alpha;
        material.setColor("Color", transparentColor);
        
        // Configurar blend mode para transparencia
        material.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        
        return material;
    }
    
    /**
     * Crea un material neón con efecto pulsante
     * 
     * @param assetManager AssetManager para cargar shaders
     * @param color Color base del material
     * @param baseIntensity Intensidad base del brillo
     * @param pulseRange Rango de pulsación (diferencia entre mín y máx)
     * @return Material configurado para pulsación
     */
    public static Material createPulsingNeonMaterial(AssetManager assetManager, ColorRGBA color, 
                                                   float baseIntensity, float pulseRange) {
        Material material = createNeonMaterial(assetManager, color, baseIntensity);
        
        // Marcar como material pulsante para que los controles lo reconozcan
        material.setName("PulsingNeon");
        material.setFloat("PulseRange", pulseRange);
        material.setFloat("BaseIntensity", baseIntensity);
        
        return material;
    }
    
    /**
     * Crea un material neón con gradiente de colores
     * 
     * @param assetManager AssetManager para cargar shaders
     * @param primaryColor Color principal
     * @param secondaryColor Color secundario para el gradiente
     * @param glowIntensity Intensidad del brillo
     * @return Material con efecto de gradiente
     */
    public static Material createGradientNeonMaterial(AssetManager assetManager, ColorRGBA primaryColor, 
                                                     ColorRGBA secondaryColor, float glowIntensity) {
        Material material = createNeonMaterial(assetManager, primaryColor, glowIntensity);
        
        // Marcar como material de gradiente
        material.setName("GradientNeon");
        material.setColor("SecondaryColor", secondaryColor);
        
        return material;
    }
    
    /**
     * Aplicar configuración de transparencia a una geometría
     * 
     * @param geometry Geometría a configurar
     */
    public static void setupTransparency(Geometry geometry) {
        geometry.setQueueBucket(RenderQueue.Bucket.Transparent);
    }
    
    /**
     * Crear material para efectos de energía
     * 
     * @param assetManager AssetManager
     * @param color Color base
     * @return Material optimizado para efectos de energía
     */
    public static Material createEnergyMaterial(AssetManager assetManager, ColorRGBA color) {
        Material material = createTransparentNeonMaterial(assetManager, color, 0.8f, 1.5f);
        material.setName("EnergyEffect");
        return material;
    }
    
    /**
     * Crear material para estelas de velocidad
     * 
     * @param assetManager AssetManager
     * @param color Color base
     * @param fadeLevel Nivel de desvanecimiento (0.0f - 1.0f)
     * @return Material optimizado para estelas
     */
    public static Material createStreakMaterial(AssetManager assetManager, ColorRGBA color, float fadeLevel) {
        float alpha = 0.9f - (fadeLevel * 0.6f); // Más transparente según fadeLevel
        Material material = createTransparentNeonMaterial(assetManager, color, alpha, 0.8f);
        material.setName("SpeedStreak");
        return material;
    }
}