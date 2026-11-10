package com.raizartesanal.app.ui

import java.text.Normalizer
import java.util.Locale

/** 180000 -> "$180.000" */
fun Int.toCop(): String = "\$" + String.format(Locale.US, "%,d", this).replace(',', '.')

/** Minúsculas y sin tildes, para búsquedas. */
fun String.normalized(): String =
    Normalizer.normalize(lowercase(Locale.ROOT), Normalizer.Form.NFD).replace(Regex("\\p{Mn}+"), "")
