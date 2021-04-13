package link.netless.flat.data.model

import com.google.gson.annotations.SerializedName

data class UserInfoWithToken(
    val name: String,
    val sex: String,
    val avatar: String,
    @SerializedName("userUUID") val uuid: String,
    val token: String
)