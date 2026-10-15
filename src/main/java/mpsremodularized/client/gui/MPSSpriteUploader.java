package mpsremodularized.client.gui;

import mpsremodularized.common.constants.MPSConstants;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.TextureAtlasHolder;
import net.minecraft.resources.ResourceLocation;

public class MPSSpriteUploader extends TextureAtlasHolder {
    static final ResourceLocation GUI_ATLAS = ResourceLocation.fromNamespaceAndPath(MPSConstants.MOD_ID,"gui");

    public MPSSpriteUploader(TextureManager textureManager) {
        super(textureManager, MPSConstants.LOCATION_MPS_GUI_TEXTURE_ATLAS, GUI_ATLAS);
    }

    /**
     * Overridden to make it public
     */
    @Override
    public TextureAtlasSprite getSprite(ResourceLocation location) {
        return super.getSprite(location);
    }

    public TextureAtlas getAtlas() {
        return super.textureAtlas;
    }
}
