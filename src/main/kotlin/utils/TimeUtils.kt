package us.cedarfarm.utils

import java.time.Instant

fun calculateWindow(window: Long): Instant = Instant.ofEpochMilli(window)
