package dev.simulated_team.simulated.compat.computercraft.peripherals;

import dan200.computercraft.api.lua.LuaFunction;
import dev.simulated_team.simulated.content.blocks.analog_transmission.AnalogTransmissionBlock;
import dev.simulated_team.simulated.content.blocks.analog_transmission.AnalogTransmissionBlockEntity;
import net.minecraft.util.Mth;

public class AnalogTransmissionPeripheral extends SimPeripheral<AnalogTransmissionBlockEntity> {

    public AnalogTransmissionPeripheral(final AnalogTransmissionBlockEntity blockEntity) {
        super(blockEntity);
    }

    @Override
    public String getType() {
        return "analog_transmission";
    }

    // --- Control ---

    @LuaFunction
    public int getSignal() {
        return this.blockEntity.getSignal();
    }

    @LuaFunction(mainThread = true)
    public final void setSignal(final int signal) {
        this.blockEntity.setExternallyControlled(true);
        this.blockEntity.applySignal(Mth.clamp(signal, 0, 15));
    }

    @LuaFunction(mainThread = true)
    public final void releaseSignal() {
        this.blockEntity.setExternallyControlled(false);
    }

    @LuaFunction
    public boolean isExternallyControlled() {
        return this.blockEntity.isExternallyControlled();
    }

    // --- Rotation state ---

    @LuaFunction
    public float getRotationModifier() {
        return this.blockEntity.getRotationModifier();
    }

    @LuaFunction
    public float getInputSpeed() {
        return this.blockEntity.getSpeed();
    }

    @LuaFunction
    public float getOutputSpeed() {
        return this.blockEntity.getExtraKinetics().getSpeed();
    }

    @LuaFunction
    public float getOutputTheoreticalSpeed() {
        return this.blockEntity.getExtraKinetics().getTheoreticalSpeed();
    }

    // --- Stress ---

    @LuaFunction
    public float getStressApplied() {
        return this.blockEntity.calculateStressApplied();
    }

    @LuaFunction
    public float getOutputStressApplied() {
        return this.blockEntity.getExtraKinetics().calculateStressApplied();
    }

    // --- Health ---

    @LuaFunction
    public boolean isOverstressed() {
        return this.blockEntity.isOverStressed();
    }

    @LuaFunction
    public boolean isOversaturated() {
        return this.blockEntity.isOversaturated();
    }

    // --- Orientation ---

    @LuaFunction
    public String getAxis() {
        return this.blockEntity.getBlockState().getValue(AnalogTransmissionBlock.AXIS).getSerializedName();
    }
}
