package com.tomwo.app.payroll.extensions

import android.util.Log

/**
 * prevents the logger tags from exceeding Android's Limits
 */
private const val MAX_LOG_TAG_LENGTH = 23
private const val LOG_PREFIX         = "TOM_"
private const val LOG_PREFIX_LENGTH  = LOG_PREFIX.length

/**
 * [getTag] - returns the name of the class preceded by [LOG_PREFIX]
 *
 * # NOTE: Don't use this when obfuscating class names!
 */
fun getTag(tag: String) = if (tag.length > MAX_LOG_TAG_LENGTH - LOG_PREFIX_LENGTH) tag.substring(0, MAX_LOG_TAG_LENGTH - LOG_PREFIX_LENGTH - 1)+tag else LOG_PREFIX +tag

//fun <T : Any> jClazz(t: KClass<T>) : Class<T> = t::class.java as Class<T>

inline fun <reified T : Any> clazzName(kClass: T) : String = kClass::class.java.simpleName
inline fun <reified T : Any> clazzName(): String = T::class.java.simpleName

inline fun <reified T: Any> clazz(): Class<T> = T::class.java


/**
 * Global Functions
 */
inline fun <reified T : Any> debug(msg: Any)
{
    Log.d(getTag(clazzName<T>()), msg.toString())
}

/**
 * Extension Functions
 */
inline fun <reified T: Any> T.debug(msg: Any)
{
    Log.d(getTag(clazzName<T>()), msg.toString())
}

inline fun <reified T: Any> T.verbose(msg: Any)
{
    Log.v(getTag(clazzName<T>()), msg.toString())
}

inline fun <reified T: Any> T.info(msg: Any)
{
    Log.i(getTag(clazzName<T>()), msg.toString())
}

inline fun <reified T: Any> T.warn(msg: Any, e: Throwable? = null)
{
    Log.w(getTag(clazzName<T>()), msg.toString(), e)
}

inline fun <reified T: Any> T.error(msg: Any, e: Throwable? = null)
{
    Log.e(getTag(clazzName<T>()), msg.toString() + e?.let { Log.getStackTraceString(it) }, e)
}



