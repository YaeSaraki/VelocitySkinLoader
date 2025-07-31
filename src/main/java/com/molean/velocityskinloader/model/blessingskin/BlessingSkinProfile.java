package com.molean.velocityskinloader.model.blessingskin;

import lombok.Data;
import lombok.Getter;

public class BlessingSkinProfile {
    private String username;
    @Getter
    private Skins skins;

    public BlessingSkinProfile(String username, Skins skins) {
        this.username = username;
        this.skins = skins;
    }

}
