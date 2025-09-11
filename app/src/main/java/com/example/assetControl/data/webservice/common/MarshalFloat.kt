package com.example.assetControl.data.webservice.common

import org.ksoap2.serialization.Marshal
import org.ksoap2.serialization.PropertyInfo
import org.ksoap2.serialization.SoapSerializationEnvelope
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import org.xmlpull.v1.XmlSerializer
import java.io.IOException

class MarshalFloat : Marshal {
    @Throws(IOException::class, XmlPullParserException::class)
    override fun readInstance(
        parser: XmlPullParser,
        namespace: String,
        name: String,
        propertyInfo: PropertyInfo,
    ): Any {
        val stringValue = parser.nextText()
        return when (name) {
            "float" -> java.lang.Float.valueOf(stringValue)
            "double" -> java.lang.Double.valueOf(stringValue)
            "decimal" -> java.math.BigDecimal(stringValue)
            else -> throw RuntimeException("float, double, or decimal expected")
        }
    }

    @Throws(IOException::class)
    override fun writeInstance(writer: XmlSerializer, instance: Any) {
        writer.text(instance.toString())
    }

    override fun register(cm: SoapSerializationEnvelope) {
        cm.addMapping(cm.xsd, "float", Float::class.javaObjectType, this)
        cm.addMapping(cm.xsd, "double", Double::class.javaObjectType, this)
        cm.addMapping(cm.xsd, "decimal", java.math.BigDecimal::class.javaObjectType, this)
    }
}