# v0.2.1 crash fix (Minecraft 1.20.1 Forge)

## Crash addressed
v0.2.0 called `PlayerRenderer.renderRightHand()` or `renderLeftHand()` from inside `RenderArmEvent`. Those methods fire the same event, so the renderer could recursively re-enter the callback during a sip. The crash report exposed a Forge EventBus `LinkageError` while handling an exception, hiding the original cause.

## Fix
`src/main/java/com/middleearthmc/rapiddrinking/animation/ClientDrinkAnimation.java` now modifies the provided `PoseStack` without calling a player arm rendering method or cancelling the event. The normal renderer then draws the transformed arm.

The v0.2.0 networking compilation fix (`PacketDistributor.PLAYER.with(() -> player)`) is retained.

## Build
Replace the repository's source files with this archive's contents; GitHub Actions builds `middleearth-rapid-drinking-0.2.1.jar` and publishes the similarly named artifact.

## Important
Not compiled or game-tested here; build with GitHub Actions. For an immediate safe rollback, use `middleearth-rapid-drinking-0.1.0-beta.jar` until v0.2.1 proves stable. Only one Rapid Drinking JAR belongs in `mods`.

## First test
Test bare-hand drinking with Punchy and Punchy Moves on your test world. If the game still crashes, provide the new crash report and `logs/latest.log` from the same session, because EventBus error handling can hide the original exception.
