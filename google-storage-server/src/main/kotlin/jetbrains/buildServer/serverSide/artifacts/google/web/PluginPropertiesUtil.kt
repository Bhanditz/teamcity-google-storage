/*
 * Copyright 2000-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * See LICENSE in the project root for license information.
 */

package jetbrains.buildServer.serverSide.artifacts.google.web

import jetbrains.buildServer.controllers.BasePropertiesBean
import jetbrains.buildServer.serverSide.crypt.RSACipher

import javax.servlet.http.HttpServletRequest

/**
 * @author Pavel.Sher
 * *         Date: 25.05.2006
 */
object PluginPropertiesUtil {
    private val PROPERTY_PREFIX = "prop:"
    private val ENCRYPTED_PROPERTY_PREFIX = "prop:encrypted:"

    fun bindPropertiesFromRequest(request: HttpServletRequest, bean: BasePropertiesBean, includeEmptyValues: Boolean) {
        bean.clearProperties()

        request.parameterMap.keys
                .filter { it.startsWith(PROPERTY_PREFIX) }
                .forEach {
                    if (it.startsWith(ENCRYPTED_PROPERTY_PREFIX)) {
                        setEncryptedProperty(it, request, bean, includeEmptyValues)
                    } else {
                        setStringProperty(it, request, bean, includeEmptyValues)
                    }
                }
    }

    private fun setStringProperty(paramName: String, request: HttpServletRequest,
                                  bean: BasePropertiesBean, includeEmptyValues: Boolean) {
        val propName = paramName.substring(PROPERTY_PREFIX.length)
        val propertyValue = request.getParameter(paramName).trim { it <= ' ' }
        if (includeEmptyValues || propertyValue.isNotEmpty()) {
            bean.setProperty(propName, toUnixLineFeeds(propertyValue))
        }
    }

    private fun setEncryptedProperty(paramName: String, request: HttpServletRequest,
                                     bean: BasePropertiesBean, includeEmptyValues: Boolean) {
        val propName = paramName.substring(ENCRYPTED_PROPERTY_PREFIX.length)
        val propertyValue = RSACipher.decryptWebRequestData(request.getParameter(paramName))
        if (propertyValue != null && (includeEmptyValues || propertyValue.isNotEmpty())) {
            bean.setProperty(propName, toUnixLineFeeds(propertyValue))
        }
    }

    private fun toUnixLineFeeds(str: String): String {
        return str.replace("\r", "")
    }
}
