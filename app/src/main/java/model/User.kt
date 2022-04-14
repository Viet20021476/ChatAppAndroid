package model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.io.Serializable

@Parcelize
class User(
    var name: String,
    var email: String,
    var password: String,
    var uid: String,
    var profileImgUrl: String,
    var status: String,
    var lastOnline: String,
    var timeStamp: Map<String, String>
) : Parcelable {

    constructor() : this("", "", "", "", "")

    constructor(
        name: String,
        email: String,
        password: String,
        uid: String,
        profileImgUrl: String
    ) : this(
        name,
        email,
        password,
        uid,
        profileImgUrl,
        "offline",
        "",
        HashMap()
    )
}