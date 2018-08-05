/*
 * Copyright (c) 2018, TheStonedTurtle <www.github.com/TheStonedTurtle>
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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import com.google.common.collect.Iterators;
import lombok.Getter;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.loottracker.data.ItemPanelEntry;
import net.runelite.client.plugins.loottracker.data.UniqueItem;
import net.runelite.client.plugins.loottracker.data.LootRecord;
import net.runelite.client.ui.ColorScheme;

@Getter
public class LootPanel extends JPanel
{
	private Collection<LootRecord> records;
	private Map<Integer, ArrayList<UniqueItem>> uniqueMap;
	private Map<Integer, ItemPanelEntry> consolidated;
	private ItemManager itemManager;

	public LootPanel(Collection<LootRecord> records, Map<Integer, ArrayList<UniqueItem>> uniqueMap, ItemManager itemManager)
	{
		this.records = (records == null ? new ArrayList<>() : records);
		this.uniqueMap = uniqueMap;
		this.itemManager = itemManager;

		setLayout(new GridBagLayout());
		setBorder(new EmptyBorder(0, 10, 0, 10));
		setBackground(ColorScheme.DARK_GRAY_COLOR);

		createConsolidatedArray();

		// Sort unique map but set defined in UniqueItem
		this.uniqueMap = this.uniqueMap.entrySet()
				.stream()
				.sorted(Map.Entry.comparingByKey())
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

		createPanel(this);
	}

	private void createConsolidatedArray()
	{
		// Create consolidated ItemPanelEntry map
		this.consolidated = ItemPanelEntry.createConsolidatedMap(this.records, itemManager);

		// Sort consolidated entries by Name
		this.consolidated = this.consolidated.entrySet().stream()
				.sorted(Map.Entry.comparingByValue(new Comparator<ItemPanelEntry>()
				{
					@Override
					public int compare(ItemPanelEntry o1, ItemPanelEntry o2)
					{
						return o1.getItemName().compareTo(o2.getItemName());
					}
				}))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
	}

	private void createPanel(LootPanel panel)
	{
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		c.gridx = 0;
		c.gridy = 0;

		// Attach all the Unique Items first
		this.uniqueMap.forEach((setPosition, set) ->
		{
			UniqueItemPanel p = new UniqueItemPanel(set, this.consolidated, panel.itemManager);
			panel.add(p, c);
			c.gridy++;
		});

		// Attach Kill Count Panel
		if (this.records.size() > 0)
		{
			int amount = this.records.size();
			LootRecord entry = Iterators.get(this.records.iterator(), (amount - 1));
			if (entry.getKillCount() != -1)
			{
				TextPanel p = new TextPanel("Current Killcount:", entry.getKillCount());
				panel.add(p, c);
				c.gridy++;
			}
			TextPanel p2 = new TextPanel("Kills Logged:", amount);
			panel.add(p2, c);
			c.gridy++;
		}

		// Track total price of all tracked items for this panel
		long totalValue = 0;
		// Ensure it is placed on top of all other panels
		int totalValueIndex = c.gridy;
		c.gridy++;
		// Loop over each dropped item and create an ItemPanel for it
		for ( Map.Entry<Integer, ItemPanelEntry> entry : this.consolidated.entrySet())
		{
			ItemPanelEntry item = entry.getValue();
			ItemPanel p = new ItemPanel(item);
			panel.add(p, c);
			c.gridy++;
			totalValue = totalValue + item.getTotal();
		}

		// Only add the total value element if it has something useful to display
		if (totalValue > 0)
		{
			c.gridy = totalValueIndex;
			TextPanel totalPanel = new TextPanel("Total Value:", totalValue);
			panel.add(totalPanel, c);
		}
	}

	// Update Loot Panel with Updated Records
	public void updateRecords(Collection<LootRecord> records)
	{
		this.records = records;
		refreshPanel();
		this.repaint();
		this.revalidate();
	}

	// Refresh the Panel without updating any data
	private void refreshPanel()
	{
		this.removeAll();
		createConsolidatedArray();
		createPanel(this);
	}
}