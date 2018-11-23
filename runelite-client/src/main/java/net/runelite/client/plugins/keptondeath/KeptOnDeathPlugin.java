/*
 * Copyright (c) 2018, TheStonedTurtle <https://github.com/TheStonedTurtle>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.runelite.client.plugins.keptondeath;

import com.google.common.eventbus.Subscribe;
import javax.inject.Inject;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.Varbits;
import net.runelite.api.events.GameTick;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;


@PluginDescriptor(
	name = "Kept on Death",
	description = "Displays extra information inside the Items Kept on Death screen"
)
@Slf4j
public class KeptOnDeathPlugin extends Plugin
{
	private static final String GREEN_OUTLINE_INFO = "<br><br>Items with a <col=00ff00>green outline<col=ff981f> are kept on death in safe zones.";

	@Inject
	private Client client;

	@Inject
	private KeptOnDeathOverlay overlay;

	@Inject
	private ItemManager itemManager;

	@Inject
	private OverlayManager overlayManager;

	private boolean justOpenedWidget = false;
	@Getter
	private boolean widgetVisible = false;
	@Getter
	private int wildyLevel = -1;

	@Override
	protected void startUp()
	{
		overlayManager.add(overlay);
	}

	@Override
	protected void shutDown()
	{
		overlayManager.remove(overlay);
	}

	@Subscribe
	protected void onGameTick(GameTick t)
	{
		boolean old = widgetVisible;
		widgetVisible = client.getWidget(WidgetInfo.ITEMS_LOST_ON_DEATH_CONTAINER) != null;
		if (widgetVisible && !old)
		{
			justOpenedWidget = true;
			wildyLevel = getCurrentWildyLevel();
		}
	}

	void ensureInfoText()
	{
		if (!justOpenedWidget)
		{
			return;
		}
		justOpenedWidget = false;

		// All non tradeable items are lost above 20 wilderness unless one of the 1,3,4 protected items.
		if (wildyLevel >= 20)
		{
			return;
		}

		Widget info = client.getWidget(WidgetInfo.ITEMS_KEPT_INFORMATION_CONTAINER);
		info.setText(info.getText() + GREEN_OUTLINE_INFO);
	}

	private int getCurrentWildyLevel()
	{
		if (client.getVar(Varbits.IN_WILDERNESS) != 1)
		{
			return -1;
		}

		int y = client.getLocalPlayer().getWorldLocation().getY();

		int underLevel = ((y - 9920) / 8) + 1;
		int upperLevel = ((y - 3520) / 8) + 1;
		return (y > 6400 ? underLevel : upperLevel);
	}
}
