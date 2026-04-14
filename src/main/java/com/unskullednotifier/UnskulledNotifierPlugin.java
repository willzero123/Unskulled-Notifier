package com.unskullednotifier;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Provides;
import java.util.Set;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.api.SkullIcon;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

@PluginDescriptor(
	name = "Unskulled Notifier",
	description = "Displays a reminder when you are unskulled, with option for revenant caves only",
	tags = { "skull", "skulled", "unskulled", "notifier", "reminder", "notify", "revs", "revenant", "revenants" }
)
public class UnskulledNotifierPlugin extends Plugin
{
	private static final Set<Integer> REVENANT_CAVE_REGION_IDS = ImmutableSet.of(
		12445, 12446, 12447, 12448,
		12701, 12702, 12703, 12704,
		12957, 12958, 12959, 12960,
		13213, 13214, 13215, 13216
	);

	@Inject
	private Client client;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private UnskulledNotifierOverlay overlay;

	@Inject
	private UnskulledNotifierConfig config;

	@Override
	protected void startUp()
	{
		overlay.updateConfig();
		overlayManager.add(overlay);
	}

	@Override
	protected void shutDown()
	{
		overlayManager.remove(overlay);
	}

	boolean shouldShowOverlay()
	{
		if (!isPlayerUnskulled())
		{
			return false;
		}

		if (!isInterfaceVisible())
		{
			return false;
		}

		return !config.revenantCavesOnly() || isInRevenantCaves();
	}

	private boolean isInterfaceVisible()
	{
		Widget welcomeScreen = client.getWidget(InterfaceID.WELCOME_SCREEN, 0);
		if (welcomeScreen != null && !welcomeScreen.isHidden())
		{
			return false;
		}

		Widget fixedViewport = client.getWidget(InterfaceID.Toplevel.OVERLAY_HUD);
		Widget resizableClassic = client.getWidget(InterfaceID.ToplevelOsrsStretch.HUD_CONTAINER_FRONT);
		Widget resizableModern = client.getWidget(InterfaceID.ToplevelPreEoc.HUD_CONTAINER_FRONT);

		return (fixedViewport != null && !fixedViewport.isHidden())
			|| (resizableClassic != null && !resizableClassic.isHidden())
			|| (resizableModern != null && !resizableModern.isHidden());
	}

	private boolean isPlayerUnskulled()
	{
		Player player = client.getLocalPlayer();
		return player != null && player.getSkullIcon() == SkullIcon.NONE;
	}

	private boolean isInRevenantCaves()
	{
		Player player = client.getLocalPlayer();
		if (player == null)
		{
			return false;
		}

		WorldPoint location = player.getWorldLocation();
		return location != null && REVENANT_CAVE_REGION_IDS.contains(location.getRegionID());
	}

	@Provides
	UnskulledNotifierConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(UnskulledNotifierConfig.class);
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (!event.getGroup().equals("unskullednotifier"))
		{
			return;
		}

		if (event.getKey().equals("scale"))
		{
			overlay.updateConfig();
		}
	}
}