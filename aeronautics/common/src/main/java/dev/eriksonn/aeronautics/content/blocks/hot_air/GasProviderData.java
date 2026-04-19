package dev.eriksonn.aeronautics.content.blocks.hot_air;

// Pure-Java view of a lifting-gas source's autopilot-readable state.
// No computer-mod imports live here, so it can be consumed by any integration
// (CC:Tweaked, OpenComputers, a bridge, …) without forcing that mod into the
// aeronautics classpath.
public interface GasProviderData extends BlockEntityLiftingGasProvider {

    int getSignalStrength();

    int getTargetAmount();

    void setTargetAmount(int amount);

    // 0..1. Non-steam heaters have no boiler, so default to "fully efficient".
    default double getBoilerEfficiency() {
        return 1.0;
    }
}
