from pathlib import Path
import base64
import hashlib

root = Path(__file__).parent
secondary = root / "app/src/main/java/com/cie/app/SecondaryScreens076.kt"
build = root / "app/build.gradle.kts"
icon_b64 = root / "cie-app-icon-original.webp.b64"
icon_out = root / "app/src/main/res/drawable/cie_shield_app_icon_original.webp"

secondary_text = secondary.read_text(encoding="utf-8")
build_text = build.read_text(encoding="utf-8")

if 'CieContactCoverageCard()' not in secondary_text:
    marker = '''            item {\n                Cie076Card {\n                    Text(cieText("sync"), fontSize = 20.sp, fontWeight = FontWeight.Bold)'''
    replacement = '''            item { CieContactCoverageCard() }\n            item {\n                Cie076Card {\n                    Text(cieText("sync"), fontSize = 20.sp, fontWeight = FontWeight.Bold)'''
    if marker not in secondary_text:
        raise SystemExit("CIE contact coverage insertion point missing")
    secondary_text = secondary_text.replace(marker, replacement, 1)

for old in ('CIE Shield 0.8.1', 'CIE Shield 0.8.2', 'CIE Shield 0.8.3', 'CIE Shield 0.8.5'):
    secondary_text = secondary_text.replace(old, 'CIE Shield 0.8.4')
secondary.write_text(secondary_text, encoding="utf-8")

raw_icon = base64.b64decode(icon_b64.read_text(encoding="utf-8").strip(), validate=True)
icon_out.write_bytes(raw_icon)
icon_hash = hashlib.sha256(raw_icon).hexdigest()
if icon_hash != '324056415986c25b3aafd274b7416353e7de3d5574d05f80ebb8e44e9a469bc9':
    raise SystemExit(f"Original CIE icon hash mismatch: {icon_hash}")

if 'CIE Shield 0.8.4' not in secondary_text:
    raise SystemExit("CIE Shield 0.8.4 UI version label missing")
if 'versionCode = 22' not in build_text or 'versionName = "0.8.4"' not in build_text:
    raise SystemExit("CIE Shield 0.8.4 Gradle version missing")
if 'applicationId = "com.cie.app.stable084"' not in build_text:
    raise SystemExit("CIE Shield stable084 package missing")
if 'CieContactCoverageCard()' not in secondary_text:
    raise SystemExit("CIE contact-call coverage card missing")

print("Prepared CIE Shield 0.8.4 with original app icon and compatibility signing")
