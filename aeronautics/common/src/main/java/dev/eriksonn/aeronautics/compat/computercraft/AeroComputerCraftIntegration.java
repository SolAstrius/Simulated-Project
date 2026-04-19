package dev.eriksonn.aeronautics.compat.computercraft;

import dan200.computercraft.api.peripheral.IPeripheral;
import dev.eriksonn.aeronautics.compat.computercraft.peripherals.GasProviderPeripheral;
import dev.eriksonn.aeronautics.index.AeroBlockEntityTypes;
import dev.simulated_team.simulated.service.ServiceUtil;
import dev.simulated_team.simulated.service.compat.SimPeripheralService;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.function.Function;
import java.util.function.Supplier;

// All CC-touching code lives here. Only class-loaded when the compat service's
// init() runs, which happens only when CC is on the classpath.
class AeroComputerCraftIntegration {

    static void register() {
        final SimPeripheralService service = ServiceUtil.load(SimPeripheralService.class);

        add(service, AeroBlockEntityTypes.HOT_AIR_BURNER, be -> new GasProviderPeripheral<>(be, "hot_air_burner"));
        add(service, AeroBlockEntityTypes.STEAM_VENT, be -> new GasProviderPeripheral<>(be, "steam_vent"));
    }

    private static <T extends BlockEntity> void add(final SimPeripheralService service, final Supplier<BlockEntityType<T>> supplier, final Function<T, IPeripheral> peripheralFunction) {
        service.addPeripheral(supplier, peripheralFunction);
    }
}
