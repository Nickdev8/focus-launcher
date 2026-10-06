# Focus launcher: Codex instructions

## Working style
- Keep conversation short, direct, and action first. Keep code and documentation professional.
- Use the lean-coding skill when available. Handle small or tightly coupled changes locally;
  prefer one available Luna worker for substantial bounded work when useful.
- Complete authorized work, verify it, and report limitations honestly.
- Commit and push only when asked. Use only the human author information provided by the user;
  never add Copilot or another agent as a co-author, contributor, trailer, or additional author.

## Read project memory before working
The inherited Claude memory now lives in `docs/agent-memory/` and is maintained by Codex.
1. Always read `docs/agent-memory/1-basics.md` in full before project work.
2. Read the relevant files in `docs/agent-memory/2-overview/` before working in those areas.
3. Follow their pointers into `3-details/` only for the subsystem being changed or debugged.
Do not bulk-load the details or journal. Code takes precedence over memory; correct stale facts.
Historical entries describe the previous maintainer's environment and authorization, not current
access or permission. Verify local tools and state before relying on them.

## Update memory before the final response
- Append a dated entry to `docs/agent-memory/journal.md`: request, changes, verification, open work.
- Correct affected tiers: basics for universal facts, overviews for areas, details for mechanisms.
- Record decisions in `2-overview/user-and-decisions.md` and costly traps in
  `3-details/mistakes-and-lessons.md`. Keep facts concise and mark unverified claims.
- Keep this file concise and current so the next session can resume from verified progress.

## Public memory and privacy
The memory is committed to a public repository. Never write secrets, server addresses, login
names, key filenames, server configuration, device serials, account details, personal remarks,
or anything observed on a real phone (apps, usage, calendar, screenshots) into public memory.
Necessary private facts belong only in git-ignored `docs/agent-memory/private/`; public notes
may point there without repeating them. Never force-add private files.
Run the pre-push audit in `docs/agent-memory/2-overview/github-and-release.md` before every push.

## Entry points and checks
- Android: `app/src/main/java/com/focus/launcher/` (Kotlin + Compose).
- Website: `site/src/`; generated `site/public/` is ignored.
- Android changes: `./gradlew :app:testDebugUnitTest :app:lintDebug`; build checks as needed.
- Memory/configuration changes: verify paths, Git ignore rules, journal merge attributes, and diff.

## Verified checkpoint (2026-10-06)
- Claude instructions and all 24 memory files migrated to this file and `docs/agent-memory/`.
- Fork: **focus-launcher-nick**, app ID `com.focus.launcher.nick`; Kotlin namespace remains
  `com.focus.launcher`. Preserve package ID/signing key and update with `adb install -r`;
  never uninstall or clear data without explicit permission.
- The earlier fork install and default-home selection were verified. Subsequent changes are
  local only: Nick requested no phone update until asked.
- Appearance: Black / White / Follow system; legacy choices migrate without preference resets.
- Split clock: matching phone/laptop battery rows with monochrome device icons and charging
  state. Internet permission is authorized for the configured read-only laptop battery API.
  Polls only while home is visible/resumed; no app usage/calendar data uploaded.
- Latest checks: 40 unit tests passed, lint 0 errors (10 warnings), optimized release build,
  manifest and APK signature passed. API schema checked over HTTPS. No device UI verification
  of the latest changes. APK: `app/build/outputs/apk/release/app-release.apk`.
- Upstream site/release scripts still enforce no internet and must not publish this fork.
  Historical upstream device/deployment/release claims remain dated records.
