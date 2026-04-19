package dev.simulated_team.simulated.compat.computercraft.peripherals;

import dan200.computercraft.api.lua.LuaFunction;
import dev.simulated_team.simulated.content.blocks.portable_engine.PortableEngineBlockEntity;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class PortableEnginePeripheral extends SimPeripheral<PortableEngineBlockEntity> {

    public PortableEnginePeripheral(final PortableEngineBlockEntity blockEntity) {
        super(blockEntity);
    }

    @Override
    public String getType() {
        return "portable_engine";
    }

    // --- Fuel state ---

    @LuaFunction
    public int getBurnTime() {
        return this.blockEntity.getCurrentBurnTime();
    }

    @LuaFunction
    public int getTotalBurnTime() {
        return this.blockEntity.getTotalBurnTime();
    }

    @LuaFunction
    public boolean isSuperHeated() {
        return this.blockEntity.isSuperHeated();
    }

    @LuaFunction
    public boolean isCurrentFuelInfinite() {
        return this.blockEntity.isCurrentFuelInfinite();
    }

    @LuaFunction
    public boolean isTotalFuelInfinite() {
        return this.blockEntity.isTotalFuelInfinite();
    }

    // --- Engine state ---

    @LuaFunction
    public boolean isLit() {
        return this.blockEntity.getBlockState().getValue(BlockStateProperties.LIT);
    }

    // --- Kinetic output ---

    @LuaFunction
    public float getSpeed() {
        return this.blockEntity.getSpeed();
    }

    @LuaFunction
    public float getGeneratedSpeed() {
        return this.blockEntity.getGeneratedSpeed();
    }

    @LuaFunction
    public float getStressCapacity() {
        return this.blockEntity.calculateAddedStressCapacity();
    }
}
