from pathlib import Path

root = Path(__file__).parent
secondary = root / "app/src/main/java/com/cie/app/SecondaryScreens076.kt"
build = root / "app/build.gradle.kts"

secondary_text = secondary.read_text(encoding="utf-8")
build_text = build.read_text(encoding="utf-8")

if 'CIE Shield 0.8.1' not in secondary_text:
    raise SystemExit("CIE Shield 0.8.1 UI version label missing")
if 'versionCode = 19' not in build_text or 'versionName = "0.8.1"' not in build_text:
    raise SystemExit("CIE Shield 0.8.1 Gradle version missing")

print("Validated CIE Shield 0.8.1 version label and Gradle version")
