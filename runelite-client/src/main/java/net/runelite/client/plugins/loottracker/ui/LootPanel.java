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

import com.google.common.collect.Iterators;
import java.util.HashSet;
import java.util.Set;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.loottracker.data.LootRecord;
import net.runelite.client.plugins.loottracker.data.LootTrackerItemEntry;
import net.runelite.client.plugins.loottracker.data.UniqueItemWithLinkedId;
import net.runelite.client.ui.ColorScheme;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class LootPanel extends JPanel
{
	private Collection<LootRecord> records;
	private Map<Integer, Collection<UniqueItemWithLinkedId>> uniqueMap;
	private boolean hideUniques;
	private ItemManager itemManager;
	private Map<Integer, LootTrackerItemEntry> consolidated;

	public LootPanel(Collection<LootRecord> records, Map<Integer, Collection<UniqueItemWithLinkedId>> uniqueMap, boolean hideUnqiues, ItemManager itemManager)
	{
		this.records = (records == null ? new ArrayList<>() : records);
		this.uniqueMap = (uniqueMap == null ? new HashMap<>() : uniqueMap);
		this.hideUniques = hideUnqiues;
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

		createPanel();
	}

	private void createConsolidatedArray()
	{
		// Consolidate all LootTrackerItemEntrys from each record for combined loot totals
		// Create consolidated ItemPanelEntry map
		this.consolidated = LootRecord.consolidateLootTrackerItemEntries(this.records);

		// Sort consolidated entries by Name
		this.consolidated = this.consolidated.entrySet().stream()
			.sorted(Map.Entry.comparingByValue(new Comparator<LootTrackerItemEntry>()
			{
				@Override
				public int compare(LootTrackerItemEntry o1, LootTrackerItemEntry o2)
				{
					return o1.getName().compareTo(o2.getName());
				}
			}))
			.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
	}

	public void createPanel()
	{
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		c.gridx = 0;
		c.gridy = 0;

		Set<Integer> uniqueIds = new HashSet<>();

		// Attach all the Unique Items first
		this.uniqueMap.forEach((setPosition, set) ->
		{
			UniqueItemPanel p = new UniqueItemPanel(set, this.consolidated, this.itemManager);
			this.add(p, c);
			c.gridy++;

			for (UniqueItemWithLinkedId i : set)
			{
				uniqueIds.add(i.getLinkedID());
				uniqueIds.add(i.getUniqueItem().getItemID());
			}
		});

		// Attach Kill Count Panel
		if (this.records.size() > 0)
		{
			int amount = this.records.size();
			LootRecord entry = Iterators.get(this.records.iterator(), (amount - 1));
			if (entry.getKillCount() != -1)
			{
				TextPanel p = new TextPanel("Current Killcount:", entry.getKillCount());
				this.add(p, c);
				c.gridy++;
			}
			TextPanel p2 = new TextPanel("Kills Logged:", amount);
			this.add(p2, c);
			c.gridy++;
		}

		// Track total price of all tracked items for this panel
		// Also ensure it is placed in correct location by preserving its gridy value
		long totalValue = 0;
		int totalValueIndex = c.gridy;
		c.gridy++;

		// Loop over each dropped item and create an ItemPanel for it
		for ( Map.Entry<Integer, LootTrackerItemEntry> entry : this.consolidated.entrySet())
		{
			LootTrackerItemEntry item = entry.getValue();
			if (!hideUniques || !(hideUniques && uniqueIds.contains(item.getId())))
			{
				ItemPanel p = new ItemPanel(item, itemManager);
				this.add(p, c);
				c.gridy++;
			}
			totalValue = totalValue + item.getTotal();
		}

		// Only add the total value element if it has something useful to display
		if (totalValue > 0)
		{
			c.gridy = totalValueIndex;
			TextPanel totalPanel = new TextPanel("Total Value:", totalValue);
			this.add(totalPanel, c);
		}
	}

	public void addedRecord()
	{
		// TODO: Smarter update system so it only repaints necessary Item and Text Panels
		this.removeAll();

		this.createConsolidatedArray();
		this.createPanel();

		this.revalidate();
		this.repaint();
	}
}
