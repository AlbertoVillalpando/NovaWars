package mygame.entities;

import com.jme3.asset.AssetManager;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import mygame.config.GameConfig;
import mygame.controls.EnemyControl;

/**
 * Enemigo básico para testing del sistema.
 * Se mueve directamente hacia el núcleo sin patrones complejos.
 * 
 * @author Alberto Villalpando
 */
public class BasicEnemy extends Enemy {
    
    /**
     * Constructor del BasicEnemy
     * 
     * @param assetManager AssetManager para crear materiales
     * @param config Configuración del juego
     */
    public BasicEnemy(AssetManager assetManager, GameConfig config) {
        super(
            assetManager,
            40f, // Salud base
            config.getEnemySpeed(),
            config.getEnemySize(),
            config.getEnemyCoreDamage()
        );
        
        // BasicEnemy no tiene parámetros adicionales, crear control directamente
        createEnemyControl();
    }
    
    @Override
    protected void createEnemyControl() {
        // Crear un control básico que solo se mueva hacia el núcleo
        enemyControl = new BasicEnemyControl(this);
        this.addControl(enemyControl);
    }
    
    @Override
    protected ColorRGBA getEnemyColor() {
        // Color verde para enemigos básicos
        return new ColorRGBA(0.2f, 1.0f, 0.2f, 1.0f);
    }
    
    /**
     * Control básico que solo se mueve hacia el núcleo
     */
    private static class BasicEnemyControl extends EnemyControl {
        
        public BasicEnemyControl(Enemy enemy) {
            super(enemy);
        }
        
        @Override
        protected void updateMovement(float tpf) {
            // Simplemente moverse hacia el núcleo
            Vector3f directionToCore = getDirectionToCore();
            moveInDirection(directionToCore, enemy.getSpeed(), tpf);
        }
    }
} 