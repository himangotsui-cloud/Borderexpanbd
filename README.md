# BorderExpand

A Paper plugin (Minecraft 1.21.1) that expands the world border every time
a player discovers a **brand-new** block or item for the first time ever on
the server — whether it comes from mining, picking something up, crafting,
smelting, or fishing. Already-discovered materials never trigger it again.

## Building the plugin

This is a normal Maven project. Because it needs to download the Paper API
from PaperMC's repository, you need to build it somewhere with internet
access (I can't reach the internet from here to compile it for you).

1. Install [Java 21+](https://adoptium.net/) and [Maven](https://maven.apache.org/download.cgi).
2. Open a terminal in this `borderexpand/` folder.
3. Run:
   ```
   mvn clean package
   ```
4. The compiled plugin will appear at:
   ```
   target/borderexpand-1.0.0.jar
   ```

Alternatively, just open the folder in IntelliJ IDEA (or any IDE with Maven
support) and run the `package` goal from the Maven side panel.

## Installing on your server

1. Drop `borderexpand-1.0.0.jar` into your Paper server's `plugins/` folder.
2. Restart (or `/reload confirm`, though a restart is safer).
3. A `plugins/BorderExpand/config.yml` and `discovered.yml` will be created.

## Targeting a different 1.21.x patch

If your server isn't exactly 1.21.1, open `pom.xml` and change:
```xml
<version>1.21.1-R0.1-SNAPSHOT</version>
```
to match your server's version, e.g. `1.21.4-R0.1-SNAPSHOT`. You can check
available versions at https://repo.papermc.io/service/rest/repository/browse/maven-public/io/papermc/paper/paper-api/

## Configuration (`config.yml`)

| Option | Default | Meaning |
|---|---|---|
| `border-expand-amount` | `10.0` | Blocks added to the border's size per discovery |
| `expand-duration-seconds` | `5` | Seconds the border takes to animate to its new size |
| `affected-worlds` | `[]` (empty = all worlds) | Restrict which worlds' borders grow |
| `track-mining` | `true` | Breaking a new block type counts |
| `track-pickup` | `true` | Picking up a new item entity counts |
| `track-crafting` | `true` | Crafting a new item counts |
| `track-smelting` | `true` | Smelting/smoking/blasting a new item counts |
| `track-fishing` | `true` | Fishing up a new item counts |
| `broadcast-message` | `true` | Announce discoveries server-wide |
| `discovery-message` | see file | `%player%` / `%material%` placeholders, `&` color codes |
| `discovery-sound` | `ENTITY_PLAYER_LEVELUP` | Sound played to everyone on discovery (blank to disable) |

## Commands

- `/borderexpand info` — shows how many things are discovered and each world's current border size
- `/borderexpand list` — lists every discovered material
- `/borderexpand reset` — wipes the discovery list (admin, `borderexpand.admin`, default op) — does **not** shrink the border back
- `/borderexpand add <MATERIAL>` — mark something as already-discovered without expanding the border (useful when seeding an existing world's inventory so old items don't "count" again)

## Notes / things worth knowing

- Discoveries are tracked **per server**, not per player — the first person
  to ever get a new material triggers it for everyone.
- The border only ever grows; nothing in this plugin shrinks it.
- If you want block *placing* (not just breaking) to also count, or want to
  scope discovery per-player instead of server-wide, let me know — that's a
  small change to `DiscoveryListener`/`DiscoveryManager`.
