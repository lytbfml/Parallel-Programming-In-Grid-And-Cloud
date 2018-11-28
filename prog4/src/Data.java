/**
 * @author Yangxiao on 11/22/2018.
 */

import scala.Tuple2;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Vertex Attributes
 */
class Data implements Serializable {
	List<Tuple2<String, Integer>> neighbors; // <neighbor0, weight0>, ...
	String status; // "INACTIVE" or "ACTIVE"
	Integer distance; // the distance so far from source to this vertex
	Integer prev; // the distance calculated in the previous iteration
	
	public Data() {
		neighbors = new ArrayList<>();
		status = "INACTIVE";
		distance = 0;
	}
	
	public Data(List<Tuple2<String, Integer>> neighbors,
	            Integer dist, Integer prev, String status) {
		if (neighbors != null) {
			this.neighbors = new ArrayList<>(neighbors);
		} else {
			this.neighbors = new ArrayList<>();
		}
		this.distance = dist;
		this.prev = prev;
		this.status = status;
	}
}
