package cs455.hadoop.analyzesongs;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.RawKeyValueIterator;
import org.apache.hadoop.mapred.Task;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;

public class CombineSegments extends Reducer<Text, Text, Text, Text> {

    @Override
    protected void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException{

        String op = key.toString();

        switch (op) {
            case "start":
                averageList(values,context, key.toString());
                break;
            case "pitches":
                averageList(values,context, key.toString());
                break;
            case "timbre":
                averageList(values,context, key.toString());
                break;
            case "loudness":
                averageList(values,context, key.toString());
                break;
            case "loudness_time":
                averageList(values,context, key.toString());
                break;
            case "loudness_start":
                averageList(values,context, key.toString());
                break;
        }
    }


    private void averageList(Iterable<Text> values, Context context, String field) throws IOException, InterruptedException {
        double total = 0;
        long num = 0;

        for (Text val : values) {
            String[] enteries = val.toString().split(" ");
            double curTotal = 0;
            for (int i = 0; i < enteries.length; ++i) {
                curTotal += Double.parseDouble(enteries[i]);
            }

            curTotal /= enteries.length;

            total += curTotal;
            ++num;
        }

        total /= num;

        context.write(new Text(field), new Text("" + total));
    }
}
