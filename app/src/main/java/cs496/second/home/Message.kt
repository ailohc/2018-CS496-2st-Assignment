package cs496.second.home


import java.io.Serializable

class Message : Serializable {
    var id: String = ""
    var message: String = ""


    constructor() {}

    constructor(id: String, message: String, createdAt: String) {
        this.id = id
        this.message = message


    }


}

