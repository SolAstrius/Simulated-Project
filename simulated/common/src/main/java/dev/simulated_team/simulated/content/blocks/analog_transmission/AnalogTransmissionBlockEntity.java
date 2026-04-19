package dev.simulated_team.simulated.content.blocks.analog_transmission;

import com.simibubi.create.content.kinetics.base.IRotate;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.simpleRelays.ICogWheel;
import com.simibubi.create.foundation.item.TooltipHelper;
import com.simibubi.create.infrastructure.config.AllConfigs;
import dev.simulated_team.simulated.data.SimLang;
import dev.simulated_team.simulated.mixin_interface.extra_kinetics.KineticBlockEntityExtension;
import dev.simulated_team.simulated.util.extra_kinetics.ExtraBlockPos;
import dev.simulated_team.simulated.util.extra_kinetics.ExtraKinetics;
import net.createmod.catnip.lang.FontHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.VisibleForTesting;

import java.util.List;

import static net.minecraft.ChatFormatting.GOLD;

/**
 * The parent BlockEntity class. implements {@link ExtraKinetics ExtraKinetics} to allow multi-kinetic functionality
 */
public class AnalogTransmissionBlockEntity extends KineticBlockEntity implements ExtraKinetics {

    /**
     * The ExtraKinetic BlockEntity associated with the AnalogTransmission
     */
    private final AnalogTransmissionCogwheel extraWheel;

    private int signal = 0;

    /**
     * When true, the signal is owned by a ComputerCraft peripheral and redstone input is ignored
     * until {@code setExternallyControlled(false)} is called.
     */
    private boolean externallyControlled = false;

    /**
     * Set whenever the analog transmission disconnects due to overspeeding
     */
    private boolean oversaturated = false;
    boolean alreadySentEffects = false;

    public AnalogTransmissionBlockEntity(final BlockEntityType<?> typeIn, final BlockPos pos, final BlockState state) {
        super(typeIn, pos, state);

        //set our ExtraKientic BlockEntity and set the proper BlockState
        this.extraWheel = new AnalogTransmissionCogwheel(typeIn, new ExtraBlockPos(pos), state, this);
    }

    /**
     * Required override, as we need our ExtraKinetic BlockEntity to tick
     */
    @Override
    public void tick() {
        if (!this.getLevel().isClientSide) {
            if (!this.externallyControlled) {
                final int bestNeighborSignal = this.getLevel().getBestNeighborSignal(this.getBlockPos());
                if (bestNeighborSignal != this.signal) {
                    this.applySignal(bestNeighborSignal);
                }
            }
        } else if (this.oversaturated) {
            if (!this.alreadySentEffects) {
                this.alreadySentEffects = true;
                this.effects.triggerOverStressedEffect();
            }
        } else {
            this.alreadySentEffects = false;
        }

        this.extraWheel.tick();
        super.tick();
    }

    /**
     * Change the active signal and re-wire the kinetic network. Callable from tick() (redstone path)
     * and from the ComputerCraft peripheral (external path).
     */
    public void applySignal(final int newSignal) {
        if (newSignal == this.signal) return;

        this.detachKinetics();
        this.extraWheel.detachKinetics();

        this.removeSource();
        this.extraWheel.removeSource();

        this.signal = newSignal;
        this.getLevel().setBlockAndUpdate(this.getBlockPos(), this.getBlockState().setValue(AnalogTransmissionBlock.POWERED, this.signal > 0));

        if (((KineticBlockEntityExtension) this).simulated$getConnectedToExtraKinetics()) {
            this.attachKinetics();
            this.extraWheel.attachKinetics();
        } else {
            this.extraWheel.attachKinetics();
            this.attachKinetics();
        }

        this.sendData();
    }

    public int getSignal() {
        return this.signal;
    }

    public boolean isOversaturated() {
        return this.oversaturated;
    }

    public boolean isExternallyControlled() {
        return this.externallyControlled;
    }

    public void setExternallyControlled(final boolean externallyControlled) {
        this.externallyControlled = externallyControlled;
    }

    @VisibleForTesting
    public float getRotationModifier() {
        return 1 - (this.signal + 1) / 16f;
    }

    /**
     * This propagateRotationTo handles both the AnalogTransmission's modifier towards the ExtraKientic BlockEntity, and vise versa
     */
    @Override
    public float propagateRotationTo(final KineticBlockEntity target, final BlockState stateFrom, final BlockState stateTo, final BlockPos diff, final boolean connectedViaAxes, final boolean connectedViaCogs) {
        float gatheredRotationModifier = 0;
        if (this.signal != 15) {
            if (target == this.extraWheel) { //reduce speed
                gatheredRotationModifier = this.signal == 0 ? 1 : this.getRotationModifier();
                if (this.oversaturated) {
                    return 0;
                }
            } else if (target == this) { //increase speed
                gatheredRotationModifier = this.signal == 0 ? 1 : (1 / this.getRotationModifier());

                if (Math.abs(this.extraWheel.getTheoreticalSpeed() * gatheredRotationModifier) > AllConfigs.server().kinetics.maxRotationSpeed.get()) {
                    this.oversaturated = true;
                    return 0;
                } else {
                    this.oversaturated = false;
                }
            }
        } else {
            this.oversaturated = false;
        }

        return gatheredRotationModifier;
    }

    @Override
    protected void write(final CompoundTag compound, final HolderLookup.Provider registries, final boolean clientPacket) {
        super.write(compound, registries, clientPacket);

        compound.putInt("Signal", this.signal);
        compound.putBoolean("Oversaturated", this.oversaturated);
        if (!clientPacket) {
            compound.putBoolean("ExternallyControlled", this.externallyControlled);
        }
    }

    @Override
    protected void read(final CompoundTag compound, final HolderLookup.Provider registries, final boolean clientPacket) {
        super.read(compound, registries, clientPacket);

        this.signal = compound.getInt("Signal");
        this.oversaturated = compound.getBoolean("Oversaturated");
        if (!clientPacket) {
            this.externallyControlled = compound.getBoolean("ExternallyControlled");
        }
    }

    @Override
    public boolean isOverStressed() {
        if (this.level.isClientSide) {
            return this.oversaturated || this.overStressed;
        }

        return super.isOverStressed();
    }

    /**
     * Accesses the ExtraKinetic BlockEntity associated with the AnalogTransmission
     */
    @Override
    public @NotNull KineticBlockEntity getExtraKinetics() {
        return this.extraWheel;
    }

    @Override
    //See javaDoc
    public boolean shouldConnectExtraKinetics() {
        return true;
    }

    @Override
    public String getExtraKineticsSaveName() {
        return "ExtraCogwheel";
    }

    @Override
    public boolean addToTooltip(final List<Component> tooltip, final boolean isPlayerSneaking) {
        if (this.oversaturated) {
            SimLang.translate("analog_transmission.too_fast")
                    .style(GOLD)
                    .forGoggles(tooltip);

            final MutableComponent component = SimLang.translate("analog_transmission.too_fast_error")
                    .component();

            final List<Component> cutString = TooltipHelper.cutTextComponent(component, FontHelper.Palette.GRAY_AND_WHITE);
            tooltip.addAll(cutString);

            return true;
        }

        return super.addToTooltip(tooltip, isPlayerSneaking);
    }

    /**
     * The ExtraKinetic BlockEntity for the AnalogTransmission. Extends KineticBlockEntity (Can be any other KBE), and implements ExtraKinetics
     */
    public static class AnalogTransmissionCogwheel extends KineticBlockEntity implements ExtraKineticsBlockEntity {

        public static final ICogWheel EXTRA_COGWHEEL_CONFIG = new ICogWheel() {
            @Override
            public boolean hasShaftTowards(final LevelReader world, final BlockPos pos, final BlockState state, final Direction face) {
                return false;
            }

            @Override
            public Direction.Axis getRotationAxis(final BlockState state) {
                return state.getValue(AnalogTransmissionBlock.AXIS);
            }
        };

        /**
         * Access to the parent BlockEntity to avoid called {@link Level#getBlockEntity(BlockPos) getBlockEntity} unnecessarily
         */
        private final KineticBlockEntity parentBlockEntity;

        /**
         * @param pos An ExtraBlockPos associated with this ExtraKinetic BlockEntity. This is needed to inform the {@link com.simibubi.create.content.kinetics.RotationPropagator} that this BlockEntity is an ExtraKinetic one.
         */
        public AnalogTransmissionCogwheel(final BlockEntityType<?> typeIn, final ExtraBlockPos pos, final BlockState state, final KineticBlockEntity parentBlockEntity) {
            super(typeIn, pos, state);
            this.parentBlockEntity = parentBlockEntity;
        }

        /**
         * We call the parent's {@link KineticBlockEntity#propagateRotationTo(KineticBlockEntity, BlockState, BlockState, BlockPos, boolean, boolean) propagateRotationTo} here for easier rotation modifier Handling.
         */
        @Override
        public float propagateRotationTo(final KineticBlockEntity target, final BlockState stateFrom, final BlockState stateTo, final BlockPos diff, final boolean connectedViaAxes, final boolean connectedViaCogs) {
            return this.parentBlockEntity.propagateRotationTo(target, stateFrom, stateTo, diff, connectedViaAxes, connectedViaCogs);
        }

        @Override
        protected boolean canPropagateDiagonally(final IRotate block, final BlockState state) {
            return true;
        }

        @Override
        public KineticBlockEntity getParentBlockEntity() {
            return this.parentBlockEntity;
        }
    }
}
