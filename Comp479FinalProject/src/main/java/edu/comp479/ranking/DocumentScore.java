package edu.comp479.ranking;

/**
 * This class is a placeholder for document scores.
 * 
 * @author Mohsen Parisay <mohsenparisay@gmail.com>
 * @version <1.0> - <26.nov.2018>
 */
public class DocumentScore {

    private double weight;
    private double sentiment;

    public double getWeight() {
        return weight;
    }

    public double getSentiment() {
        return sentiment;
    }

    public void setScore(double w, double s) {
        this.weight = w;
        this.sentiment = s;
    }

    /**
     * We are free to decide how to calculate a total score
     * from two input parameters, weight and sentiment value
     * this method returns simply the average of the product
     * of the two inputs.
     * 
     * @param w wight parameter
     * @param s sentiment value
     * @return the calculated value of the both input values
     */
    public double calculateTotalScore(double w, double s) {
        double avg = (double) (w * s) / 2;
        return avg;
    }
}
