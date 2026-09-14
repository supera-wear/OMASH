from pathlib import Path

root = Path(__file__).parent
secondary = root / "app/src/main/java/com/cie/app/SecondaryScreens076.kt"
text = secondary.read_text(encoding="utf-8")
text = text.replace('Text("CIE Shield 0.7.9"', 'Text("CIE Shield 0.8.0"')
secondary.write_text(text, encoding="utf-8")

print("Applied CIE Shield 0.8.0 version label")
