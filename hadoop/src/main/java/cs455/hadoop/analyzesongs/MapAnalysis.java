package cs455.hadoop.analyzesongs;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

//public class MapAnalysis extends Mapper<Object, Text, Text, DataWritable> {
public class MapAnalysis extends Mapper<Object, Text, Text, Text> {

    @Override
    protected void map(Object key, Text value, Context context) throws IOException, InterruptedException {
        String analysisHeader = ",song_id,song_hotttnesss,analysis_sample_rate,danceability,duration,end_of_fade_in,energy,key,key_confidence,loudness,mode,mode_confidence,start_of_fade_out,tempo,time_signature,time_signature_confidence,track_id,segments_start,segments_confidence,segments_pitches,segments_timbre,segments_loudness_max,segments_loudness_max_time,segments_loudness_start,sections_start,sections_confidence,beats_start,beats_confidence,bars_start,bars_confidence,tatums_start,tatums_confidence";
        Reader reader = new StringReader(analysisHeader + '\n' + value.toString());
        final CSVParser parser = new CSVParser(reader, CSVFormat.DEFAULT.withRecordSeparator('\n').withHeader());
        for (final CSVRecord record : parser) {

            if (record.get("song_id").equals("song_id")) {
                continue;
            }

//            if (!record.get("loudness").equals("")) {
//                context.write(new Text("Q2"), new Text("a\t" + record.get("song_id") + "\t" + record.get("loudness")));
//            }

            if (!record.get("song_hotttnesss").equals("")) {
                context.write(new Text("Q3"), new Text("a\t" + record.get("song_id") + "\t" + record.get("song_hotttnesss")));
            }

//            if (!record.get("end_of_fade_in").equals("") && !record.get("duration").equals("") && !record.get("start_of_fade_out").equals("")) {
//                context.write(new Text("Q4"), new Text("a\t" + record.get("song_id") + "\t" + record.get("end_of_fade_in") + "\t" + record.get("duration") + "\t" + record.get("start_of_fade_out")));
//            }

            if (!record.get("duration").equals("")) {
                context.write(new Text("Q5"), new Text("a\t" + record.get("song_id") + "\t" + record.get("duration")));
            }

            if (!record.get("energy").equals("") && !record.get("danceability").equals("")) {
                context.write(new Text("Q6"), new Text("a\t" + record.get("song_id") + "\t" + record.get("energy") + "\t" + record.get("danceability")));
            }
            /*
            for (String s : "Q2 Q3 Q4 Q5 Q6".split(" ")) {
                context.write(new Text(s), new Text("analysis\t" + record.get(1) + "\t" + record.get(2) + "\t" +record.get(4)+ "\t" +
                        record.get(5) + "\t" +  record.get(6) + "\t" +  record.get(7) + "\t" +  record.get(8) + "\t" +  record.get(10) + "\t" +
                        record.get(11) + "\t" +  record.get(13) + "\t" +  record.get(14) + "\t" +  record.get(15) + "\t" +
                        record.get(18) + "\t" +  record.get(20) + "\t" +  record.get(21) + "\t" +  record.get(22) + "\t" +  record.get(23) + "\t" +  record.get(24)));
            }
            */
        }
    }
}
