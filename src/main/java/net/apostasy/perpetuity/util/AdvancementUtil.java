package net.apostasy.perpetuity.util;

import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.advancement.PlayerAdvancementTracker;
import net.minecraft.advancement.criterion.CriterionProgress;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringIdentifiable;

public class AdvancementUtil {
    public static AdvancementResult grantAdvancement(ServerPlayerEntity player, Identifier advancement) {
        if (player.getEntityWorld().getServer() == null) return AdvancementResult.SERVER_NOT_FOUND;
        PlayerAdvancementTracker advancementTracker = player.getAdvancementTracker();

        AdvancementEntry entry = player.getEntityWorld().getServer().getAdvancementLoader().get(advancement);
        if (entry == null) return AdvancementResult.INVALID_ADVANCEMENT;

        if (!advancementTracker.getProgress(entry).isDone()) {
            for (String criteria : advancementTracker.getProgress(entry).getUnobtainedCriteria()) {
                advancementTracker.grantCriterion(entry, criteria);
            }
        } else return AdvancementResult.PASS;
        return AdvancementResult.SUCCESS;
    }

    public static AdvancementResult revokeAdvancement(ServerPlayerEntity player, Identifier advancement) {
        if (player.getEntityWorld().getServer() == null) return AdvancementResult.SERVER_NOT_FOUND;
        PlayerAdvancementTracker advancementTracker = player.getAdvancementTracker();

        AdvancementEntry entry = player.getEntityWorld().getServer().getAdvancementLoader().get(advancement);
        if (entry == null) return AdvancementResult.INVALID_ADVANCEMENT;

        if (advancementTracker.getProgress(entry).isDone()) {
            for (String criteria : advancementTracker.getProgress(entry).getObtainedCriteria()) {
                advancementTracker.revokeCriterion(entry, criteria);
            }
        } else return AdvancementResult.PASS;
        return AdvancementResult.SUCCESS;
    }

    public static AdvancementResult grantCriterion(ServerPlayerEntity player, Identifier advancement, String criterion) {
        if (player.getEntityWorld().getServer() == null) return AdvancementResult.SERVER_NOT_FOUND;
        PlayerAdvancementTracker advancementTracker = player.getAdvancementTracker();

        AdvancementEntry entry = player.getEntityWorld().getServer().getAdvancementLoader().get(advancement);
        if (advancement == null) return AdvancementResult.INVALID_ADVANCEMENT;

        CriterionProgress progress = advancementTracker.getProgress(entry).getCriterionProgress(criterion);
        if (progress == null) return AdvancementResult.INVALID_CRITERION;

        if (!progress.isObtained()) progress.obtain();
        else return AdvancementResult.PASS;
        return AdvancementResult.SUCCESS;
    }

    public static AdvancementResult revokeCriterion(ServerPlayerEntity player, Identifier advancement, String criterion) {
        if (player.getEntityWorld().getServer() == null) return AdvancementResult.SERVER_NOT_FOUND;
        PlayerAdvancementTracker advancementTracker = player.getAdvancementTracker();

        AdvancementEntry entry = player.getEntityWorld().getServer().getAdvancementLoader().get(advancement);
        if (advancement == null) return AdvancementResult.INVALID_ADVANCEMENT;

        CriterionProgress progress = advancementTracker.getProgress(entry).getCriterionProgress(criterion);
        if (progress == null) return AdvancementResult.INVALID_CRITERION;

        if (progress.isObtained()) progress.reset();
        else return AdvancementResult.PASS;
        return AdvancementResult.SUCCESS;
    }

    public enum AdvancementResult implements StringIdentifiable {
        SUCCESS("success", true),
        PASS("pass", true),
        SERVER_NOT_FOUND("server_not_found", false),
        INVALID_ADVANCEMENT("invalid_advancement", false),
        INVALID_CRITERION("invalid_criterion", false);

        private final String id;
        private final boolean passes;

        AdvancementResult(String id, boolean passes) {
            this.id = id;
            this.passes = passes;
        }

        @Override
        public String asString() {
            return id;
        }

        @Override
        public String toString() {
            return id;
        }

        public boolean passes() {
            return passes;
        }
    }
}
