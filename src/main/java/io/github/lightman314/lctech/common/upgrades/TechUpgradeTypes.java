package io.github.lightman314.lctech.common.upgrades;

import io.github.lightman314.lctech.LCTech;
import io.github.lightman314.lctech.common.upgrades.types.capacity.*;
import io.github.lightman314.lightmanscurrency.common.upgrades.UpgradeType;
import io.github.lightman314.lightmanscurrency.common.upgrades.types.capacity.CapacityUpgrade;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class TechUpgradeTypes {

	public static final CapacityUpgrade FLUID_CAPACITY = new FluidCapacityUpgrade();
	public static final CapacityUpgrade ENERGY_CAPACITY = new EnergyCapacityUpgrade();
	
	@SubscribeEvent
	public static void registerUpgradeTypes(UpgradeType.RegisterUpgradeTypeEvent event) {
		event.Register(new ResourceLocation(LCTech.MODID, "fluid_capacity"), FLUID_CAPACITY);
		event.Register(new ResourceLocation(LCTech.MODID, "energy_capacity"), ENERGY_CAPACITY);
	}
	
	
}
