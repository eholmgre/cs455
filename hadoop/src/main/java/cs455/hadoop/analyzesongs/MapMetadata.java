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
        String metadataHeader = ",artist_familiarity,artist_hotttnesss,artist_id,artist_latitude,artist_longitude,artist_location,artist_name,song_id,title,similar_artists,artist_terms,artist_terms_freq,artist_terms_weight,year";
        Reader reader = new StringReader(metadataHeader + '\n' + value.toString());
        final CSVParser parser = new CSVParser(reader, CSVFormat.DEFAULT.withRecordSeparator('\n').withHeader());
        for (final CSVRecord record : parser) {

            if (record.get("song_id").equals("song_id")) {
                continue;
            }

//            context.write(new Text("Q1"), new Text("m\t" + record.get("artist_name")));

//            context.write(new Text("Q2"), new Text("m\t" + record.get("song_id") + "\t" + record.get("artist_name")));

            context.write(new Text("Q3"), new Text("m\t" + record.get("song_id") + "\t" + record.get("title") + "\t" + record.get("artist_name")));

//            context.write(new Text("Q4"), new Text("m\t" + record.get("song_id") + "\t" + record.get("artist_name")));

            context.write(new Text("Q5"), new Text("m\t" + record.get("song_id") + "\t" + record.get("title") + "\t" + record.get("artist_name")));

            context.write(new Text("Q6"), new Text("m\t" + record.get("song_id") + "\t" + record.get("title") + "\t" + record.get("artist_name")));

            /*
            for (String s : "Q1 Q2 Q3 Q4 Q5 Q6".split(" ")) {
                context.write(new Text(s), new Text("metadata\t" + record.get(1) + "\t" + record.get(2) + "\t" + record.get(3) + "\t" +
                        record.get(7) + "\t" + record.get(8) + "\t" + record.get(9) + "\t" + record.get(10) + "\t" + record.get(11) + "\t" +
                        record.get(12) + "\t" + record.get(13) + "\t" + record.get(14)));
            }
            */
        }
    }
}
