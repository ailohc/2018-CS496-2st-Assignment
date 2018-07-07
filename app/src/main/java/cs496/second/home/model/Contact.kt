package cs496.second.home.model

import android.os.Parcel
import android.os.Parcelable

class Contact (var name: String?, var phone: String?, var email: String?, var facebook: String?, var profileImage: String?) : Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString()) {
        name = parcel.readString()
        phone = parcel.readString()
        email = parcel.readString()
        facebook = parcel.readString()
        profileImage = parcel.readString()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeString(phone)
        parcel.writeString(email)
        parcel.writeString(facebook)
        parcel.writeString(profileImage)
        parcel.writeString(name)
        parcel.writeString(phone)
        parcel.writeString(email)
        parcel.writeString(facebook)
        parcel.writeString(profileImage)
    }

    override fun describeContents(): Int {
        return 0
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

    companion object CREATOR : Parcelable.Creator<Contact> {
        override fun createFromParcel(parcel: Parcel): Contact {
            return Contact(parcel)
        }

        override fun newArray(size: Int): Array<Contact?> {
            return arrayOfNulls(size)
        }
    }
}