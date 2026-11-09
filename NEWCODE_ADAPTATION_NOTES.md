# Interium 0.5.7: Newcode / Delta assessment

Newcode 2.09 source archive targets Minecraft 1.16.5 and includes a large Minecraft source/assets tree plus native Discord libraries. The uploaded archive is not republished and its binaries/build scripts were not executed.

## Useful ideas implemented as 1.21.11 adaptations
- Newcode Aura mode names: Grim, SpookyTime. Delta Aura names: ФанТайм, ФанТайм ФОВ, Легит. These remain selectable, with bounded mode-specific rotation policies. This is not a verbatim/full donor event-engine transplant or a verified anticheat bypass.
- Sliders support dragging and mouse-wheel increments, named mode controls and animated toggles. Aura and movement use the same framework as visual settings.
- Flight: Motion, Glide, OnlyHit with independent XZ/Y parameters. Entity Speed follows the supplied collision-count impulse and nearest-XZ-target idea using one spatially bounded query instead of two full world iterations.
- Delta AuraHandler target motifs: orbiting crystals (Сферы/Тест) and an eased vertical ring (Круг), adapted to immutable extraction and batched rendering. The old immediate-mode shader/bloom backend is not copied; not a pixel-identical renderer port.
- Local Windows Discord IPC is opt-in and requires an application ID owned by the user, not an account token. It sends only client/game/version information, never the server address or player name. No donor application ID or DLL is reused. Real Discord integration has not been validated in CI.
- New original prism/I logo is generated during the build and used on the title screen, mod metadata and window icon. This does not modify the launcher executable icon.

## Avoid copying blindly
- Newcode contains animation expressions such as sin(0 / ...), which are constant rather than animated.
- Its displayed sorting mode strings and switch labels are inconsistent in places; its routines are not automatically correct just because they are donor code.
- Its Discord updater is an unbounded thread that ignores interruptions, uses a donor application ID and donor links. The replacement uses an interruptible daemon, closes local IPC on disable and backs off when Discord is unavailable.
- Old OpenGL immediate rendering and Minecraft 1.16.5 field patches do not map directly to 1.21.11 extraction/render separation.
- No general redistribution license was identified in the initial archive inventory. No legal clearance is asserted for donor assets/code; the archive, bundled Minecraft assets and native libraries are not imported into this repository.

## Honest limitations
- Mineblaze buttons apply conservative experimental parameter presets. No actual Mineblaze session or anticheat acceptance test has been performed. Names do not constitute working bypass claims.
- 1.8 mode changes client attack cadence to configurable CPS, bypassing the local 1.9+ cooldown gate; a server remains authoritative over damage, invulnerability timing and PvP rules. It cannot convert a modern server into 1.8 combat.
- General Minecraft performance controls include opt-in distance culling for non-player mobs/dropped items and explicit reversible video options. Players/current target are exempt from that culling. Interium world-visual range is separately bounded. Existing Sodium/Iris/Lithium are not replaced. No FPS increase on user hardware is measured or promised.
- Video options change only when the user clicks the labelled buttons. Previous values are saved for restoration. Installing the JAR does not silently alter options.txt.
- WardenESP remains removed and the previous null-world-state guard is preserved. Existing binds retain their indices; Target ESP is appended.
- CI numerical/GUI smoke checks are not world, server, modpack, real Discord or hardware-FPS tests.

## Possible later work
Newcode's Ghost/Nova target visuals, conditional settings layouts and more sophisticated target transitions are candidates for separate performance-budgeted work. They are not silently presented as already implemented.
