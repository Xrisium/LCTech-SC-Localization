package io.github.lightman314.lctech.core;

import java.util.List;

import com.google.common.collect.Lists;

import io.github.lightman314.lctech.LCTech;
import io.github.lightman314.lctech.items.*;
import io.github.lightman314.lctech.trader.upgrades.UpgradeType;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.minecraft.world.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = LCTech.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModItems {
	
	private static final List<Item> ITEMS = Lists.newArrayList();
	
	//Fluid Shard
	public static final Item FLUID_SHARD = register("fluid_shard", new FluidShardItem(new Item.Properties().stacksTo(1)));
	
	//Fluid Upgrades
	public static final Item FLUID_CAPACITY_UPGRADE_1 = register("fluid_capacity_upgrade_1", new CapacityUpgradeItem(UpgradeType.FLUID_CAPACITY, FluidAttributes.BUCKET_VOLUME * 10, new Item.Properties().tab(LightmansCurrency.MACHINE_GROUP)));
	public static final Item FLUID_CAPACITY_UPGRADE_2 = register("fluid_capacity_upgrade_2", new CapacityUpgradeItem(UpgradeType.FLUID_CAPACITY, FluidAttributes.BUCKET_VOLUME * 25, new Item.Properties().tab(LightmansCurrency.MACHINE_GROUP)));
	
	private static Item register(String name, Item item)
	{
		item.setRegistryName(name);
		ITEMS.add(item);
		return item;
	}
	
	@SubscribeEvent
	public static void registerItems(final RegistryEvent.Register<Item> event)
	{
		ITEMS.forEach(item -> event.getRegistry().register(item));
		ITEMS.clear();
	}
	
}
