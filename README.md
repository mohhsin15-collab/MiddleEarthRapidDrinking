# Middle Earth MC — Rapid Hand Drinking (Forge 1.20.1)

**Status: Source code, not an installable compiled JAR.** The Forge/CurseMaven dependencies were unavailable for downloading in the current runtime. The project has been packaged and files can be inspected, but a ForgeGradle compilation and real in-game test are still required. Do not place this source ZIP in `mods`.

## Goal
- Sneak + repeated right-click with an empty main hand while looking at a still/source water block (reach 4.5 blocks)
- Each valid click consumes one sip; minimum 4-tick cooldown (up to 5 sips/second)
- Restores 1 TAN thirst point per sip, hydration modifier 0.1
- Retains an optional dirty-water Thirst status effect penalty (default 25%, duration 20 seconds)
- Server validates water, range, thirst, crouching and cooldown; duplicate clicks cannot grant unlimited hydration
- Client sends only an empty request packet, no trusted target position or thirst parameters

## Prerequisite: prevent duplicate drinking
In Middle Earth MC, edit **`config/toughasnails/thirst.toml`** and set:

```toml
enable_hand_drinking = false
```

That disables the *built-in* slow interaction; this addon provides the new fast interaction. Keep `enable_thirst = true`.

## Build (Windows / Linux, with internet)
Requires Java **17**, Gradle **8.8**, ForgeGradle artifacts, and access to CurseMaven (TAN dependency).

1. Extract this project.
2. Run `gradle build` in its root directory.
3. Find the compiled, reobfuscated JAR in `build/libs/`.
4. Install the produced JAR on both clients and dedicated servers alongside the original Tough As Nails version.

Alternatively, upload this project to a GitHub repository and run **Actions > Build Forge addon > Run workflow**. Download the built JAR as an action artifact.

## Testing checklist
1. Back up a world. Load the game and confirm no mod-loading errors.
2. With TAN hand-drinking disabled, crouch and click a source-water surface with an empty hand.
3. Verify 1 thirst point gained per click, maximum of 1 click per 4 ticks. Spam click should not produce more.
4. Verify no drinking at full thirst, too far away, without crouching, or while holding an item.
5. Verify natural water sometimes applies TAN's dirty-water Thirst effect, and no status effect is applied when its chance is configured to 0.
6. Verify normal bucket use, block placement, and item use remain unaffected.
7. Verify multiplayer client/server both have the addon and sync thirst changes correctly.
8. Confirm actual water effect registry ID with `/effect give @s toughasnails:thirst` on a copied test world (may differ depending on build).

## Config
After a successful launch the addon creates `config/middleearthrapidwater-common.toml` with `cooldown_ticks`, `thirst_per_sip`, `hydration_per_sip`, `dirty_water_chance`, `dirty_water_seconds` and `drink_sound`.

## Notes
- The TAN API (`ThirstHelper`, `IThirst.drink`) was confirmed in the public 1.20.4 source (the 1.20.1 Forge version is a backport). API compatibility **still requires compilation against 9.2.0.171**.
- The implementation has not been compiled or runtime-tested. The client input hook and water raycast may need small Forge-specific adjustments based on build errors or game behaviour.
- Untreated natural water is assumed dirty. The addon does not currently recognise another mod's purified source blocks.
- No original Tough As Nails files or other third-party mod code are redistributed.
