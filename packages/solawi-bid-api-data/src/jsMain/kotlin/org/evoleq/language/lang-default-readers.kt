package org.evoleq.language

import org.evoleq.math.Reader

val title: Reader<Lang.Block, String> = Reader { lang -> lang["title"] }
val date: Reader<Lang.Block, String> = Reader { lang -> lang["date"] }
val text: Reader<Lang.Block, String> = Reader { lang -> lang["text"] }