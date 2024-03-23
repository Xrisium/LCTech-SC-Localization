package io.github.lightman314.lctech.common.menu.traderinterface.energy;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import io.github.lightman314.lctech.common.blockentities.EnergyTraderInterfaceBlockEntity;
import io.github.lightman314.lctech.client.gui.screen.inventory.traderinterface.energy.EnergyStorageClientTab;
import io.github.lightman314.lctech.common.menu.slots.BatteryInputSlot;
import io.github.lightman314.lctech.common.menu.util.MenuUtil;
import io.github.lightman314.lctech.common.util.EnergyUtil;
import io.github.lightman314.lctech.common.util.EnergyUtil.EnergyActionResult;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.trader_interface.menu.TraderInterfaceClientTab;
import io.github.lightman314.lightmanscurrency.api.trader_interface.menu.TraderInterfaceTab;
import io.github.lightman314.lightmanscurrency.api.upgrades.slot.UpgradeInputSlot;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.TraderInterfaceScreen;
import io.github.lightman314.lightmanscurrency.common.menus.TraderInterfaceMenu;
import io.github.lightman314.lightmanscurrency.common.menus.TraderMenu;
import io.github.lightman314.lightmanscurrency.common.menus.slots.SimpleSlot;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class EnergyStorageTab extends TraderInterfaceTab {

	public EnergyStorageTab(TraderInterfaceMenu menu) { super(menu); }
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public TraderInterfaceClientTab<?> createClientTab(TraderInterfaceScreen screen) { return new EnergyStorageClientTab(screen, this); }
	
	List<SimpleSlot> slots = new ArrayList<>();
	public List<? extends Slot> getSlots() { return this.slots; }
	
	BatteryInputSlot drainSlot;
	BatteryInputSlot fillSlot;
	Container batterySlots = new SimpleContainer(2);
	
	@Override
	public boolean canOpen(Player player) { return true; }
	
	@Override
	public void onTabOpen() {
		SimpleSlot.SetActive(this.slots);
		MinecraftForge.EVENT_BUS.register(this);
		this.drainSlot.locked = false;
		this.fillSlot.locked = false;
	}
	
	@Override
	public void onTabClose() {
		SimpleSlot.SetInactive(this.slots);
		MinecraftForge.EVENT_BUS.unregister(this);
		this.drainSlot.locked = true;
		this.fillSlot.locked = true;
	}
	
	@Override
	public void addStorageMenuSlots(Function<Slot,Slot> addSlot) {
		for(int i = 0; i < this.menu.getBE().getUpgradeInventory().getContainerSize(); ++i)
		{
			SimpleSlot upgradeSlot = new UpgradeInputSlot(this.menu.getBE().getUpgradeInventory(), i, 176, 18 + 18 * i, this.menu.getBE());
			upgradeSlot.active = false;
			upgradeSlot.setListener(this::onUpgradeModified);
			addSlot.apply(upgradeSlot);
			this.slots.add(upgradeSlot);
		}
		
		//Battery Drain Slot
		this.drainSlot = new BatteryInputSlot(this.batterySlots, 0, TraderMenu.SLOT_OFFSET + 8, 122);
		this.drainSlot.requireEnergy = true;
		this.slots.add(this.drainSlot);
		addSlot.apply(this.drainSlot);
		this.drainSlot.locked = true;
		//Battery Fill Slot
		this.fillSlot = new BatteryInputSlot(this.batterySlots, 1, TraderMenu.SLOT_OFFSET + 44, 122);
		this.slots.add(this.fillSlot);
		addSlot.apply(this.fillSlot);
		this.fillSlot.locked = true;
		
		SimpleSlot.SetInactive(this.slots);
		
	}
	
	private void onUpgradeModified() {
		this.menu.getBE().setUpgradeSlotsDirty();
	}
	
	@Override
	public void onMenuClose() {
		MenuUtil.clearContainer(this.menu.player, this.batterySlots);
	}
	
	@SubscribeEvent
	public void onWorldTick(TickEvent.LevelTickEvent event)
	{
		if(event.side.isServer() && event.phase == TickEvent.Phase.START && this.menu.getBE() instanceof EnergyTraderInterfaceBlockEntity be)
		{
			//Drain from slot 1
			if(!this.batterySlots.getItem(0).isEmpty())
			{
				ItemStack batteryStack = this.batterySlots.getItem(0);
				if(batteryStack.getCount() == 1)
				{
					EnergyUtil.getEnergyHandler(batteryStack).ifPresent(energyStorage -> {
						//Get extractable amount
						int extractedAmount = energyStorage.extractEnergy(be.getMaxEnergy() - be.getStoredEnergy(), false);
						if(extractedAmount > 0)
							be.addStoredEnergy(extractedAmount);
					});
				}
			}
			//Store into slot 2
			if(!this.batterySlots.getItem(1).isEmpty())
			{
				ItemStack batteryStack = this.batterySlots.getItem(1);
				if(batteryStack.getCount() == 1)
				{
					//Manually drain, cause apparently EnergyUtil#tryFillContainer doesn't work anymore?
					EnergyUtil.getEnergyHandler(batteryStack).ifPresent(energyStorage -> {
						int drainedAmount = energyStorage.receiveEnergy(be.getStoredEnergy(), false);
						if(drainedAmount > 0)
							be.drainStoredEnergy(drainedAmount);
					});
				}
			}
		}
	}
	
	public void toggleInputSlot(Direction side) {
		if(this.menu.getBE().isOwner(this.menu.player) && this.menu.getBE() instanceof EnergyTraderInterfaceBlockEntity be) {
			be.getEnergyHandler().toggleInputSide(side);
			be.setHandlerDirty(be.getEnergyHandler());
		}
	}
	
	public void toggleOutputSlot(Direction side) {
		if(this.menu.getBE().isOwner(this.menu.player) && this.menu.getBE() instanceof EnergyTraderInterfaceBlockEntity be) {
			be.getEnergyHandler().toggleOutputSide(side);
			be.setHandlerDirty(be.getEnergyHandler());
		}
	}
	
	//No messages to receive. All storage interactions are done via the battery slots or the upgrade slots.
	@Override
	public void receiveMessage(CompoundTag message) { }
	
}
