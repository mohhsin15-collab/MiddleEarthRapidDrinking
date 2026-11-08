# v0.1.0-beta → v0.2.0 source changes

The original installed JAR was inspected with `javap`.

- RapidDrinking constructor: registers `RapidDrinkAnimationNetwork`.
- `RapidDrinking.sip(ServerPlayer)`: sends success notification **after** thirst changes,
  dirty-water penalty, and sound; no notification on rejection.
- `ClientEvents.onUse`: now uses `event.setSwingHand(false)` instead of `true` so Punchy!
  doesn't play an attack/punch swing when the user right-clicks a water source.
- New `ClientDrinkAnimation`, `RapidDrinkAnimationNetwork`,
  `PunchyAnimationCompatibility` in the `animation` subpackage.
- Existing mod ID and existing configuration keys preserved.
