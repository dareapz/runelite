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
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.inject.Inject;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.ItemComposition;
import net.runelite.api.Varbits;
import net.runelite.api.events.GameTick;
import net.runelite.api.widgets.JavaScriptCallback;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

@PluginDescriptor(
	name = "Kept on Death",
	description = "Reworks the Items Kept on Death interface to be more accurate"
)
@Slf4j
public class KeptOnDeathPlugin extends Plugin
{
	private static final int MAX_ROW_ITEMS = 8;
	private static final int STARTING_X = 5;
	private static final int X_INCREMENT = 40;
	private static final int STARTING_Y = 25;
	private static final int Y_INCREMENT = 38;

	private static final String CHANGED_MECHANICS = "<br><br>Untradeable items are kept on death in non-pvp scenarios.";
	private static final String NON_PVP = "<br><br>If you die you have 1 hour to retrieve your lost items.";
	private static final String MAX_KEPT_ITEMS_FORMAT = "<col=ffcc33>Max items kept on death :<br><br><col=ffcc33>~ %s ~";
	private static final DecimalFormat NUMBER_FORMAT = new DecimalFormat("#,###");

	@Inject
	private Client client;

	@Inject
	private ItemManager itemManager;

	@Getter
	private boolean widgetVisible = false;

	@Subscribe
	protected void onGameTick(GameTick t)
	{
		boolean old = widgetVisible;
		widgetVisible = client.getWidget(WidgetInfo.ITEMS_LOST_ON_DEATH_CONTAINER) != null;
		if (widgetVisible && !old)
		{
			reorganizeWidgetItems();
			updateKeptWidgetInfoText();
		}
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

	private void reorganizeWidgetItems()
	{
		int wildyLevel = getCurrentWildyLevel();
		// Death mechanics aren't changed above 20 wildy since all untradeables are lost on death
		if (wildyLevel >= 20)
		{
			return;
		}

		Widget lost = client.getWidget(WidgetInfo.ITEMS_LOST_ON_DEATH_CONTAINER);
		Widget kept = client.getWidget(WidgetInfo.ITEMS_KEPT_ON_DEATH_CONTAINER);
		if (lost != null && kept != null)
		{
			Widget[] children = lost.getDynamicChildren();
			List<Widget> keptItems = new ArrayList<>(Arrays.asList(kept.getChildren()));
			List<Widget> lostItems = new ArrayList<>(Arrays.asList(children));
			for (int i = (children.length - 1); i >= 0; i--)
			{
				Widget t = children[i];
				ItemComposition c = itemManager.getItemComposition(t.getItemId());
				if (!c.isTradeable())
				{
					// Certain items are turned into broken variants inside the wilderness.
					if (BrokenOnDeathItem.check(t.getItemId()))
					{
						t.setOnOpListener((JavaScriptCallback) ev -> fixedItemOpListener(c.getName(), t.getItemQuantity()));
						keptItems.add(t);
						lostItems.remove(t);
						continue;
					}

					// Ignore all non tradeables in wildy except for the above case(s).
					if (wildyLevel > 0)
					{
						continue;
					}

					// Certain items are always lost on death and have a white outline
					AlwaysLostItem item = AlwaysLostItem.getByItemID(t.getItemId());
					if (item != null)
					{
						// Some of these items are actually lost on death, like the looting bag, so don't add a border.
						if (!item.isKept())
						{
							continue;
						}
					}

					t.setOnOpListener((JavaScriptCallback) ev -> fixedItemOpListener(c.getName(), t.getItemQuantity()));
					keptItems.add(t);
					lostItems.remove(t);
				}
			}

			setWidgetChildren(kept, keptItems);
			if (keptItems.size() > 8)
			{
				// Adjust items lost container position if new rows were added to kept items container
				lost.setOriginalY(lost.getOriginalY() + ((keptItems.size() / 8) * Y_INCREMENT));
			}
			setWidgetChildren(lost, lostItems);
		}
	}

	/**
	 * Wrapper for widget.setChildren() to avoid hn[] casting issues
	 * @param parent Widget to override children
	 * @param widgets Children to set on parent
	 */
	private void setWidgetChildren(Widget parent, List<Widget> widgets)
	{
		Widget[] children = parent.getChildren();
		// If add flag is false override existing children by starting at 0
		Widget[] itemsArray = Arrays.copyOf(children, widgets.size());

		int parentId = parent.getId();
		int startingIndex = 0;
		for (Widget w : widgets)
		{
			int originalX = STARTING_X + ((startingIndex % MAX_ROW_ITEMS) * X_INCREMENT);
			int originalY = STARTING_Y + ((startingIndex / MAX_ROW_ITEMS) * Y_INCREMENT);

			w.setParentId(parentId);
			w.setId(parentId);
			w.setIndex(startingIndex);

			w.setOriginalX(originalX);
			w.setOriginalY(originalY);
			w.revalidate();

			itemsArray[startingIndex] = w;
			startingIndex++;
		}

		parent.setChildren(itemsArray);
		parent.revalidate();
	}

	private void updateKeptWidgetInfoText()
	{
		String textToAdd = CHANGED_MECHANICS;

		if (getCurrentWildyLevel() < 1)
		{
			textToAdd += NON_PVP;
		}

		Widget info = client.getWidget(WidgetInfo.ITEMS_KEPT_INFORMATION_CONTAINER);
		info.setText(info.getText() + textToAdd);

		// Update Items lost total value
		Widget lost = client.getWidget(WidgetInfo.ITEMS_LOST_ON_DEATH_CONTAINER);
		int total = 0;
		for (Widget w : lost.getChildren())
		{
			if (w.getItemId() == -1)
			{
				continue;
			}
			total += itemManager.getItemPrice(w.getItemId());
		}
		Widget lostValue = client.getWidget(WidgetInfo.ITEMS_LOST_VALUE);
		lostValue.setText(NUMBER_FORMAT.format(total) + " gp");


		// Update Max items kept
		Widget kept = client.getWidget(WidgetInfo.ITEMS_KEPT_ON_DEATH_CONTAINER);
		Widget max = client.getWidget(WidgetInfo.ITEMS_KEPT_MAX);
		max.setText(String.format(MAX_KEPT_ITEMS_FORMAT, kept.getChildren().length));
	}

	private void fixedItemOpListener(String name, int quantity)
	{
		client.runScript(1603, 1, quantity, name);
	}
}
