package mygame.config;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Versión simplificada del ConfigLoader que usa Properties en lugar de JSON
 * No requiere dependencias externas
 * 
 * @author Alberto Villalpando
 */
public class ConfigLoader {
    
    private static final Logger logger = Logger.getLogger(ConfigLoader.class.getName());
    
    /**
     * Carga la configuración desde un archivo properties
     * 
     * @return GameConfig con los parámetros del juego
     */
    public static GameConfig load() {
        try (InputStream is = ConfigLoader.class.getResourceAsStream("/game.properties")) {
            if (is == null) {
                logger.log(Level.WARNING, "game.properties no encontrado, usando configuración por defecto");
                return createDefaultConfig();
            }
            
            Properties props = new Properties();
            props.load(is);
            
            return parseProperties(props);
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error al cargar game.properties: " + e.getMessage(), e);
            return createDefaultConfig();
        }
    }
    
    /**
     * Parsea las propiedades a GameConfig
     */
    private static GameConfig parseProperties(Properties props) {
        GameConfig config = new GameConfig();
        
        // Resolución
        config.resolution = new GameConfig.Resolution();
        config.resolution.width = Integer.parseInt(props.getProperty("resolution.width", "1920"));
        config.resolution.height = Integer.parseInt(props.getProperty("resolution.height", "1080"));
        
        // Core
        config.core = new GameConfig.CoreConfig();
        config.core.health = Integer.parseInt(props.getProperty("core.health", "100"));
        config.core.size = Float.parseFloat(props.getProperty("core.size", "1.5"));
        
        // Player
        config.player = new GameConfig.EntityConfig();
        config.player.speed = Float.parseFloat(props.getProperty("player.speed", "12.0"));
        config.player.size = Float.parseFloat(props.getProperty("player.size", "1.0"));
        
        // Bullet
        config.bullet = new GameConfig.EntityConfig();
        config.bullet.speed = Float.parseFloat(props.getProperty("bullet.speed", "25.0"));
        config.bullet.lifetime = Float.parseFloat(props.getProperty("bullet.lifetime", "2.0"));
        config.bullet.size = Float.parseFloat(props.getProperty("bullet.size", "0.2"));
        
        // Enemy
        config.enemy = new GameConfig.EntityConfig();
        config.enemy.speed = Float.parseFloat(props.getProperty("enemy.speed", "5.0"));
        config.enemy.size = Float.parseFloat(props.getProperty("enemy.size", "1.0"));
        config.enemy.spawnInterval = Float.parseFloat(props.getProperty("enemy.spawnInterval", "2.0"));
        config.enemy.maxOnScreen = Integer.parseInt(props.getProperty("enemy.maxOnScreen", "20"));
        
        return config;
    }
    
    /**
     * Crea una configuración por defecto
     */
    private static GameConfig createDefaultConfig() {
        GameConfig config = new GameConfig();
        
        // Resolución por defecto
        config.resolution = new GameConfig.Resolution();
        config.resolution.width = 1920;
        config.resolution.height = 1080;
        
        // Configuración del núcleo
        config.core = new GameConfig.CoreConfig();
        config.core.health = 100;
        config.core.size = 1.5f;
        
        // Configuración del jugador
        config.player = new GameConfig.EntityConfig();
        config.player.speed = 12.0f;
        config.player.size = 1.0f;
        
        // Configuración de balas
        config.bullet = new GameConfig.EntityConfig();
        config.bullet.speed = 25.0f;
        config.bullet.lifetime = 2.0f;
        config.bullet.size = 0.2f;
        
        // Configuración de enemigos
        config.enemy = new GameConfig.EntityConfig();
        config.enemy.speed = 5.0f;
        config.enemy.size = 1.0f;
        config.enemy.spawnInterval = 2.0f;
        config.enemy.maxOnScreen = 20;
        
        return config;
    }
}