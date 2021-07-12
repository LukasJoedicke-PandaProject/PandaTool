package xyz.vitox.discordtool.discordAPI.tokenManager;

import xyz.vitox.discordtool.discordAPI.useragents.RandomUserAgent;

public class Token {

    private String name, discriminator, id, email, avatar, phoneNumber, verified, token, fingerprint;
    private RandomUserAgent randomUserAgent;

    public Token(String name, String discriminator, String id, String email, String avatar, String phoneNumber, String verified, String token, String fingerprint, RandomUserAgent randomUserAgent) {
        this.name = name;
        this.discriminator = discriminator;
        this.id = id;
        this.email = email;
        this.avatar = avatar;
        this.phoneNumber = phoneNumber;
        this.verified = verified;
        this.token = token;
        this.fingerprint = fingerprint;
        this.randomUserAgent = randomUserAgent;
    }

    public RandomUserAgent getRandomUserAgent() {
        return randomUserAgent;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getVerified() {
        return verified;
    }

    public void setVerified(String verified) {
        this.verified = verified;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDiscriminator() {
        return discriminator;
    }

    public void setDiscriminator(String discriminator) {
        this.discriminator = discriminator;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getFingerprint() {
        return fingerprint;
    }
}

