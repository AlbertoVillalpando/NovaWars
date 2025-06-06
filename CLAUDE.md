# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

NovaWars is a twin-stick shooter game built with jMonkeyEngine 3.5.2 where the player defends a central core from waves of enemies. The project is a NetBeans-based Java application using Ant for build management.

## Build Commands

```bash
# Build the project
ant clean compile

# Run the game
ant run

# Build distributable JAR
ant jar

# Clean build artifacts
ant clean
```

## Architecture

### Core Components

- **Main.java**: Entry point that configures the jME3 application and initializes game states
- **GameState**: Central game state that manages all entities, input handling, and game logic
- **Entity System**: Player, Core, Enemy, and Bullet entities with corresponding Control classes for behavior
- **Manager System**: BulletPool for object pooling, EnemyManager for enemy spawning/management
- **Configuration**: JSON-based config system with GameConfig and ConfigLoader

### Package Structure

- `mygame.entities`: Game objects (Player, Core, Enemy variants, Bullet)
- `mygame.controls`: jME3 controls that define entity behaviors
- `mygame.states`: Application states (currently GameState)
- `mygame.managers`: Systems for managing groups of entities
- `mygame.config`: Configuration loading and data classes

### Key Dependencies

- jMonkeyEngine 3.5.2 (core game engine)
- Jackson 2.15.2 (JSON configuration parsing)
- Java 21 target/source

### Game Flow

GameState acts as the central coordinator, implementing CoreControl.CoreListener and EnemyManager.EnemyManagerListener to handle core damage and enemy events. The player is controlled via keyboard/mouse input processed in GameState, which also manages bullet spawning and collision detection.

## Development Notes

- Assets are stored in the `assets/` directory and compressed into assets.jar during build
- The main class is `mygame.Main` as defined in project.properties
- FlyByCamera is disabled in favor of custom input handling
- Configuration is loaded from JSON files via ConfigLoader