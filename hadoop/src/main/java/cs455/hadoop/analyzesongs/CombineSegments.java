package cs455.hadoop.analyzesongs;

import org.apache.hadoop.io.Text;
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
        double []total = new double[661];
        long num = 0;

        for (Text val : values) {
            String[] enteries = val.toString().split(" ");
            if (enteries.length < 661) {
                continue;
            }
            for (int i = 0; i < 661; ++i) {
                total[i] += Double.parseDouble(enteries[i]);
            }
            ++num;
        }

        for (int i = 0; i < total.length; ++i) {
            total[i] = total[i] / num;
        }

        StringBuilder doubles = new StringBuilder();

        for (double d :total) {
            doubles.append(d);
            doubles.append(' ');
        }


        context.write(new Text(field), new Text(doubles.toString()));
    }
}
