package cs455.hadoop.analyzesongs;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

public class MapMetadata extends Mapper<Object, Text, Text, Text> {

    @Override
    public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
        Reader reader = new StringReader(value.toString());
        final CSVParser parser = new CSVParser(reader, CSVFormat.DEFAULT.withRecordSeparator('\n'));
        for (final CSVRecord record : parser) {
            if (record.get(0).equals(""))
            for (String s : "Q1 Q2 Q3 Q4 Q5 Q6".split(" ")) {
                context.write(new Text(s), new Text("metadata\t" + record.get(1) + "\t" + record.get(2) + "\t" + record.get(3) + "\t" +
                        record.get(7) + "\t" + record.get(8) + "\t" + record.get(9) + "\t" + record.get(10) + "\t" + record.get(11) + "\t" +
                        record.get(12) + "\t" + record.get(13) + "\t" + record.get(14)));
            }
        }
    }
}
