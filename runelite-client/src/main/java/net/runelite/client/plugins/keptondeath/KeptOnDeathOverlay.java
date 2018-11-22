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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;

public class KeptOnDeathOverlay extends Overlay
{
	private final Client client;
	private final ItemManager itemManager;
	private final KeptOnDeathPlugin plugin;

	@Inject
	private KeptOnDeathOverlay(Client client, ItemManager itemManager, KeptOnDeathPlugin plugin)
	{
		setPosition(OverlayPosition.DYNAMIC);
		setPriority(OverlayPriority.LOW);
		setLayer(OverlayLayer.ABOVE_WIDGETS);
		this.client = client;
		this.itemManager = itemManager;
		this.plugin = plugin;
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (!plugin.isWidgetVisible())
		{
			return null;
		}

		Widget lost = client.getWidget(WidgetInfo.ITEMS_LOST_ON_DEATH_CONTAINER);
		if (lost != null)
		{
			Widget[] children = lost.getDynamicChildren();
			for (int i = (children.length - 1); i >= 0; i--)
			{
				Widget t = children[i];
				if (!itemManager.getItemComposition(t.getItemId()).isTradeable())
				{
					// Certain items have a white border which needs a thicker border to be hidden.
					if (AlwaysLostItem.check(t.getItemId()))
					{
						BufferedImage adjusted = itemManager.getItemOutline(t.getItemId(), t.getItemQuantity(), Color.GREEN, 2);
						// Do not adjust canvas positions for these items as well
						graphics.drawImage(adjusted, t.getCanvasLocation().getX(), t.getCanvasLocation().getY(), null);
						continue;
					}
					BufferedImage adjusted = itemManager.getItemOutline(t.getItemId(), t.getItemQuantity(), Color.GREEN);
					graphics.drawImage(adjusted, t.getCanvasLocation().getX() + 1, t.getCanvasLocation().getY() + 1, null);

				}
			}

			plugin.ensureInfoText();
		}

		return null;
	}
}
