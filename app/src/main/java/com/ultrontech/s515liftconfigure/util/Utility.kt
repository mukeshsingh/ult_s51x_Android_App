import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import java.util.Locale


fun changeLang(c: Context, langCode: String): ContextWrapper {
    var context = c
    val sysLocale: Locale
    val rs: Resources = context.resources
    val config: Configuration = rs.configuration

    sysLocale = config.locales.get(0)

    if (langCode != "" && sysLocale.language != langCode) {
        val locale = Locale(langCode)
        Locale.setDefault(locale)
        config.setLocale(locale)

        context = context.createConfigurationContext(config)
    }
    return ContextWrapper(context)
}