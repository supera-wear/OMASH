from pathlib import Path

root = Path(__file__).parent
main = root / "app/src/main/java/com/cie/app/MainActivity076.kt"
secondary = root / "app/src/main/java/com/cie/app/SecondaryScreens076.kt"
lab = root / "app/src/main/java/com/cie/app/MessageShieldLabActivity.kt"

checks = {
    main: ["Column(Modifier.fillMaxSize())", "Cie076Hero(callRole, loading, onSettings)"],
    secondary: ["Cie076PageHero(cieText(\"blocked\")", "Cie076PageHero(cieText(\"activity\")", "Cie076PageHero(cieText(\"settings\")"],
    lab: ["Cie076PageHero(cieText(\"message_lab\")"],
}

for path, required in checks.items():
    text = path.read_text(encoding="utf-8")
    for needle in required:
        if needle not in text:
            raise SystemExit(f"CIE UI validation failed: {path.name}: {needle}")
    if "screenWidthDp" in text or "offset(x = (-20).dp)" in text:
        raise SystemExit(f"Legacy banner width hack found in {path.name}")

print("Validated CIE Shield 0.8.1 full-width unified banner source")
