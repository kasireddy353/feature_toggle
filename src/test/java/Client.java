import com.learning.Feature;

public class Client {


    public static void main(String[] args) throws InterruptedException {


        Feature feature = Feature.create("localhost", 6379);

        for (int i = 0; i < 50; i++) {
            feature.featureEnabled("myfeature8");
            Thread.sleep(100);
        }



        Thread.sleep(Integer.MAX_VALUE);

    }
}
