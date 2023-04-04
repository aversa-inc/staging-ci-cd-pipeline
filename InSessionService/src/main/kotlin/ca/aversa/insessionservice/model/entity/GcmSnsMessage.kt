package ca.aversa.insessionservice.model.entity

import com.google.gson.annotations.SerializedName

data class GcmSnsMessage(

    @SerializedName("GCM")
    var payload: String
)

data class GcmNotificationWrapper(

    @SerializedName("notification")
    var notification: GcmNotificationPayload
)

data class GcmNotificationPayload(

    @SerializedName("title")
    var title: String = "",

    @SerializedName("body")
    var body: String = "",

    @SerializedName("sound")
    var sound: String = "default"
)
