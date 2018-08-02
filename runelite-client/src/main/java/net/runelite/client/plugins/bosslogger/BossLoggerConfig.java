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

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

import java.awt.Color;

@ConfigGroup("bosslogger")
public interface BossLoggerConfig extends Config
{
	@ConfigItem(
		keyName = "showChatMessages",
		name = "In-game Chat Message Alerts",
		description = "In-Game Chat Messages when Loot Recorded",
		position = 96
	)
	default boolean showChatMessages()
	{
		return true;
	}

	@ConfigItem(
		keyName = "chatMessageColor",
		name = "Chat Message Color",
		description = "Color of the Chat Message alerts",
		position = 97
	)
	default Color chatMessageColor()
	{
		return new Color(0, 75, 255);
	}


	@ConfigItem(
		keyName = "showTrayAlerts",
		name = "Notification Tray Alerts",
		description = "Create Notification Tray alerts when Loot Recorded?",
		position = 98
	)
	default boolean showTrayAlerts()
	{
		return true;
	}

	@ConfigItem(
		keyName = "showLootTotals",
		name = "Show Recorded Loots Panel",
		description = "Configures whether or not the Recorded Loots Panel is shown",
		position = 99
	)
	default boolean showLootTotals()
	{
		return true;
	}
}
