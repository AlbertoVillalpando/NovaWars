package mygame.entities;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Sphere;
import mygame.controls.EnemyControl;

/**
 * Clase base abstracta para todos los enemigos de NovaWars.
 * 
 * <p>Define la arquitectura común y comportamiento base que todos los tipos de
 * enemigos deben implementar. Proporciona un framework robusto para crear
 * diferentes variantes de enemigos con patrones de movimiento únicos.</p>
 * 
 * <h3>Arquitectura de enemigos:</h3>
 * <ul>
 *   <li><strong>Estructura base:</strong> Propiedades comunes de vida, velocidad, tamaño</li>
 *   <li><strong>Sistema visual:</strong> Geometría configurable con materiales neón</li>
 *   <li><strong>Control de IA:</strong> EnemyControl para patrones de movimiento</li>
 *   <li><strong>Estados de vida:</strong> Gestión de muerte y llegada al núcleo</li>
 * </ul>
 * 
 * <h3>Propiedades configurables:</h3>
 * <ul>
 *   <li><strong>Health:</strong> Vida actual y máxima del enemigo</li>
 *   <li><strong>Speed:</strong> Velocidad de movimiento en unidades/segundo</li>
 *   <li><strong>Size:</strong> Radio para colisiones y representación visual</li>
 *   <li><strong>CoreDamage:</strong> Daño infligido al núcleo al alcanzarlo</li>
 * </ul>
 * 
 * <h3>Sistema de vida y estados:</h3>
 * <ul>
 *   <li><strong>Daño progresivo:</strong> Reduce brillo visual con la salud</li>
 *   <li><strong>Muerte:</strong> Marcado para eliminación al llegar a 0 vida</li>
 *   <li><strong>Llegada al núcleo:</strong> Aplica daño y se auto-destruye</li>
 *   <li><strong>Reutilización:</strong> Reset completo para pooling de objetos</li>
 * </ul>
 * 
 * <h3>Patrones de implementación:</h3>
 * <ul>
 *   <li><strong>Template Method:</strong> createEnemyControl() personalizable</li>
 *   <li><strong>Factory Pattern:</strong> getEnemyColor() define apariencia</li>
 *   <li><strong>Observer Pattern:</strong> Notificaciones de eventos de vida</li>
 *   <li><strong>Object Pool:</strong> Sistema de reset para reutilización</li>
 * </ul>
 * 
 * <h3>Subclases implementadas:</h3>
 * <ul>
 *   <li><strong>BasicEnemy:</strong> Movimiento directo hacia el núcleo</li>
 *   <li><strong>CircularEnemy:</strong> Patrón circular alrededor del núcleo</li>
 *   <li><strong>ZigZagEnemy:</strong> Movimiento serpenteante hacia el objetivo</li>
 * </ul>
 * 
 * <h3>Integración con sistemas:</h3>
 * <ul>
 *   <li><strong>EnemyManager:</strong> Spawning, gestión y pooling</li>
 *   <li><strong>GameState:</strong> Detección de colisiones con balas</li>
 *   <li><strong>CoreControl:</strong> Aplicación de daño al núcleo</li>
 * </ul>
 * 
 * @author Alberto Villalpando
 * @version 1.0
 * @see EnemyControl
 * @see EnemyManager
 * @see BasicEnemy
 * @since 2024
 */
public abstract class Enemy extends Node {
    
    // Propiedades comunes
    protected float health;
    protected float maxHealth;
    protected float speed;
    protected float size;
    protected float coreDamage;
    
    // Componentes visuales
    protected Geometry enemyGeometry;
    protected Material enemyMaterial;
    
    // Control de IA
    protected EnemyControl enemyControl;
    
    // Estados
    protected boolean isDead = false;
    protected boolean hasReachedCore = false;
    
    /**
     * Constructor base para enemigos
     * 
     * @param assetManager AssetManager para crear materiales
     * @param health Salud del enemigo
     * @param speed Velocidad de movimiento
     * @param size Tamaño del enemigo
     * @param coreDamage Daño que hace al núcleo
     */
    public Enemy(AssetManager assetManager, float health, float speed, float size, float coreDamage) {
        super("Enemy");
        
        this.maxHealth = health;
        this.health = health;
        this.speed = speed;
        this.size = size;
        this.coreDamage = coreDamage;
        
        // Crear geometría visual
        createEnemyGeometry(assetManager);
        
        // NO crear control aquí - se hará después de la inicialización completa
        // createEnemyControl();
        
        // Posición inicial (será establecida por EnemyManager)
        this.setLocalTranslation(0, 0, 0);
    }
    
    /**
     * Crea la geometría visual del enemigo
     * Puede ser sobrescrito por subclases para geometrías específicas
     */
    protected void createEnemyGeometry(AssetManager assetManager) {
        // Crear esfera básica
        Sphere sphere = new Sphere(16, 16, size);
        enemyGeometry = new Geometry("EnemyGeometry", sphere);
        
        // Material base - será personalizado por subclases
        enemyMaterial = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        enemyMaterial.setColor("Color", getEnemyColor());
        enemyMaterial.setColor("GlowColor", getEnemyColor());
        
        enemyGeometry.setMaterial(enemyMaterial);
        this.attachChild(enemyGeometry);
    }
    
    /**
     * Método abstracto para crear el control específico del enemigo
     * Debe ser implementado por cada subclase
     */
    protected abstract void createEnemyControl();
    
    /**
     * Método abstracto para obtener el color específico del enemigo
     * Debe ser implementado por cada subclase
     */
    protected abstract ColorRGBA getEnemyColor();
    
    /**
     * Aplica daño al enemigo
     * 
     * @param damage Cantidad de daño a aplicar
     * @return true si el enemigo murió, false en caso contrario
     */
    public boolean takeDamage(float damage) {
        if (isDead) return false;
        
        health -= damage;
        health = Math.max(0, health);
        
        // Actualizar color basado en salud
        updateHealthVisuals();
        
        if (health <= 0) {
            isDead = true;
            onDeath();
            return true;
        }
        
        return false;
    }
    
    /**
     * Actualiza los visuales basados en el porcentaje de salud
     */
    protected void updateHealthVisuals() {
        float healthPercentage = health / maxHealth;
        ColorRGBA baseColor = getEnemyColor();
        
        // Hacer más oscuro conforme pierde salud
        ColorRGBA currentColor = new ColorRGBA(
            baseColor.r * healthPercentage,
            baseColor.g * healthPercentage,
            baseColor.b * healthPercentage,
            baseColor.a
        );
        
        enemyMaterial.setColor("Color", currentColor);
    }
    
    /**
     * Método llamado cuando el enemigo muere
     * Puede ser sobrescrito por subclases para efectos específicos
     */
    protected void onDeath() {
        // Marcar para eliminación
        this.setUserData("destroy", true);
        
        // TODO: Efectos de muerte (partículas, sonido, etc.)
    }
    
    /**
     * Método llamado cuando el enemigo alcanza el núcleo
     */
    public void reachCore() {
        hasReachedCore = true;
        this.setUserData("destroy", true);
        
        // TODO: Efectos de impacto en el núcleo
    }
    
    /**
     * Resetea el enemigo para reutilización (pool de objetos)
     * 
     * @param position Nueva posición inicial
     */
    public void reset(Vector3f position) {
        this.health = this.maxHealth;
        this.isDead = false;
        this.hasReachedCore = false;
        
        this.setLocalTranslation(position);
        this.setUserData("destroy", false);
        
        // Restaurar color original
        enemyMaterial.setColor("Color", getEnemyColor());
        enemyMaterial.setColor("GlowColor", getEnemyColor());
        
        // Reiniciar control
        if (enemyControl != null) {
            enemyControl.setEnabled(true);
            enemyControl.reset();
        }
    }
    
    // Getters
    public float getHealth() { return health; }
    public float getMaxHealth() { return maxHealth; }
    public float getSpeed() { return speed; }
    public float getSize() { return size; }
    public float getCoreDamage() { return coreDamage; }
    public boolean isDead() { return isDead; }
    public boolean hasReachedCore() { return hasReachedCore; }
    
    /**
     * Verifica si el enemigo debe ser destruido
     */
    public boolean shouldDestroy() {
        Boolean destroy = this.getUserData("destroy");
        return destroy != null && destroy;
    }
    
    /**
     * Obtiene el control del enemigo
     */
    public EnemyControl getControl() {
        return enemyControl;
    }
} 