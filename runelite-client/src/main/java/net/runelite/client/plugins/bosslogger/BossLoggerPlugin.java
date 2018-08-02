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
package net.runelite.client.plugins.bosslogger;

import com.google.common.eventbus.Subscribe;
import com.google.inject.Provides;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.swing.SwingUtilities;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.InventoryID;
import net.runelite.api.ItemContainer;
import net.runelite.api.NPC;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.ConfigChanged;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetID;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.Notifier;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.chat.ChatMessageBuilder;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.events.NpcLootReceived;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.ItemStack;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.bosslogger.data.LootRecord;
import net.runelite.client.plugins.bosslogger.data.LootRecordWriter;
import net.runelite.client.plugins.bosslogger.data.Pet;
import net.runelite.client.plugins.bosslogger.data.Tab;
import net.runelite.client.plugins.bosslogger.ui.BossLoggerPanel;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.Text;

@PluginDescriptor(
	name = "Boss Logger",
	description = "Log loot from PvM bosses",
	tags = {"boss", "loot", "logger", "recorder"}
)
@Slf4j
public class BossLoggerPlugin extends Plugin
{
	private static final Pattern NUMBER_PATTERN = Pattern.compile("([0-9]+)");
	private static final Pattern BOSS_NAME_NUMBER_PATTERN = Pattern.compile("Your (.*) kill count is: ([0-9]+)");
	private static final Pattern PET_RECEIVED_PATTERN = Pattern.compile("You have a funny feeling like ");
	private static final Pattern PET_RECEIVED_INVENTORY_PATTERN = Pattern.compile("You feel something weird sneaking into your backpack.");
	private static final Pattern CLUE_SCROLL_PATTERN = Pattern.compile("You have completed (\\d*) (\\w*) Treasure Trails.");

	@Inject
	private Client client;

	@Inject
	private ItemManager itemManager;

	@Inject
	private BossLoggerConfig bossLoggerConfig;

	@Inject
	private Notifier notifier;

	@Inject
	private ChatMessageManager chatMessageManager;

	@Inject
	private ClientToolbar pluginToolbar;

	@Inject
	private ClientThread clientThread;

	private BossLoggerPanel panel;
	private NavigationButton navButton;
	private LootRecordWriter writer;
	private String messageColor = ""; // in-game chat message color

	// Mapping Variables
	private Map<String, Integer> killCountMap = new HashMap<>();
	private Map<String, Collection<LootRecord>> lootMap = new HashMap<>();
	private boolean gotPet = false;
	private String eventType;

	@Provides
	BossLoggerConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(BossLoggerConfig.class);
	}

	@Override
	protected void startUp()
	{
		init();

		if (bossLoggerConfig.showLootTotals())
		{
			SwingUtilities.invokeLater(this::createPanel);
		}

		writer = new LootRecordWriter();
	}

	@Override
	protected void shutDown() throws Exception
	{
		removePanel();
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		// Only update if our plugin config was changed
		if (!event.getGroup().equals("bosslogger"))
		{
			return;
		}

		handleConfigChanged(event.getKey());
	}

	private void init()
	{
		// Ensure we are using the requested message coloring for in-game messages
		updateMessageColor();
		if (writer == null)
		{
			writer = new LootRecordWriter();
		}
		else
		{
			if (client.getLocalPlayer() != null && client.getLocalPlayer().getName() != null)
			{
				writer.updatePlayerFolder(client.getLocalPlayer().getName());
				lootMap.clear();
			}
		}

	}

	//
	// Panel Functions
	//

	// Separated from startUp for toggling panel from settings
	private void createPanel()
	{
		panel = new BossLoggerPanel(itemManager, this);
		// Panel Icon (Looting Bag)
		BufferedImage icon = null;
		synchronized (ImageIO.class)
		{
			try
			{
				icon = ImageIO.read(getClass().getResourceAsStream("panel_icon.png"));
			}
			catch (IOException e)
			{
				log.warn("Error getting panel icon:", e);
			}
		}

		navButton = NavigationButton.builder()
				.tooltip("Boss Logger")
				.icon(icon)
				.panel(panel)
				.priority(10)
				.build();

		pluginToolbar.addNavigation(navButton);
	}

	private void removePanel()
	{
		pluginToolbar.removeNavigation(navButton);
	}


	//
	// General Functionality functions
	//

	// All alerts from this plugin should use this function
	private void BossLoggedAlert(String message)
	{
		message = "Boss Logger: " + message;
		if (bossLoggerConfig.showChatMessages())
		{
			final ChatMessageBuilder chatMessage = new ChatMessageBuilder()
					.append("<col=" + messageColor + ">")
					.append(message)
					.append("</col>");

			chatMessageManager.queue(QueuedMessage.builder()
					.type(ChatMessageType.EXAMINE_ITEM)
					.runeLiteFormattedMessage(chatMessage.build())
					.build());
		}

		if (bossLoggerConfig.showTrayAlerts())
		{
			notifier.notify(message);
		}
	}

	// Returns stored data by tab
	public Collection<LootRecord> getData(Tab tab)
	{
		// Loot Entries are stored on lootMap by boss name (upper cased)
		Collection<LootRecord> recs = lootMap.get(tab.getBossName());
		if (recs == null)
		{
			recs = writer.loadData(tab.getBossName());
			lootMap.put(tab.getBossName(), recs);
		}
		return recs;
	}

	//
	// LootEntry helper functions
	//



	// Upon cleaning an Unsired add the item to the previous LootEntry
	private void receivedUnsiredLoot(int itemID)
	{
		ItemStack drop = new ItemStack(itemID, 1);
		// Update the last drop
	}

	//
	// Other Helper Functions
	//

	private ItemStack handlePet(String name)
	{
		gotPet = false;
		int petID = getPetId(name);
		BossLoggedAlert("Oh lookie a pet! Don't forget to insure it!");
		return new ItemStack(petID, 1);
	}

	private int getPetId(String name)
	{
		Pet pet = Pet.getByBossName(name);
		if (pet != null)
		{
			return pet.getPetID();
		}
		return -1;
	}

	public void clearData(Tab tab)
	{
		log.debug("Clearing data for tab: " + tab.getName());
		writer.clearData(tab.getBossName());
	}

	// Updates in-game alert chat color based on config settings
	private void updateMessageColor()
	{
		Color c = bossLoggerConfig.chatMessageColor();
		messageColor = String.format("%02x%02x%02x", c.getRed(), c.getGreen(), c.getBlue());
	}

	// Keep the subscribe a bit cleaner, may be a better way to handle this
	private void handleConfigChanged(String eventKey)
	{
		switch (eventKey)
		{
			case "showLootTotals":
				if (bossLoggerConfig.showLootTotals())
				{
					createPanel();
				}
				else
				{
					removePanel();
				}
				return;
			case "chatMessageColor":
				// Update in-game alert color
				updateMessageColor();
				BossLoggedAlert("Example Message");
				return;
			default:
				break;
		}
	}

	@Subscribe
	public void onNpcLootReceived(final NpcLootReceived npcLootReceived)
	{
		final NPC npc = npcLootReceived.getNpc();
		final Collection<ItemStack> items = npcLootReceived.getItems();
		final String name = npc.getName();
		final int combat = npc.getCombatLevel();
		int killCount = killCountMap.getOrDefault(name.toUpperCase(), -1);
		LootRecord r = new LootRecord(npc.getId(), name, combat, killCount, items);

		if (gotPet)
		{
			r.addDropEntry(handlePet(name));
		}

		writer.addData(name, r);
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
			case (WidgetID.DIALOG_SPRITE_GROUP_ID):
				Widget text = client.getWidget(WidgetInfo.DIALOG_SPRITE_TEXT);
				if ("The Font consumes the Unsired and returns you a reward.".equals(text.getText()))
				{
					Widget sprite = client.getWidget(WidgetInfo.DIALOG_SPRITE);
					receivedUnsiredLoot(sprite.getItemId());
				}
				return;
			default:

				// Received unsired loot?
				if (event.getGroupId() == WidgetID.DIALOG_SPRITE_GROUP_ID)
				{
				}
				return;
		}

		if (container == null)
		{
			return;
		}

		// Convert container items to array of ItemStack
		Collection<ItemStack> items = Arrays.stream(container.getItems())
				.map(item -> new ItemStack(item.getId(), item.getQuantity()))
				.collect(Collectors.toList());

		if (items.size() > 0)
		{
			log.debug("Loot Received from Event: {}", eventType);
			for (ItemStack item : items)
			{
				log.debug("Item Received: {}x {}", item.getQuantity(), item.getId());
			}
			int killCount = killCountMap.getOrDefault(eventType.toUpperCase(), -1);
			LootRecord r = new LootRecord(-1, eventType, -1, killCount, items);

			if (gotPet)
			{
				r.addDropEntry(handlePet(eventType));
			}

			writer.addData(eventType, r);
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

		String chatMessage = event.getMessage();

		// Check if message is for a clue scroll reward
		final Matcher m = CLUE_SCROLL_PATTERN.matcher(Text.removeTags(chatMessage));
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
				default:
					log.debug("Unhandled clue scroll case: {}", type);
					log.debug("Matched Chat Message: {}", chatMessage);
					return;
			}

			int killCount = Integer.valueOf(m.group(1));
			killCountMap.put(eventType.toUpperCase(), killCount);
			return;
		}

		// TODO: Figure out better way to handle Barrows and Raids/Raids 2
		// Barrows KC
		if (chatMessage.startsWith("Your Barrows chest count is"))
		{
			Matcher n = NUMBER_PATTERN.matcher(Text.removeTags(chatMessage));
			if (n.find())
			{
				killCountMap.put("BARROWS", Integer.valueOf(m.group()));
				return;
			}
		}

		// Raids KC
		if (chatMessage.startsWith("Your completed Chambers of Xeric count is"))
		{
			Matcher n = NUMBER_PATTERN.matcher(Text.removeTags(chatMessage));
			if (n.find())
			{
				killCountMap.put("CHAMBERS OF XERIC", Integer.valueOf(m.group()));
				return;
			}
		}

		// Raids KC
		if (chatMessage.startsWith("Your completed Theatre of Blood count is"))
		{
			Matcher n = NUMBER_PATTERN.matcher(Text.removeTags(chatMessage));
			if (n.find())
			{
				killCountMap.put("THEATRE OF BLOOD", Integer.valueOf(m.group()));
				return;
			}
		}

		// Handle all other boss
		Matcher boss = BOSS_NAME_NUMBER_PATTERN.matcher(Text.removeTags(chatMessage));
		if (boss.find())
		{
			String bossName = boss.group(1);
			int killCount = Integer.valueOf(boss.group(2));
			killCountMap.put(bossName.toUpperCase(), killCount);
		}

		Matcher pet1 = PET_RECEIVED_PATTERN.matcher(Text.removeTags(chatMessage));
		Matcher pet2 = PET_RECEIVED_INVENTORY_PATTERN.matcher(Text.removeTags(chatMessage));
		if (pet1.find() || pet2.find())
		{
			gotPet = true;
		}
	}

	@Subscribe
	protected void onGameStateChanged(GameStateChanged e)
	{
		if (e.getGameState() == GameState.LOGGED_IN)
		{
			clientThread.invokeLater(() ->
			{
				String name = client.getLocalPlayer().getName();
				if (name != null)
				{
					log.debug("Found player name: {}", name);
					writer.updatePlayerFolder(name);
					lootMap.clear();
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
}