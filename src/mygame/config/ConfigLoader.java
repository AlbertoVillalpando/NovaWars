package mygame.config;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Sistema de carga de configuración - Deserialización robusta de parámetros del juego.
 * 
 * <p>ConfigLoader maneja la carga de configuración desde archivos properties,
 * proporcionando un sistema robusto con fallbacks seguros y manejo de errores
 * que garantiza que el juego siempre pueda iniciarse con configuración válida.</p>
 * 
 * <h3>Estrategia de carga:</h3>
 * <ul>
 *   <li><strong>Archivo principal:</strong> game.properties en classpath</li>
 *   <li><strong>Fallback automático:</strong> Configuración hardcoded si archivo falla</li>
 *   <li><strong>Parsing robusto:</strong> Valores por defecto para propiedades faltantes</li>
 *   <li><strong>Error handling:</strong> Logging detallado y recuperación graceful</li>
 * </ul>
 * 
 * <h3>Formato de configuración soportado:</h3>
 * <ul>
 *   <li><strong>Properties estándar:</strong> Formato key=value tradicional</li>
 *   <li><strong>Jerarquía por puntos:</strong> core.health, player.speed, etc.</li>
 *   <li><strong>Tipos primitivos:</strong> int, float, string automáticamente parseados</li>
 *   <li><strong>Sin dependencias:</strong> Solo Java estándar, sin librerías JSON</li>
 * </ul>
 * 
 * <h3>Configuración por defecto incluida:</h3>
 * <ul>
 *   <li><strong>Resolución:</strong> 1920x1080 (Full HD estándar)</li>
 *   <li><strong>Core:</strong> 100 vida, 1.5f tamaño</li>
 *   <li><strong>Player:</strong> 12.0f velocidad, 1.0f tamaño</li>
 *   <li><strong>Bullet:</strong> 25.0f velocidad, 2.0f lifetime, 0.2f tamaño</li>
 *   <li><strong>Enemy:</strong> 5.0f velocidad, 2.0f spawn interval, 20 max pantalla</li>
 * </ul>
 * 
 * <h3>Robustez y manejo de errores:</h3>
 * <ul>
 *   <li><strong>File not found:</strong> Log warning, usar defaults</li>
 *   <li><strong>Parse errors:</strong> Usar valor por defecto para propiedad específica</li>
 *   <li><strong>IO exceptions:</strong> Log error completo, fallback total</li>
 *   <li><strong>Guarantía de funcionamiento:</strong> Siempre retorna GameConfig válido</li>
 * </ul>
 * 
 * <h3>Diseño sin dependencias:</h3>
 * <ul>
 *   <li><strong>Solo Java estándar:</strong> Properties, InputStream, Logger</li>
 *   <li><strong>Sin Jackson/GSON:</strong> Evita dependencias externas pesadas</li>
 *   <li><strong>Parsing manual:</strong> Control total sobre conversión de tipos</li>
 *   <li><strong>Lightweight:</strong> Minimal overhead en startup</li>
 * </ul>
 * 
 * <h3>Integración con sistema:</h3>
 * <ul>
 *   <li><strong>Main.loadGameConfig():</strong> Punto de entrada único</li>
 *   <li><strong>Static factory:</strong> ConfigLoader.load() sin instanciación</li>
 *   <li><strong>Logging integrado:</strong> Información detallada de carga</li>
 * </ul>
 * 
 * @author Alberto Villalpando
 * @version 1.0
 * @see GameConfig
 * @see Main#loadGameConfig()
 * @since 2024
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