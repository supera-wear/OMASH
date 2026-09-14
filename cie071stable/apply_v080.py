from pathlib import Path

root = Path(__file__).parent
secondary = root / "app/src/main/java/com/cie/app/SecondaryScreens076.kt"
build = root / "app/build.gradle.kts"

secondary_text = secondary.read_text(encoding="utf-8")
build_text = build.read_text(encoding="utf-8")

# Keep the checked-in Gradle file stable, but make the CI artifact itself 0.8.2.
build_text = build_text.replace('versionCode = 19', 'versionCode = 20')
build_text = build_text.replace('versionName = "0.8.1"', 'versionName = "0.8.2"')
build.write_text(build_text, encoding="utf-8")

# The older 0.8.1 privacy sentence says the build has no SMS permissions. That is
# no longer true once Message Shield Live is present, so replace it in the build
# with the localized on-device Message Shield explanation.
secondary_text = secondary_text.replace('cieText("privacy_desc")', 'CieSmsI18n.compose("live_desc")')
secondary.write_text(secondary_text, encoding="utf-8")

if 'CIE Shield 0.8.2' not in secondary_text:
    raise SystemExit("CIE Shield 0.8.2 UI version label missing")
if 'versionCode = 20' not in build_text or 'versionName = "0.8.2"' not in build_text:
    raise SystemExit("CIE Shield 0.8.2 Gradle version missing")
if 'CieSmsI18n.compose("live_desc")' not in secondary_text:
    raise SystemExit("CIE Shield 0.8.2 live privacy copy missing")

print("Prepared CIE Shield 0.8.2 Message Shield Live build")
