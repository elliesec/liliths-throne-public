package com.lilithsthrone.game.dialogue.npcDialogue.common;

import com.lilithsthrone.game.character.GameCharacter;
import com.lilithsthrone.game.character.PlayerCharacter;
import com.lilithsthrone.game.character.fetishes.Fetish;
import com.lilithsthrone.game.character.fetishes.FetishDesire;
import com.lilithsthrone.game.character.npc.NPC;
import com.lilithsthrone.game.character.npc.NPCFlagValue;
import com.lilithsthrone.game.dialogue.DialogueNode;
import com.lilithsthrone.game.dialogue.responses.Response;
import com.lilithsthrone.game.dialogue.utils.UtilText;
import com.lilithsthrone.game.inventory.clothing.AbstractClothing;
import com.lilithsthrone.main.Main;
import com.lilithsthrone.utils.Util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class AfterSexDefeatCommonDialogueNode extends DialogueNode {
    private final int difficulty;
    private final DialogueNode continueNode;

    private StringBuilder bondageClothingDialogue = new StringBuilder();

    public AfterSexDefeatCommonDialogueNode(final int difficulty, final DialogueNode continueNode) {
        super("Collapse", "", true);
        this.difficulty = Math.max(0, Math.min(difficulty, 5));
        this.continueNode = continueNode;
    }

    private static List<GameCharacter> getAllCharacters() {
        List<GameCharacter> allCharacters = new ArrayList<>();
        allCharacters.add(getAttacker());
        allCharacters.addAll(Main.game.getPlayer().getCompanions());
        allCharacters.sort((c1, c2) -> c1.isElemental() ? (c2.isElemental() ? 0 : 1) : (c2.isElemental() ? -1 : 0));
        return allCharacters;
    }

    private static NPC getAttacker() {
        return Main.game.getActiveNPC();
    }

    private static GameCharacter getMainCompanion() {
        return Main.game.getPlayer().getMainCompanion();
    }

    private static boolean hasCompanions() {
        return Main.game.getPlayer().hasCompanions();
    }

    @Override
    public int getSecondsPassed() {
        return difficulty * 10 * 60;
    }

    @Override
    public String getDescription() {
        return "You're completely worn out from [npc.namePos] dominant treatment, and need a while to recover.";
    }

    @Override
    public String getContent() {
        String content1 = UtilText.parseFromXMLFile("encounters/common/commonAttack", "AFTER_SEX_DEFEAT_1", getAllCharacters());
        String content2 = UtilText.parseFromXMLFile("encounters/common/commonAttack", "AFTER_SEX_DEFEAT_2", getAllCharacters());
        return content1 + bondageClothingDialogue.toString() + content2;
    }

    @Override
    public Response getResponse(int responseTab, int index) {
        if (index == 1) {
            return new Response("Continue", "Carry on your way.", continueNode) {
                @Override
                public void effects() {
                    if (getAttacker().hasFlag(NPCFlagValue.genericNPCBetrayedByPlayer)) {
                        Main.game.banishNPC(getAttacker());
                    }
                }

                @Override
                public DialogueNode getNextDialogue() {
                    return Main.game.getDefaultDialogue(false);
                }
            };
        }
        return null;
    }

    @Override
    public void applyPreParsingEffects() {
        final PlayerCharacter player = Main.game.getPlayer();
        final NPC attacker = getAttacker();
        bondageClothingDialogue = new StringBuilder();
        final Set<AbstractClothing> playerBondageClothing = attacker.generateBondageApplierClothing(player, attacker, difficulty);
        Set<AbstractClothing> companionBondageClothing = Collections.emptySet();

        if (hasCompanions()) {
            companionBondageClothing = attacker.generateBondageApplierClothing(getMainCompanion(), attacker, difficulty);
        }
        List<String> playerEquipText = new ArrayList<>();
        if (playerBondageClothing.size() > 0) {
            playerBondageClothing.forEach((clothing) -> {
                if (player.isAbleToEquip(clothing, true, attacker)) {
                    playerEquipText.add(player.equipClothingFromNowhere(clothing, true, attacker));
                }
            });
            if (playerEquipText.size() > 0) {
                bondageClothingDialogue.append("<p>")
                        .append("Looking down at you, [npc.name] smirks. [npc.speech(Before I go, I've got a present for you to remember me by.)] Exhausted, you're unable to resist as [npc.name] bears down on you with a handful of items.")
                        .append("</p>");
                playerEquipText.forEach((text) -> bondageClothingDialogue.append("<p>")
                        .append(text)
                        .append("</p>"));
            }
        }
        if (companionBondageClothing.size() > 0) {
            List<String> equipText = new ArrayList<>();
            companionBondageClothing.forEach((clothing) -> {
                if (getMainCompanion().isAbleToEquip(clothing, true, attacker)) {
                    equipText.add(getMainCompanion().equipClothingFromNowhere(clothing, true, attacker));
                }
            });
            if (equipText.size() > 0) {
                if (playerEquipText.size() > 0) {
                    bondageClothingDialogue.append("<p>")
                            .append("[npc.name] looks over to [com.name] and grins. [npc.speech(Don't worry, I've got something for you as well!)] [npc.she] quickly repeats the process with [com.name] before stepping back to admire her handywork.")
                            .append("</p>");
                } else {
                    bondageClothingDialogue.append("<p>")
                            .append("Looking over at [com.name], [npc.name] smirks. [npc.speech(Before I go, I've got a present for you to remember me by.)] Exhausted, [com.name] is unable to resist as [npc.name] bears down on her with a handful of items.")
                            .append("</p>");
                }
                equipText.forEach((text) -> bondageClothingDialogue.append("<p>")
                        .append(text)
                        .append("</p>"));
            }
        }

        if ((playerBondageClothing.size() > 0 || companionBondageClothing.size() > 0)) {
            bondageClothingDialogue.append(attacker.developFetish(Fetish.FETISH_BONDAGE_APPLIER));
            bondageClothingDialogue.append(player.developFetish(Fetish.FETISH_BONDAGE_VICTIM));
        }

        super.applyPreParsingEffects();
    }
}
