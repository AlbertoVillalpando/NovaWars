package mygame.entities;

import com.jme3.app.Application;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;
import mygame.controls.PlayerControl;

/**
 * Entidad que representa al jugador en el juego
 * Encapsula la geometría, material y control del jugador
 * 
 * @author Alberto Villalpando
 */
public class Player {
    
    private Node playerNode;
    private Geometry playerGeometry;
    private PlayerControl playerControl;
    private Application app;
    
    /**
     * Constructor del jugador
     * 
     * @param app Referencia a la aplicación para acceder a AssetManager
     * @param size Tamaño del jugador
     * @param speed Velocidad de movimiento
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
        
        // Posición inicial en el centro
        playerNode.setLocalTranslation(0, 0, 0);
    }
    
    /**
     * Crea la geometría visual del jugador
     * Por ahora es un cubo azul, pero puede ser reemplazado por un modelo 3D
     * 
     * @param size Tamaño del jugador
     */
    private void createPlayerGeometry(float size) {
        // Crear forma geométrica (cubo por ahora, puede ser cambiado a una nave)
        Box box = new Box(size * 0.5f, size * 0.2f, size * 0.5f);
        playerGeometry = new Geometry("PlayerGeometry", box);
        
        // Crear material con color neón
        Material mat = new Material(app.getAssetManager(), 
            "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", ColorRGBA.Cyan); // Color neón cian para el jugador
        
        // Agregar efecto de brillo (glow)
        mat.setColor("GlowColor", ColorRGBA.Cyan);
        
        playerGeometry.setMaterial(mat);
        
        // Adjuntar geometría al nodo
        playerNode.attachChild(playerGeometry);
        
        // Opcional: Agregar una pequeña marca para indicar la dirección frontal
        Box frontMarker = new Box(size * 0.1f, size * 0.1f, size * 0.3f);
        Geometry markerGeom = new Geometry("FrontMarker", frontMarker);
        Material markerMat = new Material(app.getAssetManager(),
            "Common/MatDefs/Misc/Unshaded.j3md");
        markerMat.setColor("Color", ColorRGBA.White);
        markerGeom.setMaterial(markerMat);
        // Cambiamos la posición del marcador para que apunte correctamente
        markerGeom.setLocalTranslation(0, 0, size * 0.7f);
        playerNode.attachChild(markerGeom);
    }
    
    /**
     * Obtiene el nodo del jugador para adjuntarlo a la escena
     * 
     * @return Node del jugador
     */
    public Node getNode() {
        return playerNode;
    }
    
    /**
     * Obtiene el control del jugador para manejar input
     * 
     * @return PlayerControl
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