package mygame.entities;

import com.jme3.asset.AssetManager;
import com.jme3.math.ColorRGBA;
import mygame.config.GameConfig;
import mygame.controls.ZigZagEnemyControl;

/**
 * Enemigo que se mueve en patrón zig-zag hacia el núcleo.
 * Se mueve directamente hacia el núcleo pero con movimiento horizontal
 * sinusoidal que crea un patrón de zig-zag.
 * 
 * @author Alberto Villalpando
 */
public class ZigZagEnemy extends Enemy {
    
    private float zigZagAmplitude;
    private float zigZagFrequency;
    
    /**
     * Constructor del ZigZagEnemy
     * 
     * @param assetManager AssetManager para crear materiales
     * @param config Configuración del juego
     */
    public ZigZagEnemy(AssetManager assetManager, GameConfig config) {
        super(
            assetManager,
            30f, // Salud base (menor que CircularEnemy, más agresivo)
            config.getZigZagEnemySpeed(),
            config.getEnemySize(),
            config.getEnemyCoreDamage()
        );
        
        this.zigZagAmplitude = config.getZigZagAmplitude();
        this.zigZagFrequency = config.getZigZagFrequency();
        
        System.out.println("ZigZagEnemy constructor - zigZagAmplitude: " + this.zigZagAmplitude + ", zigZagFrequency: " + this.zigZagFrequency);
        
        // Ahora crear el control con los valores correctamente inicializados
        createEnemyControl();
    }
    
    @Override
    protected void createEnemyControl() {
        enemyControl = new ZigZagEnemyControl(this, zigZagAmplitude, zigZagFrequency);
        this.addControl(enemyControl);
    }
    
    @Override
    protected ColorRGBA getEnemyColor() {
        // Color rojo para enemigos zig-zag (más agresivos)
        return new ColorRGBA(1.0f, 0.3f, 0.2f, 1.0f);
    }
    
    /**
     * Obtiene la amplitud del zig-zag
     * 
     * @return Amplitud del zig-zag
     */
    public float getZigZagAmplitude() {
        return zigZagAmplitude;
    }
    
    /**
     * Obtiene la frecuencia del zig-zag
     * 
     * @return Frecuencia del zig-zag
     */
    public float getZigZagFrequency() {
        return zigZagFrequency;
    }
    
    @Override
    public void reset(com.jme3.math.Vector3f position) {
        super.reset(position);
        
        // Reiniciar parámetros específicos si es necesario
        if (enemyControl instanceof ZigZagEnemyControl) {
            ZigZagEnemyControl control = (ZigZagEnemyControl) enemyControl;
            control.resetZigZag();
        }
    }
} 