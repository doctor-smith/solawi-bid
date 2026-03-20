package org.solyton.solawi.bid.module.country.i18n

import org.evoleq.language.Lang
import org.evoleq.language.LangComponent
import org.evoleq.language.component
import org.evoleq.language.get
import org.evoleq.language.subComp
import org.evoleq.math.Reader

private const val BASE_PATH = "solyton.countries"

/**
 * Structure:
 * The structure follows from the structure of the files
 * - xy.countries
 * - xy.contries.stateOrProvince.UV
 * ```
 * countries {
 *   names {
 *      AT: "Austria"         // List of countries mapped to their names
 *      BE: "Belgium"
 *      BG: "Bulgaria"
 *      ...
 *   }
 *   stateOrProvince {     // Nested structure for regional data
 *      AT {               // Austria's states
 *          AT-B: "Burgenland"
 *          ...
 *      }
 *      DE {               // Germany's states
 *          DE-BW: "Baden-Württemberg"
 *          ...
 *      }
 *   }
 * ```
 */
sealed class CountryLangComponent(
    override val path: String, 
    override val value: String = BASE_PATH
): LangComponent {
    data object Base: CountryLangComponent(BASE_PATH) {
        val component: Reader<Lang, Lang.Block> = subComp(path)
    }
    data object Countries: CountryLangComponent(
        "$BASE_PATH.names"
    ) {
        val component: Reader<Lang, Lang.Block> = subComp(path)
    }
    data class StateOrProvince(val code: String): CountryLangComponent(
        "$BASE_PATH.stateOrProvince.$code",
        "$BASE_PATH.stateOrProvince.$code"
    ) {
        val component: Reader<Lang, Lang.Block> = subComp(path)
    }
    
    companion object {
        /**
         * Accesses country-related language data stored within a hierarchical `Lang.Block` structure.
         *
         * This reader is designed to navigate a structure consisting of:
         * - Countries identified by their codes mapped to names (e.g., `AT: "Austria"`, `BE: "Belgium"`).
         * - Optional nested blocks for states or provinces associated with specific countries (e.g., `stateOrProvince.AT.AT-B` refers to Austrian states).
         *
         * This is commonly used to support internationalization (i18n), localization (l10n),
         * or structured processing of multilingual data.
         */
        val countries: Reader<Lang.Block, Lang.Block> = subComp("countries")

        val names: Reader<Lang.Block, Lang.Block> = subComp("names")

        /**
         * Retrieves the name or defined value linked to a specific country code within the `Lang.Block` structure.
         *
         * This reader maps a country code (e.g., "AT" for Austria) to its corresponding name using the country-code-to-name mapping available in `Lang.Block`.
         *
         * Example mappings:
         * - `AT` -> "Austria"
         * - `DE` -> "Germany"
         *
         * If the provided code is absent, it throws an exception related to the structure. This is ideal in contexts requiring dynamic resolution of country information.
         *
         * @param countryCode ISO-like code representing a country.
         * @return A reader resolving to the name of the country from the language block.
         */
        val country: (countryCode: String) -> Reader<Lang.Block, String> = {code -> {block -> block[code]}}

        /**
         * Retrieves a `Lang.Block` structure representing the states or provinces for a given country code.
         *
         * Using the provided `countryCode`, this function navigates a hierarchical `Lang.Block` to locate and return the block containing regional information.
         * The `Reader` abstraction handles the traversal using the `component` method.
         *
         * @property countryCode ISO-like code representing the country to query for states or provinces.
         * @return A reader that accepts a `Lang.Block` and retrieves the corresponding nested block for states or provinces.
         */
        val statesOrProvinces: (countryCode: String) -> Reader<Lang.Block, Lang.Block> = {
            code -> {block -> block.component("stateOrProvince.$code")}
        }

        /**
         * A function type variable used to retrieve a localized name of a state or province within a specified country.
         *
         * This function operates in the context of language processing and localization. It takes two parameters:
         * - `countryCode`: The code that uniquely identifies the country (e.g., ISO country code).
         * - `stateOrProvinceCode`: The code representing the state or province within the specified country.
         *
         * The function returns a `Reader` operating over a `Lang.Block` that yields the localized name of the specified
         * state or province as a `String`. The localization process involves navigating the hierarchical structure
         * of a `Lang.Block` to find the corresponding value using the combined key in the format `<countryCode>-<stateOrProvinceCode>`.
         *
         * This variable supports scenarios where hierarchical language components need to be accessed dynamically
         * based on country and region-specific codes, ensuring flexibility in handling multilingual data.
         */
        val stateOrProvince: (countryCode: String, stateOrProvinceCode: String) -> Reader<Lang.Block, String> = {
            code, state -> {block -> block.component(code)["$code-$state"]}
        }
    }
}
