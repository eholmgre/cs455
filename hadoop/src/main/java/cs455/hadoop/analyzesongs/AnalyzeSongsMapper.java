package cs455.hadoop.analyzesongs;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;
import java.util.List;

/**
 * Mapper: Reads line by line, split them into words. Emit <word, 1> pairs.
 */
public class AnalyzeSongsMapper extends Mapper<LongWritable, Text, Text, Text> {

    private int keyIndex;
    private Splitter splitter;
    private Joiner joiner;
    private Text joinKey = new Text();

    @Override
    protected void setup(Context context) throws IOException, InterruptedException{
        String sep = context.getConfiguration().get("separator");
        splitter = Splitter.on(sep);
        joiner = joiner.on(sep);
    }

    @Override
    protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
        Iterable<String> values = splitter.split(value.toString());
        joinKey.set(Iterables.get(values, keyIndex));
        if (keyIndex != 0) {
            value.set(reorderValue(values, keyIndex));
        }

        context.write(joinKey, value);
    }

    private String reorderValue(Iterable<String> value, int index) {
        List<String> temp = Lists.newArrayList(value);
        String originalFirst = temp.get(0);
        String newFirst = temp.get(index);
        temp.set(0, newFirst);
        temp.set(index, originalFirst);
        return joiner.join(temp);
    }
}
