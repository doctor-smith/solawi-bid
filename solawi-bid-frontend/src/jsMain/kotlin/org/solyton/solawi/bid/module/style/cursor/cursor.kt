package org.solyton.solawi.bid.module.style.cursor

import org.jetbrains.compose.web.css.StyleScope

/**
 * Type-safe representation of CSS cursor values.
 *
 * Covers:
 * - All standard CSS cursors
 * - Full resize directions
 * - Drag & drop cursors
 * - Zoom & scroll cursors
 * - CSS system keywords
 * - Custom `url(...)` cursors with fallback support
 *
 * Example:
 *
 * ```
 * style {
 *     cursor(Cursor.Pointer)
 * }
 *
 * style {
 *     cursor(Cursor.url("/cursor.png", Cursor.Auto))
 * }
 * ```
 */
sealed interface Cursor {

    /** Converts the cursor to its CSS string representation. */
    fun toCss(): String

    /* ============================================================
     * Core / Default
     * ============================================================ */

    /** Browser decides automatically (recommended default). */
    data object Auto : Cursor { override fun toCss() = "auto" }

    /** Standard arrow cursor. */
    data object Default : Cursor { override fun toCss() = "default" }

    /** Hides the cursor completely. */
    data object None : Cursor { override fun toCss() = "none" }


    /* ============================================================
     * Basic Interaction
     * ============================================================ */

    /** Indicates a clickable element (links, buttons). */
    data object Pointer : Cursor { override fun toCss() = "pointer" }

    /** Text selection (I-beam). */
    data object Text : Cursor { override fun toCss() = "text" }

    /** Vertical text selection cursor. */
    data object VerticalText : Cursor { override fun toCss() = "vertical-text" }

    /** Help is available. */
    data object Help : Cursor { override fun toCss() = "help" }

    /** Context menu can be opened. */
    data object ContextMenu : Cursor { override fun toCss() = "context-menu" }

    /** Table cell selection cursor. */
    data object Cell : Cursor { override fun toCss() = "cell" }


    /* ============================================================
     * Drag & Drop
     * ============================================================ */

    /** Element can be moved. */
    data object Move : Cursor { override fun toCss() = "move" }

    /** Open hand – draggable element (before dragging). */
    data object Grab : Cursor { override fun toCss() = "grab" }

    /** Closed hand – element is currently being dragged. */
    data object Grabbing : Cursor { override fun toCss() = "grabbing" }

    /** Drag operation will create an alias/shortcut. */
    data object Alias : Cursor { override fun toCss() = "alias" }

    /** Drag operation will create a copy. */
    data object Copy : Cursor { override fun toCss() = "copy" }

    /** Drop is not allowed at current position. */
    data object NoDrop : Cursor { override fun toCss() = "no-drop" }

    /** Action is forbidden. */
    data object NotAllowed : Cursor { override fun toCss() = "not-allowed" }


    /* ============================================================
     * Loading / Status
     * ============================================================ */

    /** System busy – user interaction should wait. */
    data object Wait : Cursor { override fun toCss() = "wait" }

    /** Background task running – UI still interactive. */
    data object Progress : Cursor { override fun toCss() = "progress" }


    /* ============================================================
     * Precision
     * ============================================================ */

    /** Crosshair cursor (e.g., drawing tools, selection tools). */
    data object Crosshair : Cursor { override fun toCss() = "crosshair" }


    /* ============================================================
     * Scrolling
     * ============================================================ */

    /** All-direction scrolling cursor. */
    data object AllScroll : Cursor { override fun toCss() = "all-scroll" }


    /* ============================================================
     * Resize – General
     * ============================================================ */

    /** Horizontal resize (left ↔ right). */
    data object EwResize : Cursor { override fun toCss() = "ew-resize" }

    /** Vertical resize (up ↕ down). */
    data object NsResize : Cursor { override fun toCss() = "ns-resize" }

    /** Diagonal resize (top-left ↘ bottom-right). */
    data object NwseResize : Cursor { override fun toCss() = "nwse-resize" }

    /** Diagonal resize (top-right ↙ bottom-left). */
    data object NeswResize : Cursor { override fun toCss() = "nesw-resize" }

    /** Column resize (e.g., table column borders). */
    data object ColResize : Cursor { override fun toCss() = "col-resize" }

    /** Row resize (e.g., table row borders). */
    data object RowResize : Cursor { override fun toCss() = "row-resize" }


    /* ============================================================
     * Resize – Directional (Granular)
     * ============================================================ */

    data object NResize : Cursor { override fun toCss() = "n-resize" }
    data object SResize : Cursor { override fun toCss() = "s-resize" }
    data object EResize : Cursor { override fun toCss() = "e-resize" }
    data object WResize : Cursor { override fun toCss() = "w-resize" }

    data object NeResize : Cursor { override fun toCss() = "ne-resize" }
    data object NwResize : Cursor { override fun toCss() = "nw-resize" }
    data object SeResize : Cursor { override fun toCss() = "se-resize" }
    data object SwResize : Cursor { override fun toCss() = "sw-resize" }


    /* ============================================================
     * Zoom
     * ============================================================ */

    /** Zoom in (images, maps, canvases). */
    data object ZoomIn : Cursor { override fun toCss() = "zoom-in" }

    /** Zoom out. */
    data object ZoomOut : Cursor { override fun toCss() = "zoom-out" }


    /* ============================================================
     * CSS System Keywords
     * ============================================================ */

    /** Inherit cursor from parent element. */
    data object Inherit : Cursor { override fun toCss() = "inherit" }

    /** Reset to CSS initial value. */
    data object Initial : Cursor { override fun toCss() = "initial" }

    /** Revert to user-agent or inherited value. */
    data object Revert : Cursor { override fun toCss() = "revert" }

    /** Revert considering cascade layers. */
    data object RevertLayer : Cursor { override fun toCss() = "revert-layer" }

    /** Inherit if inheritable, otherwise initial. */
    data object Unset : Cursor { override fun toCss() = "unset" }


    /* ============================================================
     * Custom URL Cursor
     * ============================================================ */

    /**
     * Custom image cursor.
     *
     * Example:
     * ```
     * cursor: url("/cursor.png"), pointer;
     * ```
     */
    data class Url(
        val path: String,
        val fallback: Cursor = Auto
    ) : Cursor {
        override fun toCss(): String =
            """url("$path"), ${fallback.toCss()}"""
    }

    companion object {
        /** Convenience factory for URL cursors. */
        fun url(path: String, fallback: Cursor = Auto): Cursor =
            Url(path, fallback)
    }
}


/**
 * Sets the CSS `cursor` property for this style scope.
 *
 * This is a type-safe wrapper around the raw CSS property and works
 * with the custom [Cursor] sealed interface.
 *
 * Example:
 *
 * ```
 * style {
 *     cursor(Cursor.Pointer)
 * }
 *
 * style {
 *     cursor(Cursor.url("/cursor.png", Cursor.Auto))
 * }
 * ```
 *
 * Internally renders:
 *
 * ```
 * cursor: pointer;
 * cursor: url("/cursor.png"), auto;
 * ```
 *
 * @param cursor The [Cursor] value to apply.
 */
fun StyleScope.cursor(cursor: Cursor) {
    property("cursor", cursor.toCss())
}
