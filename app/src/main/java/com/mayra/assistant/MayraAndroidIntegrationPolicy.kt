package com.mayra.assistant
object MayraAndroidIntegrationPolicy {
 enum class Surface { APPS, FILES, SETTINGS, NOTIFICATIONS, CAMERA, MICROPHONE, NETWORK }
 fun permissionRequired(surface:Surface)=true
 fun rule()="Android actions require OS permissions and Mayra authorization; sensitive changes remain Owner-controlled."
}