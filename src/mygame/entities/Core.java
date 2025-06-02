package mygame.entities;


import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Cylinder;
import com.jme3.scene.shape.Sphere;

/**
 * Representa el núcleo central que el jugador debe defender.
 * El núcleo tiene un sistema de salud y cambia de apariencia visual
 * según su estado de daño.
 * 
 * @author tu_nombre
 */
public class Core extends Node {
    
    // Configuración del núcleo
    private float maxHealth;
    private float currentHealth;
    private float size;
    
    // Componentes visuales
    private Geometry coreGeometry;
    private Geometry shieldGeometry;
    private Material coreMaterial;
    private Material shieldMaterial;
    
    // Estados y efectos
    private boolean isInvulnerable = false;
    private float invulnerabilityTime = 0f;
    private static final float INVULNERABILITY_DURATION = 0.5f;
    
    // Colores según estado de salud
    private static final ColorRGBA HEALTH_HIGH = new ColorRGBA(0f, 1f, 1f, 1f);    // Cyan
    private static final ColorRGBA HEALTH_MEDIUM = new ColorRGBA(1f, 1f, 0f, 1f);  // Amarillo
    private static final ColorRGBA HEALTH_LOW = new ColorRGBA(1f, 0f, 0f, 1f);     // Rojo
    private static final ColorRGBA SHIELD_COLOR = new ColorRGBA(0.2f, 0.8f, 1f, 0.3f); // Azul translúcido
    
    /**
     * Constructor del núcleo central
     * 
     * @param assetManager AssetManager para cargar materiales
     * @param health Salud máxima del núcleo
     * @param size Tamaño del núcleo
     */
    public Core(AssetManager assetManager, float health, float size) {
        super("Core");
        
        this.maxHealth = health;
        this.currentHealth = health;
        this.size = size;
        
        // Inicializar la geometría del núcleo
        initializeCoreGeometry(assetManager);
        
        // Inicializar el escudo visual
        initializeShieldGeometry(assetManager);
        
        // Posicionar en el centro del escenario
        this.setLocalTranslation(0, 0, 0);
    }
    
    /**
     * Inicializa la geometría principal del núcleo
     */
    private void initializeCoreGeometry(AssetManager assetManager) {
        // Crear una esfera para el núcleo principal
        Sphere coreSphere = new Sphere(32, 32, size);
        coreGeometry = new Geometry("CoreGeometry", coreSphere);
        
        // Material emisivo para efecto neón
        coreMaterial = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        coreMaterial.setColor("Color", HEALTH_HIGH);
        coreMaterial.setColor("GlowColor", HEALTH_HIGH);
        
        coreGeometry.setMaterial(coreMaterial);
        this.attachChild(coreGeometry);
        
        // Añadir un anillo decorativo
        Cylinder ring = new Cylinder(32, 32, size * 1.5f, 0.1f, size * 0.2f, true, false);
        Geometry ringGeometry = new Geometry("CoreRing", ring);
        
        Material ringMaterial = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        ringMaterial.setColor("Color", ColorRGBA.White);
        ringGeometry.setMaterial(ringMaterial);
        
        // Rotar el anillo para que esté horizontal
        ringGeometry.rotate(FastMath.HALF_PI, 0, 0);
        
        this.attachChild(ringGeometry);
    }
    
    /**
     * Inicializa el escudo visual del núcleo
     */
    private void initializeShieldGeometry(AssetManager assetManager) {
        // Esfera más grande para el escudo
        Sphere shieldSphere = new Sphere(16, 16, size * 1.8f);
        shieldGeometry = new Geometry("ShieldGeometry", shieldSphere);
        
        shieldMaterial = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        shieldMaterial.setColor("Color", SHIELD_COLOR);
        shieldMaterial.getAdditionalRenderState().setBlendMode(com.jme3.material.RenderState.BlendMode.Alpha);
        
        shieldGeometry.setMaterial(shieldMaterial);
        shieldGeometry.setQueueBucket(com.jme3.renderer.queue.RenderQueue.Bucket.Transparent);
        
        // El escudo empieza invisible
        this.attachChild(shieldGeometry);
        shieldGeometry.setCullHint(CullHint.Always);
    }
    
    /**
     * Aplica daño al núcleo
     * 
     * @param damage Cantidad de daño a aplicar
     * @return true si el núcleo fue destruido, false en caso contrario
     */
    public boolean takeDamage(float damage) {
        if (isInvulnerable) {
            return false;
        }
        
        currentHealth -= damage;
        currentHealth = Math.max(0, currentHealth);
        
        // Actualizar color según salud
        updateHealthVisuals();
        
        // Activar invulnerabilidad temporal
        activateInvulnerability();
        
        // Efecto de impacto
        createImpactEffect();
        
        return currentHealth <= 0;
    }
    
    /**
     * Actualiza los visuales según el porcentaje de salud
     */
    private void updateHealthVisuals() {
        float healthPercentage = currentHealth / maxHealth;
        
        ColorRGBA newColor;
        if (healthPercentage > 0.66f) {
            newColor = HEALTH_HIGH;
        } else if (healthPercentage > 0.33f) {
            newColor = HEALTH_MEDIUM;
        } else {
            newColor = HEALTH_LOW;
        }
        
        coreMaterial.setColor("Color", newColor);
        coreMaterial.setColor("GlowColor", newColor);
    }
    
    /**
     * Activa la invulnerabilidad temporal
     */
    private void activateInvulnerability() {
        isInvulnerable = true;
        invulnerabilityTime = INVULNERABILITY_DURATION;
        
        // Mostrar escudo
        shieldGeometry.setCullHint(CullHint.Dynamic);
    }
    
    /**
     * Crea un efecto visual de impacto
     */
    private void createImpactEffect() {
        // Escalar temporalmente el núcleo para simular impacto
        // Esto se manejará en el método update
    }
    
    /**
     * Actualiza el estado del núcleo
     * 
     * @param tpf Time per frame
     */
    public void update(float tpf) {
        // Rotar el núcleo lentamente
        rotate(0, tpf * 0.5f, 0);
        
        // Manejar invulnerabilidad
        if (isInvulnerable) {
            invulnerabilityTime -= tpf;
            
            // Hacer parpadear el escudo
            float alpha = 0.3f + 0.2f * FastMath.sin(invulnerabilityTime * 20f);
            shieldMaterial.setColor("Color", new ColorRGBA(0.2f, 0.8f, 1f, alpha));
            
            if (invulnerabilityTime <= 0) {
                isInvulnerable = false;
                shieldGeometry.setCullHint(CullHint.Always);
            }
        }
        
        // Efecto de pulsación cuando tiene poca salud
        if (currentHealth / maxHealth < 0.33f) {
            float pulse = 1f + 0.05f * FastMath.sin(tpf * 10f);
            coreGeometry.setLocalScale(pulse);
        }
    }
    
    /**
     * Cura el núcleo
     * 
     * @param amount Cantidad de salud a restaurar
     */
    public void heal(float amount) {
        currentHealth = Math.min(maxHealth, currentHealth + amount);
        updateHealthVisuals();
    }
    
    /**
     * Obtiene la salud actual
     * 
     * @return Salud actual del núcleo
     */
    public float getCurrentHealth() {
        return currentHealth;
    }
    
    /**
     * Obtiene la salud máxima
     * 
     * @return Salud máxima del núcleo
     */
    public float getMaxHealth() {
        return maxHealth;
    }
    
    /**
     * Obtiene el porcentaje de salud
     * 
     * @return Porcentaje de salud (0.0 - 1.0)
     */
    public float getHealthPercentage() {
        return currentHealth / maxHealth;
    }
    
    /**
     * Verifica si el núcleo está destruido
     * 
     * @return true si no tiene salud, false en caso contrario
     */
    public boolean isDestroyed() {
        return currentHealth <= 0;
    }
    
    /**
     * Obtiene el radio del núcleo para colisiones
     * 
     * @return Radio del núcleo
     */
    public float getRadius() {
        return size;
    }
    
    /**
     * Reinicia el núcleo a su estado inicial
     */
    public void reset() {
        currentHealth = maxHealth;
        isInvulnerable = false;
        invulnerabilityTime = 0f;
        updateHealthVisuals();
        shieldGeometry.setCullHint(CullHint.Always);
        coreGeometry.setLocalScale(1f);
    }
}