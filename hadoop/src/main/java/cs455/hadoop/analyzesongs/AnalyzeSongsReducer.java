package cs455.hadoop.analyzesongs;

import com.sun.org.apache.bcel.internal.generic.FLOAD;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;
import java.util.HashMap;

/**
 * Reducer: Input to the reducer is the output from the mapper. It receives word, list<count> pairs.
 * Sums up individual counts per given word. Emits <word, total count> pairs.
 */
public class AnalyzeSongsReducer extends Reducer<Text, Text, Text, Text> {
    private HashMap<String, Integer> Q1Map;

    private HashMap<String, Float> Q2LoudnessMap;
    private HashMap<String, String> Q2ArtistMap;
    private HashMap<String, Float> Q2AverageLoudnessMap;

    private HashMap<String, Float> Q3HotttnesssMap;
    private HashMap<String, String> Q3ArtistMap;
    private HashMap<String, String> Q3SongNameMap;

    @Override
    protected void setup(Context context) {
        Q1Map = new HashMap<>();
        Q2LoudnessMap = new HashMap<>();
        Q2ArtistMap = new HashMap<>();
        Q2AverageLoudnessMap = new HashMap<>();
        Q3HotttnesssMap = new HashMap<>();
        Q3ArtistMap = new HashMap<>();
    }

    @Override
    protected void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
        String op = key.toString();
        switch (op) {
            case "Q1":
                for (Text val : values) {
                    String parts[] = val.toString().split("\t");
                    if (parts[0].equals("metadata")) {
                        String artist = parts[4];
                        if (Q1Map.containsKey(artist)) {
                            int cur = Q1Map.get(artist);
                            Q1Map.replace(artist, cur + 1);
                        } else {
                            Q1Map.put(artist, 1);
                        }
                    }
                }
                break;
            case "Q2":
                for (Text val : values) {
                    try {
                        String parts[] = val.toString().split("\t");
                        if (parts[0].equals("analysis")) {
                            Q2LoudnessMap.put(parts[1], Float.parseFloat(parts[8]));
                        } else if (parts[0].equals("metadata")) {
                            Q2ArtistMap.put(parts[5], parts[4]);
                        }
                    } catch (NumberFormatException e) {
                        continue;
                    }
                }
                break;
            case "Q3":
                for (Text val : values) {
                    try {
                        String parts[] = val.toString().split("\t");
                        if (parts[0].equals("analysis")) {
                            String song_id = parts[1];
                            float song_hotttnesss = Float.parseFloat(parts[2]);
                            Q3HotttnesssMap.put(song_id, song_hotttnesss);
                        } else if (parts[0].equals("metadata")) {
                            Q3ArtistMap.put(parts[5], parts[4]);
                        }
                    } catch (NumberFormatException e) {
                        continue;
                    }
                }
        }
    }

    @Override
    protected void cleanup(Context context) throws IOException, InterruptedException {
        String maxArtist = "";
        int maxSongs = 0;
        for (String artist : Q1Map.keySet()) {
            if (Q1Map.get(artist) > maxSongs) {
                maxArtist = artist;
                maxSongs = Q1Map.get(artist);
            }
        }

        context.write(new Text("Artist w/ most songs"), new Text(maxArtist + "(" + maxSongs + ")"));

        for (String song_id : Q2LoudnessMap.keySet()) {
            float loudness = Q2LoudnessMap.get(song_id);
            String artist = Q2ArtistMap.get(song_id);
            if (Q2AverageLoudnessMap.containsKey(artist)) {
                float current = Q2AverageLoudnessMap.get(artist);
                Q2AverageLoudnessMap.replace(artist, (float) ((loudness + current) / 2.0));
            } else {
                Q2AverageLoudnessMap.put(artist, loudness);
            }
        }

        String loudestArtist = "";
        float maxLoudness = Float.MIN_VALUE;

        for (String artist : Q2AverageLoudnessMap.keySet()) {
            if (Q2AverageLoudnessMap.get(artist) > maxLoudness) {
                loudestArtist = artist;
                maxLoudness = Q2AverageLoudnessMap.get(artist);
            }
        }

        context.write(new Text("Artist w/ loudest songs"), new Text(loudestArtist + "(" + maxLoudness + ")"));

        String hotttestttArtisttt = "";
        float maxHotttness = Float.MIN_VALUE;

        for (String )
    }
}
