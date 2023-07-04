package io.github.jodlodi.minions;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class MinReflections {
    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();
    private static final MethodHandle AMBIENT_SOUND;
    private static final MethodHandle SOUND_VOLUME;

    static {
        MethodHandle tempAmbientSound = null;
        MethodHandle tempSoundVolume = null;

        try {
            tempAmbientSound = LOOKUP.unreflect(ObfuscationReflectionHelper.findMethod(Mob.class, "m_7515_"));
            tempSoundVolume = LOOKUP.unreflect(ObfuscationReflectionHelper.findMethod(LivingEntity.class, "m_6121_"));
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        AMBIENT_SOUND = tempAmbientSound;
        SOUND_VOLUME = tempSoundVolume;
    }

    @Nullable
    public static SoundEvent getAmbientSound(Mob mob) {
        try {
            return (SoundEvent)AMBIENT_SOUND.invokeExact(mob);
        } catch (Throwable e) {
            return null;
        }
    }

    public static float getSoundVolume(LivingEntity living) {
        try {
            return (Float)SOUND_VOLUME.invokeExact(living);
        } catch (Throwable e) {
            return 1.0F;
        }
    }
}
