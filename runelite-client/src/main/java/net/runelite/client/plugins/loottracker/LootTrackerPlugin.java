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

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.eventbus.Subscribe;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.swing.SwingUtilities;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.InventoryID;
import net.runelite.api.ItemComposition;
import net.runelite.api.ItemContainer;
import net.runelite.api.NPC;
import net.runelite.api.Player;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.widgets.WidgetID;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.events.NpcLootReceived;
import net.runelite.client.events.PlayerLootReceived;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.ItemStack;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.loottracker.data.LootRecord;
import net.runelite.client.plugins.loottracker.data.LootRecordWriter;
import net.runelite.client.plugins.loottracker.data.LootTrackerItemEntry;
import net.runelite.client.plugins.loottracker.data.UniqueItem;
import net.runelite.client.plugins.loottracker.data.UniqueItemWithLinkedId;
import net.runelite.client.plugins.loottracker.ui.LootTrackerPanel;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;
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
	private static final Pattern CLUE_SCROLL_PATTERN = Pattern.compile("You have completed ([0-9]+) ([a-z]+) Treasure Trails.");
	private static final Pattern BOSS_NAME_NUMBER_PATTERN = Pattern.compile("Your (.*) kill count is: ([0-9]*).");
	private static final Pattern NUMBER_PATTERN = Pattern.compile("([0-9]*)");
	private static final int THEATRE_OF_BLOOD_REGION = 12867;

	@Inject
	private ClientToolbar clientToolbar;

	@Inject
	private ItemManager itemManager;

	@Inject
	private SpriteManager spriteManager;

	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	private LootTrackerPanel panel;
	private NavigationButton navButton;
	private String eventType;

	private LootRecordWriter writer;

	private Multimap<String, LootRecord> lootRecordMultimap = ArrayListMultimap.create();
	private Multimap<String, LootRecord> sessionLootRecordMultimap = ArrayListMultimap.create();
	private Multimap<String, UniqueItemWithLinkedId> uniques = ArrayListMultimap.create();
	private Map<String, Integer> killCountMap = new HashMap<>();

	// key = name, value=current killCount
	private boolean loaded = false;
	private String currentPlayer;

	@Override
	protected void startUp() throws Exception
	{
		panel = new LootTrackerPanel(itemManager, this);

		final BufferedImage icon = ImageUtil.getResourceStreamFromClass(getClass(), "panel_icon.png");

		navButton = NavigationButton.builder()
			.tooltip("Loot Tracker")
			.icon(icon)
			.priority(5)
			.panel(panel)
			.build();

		clientToolbar.addNavigation(navButton);

		writer = new LootRecordWriter();

		// Create unique item map if player turns it on plugin while logged in
		if (client.getLocalPlayer() != null)
		{
			createUniqueItemMap();
		}
	}

	private void createUniqueItemMap()
	{
		loaded = true;
		uniques.clear();
		for (UniqueItem i : UniqueItem.values())
		{
			int linkedID = itemManager.getItemComposition(i.getItemID()).getLinkedNoteId();
			for (String s : i.getActivities())
			{
				uniques.put(s.toUpperCase(), new UniqueItemWithLinkedId(linkedID, i));
			}
		}
	}

	public Collection<UniqueItemWithLinkedId> getUniques(String name)
	{
		return uniques.get(name.toUpperCase());
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
		final int killCount = killCountMap.getOrDefault(name.toUpperCase(), -1);
		final LootTrackerItemEntry[] entries = buildEntries(items);
		LootRecord rec = new LootRecord(npc.getId(), name, combat, killCount, Arrays.asList(entries));
		lootRecordMultimap.put(name, rec);
		sessionLootRecordMultimap.put(name, rec);
		writer.addData(name, rec);
		SwingUtilities.invokeLater(() -> panel.addLog(rec));
	}

	@Subscribe
	public void onPlayerLootReceived(final PlayerLootReceived playerLootReceived)
	{
		final Player player = playerLootReceived.getPlayer();
		final Collection<ItemStack> items = playerLootReceived.getItems();
		final String name = player.getName();
		final int combat = player.getCombatLevel();
		final int killCount = killCountMap.getOrDefault(name.toUpperCase(), -1);
		final LootTrackerItemEntry[] entries = buildEntries(items);
		LootRecord rec = new LootRecord(-1, name, combat, killCount, Arrays.asList(entries));
		lootRecordMultimap.put(name, rec);
		sessionLootRecordMultimap.put(name, rec);
		writer.addData(name, rec);
		SwingUtilities.invokeLater(() -> panel.addLog(rec));
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
				int region = WorldPoint.fromLocalInstance(client, client.getLocalPlayer().getLocalLocation()).getRegionID();
				if (region != THEATRE_OF_BLOOD_REGION)
				{
					return;
				}
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
			.filter(item -> item.getId() > 0)
			.map(item -> new ItemStack(item.getId(), item.getQuantity()))
			.collect(Collectors.toList());

		if (!items.isEmpty())
		{
			final LootTrackerItemEntry[] entries = buildEntries(items);
			final int killCount = killCountMap.getOrDefault(eventType.toUpperCase(), -1);
			LootRecord rec =  new LootRecord(-1, eventType, -1, killCount, Arrays.asList(entries));
			lootRecordMultimap.put(eventType, rec);
			sessionLootRecordMultimap.put(eventType, rec);
			writer.addData(eventType, rec);
			SwingUtilities.invokeLater(() -> panel.addLog(rec));
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

		String chatMessage = Text.removeTags(event.getMessage());

		// Check if message is for a clue scroll reward
		final Matcher m = CLUE_SCROLL_PATTERN.matcher(chatMessage);
		if (m.find())
		{
			final String type = m.group(2).toLowerCase();
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


			int killCount = Integer.valueOf(m.group(1));
			killCountMap.put(eventType.toUpperCase(), killCount);
			return;
		}
		// TODO: Figure out better way to handle Barrows and Raids/Raids 2
		// Barrows KC
		if (chatMessage.startsWith("Your Barrows chest count is"))
		{
			Matcher n = NUMBER_PATTERN.matcher(chatMessage);
			if (n.find())
			{
				killCountMap.put("BARROWS", Integer.valueOf(n.group()));
				return;
			}
		}

		// Raids KC
		if (chatMessage.startsWith("Your completed Chambers of Xeric count is"))
		{
			Matcher n = NUMBER_PATTERN.matcher(chatMessage);
			if (n.find())
			{
				killCountMap.put("RAIDS", Integer.valueOf(n.group()));
				return;
			}
		}
		// Raids KC
		if (chatMessage.startsWith("Your completed Theatre of Blood count is"))
		{
			Matcher n = NUMBER_PATTERN.matcher(chatMessage);
			if (n.find())
			{
				killCountMap.put("THEATRE OF BLOOD", Integer.valueOf(n.group()));
				return;
			}
		}
		// Handle all other boss
		Matcher boss = BOSS_NAME_NUMBER_PATTERN.matcher(chatMessage);
		if (boss.find())
		{
			String bossName = boss.group(1);
			int killCount = Integer.valueOf(boss.group(2));
			killCountMap.put(bossName.toUpperCase(), killCount);
		}
	}

	private LootTrackerItemEntry[] buildEntries(final Collection<ItemStack> itemStacks)
	{
		return itemStacks.stream().map(itemStack ->
		{
			final ItemComposition itemComposition = itemManager.getItemComposition(itemStack.getId());
			final int realItemId = itemComposition.getNote() != -1 ? itemComposition.getLinkedNoteId() : itemStack.getId();
			final long price = itemManager.getItemPrice(realItemId);

			return new LootTrackerItemEntry(
				itemComposition.getName(),
				itemStack.getId(),
				itemStack.getQuantity(),
				price,
				itemComposition.isStackable());
		}).toArray(LootTrackerItemEntry[]::new);
	}

	public Collection<LootRecord> getData()
	{
		return lootRecordMultimap.values();
	}

	public Collection<LootRecord> getDataByName(String name)
	{
		return lootRecordMultimap.get(name);
	}

	public void refreshData()
	{
		// Pull data from files
		lootRecordMultimap.clear();
		Collection<LootRecord> recs = writer.loadAllData();
		for (LootRecord r : recs)
		{
			lootRecordMultimap.put(r.getName(), r);
		}
	}

	public void refreshDataByName(String name)
	{
		lootRecordMultimap.removeAll(name);
		Collection<LootRecord> recs = writer.loadData(name);
		lootRecordMultimap.putAll(name, recs);
	}

	public Collection<LootRecord> getSessionData()
	{
		return sessionLootRecordMultimap.values();
	}

	public void clearData()
	{
		lootRecordMultimap.clear();
	}

	public void clearDataByName(String name)
	{
		lootRecordMultimap.removeAll(name);
	}

	public TreeSet<String> getNames()
	{
		return new TreeSet<>(lootRecordMultimap.keySet());
	}


	@Subscribe
	public void onGameStateChanged(GameStateChanged c)
	{
		switch (c.getGameState())
		{
			case CONNECTION_LOST:
			case HOPPING:
			case LOADING:
			case LOGIN_SCREEN:
			case LOGGING_IN:
				if (!loaded)
				{
					clientThread.invoke(this::createUniqueItemMap);
				}
				break;
			case LOGGED_IN:
				if (!loaded)
				{
					clientThread.invoke(this::createUniqueItemMap);
				}

				clientThread.invokeLater(() ->
				{
					String name = client.getLocalPlayer().getName();
					if (name != null)
					{
						log.debug("Found player name: {}", name);
						updatePlayerFolder(name);
						return true;
					}
					else
					{
						log.debug("Local player name still null");
						return false;
					}
				});
		}
	}

	private void updatePlayerFolder(String name)
	{
		if (Objects.equals(currentPlayer, name))
		{
			return;
		}
		currentPlayer = name;
		writer.updatePlayerFolder(name);
		lootRecordMultimap.clear();
		Collection<LootRecord> recs = writer.loadAllData();
		for (LootRecord r : recs)
		{
			lootRecordMultimap.put(r.getName(), r);
		}

		SwingUtilities.invokeLater(() -> panel.updateNames());
	}
}
