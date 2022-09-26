package io.github.lightman314.lctech.blocks.traderblocks;

import java.util.List;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;

import io.github.lightman314.lctech.blockentities.trader.EnergyTraderBlockEntity;
import io.github.lightman314.lctech.core.ModBlockEntities;
import io.github.lightman314.lctech.items.tooltips.TechTooltips;
import io.github.lightman314.lightmanscurrency.blocks.traderblocks.templates.TraderBlockRotatable;
import io.github.lightman314.lightmanscurrency.items.TooltipItem;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class NetworkEnergyTraderBlock extends TraderBlockRotatable {
	
	public NetworkEnergyTraderBlock(Properties properties) { super(properties); }
	
	@Override
	protected BlockEntity makeTrader(BlockPos pos, BlockState state) { return new EnergyTraderBlockEntity(pos, state, true); }
	
	@Override
	protected BlockEntityType<?> traderType() { return ModBlockEntities.ENERGY_TRADER.get(); }
	
	@Override @SuppressWarnings("deprecation")
	protected List<BlockEntityType<?>> validTraderTypes() { return ImmutableList.of(ModBlockEntities.ENERGY_TRADER.get(), ModBlockEntities.UNIVERSAL_ENERGY_TRADER.get()); }

	@Override
	public void appendHoverText(ItemStack stack, @Nullable BlockGetter level, List<Component> tooltip, TooltipFlag flagIn)
	{
		TooltipItem.addTooltip(tooltip, TechTooltips.ENERGY_NETWORK_TRADER);
		super.appendHoverText(stack, level, tooltip, flagIn);
	}

}