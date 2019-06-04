package wbs.chatgame.player;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;

// Binary tree implementation for PlayerData objects that allows log(n) searches
public class DataTree {

	private PlayerData data;
	private DataTree left, right;
	private ArrayList<PlayerData> siblings = new ArrayList<>();
	
	public DataTree(PlayerData data) {
		left = null;
		right = null;
		siblings.add(data);
	}
	
	public PlayerData getData() {
		return data;
	}
	
	public void add(PlayerData newData) {
		int points = data.getTotalPoints();
		int newPoints = newData.getTotalPoints();
		if (points == newPoints) {
			siblings.add(newData);
			return;
		}
		if (points < newPoints) {
			left.add(newData);
		} else {
			right.add(newData);
		}
	}
	
	// Get what rank someone is based on their points
	public int getRank(int checkPoints) {
		int points = data.getTotalPoints();
		if (points == checkPoints) {
			if (right == null) {
				return 0;
			}
			return right.size();
		}
		if (points < checkPoints) {
			return right.getRank(checkPoints);
		} else {
			if (right == null) {
				return left.getRank(checkPoints) + siblings.size();
			}
			return left.getRank(checkPoints) + siblings.size() +  right.size();
		}
	}
	
	public ArrayList<PlayerData> getByPoints(int checkPoints) {
		int points = data.getTotalPoints();
		if (points == checkPoints) {
			return siblings;
		}
		if (points < checkPoints) {
			return right.getByPoints(checkPoints);
		} else {
			return left.getByPoints(checkPoints);
		}
	}
	
	public int size() {
		if (right == null && left == null) {
			return siblings.size();
		}
		if (left == null) {
			return right.size();
		}
		if (right == null) {
			return left.size();
		}
		return left.size() + right.size();
	}
	
	/* Splicing based on size of subtrees
	 * When the number of nodes to the right is equal to the rank, the correct
	 * sibling set is found. (Noting that rank 0 is the highest)
	 */
	public ArrayList<PlayerData> getByRank(int rank) {
		int rightSize = right.size();
		int sibSize = siblings.size();

		if (rightSize == rank) {
			return siblings;
		}
		if (rightSize > rank) { // The rank is a point value higher than this
			return right.getByRank(rank);
		} else if (rightSize + sibSize > rank) { // The rank is in the siblings
			return siblings;
		}
		return (left.getByRank(rank - (rightSize + sibSize)));
	}
}
