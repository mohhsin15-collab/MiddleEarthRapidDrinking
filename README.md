# Middle Earth MC — Rapid Hand Drinking 0.2.1

Forge **1.20.1 / 47.4.28**, Java **17**, Tough As Nails **9.2.0.171**.

## What this project is

A complete reconstructed source project based on the user's **shipped 0.1.0-beta JAR**, with
an integrated successful-sip animation patch. This is **not a clone of the GitHub repository**:
the GitHub contents were not available through the current connection.

## Game logic preserved from 0.1.0-beta

- Crouch, use empty main hand and right-click a **water source** within 4.5 blocks.
- Server checks `ThirstHelper.isThirstEnabled`, `canDrink(player, false)` and actual thirst below 20.
- `cooldown_ticks`, `thirst_per_sip`, `hydration_per_sip`, `dirty_water_chance`,
  `dirty_water_seconds` and `drink_sound` use **the same config keys and defaults**.
- Thirst restoration, TAN's `thirst` debuff and optional drinking sound remain server-side.

## Animation changes

- **Only accepted sips** send a visual notification to the drinking player.
- 12-tick first-person vanilla-skinned arm animation: reach down, lift towards mouth, lower.
- Supports left-handed primary arm.
- Rapid repeated sips don't reset the gesture on every input (cooldown is still 4 ticks).
- Punchy! `PunchyApi.disable(modId, BooleanSupplier)` is invoked reflectively, only if Punchy!
  is installed, so the scoop pose can be applied to the normal Forge `RenderArmEvent` pose.
- Removes `setSwingHand(true)` from the old click handler to avoid Punchy! interpreting
  a sip as a punch.
- No new gameplay mod dependencies: optional Punchy! requires no build-time jar.

## Build

1. Add the contents of this source project to your GitHub repository, **after backing up**
   your existing repository, or use it as a separate project/branch.
2. Java 17 is required. Run `gradle build` with Gradle 8.8 (or the existing project's
   `gradlew.bat build` if its wrapper supports ForgeGradle 6).
3. A GitHub Actions workflow is included under `.github/workflows/build.yml`.
4. Locate `build/libs/middleearth-rapid-drinking-0.2.1.jar` in the workflow artifact.
5. **Remove** `middleearth-rapid-drinking-0.1.0-beta.jar` before installing the v0.2.1 JAR.
   Never install both.
6. Keep Tough As Nails `enable_hand_drinking = false`.

## Important validation limits

This source has been checked against the old shipped **JAR bytecode**, the Forge 1.20.1
`RenderArmEvent` API docs and the Punchy! 3.1+ public compatibility API. The external Forge
and Tough As Nails dependencies cannot be downloaded here, so **it has not been compiled or
play-tested in this environment**. If the GitHub build flags a Forge mapping or type mismatch,
use the workflow failure log to adjust it rather than installing an unverified JAR.

## Test after building

- Sips only animate when thirst is missing, water is a source, player crouches, and hand is empty.
- Try dirty water, cooldown, full thirst, right-clicking non-water and nonempty hand.
- Test a 4-tick sip cadence; the arm should play a smooth continuous gesture.
- Check Punchy! enabled, Carry On rendering when lifting blocks, and left-hand mode.
- Test without Punchy! installed and on a dedicated Forge server.
