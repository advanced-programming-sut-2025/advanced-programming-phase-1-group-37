package farmgame;

import java.util.EnumMap;
import java.util.Map;

public class EnergyModule {

    public static class EnergyManager {
        private static final int DEFAULT_MAX_ENERGY = 200;

        private int maxEnergy;
        private int currentEnergy;
        private boolean unlimited;

        private boolean faintedToday;

        public EnergyManager() {
            this.maxEnergy = DEFAULT_MAX_ENERGY;
            this.currentEnergy = maxEnergy;
        }

        public void startNewDay() {
            if (unlimited) return; // unlimited persists across days
            currentEnergy = faintedToday ? (int) Math.round(maxEnergy * 0.75) : maxEnergy;
            faintedToday = false;
        }


        public int show() {
            return unlimited ? Integer.MAX_VALUE : currentEnergy;
        }


        public void set(int value) {
            if (value < 0) value = 0;
            this.currentEnergy = Math.min(value, maxEnergy);
        }

        public void toggleUnlimited() {
            this.unlimited = !unlimited;
        }

        public boolean isUnlimited() {
            return unlimited;
        }

        public void consume(int amount) throws EnergyException {
            if (amount <= 0 || unlimited) return; // no‑op
            if (currentEnergy < amount) {
                faint();
                throw new EnergyException("Not enough energy – player fainted.");
            }
            currentEnergy -= amount;
        }

        private void faint() {
            currentEnergy = 0;
            faintedToday = true;
        }
    }

    public static class EnergyException extends RuntimeException {
        public EnergyException(String msg) { super(msg); }
    }


    public enum SkillType { FARMING, MINING, FORAGING, FISHING, NONE }


    public static class SkillSet {
        private final Map<SkillType, Integer> levels = new EnumMap<>(SkillType.class);

        public SkillSet() {
            for (SkillType t : SkillType.values()) levels.put(t, 0);
        }

        public int level(SkillType t) { return levels.get(t); }

        public void gain(SkillType t) {
            int lvl = levels.get(t);
            if (lvl < 4) levels.put(t, lvl + 1);
        }
    }

    /* ─────────────────────────────────────────── tools  ─────────────────────────────────────────── */

    public enum ToolType {
        // —— HOES ———————————————————————————
        HOE_BASIC(5, SkillType.FARMING),
        HOE_COPPER(4, SkillType.FARMING),
        HOE_IRON(3, SkillType.FARMING),
        HOE_GOLD(2, SkillType.FARMING),
        HOE_IRIDIUM(1, SkillType.FARMING),

        // —— PICKAXES —————————————————————————
        PICKAXE_BASIC(5, SkillType.MINING),
        PICKAXE_COPPER(4, SkillType.MINING),
        PICKAXE_IRON(3, SkillType.MINING),
        PICKAXE_GOLD(2, SkillType.MINING),
        PICKAXE_IRIDIUM(1, SkillType.MINING),

        // —— AXES ————————————————————————————
        AXE_BASIC(5, SkillType.FORAGING),
        AXE_COPPER(4, SkillType.FORAGING),
        AXE_IRON(3, SkillType.FORAGING),
        AXE_GOLD(2, SkillType.FORAGING),
        AXE_IRIDIUM(1, SkillType.FORAGING),

        // —— WATER CANS ————————————————————————
        WATERING_CAN_BASIC(5, SkillType.FARMING),
        WATERING_CAN_COPPER(4, SkillType.FARMING),
        WATERING_CAN_IRON(3, SkillType.FARMING),
        WATERING_CAN_GOLD(2, SkillType.FARMING),
        WATERING_CAN_IRIDIUM(1, SkillType.FARMING),

        // —— FISHING RODS ——————————————————————
        FISHING_ROD_TRAINING(8, SkillType.FISHING),
        FISHING_ROD_BAMBOO(8, SkillType.FISHING),
        FISHING_ROD_FIBERGLASS(6, SkillType.FISHING),
        FISHING_ROD_IRIDIUM(4, SkillType.FISHING),

        // —— MISC ————————————————————————————
        SCYTHE(2, SkillType.NONE),
        MILK_PAIL(4, SkillType.NONE),
        SHEAR(4, SkillType.NONE);

        private final int baseCost;
        private final SkillType relatedSkill;

        ToolType(int baseCost, SkillType skill) {
            this.baseCost = baseCost;
            this.relatedSkill = skill;
        }

        public int baseCost() { return baseCost; }
        public SkillType skill() { return relatedSkill; }
    }

    /* ─────────────────────────────────── cost calculators (movement, tools) ─────────────────────────────────── */

    public static final class EnergyCosts {
        private EnergyCosts() {}


        public static int forTool(ToolType tool, SkillSet skills) {
            if (tool.baseCost() == 0) return 0;
            int lvl = skills.level(tool.skill());
            int cost = tool.baseCost() - lvl; // 1 energy saved per skill level
            return Math.max(1, cost);
        }

        public static int forMovement(int distanceInTiles) {
            if (distanceInTiles <= 0) return 0;
            return Math.max(1, distanceInTiles / 20);
        }

        public static int forMovement(int distanceInTiles, int numberOfTurns) {
            if (distanceInTiles <= 0) return 0;
            int numerator = distanceInTiles + 10 * numberOfTurns;
            return Math.max(1, numerator / 20);
        }
    }
}
