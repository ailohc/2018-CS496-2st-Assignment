package cs496.second.home

class Contact {

    var name: String
    var phone: String
    var email: String
    var facebook: String
    var profileImage: String

    constructor(name: String) {
        this.name = name
        this.phone = ""
        this.email = ""
        this.facebook = ""
        this.profileImage = ""
    }

    constructor(name: String, phone: String, email: String, facebook: String, profileImage: String) {
        this.name = name
        this.phone = phone
        this.email = email
        this.facebook = facebook
        this.profileImage = profileImage
    }

    fun setName(name: String): Contact {
        this.name = name
        return this
    }

    fun setPhone(phone: String): Contact {
        this.phone = phone
        return this
    }

    fun setEmail(email: String): Contact {
        this.email = email
        return this
    }

    fun setFacebook(facebook: String): Contact {
        this.facebook = facebook
        return this
    }

    fun setProfileImage(profileImage: String): Contact {
        this.profileImage = profileImage
        return this
    }
}
