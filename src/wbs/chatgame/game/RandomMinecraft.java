package wbs.chatgame.game;

import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.enchantments.Enchantment;

// Helper class for getting random materials given restrictions
public class RandomMinecraft {

	private static Material[] materials = Material.values(); // Cached array
	private final static int SIZE = materials.length;
	
	private static Enchantment[] enchantments = Enchantment.values();
	private final static int ENCH_SIZE = enchantments.length;
	
	private static Biome[] biomes = Biome.values();
	private final static int BIOME_SIZE = biomes.length;

	private static String[] mobs = { // More specifically spawn egg mobs + ender dragon + wither
			"BAT", "BLAZE", "CHICKEN", "CAVE SPIDER", "COD", "COW", "CREEPER", "DOLPHIN", "DONKEY", "DROWNED",
			"ELDER GUARDIAN", "ENDERMAN", "ENDERMITE", "EVOKER", "GHAST", "GUARDIAN", "HORSE", "HUSK",
			"LLAMA", "MAGMA_CUBE", "MOOSHROOM", "MULE", "OCELOT", "PARROT", "PHANTOM", "PIG", "POLAR_BEAR",
			"PUFFERFISH", "RABBIT", "SALMON", "SHEEP", "SHULKER", "SILVERFISH",
			"SKELETON", "SKELETON_HORSE", "SLIME", "SPIDER", "SQUID", "STRAY", "TROPICAL_FISH",
			"TURTLE", "VEX", "VILLAGER", "VINDICATOR", "WITCH", "WITHER_SKELETON", "WOLF",
			"ZOMBIE", "ZOMBIE_PIGMAN", "ZOMBIE_VILLAGER", "ZOMBIE_HORSE", "WITHER", "ENDER_DRAGON", "IRON_GOLEM"
	};
	private final static int MOBS_SIZE = mobs.length;

	private final static String[] VARIATIONS = {
			"BOAT", "BUTTON", "DOOR", "FENCE", "PRESSURE_PLATE",
			"SIGN", "SLAB", "STAIRS", "TRAPDOOR", "CORAL", "WALL",
			"BUCKET", "PILLAR"
	};
	private final static String[] COLOURS = {
			"BANNER", "BED", "CONCRETE", "DYE", "SHULKER_BOX",
			"STAINED_GLASS", "WOOL", "TERRACOTTA", "CARPET"
	};
	private final static String[] TOOLS = {
			"PICKAXE", "SHOVEL", "AXE", "SWORD", "HOE"
	};
	private final static String[] ARMOUR = {
			"HELMET", "CHESTPLATE", "LEGGINGS", "BOOTS"
	};
	private final static String[] TECHNICAL = {
		"WALL_SIGN", "WALL_BANNER", "CORAL_WALL_FAN", "CARROTS", "POTATOES",
		"CAVE_AIR", "CHIPPED_ANVIL", "INFESTED", "KELP_PLANT",
		"MELON_STEM", "PUMPKIN_STEM", "MOVING_PISTON", "PISTON_HEAD",
		"WALL_HEAD", "WALL_SKULL", "POTTED", "PATTERN", "VOID_AIR",
		"WALL_TORCH", "WRITABLE_BOOK", "BEETROOTS", "LEGACY", "SPAWN_EGG", "MUSIC_DISC",
		"SMOOTH", "CUT_", "FARMLAND", "CLAY_BALL", "FILLED_MAP"
	};
	
	// Arrays of numbers from 0 to size of respective type that is randomized, thus assuring no repeats until necessary
	private static int[] materialOrder = shuffleArray(newOrders(SIZE));
	private static int materialCount = 0; // Counter for which index was last used.
	private static int[] mobsOrder = shuffleArray(newOrders(MOBS_SIZE));
	private static int mobsCount = 0;
	private static int[] enchantmentsOrder = shuffleArray(newOrders(ENCH_SIZE));
	private static int enchantmentsCount = 0;
	private static int[] biomesOrder = shuffleArray(newOrders(BIOME_SIZE));
	private static int biomesCount = 0;
	
	private static Material nextMaterial() {
		materialCount++;
		if (materialCount >= SIZE) {
			materialCount = 0;
			shuffleArray(materialOrder);
		}
		return materials[materialOrder[materialCount]];
	}
	
	private static int[] newOrders(int size) {
		int[] orders = new int[size];
		for (int i = 0; i < size; i++) {
			orders[i] = i;
		}
		return orders;
	}
	
	private static int[] shuffleArray(int[] toShuffle) {
		Random random = new Random();
		// Perform an in-memory swap for each element
		int swapvar;
		int randomIndex = random.nextInt(toShuffle.length);
		for (int i = 0; i < toShuffle.length; i++) {
			swapvar = toShuffle[i];
			toShuffle[i] = toShuffle[randomIndex];
			toShuffle[randomIndex] = swapvar;
			randomIndex = random.nextInt(toShuffle.length);
		}
		return toShuffle;
	}
	
	private static int nextMob() {
		mobsCount++;
		if (mobsCount >= MOBS_SIZE) {
			mobsCount = 0;
			shuffleArray(mobsOrder);
		}
		return mobsOrder[mobsCount];
	}
	
	public static String getRandomMob() {
		return mobs[nextMob()].toLowerCase().replace('_', ' ');
	}
	
	// Helper function to check if any string in an array contains a given string.
	private static boolean arrayContainsContains(String[] array, String name) {
		for (int i = 0; i < array.length; i++) {
			if (name.contains(array[i])) {
				return true;
			}
		}
		return false;
	}
	
	private static String getFound(String[] array, String name) {
		for (int i = 0; i < array.length; i++) {
			if (name.contains(array[i])) {
				return array[i];
			}
		}
		return null;
	}

	private static boolean chance(double percentage) {
		return (Math.random() < percentage/100);
	}
	
	public static String getRandomMaterial(boolean variations, boolean colours, boolean tools, boolean armour) {
		Material returnMaterial;
		String materialName = null;
		
		boolean invalid = true;
		boolean variationsInvalid = true; // always true if variations is false - Only need to check if it's true
		boolean coloursInvalid = true; // always true if colours is false
		while (invalid) {
			returnMaterial = nextMaterial();
			materialName = returnMaterial.name();
			
			if (!tools && arrayContainsContains(TOOLS, materialName)) { 
				if (chance(20)) { // Rescale because wood stone iron gold and diamond would all give tool
					return getFound(TOOLS, materialName).toLowerCase().replace('_', ' ');
				}
			} else if (!armour && arrayContainsContains(ARMOUR, materialName)) {
				if (chance(20)) { // Rescale because leather chainmail iron gold diamond would all give armour
					return getFound(ARMOUR, materialName).toLowerCase().replace('_', ' ');
				}
			} else {
				variationsInvalid = (!variations && arrayContainsContains(VARIATIONS, materialName));
				coloursInvalid = (!colours && arrayContainsContains(COLOURS, materialName));
				invalid = arrayContainsContains(TECHNICAL, materialName) || variationsInvalid || coloursInvalid;
			}
		}
		
		// The material is now a material that does not overlap with the users choices.
		if (materialName.equals("JACK_O_LANTERN")) {
			materialName = "jack o'lantern";
		} else if (materialName.equals("DRAGON_BREATHE")) {
			materialName = "jack o'lantern";
		} else if (materialName.equals("RABIT_FOOT")) {
			materialName = "rabbit's foot";
		} else if (materialName.equals("EXPERIENCE_BOTTLE")) {
			materialName = "bottle o' enchanting";
		}
		return materialName.toLowerCase().replace('_', ' ');
	}

	private static int nextEnchantment() {
		enchantmentsCount++;
		if (enchantmentsCount >= ENCH_SIZE) {
			enchantmentsCount = 0;
			shuffleArray(enchantmentsOrder);
		}
		return enchantmentsOrder[enchantmentsCount];
	}
	
	public static String getRandomEnchantment() {
		return enchantments[nextEnchantment()].getKey().getNamespace().toLowerCase().replace('_', ' ');
	}

	private static int nextBiome() {
		biomesCount++;
		if (biomesCount >= BIOME_SIZE) {
			biomesCount = 0;
			shuffleArray(biomesOrder);
		}
		return enchantmentsCount;
	}
	public static String getRandomBiome() {
		return biomes[nextBiome()].name().toLowerCase().replace('_', ' ');
	}
	
	/******************************************************/

	private static String[] scrambleWords;
	private static int[] scrambleOrder;
	private static int scrambleCount = 0;
	
	public static void setUnscramble(Map<String, Integer> unscramble) {
		int size = unscramble.size();
		Set<String> words = unscramble.keySet();
		String[] wordArray = new String[size];
		wordArray = words.toArray(wordArray);
		
		scrambleWords = wordArray;
		scrambleOrder = shuffleArray(newOrders(size));
	}

	private static TriviaQuestion[] triviaWords;
	private static int[] triviaOrder;
	private static int triviaCount = 0;
	
	public static void setTrivia(Map<TriviaQuestion, Integer> trivia) {
		int size = trivia.size();
		Set<TriviaQuestion> words = trivia.keySet();
		TriviaQuestion[] wordArray = new TriviaQuestion[size];
		wordArray = words.toArray(wordArray);
		
		triviaWords = wordArray;
		triviaOrder = shuffleArray(newOrders(size));
	}

	private static String[] quicktypeWords;
	private static int[] quicktypeOrder;
	private static int quicktypeCount = 0;
	
	public static void setQuicktype(Map<String, Integer> quicktype) {
		int size = quicktype.size();
		Set<String> words = quicktype.keySet();
		String[] wordArray = new String[size];
		wordArray = words.toArray(wordArray);
		
		quicktypeWords = wordArray;
		quicktypeOrder = shuffleArray(newOrders(size));
	}

	private static String[] revealWords;
	private static int[] revealOrder;
	private static int revealCount = 0;
	
	public static void setReveal(Map<String, Integer> reveal) {
		int size = reveal.size();
		Set<String> words = reveal.keySet();
		String[] wordArray = new String[size];
		wordArray = words.toArray(wordArray);
		
		revealWords = wordArray;
		revealOrder = shuffleArray(newOrders(size));
	}
	
	
	public static String getTypeWord(GameType type) {
		String word = null;
		switch (type) {
		case UNSCRAMBLE:
			scrambleCount++;
			if (scrambleCount >= scrambleWords.length) {
				scrambleCount = 0;
				shuffleArray(scrambleOrder);
			}
			return scrambleWords[scrambleOrder[scrambleCount]];
			
		case QUICKTYPE:
			quicktypeCount++;
			if (quicktypeCount >= quicktypeWords.length) {
				quicktypeCount = 0;
				shuffleArray(quicktypeOrder);
			}
			return quicktypeWords[quicktypeOrder[quicktypeCount]];
			
		case REVEAL:
			revealCount++;
			if (revealCount >= revealWords.length) {
				revealCount = 0;
				shuffleArray(revealOrder);
			}
			return revealWords[revealOrder[revealCount]];
			
		default:
			word = null;
		}
		
		return word;
	}
	
	public static TriviaQuestion randomTrivia() {
		triviaCount++;
		if (triviaCount >= triviaWords.length) {
			triviaCount = 0;
			shuffleArray(triviaOrder);
		}
		
		return triviaWords[triviaOrder[triviaCount]];
	}
	
	
}
