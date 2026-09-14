from pathlib import Path

root = Path(__file__).parent
secondary = root / "app/src/main/java/com/cie/app/SecondaryScreens076.kt"
s = secondary.read_text(encoding="utf-8")

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

settings_shell = '''    LazyColumn(\n        modifier = Modifier.fillMaxSize().safeDrawingPadding(),\n        contentPadding = PaddingValues(20.dp, 10.dp, 20.dp, 36.dp),\n        verticalArrangement = Arrangement.spacedBy(14.dp)\n    ) {\n        item { Cie076BackBar("Settings", onBack) }'''
settings_replacement = '''    LazyColumn(\n        modifier = Modifier.fillMaxSize(),\n        contentPadding = PaddingValues(20.dp, 0.dp, 20.dp, 36.dp),\n        verticalArrangement = Arrangement.spacedBy(14.dp)\n    ) {\n        item {\n            val w = LocalConfiguration.current.screenWidthDp.dp\n            Box(Modifier.offset(x = (-20).dp).width(w)) { Cie076PageHero("Settings", onBack) }\n        }'''
s = s.replace(settings_shell, settings_replacement, 1)

report_shell = '''    LazyColumn(\n        modifier = Modifier.fillMaxSize().safeDrawingPadding(),\n        contentPadding = PaddingValues(20.dp, 10.dp, 20.dp, 36.dp),\n        verticalArrangement = Arrangement.spacedBy(12.dp)\n    ) {\n        item { Cie076BackBar("Report sender", onBack) }'''
report_replacement = '''    LazyColumn(\n        modifier = Modifier.fillMaxSize(),\n        contentPadding = PaddingValues(20.dp, 0.dp, 20.dp, 36.dp),\n        verticalArrangement = Arrangement.spacedBy(12.dp)\n    ) {\n        item {\n            val w = LocalConfiguration.current.screenWidthDp.dp\n            Box(Modifier.offset(x = (-20).dp).width(w)) { Cie076PageHero("Report sender", onBack) }\n        }'''
s = s.replace(report_shell, report_replacement, 1)

required_secondary = [
    'Cie076PageHero("Blocked"',
    'Cie076PageHero("Activity"',
    'Cie076PageHero("Settings"',
    'Cie076PageHero("Report sender"',
]
if any(token not in s for token in required_secondary):
    raise SystemExit("Unified banner patch did not match all SecondaryScreens pages")
secondary.write_text(s, encoding="utf-8")

lab = root / "app/src/main/java/com/cie/app/MessageShieldLabActivity.kt"
l = lab.read_text(encoding="utf-8")
l = l.replace(
    "import androidx.compose.ui.Modifier\n",
    "import androidx.compose.ui.Modifier\nimport androidx.compose.ui.platform.LocalConfiguration\n",
    1,
)
l = l.replace(
    "Modifier.fillMaxSize().background(LabColors.Background).safeDrawingPadding()",
    "Modifier.fillMaxSize().background(LabColors.Background)",
    1,
)
l = l.replace(
    "Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp)",
    "Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp).padding(bottom = 24.dp)",
    1,
)
lab_header = '''                Row(verticalAlignment = Alignment.CenterVertically) {\n                    Column(Modifier.weight(1f)) {\n                        Text("Message Shield Lab", fontSize = 30.sp, fontWeight = FontWeight.Bold)\n                        Text("CIE 0.7.5 Identity Network", color = LabColors.Muted, fontSize = 13.sp)\n                    }\n                    TextButton(onClick = { activity.finish() }) { Text("Close") }\n                }'''
lab_replacement = '''                Box(\n                    Modifier\n                        .offset(x = (-24).dp)\n                        .width(LocalConfiguration.current.screenWidthDp.dp)\n                ) {\n                    Cie076PageHero("Message Shield Lab") { activity.finish() }\n                }'''
l = l.replace(lab_header, lab_replacement, 1)
if 'Cie076PageHero("Message Shield Lab"' not in l:
    raise SystemExit("Unified banner patch did not match Message Shield Lab")
lab.write_text(l, encoding="utf-8")

print("Applied CIE Shield 0.7.9 unified banner to Shield, Blocked, Activity, Settings, Report and Message Shield Lab")
