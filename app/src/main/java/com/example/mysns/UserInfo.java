package com.example.mysns;

public class UserInfo {
    public String uid;
    public String nickname;
    public String birthday;
    public String profileImgUrl;

    public UserInfo(String uid, String nickname, String birthday, String profileImgUrl){
        uid = this.uid;
        nickname = this.nickname;
        birthday = this.birthday;
        profileImgUrl = this.profileImgUrl;
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
