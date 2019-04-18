package cs455.hadoop.analyzesongs;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

public class MapSegments extends Mapper<Object, Text, Text, Text> {
    @Override
    protected void map(Object key, Text value, Context context) throws IOException, InterruptedException {

        String analysisHeader = ",song_id,song_hotttnesss,analysis_sample_rate,danceability,duration,end_of_fade_in,energy,key,key_confidence,loudness,mode,mode_confidence,start_of_fade_out,tempo,time_signature,time_signature_confidence,track_id,segments_start,segments_confidence,segments_pitches,segments_timbre,segments_loudness_max,segments_loudness_max_time,segments_loudness_start,sections_start,sections_confidence,beats_start,beats_confidence,bars_start,bars_confidence,tatums_start,tatums_confidence";
        Reader reader = new StringReader(analysisHeader + '\n' + value.toString());
        final CSVParser parser = new CSVParser(reader, CSVFormat.DEFAULT.withRecordSeparator('\n').withHeader());
        for (final CSVRecord record : parser) {
            if (record.get("segments_start").split(" ").length == 661) {
                context.write(new Text("start"), new Text(record.get("segments_start")));
                context.write(new Text("pitches"), new Text(record.get("segments_pitches")));
                context.write(new Text("timbre"), new Text(record.get("segments_timbre")));
                context.write(new Text("loudness"), new Text(record.get("segments_loudness_max")));
                context.write(new Text("loudness_time"), new Text(record.get("segments_loudness_max_time")));
                context.write(new Text("loudness_start"), new Text(record.get("segments_loudness_start")));
            }
        }

    }
}
