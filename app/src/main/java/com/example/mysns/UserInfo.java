package com.example.mysns;

public class UserInfo {
    private String uid;
    private String email;
    private String nickname;
    private String birthday;
    private String profileImgUrl;

    public UserInfo(String uid, String email, String nickname, String birthday, String profileImgUrl){
        this.uid = uid;
        this.email = email;
        this.nickname = nickname;
        this.birthday = birthday;
        this.profileImgUrl =  profileImgUrl;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public String getProfileImgUrl() {
        return profileImgUrl;
    }

    public void setProfileImgUrl(String profileImgUrl) {
        this.profileImgUrl = profileImgUrl;
    }
}
