/*
 * Copyright (c) 2021. MachineMuse, Lehjr
 *  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *      Redistributions of source code must retain the above copyright notice, this
 *      list of conditions and the following disclaimer.
 *
 *     Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation
 *     and/or other materials provided with the distribution.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 *  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 *  ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package mpsremodularized.common.capabilities.module.powermodule;

import mpsremodularized.common.constants.MPSConstants;
import net.minecraft.network.chat.Component;

/**
 * The module categories
 */
public enum ModuleCategory {
    NONE("none", "None"),
    DEBUG("debug", "Debug"),
    ARMOR("armor", "Armor", true, false),
    ENERGY_STORAGE("energystorage", "Energy_Storage", true, true),
    ENERGY_GENERATION("energygeneration", "Energy_Generation", true, true),
    FLUID_STORAGE("fluidstorage", "Fluid_Storage", true, true),
    TOOL("tool", "Tool"),

    // FIXME/TODO finish:
    PICKAXE("pickaxe", "PickAxe", true, false),
    SHOVEL("shovel", "Shovel", true, false),
    AXE("axe", "Axe", true, false),
    HOE("hoe", "Hoe", true, false),
    PAXEL("paxel", "Paxel", true, false),
    CONSTRUCTION_STICK("construction_stick", "Construction_Stick"),
//    SHEARS("shears", "Shears"), // maybe?

    WEAPON("weapon", "Weapon"),
    MOVEMENT("movement", "Movement"),
    COSMETIC("cosmetic", "Cosmetic"),
    VISION("vision", "Vision"),
    ENVIRONMENTAL("environment", "Environment"),
    SPECIAL("special", "Special"),
    MINING_ENHANCEMENT("miningenhancement", "Mining_Enhancement"),
    MINING_ENCHANTMENT("miningenchantment", "Mining_Enchantment");;
    private final String configTitle;
    private final String translationKey;
    private final boolean hasContainerIcon;
    private final boolean usesGuiAtlasIcons;

    ModuleCategory(String translationString, String configTitle) {
        this(translationString, configTitle, false, false);
    }

    ModuleCategory(String translationString, String configTitle, boolean hasContainerIcon, boolean usesGuiAtlasIcons) {
        this.translationKey = MPSConstants.MOD_ID + ".module.category." + translationString;
        this.configTitle = configTitle;
        this.hasContainerIcon = hasContainerIcon;
        this.usesGuiAtlasIcons = usesGuiAtlasIcons;
    }

    public Component getTranslation() {
        return Component.translatable(translationKey);
    }
    
    public String getTranslationKey() {
        return translationKey;
    }

    public String getConfigTitle() {
        return configTitle;
    }

    public boolean hasContainerIcon() {
        return hasContainerIcon;
    }

    public boolean usesGuiAtlasIcons() {
        return usesGuiAtlasIcons;
    }



}
