/**
 * User: davor
 * Date: 29/11/12
 * Time: 2:30 PM
 */
public class P2PFile {

    private double rating;
    private final String name;
    private final String host;

    public P2PFile(String name,String host) {
        rating = 0;
        this.name = name;
        this.host = host;
    }

    public double rate(double r) {
        rating = 0.5 * rating + 0.5 * r;
        return rating;
    }

    public String getName() {
        return name;
    }
}
