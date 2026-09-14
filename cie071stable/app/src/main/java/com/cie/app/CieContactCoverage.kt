package com.cie.app

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun CieContactCoverageCard() {
    val context = LocalContext.current
    var granted by remember {
        mutableStateOf(context.checkSelfPermission(Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED)
    }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted = it }

    Cie076Card {
        Text(CieContactI18n.compose("title"), fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(6.dp))
        Text(CieContactI18n.compose("description"), color = Cie076Colors.Muted, lineHeight = 20.sp)
        Spacer(Modifier.height(12.dp))
        if (granted) {
            Surface(color = Color(0xFFEAF8F1), shape = RoundedCornerShape(99.dp)) {
                Text(CieContactI18n.compose("enabled"), Modifier.padding(horizontal = 12.dp, vertical = 7.dp), color = Cie076Colors.Green, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        } else {
            Cie076PrimaryButton(CieContactI18n.compose("enable"), true, Modifier.fillMaxWidth()) {
                launcher.launch(Manifest.permission.READ_CONTACTS)
            }
        }
        Spacer(Modifier.height(10.dp))
        Text(CieContactI18n.compose("privacy"), color = Cie076Colors.Muted, fontSize = 12.sp, lineHeight = 17.sp)
    }
}

internal object CieContactI18n {
    @Composable fun compose(key: String): String = text(LocalCieLanguage.current, key)
    fun text(language: CieLanguage, key: String): String = maps[language]?.get(key) ?: maps[CieLanguage.ENGLISH]?.get(key) ?: key

    private val maps = mapOf(
        CieLanguage.ENGLISH to mapOf("title" to "Contact call coverage", "description" to "Optional: let Android send calls from saved contacts through CIE Call Shield too.", "enable" to "Enable contact call coverage", "enabled" to "CONTACT CALL COVERAGE ON", "privacy" to "CIE does not query or upload your address book. This permission only expands Android call-screening coverage."),
        CieLanguage.DUTCH to mapOf("title" to "Bescherming voor contactoproepen", "description" to "Optioneel: laat Android ook oproepen van opgeslagen contacten door CIE Call Shield controleren.", "enable" to "Contactoproepen beschermen", "enabled" to "CONTACTOPROEPEN BESCHERMD", "privacy" to "CIE raadpleegt of uploadt je adresboek niet. De toestemming wordt alleen gebruikt om Android-oproepscreening uit te breiden."),
        CieLanguage.TURKISH to mapOf("title" to "Kişi aramaları için koruma", "description" to "İsteğe bağlı: Android'in kayıtlı kişilerden gelen aramaları da CIE Call Shield üzerinden kontrol etmesine izin verin.", "enable" to "Kişi aramalarını koru", "enabled" to "KİŞİ ARAMALARI KORUMADA", "privacy" to "CIE rehberinizi sorgulamaz veya yüklemez. İzin yalnızca Android arama tarama kapsamını genişletmek için kullanılır."),
        CieLanguage.GERMAN to mapOf("title" to "Schutz für Kontaktanrufe", "description" to "Optional: Lass Android auch Anrufe gespeicherter Kontakte durch CIE Call Shield prüfen.", "enable" to "Kontaktanrufe schützen", "enabled" to "KONTAKTANRUFE GESCHÜTZT", "privacy" to "CIE fragt dein Adressbuch nicht ab und lädt es nicht hoch. Die Berechtigung erweitert nur die Android-Anrufprüfung."),
        CieLanguage.PORTUGUESE to mapOf("title" to "Proteção de chamadas dos contactos", "description" to "Opcional: permita que o Android também envie chamadas de contactos guardados para o CIE Call Shield.", "enable" to "Proteger chamadas dos contactos", "enabled" to "CHAMADAS DOS CONTACTOS PROTEGIDAS", "privacy" to "O CIE não consulta nem envia a sua lista de contactos. A permissão serve apenas para ampliar a triagem de chamadas do Android."),
        CieLanguage.ARABIC to mapOf("title" to "حماية مكالمات جهات الاتصال", "description" to "اختياري: اسمح لأندرويد بإرسال مكالمات جهات الاتصال المحفوظة إلى CIE Call Shield أيضاً.", "enable" to "تفعيل حماية مكالمات جهات الاتصال", "enabled" to "حماية مكالمات جهات الاتصال مفعلة", "privacy" to "لا يستعلم CIE عن دفتر جهات الاتصال ولا يرفعه. يُستخدم الإذن فقط لتوسيع فحص المكالمات في أندرويد.")
    )
}
