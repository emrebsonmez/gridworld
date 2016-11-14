import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by emresonmez on 11/23/15.
 */
public class FileUtils {

    public void averageAndWrite(String filename, ArrayList<ArrayList<Integer>> logs){
        HashMap<Integer,Double> averages = new HashMap<>();
        for (int i = 0; i < logs.size(); i++){
            int count = 0;
            for(int j = 0 ; j < logs.get(i).size(); j++){
                count++;
                double value = (double) logs.get(i).get(j)/(double) logs.size();
                if(averages.containsKey(j)){
                    averages.put(j,averages.get(j)+value);
                }else{
                    averages.put(j,value);
                }
            }
        }
        writeToFile(filename,averages);
    }


    private void writeToFile(String filename, HashMap<Integer,Double> log){
        BufferedWriter writer = null;
        try{
            File file = new File(filename);

            writer = new BufferedWriter(new FileWriter(file));
            for(int i = 0; i < log.size(); i++){
                String record = i + " " + log.get(i).toString();
                writer.write(record);
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally{
            try{
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
