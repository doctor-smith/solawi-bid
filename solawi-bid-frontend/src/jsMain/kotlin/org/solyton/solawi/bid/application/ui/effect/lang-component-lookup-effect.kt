package org.solyton.solawi.bid.application.ui.effect

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import org.evoleq.compose.Markup
import org.evoleq.language.Lang
import org.evoleq.language.LangComponent
import org.evoleq.language.merge
import org.evoleq.math.Source
import org.evoleq.math.emit
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.storage.addToSet
import org.evoleq.optics.storage.removeFromSet
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

    // if component is loaded return
    val loaded = (i18n * componentLoaded(langComponent)).emit()
    if (loaded) return

    // dedup-flag (must! see text above)
    val loading = (i18n * componentLoading(langComponent)).emit()
    if (loading) return

    LaunchedEffect(langComponent) {
        // Check again in  Coroutine-context (Extra-Safety)
        if ((i18n * componentLoaded(langComponent)).emit()) return@LaunchedEffect
        if ((i18n * componentLoading(langComponent)).emit()) return@LaunchedEffect

        // 1) Set as loading immediately (dedup gate)
        (i18n * loadingComponents).addToSet(langComponent)

        try {
            val delta = environment.emit().componentOnDemand(
                langComponent,
                (i18n * language.get).emit(),
                (i18n * locale.get).emit()
            )
           snapshotFlow { delta }.collect { delta ->
                // 2) merge delta
                if (delta.mergeNeeded) {
                    (i18n * language).merge(delta.language)
                }

                // 3) Mark as loaded
                (i18n * loadedComponents).addToSet(langComponent)
            }
        } finally {
            // 4) Remove loading flag
            (i18n * loadingComponents).removeFromSet(langComponent)
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
