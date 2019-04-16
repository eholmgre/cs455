package cs455.hadoop.analyzesongs;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.join.TupleWritable;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

//public class MapAnalysis extends Mapper<Object, Text, Text, DataWritable> {
public class MapAnalysis extends Mapper<Object, Text, Text, Text> {

    @Override
    protected void map(Object key, Text value, Context context) throws IOException, InterruptedException {
        Reader reader = new StringReader(value.toString());
        final CSVParser parser = new CSVParser(reader, CSVFormat.DEFAULT.withRecordSeparator('\n'));
        for (final CSVRecord record : parser) {
            if (record.get(0).equals("")) {
                continue;
            }
            for (String s : "Q1 Q2 Q3 Q4 Q5 Q6".split(" ")) {
                /*
                context.write(new Text(s), new AnalysisWritable(record.get(0), record.get(1), record.get(3),
                        record.get(4), record.get(5), record.get(6), record.get(7), record.get(9),
                        record.get(10), record.get(12), record.get(13), record.get(14), record.get(16),
                        record.get(17), record.get(19), record.get(20), record.get(21), record.get(22), record.get(23)));
                        */

                context.write(new Text(s), new Text("analysis\t" + record.get(1) + "\t" + record.get(2) + "\t" +record.get(4)+ "\t" +
                        record.get(5) + "\t" +  record.get(6) + "\t" +  record.get(7) + "\t" +  record.get(8) + "\t" +  record.get(10) + "\t" +
                        record.get(11) + "\t" +  record.get(13) + "\t" +  record.get(14) + "\t" +  record.get(15) + "\t" +  record.get(17) + "\t" +
                        record.get(18) + "\t" +  record.get(20) + "\t" +  record.get(21) + "\t" +  record.get(22) + "\t" +  record.get(23) + "\t" +  record.get(24)));
            }
        }
    }
}
