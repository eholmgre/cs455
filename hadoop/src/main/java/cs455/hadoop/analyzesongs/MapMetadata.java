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

            context.write(new Text("Q1"), new Text("m\t" + record.get("artist_name")));

            context.write(new Text("Q2"), new Text("m\t" + record.get("song_id") + "\t" + record.get("artist_name")));

            context.write(new Text("Q3"), new Text("m\t" + record.get("song_id") + "\t" + record.get("title") + "\t" + record.get("artist_name")));

            context.write(new Text("Q4"), new Text("m\t" + record.get("song_id") + "\t" + record.get("artist_name")));

            context.write(new Text("Q5"), new Text("m\t" + record.get("song_id") + "\t" + record.get("title") + "\t" + record.get("artist_name")));

            context.write(new Text("Q6"), new Text("m\t" + record.get("song_id") + "\t" + record.get("title") + "\t" + record.get("artist_name")));

            if (!record.get("similar_artists").equals("")) {
                context.write(new Text("Q8"), new Text("m\t" + record.get("artist_name") + "\t" + record.get("similar_artists")));
                context.write(new Text("Q10"), new Text(record.get("artist_name") + "\t" + record.get("artist_id") + "\t" +  record.get("similar_artists")));
            }

            if (!record.get("artist_terms").equals("")) {
                context.write(new Text("Q9"), new Text("m\t" + record.get("song_id") + "\t" + record.get("artist_name") + "\t" + record.get("title") + record.get("artist_terms")));
            }
        }
    }
}
