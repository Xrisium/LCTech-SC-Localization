package io.github.lightman314.lctech;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.Lists;

import io.github.lightman314.lctech.common.notifications.types.EnergyTradeNotification;
import io.github.lightman314.lctech.common.notifications.types.FluidTradeNotification;
import io.github.lightman314.lctech.common.universaldata.UniversalEnergyTraderData;
import io.github.lightman314.lctech.common.universaldata.UniversalFluidTraderData;
import io.github.lightman314.lctech.common.universaldata.traderSearching.FluidTraderSearchFilter;
import io.github.lightman314.lctech.core.ModBlockEntities;
import io.github.lightman314.lctech.core.ModBlocks;
import io.github.lightman314.lctech.core.ModItems;
import io.github.lightman314.lctech.core.ModRegistries;
import io.github.lightman314.lctech.network.LCTechPacketHandler;
import io.github.lightman314.lctech.proxy.*;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.notifications.Notification;
import io.github.lightman314.lightmanscurrency.common.universal_traders.TradingOffice;
import io.github.lightman314.lightmanscurrency.common.universal_traders.traderSearching.TraderSearchFilter;

@Mod("lctech")
public class LCTech
{
	
	public static final String MODID = "lctech";
	
	public static final CommonProxy PROXY = DistExecutor.safeRunForDist(() -> ClientProxy::new, () -> CommonProxy::new);
	
    // Directly reference a log4j logger.
    public static final Logger LOGGER = LogManager.getLogger();

    public LCTech() {
    	
        // Register the setup method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::doCommonStuff);
        // Register the doClientStuff method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::doClientStuff);

        //Register configs
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, TechConfig.serverSpec);
        
        //.Setup Deferred Registries
        ModRegistries.register(FMLJavaModLoadingContext.get().getModEventBus());
        
        //Pre-register items/blocks
        ModItems.init();
        ModBlocks.init();
        ModBlockEntities.init();
        
        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
        
    }

    private void doCommonStuff(final FMLCommonSetupEvent event)
    {
        LCTechPacketHandler.init();
        
        //Register Trader Search Filters
        TraderSearchFilter.addFilter(new FluidTraderSearchFilter());
        
        //Register the universal data deserializer
        TradingOffice.RegisterDataType(UniversalFluidTraderData.TYPE, UniversalFluidTraderData::new);
        TradingOffice.RegisterDataType(UniversalEnergyTraderData.TYPE, UniversalEnergyTraderData::new);
        
        //Register custom notification types
        Notification.register(FluidTradeNotification.TYPE, FluidTradeNotification::new);
        Notification.register(EnergyTradeNotification.TYPE, EnergyTradeNotification::new);
        
        //Add our items/blocks to the creative tab sorting
        try {
        	LightmansCurrency.MACHINE_GROUP.addToSortingList(Lists.newArrayList(
            		ModBlocks.IRON_TANK, ModBlocks.GOLD_TANK, ModBlocks.DIAMOND_TANK,
            		ModBlocks.FLUID_TRADER_INTERFACE,
            		ModItems.BATTERY, ModItems.BATTERY_LARGE,
            		ModBlocks.ENERGY_TRADER_INTERFACE
            		));
            
        	LightmansCurrency.UPGRADE_GROUP.addToSortingList(Lists.newArrayList(
        			ModItems.FLUID_CAPACITY_UPGRADE_1, ModItems.FLUID_CAPACITY_UPGRADE_2, ModItems.FLUID_CAPACITY_UPGRADE_3,
        			ModItems.ENERGY_CAPACITY_UPGRADE_1, ModItems.ENERGY_CAPACITY_UPGRADE_2, ModItems.ENERGY_CAPACITY_UPGRADE_3
        			));
        	
            LightmansCurrency.TRADING_GROUP.addToSortingList(Lists.newArrayList(
            		ModBlocks.FLUID_TAP, ModBlocks.FLUID_TAP_BUNDLE,
            		ModBlocks.FLUID_SERVER_SML, ModBlocks.FLUID_SERVER_MED, ModBlocks.FLUID_SERVER_LRG, ModBlocks.FLUID_SERVER_XLRG,
            		ModBlocks.BATTERY_SHOP, ModBlocks.ENERGY_SERVER
            		));
            
        } catch(Exception e) {  }
        
        
    }

    private void doClientStuff(final FMLClientSetupEvent event) {
        PROXY.setupClient();
    }

}
