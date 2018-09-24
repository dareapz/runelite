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
package net.runelite.client.plugins.skillcalculator.banked.beans;

import lombok.Getter;
import net.runelite.api.ItemID;
import net.runelite.api.Skill;
import net.runelite.client.plugins.skillcalculator.banked.CriticalItem;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Getter
public enum Activity
{
	/**
	 * Herblore Activities
	 */
	// Creating Potions
	// Guam
	ATTACK_POTION(ItemID.ATTACK_POTION4, "Attack Potion", Skill.HERBLORE, 3, 25, CriticalItem.GUAM_LEAF_POTION_UNF, ActivitySecondaries.ATTACK_POTION),
	GUAM_TAR(ItemID.GUAM_TAR, "Guam tar", Skill.HERBLORE, 19, 30, CriticalItem.GUAM_LEAF_POTION_UNF, ActivitySecondaries.GUAM_TAR),
	// Marrentil
	ANTIPOISON(ItemID.ANTIPOISON4, "Antipoison", Skill.HERBLORE, 5, 37.5, CriticalItem.MARRENTILL_POTION_UNF, ActivitySecondaries.ANTIPOISON),
	MARRENTILL_TAR(ItemID.MARRENTILL_TAR, "Marrentill tar", Skill.HERBLORE, 31, 42.5, CriticalItem.MARRENTILL_POTION_UNF, ActivitySecondaries.MARRENTILL_TAR),
	// Tarromin
	STRENGTH_POTION(ItemID.STRENGTH_POTION4, "Strength potion", Skill.HERBLORE, 12, 50, CriticalItem.TARROMIN_POTION_UNF, ActivitySecondaries.STRENGTH_POTION),
	SERUM_207(ItemID.SERUM_207_4, "Serum 207", Skill.HERBLORE, 15, 50, CriticalItem.TARROMIN_POTION_UNF, ActivitySecondaries.SERUM_207),
	TARROMIN_TAR(ItemID.TARROMIN_TAR, "Tarromin tar", Skill.HERBLORE, 39, 55, CriticalItem.TARROMIN_POTION_UNF, ActivitySecondaries.TARROMIN_TAR),
	// Harralander
	COMPOST_POTION(ItemID.COMPOST_POTION4, "Compost potion", Skill.HERBLORE, 21, 60, CriticalItem.HARRALANDER_POTION_UNF, ActivitySecondaries.COMPOST_POTION),
	RESTORE_POTION(ItemID.RESTORE_POTION4, "Restore potion", Skill.HERBLORE, 22, 62.5, CriticalItem.HARRALANDER_POTION_UNF, ActivitySecondaries.RESTORE_POTION),
	ENERGY_POTION(ItemID.ENERGY_POTION4, "Energy potion", Skill.HERBLORE, 26, 67.5, CriticalItem.HARRALANDER_POTION_UNF, ActivitySecondaries.ENERGY_POTION),
	COMBAT_POTION(ItemID.COMBAT_POTION4, "Combat potion", Skill.HERBLORE, 36, 84, CriticalItem.HARRALANDER_POTION_UNF, ActivitySecondaries.COMBAT_POTION),
	HARRALANDER_TAR(ItemID.HARRALANDER_TAR, "Harralander tar", Skill.HERBLORE, 44, 72.5, CriticalItem.HARRALANDER_POTION_UNF, ActivitySecondaries.HARRALANDER_TAR),
	// Ranarr Weed
	DEFENCE_POTION(ItemID.DEFENCE_POTION4, "Defence potion", Skill.HERBLORE, 30, 75, CriticalItem.RANARR_POTION_UNF, ActivitySecondaries.DEFENCE_POTION),
	PRAYER_POTION(ItemID.PRAYER_POTION4, "Prayer potion", Skill.HERBLORE, 38, 87.5, CriticalItem.RANARR_POTION_UNF, ActivitySecondaries.PRAYER_POTION),
	// Toadflax
	AGILITY_POTION(ItemID.AGILITY_POTION4, "Agility potion", Skill.HERBLORE, 34, 80, CriticalItem.TOADFLAX_POTION_UNF, ActivitySecondaries.AGILITY_POTION),
	SARADOMIN_BREW(ItemID.SARADOMIN_BREW4, "Saradomin brew", Skill.HERBLORE, 81, 180, CriticalItem.TOADFLAX_POTION_UNF, ActivitySecondaries.SARADOMIN_BREW),
	// Irit
	SUPER_ATTACK(ItemID.SUPER_ATTACK4, "Super attack", Skill.HERBLORE, 45, 100, CriticalItem.IRIT_POTION_UNF, ActivitySecondaries.SUPER_ATTACK),
	SUPERANTIPOISON(ItemID.SUPERANTIPOISON4, "Superantipoison", Skill.HERBLORE, 48, 106.3, CriticalItem.IRIT_POTION_UNF, ActivitySecondaries.SUPERANTIPOISON),
	// Avantoe
	FISHING_POTION(ItemID.FISHING_POTION4, "Fishing potion", Skill.HERBLORE, 50, 112.5, CriticalItem.AVANTOE_POTION_UNF, ActivitySecondaries.FISHING_POTION),
	SUPER_ENERGY_POTION(ItemID.SUPER_ENERGY3_20549, "Super energy potion", Skill.HERBLORE, 52, 117.5, CriticalItem.AVANTOE_POTION_UNF, ActivitySecondaries.SUPER_ENERGY_POTION),
	HUNTER_POTION(ItemID.HUNTER_POTION4, "Hunter potion", Skill.HERBLORE, 53, 120, CriticalItem.AVANTOE_POTION_UNF, ActivitySecondaries.HUNTER_POTION),
	// Kwuarm
	SUPER_STRENGTH(ItemID.SUPER_STRENGTH4, "Super strength", Skill.HERBLORE, 55, 125, CriticalItem.KWUARM_POTION_UNF, ActivitySecondaries.SUPER_STRENGTH),
	// Snapdragon
	SUPER_RESTORE(ItemID.SUPER_RESTORE4, "Super restore", Skill.HERBLORE, 63, 142.5, CriticalItem.SNAPDRAGON_POTION_UNF, ActivitySecondaries.SUPER_RESTORE),
	SANFEW_SERUM(ItemID.SANFEW_SERUM4, "Sanfew serum", Skill.HERBLORE, 65, 160, CriticalItem.SNAPDRAGON_POTION_UNF, ActivitySecondaries.SANFEW_SERUM),
	// Cadantine
	SUPER_DEFENCE_POTION(ItemID.SUPER_DEFENCE4, "Super defence", Skill.HERBLORE, 66, 150, CriticalItem.CADANTINE_POTION_UNF, ActivitySecondaries.SUPER_DEFENCE_POTION),
	// Lantadyme
	ANTIFIRE_POTION(ItemID.ANTIFIRE_POTION4, "Anti-fire potion", Skill.HERBLORE, 69, 157.5, CriticalItem.LANTADYME_POTION_UNF, ActivitySecondaries.ANTIFIRE_POTION),
	MAGIC_POTION(ItemID.MAGIC_POTION4, "Magic potion", Skill.HERBLORE, 76, 172.5, CriticalItem.LANTADYME_POTION_UNF, ActivitySecondaries.MAGIC_POTION),
	// Dwarf Weed
	RANGING_POTION(ItemID.RANGING_POTION4, "Ranging potion", Skill.HERBLORE, 72, 162.5, CriticalItem.DWARF_WEED_POTION_UNF, ActivitySecondaries.RANGING_POTION),
	// Torstol
	ZAMORAK_BREW(ItemID.ZAMORAK_BREW4, "Zamorak brew", Skill.HERBLORE, 78, 175, CriticalItem.TORSTOL_POTION_UNF, ActivitySecondaries.ZAMORAK_BREW),
	SUPER_COMBAT_POTION(ItemID.SUPER_COMBAT_POTION4, "Super combat", Skill.HERBLORE, 90, 150, CriticalItem.TORSTOL, ActivitySecondaries.SUPER_COMBAT_POTION),

	// Cleaning Grimy Herbs
	CLEAN_GUAM(ItemID.GUAM_LEAF, "Clean guam", Skill.HERBLORE, 3, 2.5, CriticalItem.GRIMY_GUAM_LEAF, null),
	CLEAN_MARRENTILL(ItemID.MARRENTILL, "Clean marrentill", Skill.HERBLORE, 5, 3.8, CriticalItem.GRIMY_MARRENTILL, null),
	CLEAN_TARROMIN(ItemID.TARROMIN, "Clean tarromin", Skill.HERBLORE, 11, 5, CriticalItem.GRIMY_TARROMIN, null),
	CLEAN_HARRALANDER(ItemID.HARRALANDER, "Clean harralander", Skill.HERBLORE, 20, 6.3, CriticalItem.GRIMY_HARRALANDER, null),
	CLEAN_RANARR_WEED(ItemID.RANARR_WEED, "Clean ranarr weed", Skill.HERBLORE, 25, 7.5, CriticalItem.GRIMY_RANARR_WEED, null),
	CLEAN_TOADFLAX(ItemID.TOADFLAX, "Clean toadflax", Skill.HERBLORE, 30, 8, CriticalItem.GRIMY_TOADFLAX, null),
	CLEAN_IRIT_LEAF(ItemID.IRIT_LEAF, "Clean irit leaf", Skill.HERBLORE, 40, 8.8, CriticalItem.GRIMY_IRIT_LEAF, null),
	CLEAN_AVANTOE(ItemID.AVANTOE, "Clean avantoe", Skill.HERBLORE, 48, 10, CriticalItem.GRIMY_AVANTOE, null),
	CLEAN_KWUARM(ItemID.KWUARM, "Clean kwuarm", Skill.HERBLORE, 54, 11.3, CriticalItem.GRIMY_KWUARM, null),
	CLEAN_SNAPDRAGON(ItemID.SNAPDRAGON, "Clean snapdragon", Skill.HERBLORE, 59, 11.8, CriticalItem.GRIMY_SNAPDRAGON, null),
	CLEAN_CADANTINE(ItemID.CADANTINE, "Clean cadantine", Skill.HERBLORE, 65, 12.5, CriticalItem.GRIMY_CADANTINE, null),
	CLEAN_LANTADYME(ItemID.LANTADYME, "Clean lantadyme", Skill.HERBLORE, 67, 13.1, CriticalItem.GRIMY_LANTADYME, null),
	CLEAN_DWARF_WEED(ItemID.DWARF_WEED, "Clean dwarf weed", Skill.HERBLORE, 70, 13.8, CriticalItem.GRIMY_DWARF_WEED, null),
	CLEAN_TORSTOL(ItemID.TORSTOL, "Clean torstol", Skill.HERBLORE, 75, 15, CriticalItem.GRIMY_TORSTOL, null),

	/**
	 * Construction Options
	 */
	TEAKS(ItemID.TEAK_PLANK, "Regular Teak Products", Skill.CONSTRUCTION, 1, 90, CriticalItem.TEAK_PLANK, null),
	MYTHCIAL_CAPE(ItemID.MYTHICAL_CAPE, "Mythical Cape Rakes", Skill.CONSTRUCTION, 1, 123.33, CriticalItem.TEAK_PLANK, null);

	private final int icon;
	private final String name;
	private final Skill skill;
	private final int level;
	private final double xp;
	private final SecondaryItem[] secondaries;
	private final CriticalItem criticalItem;

	Activity(int Icon, String name, Skill skill, int level, double xp, CriticalItem criticalItem, ActivitySecondaries secondaries)
	{
		this.icon = Icon;
		this.name = name;
		this.skill = skill;
		this.level = level;
		this.xp = xp;
		this.criticalItem = criticalItem;
		this.secondaries = secondaries == null ? new SecondaryItem[0] : secondaries.getItems();
	}


	// Return the different item categories for this skill
	private static final Map<Skill, ArrayList<Activity>> bySkill = buildSkillMap();

	public static ArrayList<Activity> getBySkill(Skill skill)
	{
		return bySkill.get(skill);
	}

	private static Map<Skill, ArrayList<Activity>> buildSkillMap()
	{
		Map<Skill, ArrayList<Activity>> map = new HashMap<>();
		for (Activity item : values())
		{
			map.computeIfAbsent(item.getSkill(), e -> new ArrayList<Activity>()).add(item);
		}

		return map;
	}
}
