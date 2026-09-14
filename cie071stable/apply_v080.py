from pathlib import Path

root = Path(__file__).parent
secondary = root / "app/src/main/java/com/cie/app/SecondaryScreens076.kt"
build = root / "app/build.gradle.kts"

secondary_text = secondary.read_text(encoding="utf-8")
build_text = build.read_text(encoding="utf-8")

secondary_text = secondary_text.replace('cieText("privacy_desc")', 'CieSmsI18n.compose("live_desc")')

if 'CieContactCoverageCard()' not in secondary_text:
    marker = '''            item {\n                Cie076Card {\n                    Text(cieText("sync"), fontSize = 20.sp, fontWeight = FontWeight.Bold)'''
    replacement = '''            item { CieContactCoverageCard() }\n            item {\n                Cie076Card {\n                    Text(cieText("sync"), fontSize = 20.sp, fontWeight = FontWeight.Bold)'''
    if marker not in secondary_text:
        raise SystemExit("CIE contact coverage insertion point missing")
    secondary_text = secondary_text.replace(marker, replacement, 1)

secondary_text = secondary_text.replace('CIE Shield 0.8.2', 'CIE Shield 0.8.3')
secondary.write_text(secondary_text, encoding="utf-8")

if 'CIE Shield 0.8.3' not in secondary_text:
    raise SystemExit("CIE Shield 0.8.3 UI version label missing")
if 'versionCode = 21' not in build_text or 'versionName = "0.8.3"' not in build_text:
    raise SystemExit("CIE Shield 0.8.3 Gradle version missing")
if 'CieSmsI18n.compose("live_desc")' not in secondary_text:
    raise SystemExit("CIE live privacy copy missing")
if 'CieContactCoverageCard()' not in secondary_text:
    raise SystemExit("CIE contact-call coverage card missing")

print("Prepared CIE Shield 0.8.3 contact-call coverage UI")
