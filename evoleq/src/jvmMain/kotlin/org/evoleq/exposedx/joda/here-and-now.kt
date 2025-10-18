package org.evoleq.exposedx.joda

import org.joda.time.DateTime
import org.joda.time.DateTimeZone

fun hereAndNow(): DateTime = DateTime.now()

fun now(): DateTime = DateTime.now(DateTimeZone.UTC)
