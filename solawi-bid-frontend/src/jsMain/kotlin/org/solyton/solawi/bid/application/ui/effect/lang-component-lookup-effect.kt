package org.solyton.solawi.bid.application.ui.effect

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import org.evoleq.compose.Markup
import org.evoleq.language.Lang
import org.evoleq.language.LangComponent
import org.evoleq.language.merge
import org.evoleq.math.Source
import org.evoleq.math.Writer
import org.evoleq.math.emit
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.storage.add
import org.evoleq.optics.transform.times
import org.solyton.solawi.bid.module.i18n.data.*
import org.solyton.solawi.bid.module.i18n.service.componentOnDemand

/**
 * Launches and manages the lookup process for a language component.
 * This method checks if a language component is already loaded in
 * the internationalization (i18n) storage. If not, it triggers the
 * asynchronous loading of the component and handles its integration
 * into the i18n storage based on the specified environment.
 *
 * @param langComponent The language component to be loaded or verified.
 * @param environment A source providing the environment configuration.
 * @param i18n A storage for managing internationalization components.
 */
@Markup
@Suppress("FunctionName")
@Composable
fun LaunchComponentLookup(
    langComponent: LangComponent,
    environment: Source<Environment>,
    i18n: Storage<I18N>,
) {
    @Suppress("ForbiddenComment")
    // TODO:dev improve performance:
    //  At the moment langs will be merged twice:
    //        1. in [componentOnDemand]
    //        2. at the end of the launched effect
    //  This was introduced with SMA-418, SMA-419, SMA-420
    //  Reason: Parallel loading led to concurrency issues -> lang components were overridden
    //  Ticket: SMA-421
    val loaded = (i18n * componentLoaded(langComponent)).emit()
    if(loaded) return

    LaunchedEffect(Unit) {
        val componentLookup = environment.emit()
            .componentOnDemand(
                langComponent,
                (i18n * language.get).emit(),
                (i18n * locale.get).emit()
            )

        (i18n * loadedComponents).add(langComponent)

        // replace component in storage
        if(componentLookup.mergeNeeded){
            (i18n * language).merge(componentLookup.language)
        }
    }
}

/**
 * Merges the provided language component into the current stored language.
 *
 * The method reads the current language component from the storage, merges it
 * with the provided `other` language based on their key, and writes the result
 * back into the storage.
 *
 * @param other The language component to be merged with the stored language.
 */
fun Storage<Lang>.merge(other: Lang) = write(read().merge(other))
