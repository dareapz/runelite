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
	GUAM_POTION_UNF(ItemID.GUAM_POTION_UNF, "Unfinished Potion", Skill.HERBLORE, 1, 0, CriticalItem.GUAM_LEAF, ActivitySecondaries.UNFINISHED_POTION),
	GUAM_TAR(ItemID.GUAM_TAR, "Guam tar", Skill.HERBLORE, 19, 30, CriticalItem.GUAM_LEAF, ActivitySecondaries.SWAMP_TAR, true),

	ATTACK_POTION(ItemID.ATTACK_POTION4, "Attack Potion", Skill.HERBLORE, 3, 25, CriticalItem.GUAM_LEAF_POTION_UNF, ActivitySecondaries.ATTACK_POTION),
	// Marrentil
	MARRENTILL_POTION_UNF(ItemID.MARRENTILL_POTION_UNF, "Unfinished Potion", Skill.HERBLORE, 1, 0, CriticalItem.MARRENTILL, ActivitySecondaries.UNFINISHED_POTION),
	MARRENTILL_TAR(ItemID.MARRENTILL_TAR, "Marrentill tar", Skill.HERBLORE, 31, 42.5, CriticalItem.MARRENTILL, ActivitySecondaries.SWAMP_TAR, true),

	ANTIPOISON(ItemID.ANTIPOISON4, "Antipoison", Skill.HERBLORE, 5, 37.5, CriticalItem.MARRENTILL_POTION_UNF, ActivitySecondaries.ANTIPOISON),
	// Tarromin
	TARROMIN_POTION_UNF(ItemID.TARROMIN_POTION_UNF, "Unfinished Potion", Skill.HERBLORE, 1, 0, CriticalItem.TARROMIN, ActivitySecondaries.UNFINISHED_POTION),
	TARROMIN_TAR(ItemID.TARROMIN_TAR, "Tarromin tar", Skill.HERBLORE, 39, 55, CriticalItem.TARROMIN, ActivitySecondaries.SWAMP_TAR, true),

	STRENGTH_POTION(ItemID.STRENGTH_POTION4, "Strength potion", Skill.HERBLORE, 12, 50, CriticalItem.TARROMIN_POTION_UNF, ActivitySecondaries.STRENGTH_POTION),
	SERUM_207(ItemID.SERUM_207_4, "Serum 207", Skill.HERBLORE, 15, 50, CriticalItem.TARROMIN_POTION_UNF, ActivitySecondaries.SERUM_207),
	// Harralander
	HARRALANDER_POTION_UNF(ItemID.HARRALANDER_POTION_UNF, "Unfinished Potion", Skill.HERBLORE, 1, 0, CriticalItem.HARRALANDER, ActivitySecondaries.UNFINISHED_POTION),
	HARRALANDER_TAR(ItemID.HARRALANDER_TAR, "Harralander tar", Skill.HERBLORE, 44, 72.5, CriticalItem.HARRALANDER, ActivitySecondaries.SWAMP_TAR, true),

	COMPOST_POTION(ItemID.COMPOST_POTION4, "Compost potion", Skill.HERBLORE, 21, 60, CriticalItem.HARRALANDER_POTION_UNF, ActivitySecondaries.COMPOST_POTION),
	RESTORE_POTION(ItemID.RESTORE_POTION4, "Restore potion", Skill.HERBLORE, 22, 62.5, CriticalItem.HARRALANDER_POTION_UNF, ActivitySecondaries.RESTORE_POTION),
	ENERGY_POTION(ItemID.ENERGY_POTION4, "Energy potion", Skill.HERBLORE, 26, 67.5, CriticalItem.HARRALANDER_POTION_UNF, ActivitySecondaries.ENERGY_POTION),
	COMBAT_POTION(ItemID.COMBAT_POTION4, "Combat potion", Skill.HERBLORE, 36, 84, CriticalItem.HARRALANDER_POTION_UNF, ActivitySecondaries.COMBAT_POTION),
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
	TORSTOL_POTION_UNF(ItemID.TORSTOL_POTION_UNF, "Unfinished Potion", Skill.HERBLORE, 78, 0, CriticalItem.TORSTOL, ActivitySecondaries.UNFINISHED_POTION),
	SUPER_COMBAT_POTION(ItemID.SUPER_COMBAT_POTION4, "Super combat", Skill.HERBLORE, 90, 150, CriticalItem.TORSTOL, ActivitySecondaries.SUPER_COMBAT_POTION, true),
	ANTIVENOM_PLUS(ItemID.ANTIVENOM4_12913, "Anti-venom+", Skill.HERBLORE, 94, 125, CriticalItem.TORSTOL, ActivitySecondaries.ANTIVENOM_PLUS, true),

	ZAMORAK_BREW(ItemID.ZAMORAK_BREW4, "Zamorak brew", Skill.HERBLORE, 78, 175, CriticalItem.TORSTOL_POTION_UNF, ActivitySecondaries.ZAMORAK_BREW),

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
	PLANKS(ItemID.PLANK	, "Normal Plank Products", Skill.CONSTRUCTION, 1, 29, CriticalItem.PLANK, null),
	OAK_PLANKS(ItemID.OAK_PLANK, "Normal Oak Products", Skill.CONSTRUCTION, 1, 60, CriticalItem.OAK_PLANK, null),
	TEAK_PLANKS(ItemID.TEAK_PLANK, "Normal Teak Products", Skill.CONSTRUCTION, 1, 90, CriticalItem.TEAK_PLANK, null),
	MYTHICAL_CAPE(ItemID.MYTHICAL_CAPE, "Mythical Cape Rakes", Skill.CONSTRUCTION, 1, 123.33, CriticalItem.TEAK_PLANK, null),
	MAHOGANY_PLANKS(ItemID.MAHOGANY_PLANK, "Normal Mahogany Products", Skill.CONSTRUCTION, 1, 140, CriticalItem.MAHOGANY_PLANK, null),

	/**
	 * Prayer Options
	 */

	BONES(ItemID.BONES, "Bones", Skill.PRAYER, 1, 4.5, CriticalItem.BONES, null),
	WOLF_BONES(ItemID.WOLF_BONES, "Bones", Skill.PRAYER, 1, 4.5, CriticalItem.WOLF_BONES, null),
	BURNT_BONES(ItemID.BURNT_BONES, "Bones", Skill.PRAYER, 1, 4.5, CriticalItem.BURNT_BONES, null),
	MONKEY_BONES(ItemID.MONKEY_BONES, "Bones", Skill.PRAYER, 1, 5.0, CriticalItem.MONKEY_BONES, null),
	BAT_BONES(ItemID.BAT_BONES, "Bones", Skill.PRAYER, 1, 5.3, CriticalItem.BAT_BONES, null),
	JOGRE_BONES(ItemID.JOGRE_BONES, "Bones", Skill.PRAYER, 1, 15.0, CriticalItem.JOGRE_BONES, null),
	BIG_BONES(ItemID.BIG_BONES, "Bones", Skill.PRAYER, 1, 15.0, CriticalItem.BIG_BONES, null),
	ZOGRE_BONES(ItemID.ZOGRE_BONES, "Bones", Skill.PRAYER, 1, 22.5, CriticalItem.ZOGRE_BONES, null),
	SHAIKAHAN_BONES(ItemID.SHAIKAHAN_BONES, "Bones", Skill.PRAYER, 1, 25.0, CriticalItem.SHAIKAHAN_BONES, null),
	BABYDRAGON_BONES(ItemID.BABYDRAGON_BONES, "Bones", Skill.PRAYER, 1, 30.0, CriticalItem.BABYDRAGON_BONES, null),
	WYVERN_BONES(ItemID.WYVERN_BONES, "Bones", Skill.PRAYER, 1, 72.0, CriticalItem.WYVERN_BONES, null),
	DRAGON_BONES(ItemID.DRAGON_BONES, "Bones", Skill.PRAYER, 1, 72.0, CriticalItem.DRAGON_BONES, null),
	FAYRG_BONES(ItemID.FAYRG_BONES, "Bones", Skill.PRAYER, 1, 84.0, CriticalItem.FAYRG_BONES, null),
	LAVA_DRAGON_BONES(ItemID.LAVA_DRAGON_BONES, "Bones", Skill.PRAYER, 1, 85.0, CriticalItem.LAVA_DRAGON_BONES, null),
	RAURG_BONES(ItemID.RAURG_BONES, "Bones", Skill.PRAYER, 1, 96.0, CriticalItem.RAURG_BONES, null),
	DAGANNOTH_BONES(ItemID.DAGANNOTH_BONES, "Bones", Skill.PRAYER, 1, 125.0, CriticalItem.DAGANNOTH_BONES, null),
	OURG_BONES(ItemID.OURG_BONES, "Bones", Skill.PRAYER, 1, 140.0, CriticalItem.OURG_BONES, null),
	SUPERIOR_DRAGON_BONES(ItemID.SUPERIOR_DRAGON_BONES, "Bones", Skill.PRAYER, 1, 150.0, CriticalItem.SUPERIOR_DRAGON_BONES, null),
	// Shade Remains (Pyre Logs)
	LOAR_REMAINS(ItemID.LOAR_REMAINS, "Shades", Skill.PRAYER, 1, 33.0, CriticalItem.LOAR_REMAINS, null),
	PHRIN_REMAINS(ItemID.PHRIN_REMAINS, "Shades", Skill.PRAYER, 1, 46.5, CriticalItem.PHRIN_REMAINS, null),
	RIYL_REMAINS(ItemID.RIYL_REMAINS, "Shades", Skill.PRAYER, 1, 59.5, CriticalItem.RIYL_REMAINS, null),
	ASYN_REMAINS(ItemID.ASYN_REMAINS, "Shades", Skill.PRAYER, 1, 82.5, CriticalItem.ASYN_REMAINS, null),
	FIYR_REMAINS(ItemID.FIYR_REMAINS, "Shades", Skill.PRAYER, 1, 84.0, CriticalItem.FIYR_REMAINS, null),
	// Ensouled Heads
	ENSOULED_GOBLIN_HEAD(ItemID.ENSOULED_GOBLIN_HEAD_13448, "Ensouled Heads", Skill.PRAYER, 1, 130.0, CriticalItem.ENSOULED_GOBLIN_HEAD, null),
	ENSOULED_MONKEY_HEAD(ItemID.ENSOULED_MONKEY_HEAD_13451, "Ensouled Heads", Skill.PRAYER, 1, 182.0, CriticalItem.ENSOULED_MONKEY_HEAD, null),
	ENSOULED_IMP_HEAD(ItemID.ENSOULED_IMP_HEAD_13454, "Ensouled Heads", Skill.PRAYER, 1, 286.0, CriticalItem.ENSOULED_IMP_HEAD, null),
	ENSOULED_MINOTAUR_HEAD(ItemID.ENSOULED_MINOTAUR_HEAD_13457, "Ensouled Heads", Skill.PRAYER, 1, 364.0, CriticalItem.ENSOULED_MINOTAUR_HEAD, null),
	ENSOULED_SCORPION_HEAD(ItemID.ENSOULED_SCORPION_HEAD_13460, "Ensouled Heads", Skill.PRAYER, 1, 454.0, CriticalItem.ENSOULED_SCORPION_HEAD, null),
	ENSOULED_BEAR_HEAD(ItemID.ENSOULED_BEAR_HEAD_13463, "Ensouled Heads", Skill.PRAYER, 1, 480.0, CriticalItem.ENSOULED_BEAR_HEAD, null),
	ENSOULED_UNICORN_HEAD(ItemID.ENSOULED_UNICORN_HEAD_13466, "Ensouled Heads", Skill.PRAYER, 1, 494.0, CriticalItem.ENSOULED_UNICORN_HEAD, null),
	ENSOULED_DOG_HEAD(ItemID.ENSOULED_DOG_HEAD_13469, "Ensouled Heads", Skill.PRAYER, 1, 520.0, CriticalItem.ENSOULED_DOG_HEAD, null),
	ENSOULED_CHAOS_DRUID_HEAD(ItemID.ENSOULED_CHAOS_DRUID_HEAD_13472, "Ensouled Heads", Skill.PRAYER, 1, 584.0, CriticalItem.ENSOULED_CHAOS_DRUID_HEAD, null),
	ENSOULED_GIANT_HEAD(ItemID.ENSOULED_GIANT_HEAD_13475, "Ensouled Heads", Skill.PRAYER, 1, 650.0, CriticalItem.ENSOULED_GIANT_HEAD, null),
	ENSOULED_OGRE_HEAD(ItemID.ENSOULED_OGRE_HEAD_13478, "Ensouled Heads", Skill.PRAYER, 1, 716.0, CriticalItem.ENSOULED_OGRE_HEAD, null),
	ENSOULED_ELF_HEAD(ItemID.ENSOULED_ELF_HEAD_13481, "Ensouled Heads", Skill.PRAYER, 1, 754.0, CriticalItem.ENSOULED_ELF_HEAD, null),
	ENSOULED_TROLL_HEAD(ItemID.ENSOULED_TROLL_HEAD_13484, "Ensouled Heads", Skill.PRAYER, 1, 780.0, CriticalItem.ENSOULED_TROLL_HEAD, null),
	ENSOULED_HORROR_HEAD(ItemID.ENSOULED_HORROR_HEAD_13487, "Ensouled Heads", Skill.PRAYER, 1, 832.0, CriticalItem.ENSOULED_HORROR_HEAD, null),
	ENSOULED_KALPHITE_HEAD(ItemID.ENSOULED_KALPHITE_HEAD_13490, "Ensouled Heads", Skill.PRAYER, 1, 884.0, CriticalItem.ENSOULED_KALPHITE_HEAD, null),
	ENSOULED_DAGANNOTH_HEAD(ItemID.ENSOULED_DAGANNOTH_HEAD_13493, "Ensouled Heads", Skill.PRAYER, 1, 936.0, CriticalItem.ENSOULED_DAGANNOTH_HEAD, null),
	ENSOULED_BLOODVELD_HEAD(ItemID.ENSOULED_BLOODVELD_HEAD_13496, "Ensouled Heads", Skill.PRAYER, 1, 1040.0, CriticalItem.ENSOULED_BLOODVELD_HEAD, null),
	ENSOULED_TZHAAR_HEAD(ItemID.ENSOULED_TZHAAR_HEAD_13499, "Ensouled Heads", Skill.PRAYER, 1, 1104.0, CriticalItem.ENSOULED_TZHAAR_HEAD, null),
	ENSOULED_DEMON_HEAD(ItemID.ENSOULED_DEMON_HEAD_13502, "Ensouled Heads", Skill.PRAYER, 1, 1170.0, CriticalItem.ENSOULED_DEMON_HEAD, null),
	ENSOULED_AVIANSIE_HEAD(ItemID.ENSOULED_AVIANSIE_HEAD_13505, "Ensouled Heads", Skill.PRAYER, 1, 1234.0, CriticalItem.ENSOULED_AVIANSIE_HEAD, null),
	ENSOULED_ABYSSAL_HEAD(ItemID.ENSOULED_ABYSSAL_HEAD_13508, "Ensouled Heads", Skill.PRAYER, 1, 1300.0, CriticalItem.ENSOULED_ABYSSAL_HEAD, null),
	ENSOULED_DRAGON_HEAD(ItemID.ENSOULED_DRAGON_HEAD_13511, "Ensouled Heads", Skill.PRAYER, 1, 1560.0, CriticalItem.ENSOULED_DRAGON_HEAD, null),


	/*
	 * Cooking Items
	 */
	RAW_HERRING(ItemID.RAW_HERRING, "Fish", Skill.COOKING, 5, 50.0, CriticalItem.RAW_HERRING, null),
	RAW_MACKEREL(ItemID.RAW_MACKEREL, "Fish", Skill.COOKING, 10, 60.0, CriticalItem.RAW_MACKEREL, null),
	RAW_TROUT(ItemID.RAW_TROUT, "Fish", Skill.COOKING, 15, 70.0, CriticalItem.RAW_TROUT, null),
	RAW_COD(ItemID.RAW_COD, "Fish", Skill.COOKING, 18, 75.0, CriticalItem.RAW_COD, null),
	RAW_PIKE(ItemID.RAW_PIKE, "Fish", Skill.COOKING, 20, 80.0, CriticalItem.RAW_PIKE, null),
	RAW_SALMON(ItemID.RAW_SALMON, "Fish", Skill.COOKING, 25, 90.0, CriticalItem.RAW_SALMON, null),
	RAW_TUNA(ItemID.RAW_TUNA, "Fish", Skill.COOKING, 30, 100.0, CriticalItem.RAW_TUNA, null),
	RAW_KARAMBWAN(ItemID.RAW_KARAMBWAN, "Fish", Skill.COOKING, 30, 190.0, CriticalItem.RAW_KARAMBWAN, null),
	RAW_LOBSTER(ItemID.RAW_LOBSTER, "Fish", Skill.COOKING, 40, 120.0, CriticalItem.RAW_LOBSTER, null),
	RAW_BASS(ItemID.RAW_BASS, "Fish", Skill.COOKING, 43, 130.0, CriticalItem.RAW_BASS, null),
	RAW_SWORDFISH(ItemID.RAW_SWORDFISH, "Fish", Skill.COOKING, 45, 140.0, CriticalItem.RAW_SWORDFISH, null),
	RAW_MONKFISH(ItemID.RAW_MONKFISH, "Fish", Skill.COOKING, 62, 150.0, CriticalItem.RAW_MONKFISH, null),
	RAW_SHARK(ItemID.RAW_SHARK, "Fish", Skill.COOKING, 80, 210.0, CriticalItem.RAW_SHARK, null),
	RAW_SEA_TURTLE(ItemID.RAW_SEA_TURTLE, "Fish", Skill.COOKING, 82, 211.3, CriticalItem.RAW_SEA_TURTLE, null),
	RAW_ANGLERFISH(ItemID.RAW_ANGLERFISH, "Fish", Skill.COOKING, 84, 230.0, CriticalItem.RAW_ANGLERFISH, null),
	RAW_DARK_CRAB(ItemID.RAW_DARK_CRAB, "Fish", Skill.COOKING, 90, 215.0, CriticalItem.RAW_DARK_CRAB, null),
	RAW_MANTA_RAY(ItemID.RAW_MANTA_RAY, "Fish", Skill.COOKING, 91, 216.2, CriticalItem.RAW_MANTA_RAY, null),

	/*
	 * Crafting Items
	 */
	// D'hide/Dragon Leather
	GREEN_DRAGON_LEATHER(ItemID.GREEN_DRAGON_LEATHER, "D'hide", Skill.CRAFTING, 57, 62.0, CriticalItem.GREEN_DRAGON_LEATHER, null),
	BLUE_DRAGON_LEATHER(ItemID.BLUE_DRAGON_LEATHER, "D'hide", Skill.CRAFTING, 66, 70.0, CriticalItem.BLUE_DRAGON_LEATHER, null),
	RED_DRAGON_LEATHER(ItemID.RED_DRAGON_LEATHER, "D'hide", Skill.CRAFTING, 73, 78.0, CriticalItem.RED_DRAGON_LEATHER, null),
	BLACK_DRAGON_LEATHER(ItemID.BLACK_DRAGON_LEATHER, "D'hide", Skill.CRAFTING, 79, 86.0, CriticalItem.BLACK_DRAGON_LEATHER, null),
	// Uncut Gems
	UNCUT_OPAL(ItemID.UNCUT_OPAL, "Gems", Skill.CRAFTING, 1, 15.0, CriticalItem.UNCUT_OPAL, null),
	UNCUT_JADE(ItemID.UNCUT_JADE, "Gems", Skill.CRAFTING, 13, 20.0, CriticalItem.UNCUT_JADE, null),
	UNCUT_RED_TOPAZ(ItemID.UNCUT_RED_TOPAZ, "Gems", Skill.CRAFTING, 16, 25.0, CriticalItem.UNCUT_RED_TOPAZ, null),
	UNCUT_SAPPHIRE(ItemID.UNCUT_SAPPHIRE, "Gems", Skill.CRAFTING, 20, 50.0, CriticalItem.UNCUT_SAPPHIRE, null),
	UNCUT_EMERALD(ItemID.UNCUT_EMERALD, "Gems", Skill.CRAFTING, 27, 67.5, CriticalItem.UNCUT_EMERALD, null),
	UNCUT_RUBY(ItemID.UNCUT_RUBY, "Gems", Skill.CRAFTING, 34, 85, CriticalItem.UNCUT_RUBY, null),
	UNCUT_DIAMOND(ItemID.UNCUT_DIAMOND, "Gems", Skill.CRAFTING, 43, 107.5, CriticalItem.UNCUT_DIAMOND, null),
	UNCUT_DRAGONSTONE(ItemID.UNCUT_DRAGONSTONE, "Gems", Skill.CRAFTING, 55, 137.5, CriticalItem.UNCUT_DRAGONSTONE, null),
	UNCUT_ONYX(ItemID.UNCUT_ONYX, "Gems", Skill.CRAFTING, 67, 167.5, CriticalItem.UNCUT_ONYX, null),
	UNCUT_ZENYTE(ItemID.UNCUT_ZENYTE, "Gems", Skill.CRAFTING, 89, 200.0, CriticalItem.UNCUT_ZENYTE, null),

	/*
	 * Smithing Items
	 */

	// Smelting ores (Furnace)
	IRON_ORE(ItemID.IRON_BAR, "Iron Bars", Skill.SMITHING, 15, 12.5, CriticalItem.IRON_ORE, ActivitySecondaries.COAL_ORE),
	STEEL_ORE(ItemID.STEEL_BAR, "Steel Bars", Skill.SMITHING, 30, 17.5, CriticalItem.IRON_ORE, ActivitySecondaries.COAL_ORE_2),
	SILVER_ORE(ItemID.SILVER_ORE, "Bar", Skill.SMITHING, 20, 13.67, CriticalItem.SILVER_ORE, null),
	GOLD_ORE(ItemID.GOLD_BAR, "Regular exp", Skill.SMITHING, 40, 22.5, CriticalItem.GOLD_ORE, null),
	GOLD_ORE_GAUNTLETS(ItemID.GOLDSMITH_GAUNTLETS, "Goldsmith Gauntlets", Skill.SMITHING, 40, 56.2, CriticalItem.GOLD_ORE, null),
	MITHRIL_ORE(ItemID.IRON_ORE, "Bar", Skill.SMITHING, 50, 30, CriticalItem.MITHRIL_ORE, ActivitySecondaries.COAL_ORE_4),
	ADAMANTITE_ORE(ItemID.IRON_ORE, "Bar", Skill.SMITHING, 70, 37.5, CriticalItem.ADAMANTITE_ORE, ActivitySecondaries.COAL_ORE_6),
	RUNITE_ORE(ItemID.IRON_ORE, "Bar", Skill.SMITHING, 85, 50, CriticalItem.RUNITE_ORE, ActivitySecondaries.COAL_ORE_8),

	// Smelting bars (Anvil)
	BRONZE_BAR(ItemID.BRONZE_BAR, "Bars", Skill.SMITHING, 1, 12.5, CriticalItem.BRONZE_BAR, null),
	IRON_BAR(ItemID.IRON_BAR, "Bars", Skill.SMITHING, 15, 25.0, CriticalItem.IRON_BAR, null),
	STEEL_BAR(ItemID.STEEL_BAR, "Steel Products", Skill.SMITHING, 30, 37.5, CriticalItem.STEEL_BAR, null),
	CANNONBALLS(ItemID.CANNONBALL, "Cannonballs", Skill.SMITHING, 35, 25.5, CriticalItem.STEEL_BAR, null),
	MITHRIL_BAR(ItemID.MITHRIL_BAR, "Bars", Skill.SMITHING, 50, 50.0, CriticalItem.MITHRIL_BAR, null),
	ADAMANTITE_BAR(ItemID.ADAMANTITE_BAR, "Bars", Skill.SMITHING, 70, 62.5, CriticalItem.ADAMANTITE_BAR, null),
	RUNITE_BAR(ItemID.RUNITE_BAR, "Bars", Skill.SMITHING, 85, 75.0, CriticalItem.RUNITE_BAR, null)

		;

	private final int icon;
	private final String name;
	private final Skill skill;
	private final int level;
	private final double xp;
	private final SecondaryItem[] secondaries;
	private final CriticalItem criticalItem;
	private final boolean preventLinked;

	Activity(int Icon, String name, Skill skill, int level, double xp, CriticalItem criticalItem, ActivitySecondaries secondaries)
	{
		this.icon = Icon;
		this.name = name;
		this.skill = skill;
		this.level = level;
		this.xp = xp;
		this.criticalItem = criticalItem;
		this.secondaries = secondaries == null ? new SecondaryItem[0] : secondaries.getItems();
		this.preventLinked = false;
	}

	Activity(int Icon, String name, Skill skill, int level, double xp, CriticalItem criticalItem, ActivitySecondaries secondaries, boolean preventLinked)
	{
		this.icon = Icon;
		this.name = name;
		this.skill = skill;
		this.level = level;
		this.xp = xp;
		this.criticalItem = criticalItem;
		this.secondaries = secondaries == null ? new SecondaryItem[0] : secondaries.getItems();
		this.preventLinked = preventLinked;
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

	// Return the different item categories for this skill
	private static final Map<CriticalItem, ArrayList<Activity>> byCriticalItem = buildItemMap();

	public static ArrayList<Activity> getByCriticalItem(CriticalItem item)
	{
		return byCriticalItem.getOrDefault(item, new ArrayList<>());
	}

	private static Map<CriticalItem, ArrayList<Activity>> buildItemMap()
	{
		Map<CriticalItem, ArrayList<Activity>> map = new HashMap<>();
		for (Activity item : values())
		{
			map.computeIfAbsent(item.getCriticalItem(), e -> new ArrayList<Activity>()).add(item);
		}

		return map;
	}

	public static ArrayList<Activity> getByCriticalItem(CriticalItem item, int limitLevel)
	{
		ArrayList<Activity> activities = new ArrayList<>();

		// -1/0 Turns off limits
		if (limitLevel <= 0)
		{
			limitLevel = 99;
		}

		for (Activity a : getByCriticalItem(item))
		{
			if (a.getLevel() <= limitLevel)
			{
				activities.add(a);
			}
		}

		return activities;
	}
}
