package net.runelite.client.plugins.loottracker;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ItemSortTypes
{
	ALPHABETICAL("Alphabetical"),
	ITEM_ID("Item ID"),
	PRICE("PRICE"),
	VALUE("Value");

	private final String name;

	@Override
	public String toString()
	{
		return name;
	}
}
