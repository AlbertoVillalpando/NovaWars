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
 * Entidad que representa un proyectil en el juego
 * Estilo visual neón para mantener coherencia con el arte del juego
 * 
 * @author Alberto Villalpando
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