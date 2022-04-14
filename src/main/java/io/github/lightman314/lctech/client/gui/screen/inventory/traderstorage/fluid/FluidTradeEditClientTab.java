package io.github.lightman314.lctech.client.gui.screen.inventory.traderstorage.fluid;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import io.github.lightman314.lctech.client.gui.widget.FluidEditWidget;
import io.github.lightman314.lctech.client.gui.widget.FluidEditWidget.IFluidEditListener;
import io.github.lightman314.lctech.menu.traderstorage.fluid.FluidTradeEditTab;
import io.github.lightman314.lctech.trader.tradedata.FluidTradeData;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.TraderScreen;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.TraderStorageScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.CoinValueInput;
import io.github.lightman314.lightmanscurrency.client.gui.widget.CoinValueInput.ICoinValueInput;
import io.github.lightman314.lightmanscurrency.client.gui.widget.ScrollBarWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.TradeButtonArea.InteractionConsumer;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.IconButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade.TradeButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade.TradeButton.ITradeData;
import io.github.lightman314.lightmanscurrency.core.ModItems;
import io.github.lightman314.lightmanscurrency.menus.traderstorage.TraderStorageClientTab;
import io.github.lightman314.lightmanscurrency.money.CoinValue;
import io.github.lightman314.lightmanscurrency.trader.ITrader;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.fluids.FluidStack;

public class FluidTradeEditClientTab extends TraderStorageClientTab<FluidTradeEditTab> implements InteractionConsumer, ICoinValueInput, IFluidEditListener{

	private static final int X_OFFSET = 13;
	private static final int Y_OFFSET = 71;
	private static final int COLUMNS = 10;
	private static final int ROWS = 2;
	
	public FluidTradeEditClientTab(TraderStorageScreen screen, FluidTradeEditTab commonTab) { super(screen, commonTab); }
	
	@Override
	public IconData getIcon() { return IconData.of(ModItems.TRADING_CORE); }
	
	@Override
	public Component getTooltip() { return new TextComponent(""); }
	
	@Override
	public boolean tabButtonVisible() { return false; }
	
	@Override
	public boolean blockInventoryClosing() { return true; }
	
	@Override
	public int getTradeRuleTradeIndex() { return this.commonTab.getTradeIndex(); }
	
	TradeButton tradeDisplay;
	CoinValueInput priceSelection;
	
	Button buttonAddBucket;
	Button buttonRemoveBucket;
	
	FluidEditWidget fluidEdit;
	ScrollBarWidget fluidEditScroll;
	
	Button buttonToggleTradeType;
	
	private int selection;
	
	@Override
	public void onOpen() {
		
		FluidTradeData trade = this.getTrade();
		
		this.tradeDisplay = this.screen.addRenderableTabWidget(new TradeButton(this.menu::getContext, this.commonTab::getTrade, button -> {}));
		this.tradeDisplay.move(this.screen.getGuiLeft() + 10, this.screen.getGuiTop() + 18);
		this.priceSelection = this.screen.addRenderableTabWidget(new CoinValueInput(this.screen.getGuiTop() + 40, new TextComponent(""), trade == null ? CoinValue.EMPTY : trade.getCost(), this));
		this.priceSelection.drawBG = false;
		this.priceSelection.init();
		
		this.fluidEdit = this.screen.addRenderableTabWidget(new FluidEditWidget(this.screen.getGuiLeft() + X_OFFSET, this.screen.getGuiTop() + Y_OFFSET, COLUMNS, ROWS, this));
		this.fluidEdit.init(this.screen::addRenderableTabWidget, this.screen::addTabListener);
		
		this.fluidEditScroll = this.screen.addRenderableTabWidget(new ScrollBarWidget(this.screen.getGuiLeft() + X_OFFSET + 18 * COLUMNS, this.screen.getGuiTop() + Y_OFFSET, 18 * ROWS, this.fluidEdit
				));
		this.fluidEditScroll.smallKnob = true;
		
		//this.customNameInput = this.screen.addRenderableTabWidget(new EditBox(this.font, this.screen.getGuiLeft() + 13, this.screen.getGuiTop() + 38, this.screen.getXSize() - 26, 18, new TextComponent("")));
		//if(this.selection >= 0 && this.selection < 2 && trade != null)
		//	this.customNameInput.setValue(trade.getCustomName(this.selection));
		this.buttonAddBucket = this.screen.addRenderableTabWidget(new IconButton(this.screen.getGuiLeft() + 74, this.screen.getGuiTop() + 38, this::ChangeQuantity, IconData.of(FluidStorageClientTab.GUI_TEXTURE, 32, 0)));
		this.buttonRemoveBucket = this.screen.addRenderableTabWidget(new IconButton(this.screen.getGuiLeft() + 113, this.screen.getGuiTop() + 38, this::ChangeQuantity, IconData.of(FluidStorageClientTab.GUI_TEXTURE, 48, 0)));
		
		this.buttonToggleTradeType = this.screen.addRenderableTabWidget(new Button(this.screen.getGuiLeft() + 113, this.screen.getGuiTop() + 15, 80, 20, new TextComponent(""), this::ToggleTradeType));
		
	}
	
	@Override
	public void onClose() { this.selection = -1; }
	
	@Override
	public void renderBG(PoseStack pose, int mouseX, int mouseY, float partialTicks) {
		
		if(this.getTrade() == null)
			return;
		
		this.validateRenderables();
		
		//Render the local quantity text
		if(this.selection >= 0)
		{
			String quantityText = this.getTrade().getBucketQuantity() + "B";
			int textWidth = this.font.width(quantityText);
			this.font.draw(pose, quantityText, this.screen.getGuiLeft() + 1 + (this.screen.getXSize() / 2) - (textWidth / 2), this.screen.getGuiTop() + 42, 0xFFFFFF);
		}
		
		//Render a down arrow over the selected position
		RenderSystem.setShaderTexture(0, TraderScreen.GUI_TEXTURE);
		RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
		
		this.screen.blit(pose, this.getArrowPosition(), this.screen.getGuiTop() + 10, TraderScreen.WIDTH + 8, 18, 8, 6);
		
	}
	
	private int getArrowPosition() {
		
		FluidTradeData trade = this.getTrade();
		if(this.selection == -1)
		{
			if(trade.isSale())
				return this.screen.getGuiLeft() + 25;
			else
				return this.screen.getGuiLeft() + 63;
		}
		else
		{
			if(trade.isSale())
				return this.screen.getGuiLeft() + 72;
			else
				return this.screen.getGuiLeft() + 16;
		}
	}
	
	private void validateRenderables() {
		
		this.priceSelection.visible = this.selection < 0;
		if(this.priceSelection.visible)
			this.priceSelection.tick();
		this.fluidEdit.visible = this.selection >= 0;
		
		this.buttonAddBucket.visible = this.buttonRemoveBucket.visible = this.selection >= 0;
		if(this.buttonAddBucket.visible)
			this.buttonAddBucket.active = this.getTrade().getBucketQuantity() < this.getTrade().getMaxBucketQuantity();
		if(this.buttonRemoveBucket.visible)
			this.buttonRemoveBucket.active = this.getTrade().getBucketQuantity() > 1;
		
		this.buttonToggleTradeType.setMessage(new TranslatableComponent("gui.button.lightmanscurrency.tradedirection." + this.getTrade().getTradeDirection().name().toLowerCase()));
		
	}
	
	@Override
	public void renderTooltips(PoseStack pose, int mouseX, int mouseY) {
		
		this.tradeDisplay.renderTooltips(pose, mouseX, mouseY);
		
		if(this.selection >= 0)
			this.fluidEdit.renderTooltips(this.screen, pose, mouseX, mouseY);
		
	}
	
	@Override
	public void receiveSelfMessage(CompoundTag message) {
		if(message.contains("TradeIndex"))
			this.commonTab.setTradeIndex(message.getInt("TradeIndex"));
		if(message.contains("StartingSlot"))
			this.selection = message.getInt("StartingSlot");
	}
	
	@Override
	public void onTradeButtonInputInteraction(ITrader trader, ITradeData trade, int index, int mouseButton) {
		if(trade instanceof FluidTradeData)
		{
			FluidTradeData t = (FluidTradeData)trade;
			if(t.isSale())
				this.changeSelection(-1);
			else if(t.isPurchase())
				this.changeSelection(index);
		}
	}
	
	@Override
	public void onTradeButtonOutputInteraction(ITrader trader, ITradeData trade, int index, int mouseButton) {
		if(trade instanceof FluidTradeData)
		{
			FluidTradeData t = (FluidTradeData)trade;
			if(t.isSale())
				this.changeSelection(index);
			else if(t.isPurchase())
				this.changeSelection(-1);
		}
	}
	
	private void changeSelection(int newSelection) {
		this.selection = newSelection;
		if(this.selection == -1)
			this.priceSelection.setCoinValue(this.getTrade().getCost());
		//if(this.selection >= 0 && this.selection < 2)
		//	this.customNameInput.setValue(this.commonTab.getTrade().getCustomName(this.selection));
		if(this.selection >= 0)
			this.fluidEdit.refreshSearch();
	}
	
	@Override
	public void onTradeButtonInteraction(ITrader trader, ITradeData trade, int localMouseX, int localMouseY, int mouseButton) { }
	
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		this.tradeDisplay.onInteractionClick((int)mouseX, (int)mouseY, button, this);
		return false;
	}
	
	@Override
	public <T extends GuiEventListener & Widget & NarratableEntry> T addCustomWidget(T button) {
		if(button instanceof AbstractWidget)
			this.screen.addRenderableTabWidget((AbstractWidget)button);
		return button;
	}
	
	@Override
	public int getWidth() { return this.screen.width; }

	@Override
	public Font getFont() { return this.font; }
	
	@Override
	public void OnCoinValueChanged(CoinValueInput input) { this.commonTab.setPrice(input.getCoinValue()); }
	
	public FluidTradeData getTrade() { return this.commonTab.getTrade(); }
	
	public void onFluidClicked(FluidStack fluid) {
		this.commonTab.setFluid(fluid);
	}
	
	private void ChangeQuantity(Button button) {
		if(this.getTrade() != null)
		{
			int deltaQuantity = 1;
			if(button == this.buttonRemoveBucket)
				deltaQuantity = -1;
			this.commonTab.setQuantity(this.getTrade().getBucketQuantity() + deltaQuantity);
		}
	}
	
	private void ToggleTradeType(Button button) {
		if(this.getTrade() != null)
			this.commonTab.setType(this.getTrade().getTradeDirection().next());
	}
	
}