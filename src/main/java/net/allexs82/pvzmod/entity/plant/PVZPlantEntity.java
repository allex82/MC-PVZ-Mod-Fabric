package net.allexs82.pvzmod.entity.plant;

import net.allexs82.pvzmod.util.PlantType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.builder.ILoopType;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;

import java.util.List;

public abstract class PVZPlantEntity<T extends IAnimatable> extends MobEntity implements IAnimatable {
    protected final PlantType plantType;

    protected String getIdleAnimName(){
        return "";
    }


    protected PVZPlantEntity(EntityType<? extends MobEntity> entityType, World world, PlantType plantType) {
        super(entityType, world);
        this.plantType = plantType;
    }

    public PVZPlantEntity(EntityType<? extends MobEntity> entityType, World world) {
        super(entityType, world);
        this.plantType = PlantType.DEFAULT;
    }

    protected  <E extends IAnimatable> PlayState predicate(AnimationEvent<T> event) {
        event.getController().setAnimation(new AnimationBuilder().addAnimation(getIdleAnimName(), ILoopType.EDefaultLoopTypes.LOOP));
        return PlayState.CONTINUE;
    }

    @Override
    public void registerControllers(AnimationData animationData) {
        animationData.addAnimationController(new AnimationController<T>((T) this,
                "controller", 0, this::predicate));
    }

    public PlantType getPlantType() {
        return plantType;
    }

    @Override
    public void addVelocity(double deltaX, double deltaY, double deltaZ) {}

    @Override
    protected boolean shouldDropXp() {
        return false;
    }

    @Override
    public boolean cannotDespawn() {
        return true;
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        Entity attacker = source.getAttacker();
        return !(attacker instanceof PlayerEntity || attacker instanceof PVZPlantEntity) && super.damage(source, amount);
    }

    @Override
    protected void tickCramming() {
        List<Entity> list = this.world.getOtherEntities(this, this.getBoundingBox(), EntityPredicates.canBePushedBy(this));
        if (!list.isEmpty()) {
            int j;
            int i = this.world.getGameRules().getInt(GameRules.MAX_ENTITY_CRAMMING);
            for (Entity value : list) {
                if (value instanceof PVZPlantEntity) {
                    i = 1;
                    if (((PVZPlantEntity) value).plantType == PlantType.PROTECTING) i = 2;
                    break;
                }
            }
            if (i > 0 && list.size() > i - 1 && this.random.nextInt(4) == 0) {
                j = 0;
                for (Entity entity : list) {
                    if (entity.hasVehicle()) continue;
                    ++j;
                }
                if (j > i - 1) {
                    this.damage(DamageSource.CRAMMING, 6.0f);
                }
            }
            for (j = 0; j < list.size(); ++j) {
                Entity entity = list.get(j);
                this.pushAway(entity);
            }
        }
    }
}
