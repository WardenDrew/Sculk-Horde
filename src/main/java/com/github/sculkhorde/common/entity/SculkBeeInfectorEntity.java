package com.github.sculkhorde.common.entity;

import com.github.sculkhorde.common.entity.infection.CursorSurfaceInfectorEntity;
import com.github.sculkhorde.core.BlockRegistry;
import com.github.sculkhorde.core.EntityRegistry;
import com.github.sculkhorde.core.SculkHorde;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraftforge.common.Tags;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.constant.DefaultAnimations;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.function.Predicate;

public class SculkBeeInfectorEntity extends SculkBeeHarvesterEntity implements GeoEntity {

    /**
     * In order to create a mob, the following java files were created/edited.<br>
     * Edited core/ EntityRegistry.java<br>
     * Edited util/ ModEventSubscriber.java<br>
     * Edited client/ ClientModEventSubscriber.java<br>
     * Added common/entity/ SculkBeeInfectorEntity.java<br>
     * Added client/model/entity/ SculkBeeInfectorModel.java<br>
     * Added client/renderer/entity/ SculkBeeInfectorRenderer.java
     */

    //The Health
    public static final float MAX_HEALTH = 20F;
    //FOLLOW_RANGE determines how far away this mob can see and chase enemies
    public static final float FOLLOW_RANGE = 25F;
    //MOVEMENT_SPEED determines how fast this mob moves
    public static final float MOVEMENT_SPEED = 0.5F;

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    /**
     * The Constructor
     * @param type The Mob Type
     * @param worldIn The world to initialize this mob in
     */
    public SculkBeeInfectorEntity(EntityType<? extends SculkBeeInfectorEntity> type, Level worldIn) {
        super(type, worldIn);
    }

    /**
     * An Easier Constructor where you do not have to specify the Mob Type
     * @param worldIn  The world to initialize this mob in
     */
    public SculkBeeInfectorEntity(Level worldIn) {super(EntityRegistry.SCULK_BEE_INFECTOR.get(), worldIn);}

    /**
     * Determines & registers the attributes of the mob.
     * @return The Attributes
     */
    public static AttributeSupplier.Builder createAttributes()
    {
        return LivingEntity.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, MAX_HEALTH)
                .add(Attributes.FOLLOW_RANGE,FOLLOW_RANGE)
                .add(Attributes.MOVEMENT_SPEED, MOVEMENT_SPEED)
                .add(Attributes.FLYING_SPEED, 1.5F);
    }

    private final Predicate<BlockState> IS_VALID_FLOWER = (blockState) -> {
        if (blockState.hasProperty(BlockStateProperties.WATERLOGGED) && blockState.getValue(BlockStateProperties.WATERLOGGED)) {
            return false;
        } else if (blockState.is(BlockTags.FLOWERS)) {
            if (blockState.is(Blocks.SUNFLOWER)) {
                return blockState.getValue(DoublePlantBlock.HALF) == DoubleBlockHalf.UPPER;
            } else {
                return true;
            }
        }
        else if(blockState.is(Blocks.GRASS) || blockState.is(Blocks.TALL_GRASS) || blockState.is(Blocks.FERN))
        {
            return true;
        }
        else {
            return false;
        }
    };

    @Override
    public Predicate<BlockState> getIsFlowerValidPredicate() {
        return this.IS_VALID_FLOWER;
    }

    @Override
    protected void executeCodeOnPollination()
    {
        CursorSurfaceInfectorEntity cursor = new CursorSurfaceInfectorEntity(level);
        cursor.setPos(this.blockPosition().getX(), this.blockPosition().getY() - 1, this.blockPosition().getZ());
        cursor.setMaxInfections(100);
        cursor.setMaxRange(100);
        cursor.setTickIntervalMilliseconds(500);
        cursor.setSearchIterationsPerTick(10);
        level.addFreshEntity(cursor);
    }

    /** ~~~~~~~~ ANIMATION ~~~~~~~~ **/

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(DefaultAnimations.genericFlyController(this));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }


    @Override
    public boolean isFlying() {
        return true;
    }

    public boolean dampensVibrations() {
        return true;
    }


    /**
     * If a sculk living entity despawns, refund it's current health to the sculk hoard
     */
    @Override
    public void onRemovedFromWorld() {
        SculkHorde.gravemind.getGravemindMemory().addSculkAccumulatedMass((int) this.getHealth());
        super.onRemovedFromWorld();
    }

}
