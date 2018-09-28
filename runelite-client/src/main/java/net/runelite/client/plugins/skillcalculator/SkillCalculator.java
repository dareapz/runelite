/*
 * Copyright (c) 2018, Kruithne <kruithne@gmail.com>
 * Copyright (c) 2018, Psikoi <https://github.com/psikoi>
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
package net.runelite.client.plugins.skillcalculator;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.Experience;
import net.runelite.api.ItemID;
import net.runelite.api.Skill;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.plugins.skillcalculator.banked.CriticalItem;
import net.runelite.client.plugins.skillcalculator.banked.beans.Activity;
import net.runelite.client.plugins.skillcalculator.banked.ui.ItemPanel;
import net.runelite.client.plugins.skillcalculator.beans.SkillData;
import net.runelite.client.plugins.skillcalculator.beans.SkillDataBonus;
import net.runelite.client.plugins.skillcalculator.beans.SkillDataEntry;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.DynamicGridLayout;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.ui.components.IconTextField;

@Slf4j
public class SkillCalculator extends JPanel
{
	private static final int MAX_XP = 200_000_000;
	private static final DecimalFormat XP_FORMAT = new DecimalFormat("#.#");
	private static final DecimalFormat XP_FORMAT_COMMA = new DecimalFormat("#,###.#");
	private static final Pattern NUMBER_PATTERN = Pattern.compile("([0-9]+)");

	private final UICalculatorInputArea uiInput;
	private final Client client;
	private final SpriteManager spriteManager;
	private final ItemManager itemManager;
	private final List<UIActionSlot> uiActionSlots = new ArrayList<>();
	private final CacheSkillData cacheSkillData = new CacheSkillData();
	private final UICombinedActionSlot combinedActionSlot;
	private final ArrayList<UIActionSlot> combinedActionSlots = new ArrayList<>();
	private final List<JCheckBox> bonusCheckBoxes = new ArrayList<>();
	private final IconTextField searchBar = new IconTextField();

	private SkillData skillData;
	private int currentLevel = 1;
	private int currentXP = Experience.getXpForLevel(currentLevel);
	private int targetLevel = currentLevel + 1;
	private int targetXP = Experience.getXpForLevel(targetLevel);
	private float xpFactor = 1.0f;
	private String currentTab;
	private Skill skill;

	// Banked Experience Variables
	private Map<Integer, Integer> bankMap = new HashMap<>();
	private Map<String, Boolean> categoryMap = new HashMap<>();
	private double totalBankedXp = 0.0f;
	private JLabel totalLabel = new JLabel();
	private JPanel detailConfigContainer;
	private JPanel detailContainer;
	// Ignore item ids for banked experience
	private Map<Integer, Boolean> ignoreMap = new HashMap<>();
	// Activity Magic
	private Map<CriticalItem, Integer> criticalMap = new HashMap<>();
	private Map<CriticalItem, List<Activity>> activityMap = new HashMap<>();
	private Map<CriticalItem, Activity> indexMap = new HashMap<>();
	private Map<CriticalItem, Integer> linkedMap = new HashMap<>();

	SkillCalculator(Client client, UICalculatorInputArea uiInput, SpriteManager spriteManager, ItemManager itemManager)
	{
		this.client = client;
		this.uiInput = uiInput;
		this.spriteManager = spriteManager;
		this.itemManager = itemManager;

		combinedActionSlot = new UICombinedActionSlot(spriteManager);

		searchBar.setIcon(IconTextField.Icon.SEARCH);
		searchBar.setPreferredSize(new Dimension(PluginPanel.PANEL_WIDTH - 20, 30));
		searchBar.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		searchBar.setHoverBackgroundColor(ColorScheme.DARK_GRAY_HOVER_COLOR);
		searchBar.addKeyListener(e -> onSearch());

		setLayout(new DynamicGridLayout(0, 1, 0, 5));

		// Register listeners on the input fields and then move on to the next related text field
		uiInput.getUiFieldCurrentLevel().addActionListener(e ->
		{
			onFieldCurrentLevelUpdated();
			uiInput.getUiFieldTargetLevel().requestFocusInWindow();
		});

		uiInput.getUiFieldCurrentXP().addActionListener(e ->
		{
			onFieldCurrentXPUpdated();
			uiInput.getUiFieldTargetXP().requestFocusInWindow();
		});

		uiInput.getUiFieldTargetLevel().addActionListener(e -> onFieldTargetLevelUpdated());
		uiInput.getUiFieldTargetXP().addActionListener(e -> onFieldTargetXPUpdated());

		detailContainer = new JPanel();
		detailContainer.setLayout(new BoxLayout(detailContainer, BoxLayout.Y_AXIS));

		detailConfigContainer = new JPanel();
		detailConfigContainer.setLayout(new BoxLayout(detailConfigContainer, BoxLayout.Y_AXIS));
	}


	// Opens the Calculator tab for the current Skill
	void openCalculator(CalculatorType calculatorType)
	{
		currentTab = "Calculator";

		// clean slate for creating the required panel
		removeAll();
		updateData(calculatorType);

		// Add in checkboxes for available skill bonuses.
		renderBonusOptions();

		// Add the combined action slot.
		add(combinedActionSlot);

		// Add the search bar
		add(searchBar);

		// Create action slots for the skill actions.
		renderActionSlots();

		// Update the input fields.
		updateInputFields();
	}

	private void updateCombinedAction()
	{
		int size = combinedActionSlots.size();
		if (size > 1)
		{
			combinedActionSlot.setTitle(size + " actions selected");
		}
		else if (size == 1)
		{
			combinedActionSlot.setTitle("1 action selected");
		}
		else
		{
			combinedActionSlot.setTitle("No action selected");
			combinedActionSlot.setText("Shift-click to select multiple");
			return;
		}

		int actionCount = 0;
		int neededXP = targetXP - currentXP;
		double xp = 0;

		for (UIActionSlot slot : combinedActionSlots)
		{
			xp += slot.getValue();
		}

		if (neededXP > 0)
		{
			assert xp != 0;
			actionCount = (int) Math.ceil(neededXP / xp);
		}

		combinedActionSlot.setText(formatXPActionString(xp, actionCount, "exp - "));
	}

	private void clearCombinedSlots()
	{
		for (UIActionSlot slot : combinedActionSlots)
		{
			slot.setSelected(false);
		}

		combinedActionSlots.clear();
	}

	private void renderBonusOptions()
	{
		if (skillData.getBonuses() != null)
		{
			for (SkillDataBonus bonus : skillData.getBonuses())
			{
				JPanel checkboxPanel = buildCheckboxPanel(bonus);

				add(checkboxPanel);
				add(Box.createRigidArea(new Dimension(0, 5)));
			}
		}
	}

	private JPanel buildCheckboxPanel(SkillDataBonus bonus)
	{
		JPanel uiOption = new JPanel(new BorderLayout());
		JLabel uiLabel = new JLabel(bonus.getName());
		JCheckBox uiCheckbox = new JCheckBox();

		uiLabel.setForeground(Color.WHITE);
		uiLabel.setFont(FontManager.getRunescapeSmallFont());

		uiOption.setBorder(BorderFactory.createEmptyBorder(3, 7, 3, 0));
		uiOption.setBackground(ColorScheme.DARKER_GRAY_COLOR);

		// Adjust XP bonus depending on check-state of the boxes.
		uiCheckbox.addActionListener(event -> adjustCheckboxes(uiCheckbox, bonus));

		uiCheckbox.setBackground(ColorScheme.MEDIUM_GRAY_COLOR);

		uiOption.add(uiLabel, BorderLayout.WEST);
		uiOption.add(uiCheckbox, BorderLayout.EAST);
		bonusCheckBoxes.add(uiCheckbox);

		return uiOption;
	}

	private void adjustCheckboxes(JCheckBox target, SkillDataBonus bonus)
	{
		adjustXPBonus(0);
		bonusCheckBoxes.forEach(otherSelectedCheckbox ->
		{
			if (otherSelectedCheckbox != target)
			{
				otherSelectedCheckbox.setSelected(false);
			}
		});

		if (target.isSelected())
		{
			adjustXPBonus(bonus.getValue());
		}
	}

	private void renderActionSlots()
	{
		// Wipe the list of references to the slot components.
		uiActionSlots.clear();

		// Create new components for the action slots.
		for (SkillDataEntry action : skillData.getActions())
		{
			JLabel uiIcon = new JLabel();

			if (action.getIcon() != null)
			{
				itemManager.getImage(action.getIcon()).addTo(uiIcon);
			}
			else if (action.getSprite() != null)
			{
				spriteManager.addSpriteTo(uiIcon, action.getSprite(), 0);
			}

			UIActionSlot slot = new UIActionSlot(action, uiIcon);
			uiActionSlots.add(slot); // Keep our own reference.
			add(slot); // Add component to the panel.

			slot.addMouseListener(new MouseAdapter()
			{
				@Override
				public void mousePressed(MouseEvent e)
				{
					if (!e.isShiftDown())
					{
						clearCombinedSlots();
					}

					if (slot.isSelected())
					{
						combinedActionSlots.remove(slot);
					}
					else
					{
						combinedActionSlots.add(slot);
					}

					slot.setSelected(!slot.isSelected());
					updateCombinedAction();
				}
			});
		}

		// Refresh the rendering of this panel.
		revalidate();
		repaint();
	}

	private void calculate()
	{
		for (UIActionSlot slot : uiActionSlots)
		{
			int actionCount = 0;
			int neededXP = targetXP - currentXP;
			SkillDataEntry action = slot.getAction();
			double xp = (action.isIgnoreBonus()) ? action.getXp() : action.getXp() * xpFactor;

			if (neededXP > 0)
			{
				actionCount = (int) Math.ceil(neededXP / xp);
			}

			slot.setText("Lvl. " + action.getLevel() + " (" + formatXPActionString(xp, actionCount, "exp) - "));
			slot.setAvailable(currentLevel >= action.getLevel());
			slot.setOverlapping(action.getLevel() < targetLevel);
			slot.setValue(xp);
		}

		updateCombinedAction();
	}

	private String formatXPActionString(double xp, int actionCount, String expExpression)
	{
		return XP_FORMAT.format(xp) + expExpression + NumberFormat.getIntegerInstance().format(actionCount) + (actionCount > 1 ? " actions" : " action");
	}

	private void updateInputFields()
	{
		if (targetXP < currentXP)
		{
			targetLevel = enforceSkillBounds(currentLevel + 1);
			targetXP = Experience.getXpForLevel(targetLevel);
		}

		syncInputFields();
	}

	private void syncInputFields()
	{
		uiInput.setCurrentLevelInput(currentLevel);
		uiInput.setCurrentXPInput(currentXP);
		uiInput.setTargetLevelInput(targetLevel);
		uiInput.setTargetXPInput(targetXP);

		// Can only edit input fields when on Calculator tab
		if (currentTab.equals("Calculator"))
		{
			calculate();
		}
	}

	private void adjustXPBonus(float value)
	{
		xpFactor = 1f + value;
		switch (currentTab)
		{
			case "Calculator":
				calculate();
				break;
			case "Banked Xp":
				calculateBankedExpTotal();
				refreshBankedExpDetails();
				break;
		}
	}

	private void adjustBankedXp(boolean removeBonus, String category)
	{
		categoryMap.put(category, removeBonus);
		calculateBankedExpTotal();
		refreshBankedExpDetails();
	}

	private void onFieldCurrentLevelUpdated()
	{
		currentLevel = enforceSkillBounds(uiInput.getCurrentLevelInput());
		currentXP = Experience.getXpForLevel(currentLevel);
		updateInputFields();
	}

	private void onFieldCurrentXPUpdated()
	{
		currentXP = enforceXPBounds(uiInput.getCurrentXPInput());
		currentLevel = Experience.getLevelForXp(currentXP);
		updateInputFields();
	}

	private void onFieldTargetLevelUpdated()
	{
		targetLevel = enforceSkillBounds(uiInput.getTargetLevelInput());
		targetXP = Experience.getXpForLevel(targetLevel);
		updateInputFields();
	}

	private void onFieldTargetXPUpdated()
	{
		targetXP = enforceXPBounds(uiInput.getTargetXPInput());
		targetLevel = Experience.getLevelForXp(targetXP);
		updateInputFields();
	}

	private static int enforceSkillBounds(int input)
	{
		return Math.min(Experience.MAX_VIRT_LEVEL, Math.max(1, input));
	}

	private static int enforceXPBounds(int input)
	{
		return Math.min(MAX_XP, Math.max(0, input));
	}

	private void onSearch()
	{
		//only show slots that match our search text
		uiActionSlots.forEach(slot ->
		{
			if (slotContainsText(slot, searchBar.getText()))
			{
				super.add(slot);
			}
			else
			{
				super.remove(slot);
			}

			revalidate();
		});
	}

	private boolean slotContainsText(UIActionSlot slot, String text)
	{
		return slot.getAction().getName().toLowerCase().contains(text.toLowerCase());
	}

	private void reset()
	{
		ignoreMap.clear();
		categoryMap.clear();
		criticalMap.clear();
		activityMap.clear();
		indexMap.clear();
		linkedMap.clear();
	}






















	// Bank map changed so recreate the banked xp panel if they are currently viewing it
	void setBankMap(Map<Integer, Integer> map)
	{
		boolean oldMapFlag = (bankMap.size() <= 0);
		bankMap = map;

		if (currentTab.equals("Banked Xp"))
		{
			// Refresh entire panel if old map was empty
			if (oldMapFlag)
			{
				CalculatorType calc = CalculatorType.getBySkill(skill);
				SwingUtilities.invokeLater(() -> openBanked(calc));
				return;
			}

			// Otherwise just update the Total XP banked and the details panel
			SwingUtilities.invokeLater(this::calculateBankedExpTotal);
			SwingUtilities.invokeLater(this::refreshBankedExpDetails);
		}
	}

	// Update UI panel for new skill data
	private void updateData(CalculatorType calculatorType)
	{
		reset();

		// Load the skill data.
		skillData = cacheSkillData.getSkillData(calculatorType.getDataFile());

		// Store the current skill
		skill = calculatorType.getSkill();
		//bankMap = plugin.getBankMap();

		// Reset the XP factor, removing bonuses.
		xpFactor = 1.0f;
		totalBankedXp = 0.0f;

		// Update internal skill/XP values.
		currentXP = client.getSkillExperience(skill);
		currentLevel = Experience.getLevelForXp(currentXP);
		targetLevel = enforceSkillBounds(currentLevel + 1);
		targetXP = Experience.getXpForLevel(targetLevel);

		if (currentTab.equals("Banked Xp"))
		{
			uiInput.getUiFieldTargetLevel().setEditable(false);
			uiInput.getUiFieldTargetXP().setEditable(false);
		}
		else
		{
			uiInput.getUiFieldTargetLevel().setEditable(true);
			uiInput.getUiFieldTargetXP().setEditable(true);
		}

	}

	// Critical Item Activity Selected
	public void activitySelected(CriticalItem i, Activity a)
	{
		indexMap.put(i, a);

		// Total banked experience
		calculateBankedExpTotal();

		// Create the banked experience details container
		refreshBankedExpDetails();
	}

	// Ignore an item in banked xp
	private void ignoreItemID(int id)
	{
		ignoreMap.put(id, true);

		// Update bonus experience calculations
		calculateBankedExpTotal();
		refreshBankedExpDetails();
	}












































































	// Opens the Banked XP tab for the current Skill
	void openBanked(CalculatorType calculatorType)
	{
		currentTab = "Banked Xp";

		// clean slate for creating the required panel
		removeAll();
		updateData(calculatorType);

		// Only adds Banked Experience portion if enabled for this SkillCalc and have seen their bank
		if (!calculatorType.isBankedXpFlag())
		{
			add(new JLabel("<html><div style='text-align: center;'>Banked Experience is not enabled for this skill.</div></html>", JLabel.CENTER));
			revalidate();
			repaint();
		}
		else if (bankMap.size() <= 0)
		{
			// Test Data
			/*
			bankMap.put(ItemID.TORSTOL, 25);
			bankMap.put(ItemID.TORSTOL_POTION_UNF, 50);
			bankMap.put(ItemID.GRIMY_TOADFLAX, 100);
			bankMap.put(ItemID.TOADFLAX, 100);
			bankMap.put(ItemID.TOADFLAX_POTION_UNF, 100);
			bankMap.put(ItemID.GRIMY_MARRENTILL, 100);
			bankMap.put(ItemID.MARRENTILL, 100);
			bankMap.put(ItemID.MARRENTILL_POTION_UNF, 100);
			 */
			bankMap.put(ItemID.GRIMY_TOADFLAX, 100);
			bankMap.put(ItemID.TOADFLAX, 100);
			bankMap.put(ItemID.TOADFLAX_POTION_UNF, 100);
			openBanked(calculatorType);
			return;
			//add(new JLabel( "Please visit a bank!", JLabel.CENTER));
			//revalidate();
			//repaint();
		}
		else
		{
			banked();

			// Now we can actually show the Banked Experience Panel
			// Adds Config Options for this panel
			renderBankedExpOptions();

			// Adds in checkboxes for available skill bon uses, same as Skill Calc
			renderBonusOptions();

			// Total banked experience
			calculateBankedExpTotal();

			// Create the banked experience details container
			detailContainer.removeAll();
			refreshBankedExpDetails();

			add(detailConfigContainer);
			add(totalLabel);
			add(detailContainer);
		}

		// Update the input fields.
		syncInputFields();
	}

	// Adds the Configuration checkboxes to the panel
	private void renderBankedExpOptions()
	{
		Set<String> categories = CriticalItem.getSkillCategories(skill);
		if (categories == null)
			return;

		add(new JLabel("Banked Experience Configuration:"));

		for (String category : categories)
		{
			JPanel uiOption = new JPanel(new BorderLayout());
			JLabel uiLabel = new JLabel(category);
			JCheckBox uiCheckbox = new JCheckBox();

			uiLabel.setForeground(Color.WHITE);
			uiLabel.setFont(FontManager.getRunescapeSmallFont());

			uiOption.setBorder(BorderFactory.createEmptyBorder(3, 7, 3, 0));
			uiOption.setBackground(ColorScheme.DARKER_GRAY_COLOR);

			// Everything is enabled by default
			uiCheckbox.setSelected(true);
			categoryMap.put(category, true);

			// Adjust Total Banked XP check-state of the box.
			uiCheckbox.addActionListener(e -> adjustBankedXp(uiCheckbox.isSelected(), category));
			uiCheckbox.setBackground(ColorScheme.MEDIUM_GRAY_COLOR);

			uiOption.add(uiLabel, BorderLayout.WEST);
			uiOption.add(uiCheckbox, BorderLayout.EAST);

			add(uiOption);
			add(Box.createRigidArea(new Dimension(0, 5)));
		}
	}

	// Calculate the total banked experience and display it in the panel
	private void calculateBankedExpTotal()
	{
		if (!currentTab.equals("Banked Xp"))
			return;

		totalBankedXp = calculateBankedTotal();

		totalLabel.setText("Banked Exp: " + XP_FORMAT_COMMA.format(totalBankedXp));

		// Update Target XP & Level to include total banked xp
		targetXP = (int) (currentXP + totalBankedXp);
		targetLevel = Experience.getLevelForXp(targetXP);
		syncInputFields();

		revalidate();
		repaint();
	}

	// Calculates Total Banked XP for this Skill
	private double calculateBankedTotal()
	{
		double total = 0;
		for (Map.Entry<CriticalItem, Integer> e : criticalMap.entrySet())
		{
			CriticalItem i = e.getKey();
			if (ignoreMap.containsKey(i.getItemID()))
			{
				continue;
			}

			// Ignore certain categories.
			boolean flag = categoryMap.get(i.getCategory());
			if (!flag)
			{
				continue;
			}

			if (e.getValue() > 0)
			{
				total += e.getValue() * getItemXpRate(i);
			}
		}

		return total;
	}












































	private void banked()
	{
		// Grab all CriticalItems for this skill
		ArrayList<CriticalItem> items = CriticalItem.getBySkillName(skill);

		// Loop over all Critical Items for this skill and determine how many are in the bank
		for (CriticalItem item : items)
		{
			Integer qty = bankMap.get(item.getItemID());
			if (qty != null && qty > 0)
			{
				if (criticalMap.containsKey(item))
				{
					criticalMap.put(item, criticalMap.get(item) + qty);
				}
				else
				{
					criticalMap.put(item, qty);
				}

				if (!activityMap.containsKey(item))
				{
					ArrayList<Activity> x = Activity.getByCriticalItem(item);
					if (x != null)
					{
						activityMap.put(item, x);
					}
				}

				// Convert all Critical Items to their final form.
				if (item.getLinkedItemId() != -1)
				{
					finalForm(item);
				}
			}
		}
		log.info("Critical Map: {}", criticalMap);
		log.info("Activity Map: {}", activityMap);
		log.info("Linked Map: {}", linkedMap);
	}

	//
	private void finalForm(CriticalItem item)
	{
		CriticalItem i = CriticalItem.getByItemId(item.getLinkedItemId());
		if (i != null)
		{

			int id = item.getItemID();
			Integer qty = bankMap.get(id);
			linkedMap.put(i, id);

			// Update quantity in critical map
			if (criticalMap.containsKey(i))
			{
				criticalMap.put(i, criticalMap.get(i) + qty);
			}
			else
			{
				criticalMap.put(i, qty);
			}

			if (!activityMap.containsKey(i))
			{
				ArrayList<Activity> x = Activity.getByCriticalItem(i);
				if (x != null)
				{
					activityMap.put(i, x);
				}
			}

			if (i.getLinkedItemId() != -1)
			{
				finalForm(i);
			}

		}
	}

	private double getItemXpRate(CriticalItem i)
	{
		List<Activity> activities = activityMap.get(i);
		if (activities != null)
		{
			Activity selected;
			if (activities.size() > 1)
			{
				Activity stored = indexMap.get(i);
				selected = (stored == null) ? activities.get(0) : stored;
			}
			else
			{
				selected = activities.get(0);
			}

			return selected.getXp();
		}
		else
		{
			if (i.getLinkedItemId() == -1)
			{
				return 0;
			}
			else
			{
				return getItemXpRate(CriticalItem.getByItemId(i.getLinkedItemId()));
			}

		}
	}

	// Recreates the Banked Experience Detail container
	private void refreshBankedExpDetails()
	{
		detailContainer.removeAll();

		Map<CriticalItem, Integer> map = getBankedExpBreakdown();
		for (Map.Entry<CriticalItem, Integer> entry : map.entrySet())
		{
			CriticalItem item = entry.getKey();
			boolean flag = categoryMap.get(item.getCategory());
			boolean ignoreFlag = ignoreMap.containsKey(item.getItemID());
			// Category Included and Not ignoring this item ID?
			if (flag && !ignoreFlag)
			{
				double xp = getItemXpRate(item);
				double total = entry.getValue() * xp;

				// Right-Click Menu
				JPopupMenu menu = new JPopupMenu("");
				JMenuItem ignore = new JMenuItem("Ignore Item");
				ignore.addActionListener(e -> ignoreItemID(item.getItemID()));
				menu.add(ignore);

				// Exp panel
				ItemPanel panel = new ItemPanel(this, itemManager, item, xp, entry.getValue(), total);
				panel.setComponentPopupMenu(menu);

				detailContainer.add(panel);
			}
		}

		detailContainer.revalidate();
		detailContainer.repaint();
	}

	// Returns a Map of Items with the amount inside the bank as the value. Items added by category.
	private Map<CriticalItem, Integer> getBankedExpBreakdown()
	{
		Map<CriticalItem, Integer> map = new LinkedHashMap<>();

		for (String category : CriticalItem.getSkillCategories(skill))
		{
			ArrayList<CriticalItem> items = CriticalItem.getItemsForSkillCategories(skill, category);
			for (CriticalItem item : items)
			{
				Integer amount = bankMap.get(item.getItemID());
				if (amount != null && amount > 0)
				{
					map.put(item, amount);
				}
			}
		}

		return map;
	}
}
