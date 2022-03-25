package model

import java.io.Serializable

class User(var name: String, var email: String, var password: String, var uid: String) {

    constructor() : this("", "", "", "")
}