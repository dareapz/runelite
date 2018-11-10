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
import javax.swing.SwingUtilities;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.loottracker.ItemSortTypes;
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

@Slf4j
public class LootPanel extends JPanel
{
	private Collection<LootRecord> records;
	private Map<Integer, Collection<UniqueItemWithLinkedId>> uniqueMap;
	private boolean hideUniques;
	private ItemSortTypes sortType;
	private boolean itemBreakdown;
	private ItemManager itemManager;
	private Map<Integer, LootTrackerItemEntry> consolidated;

	@Getter
	private boolean playbackPlaying = false;
	private boolean cancelPlayback = false;

	public LootPanel(Collection<LootRecord> records, Map<Integer, Collection<UniqueItemWithLinkedId>> uniqueMap, boolean hideUnqiues, ItemSortTypes sort, boolean itemBreakdown, ItemManager itemManager)
	{
		this.records = (records == null ? new ArrayList<>() : records);
		this.uniqueMap = (uniqueMap == null ? new HashMap<>() : uniqueMap);
		this.hideUniques = hideUnqiues;
		this.sortType = sort;
		this.itemBreakdown = itemBreakdown;
		this.itemManager = itemManager;

		setLayout(new GridBagLayout());
		setBorder(new EmptyBorder(0, 10, 0, 10));
		setBackground(ColorScheme.DARK_GRAY_COLOR);

		createConsolidatedArray(this.records);

		// Sort unique map but set defined in UniqueItem
		this.uniqueMap = this.uniqueMap.entrySet()
				.stream()
				.sorted(Map.Entry.comparingByKey())
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

		createPanel(this.records);
	}

	private void createConsolidatedArray(Collection<LootRecord> records)
	{
		// Consolidate all LootTrackerItemEntrys from each record for combined loot totals
		// Create consolidated ItemPanelEntry map
		this.consolidated = LootRecord.consolidateLootTrackerItemEntries(records);

		// Sort consolidated entries by Name
		this.consolidated = this.consolidated.entrySet().stream()
			.sorted(Map.Entry.comparingByValue(new Comparator<LootTrackerItemEntry>()
			{
				@Override
				public int compare(LootTrackerItemEntry o1, LootTrackerItemEntry o2)
				{
					switch (sortType)
					{
						case ITEM_ID:
							return o1.getId() - o2.getId();
						case PRICE:
							if (o1.getPrice() != o2.getPrice())
							{
								return o1.getPrice() > o2.getPrice() ? -1 : 1;
							}
							break;
						case VALUE:
							if (o1.getTotal() != o2.getTotal())
							{
								return o1.getTotal() > o2.getTotal() ? -1 : 1;
							}
							break;
						case ALPHABETICAL:
							// Handled below
							break;
						default:
							log.warn("Sort Type not being handled correctly, defaulting to alphabetical.");
							break;
					}

					// Default to alphabetical
					return o1.getName().compareTo(o2.getName());
				}
			}))
			.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
	}

	public void createPanel(Collection<LootRecord> records)
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
		if (records.size() > 0)
		{
			int amount = records.size();
			LootRecord entry = Iterators.get(records.iterator(), (amount - 1));
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

		if (itemBreakdown)
		{
			// Loop over each dropped item and create an ItemPanel for it
			for (Map.Entry<Integer, LootTrackerItemEntry> entry : this.consolidated.entrySet())
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
		}
		else
		{
			LootGrid grid = new LootGrid(consolidated, hideUniques, uniqueIds, itemManager);
			totalValue = grid.getTotalValue();
			this.add(grid, c);
			c.gridy++;
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

		this.createConsolidatedArray(this.records);
		this.createPanel(this.records);

		this.revalidate();
		this.repaint();
	}

	public void playback()
	{
		playbackPlaying = true;

		if (this.records.size() > 0)
		{
			Collection<LootRecord> recs = new ArrayList<>();
			for (LootRecord r : this.records)
			{
				recs.add(r);

				SwingUtilities.invokeLater(() -> refreshPlayback(recs));

				try
				{
					if (cancelPlayback)
					{
						playbackPlaying = false;
						cancelPlayback = false;
						SwingUtilities.invokeLater(this::addedRecord);
						break;
					}
					Thread.sleep(250);
				}
				catch (InterruptedException e)
				{
					System.out.println(e.getMessage());
				}
			}
		}

		playbackPlaying = false;
	}

	public void cancelPlayback()
	{
		// Only cancel if actually playing
		cancelPlayback = playbackPlaying;
	}

	private void refreshPlayback(Collection<LootRecord> recs)
	{
		this.removeAll();

		this.createConsolidatedArray(recs);
		this.createPanel(recs);

		this.revalidate();
		this.repaint();
	}
}
