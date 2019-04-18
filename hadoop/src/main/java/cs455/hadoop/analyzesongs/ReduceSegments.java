package cs455.hadoop.analyzesongs;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;

public class ReduceSegments extends Reducer<Text, Text, Text, Text> {

    @Override
    protected void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException{

        String op = key.toString();

        switch (op) {
            case "start":
                averageList(values,context, "start");
                break;
            case "pitches":
                averageList(values,context, "pitches");
                break;
            case "timbre":
                averageList(values,context, "timbre");
                break;
            case "loudness":
                averageList(values,context, "loudness");
                break;
            case "loudness_time":
                averageList(values,context, "loudness time");
                break;
            case "loudness_start":
                averageList(values,context, "loudness start");
                break;
        }
    }


    private void averageList(Iterable<Text> values, Context context, String field) throws IOException, InterruptedException{
        double total = 0;
        long num = 0;

        for (Text val : values) {
            String []enteries = val.toString().split(" ");
            double curTotal = 0;
            for (int i = 0; i < enteries.length; ++i) {
                curTotal += Double.parseDouble(enteries[i]);
            }

            curTotal /= enteries.length;

            total += curTotal;
            ++num;
        }

        total /= num;

        context.write(new Text("average " + field), new Text("" + total));
    }
}
