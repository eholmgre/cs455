package cs455.hadoop.analyzesongs;

import org.apache.commons.math3.util.Pair;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;
import java.util.*;

/**
 * Reducer: Input to the reducer is the output from the mapper. It receives word, list<count> pairs.
 * Sums up individual counts per given word. Emits <word, total count> pairs.
 */
public class AnalyzeSongsReducer extends Reducer<Text, Text, Text, Text> {
    private HashMap<String, Integer> Q1Map;

    private HashMap<String, Float> Q2LoudnessMap;
    private HashMap<String, String> Q2ArtistMap;
    private HashMap<String, ArrayList<Float>> Q2AverageLoudnessMap;

    private HashMap<String, Float> Q3HotttnesssMap;
    private HashMap<String, String> Q3ArtistMap;
    private HashMap<String, String> Q3SongNameMap;

    private HashMap<String, Float> Q4FadeTimeMap;
    private HashMap<String, String> Q4ArtistMap;
    private HashMap<String, Float> Q4TotalFadeTimeMap;

    private HashMap<String, Float> Q5LengthMap;
    private HashMap<String, String> Q5SongMap;
    private HashMap<String, String> Q5ArtistMap;
    private ArrayList<Pair<String, Float>> Q5LengthList;

    private HashMap<String, Float> Q6EnergyMap;
    private HashMap<String, Float> Q6DanceabilityMap;
    private HashMap<String, String> Q6SongNameMap;
    private HashMap<String, String> Q6ArtistMap;
    private ArrayList<Pair<String, Float>> Q6EnergyList;
    private ArrayList<Pair<String, Float>> Q6DanceabilityList;

    private HashMap<String, String[]> Q8SimilarArtistsMap;

    private HashMap<String, Float> Q9HotttnessMap;
    private HashMap<String, String> Q9MetaMap;
    private HashMap<String, String> Q9AnyzMap;

    private HashMap<Integer, Integer> T7ModeMap;

    @Override
    protected void setup(Context context) {
        //todo: find a good init value for these maps
        Q1Map = new HashMap<>();
        Q2LoudnessMap = new HashMap<>();
        Q2ArtistMap = new HashMap<>();
        Q2AverageLoudnessMap = new HashMap<>();
        Q3HotttnesssMap = new HashMap<>();
        Q3ArtistMap = new HashMap<>();
        Q3SongNameMap = new HashMap<>();
        Q4FadeTimeMap = new HashMap<>();
        Q4ArtistMap = new HashMap<>();
        Q4TotalFadeTimeMap = new HashMap<>();
        Q5LengthMap = new HashMap<>();
        Q5SongMap = new HashMap<>();
        Q5ArtistMap = new HashMap<>();
        Q5LengthList = new ArrayList<>();
        Q6EnergyMap = new HashMap<>();
        Q6DanceabilityMap = new HashMap<>();
        Q6SongNameMap = new HashMap<>();
        Q6ArtistMap = new HashMap<>();
        Q6EnergyList = new ArrayList<>();
        Q6DanceabilityList = new ArrayList<>();
        Q8SimilarArtistsMap = new HashMap<>();
        Q9HotttnessMap = new HashMap<>();
        Q9MetaMap = new HashMap<>();
        Q9AnyzMap = new HashMap<>();
        T7ModeMap = new HashMap<>();
    }

    @Override
    protected void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
        String op = key.toString();

        switch (op) {
            case "Q1":
                reduceQ1(context, values);
                break;
            case "Q2":
                reduceQ2(context, values);
                break;
            case "Q3":
                reduceQ3(context, values);
                break;
            case "Q4":
                reduceQ4(context, values);
                break;
            case "Q5":
                reduceQ5(context, values);
                break;
            case "Q6":
                reduceQ6(context, values);
                break;
            case "Q8":
                reduceQ8(context, values);
                break;
            case "Q9":
                reduceQ9(context, values);
                break;
            case "T7":
                for (Text val : values) {
                    int parsed = Integer.parseInt(val.toString());
                    int cur = 0;
                    if (T7ModeMap.keySet().contains(parsed)) {
                        cur = T7ModeMap.get(parsed);
                    }

                    T7ModeMap.put(parsed, cur + 1);
                }

                int mode = 0;
                int cnt = 0;

                for (Integer val : T7ModeMap.keySet()) {
                    int cur = T7ModeMap.get(val);

                    if (cur > cnt) {
                        mode = val;
                        cnt = cur;
                    }
                }

                context.write(new Text("mode num segs"), new Text("" + mode));
                break;
            case "Q10":
                for (Text val : values) {
                    String parts[] = val.toString().split("\t");
                    context.write(new Text(parts[1] + " (" + parts[0] + ")"), new Text(parts[2]));
                }
        }
    }


    private void reduceQ1(Context context, Iterable<Text> values) throws IOException, InterruptedException {
        // context.write(new Text("Q1"), new Text("m\t" + record.get("artist_name")));
        for (Text val : values) {
            String parts[] = val.toString().split("\t");
            if (parts[0].equals("m")) {
                if (Q1Map.containsKey(parts[1])) {
                    int cur = Q1Map.get(parts[1]);
                    Q1Map.replace(parts[1], cur + 1);
                } else {
                    Q1Map.put(parts[1], 1);
                }
            }
        }
        String maxArtist = "";
        int maxSongs = 0;
        for (String artist : Q1Map.keySet()) {
            if (Q1Map.get(artist) > maxSongs) {
                maxArtist = artist;
                maxSongs = Q1Map.get(artist);
            }
        }

        context.write(new Text("Artist w/ most songs"), new Text(maxArtist + " (val: " + maxSongs + ", map size: " + Q1Map.size() + ")"));
    }

    private void reduceQ2(Context context, Iterable<Text> values) throws IOException, InterruptedException {

        // context.write(new Text("Q2"), new Text("a\t" + record.get("song_id") + "\t" + record.get("loudness")));
        // context.write(new Text("Q2"), new Text("m\t" + record.get("song_id") + "\t" + record.get("artist_name")));
        for (Text val : values) {
            try {
                String parts[] = val.toString().split("\t");
                if (parts[0].equals("a")) {
                    //                  song_id     loudness
                    Q2LoudnessMap.put(parts[1], Float.parseFloat(parts[2]));
                } else if (parts[0].equals("m")) {
                    //              song_id         artist_name
                    Q2ArtistMap.put(parts[1], parts[2]);
                }
            } catch (NumberFormatException e) {
                //context.write(new Text("Q2 NF Exception:"), new Text(e.getMessage() + ", val: [" + val.toString() + "]"));
            } catch (ArrayIndexOutOfBoundsException e) {
                //context.write(new Text("Q2 AIOOB Exception:"), new Text(e.getMessage() + ", val: [" + val.toString() + "]"));
            }
        }
        for (String song_id : Q2LoudnessMap.keySet()) {
            float loudness = Q2LoudnessMap.get(song_id);
            String artist = Q2ArtistMap.get(song_id);
            if (!Q2AverageLoudnessMap.containsKey(artist)) {
                Q2AverageLoudnessMap.put(artist, new ArrayList<>());
            }
            Q2AverageLoudnessMap.get(artist).add(loudness);
        }

        String loudestArtist = "";
        float maxLoudness = -1 * Float.MAX_VALUE;

        for (String artist : Q2AverageLoudnessMap.keySet()) {
            ArrayList<Float> loudnesses = Q2AverageLoudnessMap.get(artist);
            //context.write(new Text(artist + " loudness list size: " + loudnesses.size()), new Text(Arrays.toString(loudnesses.toArray())));
            float sum = 0;
            for (float f : loudnesses) {
                sum += f;
            }
            sum = sum / (float) loudnesses.size();
            //context.write(new Text(artist + " loudness avg: " + sum), new Text("curernt max: " + loudestArtist + " (" + maxLoudness + ")"));
            if (sum > maxLoudness) {
                loudestArtist = artist;
                maxLoudness = sum;
                //context.write(new Text("new max"), new Text("yee"));
            }
        }

        context.write(new Text("Artist w/ loudest songs"), new Text(loudestArtist
                + " (val: " + maxLoudness + ", loudness map size: " + Q2LoudnessMap.size() + ", artist map size: "
                + Q2ArtistMap.size() + ", avg loudness map size: " + Q2AverageLoudnessMap.size() + ")"));
    }

    private void reduceQ3(Context context, Iterable<Text> values) throws IOException, InterruptedException {

        // context.write(new Text("Q3"), new Text("a\t" + record.get("song_id") + "\t" + record.get("song_hotttnesss")));
        // context.write(new Text("Q3"), new Text("m\t" + record.get("song_id") + "\t" + record.get("title") + "\t" + record.get("artist_name")));
        for (Text val : values) {
            try {
                String parts[] = val.toString().split("\t");
                if (parts[0].equals("a")) {
                    float hottness = Float.parseFloat(parts[2]);
                    if (hottness < 0) {
                        context.write(new Text("hottness wrong: "), new Text(val.toString()));
                    }
                    Q3HotttnesssMap.put(parts[1], hottness);
                } else if (parts[0].equals("m")) {
                    //              song-id     title
                    Q3SongNameMap.put(parts[1], parts[2]);
                    //              song-id     artist-name
                    Q3ArtistMap.put(parts[1], parts[3]);
                }
            } catch (NumberFormatException e) {
                //context.write(new Text("Q3 NF Exception:"), new Text(e.getMessage() + ", val: [" + val.toString() + "]"));
            } catch (ArrayIndexOutOfBoundsException e) {
                //context.write(new Text("Q3 AIOOB Exception:"), new Text(e.getMessage() + ", val: [" + val.toString() + "]"));
            }
        }
        String hotttestttArtisttt = "";
        String hottestttSong = "";
        float maxHotttness = 0;

        for (String song_id : Q3HotttnesssMap.keySet()) {
            try {
                //context.write(new Text(Q3ArtistMap.get(song_id + " - " + Q3SongNameMap.get(song_id))), new Text("" + Q3HotttnesssMap.get(song_id)));
                //context.write(new Text("current hottest: " + hotttestttArtisttt + " - " + hottestttSong), new Text("" + maxHotttness));
                float hottness = Q3HotttnesssMap.get(song_id);
                if (hottness > maxHotttness) {
                    hottestttSong = Q3SongNameMap.get(song_id);
                    hotttestttArtisttt = Q3ArtistMap.get(song_id);
                    maxHotttness = hottness;
                    //context.write(new Text("new max hottness"), new Text("yee"));
                }
            } catch (NullPointerException e) {
                //context.write(new Text("Q3 NPE for song id " + song_id), new Text(e.getMessage()));
            }
        }

        context.write(new Text("Hotttestt song"), new Text(hottestttSong + " by "
                + hotttestttArtisttt + " (val: " + maxHotttness + ", hotttness map size: " + Q3HotttnesssMap.size()
                + ", song name map size: " + Q3SongNameMap.size() + ", artist map size: " + Q3ArtistMap.size() + ")"));
    }

    private void reduceQ4(Context context, Iterable<Text> values) throws IOException, InterruptedException {

        // context.write(new Text("Q4"), new Text("a\t" + record.get("song_id") + "\t" + record.get("end_of_fade_in") + "\t" + record.get("duration") + "\t" + record.get("start_of_fade_out")));
        // context.write(new Text("Q4"), new Text("m\t" + record.get("song_id") + "\t" + record.get("artist_name")));
        for (Text val : values) {
            try {
                String[] parts = val.toString().split("\t");
                if (parts[0].equals("a")) {
                    float end_fade_in = Float.parseFloat(parts[2]);
                    float duration = Float.parseFloat(parts[3]);
                    float start_fade_out = Float.parseFloat(parts[4]);
                    Q4FadeTimeMap.put(parts[1], end_fade_in + duration - start_fade_out);
                } else if (parts[0].equals("m")) {
                    Q4ArtistMap.put(parts[1], parts[2]);
                }
            } catch (NumberFormatException e) {
                //context.write(new Text("Q4 NF Exception"), new Text(e.getMessage() + " val [" + val.toString() + "]"));
            } catch (ArrayIndexOutOfBoundsException e) {
                //context.write(new Text("Q4 AIOOB Exception"), new Text(e.getMessage() + " val [" + val.toString() + "]"));
            } catch (NullPointerException e) {
                //context.write(new Text("Q4 NP Exception"), new Text(e.getMessage() + " val [" + val.toString() + "]"));
            }
        }

        float fadingMax = -1 * Float.MAX_VALUE;
        String fadingArtist = "";

        for (String song_id : Q4FadeTimeMap.keySet()) {
            String artist = Q4ArtistMap.get(song_id);

            float currentFade = 0;
            if (Q4TotalFadeTimeMap.keySet().contains(artist)) {
                currentFade = Q4TotalFadeTimeMap.get(artist);
            }

            currentFade += Q4FadeTimeMap.get(song_id);

            if (currentFade > fadingMax) {
                fadingMax = currentFade;
                fadingArtist = artist;
            }

            Q4TotalFadeTimeMap.put(artist, currentFade);

        }

        context.write(new Text("Artist with most fading: "), new Text(fadingArtist + " (" + fadingMax + ")"));
    }

    private void reduceQ5(Context context, Iterable<Text> values) throws IOException, InterruptedException {

        // context.write(new Text("Q5"), new Text("a\t" + record.get("song_id") + "\t" + record.get("duration")));
        // context.write(new Text("Q5"), new Text("m\t" + record.get("song_id") + "\t" + record.get("title") + artist name));
        for (Text val : values) {
            try {
                String[] parts = val.toString().split("\t");
                if (parts[0].equals("a")) {
                    Q5LengthMap.put(parts[1], Float.parseFloat(parts[2]));
                } else if (parts[0].equals("m")) {
                    Q5SongMap.put(parts[1], parts[2]);
                    Q5ArtistMap.put(parts[1], parts[3]);
                }
            } catch (NumberFormatException e) {
                // do som[h
            }
        }

        for (String song_id : Q5LengthMap.keySet()) {
            Q5LengthList.add(new Pair<>(song_id, Q5LengthMap.get(song_id)));
        }

        Q5LengthList.sort((Pair<String, Float> x, Pair<String, Float> y) -> x.getSecond().compareTo(y.getSecond()));

        ArrayList<Pair<String, Float>> medians = new ArrayList<>();

        int m = Q5LengthList.size() / 2;
        int lower = m;
        int upper = m;

        while (Q5LengthList.get(lower).getSecond().equals(Q5LengthList.get(m).getSecond())) {
            medians.add(Q5LengthList.get(lower));
            --lower;
        }

        while (Q5LengthList.get(upper).getSecond().equals(Q5LengthList.get(m).getSecond())) {
            medians.add(Q5LengthList.get(upper));
            ++upper;
        }

        context.write(new Text("num songs w/ med length"), new Text("" + medians.size()));

        StringBuilder meds = new StringBuilder();

        for (Pair<String, Float> p : medians) {
            meds.append(Q5ArtistMap.get(p.getFirst()) + " - " + Q5SongMap.get(p.getFirst()) + " (" + p.getSecond() + "), ");
        }
        context.write(new Text("Shortest Song"), new Text(Q5ArtistMap.get(Q5LengthList.get(0).getFirst()) + " - " + Q5SongMap.get(Q5LengthList.get(0).getFirst()) + " (" + Q5LengthList.get(0).getSecond() + ")"));

        context.write(new Text("Longest Song"), new Text(Q5ArtistMap.get(Q5LengthList.get(Q5LengthList.size() - 1).getFirst()) + " - " + Q5SongMap.get(Q5LengthList.get(Q5LengthList.size() - 1).getFirst()) + " (" + Q5LengthList.get(Q5LengthList.size() - 1).getSecond() + ")"));

        context.write(new Text("Median Length Songs"), new Text(meds.toString()));
    }

    private void reduceQ6(Context context, Iterable<Text> values) throws IOException, InterruptedException {

        // context.write(new Text("Q6"), new Text("m\t" + record.get("song_id") + "\t" + record.get("title") + "\t" + record.get("artist_name")));
        // context.write(new Text("Q6"), new Text("a\t" + record.get("song_id") + "\te\t" + record.get("energy")));
        // context.write(new Text("Q6"), new Text("a\t" + record.get("song_id") + "\td\t" + record.get("danceability")));
        for (Text val : values) {
            try {
                String[] parts = val.toString().split("\t");
                if (parts[0].equals("a")) {
                    if (parts[2].equals("e")) {
                        Q6EnergyMap.put(parts[1], Float.parseFloat(parts[3]));
                    } else if (parts[2].equals("d")) {
                        Q6DanceabilityMap.put(parts[1], Float.parseFloat(parts[3]));
                    }
                } else if (parts[0].equals("m")) {
                    Q6SongNameMap.put(parts[1], parts[2]);
                    Q6ArtistMap.put(parts[1], parts[3]);
                }
            } catch (NumberFormatException e) {
                //context.write(new Text("Q6 NF Exception"), new Text(e.getMessage() + " val [" + val.toString() + "]"));
            } catch (ArrayIndexOutOfBoundsException e) {
                //context.write(new Text("Q6 AIOOB Exception"), new Text(e.getMessage() + " val [" + val.toString() + "]"));
            } catch (NullPointerException e) {
                //context.write(new Text("Q6 NP Exception"), new Text(e.getMessage() + " val [" + val.toString() + "]"));
            }
        }

        float minMaxEnergy = 0;
        float minMaxDanceability = 0;


        for (String song_id : Q6EnergyMap.keySet()) {
            float energy = Q6EnergyMap.get(song_id);

            if (energy > minMaxEnergy || Q6EnergyList.size() < 10) {
                Q6EnergyList.add(new Pair<>(song_id, energy));
                Collections.sort(Q6EnergyList, (Pair<String, Float> x, Pair<String, Float> y) -> x.getSecond().compareTo(y.getSecond()));
                while (Q6EnergyList.size() > 10) {
                    Q6EnergyList.remove(0);
                }
                minMaxEnergy = Q6EnergyList.get(0).getSecond();
            }
        }

        for (String song_id : Q6DanceabilityMap.keySet()) {
            float danceablility = Q6DanceabilityMap.get(song_id);

            if (danceablility > minMaxDanceability || Q6DanceabilityList.size() < 10) {
                Q6DanceabilityList.add(new Pair<>(song_id, danceablility));
                Collections.sort(Q6DanceabilityList, (Pair<String, Float> x, Pair<String, Float> y) -> x.getSecond().compareTo(y.getSecond()));
                while (Q6DanceabilityList.size() > 10) {
                    Q6DanceabilityList.remove(0);
                }
                minMaxDanceability = Q6DanceabilityList.get(0).getSecond();
            }

        }

        Collections.reverse(Q6EnergyList);

        StringBuilder energyBuilder = new StringBuilder();
        for (Pair<String, Float> energy : Q6EnergyList) {
            energyBuilder.append(Q6ArtistMap.get(energy.getFirst()) + " - " + Q6SongNameMap.get(energy.getFirst())
                    + " (" + energy.getSecond() + "), ");
        }

        Collections.reverse(Q6DanceabilityList);

        StringBuilder danceBuilder = new StringBuilder();
        for (Pair<String, Float> dance : Q6DanceabilityList) {
            danceBuilder.append(Q6ArtistMap.get(dance.getFirst()) + " - " + Q6SongNameMap.get(dance.getFirst())
                    + " (" + dance.getSecond() + "), ");
        }

        context.write(new Text("Top 10 songs w/ max energy"), new Text(energyBuilder.toString()));
        context.write(new Text("Top 10 songs w/ max danceability"), new Text(danceBuilder.toString()));
    }

    private void reduceQ8(Context context, Iterable<Text> values) throws IOException, InterruptedException {

        //context.write(new Text("Q8"), new Text("m\t" + record.get("artist_name") + "\t" + record.get("similar_artists")));
        String mostUniqueArtist = "";
        int mostUniqueSimilar = Integer.MAX_VALUE;

        String leastUniqueArtist = "";
        int leastUniqueSimilar = -1;
        for (Text val : values) {
            String[] parts = val.toString().split("\t");
            if (!Q8SimilarArtistsMap.keySet().contains(parts[1])) {
                String[] similar = parts[2].split(" ");
                Q8SimilarArtistsMap.put(parts[1], parts[2].split(" "));

                if (similar.length > leastUniqueSimilar) {
                    leastUniqueArtist = parts[1];
                    leastUniqueSimilar = similar.length;
                }

                if (similar.length < mostUniqueSimilar) {
                    mostUniqueArtist = parts[1];
                    mostUniqueSimilar = similar.length;
                }
            }
        }

        context.write(new Text("least unique artist"), new Text(leastUniqueArtist + " (" + leastUniqueSimilar + ")"));
        context.write(new Text("most unique artist"), new Text(mostUniqueArtist + " (" + mostUniqueSimilar + ")"));
    }

    private void reduceQ9(Context context, Iterable<Text> values) throws IOException, InterruptedException {

        for (Text val : values) {
            try {
                String parts[] = val.toString().split("\t");
                if (parts[0].equals("a")) {
                    float hottness = Float.parseFloat(parts[2]);
                    Q9HotttnessMap.put(parts[1], hottness);
                    Q9AnyzMap.put(parts[1], parts[2] + ';' + parts[3] + ';' + parts[4] + ';' + parts[5] + ';' + parts[6] + ';' + parts[7] + ';' + parts[8] + ';' + parts[9] + ';' + parts[10] + ';' + parts[11] + ';' + parts[12]);
                } else if (parts[0].equals("m")) {
                    Q9MetaMap.put(parts[1], parts[2] + ';' + parts[3] + ';' + parts[4]);
                }
            } catch (NumberFormatException e) {
                //context.write(new Text("Q3 NF Exception:"), new Text(e.getMessage() + ", val: [" + val.toString() + "]"));
            } catch (ArrayIndexOutOfBoundsException e) {
                //context.write(new Text("Q3 AIOOB Exception:"), new Text(e.getMessage() + ", val: [" + val.toString() + "]"));
            }
        }

        context.write(new Text("hot map size: " + Q9HotttnessMap.size()), new Text("anyz map size: " + Q9AnyzMap.size() + " meta map size: " + Q9MetaMap.size()));

        float minMaxHotttnesss = 0;

        ArrayList<Pair<Float, String>> hottnessPatternsList = new ArrayList<>();


        for (String song_id : Q9HotttnessMap.keySet()) {
            float hot = Q9HotttnessMap.get(song_id);

            if ((hot > minMaxHotttnesss || hottnessPatternsList.size() < 20) && Q9MetaMap.keySet().contains(song_id)) {
                hottnessPatternsList.add(new Pair<>(hot, song_id));
                Collections.sort(hottnessPatternsList, (Pair<Float, String> x, Pair<Float, String> y) -> x.getFirst().compareTo(y.getFirst()));
                while (hottnessPatternsList.size() > 20) {
                    hottnessPatternsList.remove(0);
                }
                minMaxHotttnesss = hottnessPatternsList.get(0).getFirst();
            }
        }

        Collections.reverse(hottnessPatternsList);

        StringBuilder hottnessBuilder = new StringBuilder();
        for (Pair<Float, String> hott : hottnessPatternsList) {
            hottnessBuilder.append(hott.getFirst() + ';' + Q9MetaMap.get(hott.getSecond()) + ';' + Q9AnyzMap.get(hott.getSecond()) + '\n');
        }

        context.write(new Text("data for top 20 hot songs"), new Text(hottnessBuilder.toString()));
    }

    @Override
    protected void cleanup(Context context) throws IOException, InterruptedException {


    }

}
