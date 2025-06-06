package mygame.config;


/**
 * Configuración centralizada del juego NovaWars - Sistema de parámetros ajustables.
 * 
 * <p>GameConfig actúa como un almacén central de todos los parámetros configurables
 * del juego, proporcionando valores por defecto robustos y métodos de conveniencia
 * para acceso fácil desde cualquier parte del sistema.</p>
 * 
 * <h3>Categorías de configuración:</h3>
 * <ul>
 *   <li><strong>Resolution:</strong> Configuración de pantalla y renderizado</li>
 *   <li><strong>Core:</strong> Parámetros del núcleo central (vida, tamaño)</li>
 *   <li><strong>Player:</strong> Configuración del jugador (velocidad, tamaño)</li>
 *   <li><strong>Bullet:</strong> Propiedades de proyectiles (velocidad, vida, tamaño)</li>
 *   <li><strong>Enemy:</strong> Configuración base de enemigos</li>
 * </ul>
 * 
 * <h3>Sistema de configuración especializada:</h3>
 * <ul>
 *   <li><strong>CircularEnemy:</strong> Parámetros de movimiento orbital</li>
 *   <li><strong>ZigZagEnemy:</strong> Configuración de patrón serpenteante</li>
 *   <li><strong>Wave system:</strong> Escalado de oleadas y dificultad</li>
 *   <li><strong>Combat balance:</strong> Daño, cooldowns y límites</li>
 * </ul>
 * 
 * <h3>Patrón de diseño aplicado:</h3>
 * <ul>
 *   <li><strong>Fallback seguro:</strong> Valores por defecto para propiedades null</li>
 *   <li><strong>Getters de conveniencia:</strong> Acceso directo sin verificación manual</li>
 *   <li><strong>Configuración jerárquica:</strong> Clases anidadas para organización</li>
 *   <li><strong>Extensibilidad:</strong> Fácil adición de nuevos parámetros</li>
 * </ul>
 * 
 * <h3>Cargas de configuración:</h3>
 * <ul>
 *   <li><strong>Archivo properties:</strong> game.properties en classpath</li>
 *   <li><strong>Valores por defecto:</strong> Configuración hardcoded de respaldo</li>
 *   <li><strong>Carga dinámica:</strong> ConfigLoader maneja la deserialización</li>
 *   <li><strong>Error handling:</strong> Graceful fallback ante errores de carga</li>
 * </ul>
 * 
 * <h3>Balance de gameplay:</h3>
 * <ul>
 *   <li><strong>Escalado progresivo:</strong> Oleadas más difíciles con el tiempo</li>
 *   <li><strong>Especialización de enemigos:</strong> Cada tipo con características únicas</li>
 *   <li><strong>Pooling optimization:</strong> Límites para rendimiento óptimo</li>
 *   <li><strong>Combat pacing:</strong> Cooldowns y intervalos balanceados</li>
 * </ul>
 * 
 * <h3>Integración con sistemas:</h3>
 * <ul>
 *   <li><strong>ConfigLoader:</strong> Sistema de carga desde archivo</li>
 *   <li><strong>Main:</strong> Configuración inicial de aplicación</li>
 *   <li><strong>GameState:</strong> Parámetros de entidades y sistemas</li>
 *   <li><strong>EnemyManager:</strong> Configuración de oleadas y spawning</li>
 * </ul>
 * 
 * @author Alberto Villalpando
 * @version 1.0
 * @see ConfigLoader
 * @see Main#loadGameConfig()
 * @since 2024
 */
public class GameConfig {
    
    public Resolution resolution;
    public CoreConfig core;
    public EntityConfig player;
    public EntityConfig bullet;
    public EntityConfig enemy;
    
    // Getters de conveniencia para Core
    public float getCoreHealth() { return core != null ? core.health : 100f; }
    public float getCoreSize() { return core != null ? core.size : 2f; }
    
    // Getters de conveniencia para Player
    public float getPlayerSpeed() { return player != null ? player.speed : 15f; }
    public float getPlayerSize() { return player != null ? player.size : 1f; }
    
    // Getters de conveniencia para Bullet
    public float getBulletSpeed() { return bullet != null ? bullet.speed : 30f; }
    public float getBulletSize() { return bullet != null ? bullet.size : 0.2f; }
    public float getBulletLifetime() { return bullet != null ? bullet.lifetime : 3f; }
    public int getBulletMaxPool() { return 50; } // Valor por defecto
    public float getBulletCooldown() { return 0.1f; } // 100ms entre disparos
    
    // Getters de conveniencia para Enemy
    public float getEnemySpeed() { return enemy != null ? enemy.speed : 10f; }
    public float getEnemySize() { return enemy != null ? enemy.size : 0.8f; }
    public float getEnemySpawnInterval() { return enemy != null ? enemy.spawnInterval : 2f; }
    public int getEnemyMaxOnScreen() { return enemy != null ? enemy.maxOnScreen : 10; }
    
    // Getters específicos para enemigos CircularEnemy
    public float getCircularEnemySpeed() { 
        float result = getEnemySpeed() * 0.8f;
        System.out.println("DEBUG: getCircularEnemySpeed() = " + result + " (base: " + getEnemySpeed() + ")");
        return result;
    }
    
    public float getCircularEnemyRadius() { 
        float result = 8f;
        System.out.println("DEBUG: getCircularEnemyRadius() = " + result);
        return result;
    }
    
    public float getCircularEnemyOrbitSpeed() { 
        float result = 1.5f;
        System.out.println("DEBUG: getCircularEnemyOrbitSpeed() = " + result);
        return result;
    }
    
    // Getters específicos para enemigos ZigZagEnemy  
    public float getZigZagEnemySpeed() { 
        float result = getEnemySpeed() * 1.2f;
        System.out.println("DEBUG: getZigZagEnemySpeed() = " + result + " (base: " + getEnemySpeed() + ")");
        return result;
    }
    
    public float getZigZagAmplitude() { 
        float result = 3f;
        System.out.println("DEBUG: getZigZagAmplitude() = " + result);
        return result;
    }
    
    public float getZigZagFrequency() { 
        float result = 2f;
        System.out.println("DEBUG: getZigZagFrequency() = " + result);
        return result;
    }
    
    // Configuración de oleadas
    public int getBaseWaveSize() { return 3; } // Enemigos en la primera oleada
    public float getWaveScaling() { return 1.5f; } // Factor de escalamiento por oleada
    public float getWaveInterval() { return 15f; } // Segundos entre oleadas
    public float getEnemyCoreDamage() { return 0.1f; } // 10% de daño al núcleo

    /**
     * Configuración de resolución de pantalla
     */
    public static class Resolution {
        public int width;
        public int height;
    }
    
    /**
     * Configuración específica del núcleo central
     */
    public static class CoreConfig {
        public int health;
        public float size;
    }
    
    /**
     * Configuración genérica para entidades del juego
     * Usada para jugador, balas y enemigos
     */
    public static class EntityConfig {
        public float speed;
        public float size;
        public float lifetime;      // Solo usado por bullets
        public float spawnInterval; // Solo usado por enemies
        public int maxOnScreen;     // Solo usado por enemies
    }
}