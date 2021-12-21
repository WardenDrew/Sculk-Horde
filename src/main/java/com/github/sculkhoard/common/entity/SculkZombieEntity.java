package com.github.sculkhoard.common.entity;

import com.github.sculkhoard.core.EntityRegistry;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.monster.ZombifiedPiglinEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Difficulty;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

import java.util.Random;

public class SculkZombieEntity extends MonsterEntity implements IAnimatable {

    /* NOTE: In order to create a mob, there is a lot of things that need to be created/modified
     * For this entity, I created/modified the following files:
     * Edited core/ EntityRegistry.java
     * Edited util/ ModEventSubscriber.java
     * Edited client/ ClientModEventSubscriber.java
     * Edited common/world/ModWorldEvents.java
     * Edited common/world/gen/ModEntityGen.java
     * Added common/entity/ SculkZombieEntity.java
     * Added client/model/entity/ SculkZombieModel.java
     * Added client/renderer/entity/ SculkZombieRenderer.java
     */

    /* SPAWN_WEIGHT
     * Used to determine spawn rarity
     * Zombies = 100
     * Sheep = 12
     * Endermen = 10
     * Cows = 8
     * Witches = 5
     */
    public static int SPAWN_WEIGHT = 100;

    //Used to Determine the minimum amount of this mob that will spawn in a group
    public static int SPAWN_MIN = 1;

    //Used to Determine the maximum amount of this mob that will spawn in a group
    public static int SPAWN_MAX = 3;

    //The Max Y-level that this mob can spawn (Diamonds spawn at 14)
    public static int SPAWN_Y_MAX = 15;

    private AnimationFactory factory = new AnimationFactory(this);

    //Main Constructor
    public SculkZombieEntity(EntityType<? extends SculkZombieEntity> type, World worldIn) {
        super(type, worldIn);
    }

    //Constructor where you only have to specify the world.
    public SculkZombieEntity(World worldIn) {super(EntityRegistry.SCULK_ZOMBIE.get(), worldIn);}

    /* createAttributes
     * @description A function that is called in ModEventSubscriber.java to give
     * this mob its attributes.
     */
    public static AttributeModifierMap.MutableAttribute createAttributes()
    {
        return LivingEntity.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.ARMOR, 4.0D)
                .add(Attributes.ATTACK_DAMAGE, 3.0D)
                .add(Attributes.ATTACK_KNOCKBACK, 1.0D)
                .add(Attributes.FOLLOW_RANGE,25.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D);
    }

    /* passSpawnCondition
     * @description determines whether a given possible spawn location meets your criteria
     * @param config ???
     * @param world The dimension the mob is attempting to be spawned in??
     * @param reason Specifies on why a mob is attempting to be spawned.
     * @param pos The Block Coordinates that the mob is being attempted to spawn at.
     * @param random ???
     */
    public static boolean passSpawnCondition(EntityType<? extends CreatureEntity> config, IWorld world, SpawnReason reason, BlockPos pos, Random random)
    {
        // peaceful check
        if (world.getDifficulty() == Difficulty.PEACEFUL) return false;
        // pass through if natural spawn and using individual spawn rules
        else if (reason != SpawnReason.CHUNK_GENERATION && reason != SpawnReason.NATURAL) return false;
        else if (pos.getY() > SPAWN_Y_MAX) return false;
        return true;
    }

    /* registerGoals
     * @description Registers Goals with the entity. The goals determine how an AI behaves ingame.
     * Each goal has a priority with 0 being the highest and as the value increases, the priority is lower.
     * You can manually add in goals in this function, however, I made an automatic system for this.
     */
    @Override
    public void registerGoals() {

        Goal[] goalSelectorPayload = goalSelectorPayload();
        for(int priority = 0; priority < goalSelectorPayload.length; priority++)
        {
            this.goalSelector.addGoal(priority, goalSelectorPayload[priority]);
        }

        Goal[] targetSelectorPayload = targetSelectorPayload();
        for(int priority = 0; priority < targetSelectorPayload.length; priority++)
        {
            this.goalSelector.addGoal(priority, targetSelectorPayload[priority]);
        }

    }

    /* goalSelectorPayload
     * @description Prepares an array of goals to give to registerGoals() for the goalSelector.
     * The purpose was to make registering goals simpler by automatically determining priority
     * based on the order of the items in the array. First element is of priority 0, which
     * represents highest priority. Priority value then increases by 1, making each element
     * less of a priority than the last.
     * @return Goal[] Returns an array of goals ordered by priority
     */
    public Goal[] goalSelectorPayload()
    {
        Goal[] goals =
                {
                        //SwimGoal(mob)
                        new SwimGoal(this),
                        //MeleeAttackGoal(mob, speedModifier, followingTargetEvenIfNotSeen)
                        new MeleeAttackGoal(this, 1.0D, false),
                        //MoveTowardsTargetGoal(mob, speedModifier, within) THIS IS FOR NON-ATTACKING GOALS
                        new MoveTowardsTargetGoal(this, 0.8F, 20F),
                        //WaterAvoidingRandomWalkingGoal(mob, speedModifier)
                        new WaterAvoidingRandomWalkingGoal(this, 1.0D),
                        //LookAtGoal(mob, targetType, lookDistance)
                        new LookAtGoal(this, PigEntity.class, 8.0F),
                        //LookRandomlyGoal(mob)
                        new LookRandomlyGoal(this)
                };
        return goals;
    }

    /* targetSelectorPayload
     * @description Prepares an array of goals to give to registerGoals() for the targetSelector.
     * The purpose was to make registering goals simpler by automatically determining priority
     * based on the order of the items in the array. First element is of priority 0, which
     * represents highest priority. Priority value then increases by 1, making each element
     * less of a priority than the last.
     * @return Goal[] Returns an array of goals ordered by priority
     */

    public Goal[] targetSelectorPayload()
    {
        Goal[] goals =
                {
                        //HurtByTargetGoal(mob)
                        new HurtByTargetGoal(this).setAlertOthers(),
                        //NearestAttackableTargetGoal(Mob, targetType, mustSee)
                        new NearestAttackableTargetGoal<>(this, PlayerEntity.class, true),
                        //NearestAttackableTargetGoal(Mob, targetType, mustSee)
                        new NearestAttackableTargetGoal<>(this, IronGolemEntity.class, true),

                };
        return goals;
    }



    /*
    @Override
    protected int getExperienceReward(PlayerEntity player)
    {
        return 3;
    }


    @Override
    public boolean doHurtTarget(Entity entityIn)
    {
        boolean flag = super.doHurtTarget(entityIn);
        if(!flag)
        {
            return false;
        }
        else
        {
            if(entityIn instanceof LivingEntity)
            {
                ((LivingEntity) entityIn).addEffect(new EffectInstance(Effects.MOVEMENT_SLOWDOWN, 280, 1));
            }
            return true;
        }
    }
    */
    //Animation Related Functions

    private <E extends IAnimatable> PlayState predicate(AnimationEvent<E> event)
    {
        //event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.bat.fly", true));
        return PlayState.STOP;
    }

    @Override
    public void registerControllers(AnimationData data) {
        data.addAnimationController(new AnimationController(this, "controller", 0, this::predicate));
    }

    @Override
    public AnimationFactory getFactory() {
        return this.factory;
    }
}
