package app.xrename.patches

import app.morphe.patcher.patch.resourcePatch
import app.morphe.patcher.patch.stringOption
import org.w3c.dom.Element

private const val ACTION_MAIN = "android.intent.action.MAIN"
private const val ANDROID_NAMESPACE = "http://schemas.android.com/apk/res/android"

private val LAUNCHER_CATEGORIES = setOf(
    "android.intent.category.LAUNCHER",
    "android.intent.category.LEANBACK_LAUNCHER",
    "android.intent.category.CAR_LAUNCHER",
)

@Suppress("unused")
val xRenameAppPatch = resourcePatch(
    name = "X Rename App",
    description = "Changes the app name shown by Android launchers. Designed to run after branding patches such as Piko NewX.",
    default = false,
) {
    val appName by stringOption(
        key = "appName",
        default = "Twitter NewX",
        title = "App name",
        description = "The name shown under the app icon and in Android app info.",
        required = true,
        validator = { value -> !value.isNullOrBlank() },
    )

    execute {
        document("AndroidManifest.xml").use { document ->
            val newName = appName?.trim().orEmpty()
            require(newName.isNotEmpty()) { "App name must not be blank" }

            val application =
                document.getElementsByTagName("application").item(0) as? Element
                    ?: error("AndroidManifest.xml does not contain an <application> element")

            application.setAndroidAttribute("label", newName)

            listOf("activity", "activity-alias").forEach { tagName ->
                val components = document.getElementsByTagName(tagName)

                for (index in 0 until components.length) {
                    val component = components.item(index) as Element

                    if (component.hasLauncherIntentFilter()) {
                        component.setAndroidAttribute("label", newName)
                    }
                }
            }
        }
    }
}

private fun Element.hasLauncherIntentFilter(): Boolean {
    val intentFilters = getElementsByTagName("intent-filter")

    for (filterIndex in 0 until intentFilters.length) {
        val intentFilter = intentFilters.item(filterIndex) as Element

        val hasMainAction = intentFilter
            .getElementsByTagName("action")
            .hasAndroidName(ACTION_MAIN)

        val hasLauncherCategory = intentFilter
            .getElementsByTagName("category")
            .hasAnyAndroidName(LAUNCHER_CATEGORIES)

        if (hasMainAction && hasLauncherCategory) return true
    }

    return false
}

private fun org.w3c.dom.NodeList.hasAndroidName(expectedName: String): Boolean =
    hasAnyAndroidName(setOf(expectedName))

private fun org.w3c.dom.NodeList.hasAnyAndroidName(expectedNames: Set<String>): Boolean {
    for (index in 0 until length) {
        val element = item(index) as Element

        if (element.getAndroidAttribute("name") in expectedNames) {
            return true
        }
    }

    return false
}

private fun Element.getAndroidAttribute(name: String): String =
    getAttribute("android:$name").ifEmpty {
        getAttributeNS(ANDROID_NAMESPACE, name)
    }

private fun Element.setAndroidAttribute(name: String, value: String) {
    setAttribute("android:$name", value)
}
