from pathlib import Path

root = Path(__file__).parent
secondary = root / "app/src/main/java/com/cie/app/SecondaryScreens076.kt"
build = root / "app/build.gradle.kts"

secondary_text = secondary.read_text(encoding="utf-8")
build_text = build.read_text(encoding="utf-8")

# The install-safe APK intentionally keeps Message Shield in local lab mode.
# Restricted SMS/default-handler components are not shipped in this sideload build.
live_block = '''                    Text(CieSmsI18n.compose("live_desc"), color = Cie076Colors.Muted, lineHeight = 20.sp)\n                    Spacer(Modifier.height(12.dp))\n                    Cie076PrimaryButton(CieSmsI18n.compose("live_title"), true, Modifier.fillMaxWidth()) {\n                        context.startActivity(Intent(context, MessageShieldLiveActivity::class.java))\n                    }\n                    Spacer(Modifier.height(8.dp))\n                    OutlinedButton(\n                        onClick = { context.startActivity(Intent(context, MessageShieldLabActivity::class.java)) },\n                        modifier = Modifier.fillMaxWidth(),\n                        shape = RoundedCornerShape(14.dp)\n                    ) { Text(cieText("open_message_lab")) }'''
lab_block = '''                    Text(cieText("privacy_desc"), color = Cie076Colors.Muted, lineHeight = 20.sp)\n                    Spacer(Modifier.height(12.dp))\n                    Cie076PrimaryButton(cieText("open_message_lab"), true, Modifier.fillMaxWidth()) {\n                        context.startActivity(Intent(context, MessageShieldLabActivity::class.java))\n                    }'''
if live_block in secondary_text:
    secondary_text = secondary_text.replace(live_block, lab_block, 1)

if 'CieContactCoverageCard()' not in secondary_text:
    marker = '''            item {\n                Cie076Card {\n                    Text(cieText("sync"), fontSize = 20.sp, fontWeight = FontWeight.Bold)'''
    replacement = '''            item { CieContactCoverageCard() }\n            item {\n                Cie076Card {\n                    Text(cieText("sync"), fontSize = 20.sp, fontWeight = FontWeight.Bold)'''
    if marker not in secondary_text:
        raise SystemExit("CIE contact coverage insertion point missing")
    secondary_text = secondary_text.replace(marker, replacement, 1)

for old in ('CIE Shield 0.8.2', 'CIE Shield 0.8.3', 'CIE Shield 0.8.4'):
    secondary_text = secondary_text.replace(old, 'CIE Shield 0.8.5')
secondary.write_text(secondary_text, encoding="utf-8")

if 'CIE Shield 0.8.5' not in secondary_text:
    raise SystemExit("CIE Shield 0.8.5 UI version label missing")
if 'versionCode = 23' not in build_text or 'versionName = "0.8.5"' not in build_text:
    raise SystemExit("CIE Shield 0.8.5 Gradle version missing")
if 'applicationId = "com.cie.app.safe085"' not in build_text:
    raise SystemExit("CIE Shield safe085 install-safe package missing")
if 'MessageShieldLiveActivity::class.java' in secondary_text:
    raise SystemExit("Install-safe settings still expose Message Shield Live")
if 'CieContactCoverageCard()' not in secondary_text:
    raise SystemExit("CIE contact-call coverage card missing")

print("Prepared CIE Shield 0.8.5 install-safe build")
