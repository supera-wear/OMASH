from pathlib import Path

p = Path(__file__).parent / "app/src/main/java/com/cie/app/SecondaryScreens076.kt"
s = p.read_text(encoding="utf-8")

s = s.replace(
    "import androidx.compose.ui.platform.LocalContext\n",
    "import androidx.compose.ui.platform.LocalConfiguration\nimport androidx.compose.ui.platform.LocalContext\n",
    1,
)

blocked_header = '''        item { Cie076TopBar(onSettings) }\n        item {\n            Text("Blocked", fontSize = 34.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.8).sp)\n            Spacer(Modifier.height(6.dp))\n            Text("Block a verified company across its known calls and marketing identities.", color = Cie076Colors.Muted, lineHeight = 21.sp)\n        }'''
blocked_replacement = '''        item {\n            val w = LocalConfiguration.current.screenWidthDp.dp\n            Box(Modifier.offset(x = (-20).dp).width(w)) { Cie076PageHero("Blocked", onSettings) }\n        }\n        item {\n            Text("Block a verified company across its known calls and marketing identities.", color = Cie076Colors.Muted, lineHeight = 21.sp)\n        }'''
s = s.replace(blocked_header, blocked_replacement, 1)

activity_header = '''        item { Cie076TopBar(onSettings) }\n        item {\n            Text("Activity", fontSize = 34.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.8).sp)\n            Spacer(Modifier.height(6.dp))\n            Text("What CIE allowed or blocked on this phone.", color = Cie076Colors.Muted, lineHeight = 21.sp)\n        }'''
activity_replacement = '''        item {\n            val w = LocalConfiguration.current.screenWidthDp.dp\n            Box(Modifier.offset(x = (-20).dp).width(w)) { Cie076PageHero("Activity", onSettings) }\n        }\n        item {\n            Text("What CIE allowed or blocked on this phone.", color = Cie076Colors.Muted, lineHeight = 21.sp)\n        }'''
s = s.replace(activity_header, activity_replacement, 1)

if "Cie076PageHero(\"Blocked\"" not in s or "Cie076PageHero(\"Activity\"" not in s:
    raise SystemExit("UI polish patch did not match expected source")

p.write_text(s, encoding="utf-8")
print("Applied 0.7.9 unified banner patch")
