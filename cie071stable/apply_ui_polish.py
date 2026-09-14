from pathlib import Path

root = Path(__file__).parent


def replace_once(text: str, old: str, new: str, label: str) -> str:
    if old not in text:
        # Allow the script to be re-run on an already-patched checkout.
        if new in text:
            return text
        raise SystemExit(f"UI polish patch did not match: {label}")
    return text.replace(old, new, 1)


# ---------------------------------------------------------------------------
# Shield: put the exact same full-width banner outside the padded/scrolling
# content. This removes any possibility of parent padding changing its width.
# ---------------------------------------------------------------------------
main = root / "app/src/main/java/com/cie/app/MainActivity076.kt"
m = main.read_text(encoding="utf-8")

shield_old = '''    LazyColumn(\n        modifier = Modifier.fillMaxSize(),\n        contentPadding = PaddingValues(bottom = 20.dp),\n        verticalArrangement = Arrangement.spacedBy(16.dp)\n    ) {\n        item { Cie076Hero(callRole, loading, onSettings) }'''
shield_new = '''    Column(Modifier.fillMaxSize()) {\n        Cie076Hero(callRole, loading, onSettings)\n        LazyColumn(\n            modifier = Modifier.weight(1f),\n            contentPadding = PaddingValues(top = 16.dp, bottom = 20.dp),\n            verticalArrangement = Arrangement.spacedBy(16.dp)\n        ) {'''
m = replace_once(m, shield_old, shield_new, "Shield banner container")

shield_end_old = '''        }\n    }\n}\n\nprivate fun cie076HasCallRole'''
shield_end_new = '''        }\n        }\n    }\n}\n\nprivate fun cie076HasCallRole'''
m = replace_once(m, shield_end_old, shield_end_new, "Shield outer column close")
main.write_text(m, encoding="utf-8")


# ---------------------------------------------------------------------------
# Blocked / Activity / Settings / Report: one full-width banner, then a padded
# LazyColumn below it. No screenWidthDp, negative offset or fixed-width hacks.
# ---------------------------------------------------------------------------
secondary = root / "app/src/main/java/com/cie/app/SecondaryScreens076.kt"
s = secondary.read_text(encoding="utf-8")
s = s.replace("import androidx.compose.ui.platform.LocalConfiguration\n", "")

blocked_old = '''    LazyColumn(\n        modifier = Modifier.fillMaxSize().statusBarsPadding(),\n        contentPadding = PaddingValues(20.dp, 10.dp, 20.dp, 20.dp),\n        verticalArrangement = Arrangement.spacedBy(14.dp)\n    ) {\n        item { Cie076TopBar(onSettings) }\n        item {\n            Text("Blocked", fontSize = 34.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.8).sp)\n            Spacer(Modifier.height(6.dp))\n            Text("Block a verified company across its known calls and marketing identities.", color = Cie076Colors.Muted, lineHeight = 21.sp)\n        }'''
blocked_new = '''    Column(Modifier.fillMaxSize()) {\n        Cie076PageHero("Blocked", onSettings)\n        LazyColumn(\n            modifier = Modifier.weight(1f),\n            contentPadding = PaddingValues(20.dp, 14.dp, 20.dp, 20.dp),\n            verticalArrangement = Arrangement.spacedBy(14.dp)\n        ) {\n            item {\n                Text("Block a verified company across its known calls and marketing identities.", color = Cie076Colors.Muted, lineHeight = 21.sp)\n            }'''
s = replace_once(s, blocked_old, blocked_new, "Blocked full-width banner")

blocked_end_old = '''        item { TextButton(onClick = onReport, modifier = Modifier.fillMaxWidth()) { Text("Report sender") } }\n    }\n}\n\n@Composable\ninternal fun Cie076ActivityScreen'''
blocked_end_new = '''        item { TextButton(onClick = onReport, modifier = Modifier.fillMaxWidth()) { Text("Report sender") } }\n        }\n    }\n}\n\n@Composable\ninternal fun Cie076ActivityScreen'''
s = replace_once(s, blocked_end_old, blocked_end_new, "Blocked outer column close")

activity_old = '''    LazyColumn(\n        modifier = Modifier.fillMaxSize().statusBarsPadding(),\n        contentPadding = PaddingValues(20.dp, 10.dp, 20.dp, 20.dp),\n        verticalArrangement = Arrangement.spacedBy(14.dp)\n    ) {\n        item { Cie076TopBar(onSettings) }\n        item {\n            Text("Activity", fontSize = 34.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.8).sp)\n            Spacer(Modifier.height(6.dp))\n            Text("What CIE allowed or blocked on this phone.", color = Cie076Colors.Muted, lineHeight = 21.sp)\n        }'''
activity_new = '''    Column(Modifier.fillMaxSize()) {\n        Cie076PageHero("Activity", onSettings)\n        LazyColumn(\n            modifier = Modifier.weight(1f),\n            contentPadding = PaddingValues(20.dp, 14.dp, 20.dp, 20.dp),\n            verticalArrangement = Arrangement.spacedBy(14.dp)\n        ) {\n            item {\n                Text("What CIE allowed or blocked on this phone.", color = Cie076Colors.Muted, lineHeight = 21.sp)\n            }'''
s = replace_once(s, activity_old, activity_new, "Activity full-width banner")

activity_end_old = '''        if (events.isNotEmpty()) item {\n            TextButton(onClick = onClear, modifier = Modifier.fillMaxWidth()) { Text("Clear local activity") }\n        }\n    }\n}\n\n@Composable\ninternal fun Cie076SettingsScreen'''
activity_end_new = '''        if (events.isNotEmpty()) item {\n            TextButton(onClick = onClear, modifier = Modifier.fillMaxWidth()) { Text("Clear local activity") }\n        }\n        }\n    }\n}\n\n@Composable\ninternal fun Cie076SettingsScreen'''
s = replace_once(s, activity_end_old, activity_end_new, "Activity outer column close")

settings_old = '''    LazyColumn(\n        modifier = Modifier.fillMaxSize().safeDrawingPadding(),\n        contentPadding = PaddingValues(20.dp, 10.dp, 20.dp, 36.dp),\n        verticalArrangement = Arrangement.spacedBy(14.dp)\n    ) {\n        item { Cie076BackBar("Settings", onBack) }'''
settings_new = '''    Column(Modifier.fillMaxSize()) {\n        Cie076PageHero("Settings", onBack)\n        LazyColumn(\n            modifier = Modifier.weight(1f),\n            contentPadding = PaddingValues(20.dp, 14.dp, 20.dp, 36.dp),\n            verticalArrangement = Arrangement.spacedBy(14.dp)\n        ) {'''
s = replace_once(s, settings_old, settings_new, "Settings full-width banner")

settings_end_old = '''        item {\n            Text("CIE Shield 0.7.9", color = Cie076Colors.Muted, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))\n        }\n    }\n}\n\n@Composable\ninternal fun Cie076ReportScreen'''
settings_end_new = '''        item {\n            Text("CIE Shield 0.7.9", color = Cie076Colors.Muted, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))\n        }\n        }\n    }\n}\n\n@Composable\ninternal fun Cie076ReportScreen'''
s = replace_once(s, settings_end_old, settings_end_new, "Settings outer column close")

report_old = '''    LazyColumn(\n        modifier = Modifier.fillMaxSize().safeDrawingPadding(),\n        contentPadding = PaddingValues(20.dp, 10.dp, 20.dp, 36.dp),\n        verticalArrangement = Arrangement.spacedBy(12.dp)\n    ) {\n        item { Cie076BackBar("Report sender", onBack) }'''
report_new = '''    Column(Modifier.fillMaxSize()) {\n        Cie076PageHero("Report sender", onBack)\n        LazyColumn(\n            modifier = Modifier.weight(1f),\n            contentPadding = PaddingValues(20.dp, 14.dp, 20.dp, 36.dp),\n            verticalArrangement = Arrangement.spacedBy(12.dp)\n        ) {'''
s = replace_once(s, report_old, report_new, "Report full-width banner")

report_end_old = '''        item {\n            Cie076PrimaryButton(if (loading) "Submitting…" else "Submit report", !loading && value.isNotBlank(), Modifier.fillMaxWidth()) {\n                onSubmit(type, value.trim(), category.trim().ifBlank { "spam" }, reason.trim())\n            }\n        }\n    }\n}\n'''
report_end_new = '''        item {\n            Cie076PrimaryButton(if (loading) "Submitting…" else "Submit report", !loading && value.isNotBlank(), Modifier.fillMaxWidth()) {\n                onSubmit(type, value.trim(), category.trim().ifBlank { "spam" }, reason.trim())\n            }\n        }\n        }\n    }\n}\n'''
s = replace_once(s, report_end_old, report_end_new, "Report outer column close")

secondary.write_text(s, encoding="utf-8")


# ---------------------------------------------------------------------------
# Message Shield Lab: same banner, full width, with padded scrolling content
# below. Again: no width/offset hack.
# ---------------------------------------------------------------------------
lab = root / "app/src/main/java/com/cie/app/MessageShieldLabActivity.kt"
l = lab.read_text(encoding="utf-8")
l = l.replace("import androidx.compose.ui.platform.LocalConfiguration\n", "")

lab_outer_old = '''        Box(\n            Modifier.fillMaxSize().background(LabColors.Background).safeDrawingPadding()\n        ) {\n            Column(\n                Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),\n                verticalArrangement = Arrangement.spacedBy(14.dp)\n            ) {\n                Row(verticalAlignment = Alignment.CenterVertically) {\n                    Column(Modifier.weight(1f)) {\n                        Text("Message Shield Lab", fontSize = 30.sp, fontWeight = FontWeight.Bold)\n                        Text("CIE 0.7.5 Identity Network", color = LabColors.Muted, fontSize = 13.sp)\n                    }\n                    TextButton(onClick = { activity.finish() }) { Text("Close") }\n                }'''
lab_outer_new = '''        Column(\n            Modifier.fillMaxSize().background(LabColors.Background)\n        ) {\n            Cie076PageHero("Message Shield Lab") { activity.finish() }\n            Column(\n                Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(24.dp),\n                verticalArrangement = Arrangement.spacedBy(14.dp)\n            ) {'''
l = replace_once(l, lab_outer_old, lab_outer_new, "Message Shield Lab full-width banner")
lab.write_text(l, encoding="utf-8")

print("Applied CIE Shield 0.7.9 full-width unified banner without offset/width hacks")
