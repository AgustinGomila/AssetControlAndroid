package com.dacosys.assetControl.network.download

/**
 * Tiene el tipo de archivo que se está descargando.
 * De esto depende la lógica para la secuencia de descargas.
 */
@Suppress("unused")
enum class FileType(val id: Long) {
    TIMEFILE(1), CREATIONLOGFILE(2), DBFILE(3)
}