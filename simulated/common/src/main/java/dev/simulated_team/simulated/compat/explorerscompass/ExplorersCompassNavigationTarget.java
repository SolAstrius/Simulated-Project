package dev.simulated_team.simulated.compat.explorerscompass;

import com.chaosthedude.explorerscompass.ExplorersCompass;
import com.chaosthedude.explorerscompass.util.CompassState;
import dev.simulated_team.simulated.content.blocks.nav_table.NavTableBlockEntity;
import dev.simulated_team.simulated.content.blocks.nav_table.navigation_target.NavigationTarget;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public class ExplorersCompassNavigationTarget implements NavigationTarget {
	@Override
	public @Nullable Vec3 getTarget(final NavTableBlockEntity navBE, final ItemStack self) {
		final Integer x = self.getComponents().get(ExplorersCompass.FOUND_X_COMPONENT);
		final Integer z = self.getComponents().get(ExplorersCompass.FOUND_Z_COMPONENT);
		if (x != null && z != null) {
			final Vec3 pos = navBE.getProjectedSelfPos();
			return new Vec3(x, pos.y(), z);
		}

		return null;
	}

	@Override
	public float getMaxRange() {
		return 0;
	}

	@Override
	public Map<String, Object> getPeripheralMetadata(final NavTableBlockEntity be, final ItemStack self) {
		final Map<String, Object> out = new LinkedHashMap<>();

		final String structure = self.getComponents().get(ExplorersCompass.STRUCTURE_ID_COMPONENT);
		if (structure != null) {
			out.put("structure", structure);
		}

		final Integer stateId = self.getComponents().get(ExplorersCompass.COMPASS_STATE_COMPONENT);
		if (stateId != null) {
			final CompassState state = CompassState.fromID(stateId);
			if (state != null) {
				out.put("state", state.name().toLowerCase(Locale.ROOT));
			}
		}

		final Integer radius = self.getComponents().get(ExplorersCompass.SEARCH_RADIUS_COMPONENT);
		if (radius != null) {
			out.put("search_radius", radius);
		}

		final Boolean isGroup = self.getComponents().get(ExplorersCompass.IS_GROUP_COMPONENT);
		if (isGroup != null) {
			out.put("is_group", isGroup);
		}

		return out;
	}
}
