package dev.eriksonn.aeronautics.compat.computercraft;

import dev.simulated_team.simulated.service.SimModCompatibilityService;

// Intentionally has ZERO CC:Tweaked references so that ServiceLoader reflection
// over this class succeeds when CC is absent. Actual registration lives in
// AeroComputerCraftIntegration, only class-loaded once init() runs (which is
// gated by isLoaded("computercraft") in SimModCompatibilityService.initLoaded).
public class AeroComputerCraftPeripherals implements SimModCompatibilityService {

    @Override
    public void init() {
        AeroComputerCraftIntegration.register();
    }

    @Override
    public String getModId() {
        return "computercraft";
    }
}
