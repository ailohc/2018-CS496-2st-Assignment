package cs496.second.home;

public class Contact {

    public String name, phone, email, profileImage;

    public Contact(String name) {
        this.name = name;
        this.phone = "";
        this.email = "";
        this.profileImage = "";
    }

    public Contact(String name, String phone, String email, String profileImage) {
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.profileImage = profileImage;
    }

    public Contact setName(String name) {
        this.name = name;
        return this;
    }

    public Contact setPhone(String phone) {
        this.phone = phone;
        return this;
    }

    public Contact setEmail(String email) {
        this.email = email;
        return this;
    }


    public Contact setProfileImage(String profileImage) {
        this.profileImage = profileImage;
        return this;
    }
}
