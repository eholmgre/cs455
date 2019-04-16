package cs455.hadoop.analyzesongs;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;

public class ReduceJoiner extends Reducer<Text, Text, Text, Text>{
    public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
        for (Text t : values) {
            String record = t.toString();
            String []parts = record.split(",");
            if (parts[0].contains("metadata")) {
                context.write(new Text("metadata"), new Text(record));
            } else if (parts[0].contains("analysis")) {
                context.write(new Text("analysis"), new Text(record));
            } else {
                context.write(new Text("what"), new Text(record));
                //aww hell what happened
            }
        }
    }
}
