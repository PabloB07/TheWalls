# TheWalls (Paper 1.21)

Plugin de minijuego estilo The Walls con multiarena, lobby avanzado, eventos y scoreboard moderno.

**Resumen rapido**
- Paper 1.21, Java 21.
- Multiarena con lobby por arena.
- Scoreboard con FastBoard.
- Menus con SpiGUI.
- Hologramas de lobby con HologramLib (opcional, externo).

**Requisitos**
- Paper 1.21.
- Java 21.
- Recomendado: HologramLib + PacketEvents (para hologramas de lobby).

**Instalacion**
1. Compila o descarga el jar.
2. Copia el jar en `plugins/`.
3. Inicia el servidor.
4. Configura los archivos en `plugins/TheWalls/`.

**Comandos**
```
/wstart [size] [prepTime] [borderCloseTime] [borderCloseSpeed] [eventCooldown]
/wend
/wforceteam <player> <team>
/wleaderboard
/wevents
/wreload
/walls
/walls list
/walls join <arena>
/walls leave
/walls team <red|blue|yellow|green>
/walls lobby set <arena>
/walls lobby tp <arena>
/walls sign add <arena>
```

**Permisos**
- `thewalls.start`
- `thewalls.end`
- `thewalls.forceteam`
- `thewalls.reload`
- `thewalls.leaderboard`
- `thewalls.events`
- `thewalls.walls.list`
- `thewalls.walls.join`
- `thewalls.walls.leave`
- `thewalls.walls.team`
- `thewalls.walls.lobby`
- `thewalls.walls.sign`

**Archivos de configuracion**
- `config.yml`: reglas del juego, eventos, equipos, lobby (minPlayers, countdown, items).
- `messages.yml`: todos los textos y formatos (MiniMessage o legacy `&`).
- `lobbies.yml`: ubicacion de lobby por arena.
- `leaderboard.yml`: estadisticas de victorias/derrotas.

**Como funciona el plugin**
1. El servidor crea una arena principal llamada `main`.
2. Los jugadores se asignan a una arena con `/walls join <arena>` o por cartel.
3. Si la arena tiene lobby configurado, el jugador se teletransporta ahi y recibe items de lobby.
4. Cuando hay suficientes jugadores, empieza un conteo. Al terminar, se inicia la partida.
5. El juego crea equipos, coloca muros, ejecuta eventos y mantiene el scoreboard.
6. Al finalizar, el mundo se restaura segun la configuracion.

**Lobby avanzado**
- Items de lobby para seleccionar equipo y salir.
- Protecciones en lobby (no PVP, no romper/colocar, sin drop).
- Scoreboard del lobby con estado y conteo.
- Holograma de lobby por arena si HologramLib esta instalado.

**Carteles (join signs)**
- Coloca un cartel con:
  - Linea 1: `[TheWalls]`
  - Linea 2: `join`
  - Linea 3: `<arena>`
- Luego ejecuta `/walls sign add <arena>` mirando el cartel.
- El cartel se actualiza automaticamente con estado y jugadores.

**Menus (GUI)**
- Selector de arena y selector de equipo con SpiGUI.
- Se abren desde `/walls` o los items del lobby.

**Scoreboard**
- FastBoard para rendimiento y soporte moderno.
- Scoreboard de lobby y de partida.

**Eventos**
- Varios eventos configurables (TNT, supply chest, reveal, blind snail, bombing run, boss man, etc).
- Todos controlados desde `config.yml`.

**Notas**
- Los hologramas requieren HologramLib + PacketEvents como plugins externos.
- Si actualizas desde una version vieja, los lobbies se migran automaticamente a `lobbies.yml`.

**Build**
```
mvn -DskipTests package
```
