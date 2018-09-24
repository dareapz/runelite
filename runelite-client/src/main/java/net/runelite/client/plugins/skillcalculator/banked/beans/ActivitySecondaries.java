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

@Getter
public enum ActivitySecondaries
{
	// Herblore Creations

	// Guam
	ATTACK_POTION(new SecondaryItem(ItemID.EYE_OF_NEWT)),
	GUAM_TAR(new SecondaryItem(ItemID.GUAM_TAR, 15)),
	// Marrentil
	ANTIPOISON(new SecondaryItem(ItemID.UNICORN_HORN_DUST)),
	MARRENTILL_TAR(new SecondaryItem(ItemID.GUAM_TAR, 15)),
	// Tarromin
	STRENGTH_POTION(new SecondaryItem(ItemID.LIMPWURT_ROOT)),
	SERUM_207(new SecondaryItem(ItemID.ASHES)),
	TARROMIN_TAR(new SecondaryItem(ItemID.GUAM_TAR, 15)),
	// Harralander
	COMPOST_POTION(new SecondaryItem(ItemID.VOLCANIC_ASH)),
	RESTORE_POTION(new SecondaryItem(ItemID.RED_SPIDERS_EGGS)),
	ENERGY_POTION(new SecondaryItem(ItemID.CHOCOLATE_DUST)),
	COMBAT_POTION(new SecondaryItem(ItemID.GOAT_HORN_DUST)),
	HARRALANDER_TAR(new SecondaryItem(ItemID.GUAM_TAR, 15)),
	// Ranarr Weed
	DEFENCE_POTION(new SecondaryItem(ItemID.WHITE_BERRIES)),
	PRAYER_POTION(new SecondaryItem(ItemID.SNAPE_GRASS)),
	// Toadflax
	AGILITY_POTION(new SecondaryItem(ItemID.TOADS_LEGS)),
	SARADOMIN_BREW(new SecondaryItem(ItemID.CRUSHED_NEST)),
	// Irit
	SUPER_ATTACK(new SecondaryItem(ItemID.EYE_OF_NEWT)),
	SUPERANTIPOISON(new SecondaryItem(ItemID.UNICORN_HORN_DUST)),
	// Avantoe
	FISHING_POTION(new SecondaryItem(ItemID.SNAPE_GRASS)),
	SUPER_ENERGY_POTION(new SecondaryItem(ItemID.MORT_MYRE_FUNGUS)),
	HUNTER_POTION(new SecondaryItem(ItemID.KEBBIT_TEETH_DUST)),
	// Kwuarm
	SUPER_STRENGTH(new SecondaryItem(ItemID.LIMPWURT_ROOT)),
	// Snapdragon
	SUPER_RESTORE(new SecondaryItem(ItemID.RED_SPIDERS_EGGS)),
	SANFEW_SERUM(new SecondaryItem(ItemID.RED_SPIDERS_EGGS), new SecondaryItem(ItemID.UNICORN_HORN_DUST), new SecondaryItem(ItemID.UNICORN_HORN_DUST), new SecondaryItem(ItemID.NAIL_BEAST_NAILS)),
	// Cadantine
	SUPER_DEFENCE_POTION(new SecondaryItem(ItemID.WHITE_BERRIES)),
	// Lantadyme
	ANTIFIRE_POTION(new SecondaryItem(ItemID.DRAGON_SCALE_DUST)),
	MAGIC_POTION(new SecondaryItem(ItemID.POTATO_CACTUS)),
	// Dwarf Weed
	RANGING_POTION(new SecondaryItem(ItemID.WINE_OF_ZAMORAK)),
	// Torstol
	ZAMORAK_BREW(new SecondaryItem(ItemID.JANGERBERRIES)),
	SUPER_COMBAT_POTION(new SecondaryItem(ItemID.SUPER_ATTACK3), new SecondaryItem(ItemID.SUPER_STRENGTH3), new SecondaryItem(ItemID.SUPER_DEFENCE3));

	private final SecondaryItem[] items;

	ActivitySecondaries(SecondaryItem... items)
	{
		this.items = items;
	}
}
