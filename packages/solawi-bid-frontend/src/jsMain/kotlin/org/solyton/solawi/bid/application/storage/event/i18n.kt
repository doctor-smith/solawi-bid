package org.solyton.solawi.bid.application.storage.event

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.evoleq.language.Lang
import org.evoleq.language.LanguageP
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.transform.times
import org.solyton.solawi.bid.application.data.Application
import org.solyton.solawi.bid.application.data.environment
import org.solyton.solawi.bid.application.data.i18N
import org.solyton.solawi.bid.application.service.useI18nTransform
import org.solyton.solawi.bid.module.cookie.api.writeLang
import org.solyton.solawi.bid.module.i18n.api.i18n
import org.solyton.solawi.bid.module.i18n.data.language
import org.solyton.solawi.bid.module.i18n.data.locale
import org.solyton.solawi.bid.module.i18n.data.locales


fun Storage<Application>.langLoaded (): Boolean  {

    val languageStorage = (this * i18N * language)
    val localesStorage = (this * i18N * locales)

    return (languageStorage.read() as Lang.Block).value.isNotEmpty() &&
        localesStorage.read().isNotEmpty()
}



fun Storage<Application>.onLocaleChanged(oldApplication: Application, newApplication: Application) {
    if (newApplication.i18N.locale != oldApplication.i18N.locale) {
        CoroutineScope(Job()).launch {
            val app = read()
            val environment = (this@onLocaleChanged * environment).read()
            try {
                with(LanguageP().run(environment.useI18nTransform().i18n(newApplication.i18N.locale)).result) {
                    if (this != null) {
                        write(app.copy(
                            i18N = app.i18N.copy(
                                locale = newApplication.i18N.locale,
                                language = this,
                                loadedComponents = listOf()
                            )
                        ) )
                        writeLang(newApplication.i18N.locale)
                    }
                }
            } catch (exception: Exception) {
                console.log(exception)
            }
        }
    }
}


@Composable
fun Storage<Application>.loadLanguage() {
    val storage = this
    val languageStorage = (storage * i18N * language)
    val localesStorage = (storage * i18N * locales)
    val localeStorage = (storage * i18N * locale)
    val environment = (storage * environment)

    val langLoaded: () -> Boolean = {
        (languageStorage.read() as Lang.Block).value.isNotEmpty() &&
            localesStorage.read().isNotEmpty()
    }

    if (!langLoaded()) {
        LaunchedEffect(Unit) {
            val rawLocale = environment.read().useI18nTransform().i18n(localeStorage.read())
            val locale = LanguageP().run(rawLocale).result
            with(locale) {
                if (this != null) {
                    languageStorage.write(this)
                }
            }
            with(LanguageP().run(environment.read().useI18nTransform().i18n("locales")).result) {
                if (this != null) {
                    localesStorage.write((this as Lang.Block).value.map { it.key })
                }
            }
        }
    }
}
