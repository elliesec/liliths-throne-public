package com.lilithsthrone.game.inventory;

import java.util.*;
import java.util.Map.Entry;

import com.lilithsthrone.game.character.GameCharacter;
import com.lilithsthrone.game.character.body.FluidCum;
import com.lilithsthrone.game.character.body.FluidMilk;
import com.lilithsthrone.game.combat.DamageType;
import com.lilithsthrone.game.dialogue.eventLog.EventLogEntryEncyclopediaUnlock;
import com.lilithsthrone.game.inventory.clothing.AbstractClothing;
import com.lilithsthrone.game.inventory.clothing.AbstractClothingType;
import com.lilithsthrone.game.inventory.clothing.ClothingType;
import com.lilithsthrone.game.inventory.enchanting.ItemEffect;
import com.lilithsthrone.game.inventory.enchanting.ItemEffectType;
import com.lilithsthrone.game.inventory.enchanting.TFModifier;
import com.lilithsthrone.game.inventory.enchanting.TFPotency;
import com.lilithsthrone.game.inventory.item.AbstractFilledBreastPump;
import com.lilithsthrone.game.inventory.item.AbstractFilledCondom;
import com.lilithsthrone.game.inventory.item.AbstractItem;
import com.lilithsthrone.game.inventory.item.AbstractItemType;
import com.lilithsthrone.game.inventory.item.ItemType;
import com.lilithsthrone.game.inventory.weapon.AbstractWeapon;
import com.lilithsthrone.game.inventory.weapon.AbstractWeaponType;
import com.lilithsthrone.game.inventory.weapon.WeaponType;
import com.lilithsthrone.main.Main;
import com.lilithsthrone.utils.Util;
import com.lilithsthrone.utils.colours.Colour;


/**
 * @since 0.3.9
 * @version 0.4
 * @author Innoxia
 */
public class ItemGeneration {
	
	// Item generation:

	public AbstractItem generateItem(String id) {
		return new AbstractItem(ItemType.getItemTypeFromId(id)) {};
	}
	
	public AbstractItem generateItem(AbstractItemType itemType) {
		return new AbstractItem(itemType) {};
	}
	
	public AbstractItem generateFilledCondom(AbstractItemType filledCondomType, Colour colour, GameCharacter character, FluidCum cum, int millilitres) {
		return new AbstractFilledCondom(filledCondomType, colour, character, cum, millilitres) {};
	}

	public AbstractItem generateFilledBreastPump(Colour colour, GameCharacter character, FluidMilk milk, int quantity) {
		return new AbstractFilledBreastPump(ItemType.MOO_MILKER_FULL, colour, character, milk, quantity) {};
	}
	
	
	
	// Weapon generation:
	
	public AbstractWeapon generateWeapon(String id) {
		return generateWeapon(WeaponType.getWeaponTypeFromId(id));
	}

	public AbstractWeapon generateWeapon(AbstractWeaponType wt) {
		return this.generateWeapon(wt, wt.getAvailableDamageTypes().get(Util.random.nextInt(wt.getAvailableDamageTypes().size())));
	}
	
	public AbstractWeapon generateWeapon(AbstractWeaponType wt, DamageType dt) {
		return generateWeapon(wt, dt, null);
	}
	
	public AbstractWeapon generateWeapon(String id, DamageType dt) {
		return generateWeapon(WeaponType.getWeaponTypeFromId(id), dt, null);
	}
	
	public AbstractWeapon generateWeapon(String id, DamageType dt, List<Colour> colours) {
		return generateWeapon(WeaponType.getWeaponTypeFromId(id), dt, colours);
	}
	
	public AbstractWeapon generateWeapon(AbstractWeaponType wt, DamageType dt, List<Colour> colours) {
		if(colours==null) {
			colours = new ArrayList<>();
			
		} else {
			colours = new ArrayList<>(colours);
		}
		
		int index = 0;
		ColourReplacement cr = wt.getColourReplacement(false, index);
		while(cr!=null) {
			if(colours.size()<=index || !cr.getAllColours().contains(colours.get(index))) {
				colours.add(cr.getRandomOfDefaultColours());
			}
			index++;
			cr = wt.getColourReplacement(false, index);
		}
		
		for(Entry<Integer, Integer> entry : wt.copyGenerationColours.entrySet()) {
			Colour replacement = colours.get(entry.getValue());
			colours.remove((int)entry.getKey());
			colours.add(entry.getKey(), replacement);
		}
		
		return new AbstractWeapon(wt, dt, colours) {
			@Override
			public String onEquip(GameCharacter character) {
				if (character.isPlayer()) {
					if (Main.getProperties().addWeaponDiscovered(wt)) {
						Main.game.addEvent(new EventLogEntryEncyclopediaUnlock(wt.getName(), wt.getRarity().getColour()), true);
					}
				}
				return wt.equipText(character);
			}

			@Override
			public String onUnequip(GameCharacter character) {
				return wt.unequipText(character);
			}
		};
	}
	
	public AbstractWeapon generateWeapon(AbstractWeapon weapon) {
		return new AbstractWeapon(weapon) {
			@Override
			public String onEquip(GameCharacter character) {
				if (character.isPlayer()) {
					if (Main.getProperties().addWeaponDiscovered(weapon.getWeaponType())) {
						Main.game.addEvent(new EventLogEntryEncyclopediaUnlock(weapon.getWeaponType().getName(), weapon.getWeaponType().getRarity().getColour()), true);
					}
				}
				return weapon.getWeaponType().equipText(character);
			}

			@Override
			public String onUnequip(GameCharacter character) {
				return weapon.getWeaponType().unequipText(character);
			}
		};
	}
	
	
	// Clothing generation:
	
	public AbstractClothing generateClothing(String clothingTypeId, Colour primaryColour, Colour secondaryColour, Colour tertiaryColour, boolean allowRandomEnchantment) {
		return this.generateClothing(ClothingType.getClothingTypeFromId(clothingTypeId), primaryColour, secondaryColour, tertiaryColour, allowRandomEnchantment);
	}
	
	public AbstractClothing generateClothing(AbstractClothingType clothingType, Colour primaryColour, Colour secondaryColour, Colour tertiaryColour, boolean allowRandomEnchantment) {
		List<Colour> colours = Util.newArrayListOfValues(primaryColour, secondaryColour, tertiaryColour);
		
		int index = 0;
		ColourReplacement cr = clothingType.getColourReplacement(index);
		while(cr!=null) {
			if(colours.size()<=index || !cr.getAllColours().contains(colours.get(index))) {
				colours.add(cr.getRandomOfDefaultColours());
			}
			index++;
			cr = clothingType.getColourReplacement(index);
		}
		
		for(Entry<Integer, Integer> entry : clothingType.copyGenerationColours.entrySet()) {
			Colour replacement = colours.get(entry.getValue());
			colours.remove((int)entry.getKey());
			colours.add(entry.getKey(), replacement);
		}
		
		return new AbstractClothing(clothingType, colours, allowRandomEnchantment) {};
	}

	public AbstractClothing generateClothing(AbstractClothingType clothingType, Colour colourShade, boolean allowRandomEnchantment) {
		return this.generateClothing(clothingType, colourShade, null, null, allowRandomEnchantment);
	}

	public AbstractClothing generateClothing(String clothingTypeId, Colour colourShade, boolean allowRandomEnchantment) {
		return this.generateClothing(ClothingType.getClothingTypeFromId(clothingTypeId), colourShade, null, null, allowRandomEnchantment);
	}

	/** Uses random colour.*/
	public AbstractClothing generateClothing(AbstractClothingType clothingType) {
		return this.generateClothing(clothingType, null, true);
	}

	/** Allows random enchantment. Uses random colour.*/
	public AbstractClothing generateClothing(AbstractClothingType clothingType, boolean allowRandomEnchantment) {
		return this.generateClothing(clothingType, null, allowRandomEnchantment);
	}

	/** Allows random enchantment. Uses random colour.*/
	public AbstractClothing generateClothing(String clothingTypeId, boolean allowRandomEnchantment) {
		AbstractClothingType type = ClothingType.getClothingTypeFromId(clothingTypeId);
		return this.generateClothing(type, null, allowRandomEnchantment);
	}

	/** Allows random enchantment. Uses random colour. Restricted by slotHint.*/
	public AbstractClothing generateClothing(String clothingTypeId, boolean allowRandomEnchantment, String slotHint) {
		AbstractClothingType type = ClothingType.getClothingTypeFromId(clothingTypeId, slotHint);
		return this.generateClothing(type, null, allowRandomEnchantment);
	}

	/** Allows random enchantment. Uses random colour. Restricted by slot.*/
	public AbstractClothing generateClothing(String clothingTypeId, boolean allowRandomEnchantment, InventorySlot slot) {
		AbstractClothingType type = ClothingType.getClothingTypeFromId(clothingTypeId, slot.toString());
		return this.generateClothing(type, null, allowRandomEnchantment);
	}

	public AbstractClothing generateClothing(AbstractClothingType clothingType, List<Colour> colours, List<ItemEffect> effects) {
		if(colours==null) {
			colours = new ArrayList<>();
			
		} else {
			colours = new ArrayList<>(colours);
		}
		
		int index = 0;
		ColourReplacement cr = clothingType.getColourReplacement(index);
		while(cr!=null) {
			if(colours.size()<=index || !cr.getAllColours().contains(colours.get(index))) {
				colours.add(cr.getRandomOfDefaultColours());
			}
			index++;
			cr = clothingType.getColourReplacement(index);
		}
		
		for(Entry<Integer, Integer> entry : clothingType.copyGenerationColours.entrySet()) {
			Colour replacement = colours.get(entry.getValue());
			colours.remove((int)entry.getKey());
			colours.add(entry.getKey(), replacement);
		}
		
		return new AbstractClothing(clothingType, colours, effects) {};
	}
	
	/**
	 * Generates clothing with the provided enchantments.
	 */
	public AbstractClothing generateClothing(AbstractClothingType clothingType, Colour primaryColour, Colour secondaryColour, Colour tertiaryColour, List<ItemEffect> effects) {
		return generateClothing(clothingType, Util.newArrayListOfValues(primaryColour, secondaryColour, tertiaryColour), effects);
	}
	
	/**
	 * Generates clothing with the provided enchantments.
	 */
	public AbstractClothing generateClothing(AbstractClothingType clothingType, Colour colour, List<ItemEffect> effects) {
		return generateClothing(clothingType, colour, null, null, effects);
	}
	
	public AbstractClothing generateClothing(String clothingTypeId, Colour colour, List<ItemEffect> effects) {
		return generateClothing(ClothingType.getClothingTypeFromId(clothingTypeId), colour, null, null, effects);
	}
	
	/**
	 * Uses random colour.
	 */
	public AbstractClothing generateClothing(AbstractClothingType clothingType, List<ItemEffect> effects) {
		List<Colour> colours = new ArrayList<>();
		for(ColourReplacement cr : clothingType.getColourReplacements()) {
			colours.add(cr.getRandomOfDefaultColours());
		}
		return this.generateClothing(clothingType, colours, effects);
	}
	
	/**
	 * Generates clothing with a random enchantment.
	 */
	public AbstractClothing generateClothingWithEnchantment(AbstractClothingType clothingType, Colour colour) {
		List<ItemEffect> effects = new ArrayList<>();

		TFModifier rndMod = TFModifier.getClothingAttributeList().get(Util.random.nextInt(TFModifier.getClothingAttributeList().size()));
		effects.add(new ItemEffect(ItemEffectType.CLOTHING, TFModifier.CLOTHING_ATTRIBUTE, rndMod, TFPotency.getRandomWeightedPositivePotency(), 0));
		
		return generateClothing(clothingType, colour, effects);
	}
	
	/**
	 * Uses random colour.
	 */
	public AbstractClothing generateClothingWithEnchantment(AbstractClothingType clothingType) {
		return this.generateClothingWithEnchantment(clothingType, null);
	}

	public AbstractClothing generateClothingWithNegativeEnchantment(AbstractClothingType clothingType, Colour colour) {
		List<ItemEffect> effects = new ArrayList<>();

		TFModifier rndMod = TFModifier.getClothingAttributeList().get(Util.random.nextInt(TFModifier.getClothingAttributeList().size()));
		effects.add(new ItemEffect(ItemEffectType.CLOTHING, TFModifier.CLOTHING_ATTRIBUTE, rndMod, TFPotency.getRandomWeightedNegativePotency(), 0));
		
		return generateClothing(clothingType, colour, effects);
	}
	
	public AbstractClothing generateClothingWithNegativeEnchantment(AbstractClothingType clothingType) {
		return this.generateClothingWithNegativeEnchantment(clothingType, null);
	}
	
	public AbstractClothing generateRareClothing(AbstractClothingType type) {
		List<ItemEffect> effects = new ArrayList<>();
		
		List<TFModifier> attributeMods = new ArrayList<>(TFModifier.getClothingAttributeList());
		
		TFModifier rndMod = attributeMods.get(Util.random.nextInt(attributeMods.size()));
		attributeMods.remove(rndMod);
		TFModifier rndMod2 = attributeMods.get(Util.random.nextInt(attributeMods.size()));
		
		effects.add(new ItemEffect(ItemEffectType.CLOTHING, TFModifier.CLOTHING_ATTRIBUTE, rndMod, TFPotency.MAJOR_BOOST, 0));
		effects.add(new ItemEffect(ItemEffectType.CLOTHING, TFModifier.CLOTHING_ATTRIBUTE, rndMod2, TFPotency.MAJOR_BOOST, 0));
		
		return this.generateClothing(type, effects);
	}

	public List<ItemEffect> generateCursedClothingEnchantments(AbstractClothing clothing, int chanceModifier) {
		chanceModifier = Math.max(0, Math.min(5, chanceModifier));
		int chanceModifierTen = chanceModifier * 10;

		List<ItemEffect> effects = new ArrayList<>();

		int roll = Util.random.nextInt(100);
		TFPotency sealPotency;
		if (roll < 5 + 5 * chanceModifier) {
			sealPotency = TFPotency.MAJOR_DRAIN;
		} else if (roll < 10 + chanceModifierTen) {
			sealPotency = TFPotency.DRAIN;
		} else if (roll < 20 * chanceModifier) {
			sealPotency = TFPotency.MINOR_DRAIN;
		} else {
			sealPotency = TFPotency.MINOR_BOOST;
		}
		effects.add(new ItemEffect(ItemEffectType.CLOTHING, TFModifier.CLOTHING_SPECIAL, TFModifier.CLOTHING_SEALING, sealPotency, 0));

		roll = Util.random.nextInt(100);
		if (roll < chanceModifierTen) {
			effects.add(new ItemEffect(ItemEffectType.CLOTHING, TFModifier.CLOTHING_SPECIAL, TFModifier.CLOTHING_SERVITUDE, TFPotency.MINOR_BOOST, 0));
		}

		if (allowVibrateOrDeny(clothing)) {
			roll = Util.random.nextInt(100);
			if (roll < 20 + chanceModifierTen) {
				effects.add(new ItemEffect(ItemEffectType.CLOTHING, TFModifier.CLOTHING_SPECIAL, TFModifier.CLOTHING_VIBRATION, TFPotency.getRandomWeightedPositivePotency(), 0));
			}
			roll = Util.random.nextInt(100);
			if (roll < 5 * chanceModifier) {
				effects.add(new ItemEffect(ItemEffectType.CLOTHING, TFModifier.CLOTHING_SPECIAL, TFModifier.CLOTHING_ORGASM_PREVENTION, TFPotency.MINOR_BOOST, 0));
			}
		}

		Map<TFModifier, TFModifier> bottomBehaviouralFetishList = new HashMap<>();
		bottomBehaviouralFetishList.put(TFModifier.TF_MOD_FETISH_ANAL_RECEIVING, TFModifier.TF_MOD_FETISH_BODY_PART);
		bottomBehaviouralFetishList.put(TFModifier.TF_MOD_FETISH_VAGINAL_RECEIVING, TFModifier.TF_MOD_FETISH_BODY_PART);
		bottomBehaviouralFetishList.put(TFModifier.TF_MOD_FETISH_BREASTS_SELF, TFModifier.TF_MOD_FETISH_BODY_PART);
		bottomBehaviouralFetishList.put(TFModifier.TF_MOD_FETISH_ORAL_GIVING, TFModifier.TF_MOD_FETISH_BODY_PART);
		bottomBehaviouralFetishList.put(TFModifier.TF_MOD_FETISH_PENIS_RECEIVING, TFModifier.TF_MOD_FETISH_BODY_PART);
		bottomBehaviouralFetishList.put(TFModifier.TF_MOD_FETISH_STRUTTER, TFModifier.TF_MOD_FETISH_BODY_PART);
		bottomBehaviouralFetishList.put(TFModifier.TF_MOD_FETISH_ARMPIT_RECEIVING, TFModifier.TF_MOD_FETISH_BODY_PART);
		bottomBehaviouralFetishList.put(TFModifier.TF_MOD_FETISH_FOOT_RECEIVING, TFModifier.TF_MOD_FETISH_BODY_PART);
		bottomBehaviouralFetishList.put(TFModifier.TF_MOD_FETISH_LACTATION_SELF, TFModifier.TF_MOD_FETISH_BODY_PART);
		bottomBehaviouralFetishList.put(TFModifier.TF_MOD_FETISH_BONDAGE_VICTIM, TFModifier.TF_MOD_FETISH_BEHAVIOUR);
		bottomBehaviouralFetishList.put(TFModifier.TF_MOD_FETISH_SUBMISSIVE, TFModifier.TF_MOD_FETISH_BEHAVIOUR);
		bottomBehaviouralFetishList.put(TFModifier.TF_MOD_FETISH_CUM_ADDICT, TFModifier.TF_MOD_FETISH_BEHAVIOUR);
		bottomBehaviouralFetishList.put(TFModifier.TF_MOD_FETISH_PREGNANCY, TFModifier.TF_MOD_FETISH_BEHAVIOUR);
		bottomBehaviouralFetishList.put(TFModifier.TF_MOD_FETISH_MASOCHIST, TFModifier.TF_MOD_FETISH_BEHAVIOUR);
		bottomBehaviouralFetishList.put(TFModifier.TF_MOD_FETISH_NON_CON_SUB, TFModifier.TF_MOD_FETISH_BEHAVIOUR);
		bottomBehaviouralFetishList.put(TFModifier.TF_MOD_FETISH_DENIAL_SELF, TFModifier.TF_MOD_FETISH_BEHAVIOUR);
		bottomBehaviouralFetishList.put(TFModifier.TF_MOD_FETISH_EXHIBITIONIST, TFModifier.TF_MOD_FETISH_BEHAVIOUR);
		bottomBehaviouralFetishList.put(TFModifier.TF_MOD_FETISH_KINK_RECEIVING, TFModifier.TF_MOD_FETISH_BEHAVIOUR);
		bottomBehaviouralFetishList.put(TFModifier.TF_MOD_FETISH_MASTURBATION, TFModifier.TF_MOD_FETISH_BEHAVIOUR);
		bottomBehaviouralFetishList.put(TFModifier.TF_MOD_FETISH_BIMBO, TFModifier.TF_MOD_FETISH_BEHAVIOUR);
		bottomBehaviouralFetishList.put(TFModifier.TF_MOD_FETISH_SIZE_QUEEN, TFModifier.TF_MOD_FETISH_BEHAVIOUR);

		Map<TFModifier, TFModifier> topBehaviouralFetishList = new HashMap<>();
		topBehaviouralFetishList.put(TFModifier.TF_MOD_FETISH_ANAL_GIVING, TFModifier.TF_MOD_FETISH_BODY_PART);
		topBehaviouralFetishList.put(TFModifier.TF_MOD_FETISH_VAGINAL_GIVING, TFModifier.TF_MOD_FETISH_BODY_PART);
		topBehaviouralFetishList.put(TFModifier.TF_MOD_FETISH_BREASTS_OTHERS, TFModifier.TF_MOD_FETISH_BODY_PART);
		topBehaviouralFetishList.put(TFModifier.TF_MOD_FETISH_ORAL_RECEIVING, TFModifier.TF_MOD_FETISH_BODY_PART);
		topBehaviouralFetishList.put(TFModifier.TF_MOD_FETISH_PENIS_GIVING, TFModifier.TF_MOD_FETISH_BODY_PART);
		topBehaviouralFetishList.put(TFModifier.TF_MOD_FETISH_LEG_LOVER, TFModifier.TF_MOD_FETISH_BODY_PART);
		topBehaviouralFetishList.put(TFModifier.TF_MOD_FETISH_ARMPIT_GIVING, TFModifier.TF_MOD_FETISH_BODY_PART);
		topBehaviouralFetishList.put(TFModifier.TF_MOD_FETISH_FOOT_GIVING, TFModifier.TF_MOD_FETISH_BODY_PART);
		topBehaviouralFetishList.put(TFModifier.TF_MOD_FETISH_LACTATION_OTHERS, TFModifier.TF_MOD_FETISH_BODY_PART);
		topBehaviouralFetishList.put(TFModifier.TF_MOD_FETISH_BONDAGE_APPLIER, TFModifier.TF_MOD_FETISH_BEHAVIOUR);
		topBehaviouralFetishList.put(TFModifier.TF_MOD_FETISH_DOMINANT, TFModifier.TF_MOD_FETISH_BEHAVIOUR);
		topBehaviouralFetishList.put(TFModifier.TF_MOD_FETISH_CUM_STUD, TFModifier.TF_MOD_FETISH_BEHAVIOUR);
		topBehaviouralFetishList.put(TFModifier.TF_MOD_FETISH_DEFLOWERING, TFModifier.TF_MOD_FETISH_BEHAVIOUR);
		topBehaviouralFetishList.put(TFModifier.TF_MOD_FETISH_DENIAL, TFModifier.TF_MOD_FETISH_BEHAVIOUR);
		topBehaviouralFetishList.put(TFModifier.TF_MOD_FETISH_VOYEURIST, TFModifier.TF_MOD_FETISH_BEHAVIOUR);
		topBehaviouralFetishList.put(TFModifier.TF_MOD_FETISH_IMPREGNATION, TFModifier.TF_MOD_FETISH_BEHAVIOUR);
		topBehaviouralFetishList.put(TFModifier.TF_MOD_FETISH_NON_CON_DOM, TFModifier.TF_MOD_FETISH_BEHAVIOUR);
		topBehaviouralFetishList.put(TFModifier.TF_MOD_FETISH_SADIST, TFModifier.TF_MOD_FETISH_BEHAVIOUR);
		topBehaviouralFetishList.put(TFModifier.TF_MOD_FETISH_TRANSFORMATION_GIVING, TFModifier.TF_MOD_FETISH_BEHAVIOUR);
		topBehaviouralFetishList.put(TFModifier.TF_MOD_FETISH_KINK_GIVING, TFModifier.TF_MOD_FETISH_BEHAVIOUR);

		int counter = 0;
		roll = Util.random.nextInt(100);
		Set<TFModifier> chosenTFModifiers = new HashSet<>();
		while (counter < chanceModifier + 1 && roll < chanceModifierTen) {
			chosenTFModifiers.add(Util.randomItemFrom((Util.random.nextBoolean() ? bottomBehaviouralFetishList : topBehaviouralFetishList).keySet()));
			counter++;
			roll = Util.random.nextInt(100);
		}
		chosenTFModifiers.forEach((modifier) -> {
			boolean bottom = bottomBehaviouralFetishList.keySet().contains(modifier);
			TFPotency potency = bottom ? TFPotency.getRandomWeightedPositivePotency() : TFPotency.getRandomWeightedNegativePotency();
			effects.add(new ItemEffect(ItemEffectType.CLOTHING, (bottom ? bottomBehaviouralFetishList : topBehaviouralFetishList).get(modifier), modifier, potency, 0));
		});

		counter = 0;

		List<TFModifier> clothingMajorAttributeList = TFModifier.getClothingMajorAttributeList();
		List<TFModifier> clothingAttributeList = TFModifier.getClothingAttributeList();
		List<TFModifier> attributeModifierList = new ArrayList<>();
		attributeModifierList.addAll(clothingMajorAttributeList);
		attributeModifierList.addAll(clothingAttributeList);
		do {
			TFModifier modifier = Util.randomItemFrom(attributeModifierList);
			TFPotency potency = modifier == TFModifier.CORRUPTION ||
					modifier == TFModifier.FERTILITY ?
					TFPotency.getRandomWeightedPositivePotency() :
					TFPotency.getRandomWeightedNegativePotency();
			boolean major = clothingMajorAttributeList.contains(modifier);
			int effectCount = Util.random.nextInt(chanceModifier * 2);
			for (int i = 0; i < effectCount; i++) {
				effects.add(new ItemEffect(ItemEffectType.CLOTHING, major ? TFModifier.CLOTHING_MAJOR_ATTRIBUTE : TFModifier.CLOTHING_ATTRIBUTE, modifier, potency, 0));
			}
			counter++;
			roll = Util.random.nextInt(100);
		} while (counter < chanceModifier + 1 && roll < 30 + chanceModifierTen);

		return effects;
	}

	private boolean allowVibrateOrDeny(AbstractClothing targetItem) {
		return targetItem.getItemTags().contains(ItemTag.ENABLE_SEX_EQUIP) ||
				!Collections.disjoint(
						targetItem.getClothingType().getEquipSlots(),
						Util.newArrayListOfValues(
								InventorySlot.GROIN,
								InventorySlot.VAGINA,
								InventorySlot.PENIS,
								InventorySlot.ANUS,
								InventorySlot.NIPPLE,
								InventorySlot.CHEST,
								InventorySlot.PIERCING_NIPPLE,
								InventorySlot.PIERCING_PENIS,
								InventorySlot.PIERCING_VAGINA
						)
				);
	}
}
