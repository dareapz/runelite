/*
 * Copyright (c) 2018, Psikoi <https://github.com/psikoi>
 * Copyright (c) 2018, Adam <Adam@sigterm.info>
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
package net.runelite.client.plugins.loottracker;

import com.google.common.eventbus.Subscribe;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.swing.SwingUtilities;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.InventoryID;
import net.runelite.api.ItemContainer;
import net.runelite.api.NPC;
import net.runelite.api.Player;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.widgets.WidgetID;
import net.runelite.client.events.NpcLootReceived;
import net.runelite.client.events.PlayerLootReceived;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.ItemStack;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.loottracker.data.LootRecord;
import net.runelite.client.plugins.loottracker.data.LootRecordWriter;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.Text;

@PluginDescriptor(
	name = "Loot Tracker",
	description = "Tracks loot from monsters and minigames",
	tags = {"drops"},
	enabledByDefault = false
)
@Slf4j
public class LootTrackerPlugin extends Plugin
{
	// Activity/Event loot handling
	private static final Pattern CLUE_SCROLL_PATTERN = Pattern.compile("You have completed [0-9]+ ([a-z]+) Treasure Trails.");

	@Inject
	private ClientToolbar clientToolbar;

	@Inject
	private ItemManager itemManager;

	@Inject
	private SpriteManager spriteManager;

	@Inject
	private Client client;

	private LootRecordWriter writer;
	private LootTrackerPanel panel;
	private NavigationButton navButton;
	private String eventType;

	@Override
	protected void startUp() throws Exception
	{
		writer = new LootRecordWriter();

		panel = new LootTrackerPanel(itemManager, this);

		final BufferedImage icon;
		synchronized (ImageIO.class)
		{
			icon = ImageIO.read(LootTrackerPanel.class.getResourceAsStream("panel_icon.png"));
		}

		navButton = NavigationButton.builder()
			.tooltip("Loot Tracker")
			.icon(icon)
			.priority(5)
			.panel(panel)
			.build();

		clientToolbar.addNavigation(navButton);
	}

	@Override
	protected void shutDown()
	{
		clientToolbar.removeNavigation(navButton);
	}

	@Subscribe
	public void onNpcLootReceived(final NpcLootReceived npcLootReceived)
	{
		final NPC npc = npcLootReceived.getNpc();
		final Collection<ItemStack> items = npcLootReceived.getItems();
		final String name = npc.getName();
		final int combat = npc.getCombatLevel();
		SwingUtilities.invokeLater(() -> panel.addLootRecord(new LootRecord(npc.getId(), name, combat, -1, items)));
	}

	@Subscribe
	public void onPlayerLootReceived(final PlayerLootReceived playerLootReceived)
	{
		final Player player = playerLootReceived.getPlayer();
		final Collection<ItemStack> items = playerLootReceived.getItems();
		final String name = player.getName();
		final int combat = player.getCombatLevel();
		SwingUtilities.invokeLater(() -> panel.addLootRecord(new LootRecord(-1, name, combat, -1, items)));
	}

	@Subscribe
	public void onWidgetLoaded(WidgetLoaded event)
	{
		final ItemContainer container;
		switch (event.getGroupId())
		{
			case (WidgetID.BARROWS_REWARD_GROUP_ID):
				eventType = "Barrows";
				container = client.getItemContainer(InventoryID.BARROWS_REWARD);
				break;
			case (WidgetID.CHAMBERS_OF_XERIC_REWARD_GROUP_ID):
				eventType = "Chambers of Xeric";
				container = client.getItemContainer(InventoryID.CHAMBERS_OF_XERIC_CHEST);
				break;
			case (WidgetID.THEATRE_OF_BLOOD_GROUP_ID):
				eventType = "Theatre of Blood";
				container = client.getItemContainer(InventoryID.THEATRE_OF_BLOOD_CHEST);
				break;
			case (WidgetID.CLUE_SCROLL_REWARD_GROUP_ID):
				// event type should be set via ChatMessage for clue scrolls.
				// Clue Scrolls use same InventoryID as Barrows
				container = client.getItemContainer(InventoryID.BARROWS_REWARD);
				break;
			default:
				return;
		}

		if (container == null)
		{
			return;
		}

		// Convert container items to array of ItemStack
		final Collection<ItemStack> items = Arrays.stream(container.getItems())
			.map(item -> new ItemStack(item.getId(), item.getQuantity()))
			.collect(Collectors.toList());

		if (items.size() > 0)
		{
			log.debug("Loot Received from Event: {}", eventType);
			for (ItemStack item : items)
			{
				log.debug("Item Received: {}x {}", item.getQuantity(), item.getId());
			}

			SwingUtilities.invokeLater(() -> panel.addLootRecord(new LootRecord(-1, eventType, -1, -1, items)));
		}
		else
		{
			log.debug("No items to find for Event: {} | Container: {}", eventType, container);
		}
	}

	@Subscribe
	public void onChatMessage(ChatMessage event)
	{
		if (event.getType() != ChatMessageType.SERVER && event.getType() != ChatMessageType.FILTERED)
		{
			return;
		}

		// Check if message is for a clue scroll reward
		final Matcher m = CLUE_SCROLL_PATTERN.matcher(Text.removeTags(event.getMessage()));
		if (m.find())
		{
			final String type = m.group(1).toLowerCase();
			switch (type)
			{
				case "easy":
					eventType = "Clue Scroll (Easy)";
					break;
				case "medium":
					eventType = "Clue Scroll (Medium)";
					break;
				case "hard":
					eventType = "Clue Scroll (Hard)";
					break;
				case "elite":
					eventType = "Clue Scroll (Elite)";
					break;
				case "master":
					eventType = "Clue Scroll (Master)";
					break;
			}
		}
	}

	public Set<String> getUniqueNames()
	{
		Set<String> n = new HashSet<>();
		for (LootRecord r : getAllData())
		{
			n.add(r.getName());
		}
		return n;
	}

	public Collection<LootRecord> getAllData()
	{
		return writer.loadAllData();
	}

	public Collection<LootRecord> getData(String name)
	{
		return writer.loadData(name);
	}

	// Wrapper for writer.clearData so its available elsewhere
	public void clearData(String name)
	{
		log.debug("Clearing data for: " + name);
		writer.clearData(name);

	}
}
