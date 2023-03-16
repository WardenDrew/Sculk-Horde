package com.github.sculkhorde.client.renderer.entity;

import com.github.sculkhorde.common.entity.infection.CursorLongRangeEntity;
import com.github.sculkhorde.common.entity.infection.CursorShortRangeEntity;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;

public class CursorShortRangeRenderer extends EntityRenderer<CursorShortRangeEntity> {
    public CursorShortRangeRenderer(EntityRendererManager p_i46179_1_) {
        super(p_i46179_1_);
    }

    /**
     * Returns the location of an entity's texture.
     *
     * @param pEntity
     */
    @Override
    public ResourceLocation getTextureLocation(CursorShortRangeEntity pEntity) {
        return null;
    }
}
