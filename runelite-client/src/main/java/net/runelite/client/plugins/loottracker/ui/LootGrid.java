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
package net.runelite.client.plugins.loottracker.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.Map;
import java.util.Set;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.loottracker.data.LootTrackerItemEntry;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.util.StackFormatter;

@Slf4j
public class LootGrid extends JPanel
{
	private static final int ITEMS_PER_ROW = 5;
	private static final Dimension PANEL_SIZE = new Dimension(215, 60);
	private final JPanel itemContainer = new JPanel();
	private final Map<Integer, LootTrackerItemEntry> consolidated;
	private boolean hideUniques;
	private Set<Integer> uniqueIds;
	private ItemManager itemManager;

	@Getter
	private long totalValue;

	LootGrid(Map<Integer, LootTrackerItemEntry> consolidated, boolean hideUniques, Set<Integer> uniqueIds, ItemManager itemManager)
	{
		this.consolidated = consolidated;
		this.hideUniques = hideUniques;
		this.uniqueIds = uniqueIds;
		this.itemManager = itemManager;

		this.setLayout(new BorderLayout());

		add(itemContainer, BorderLayout.NORTH);

		buildItems();
	}

	/**
	 * This method creates stacked items from the item list, calculates total price and then
	 * displays all the items in the UI.
	 */
	private void buildItems()
	{
		totalValue = 0;

		// Calculates how many rows need to be display to fit all items
		final int rowSize = ((consolidated.size() % ITEMS_PER_ROW == 0) ? 0 : 1) + consolidated.size() / ITEMS_PER_ROW;

		itemContainer.removeAll();
		itemContainer.setLayout(new GridLayout(rowSize, ITEMS_PER_ROW, 1, 1));

		Object[] items = consolidated.values().toArray();
		for (int i = 0; i < rowSize * ITEMS_PER_ROW; i++)
		{
			final JPanel slotContainer = new JPanel();
			slotContainer.setBackground(ColorScheme.DARKER_GRAY_COLOR);
			slotContainer.setSize(PANEL_SIZE);

			if (i < items.length)
			{
				final LootTrackerItemEntry item = (LootTrackerItemEntry) items[i];
				if (item == null || (this.hideUniques && this.uniqueIds.contains(item.getId())))
				{
					continue;
				}
				final JLabel imageLabel = new JLabel();
				imageLabel.setToolTipText(buildToolTip(item));
				imageLabel.setVerticalAlignment(SwingConstants.CENTER);
				imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
				itemManager.getImage(item.getId(), item.getQuantity(), item.getQuantity() > 1).addTo(imageLabel);
				slotContainer.add(imageLabel);


				totalValue = totalValue + item.getTotal();
			}

			itemContainer.add(slotContainer);
		}

		itemContainer.repaint();

		repaint();
	}

	private static String buildToolTip(LootTrackerItemEntry item)
	{
		final String name = item.getName();
		final int quantity = item.getQuantity();
		final long price = item.getPrice();

		return "<html>" + name + " x " + StackFormatter.formatNumber(quantity)
			+ "<br/>Price: " + StackFormatter.quantityToStackSize(price)
			+ "<br/>Total: " + StackFormatter.quantityToStackSize(quantity * price) +	"</html";
	}
}
