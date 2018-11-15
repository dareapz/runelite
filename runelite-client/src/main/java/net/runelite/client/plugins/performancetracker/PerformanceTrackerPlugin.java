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
package net.runelite.client.plugins.performancetracker;

import static com.google.common.base.MoreObjects.firstNonNull;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Provides;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.inject.Inject;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Actor;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.Skill;
import net.runelite.api.Varbits;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.ExperienceChanged;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.HitsplatApplied;
import net.runelite.api.events.LocalPlayerDeath;
import net.runelite.api.events.ScriptCallbackEvent;
import net.runelite.api.events.VarbitChanged;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.performancetracker.data.ActivityInfo;
import net.runelite.client.plugins.performancetracker.data.NpcExpModifier;
import net.runelite.client.plugins.performancetracker.data.stats.CastleWars;
import net.runelite.client.plugins.performancetracker.data.stats.Performance;
import net.runelite.client.plugins.performancetracker.data.stats.TheatreOfBlood;
import net.runelite.client.plugins.performancetracker.overlays.GenericOverlay;
import net.runelite.client.plugins.xptracker.XpTrackerPlugin;
import net.runelite.client.plugins.xptracker.XpWorldType;
import net.runelite.client.task.Schedule;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.Text;

@PluginDescriptor(
	name = "Performance Tracker",
	description = "Shows your current performance stats",
	tags = {"performance", "stats", "activity", "tracker"}
)
@Slf4j
public class PerformanceTrackerPlugin extends Plugin
{
	// For every damage point dealt, 1.33 experience is given to the player's hitpoints (base rate)
	private static final double HITPOINT_RATIO = 1.33;
	private static final double DMM_MULTIPLIER_RATIO = 10;

	private static final Pattern DEATH_TEXT = Pattern.compile("You have died. Death count: \\d.");

	@Inject
	private Client client;

	@Inject
	private ChatMessageManager chatMessageManager;

	@Inject
	private GenericOverlay genericOverlay;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private PerformanceTrackerConfig config;

	@Getter
	private boolean enabled = false;
	private boolean ignoreLoginTick = false;
	private int region = 0;
	private XpWorldType lastWorldType;
	private boolean playingDeadManMode = false;
	// Damage Tracking
	private Actor oldTarget;
	private double hpExp = 0;
	// Current stats

	private final List<Performance> performances = new ArrayList<>();
	private final List<String> messages = new ArrayList<>();
	@Getter
	private Performance current;

	// Theatre of Blood
	private int tobVarbit = 0;
	private boolean isSpectator = false;

	// Castle Wars
	private boolean inCastleWarsGame = false;

	@Provides
	PerformanceTrackerConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(PerformanceTrackerConfig.class);
	}

	@Override
	protected void startUp()
	{
		overlayManager.add(genericOverlay);
	}

	@Override
	protected void shutDown()
	{
		overlayManager.remove(genericOverlay);
		disablePlugin();
		messages.clear();
		performances.clear();
		tobVarbit = -1;
	}

	// Determine Region change
	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		switch (event.getGameState())
		{
			case LOGGED_IN:
				break;
			case LOGIN_SCREEN:
				if (enabled)
				{
					// Disable plugin on logout
					disablePlugin();
				}
			case HOPPING:
				ignoreLoginTick = true;
			default:
				return;
		}

		// Check for world type changes
		XpWorldType type = XpTrackerPlugin.worldSetToType(client.getWorldType());
		if (lastWorldType != type)
		{
			// Reset
			log.debug("World Type change: {} -> {}",
				firstNonNull(lastWorldType, "<unknown>"),
				firstNonNull(type, "<unknown>"));

			lastWorldType = type;
			playingDeadManMode = type.equals(XpWorldType.DMM) || type.equals(XpWorldType.SDMM);
		}

		// Region Change
		int oldRegion = region;
		region = WorldPoint.fromLocalInstance(client, client.getLocalPlayer().getLocalLocation()).getRegionID();
		if (oldRegion != region)
		{
			handleRegionChange();
		}
	}

	// Calculate Damage Dealt
	@Subscribe
	protected void onExperienceChanged(ExperienceChanged c)
	{
		if (!enabled)
		{
			return;
		}

		if (c.getSkill().equals(Skill.HITPOINTS))
		{
			double oldExp = hpExp;
			hpExp = client.getSkillExperience(Skill.HITPOINTS);
			if (ignoreLoginTick)
			{
				// We've updated the stored HP but we need to ignore this XP drop.
				return;
			}
			double diff = hpExp - oldExp;
			if (diff < 1)
			{
				return;
			}
			double damageDealt = calculateDamageDealt(diff);

			// Add damage dealt to the current logs
			log.debug("Damage Dealt: {} | Exact: {}", Math.round(damageDealt), damageDealt);
			handleDamageDealt(damageDealt);
		}
	}

	// Calculate Damage Taken
	@Subscribe
	protected void onHitsplatApplied(HitsplatApplied e)
	{
		if (!enabled)
		{
			return;
		}

		if (e.getActor().equals(client.getLocalPlayer()))
		{
			handleDamageTaken(e.getHitsplat().getAmount());
		}
	}

	// Ensure we know who we were last interacting with.
	@Subscribe
	public void onGameTick(GameTick tick)
	{
		oldTarget = client.getLocalPlayer().getInteracting();
		if (ignoreLoginTick)
		{
			ignoreLoginTick = false;
		}

		checkCwGame();
	}

	@Subscribe
	public void onScriptCallbackEvent(ScriptCallbackEvent e)
	{
		final String eventName = e.getEventName();

		// Handles Fake XP drops (Ironman, DMM Cap, 200m xp, etc)
		if (eventName.equals("fakeXpDrop"))
		{
			final int[] intStack = client.getIntStack();
			final int intStackSize = client.getIntStackSize();

			final int skillId = intStack[intStackSize - 2];
			final Skill skill = Skill.values()[skillId];
			if (skill.equals(Skill.HITPOINTS))
			{
				final int exp = intStack[intStackSize - 1];
				double damageDealt = calculateDamageDealt(exp);
				// Add damage dealt to the current logs
				log.debug("Fake Exp Damage Dealt: {} | Exact: {}", Math.round(damageDealt), damageDealt);
				handleDamageDealt(damageDealt);
			}

			client.setIntStackSize(intStackSize - 2);
		}
	}

	// Help us calculate damage per second.
	@Schedule(
		period = 1,
		unit = ChronoUnit.SECONDS
	)
	public void secondTick()
	{
		if (enabled)
		{
			current.incrementSeconds();
		}
	}

	@Subscribe
	protected void onChatMessage(ChatMessage m)
	{
		// Theatre of Blood death check
		if (!isSpectator && tobVarbit > 1 && m.getType() == ChatMessageType.SERVER)
		{
			String message = Text.removeTags(m.getMessage());
			Matcher match = DEATH_TEXT.matcher(message);
			if (match.matches())
			{
				// Died
				current.addDeath();
			}
		}
	}

	@Subscribe
	protected void onLocalPlayerDeath(LocalPlayerDeath e)
	{
		if (enabled)
		{
			// Theatre of Blood deaths are handled via chat messages.
			if (tobVarbit > 1)
			{
				return;
			}
			current.addDeath();
		}
	}

	@Subscribe
	protected void onVarbitChanged(VarbitChanged e)
	{
		// Theatre of Blood Varbit
		int oldTobVarbit = tobVarbit;
		tobVarbit = client.getVar(Varbits.THEATRE_OF_BLOOD);
		if (oldTobVarbit != tobVarbit)
		{
			tobVarbitChanged(oldTobVarbit);
		}
	}

	private void handleDamageTaken(double damage)
	{
		current.addDamageTaken(damage);
	}

	private void handleDamageDealt(double damage)
	{
		current.addDamageDealt(damage);
	}

	private void submitPerformance()
	{
		performances.add(current);
		reset();
	}

	private void reset()
	{
		current = null;
	}

	private void enablePlugin()
	{
		if (enabled)
		{
			return;
		}

		// Grab Starting EXP
		hpExp = client.getSkillExperience(Skill.HITPOINTS);

		if (current == null)
		{
			current = new Performance();
		}
		enabled = true;
	}

	private void disablePlugin()
	{
		enabled = false;
		reset();
	}

	/**
	 * Send all queued messages via in-game chat
	 */
	private void sendChatMessages()
	{
		for (String message : messages)
		{
			log.debug("Sending Message: {}", message);
			chatMessageManager.queue(QueuedMessage.builder()
				.type(ChatMessageType.GAME)
				.runeLiteFormattedMessage(message)
				.build());
		}

		messages.clear();
	}

	/**
	 * Handles region changes and triggers region-specific functionality
	 */
	private void handleRegionChange()
	{
		boolean configCheck = true;
		switch (region)
		{
			// Edge men for testing
			case 12342:
			case 12343:
			case 12598:
			case 12599:
			case 12086:
			case 12087:
				break;
			case ActivityInfo.TOB.REGION.MAIDEN:
				// Maiden means starting a new raid
				if (config.trackTheatreOfBlood())
				{
					reset();
					current = new TheatreOfBlood();
					enablePlugin();
				}
				handleTheatreOfBloodAct(ActivityInfo.TOB.ACT.MAIDEN);
				return;
			case ActivityInfo.TOB.REGION.BLOAT:
				handleTheatreOfBloodAct(ActivityInfo.TOB.ACT.BLOAT);
				configCheck = config.trackTheatreOfBlood();
				break;
			case ActivityInfo.TOB.REGION.NYLOCAS:
				handleTheatreOfBloodAct(ActivityInfo.TOB.ACT.NYLOCAS);
				configCheck = config.trackTheatreOfBlood();
				break;
			case ActivityInfo.TOB.REGION.SOTETSEG:
				handleTheatreOfBloodAct(ActivityInfo.TOB.ACT.SOTETSEG);
				configCheck = config.trackTheatreOfBlood();
				break;
			case ActivityInfo.TOB.REGION.XARPUS:
				handleTheatreOfBloodAct(ActivityInfo.TOB.ACT.XARPUS);
				configCheck = config.trackTheatreOfBlood();
				break;
			case ActivityInfo.TOB.REGION.VERZIK:
				handleTheatreOfBloodAct(ActivityInfo.TOB.ACT.VERZIK);
				configCheck = config.trackTheatreOfBlood();
				break;
			case ActivityInfo.TOB.REGION.REWARD:
				handleTheatreOfBloodAct(ActivityInfo.TOB.ACT.REWARD);
				configCheck = config.trackTheatreOfBlood();
				break;
			case ActivityInfo.TOB.REGION.LOBBY:
				if (tobVarbit != 0)
				{
					// Only handle lobby if we are in a party
					handleTheatreOfBloodAct(ActivityInfo.TOB.ACT.LOBBY);
				}
				configCheck = false; // Disable overlay while in lobby, going to maiden will re-enable it.
				break;
			default:
				if (enabled)
				{
					// Certain Activities should not reset between region changes
					if (tobVarbit > 1 || inCastleWarsGame)
					{
						return;
					}
					disablePlugin();
				}
				return;
		}

		// Did they enable the activity for this region?
		if (configCheck)
		{
			enablePlugin();
		}
		else if (enabled)
		{
			// toggled config mid activity?
			disablePlugin();
		}

		sendChatMessages();
	}

	/**
	 * Calculates damage dealt based on HP xp gained
	 * @param diff HP xp gained
	 * @return damage dealt
	 */
	private double calculateDamageDealt(double diff)
	{
		double damageDealt = diff / HITPOINT_RATIO;

		// Determine which NPC we attacked.
		Actor a = client.getLocalPlayer().getInteracting();
		if (a == null)
		{
			// If we are interacting with nothing we may have clicked away at the perfect time
			// Fall back to the actor we were interacting with last game tick
			if (oldTarget == null)
			{
				log.warn("Couldn't find current or past target...");
				return damageDealt;
			}
			a = oldTarget;
		}
		if (!(a instanceof NPC))
		{
			return damageDealt;
		}
		NPC target = (NPC) a;

		// Account for NPCs phases by NPC id
		String targetName = getRealNpcName(target);
		log.debug("Attacking NPC named: {}", targetName);

		// Some NPCs have an XP modifier
		NpcExpModifier m = NpcExpModifier.getByName(targetName);
		if (m != null)
		{
			damageDealt = damageDealt / NpcExpModifier.calculateBonus(m);
		}

		// DeadMan mode has an XP modifier
		if (playingDeadManMode)
		{
			damageDealt = damageDealt / DMM_MULTIPLIER_RATIO;
		}
		return damageDealt;
	}

	/**
	 * Return the NPC name accounting for special use cases
	 * @param target target NPC
	 * @return NPC name adjusted for special internal use cases
	 */
	private String getRealNpcName(NPC target)
	{
		String name = target.getName();

		switch (name.toUpperCase())
		{
			case "VERZIK":
				return NpcExpModifier.getNameByNpcId(target.getId());
			default:
				return name;
		}
	}


	/** Theatre of Blood Section **/
	// Advancing Room (region change)
	private void handleTheatreOfBloodAct(int act)
	{
		if (isSpectator)
		{
			return;
		}

		if (!(current instanceof TheatreOfBlood))
		{
			log.warn("Tried handling ToB Act on non-TheatreOfBlood performance: {} | {}", act, current);
			return;
		}

		TheatreOfBlood tob = (TheatreOfBlood) current;

		List<String> ms = new ArrayList<>();

		// Went to new room which means last room was completed.
		switch (act)
		{
			// Reward Region
			case ActivityInfo.TOB.ACT.REWARD:
				tob.setCompleted(true);
				// Show verzik room stats upon entering reward room
				ms.add(PerformanceTrackerMessages.tobRoomMessage(tob));
				ms.add(PerformanceTrackerMessages.tobCurrentMessage(tob));
				break;
			// Starting Maiden
			case ActivityInfo.TOB.ACT.MAIDEN:
				break;
			// Back to lobby, show total stats
			case ActivityInfo.TOB.ACT.LOBBY:
				// If we didn't complete the raid show the last rooms stats as well
				if (!tob.isCompleted())
				{
					ms.add(PerformanceTrackerMessages.tobRoomMessage(tob));
					ms.add(PerformanceTrackerMessages.tobCurrentMessage(tob));
				}
				submitPerformance();
				ms.addAll(PerformanceTrackerMessages.tobTotalMessage(performances));
				break;
			default:
				// Between each room display previous stats
				ms.add(PerformanceTrackerMessages.tobRoomMessage(tob));
				ms.add(PerformanceTrackerMessages.tobCurrentMessage(tob));
				tob.nextRoom(act);
		}

		messages.addAll(ms);
		log.debug("Starting act {}", act);
	}


	/**
	 * Handles changes to the Theatre of Blood Varbit
	 * @param old previous Theatre of Blood varbit value
	 */
	private void tobVarbitChanged(int old)
	{
		// TODO: Figure out a way to determine if they are logging back into a raid
		switch (tobVarbit)
		{
			case ActivityInfo.TOB.STATE.DEFAULT:
				if (old == ActivityInfo.TOB.STATE.INSIDE)
				{
					isSpectator = false;
					return;
				}
				reset();
				break;
			case ActivityInfo.TOB.STATE.PARTY:
				// Joined party, nothing we need to do?
				break;
			case ActivityInfo.TOB.STATE.INSIDE:
				// Inside the Theatre, are they a spectator?
				if (old == ActivityInfo.TOB.STATE.DEFAULT)
				{
					isSpectator = true;
					return;
				}
				break;
			case ActivityInfo.TOB.STATE.SPECTATING_MEMBER:
				if (old == ActivityInfo.TOB.STATE.DEFAULT)
				{
					log.debug("Logged out and returned to non-existing raid");
					return;
				}
				break;
		}
	}


	/** Castle Wars Section **/
	// Check for Time Remaining in-game widget.
	private void checkCwGame()
	{
		WidgetInfo info;
		switch (client.getLocalPlayer().getTeam())
		{
			case ActivityInfo.CW.TEAM.SARA:
				info = WidgetInfo.CW_TIME_REMAINING_SARA;
				break;
			case ActivityInfo.CW.TEAM.ZAM:
				info = WidgetInfo.CW_TIME_REMAINING_ZAM;
				break;
			default:
				return;
		}

		Widget gameTimeRemaining = client.getWidget(info);
		boolean old = inCastleWarsGame;
		inCastleWarsGame = gameTimeRemaining != null && !gameTimeRemaining.isHidden();
		if (old != inCastleWarsGame)
		{
			toggleCastleWarsGame(old);
		}
	}

	private void toggleCastleWarsGame(boolean old)
	{
		if (!config.trackCastleWars())
		{
			return;
		}

		// Was inside the game
		if (old)
		{
			finishCwGame();
		}
		else
		{
			reset();
			CastleWars game = new CastleWars();
			game.setTeam(client.getLocalPlayer().getTeam());
			current = game;
			enablePlugin();
		}
	}

	private void finishCwGame()
	{
		CastleWars game = (CastleWars) current;

		game.setSaraScore(client.getVar(Varbits.CW_SARA_SCORE));
		game.setZamScore(client.getVar(Varbits.CW_ZAM_SCORE));

		messages.addAll(PerformanceTrackerMessages.castleWarsMessage(game));
		submitPerformance();
		disablePlugin();
		sendChatMessages();
	}
}
