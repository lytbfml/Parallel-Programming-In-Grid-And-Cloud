import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import scala.Tuple2;

/**                                                                                                                        
 * Vertex Attributes
 */
public class Data implements Serializable {
    List<Tuple2<String,Integer>> neighbors; // <neighbor0, weight0>, <neighbor1, weight1>, ...
    String status;                          // "INACTIVE" or "ACTIVE"
    Integer distance;                       // the distance so far from source to this vertex
    Integer prev;                           // the distance calculated in the previous iteration

    public Data(){
        neighbors = new ArrayList<>();
        status = "INACTIVE";
        distance = 0;
    }

    public Data( List<Tuple2<String,Integer>> neighbors, Integer dist, Integer prev, String status ){
        if ( neighbors != null ) {
            this.neighbors = new ArrayList<>( neighbors );
        } else {
            this.neighbors = new ArrayList<>( );
        }
        this.distance = dist;
	this.prev = prev;
        this.status = status;
    }
}
