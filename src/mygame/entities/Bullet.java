package mygame.entities;


import com.jme3.app.Application;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Sphere;
import mygame.controls.BulletControl;

/**
 * Proyectil disparado por el jugador - Entidad de combate optimizada para pooling.
 * 
 * <p>Bullet representa los proyectiles que el jugador dispara hacia los enemigos.
 * Diseñada específicamente para ser utilizada con BulletPool, implementa
 * reutilización eficiente de objetos y limpieza automática de recursos.</p>
 * 
 * <h3>Características visuales:</h3>
 * <ul>
 *   <li><strong>Forma:</strong> Esfera pequeña y brillante</li>
 *   <li><strong>Color:</strong> Amarillo neón con efecto de brillo</li>
 *   <li><strong>Tamaño:</strong> Configurable desde GameConfig</li>
 *   <li><strong>Material:</strong> Shader unshaded para máximo brillo</li>
 * </ul>
 * 
 * <h3>Comportamiento de movimiento:</h3>
 * <ul>
 *   <li><strong>Dirección lineal:</strong> Se mueve en línea recta hacia el objetivo</li>
 *   <li><strong>Velocidad constante:</strong> Configurada desde GameConfig</li>
 *   <li><strong>Tiempo de vida:</strong> Auto-destrucción tras duración configurada</li>
 *   <li><strong>Colisiones:</strong> Se destruye al impactar enemigos</li>
 * </ul>
 * 
 * <h3>Sistema de pooling:</h3>
 * <ul>
 *   <li><strong>Reutilización:</strong> Las instancias se reciclan en lugar de crear nuevas</li>
 *   <li><strong>Reset eficiente:</strong> Restablece estado sin recrear objetos</li>
 *   <li><strong>Limpieza automática:</strong> Gestión de memoria optimizada</li>
 *   <li><strong>Control dinámico:</strong> BulletControl se recrea por disparo</li>
 * </ul>
 * 
 * <h3>Integración con sistemas:</h3>
 * <ul>
 *   <li><strong>BulletPool:</strong> Origen y gestión del ciclo de vida</li>
 *   <li><strong>BulletControl:</strong> Lógica de movimiento y auto-destrucción</li>
 *   <li><strong>GameState:</strong> Detección de colisiones con enemigos</li>
 *   <li><strong>Player:</strong> Punto de origen para nuevos disparos</li>
 * </ul>
 * 
 * <h3>Estados de vida:</h3>
 * <ul>
 *   <li><strong>Activa:</strong> Moviéndose hacia objetivo</li>
 *   <li><strong>Colisionó:</strong> Marcada para destrucción</li>
 *   <li><strong>Expiró:</strong> Tiempo de vida agotado</li>
 *   <li><strong>Pooled:</strong> Inactiva, esperando reutilización</li>
 * </ul>
 * 
 * @author Alberto Villalpando
 * @version 1.0
 * @see BulletPool
 * @see BulletControl
 * @see GameState#detectBulletEnemyCollisions()
 * @since 2024
 */
public class Bullet {
    
    private Node bulletNode;
    private Geometry bulletGeometry;
    private BulletControl bulletControl;
    private Application app;
    
    /**
     * Constructor de la bala
     * 
     * @param app Referencia a la aplicación
     * @param origin Posición inicial
     * @param direction Dirección de disparo
     * @param speed Velocidad del proyectil
     * @param size Tamaño del proyectil
     * @param lifetime Tiempo de vida en segundos
     */
    public Bullet(Application app, Vector3f origin, Vector3f direction, 
                  float speed, float size, float lifetime) {
        this.app = app;
        
        // Crear nodo contenedor
        bulletNode = new Node("Bullet");
        
        // Crear geometría de la bala
        createBulletGeometry(size);
        
        // Establecer posición inicial
        bulletNode.setLocalTranslation(origin);
        
        // Crear y adjuntar control
        bulletControl = new BulletControl(direction, speed, lifetime);
        bulletNode.addControl(bulletControl);
    }
    
    /**
     * Crea la geometría visual del proyectil
     * 
     * @param size Tamaño del proyectil
     */
    private void createBulletGeometry(float size) {
        // Usar esfera para el proyectil
        Sphere sphere = new Sphere(8, 8, size);
        bulletGeometry = new Geometry("BulletGeometry", sphere);
        
        // Material neón amarillo brillante
        Material mat = new Material(app.getAssetManager(),
            "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", ColorRGBA.Yellow);
        
        // Efecto de brillo
        mat.setColor("GlowColor", ColorRGBA.Yellow);
        
        bulletGeometry.setMaterial(mat);
        bulletNode.attachChild(bulletGeometry);
        
        // Opcional: Agregar trail o efecto de estela
        // Por ahora mantenemos simple
    }
    
    /**
     * Obtiene el nodo de la bala para adjuntarlo a la escena
     * 
     * @return Node de la bala
     */
    public Node getNode() {
        return bulletNode;
    }
    
    /**
     * Obtiene el control de la bala
     * 
     * @return BulletControl
     */
    public BulletControl getControl() {
        return bulletControl;
    }
    
    /**
     * Obtiene la posición actual de la bala
     * 
     * @return Vector3f con la posición
     */
    public Vector3f getPosition() {
        return bulletNode.getLocalTranslation();
    }
    
    /**
     * Obtiene el tamaño (radio) de la bala
     * 
     * @return float con el radio de la bala
     */
    public float getSize() {
        if (bulletGeometry != null && bulletGeometry.getMesh() instanceof Sphere) {
            Sphere sphere = (Sphere) bulletGeometry.getMesh();
            return sphere.getRadius();
        }
        return 0.2f; // Valor por defecto
    }
    
    /**
     * Verifica si la bala está marcada para destrucción
     * 
     * @return true si debe ser destruida
     */
    public boolean shouldDestroy() {
        Boolean destroy = bulletNode.getUserData("destroy");
        return destroy != null && destroy;
    }
    
    /**
     * Limpia recursos de la bala
     */
    public void cleanup() {
        bulletNode.removeFromParent();
    }
    
    /**
     * Actualiza la referencia del control de la bala
     * Usado al reciclar balas del pool
     * 
     * @param newControl Nuevo control a asignar
     */
    public void updateControl(BulletControl newControl) {
        this.bulletControl = newControl;
    }
    
    /**
     * Resetea la bala para reutilización
     * 
     * @param origin Nueva posición inicial
     * @param direction Nueva dirección
     * @param speed Nueva velocidad
     * @param lifetime Nuevo tiempo de vida
     */
    public void reset(Vector3f origin, Vector3f direction, float speed, float lifetime) {
        // Actualizar posición
        bulletNode.setLocalTranslation(origin);
        
        // Remover control anterior si existe
        if (bulletControl != null) {
            bulletNode.removeControl(bulletControl);
        }
        
        // Crear y agregar nuevo control
        bulletControl = new BulletControl(direction, speed, lifetime);
        bulletNode.addControl(bulletControl);
        
        // Limpiar flag de destrucción
        bulletNode.setUserData("destroy", false);
    }
}