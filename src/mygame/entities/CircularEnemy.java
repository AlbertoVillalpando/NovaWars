package mygame.entities;

import com.jme3.asset.AssetManager;
import com.jme3.math.ColorRGBA;
import mygame.config.GameConfig;
import mygame.controls.CircularEnemyControl;

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
    protected void createEnemyControl() {
        enemyControl = new CircularEnemyControl(this, orbitRadius, orbitSpeed);
        this.addControl(enemyControl);
    }
    
    @Override
    protected ColorRGBA getEnemyColor() {
        // Color azul para enemigos circulares
        return new ColorRGBA(0.2f, 0.6f, 1.0f, 1.0f);
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