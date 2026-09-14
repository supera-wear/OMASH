package com.cie.app

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import java.util.Locale

enum class CieLanguage(val code: String, val nativeName: String, val rtl: Boolean = false) {
    ENGLISH("en", "English"),
    DUTCH("nl", "Nederlands"),
    TURKISH("tr", "Türkçe"),
    GERMAN("de", "Deutsch"),
    PORTUGUESE("pt", "Português"),
    ARABIC("ar", "العربية", true)
}

object CieLanguageStore {
    const val SYSTEM = "system"
    private const val PREFS = "cie_language_preferences"
    private const val KEY = "language"

    fun selection(context: Context): String =
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY, SYSTEM) ?: SYSTEM

    fun setSelection(context: Context, value: String) {
        val safe = if (value == SYSTEM || CieLanguage.entries.any { it.code == value }) value else SYSTEM
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString(KEY, safe).apply()
    }

    fun resolve(selection: String, systemLanguage: String = Locale.getDefault().language): CieLanguage {
        val code = if (selection == SYSTEM) systemLanguage.lowercase(Locale.ROOT) else selection.lowercase(Locale.ROOT)
        return CieLanguage.entries.firstOrNull { it.code == code } ?: CieLanguage.ENGLISH
    }
}

val LocalCieLanguage = staticCompositionLocalOf { CieLanguage.ENGLISH }

@Composable
fun CieLocalizedContent(selection: String, content: @Composable () -> Unit) {
    val configuration = LocalConfiguration.current
    val systemLanguage = configuration.locales.get(0)?.language ?: Locale.getDefault().language
    val language = CieLanguageStore.resolve(selection, systemLanguage)
    CompositionLocalProvider(
        LocalCieLanguage provides language,
        LocalLayoutDirection provides if (language.rtl) LayoutDirection.Rtl else LayoutDirection.Ltr,
        content = content
    )
}

@Composable
internal fun cieText(key: String, vararg args: Any?): String =
    CieI18n.text(LocalCieLanguage.current, key, *args)

internal object CieI18n {
    fun text(language: CieLanguage, key: String, vararg args: Any?): String {
        var value = translations[language]?.get(key) ?: translations[CieLanguage.ENGLISH]?.get(key) ?: key
        args.forEachIndexed { index, arg -> value = value.replace("{$index}", arg?.toString().orEmpty()) }
        return value
    }

    private val translations: Map<CieLanguage, Map<String, String>> = mapOf(
        CieLanguage.ENGLISH to mapOf(
            "language" to "Language",
            "language_desc" to "Choose a language or let CIE follow your phone automatically.",
            "language_system" to "Automatic (system)",
            "language_current" to "Current language: {0}",
            "settings" to "Settings",
            "shield" to "Shield",
            "blocked" to "Blocked",
            "activity" to "Activity",
            "report_sender" to "Report sender",
            "back" to "Back",
            "verified" to "VERIFIED",
            "unknown_caller" to "Unknown caller",
            "allowed" to "Allowed",
            "blocked_state" to "Blocked",
            "calls" to "Calls",
            "cie_updating" to "CIE Shield is updating",
            "cie_ready" to "CIE Shield is ready",
            "cie_needs_setup" to "CIE Shield needs setup",
            "phone_protected" to "Your phone is protected",
            "enable_call_shield_setup" to "Enable Call Shield to finish setup",
            "protection_active" to "Protection active",
            "protection_needs_setup" to "Protection needs setup",
            "shield_description" to "CIE recognizes the company behind incoming business calls and applies your company-wide block policy.",
            "on_device_on" to "On-device call protection is on",
            "enable_android_screening" to "Enable Android call screening",
            "recent" to "Recent",
            "view_all" to "View all",
            "no_call_activity" to "No call activity yet",
            "activity_empty_hint" to "Blocked and recent calls will appear here.",
            "blocked_desc" to "Block a verified company across its known calls and marketing identities.",
            "search_companies" to "Search companies",
            "no_companies" to "No companies match your search",
            "activity_desc" to "What CIE allowed or blocked on this phone.",
            "no_activity" to "No activity yet",
            "clear_activity" to "Clear local activity",
            "local_protection_active" to "Local protection stays active",
            "company_protection_ready" to "Company identity protection is ready",
            "sync" to "Sync",
            "service_online" to "CIE service online",
            "local_data" to "Using local protection data",
            "status_not_checked" to "Service status not checked",
            "syncing" to "Syncing…",
            "sync_now" to "Sync now",
            "check_service" to "Check service",
            "message_shield" to "Message Shield",
            "message_shield_desc" to "Identity logic is active in the Message Lab. Real SMS interception remains reserved for the production SMS build.",
            "open_message_lab" to "Open Message Lab",
            "beta_test_lab" to "Beta Test Lab",
            "beta_test_desc" to "Use a second phone number to prove Call Shield end-to-end.",
            "second_phone" to "Second phone number",
            "arm_test" to "Arm test",
            "clear" to "Clear",
            "test_armed" to "Test number armed",
            "invalid_number" to "Invalid number",
            "test_cleared" to "Test cleared",
            "privacy_local" to "Privacy & local data",
            "privacy_desc" to "Call decisions use a local identity cache. Unknown or stale identities fail open. This build has no SMS, contacts or call-log permissions.",
            "cached_call_identities" to "{0} cached call identities",
            "help_improve" to "Help improve CIE",
            "help_improve_desc" to "Report an unwanted sender for review. A report never auto-blocks a company by itself.",
            "report_desc" to "Reports help CIE verify company identity. One report never auto-blocks anyone.",
            "phone" to "Phone",
            "sender_id" to "Sender ID",
            "short_code" to "Short code",
            "sender" to "Sender",
            "category" to "Category",
            "report_reason" to "Why are you reporting this?",
            "submitting" to "Submitting…",
            "submit_report" to "Submit report",
            "status_ready" to "CIE is ready",
            "status_up_to_date" to "CIE is up to date",
            "status_company_blocked" to "{0} is blocked",
            "status_company_allowed" to "{0} is allowed",
            "status_service_online" to "CIE service is online",
            "status_service_unreachable" to "CIE service is unreachable",
            "status_report_received" to "Report received",
            "error_no_internet" to "No internet connection. Local protection stays active.",
            "error_timeout" to "CIE took too long to respond. Local protection stays active.",
            "error_sync" to "Could not sync with CIE. Local protection stays active.",
            "message_lab" to "Message Shield Lab",
            "identity_ready" to "Identity Network ready",
            "identity_needs_sync" to "Identity Network needs sync",
            "identity_synced" to "Identity Network synced",
            "using_cached" to "Using cached identities",
            "identity_unavailable" to "Identity Network unavailable",
            "verified_cached" to "{0} verified identities cached locally",
            "sync_identity" to "Sync identity network",
            "permission_free" to "Permission-free simulation",
            "permission_free_desc" to "This lab does not read your SMS. Sender identities come from CIE's verified backend cache; message content stays on this phone.",
            "test_message" to "Test message",
            "sender_or_phone" to "Sender ID or phone number",
            "use_cached_identity" to "Use cached identity: {0}",
            "message" to "Message",
            "marketing" to "Marketing",
            "security" to "Security",
            "run_identity_test" to "Run identity test",
            "unknown_company" to "Unknown company",
            "no_company_match" to "No company ID match",
            "block" to "BLOCK",
            "allow" to "ALLOW",
            "sender_type" to "Sender type",
            "normalized" to "Normalized",
            "message_type" to "Message type",
            "identity_confidence" to "Identity confidence",
            "not_matched" to "Not matched",
            "company_policy" to "Company-wide policy",
            "company_policy_desc" to "Verified phone numbers, sender IDs and short codes can point to one company ID. Block the company once and every trusted identity inherits the same marketing policy.",
            "close" to "Close"
        ),
        CieLanguage.DUTCH to mapOf(
            "language" to "Taal", "language_desc" to "Kies een taal of laat CIE automatisch de taal van je telefoon volgen.", "language_system" to "Automatisch (systeem)", "language_current" to "Huidige taal: {0}",
            "settings" to "Instellingen", "shield" to "Shield", "blocked" to "Geblokkeerd", "activity" to "Activiteit", "report_sender" to "Afzender melden", "back" to "Terug", "verified" to "GEVERIFIEERD", "unknown_caller" to "Onbekende beller", "allowed" to "Toegestaan", "blocked_state" to "Geblokkeerd", "calls" to "Oproepen",
            "cie_updating" to "CIE Shield wordt bijgewerkt", "cie_ready" to "CIE Shield is gereed", "cie_needs_setup" to "CIE Shield moet worden ingesteld", "phone_protected" to "Je telefoon is beschermd", "enable_call_shield_setup" to "Schakel Call Shield in om de installatie af te ronden",
            "protection_active" to "Bescherming actief", "protection_needs_setup" to "Bescherming moet worden ingesteld", "shield_description" to "CIE herkent het bedrijf achter inkomende zakelijke oproepen en past je bedrijfsbrede blokkeerbeleid toe.", "on_device_on" to "Oproepbescherming op het toestel is actief", "enable_android_screening" to "Android-oproepscreening inschakelen", "recent" to "Recent", "view_all" to "Alles bekijken", "no_call_activity" to "Nog geen oproepactiviteit", "activity_empty_hint" to "Geblokkeerde en recente oproepen verschijnen hier.",
            "blocked_desc" to "Blokkeer een geverifieerd bedrijf voor bekende oproep- en marketingidentiteiten.", "search_companies" to "Bedrijven zoeken", "no_companies" to "Geen bedrijven gevonden", "activity_desc" to "Wat CIE op deze telefoon heeft toegestaan of geblokkeerd.", "no_activity" to "Nog geen activiteit", "clear_activity" to "Lokale activiteit wissen",
            "local_protection_active" to "Lokale bescherming blijft actief", "company_protection_ready" to "Bedrijfsidentiteitsbescherming is gereed", "sync" to "Synchronisatie", "service_online" to "CIE-service online", "local_data" to "Lokale beschermingsgegevens worden gebruikt", "status_not_checked" to "Servicestatus niet gecontroleerd", "syncing" to "Synchroniseren…", "sync_now" to "Nu synchroniseren", "check_service" to "Service controleren",
            "message_shield" to "Message Shield", "message_shield_desc" to "De identiteitslogica is actief in Message Lab. Echte sms-interceptie blijft gereserveerd voor de productie-sms-build.", "open_message_lab" to "Message Lab openen", "beta_test_lab" to "Beta Test Lab", "beta_test_desc" to "Gebruik een tweede telefoonnummer om Call Shield end-to-end te testen.", "second_phone" to "Tweede telefoonnummer", "arm_test" to "Test activeren", "clear" to "Wissen", "test_armed" to "Testnummer geactiveerd", "invalid_number" to "Ongeldig nummer", "test_cleared" to "Test gewist",
            "privacy_local" to "Privacy & lokale gegevens", "privacy_desc" to "Oproepbeslissingen gebruiken een lokale identiteitscache. Onbekende of verouderde identiteiten worden standaard doorgelaten. Deze build heeft geen sms-, contacten- of oproeplogrechten.", "cached_call_identities" to "{0} oproepidentiteiten in cache", "help_improve" to "Help CIE verbeteren", "help_improve_desc" to "Meld een ongewenste afzender ter beoordeling. Eén melding blokkeert nooit automatisch een bedrijf.",
            "report_desc" to "Meldingen helpen CIE bedrijfsidentiteiten te verifiëren. Eén melding blokkeert nooit automatisch iemand.", "phone" to "Telefoon", "sender_id" to "Afzender-ID", "short_code" to "Korte code", "sender" to "Afzender", "category" to "Categorie", "report_reason" to "Waarom meld je dit?", "submitting" to "Verzenden…", "submit_report" to "Melding verzenden",
            "status_ready" to "CIE is gereed", "status_up_to_date" to "CIE is bijgewerkt", "status_company_blocked" to "{0} is geblokkeerd", "status_company_allowed" to "{0} is toegestaan", "status_service_online" to "CIE-service is online", "status_service_unreachable" to "CIE-service is niet bereikbaar", "status_report_received" to "Melding ontvangen", "error_no_internet" to "Geen internetverbinding. Lokale bescherming blijft actief.", "error_timeout" to "CIE reageerde niet op tijd. Lokale bescherming blijft actief.", "error_sync" to "Synchroniseren met CIE is mislukt. Lokale bescherming blijft actief.",
            "message_lab" to "Message Shield Lab", "identity_ready" to "Identity Network gereed", "identity_needs_sync" to "Identity Network moet synchroniseren", "identity_synced" to "Identity Network gesynchroniseerd", "using_cached" to "Identiteiten uit cache worden gebruikt", "identity_unavailable" to "Identity Network niet beschikbaar", "verified_cached" to "{0} geverifieerde identiteiten lokaal opgeslagen", "sync_identity" to "Identity Network synchroniseren", "permission_free" to "Simulatie zonder toestemmingen", "permission_free_desc" to "Dit lab leest je sms-berichten niet. Afzenderidentiteiten komen uit CIE's geverifieerde backendcache; de berichtinhoud blijft op deze telefoon.", "test_message" to "Testbericht", "sender_or_phone" to "Afzender-ID of telefoonnummer", "use_cached_identity" to "Gebruik identiteit uit cache: {0}", "message" to "Bericht", "marketing" to "Marketing", "security" to "Beveiliging", "run_identity_test" to "Identiteitstest uitvoeren", "unknown_company" to "Onbekend bedrijf", "no_company_match" to "Geen bedrijfs-ID gevonden", "block" to "BLOKKEREN", "allow" to "TOESTAAN", "sender_type" to "Afzendertype", "normalized" to "Genormaliseerd", "message_type" to "Berichttype", "identity_confidence" to "Identiteitszekerheid", "not_matched" to "Geen match", "company_policy" to "Bedrijfsbreed beleid", "company_policy_desc" to "Geverifieerde telefoonnummers, afzender-ID's en korte codes kunnen naar één bedrijfs-ID verwijzen. Blokkeer het bedrijf één keer en elke vertrouwde identiteit erft hetzelfde marketingbeleid.", "close" to "Sluiten"
        ),
        CieLanguage.TURKISH to mapOf(
            "language" to "Dil", "language_desc" to "Bir dil seçin veya CIE'nin telefon dilini otomatik olarak takip etmesine izin verin.", "language_system" to "Otomatik (sistem)", "language_current" to "Geçerli dil: {0}",
            "settings" to "Ayarlar", "shield" to "Kalkan", "blocked" to "Engellenen", "activity" to "Etkinlik", "report_sender" to "Göndericiyi bildir", "back" to "Geri", "verified" to "DOĞRULANDI", "unknown_caller" to "Bilinmeyen arayan", "allowed" to "İzin verildi", "blocked_state" to "Engellendi", "calls" to "Aramalar",
            "cie_updating" to "CIE Shield güncelleniyor", "cie_ready" to "CIE Shield hazır", "cie_needs_setup" to "CIE Shield kurulumu gerekiyor", "phone_protected" to "Telefonunuz korunuyor", "enable_call_shield_setup" to "Kurulumu tamamlamak için Call Shield'ı etkinleştirin",
            "protection_active" to "Koruma aktif", "protection_needs_setup" to "Koruma kurulumu gerekiyor", "shield_description" to "CIE, gelen kurumsal aramaların arkasındaki şirketi tanır ve şirket genelindeki engelleme politikanızı uygular.", "on_device_on" to "Cihaz içi arama koruması açık", "enable_android_screening" to "Android arama taramasını etkinleştir", "recent" to "Son", "view_all" to "Tümünü gör", "no_call_activity" to "Henüz arama etkinliği yok", "activity_empty_hint" to "Engellenen ve son aramalar burada görünür.",
            "blocked_desc" to "Doğrulanmış bir şirketi bilinen arama ve pazarlama kimliklerinin tamamında engelleyin.", "search_companies" to "Şirket ara", "no_companies" to "Aramanızla eşleşen şirket yok", "activity_desc" to "CIE'nin bu telefonda izin verdiği veya engellediği işlemler.", "no_activity" to "Henüz etkinlik yok", "clear_activity" to "Yerel etkinliği temizle",
            "local_protection_active" to "Yerel koruma aktif kalır", "company_protection_ready" to "Şirket kimliği koruması hazır", "sync" to "Senkronizasyon", "service_online" to "CIE hizmeti çevrimiçi", "local_data" to "Yerel koruma verileri kullanılıyor", "status_not_checked" to "Hizmet durumu kontrol edilmedi", "syncing" to "Senkronize ediliyor…", "sync_now" to "Şimdi senkronize et", "check_service" to "Hizmeti kontrol et",
            "message_shield" to "Message Shield", "message_shield_desc" to "Kimlik mantığı Message Lab içinde aktiftir. Gerçek SMS filtreleme üretim SMS sürümüne ayrılmıştır.", "open_message_lab" to "Message Lab'i aç", "beta_test_lab" to "Beta Test Lab", "beta_test_desc" to "Call Shield'ı uçtan uca kanıtlamak için ikinci bir telefon numarası kullanın.", "second_phone" to "İkinci telefon numarası", "arm_test" to "Testi hazırla", "clear" to "Temizle", "test_armed" to "Test numarası hazır", "invalid_number" to "Geçersiz numara", "test_cleared" to "Test temizlendi",
            "privacy_local" to "Gizlilik ve yerel veriler", "privacy_desc" to "Arama kararları yerel kimlik önbelleğini kullanır. Bilinmeyen veya eski kimlikler varsayılan olarak geçirilir. Bu sürümde SMS, kişiler veya arama kaydı izinleri yoktur.", "cached_call_identities" to "{0} arama kimliği önbellekte", "help_improve" to "CIE'yi geliştirmeye yardımcı olun", "help_improve_desc" to "İstenmeyen bir göndericiyi inceleme için bildirin. Tek bir bildirim bir şirketi otomatik olarak engellemez.",
            "report_desc" to "Bildirimler CIE'nin şirket kimliğini doğrulamasına yardımcı olur. Tek bir bildirim kimseyi otomatik olarak engellemez.", "phone" to "Telefon", "sender_id" to "Gönderici kimliği", "short_code" to "Kısa kod", "sender" to "Gönderici", "category" to "Kategori", "report_reason" to "Bunu neden bildiriyorsunuz?", "submitting" to "Gönderiliyor…", "submit_report" to "Bildirimi gönder",
            "status_ready" to "CIE hazır", "status_up_to_date" to "CIE güncel", "status_company_blocked" to "{0} engellendi", "status_company_allowed" to "{0} izinli", "status_service_online" to "CIE hizmeti çevrimiçi", "status_service_unreachable" to "CIE hizmetine ulaşılamıyor", "status_report_received" to "Bildirim alındı", "error_no_internet" to "İnternet bağlantısı yok. Yerel koruma aktif kalır.", "error_timeout" to "CIE zamanında yanıt vermedi. Yerel koruma aktif kalır.", "error_sync" to "CIE ile senkronizasyon yapılamadı. Yerel koruma aktif kalır.",
            "message_lab" to "Message Shield Lab", "identity_ready" to "Identity Network hazır", "identity_needs_sync" to "Identity Network senkronizasyon gerektiriyor", "identity_synced" to "Identity Network senkronize edildi", "using_cached" to "Önbellekteki kimlikler kullanılıyor", "identity_unavailable" to "Identity Network kullanılamıyor", "verified_cached" to "{0} doğrulanmış kimlik yerel olarak önbellekte", "sync_identity" to "Identity Network'ü senkronize et", "permission_free" to "İzinsiz simülasyon", "permission_free_desc" to "Bu laboratuvar SMS'lerinizi okumaz. Gönderici kimlikleri CIE'nin doğrulanmış backend önbelleğinden gelir; mesaj içeriği bu telefonda kalır.", "test_message" to "Test mesajı", "sender_or_phone" to "Gönderici kimliği veya telefon numarası", "use_cached_identity" to "Önbellekteki kimliği kullan: {0}", "message" to "Mesaj", "marketing" to "Pazarlama", "security" to "Güvenlik", "run_identity_test" to "Kimlik testini çalıştır", "unknown_company" to "Bilinmeyen şirket", "no_company_match" to "Şirket kimliği eşleşmedi", "block" to "ENGELLE", "allow" to "İZİN VER", "sender_type" to "Gönderici türü", "normalized" to "Normalize edilmiş", "message_type" to "Mesaj türü", "identity_confidence" to "Kimlik güveni", "not_matched" to "Eşleşmedi", "company_policy" to "Şirket genelinde politika", "company_policy_desc" to "Doğrulanmış telefon numaraları, gönderici kimlikleri ve kısa kodlar tek bir şirket kimliğine bağlanabilir. Şirketi bir kez engelleyin; tüm güvenilir kimlikleri aynı pazarlama politikasını devralır.", "close" to "Kapat"
        ),
        CieLanguage.GERMAN to mapOf(
            "language" to "Sprache", "language_desc" to "Wähle eine Sprache oder lass CIE automatisch der Systemsprache folgen.", "language_system" to "Automatisch (System)", "language_current" to "Aktuelle Sprache: {0}",
            "settings" to "Einstellungen", "shield" to "Schutz", "blocked" to "Blockiert", "activity" to "Aktivität", "report_sender" to "Absender melden", "back" to "Zurück", "verified" to "VERIFIZIERT", "unknown_caller" to "Unbekannter Anrufer", "allowed" to "Zugelassen", "blocked_state" to "Blockiert", "calls" to "Anrufe",
            "cie_updating" to "CIE Shield wird aktualisiert", "cie_ready" to "CIE Shield ist bereit", "cie_needs_setup" to "CIE Shield muss eingerichtet werden", "phone_protected" to "Dein Telefon ist geschützt", "enable_call_shield_setup" to "Call Shield aktivieren, um die Einrichtung abzuschließen",
            "protection_active" to "Schutz aktiv", "protection_needs_setup" to "Schutz muss eingerichtet werden", "shield_description" to "CIE erkennt das Unternehmen hinter eingehenden geschäftlichen Anrufen und wendet deine unternehmensweite Blockierregel an.", "on_device_on" to "Geräteinterner Anrufschutz ist aktiv", "enable_android_screening" to "Android-Anrufprüfung aktivieren", "recent" to "Zuletzt", "view_all" to "Alle anzeigen", "no_call_activity" to "Noch keine Anrufaktivität", "activity_empty_hint" to "Blockierte und letzte Anrufe erscheinen hier.",
            "blocked_desc" to "Blockiere ein verifiziertes Unternehmen über seine bekannten Anruf- und Marketingidentitäten hinweg.", "search_companies" to "Unternehmen suchen", "no_companies" to "Keine passenden Unternehmen", "activity_desc" to "Was CIE auf diesem Telefon zugelassen oder blockiert hat.", "no_activity" to "Noch keine Aktivität", "clear_activity" to "Lokale Aktivität löschen",
            "local_protection_active" to "Lokaler Schutz bleibt aktiv", "company_protection_ready" to "Unternehmensidentitätsschutz ist bereit", "sync" to "Synchronisierung", "service_online" to "CIE-Dienst online", "local_data" to "Lokale Schutzdaten werden verwendet", "status_not_checked" to "Dienststatus nicht geprüft", "syncing" to "Synchronisiere…", "sync_now" to "Jetzt synchronisieren", "check_service" to "Dienst prüfen",
            "message_shield" to "Message Shield", "message_shield_desc" to "Die Identitätslogik ist im Message Lab aktiv. Die echte SMS-Filterung bleibt dem Produktions-SMS-Build vorbehalten.", "open_message_lab" to "Message Lab öffnen", "beta_test_lab" to "Beta Test Lab", "beta_test_desc" to "Verwende eine zweite Telefonnummer, um Call Shield Ende-zu-Ende zu testen.", "second_phone" to "Zweite Telefonnummer", "arm_test" to "Test aktivieren", "clear" to "Löschen", "test_armed" to "Testnummer aktiviert", "invalid_number" to "Ungültige Nummer", "test_cleared" to "Test gelöscht",
            "privacy_local" to "Datenschutz & lokale Daten", "privacy_desc" to "Anrufentscheidungen verwenden einen lokalen Identitätscache. Unbekannte oder veraltete Identitäten werden standardmäßig durchgelassen. Dieser Build hat keine SMS-, Kontakt- oder Anrufprotokollberechtigungen.", "cached_call_identities" to "{0} Anrufidentitäten im Cache", "help_improve" to "CIE verbessern", "help_improve_desc" to "Melde einen unerwünschten Absender zur Prüfung. Eine einzelne Meldung blockiert niemals automatisch ein Unternehmen.",
            "report_desc" to "Meldungen helfen CIE, Unternehmensidentitäten zu verifizieren. Eine einzelne Meldung blockiert niemanden automatisch.", "phone" to "Telefon", "sender_id" to "Absender-ID", "short_code" to "Kurzwahl", "sender" to "Absender", "category" to "Kategorie", "report_reason" to "Warum meldest du das?", "submitting" to "Wird gesendet…", "submit_report" to "Meldung senden",
            "status_ready" to "CIE ist bereit", "status_up_to_date" to "CIE ist aktuell", "status_company_blocked" to "{0} ist blockiert", "status_company_allowed" to "{0} ist zugelassen", "status_service_online" to "CIE-Dienst ist online", "status_service_unreachable" to "CIE-Dienst ist nicht erreichbar", "status_report_received" to "Meldung erhalten", "error_no_internet" to "Keine Internetverbindung. Lokaler Schutz bleibt aktiv.", "error_timeout" to "CIE hat nicht rechtzeitig geantwortet. Lokaler Schutz bleibt aktiv.", "error_sync" to "Synchronisierung mit CIE fehlgeschlagen. Lokaler Schutz bleibt aktiv.",
            "message_lab" to "Message Shield Lab", "identity_ready" to "Identity Network bereit", "identity_needs_sync" to "Identity Network muss synchronisiert werden", "identity_synced" to "Identity Network synchronisiert", "using_cached" to "Identitäten aus dem Cache werden verwendet", "identity_unavailable" to "Identity Network nicht verfügbar", "verified_cached" to "{0} verifizierte Identitäten lokal gespeichert", "sync_identity" to "Identity Network synchronisieren", "permission_free" to "Simulation ohne Berechtigungen", "permission_free_desc" to "Dieses Lab liest deine SMS nicht. Absenderidentitäten stammen aus dem verifizierten CIE-Backend-Cache; der Nachrichteninhalt bleibt auf diesem Telefon.", "test_message" to "Testnachricht", "sender_or_phone" to "Absender-ID oder Telefonnummer", "use_cached_identity" to "Identität aus Cache verwenden: {0}", "message" to "Nachricht", "marketing" to "Marketing", "security" to "Sicherheit", "run_identity_test" to "Identitätstest starten", "unknown_company" to "Unbekanntes Unternehmen", "no_company_match" to "Keine Unternehmens-ID gefunden", "block" to "BLOCKIEREN", "allow" to "ZULASSEN", "sender_type" to "Absendertyp", "normalized" to "Normalisiert", "message_type" to "Nachrichtentyp", "identity_confidence" to "Identitätssicherheit", "not_matched" to "Keine Übereinstimmung", "company_policy" to "Unternehmensweite Richtlinie", "company_policy_desc" to "Verifizierte Telefonnummern, Absender-IDs und Kurzwahlen können auf eine Unternehmens-ID verweisen. Blockiere das Unternehmen einmal und jede vertrauenswürdige Identität übernimmt dieselbe Marketingrichtlinie.", "close" to "Schließen"
        ),
        CieLanguage.PORTUGUESE to mapOf(
            "language" to "Idioma", "language_desc" to "Escolha um idioma ou deixe o CIE seguir automaticamente o idioma do telefone.", "language_system" to "Automático (sistema)", "language_current" to "Idioma atual: {0}",
            "settings" to "Definições", "shield" to "Proteção", "blocked" to "Bloqueados", "activity" to "Atividade", "report_sender" to "Denunciar remetente", "back" to "Voltar", "verified" to "VERIFICADO", "unknown_caller" to "Chamador desconhecido", "allowed" to "Permitido", "blocked_state" to "Bloqueado", "calls" to "Chamadas",
            "cie_updating" to "CIE Shield está a atualizar", "cie_ready" to "CIE Shield está pronto", "cie_needs_setup" to "CIE Shield precisa de configuração", "phone_protected" to "O seu telefone está protegido", "enable_call_shield_setup" to "Ative o Call Shield para concluir a configuração",
            "protection_active" to "Proteção ativa", "protection_needs_setup" to "Proteção precisa de configuração", "shield_description" to "O CIE reconhece a empresa por trás das chamadas comerciais recebidas e aplica a sua política de bloqueio em toda a empresa.", "on_device_on" to "Proteção de chamadas no dispositivo ativa", "enable_android_screening" to "Ativar filtragem de chamadas do Android", "recent" to "Recentes", "view_all" to "Ver tudo", "no_call_activity" to "Ainda não há atividade de chamadas", "activity_empty_hint" to "Chamadas bloqueadas e recentes aparecerão aqui.",
            "blocked_desc" to "Bloqueie uma empresa verificada em todas as suas identidades conhecidas de chamadas e marketing.", "search_companies" to "Pesquisar empresas", "no_companies" to "Nenhuma empresa corresponde à pesquisa", "activity_desc" to "O que o CIE permitiu ou bloqueou neste telefone.", "no_activity" to "Ainda sem atividade", "clear_activity" to "Limpar atividade local",
            "local_protection_active" to "A proteção local permanece ativa", "company_protection_ready" to "A proteção de identidade empresarial está pronta", "sync" to "Sincronização", "service_online" to "Serviço CIE online", "local_data" to "A utilizar dados de proteção locais", "status_not_checked" to "Estado do serviço não verificado", "syncing" to "A sincronizar…", "sync_now" to "Sincronizar agora", "check_service" to "Verificar serviço",
            "message_shield" to "Message Shield", "message_shield_desc" to "A lógica de identidade está ativa no Message Lab. A filtragem real de SMS fica reservada para a versão SMS de produção.", "open_message_lab" to "Abrir Message Lab", "beta_test_lab" to "Beta Test Lab", "beta_test_desc" to "Use um segundo número de telefone para testar o Call Shield de ponta a ponta.", "second_phone" to "Segundo número de telefone", "arm_test" to "Preparar teste", "clear" to "Limpar", "test_armed" to "Número de teste preparado", "invalid_number" to "Número inválido", "test_cleared" to "Teste limpo",
            "privacy_local" to "Privacidade e dados locais", "privacy_desc" to "As decisões de chamadas usam uma cache de identidade local. Identidades desconhecidas ou desatualizadas são permitidas por defeito. Esta versão não tem permissões de SMS, contactos ou registo de chamadas.", "cached_call_identities" to "{0} identidades de chamada em cache", "help_improve" to "Ajude a melhorar o CIE", "help_improve_desc" to "Denuncie um remetente indesejado para análise. Uma única denúncia nunca bloqueia automaticamente uma empresa.",
            "report_desc" to "As denúncias ajudam o CIE a verificar a identidade das empresas. Uma única denúncia nunca bloqueia alguém automaticamente.", "phone" to "Telefone", "sender_id" to "ID do remetente", "short_code" to "Código curto", "sender" to "Remetente", "category" to "Categoria", "report_reason" to "Por que está a denunciar isto?", "submitting" to "A enviar…", "submit_report" to "Enviar denúncia",
            "status_ready" to "CIE está pronto", "status_up_to_date" to "CIE está atualizado", "status_company_blocked" to "{0} está bloqueado", "status_company_allowed" to "{0} está permitido", "status_service_online" to "O serviço CIE está online", "status_service_unreachable" to "O serviço CIE está inacessível", "status_report_received" to "Denúncia recebida", "error_no_internet" to "Sem ligação à Internet. A proteção local permanece ativa.", "error_timeout" to "O CIE demorou demasiado a responder. A proteção local permanece ativa.", "error_sync" to "Não foi possível sincronizar com o CIE. A proteção local permanece ativa.",
            "message_lab" to "Message Shield Lab", "identity_ready" to "Identity Network pronto", "identity_needs_sync" to "Identity Network precisa de sincronização", "identity_synced" to "Identity Network sincronizado", "using_cached" to "A utilizar identidades em cache", "identity_unavailable" to "Identity Network indisponível", "verified_cached" to "{0} identidades verificadas guardadas localmente", "sync_identity" to "Sincronizar Identity Network", "permission_free" to "Simulação sem permissões", "permission_free_desc" to "Este laboratório não lê os seus SMS. As identidades dos remetentes vêm da cache verificada do backend CIE; o conteúdo da mensagem permanece neste telefone.", "test_message" to "Mensagem de teste", "sender_or_phone" to "ID do remetente ou número de telefone", "use_cached_identity" to "Usar identidade em cache: {0}", "message" to "Mensagem", "marketing" to "Marketing", "security" to "Segurança", "run_identity_test" to "Executar teste de identidade", "unknown_company" to "Empresa desconhecida", "no_company_match" to "Sem correspondência de ID da empresa", "block" to "BLOQUEAR", "allow" to "PERMITIR", "sender_type" to "Tipo de remetente", "normalized" to "Normalizado", "message_type" to "Tipo de mensagem", "identity_confidence" to "Confiança da identidade", "not_matched" to "Sem correspondência", "company_policy" to "Política para toda a empresa", "company_policy_desc" to "Números de telefone, IDs de remetente e códigos curtos verificados podem apontar para um único ID de empresa. Bloqueie a empresa uma vez e todas as identidades confiáveis herdam a mesma política de marketing.", "close" to "Fechar"
        ),
        CieLanguage.ARABIC to mapOf(
            "language" to "اللغة", "language_desc" to "اختر لغة أو دع CIE يتبع لغة الهاتف تلقائياً.", "language_system" to "تلقائي (النظام)", "language_current" to "اللغة الحالية: {0}",
            "settings" to "الإعدادات", "shield" to "الحماية", "blocked" to "المحظور", "activity" to "النشاط", "report_sender" to "الإبلاغ عن مرسل", "back" to "رجوع", "verified" to "موثّق", "unknown_caller" to "متصل غير معروف", "allowed" to "مسموح", "blocked_state" to "محظور", "calls" to "المكالمات",
            "cie_updating" to "يتم تحديث CIE Shield", "cie_ready" to "CIE Shield جاهز", "cie_needs_setup" to "يحتاج CIE Shield إلى الإعداد", "phone_protected" to "هاتفك محمي", "enable_call_shield_setup" to "فعّل Call Shield لإكمال الإعداد",
            "protection_active" to "الحماية نشطة", "protection_needs_setup" to "تحتاج الحماية إلى الإعداد", "shield_description" to "يتعرّف CIE على الشركة خلف المكالمات التجارية الواردة ويطبّق سياسة الحظر على مستوى الشركة.", "on_device_on" to "حماية المكالمات على الجهاز مفعّلة", "enable_android_screening" to "تفعيل فحص مكالمات Android", "recent" to "الأخيرة", "view_all" to "عرض الكل", "no_call_activity" to "لا يوجد نشاط مكالمات بعد", "activity_empty_hint" to "ستظهر المكالمات المحظورة والأخيرة هنا.",
            "blocked_desc" to "احظر شركة موثقة عبر هويات الاتصال والتسويق المعروفة التابعة لها.", "search_companies" to "البحث عن الشركات", "no_companies" to "لا توجد شركات مطابقة للبحث", "activity_desc" to "ما سمح به CIE أو حظره على هذا الهاتف.", "no_activity" to "لا يوجد نشاط بعد", "clear_activity" to "مسح النشاط المحلي",
            "local_protection_active" to "تبقى الحماية المحلية نشطة", "company_protection_ready" to "حماية هوية الشركات جاهزة", "sync" to "المزامنة", "service_online" to "خدمة CIE متصلة", "local_data" to "يتم استخدام بيانات الحماية المحلية", "status_not_checked" to "لم يتم فحص حالة الخدمة", "syncing" to "جارٍ المزامنة…", "sync_now" to "مزامنة الآن", "check_service" to "فحص الخدمة",
            "message_shield" to "Message Shield", "message_shield_desc" to "منطق الهوية نشط في Message Lab. اعتراض الرسائل الحقيقي مخصص لإصدار SMS الإنتاجي.", "open_message_lab" to "فتح Message Lab", "beta_test_lab" to "مختبر الاختبار التجريبي", "beta_test_desc" to "استخدم رقم هاتف ثانياً لاختبار Call Shield من البداية للنهاية.", "second_phone" to "رقم الهاتف الثاني", "arm_test" to "تجهيز الاختبار", "clear" to "مسح", "test_armed" to "تم تجهيز رقم الاختبار", "invalid_number" to "رقم غير صالح", "test_cleared" to "تم مسح الاختبار",
            "privacy_local" to "الخصوصية والبيانات المحلية", "privacy_desc" to "تستخدم قرارات المكالمات ذاكرة هوية محلية. الهويات غير المعروفة أو القديمة يتم السماح بها افتراضياً. هذا الإصدار لا يملك أذونات الرسائل أو جهات الاتصال أو سجل المكالمات.", "cached_call_identities" to "{0} هوية مكالمة محفوظة محلياً", "help_improve" to "ساعد في تحسين CIE", "help_improve_desc" to "أبلغ عن مرسل غير مرغوب لمراجعته. البلاغ الواحد لا يحظر شركة تلقائياً.",
            "report_desc" to "تساعد البلاغات CIE على التحقق من هوية الشركات. البلاغ الواحد لا يحظر أي شخص تلقائياً.", "phone" to "هاتف", "sender_id" to "معرّف المرسل", "short_code" to "رمز قصير", "sender" to "المرسل", "category" to "الفئة", "report_reason" to "لماذا تبلغ عن هذا؟", "submitting" to "جارٍ الإرسال…", "submit_report" to "إرسال البلاغ",
            "status_ready" to "CIE جاهز", "status_up_to_date" to "CIE محدّث", "status_company_blocked" to "تم حظر {0}", "status_company_allowed" to "تم السماح لـ {0}", "status_service_online" to "خدمة CIE متصلة", "status_service_unreachable" to "لا يمكن الوصول إلى خدمة CIE", "status_report_received" to "تم استلام البلاغ", "error_no_internet" to "لا يوجد اتصال بالإنترنت. تبقى الحماية المحلية نشطة.", "error_timeout" to "تأخر رد CIE. تبقى الحماية المحلية نشطة.", "error_sync" to "تعذرت المزامنة مع CIE. تبقى الحماية المحلية نشطة.",
            "message_lab" to "Message Shield Lab", "identity_ready" to "Identity Network جاهزة", "identity_needs_sync" to "تحتاج Identity Network إلى المزامنة", "identity_synced" to "تمت مزامنة Identity Network", "using_cached" to "يتم استخدام الهويات المحفوظة", "identity_unavailable" to "Identity Network غير متاحة", "verified_cached" to "{0} هوية موثقة محفوظة محلياً", "sync_identity" to "مزامنة Identity Network", "permission_free" to "محاكاة بلا أذونات", "permission_free_desc" to "هذا المختبر لا يقرأ رسائلك. هويات المرسلين تأتي من ذاكرة CIE الخلفية الموثقة؛ ويظل محتوى الرسالة على هذا الهاتف.", "test_message" to "رسالة اختبار", "sender_or_phone" to "معرّف المرسل أو رقم الهاتف", "use_cached_identity" to "استخدام هوية محفوظة: {0}", "message" to "الرسالة", "marketing" to "تسويق", "security" to "أمان", "run_identity_test" to "تشغيل اختبار الهوية", "unknown_company" to "شركة غير معروفة", "no_company_match" to "لا توجد مطابقة لمعرّف الشركة", "block" to "حظر", "allow" to "سماح", "sender_type" to "نوع المرسل", "normalized" to "بعد التوحيد", "message_type" to "نوع الرسالة", "identity_confidence" to "ثقة الهوية", "not_matched" to "غير مطابق", "company_policy" to "سياسة على مستوى الشركة", "company_policy_desc" to "يمكن ربط أرقام الهاتف ومعرّفات المرسل والرموز القصيرة الموثقة بمعرّف شركة واحد. احظر الشركة مرة واحدة لترث جميع هوياتها الموثوقة سياسة التسويق نفسها.", "close" to "إغلاق"
        )
    )
}
