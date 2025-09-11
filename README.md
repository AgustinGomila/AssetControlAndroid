<br/>
<p align="center">
    <a href="https://github.com/AgustinGomila/AssetControlAndroid">
    <img src="screenshots/logo.png" alt="Logo" width="120"   height="120" />
    </a>

<h3 align="center">AssetControl</h3>
<p align="center">
App para control de activos fijos en Android
<br/>
<a href="https://github.com/AgustinGomila/AssetControlAndroid/issues">Reportar Bug</a>
·
<a href="https://github.com/AgustinGomila/AssetControlAndroid/issues">Solicitar cambios</a>
</p>

Tabla de contenidos

* [Librerías utilizadas](#librerías-utilizadas)
* [Algunas características](#algunas-características)
* [Acerca del proyecto](#acerca-del-proyecto)
* [Capturas de pantalla](#capturas-de-pantalla)
* [Contacto](#contacto)

## Librerías utilizadas

* [Kotlin](https://kotlinlang.org/)
* [Koin](https://github.com/InsertKoinIO/koin)
* [Room](https://android.googlesource.com/platform/frameworks/support/+/androidx-master-dev/room)
* [ZXing](https://github.com/journeyapps/zxing-android-embedded)

## Algunas características

* Actualización por APK desde la app.
* Sincronización en modo online y offline.
* [Toma de fotografías y vinculación con activos.](/manual/image_control/image_control.md)
* [Realización de recorridas programadas para captura de datos.](/manual/route_process/index.md)
* Impresión de etiquetas de códigos de barra con impresoras de red o Bluetooth.
* Revisión de activos, movimientos, altas y modificaciones.
* Pantalla de inicio animada.
* [Filtrado de rutas por dominio](/manual/configuration/route_filter/index.md)
* Interfaz intuitiva para gestión de activos.
* [Soporte para lectura y escritura RFID/NFC](/manual/configuration/rfid_config/index.md)

## Acerca del proyecto

Esta aplicación permite a las empresas gestionar sus activos fijos utilizando un dispositivo Android. Permite realizar
inventarios, capturar información de activos mediante escaneo de códigos de barras, tomar fotos de los activos,
registrar movimientos y revisiones. La aplicación puede funcionar tanto en modo online (sincronizando con un servidor)
como en modo offline (almacenando datos localmente para sincronizar posteriormente).

## Capturas de pantalla

| ![img_2.png](/manual/image_control/img_2.png)                                     | ![img_3.png](/manual/image_control/img_3.png)                                   |
|-----------------------------------------------------------------------------------|---------------------------------------------------------------------------------|
| Captura de fotografía de activos                                                  | Posibilidad de añadir observaciones                                             |
| ![data_collection_actual.png](/manual/route_process/data_collection_actual.png)   | ![data_collection_detail.png](/manual/route_process/data_collection_detail.png) |
| Detalle de la recolección                                                         | Detalles de los activos                                                         |
| ![data_collection_confirm.png](/manual/route_process/data_collection_confirm.png) | ![send_data_full.png](/manual/route_process/send_data_full.png)                 |
| Recorrida programada con unidades técnicas de control                             | Sincronización de datos                                                         |

## Contacto

[https://github.com/AgustinGomila](https://github.com/AgustinGomila)