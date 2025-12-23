package org.evoleq.language

import org.evoleq.math.Reader

@I18N
val date: Reader<Lang.Block, String> = Reader { lang -> lang["date"] }
@I18N
val description: Reader<Lang.Block, String> = Reader { lang -> lang["description"] }
val subTitle: Reader<Lang.Block, String> = Reader { lang -> lang["subTitle"] }
@I18N
val title: Reader<Lang.Block, String> = Reader { lang -> lang["title"] }
@I18N
val text: Reader<Lang.Block, String> = Reader { lang -> lang["text"] }
@I18N
val tooltip: Reader<Lang.Block, String> = Reader{ lang -> lang["tooltip"]}
