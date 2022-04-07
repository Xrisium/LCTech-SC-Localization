package io.github.lightman314.lctech.client.gui.screen;

import com.google.common.base.Supplier;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import io.github.lightman314.lctech.LCTech;
import io.github.lightman314.lctech.client.gui.widget.button.FluidTradeButton;
import io.github.lightman314.lctech.trader.fluid.IFluidTrader;
import io.github.lightman314.lctech.trader.tradedata.FluidTradeData;
import io.github.lightman314.lightmanscurrency.client.gui.screen.ITradeRuleScreenHandler;
import io.github.lightman314.lightmanscurrency.client.gui.screen.TradeRuleScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.CoinValueInput;
import io.github.lightman314.lightmanscurrency.client.gui.widget.CoinValueInput.ICoinValueInput;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.IconButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.PlainButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.client.util.IconAndButtonUtil;
import io.github.lightman314.lightmanscurrency.money.CoinValue;
import io.github.lightman314.lightmanscurrency.trader.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.trader.tradedata.TradeData.TradeDirection;
import io.github.lightman314.lightmanscurrency.trader.tradedata.rules.ITradeRuleHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;

@Deprecated
public class TradeFluidPriceScreen extends Screen implements ICoinValueInput{

	private static final ResourceLocation GUI_TEXTURE = new ResourceLocation(LCTech.MODID, "textures/gui/fluidtradeprice.png");
	
	private int xSize = 176;
	private int ySize = 88 + CoinValueInput.HEIGHT;
	
	Supplier<IFluidTrader> trader;
	public IFluidTrader getTrader() { return this.trader.get(); }
	Supplier<FluidTradeData> trade;
	int tradeIndex;
	TradeDirection localDirection;
	int localQuantity;
	
	Button buttonSetSell;
	Button buttonSetPurchase;
	
	Button buttonAddBucket;
	Button buttonRemoveBucket;
	
	PlainButton buttonToggleDrainable;
	boolean localDrainable;
	PlainButton buttonToggleFillable;
	boolean localFillable;
	
	Button buttonTradeRules;
	
	CoinValueInput priceInput;
	
	public TradeFluidPriceScreen(Supplier<IFluidTrader> trader, int tradeIndex) {
		super(new TranslatableComponent("gui.lightmanscurrency.changeprice"));
		this.trader = trader;
		this.tradeIndex = tradeIndex;
		this.trade = () -> this.trader.get().getTrade(this.tradeIndex);
		//Store local copies of togglable data
		FluidTradeData localTrade = this.trade.get();
		this.localDirection = localTrade.getTradeDirection();
		//this.localDrainable = localTrade.canDrainExternally();
		//this.localFillable = localTrade.canFillExternally();
		this.localQuantity = localTrade.getBucketQuantity();
	}
	
	protected void init()
	{
		
		int guiLeft = (this.width - this.xSize) / 2;
		int guiTop = (this.height - this.ySize) / 2;
		
		this.priceInput = this.addRenderableWidget(new CoinValueInput(guiTop, this.title, this.trade.get().getCost(), this));
		this.priceInput.init();
		
		this.buttonSetSell = this.addRenderableWidget(new Button(guiLeft + 7, guiTop + CoinValueInput.HEIGHT + 6, 50, 20, new TranslatableComponent("gui.button.lightmanscurrency.tradedirection.sale"), this::SetTradeType));
		this.buttonSetPurchase = this.addRenderableWidget(new Button(guiLeft + 120, guiTop + CoinValueInput.HEIGHT + 6, 50, 21, new TranslatableComponent("gui.button.lightmanscurrency.tradedirection.purchase"), this::SetTradeType));
		
		this.buttonAddBucket = this.addRenderableWidget(new IconButton(guiLeft + 59, guiTop + CoinValueInput.HEIGHT + 6, this::PressQuantityButton, IconData.of(GUI_TEXTURE, this.xSize + 16, 0)));
		this.buttonRemoveBucket = this.addRenderableWidget(new IconButton(guiLeft + 98, guiTop + CoinValueInput.HEIGHT + 6, this::PressQuantityButton, IconData.of(GUI_TEXTURE, this.xSize + 32, 0)));
		
		this.addRenderableWidget(new Button(guiLeft + 7, guiTop + CoinValueInput.HEIGHT + 62, 50, 20, new TranslatableComponent("gui.button.lightmanscurrency.save"), this::PressSaveButton));
		this.addRenderableWidget(new Button(guiLeft + 120, guiTop + CoinValueInput.HEIGHT + 62, 50, 20, new TranslatableComponent("gui.button.lightmanscurrency.back"), this::PressBackButton));
		//this.addButton(new Button(guiLeft + 63, guiTop + CoinValueInput.HEIGHT + 62, 51, 20, new TranslationTextComponent("gui.button.lightmanscurrency.free"), this::PressFreeButton));
		this.buttonTradeRules = this.addRenderableWidget(IconAndButtonUtil.tradeRuleButton(guiLeft + this.xSize, guiTop + CoinValueInput.HEIGHT, this::PressTradeRuleButton));
		this.buttonTradeRules.visible = this.trader.get().getCoreSettings().hasPermission(this.minecraft.player, Permissions.EDIT_TRADE_RULES);
		
		this.buttonToggleDrainable = this.addRenderableWidget(new PlainButton(guiLeft + 7, guiTop + CoinValueInput.HEIGHT + 37, 10, 10, this::PressToggleDrainButton, GUI_TEXTURE, this.xSize, 16));
		this.buttonToggleDrainable.visible = this.trader.get().drainCapable();
		this.buttonToggleFillable = this.addRenderableWidget(new PlainButton(guiLeft + 95, guiTop + CoinValueInput.HEIGHT + 37, 10, 10, this::PressToggleFillButton, GUI_TEXTURE, this.xSize + 20, 16));
		this.buttonToggleFillable.visible = this.trader.get().drainCapable();
		
		tick();
		
	}

	public void tick()
	{
		
		if(this.getTrader() == null)
		{
			this.minecraft.setScreen(null);
			return;
		}
		
		this.buttonSetSell.active = this.localDirection != TradeDirection.SALE;
		this.buttonSetPurchase.active = this.localDirection != TradeDirection.PURCHASE;

		this.buttonToggleDrainable.setResource(GUI_TEXTURE, this.xSize + (this.localDrainable ? 0 : 10), 16);
		this.buttonToggleFillable.setResource(GUI_TEXTURE, this.xSize + (this.localFillable ? 20 : 30), 16);
		
		this.buttonAddBucket.active = this.localQuantity < this.trade.get().getMaxBucketQuantity();
		this.buttonRemoveBucket.active = this.localQuantity > 1;
		
		this.buttonTradeRules.visible = this.trader.get().getCoreSettings().hasPermission(this.minecraft.player, Permissions.EDIT_TRADE_RULES);
		
		super.tick();
		this.priceInput.tick();
		
	}
	
	@Override
	public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks)
	{
		
		if(this.getTrader() == null)
		{
			this.minecraft.setScreen(null);
			return;
		}
		
		this.renderBackground(matrixStack);
		
		RenderSystem.setShaderTexture(0, GUI_TEXTURE);
		RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
		int startX = (this.width - this.xSize) / 2;
		int startY = (this.height - this.ySize) / 2;
		this.blit(matrixStack, startX, startY + CoinValueInput.HEIGHT, 0, 0, this.xSize, this.ySize - CoinValueInput.HEIGHT);
		
		super.render(matrixStack, mouseX, mouseY, partialTicks);
		
		//Render the Drainable & Fillable text
		if(this.trader.get().drainCapable())
		{
			this.font.draw(matrixStack, new TranslatableComponent("tooltip.lctech.trader.fluid_settings.drainable").setStyle(Style.EMPTY.withColor(this.localDrainable ? FluidTradeButton.ENABLED_COLOR : FluidTradeButton.DISABLED_COLOR)), startX + 18, startY + CoinValueInput.HEIGHT + 38, 0xFFFFFF);
			this.font.draw(matrixStack, new TranslatableComponent("tooltip.lctech.trader.fluid_settings.fillable").setStyle(Style.EMPTY.withColor(this.localFillable ? FluidTradeButton.ENABLED_COLOR : FluidTradeButton.DISABLED_COLOR)), startX + 106, startY + CoinValueInput.HEIGHT + 38, 0xFFFFFF);
		}
		
		//Render the local quantity text
		String quantityText = this.localQuantity + "B";
		int textWidth = this.font.width(quantityText);
		this.font.draw(matrixStack, quantityText, startX + 1 + (176 / 2) - (textWidth / 2), startY + CoinValueInput.HEIGHT + 12, 0xFFFFFF);
		
		//Mouse over for buttons
		IconAndButtonUtil.renderButtonTooltips(matrixStack, mouseX, mouseY, this.renderables);
		
		if(this.buttonToggleDrainable.isMouseOver(mouseX, mouseY))
			this.renderTooltip(matrixStack, new TranslatableComponent("tooltip.lctech.trader.fluid_settings.drainable.wordy"), mouseX, mouseY);
		else if(this.buttonToggleFillable.isMouseOver(mouseX, mouseY))
			this.renderTooltip(matrixStack, new TranslatableComponent("tooltip.lctech.trader.fluid_settings.fillable.wordy"), mouseX, mouseY);
		else if(this.buttonAddBucket.isMouseOver(mouseX, mouseY))
			this.renderTooltip(matrixStack, new TranslatableComponent("tooltip.lctech.trader.fluid.add_bucket"), mouseX, mouseY);
		else if(this.buttonRemoveBucket.isMouseOver(mouseX, mouseY))
			this.renderTooltip(matrixStack, new TranslatableComponent("tooltip.lctech.trader.fluid.remove_bucket"), mouseX, mouseY);
	}
	
	private void SetTradeType(Button button)
	{
		if(button == buttonSetSell)
			this.localDirection = TradeDirection.SALE;
		else
			this.localDirection = TradeDirection.PURCHASE;
	}
	
	private void PressSaveButton(Button button)
	{
		SaveChanges();
		PressBackButton(button);
	}
	
	protected void SaveChanges()
	{
		//this.getTrader().sendPriceMessage(new TradePriceData(this.tradeIndex, this.priceInput.getCoinValue(), this.localDirection, this.localQuantity, this.localDrainable, this.localFillable));
	}
	
	private void PressBackButton(Button button)
	{
		this.getTrader().sendOpenStorageMessage();
	}
	
	private void PressToggleDrainButton(Button button)
	{
		this.localDrainable = !this.localDrainable;
	}
	
	private void PressToggleFillButton(Button button)
	{
		this.localFillable = !this.localFillable;
	}
	
	private void PressQuantityButton(Button button)
	{
		int deltaQuantity = button == this.buttonAddBucket ? 1 : -1;
		if(deltaQuantity < 0 && this.localQuantity <= 1)
			return;
		else if(deltaQuantity > 1 && this.localQuantity >= this.trade.get().getMaxBucketQuantity())
			return;
		this.localQuantity += deltaQuantity;
	}
	
	@Override
	public void OnCoinValueChanged(CoinValueInput input) { }

	@Override
	public <T extends GuiEventListener & Widget & NarratableEntry> T addCustomWidget(T button) {
		return super.addRenderableWidget(button);
	}

	@Override
	public Font getFont() {
		return this.font;
	}

	@Override
	public int getWidth() {
		return this.width;
	}
	
	private void PressTradeRuleButton(Button button)
	{
		SaveChanges();
		this.minecraft.setScreen(new TradeRuleScreen(this.getRuleScreenHandler()));
	}
	
	public ITradeRuleScreenHandler getRuleScreenHandler() { return new CloseRuleHandler(this.trader, this.tradeIndex); }
	
	private static class CloseRuleHandler implements ITradeRuleScreenHandler
	{
		final Supplier<IFluidTrader> trader;
		final int tradeIndex;
		
		public CloseRuleHandler(Supplier<IFluidTrader> trader, int tradeIndex)
		{
			this.trader = trader;
			this.tradeIndex = tradeIndex;
		}
		
		public ITradeRuleHandler ruleHandler() { return this.trader.get().getTrade(this.tradeIndex); }
		
		public void reopenLastScreen() {
			Minecraft.getInstance().setScreen(new TradeFluidPriceScreen(this.trader, this.tradeIndex));
		}
		
		public void updateServer(ResourceLocation type, CompoundTag updateInfo)
		{
			this.trader.get().sendUpdateTradeRuleMessage(this.tradeIndex, type, updateInfo);
		}
		
	}
	
	public static class TradePriceData
	{
		
		public final int tradeIndex;
		public final CoinValue cost;
		public final TradeDirection type;
		public final int quantity;
		public final boolean canDrain;
		public final boolean canFill;
		
		public TradePriceData(int tradeIndex, CoinValue cost, TradeDirection type, int quantity, boolean canDrain, boolean canFill)
		{
			this.tradeIndex = tradeIndex;
			this.cost = cost;
			this.type = type;
			this.quantity = quantity;
			this.canDrain = canDrain;
			this.canFill = canFill;
		}
	}

	
}
