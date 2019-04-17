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
    private HashMap<String,String> Q5SongMap;
    private ArrayList<Pair<String, Float>> Q5LengthList;

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
        Q5LengthList = new ArrayList<>();
    }

    @Override
    protected void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
        String op = key.toString();

        switch (op) {
            case "Q1":
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

                context.write(new Text("Artist w/ most songs"), new Text(maxArtist + " (val: " + maxSongs + ", map size: " + Q1Map.size() +")"));
                break;
            case "Q2":
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
                        context.write(new Text("Q2 NF Exception:"), new Text(e.getMessage() + ", val: [" + val.toString() + "]"));
                    } catch (ArrayIndexOutOfBoundsException e) {
                        context.write(new Text("Q2 AIOOB Exception:"), new Text(e.getMessage() + ", val: [" + val.toString() + "]"));
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
                    context.write(new Text(artist + " loudness list size: " + loudnesses.size()), new Text(Arrays.toString(loudnesses.toArray())));
                    float sum = 0;
                    for (float f : loudnesses) {
                        sum += f;
                    }
                    sum = sum / (float)loudnesses.size();
                    context.write(new Text(artist + " loudness avg: " + sum), new Text("curernt max: " + loudestArtist + " (" + maxLoudness + ")"));
                    if (sum > maxLoudness) {
                        loudestArtist = artist;
                        maxLoudness = sum;
                        context.write(new Text("new max"), new Text("yee"));
                    }
                }

                context.write(new Text("Artist w/ loudest songs"), new Text(loudestArtist
                        + " (val: " + maxLoudness + ", loudness map size: " + Q2LoudnessMap.size() + ", artist map size: "
                        + Q2ArtistMap.size() + ", avg loudness map size: " + Q2AverageLoudnessMap.size() + ")"));
                break;
            case "Q3":
                // context.write(new Text("Q3"), new Text("a\t" + record.get("song_id") + "\t" + record.get("song_hotttnesss")));
                // context.write(new Text("Q3"), new Text("m\t" + record.get("song_id") + "\t" + record.get("title") + "\t" + record.get("artist_name")));
                for (Text val : values) {
                    try {
                        String parts[] = val.toString().split("\t");
                        if (parts[0].equals("a")) {
                            Q3HotttnesssMap.put(parts[1], Float.parseFloat(parts[2]));
                        } else if (parts[0].equals("m")) {
                            //              song-id     artist-name
                            Q3ArtistMap.put(parts[1], parts[3]);
                            //              song-id     title
                            Q3SongNameMap.put(parts[1], parts[2]);
                        }
                    } catch (NumberFormatException e) {
                        context.write(new Text("Q3 NF Exception:"), new Text(e.getMessage() + ", val: [" + val.toString() + "]"));
                    } catch (ArrayIndexOutOfBoundsException e) {
                        context.write(new Text("Q3 AIOOB Exception:"), new Text(e.getMessage() + ", val: [" + val.toString() + "]"));
                    }
                }
                String hotttestttArtisttt = "";
                String hottestttSong = "";
                float maxHotttness = -1 * Float.MAX_VALUE;

                for (String song_id : Q3HotttnesssMap.keySet()) {
                    try {
                        context.write(new Text(Q3ArtistMap.get(song_id + " - " + Q3SongNameMap.get(song_id))), new Text("" + Q3HotttnesssMap.get(song_id)));
                        context.write(new Text("current hottest: " + hotttestttArtisttt + " - " + hottestttSong), new Text("" + maxHotttness));
                        float hottness = Q3HotttnesssMap.get(song_id);
                        if (hottness > maxHotttness) {
                            hottestttSong = Q3SongNameMap.get(song_id);
                            hotttestttArtisttt = Q3ArtistMap.get(song_id);
                            context.write(new Text("new max hottness"), new Text("yee"));
                        }
                    } catch (NullPointerException e) {
                        context.write(new Text("Q3 NPE for song id " + song_id), new Text(e.getMessage()));
                    }
                }

                context.write(new Text("Hotttestt song"), new Text(hottestttSong + " by "
                        + hotttestttArtisttt + " (val: " + maxHotttness + ", hotttness map size: " + Q3HotttnesssMap.size()
                        + ", song name map size: " + Q3SongNameMap.size() + ", artist map size: " + Q3ArtistMap.size() + ")"));
                break;
            case "Q4":
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
                        context.write(new Text("Q4 NF Exception"), new Text(e.getMessage() + " val [" + val.toString() + "]"));
                    } catch (ArrayIndexOutOfBoundsException e) {
                        context.write(new Text("Q4 AIOOB Exception"), new Text(e.getMessage() + " val [" + val.toString() + "]"));
                    } catch (NullPointerException e) {
                        context.write(new Text("Q4 NP Exception"), new Text(e.getMessage() + " val [" + val.toString() + "]"));
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
                break;

            case "Q5":
                // context.write(new Text("Q5"), new Text("a\t" + record.get("song_id") + "\t" + record.get("duration")));
                // context.write(new Text("Q5"), new Text("m\t" + record.get("song_id") + "\t" + record.get("title")));
                for (Text val : values) {
                    try {
                        String []parts = val.toString().split("\t");
                        if (parts[0].equals("a")) {
                            Q5LengthMap.put(parts[1], Float.parseFloat(parts[2]));
                        } else if (parts[0].equals("m")) {
                            Q5SongMap.put(parts[1], parts[2]);
                        }
                    } catch (NumberFormatException e) {
                        // do som[h
                    }
                }

                for (String song_id : Q5SongMap.keySet()) {
                    Q5LengthList.add(new Pair<>(Q5SongMap.get(song_id), Q5LengthMap.get(song_id)));
                }

                //Comparator.comparing(Pair::getSecond);

                Collections.sort(Q5LengthList, (Pair<String, Float> x,Pair<String, Float> y) -> x.getSecond().compareTo(y.getSecond()));

                ArrayList<Pair<String, Float>> medians = new ArrayList<>();

                int m = Q5LengthList.size() / 2;
                int lower = m, upper = m;

                while (Q5LengthList.get(lower).getSecond().equals(Q5LengthList.get(m).getSecond())) {
                    medians.add(Q5LengthList.get(lower));
                }

                while (Q5LengthList.get(upper).getSecond().equals(Q5LengthList.get(m).getSecond())) {
                    medians.add(Q5LengthList.get(upper));
                }

                StringBuilder meds = new StringBuilder();

                for (Pair<String, Float> p : medians) {
                    meds.append(p.getFirst() + " (" + p.getSecond() + "), ");
            }


                context.write(new Text("Shortest Song"), new Text(Q5LengthList.get(0).getFirst() + " (" + Q5LengthList.get(0).getSecond() + ")"));

                context.write(new Text("Longest Song"), new Text(Q5LengthList.get(Q5LengthList.size() - 1).getFirst() + " (" + Q5LengthList.get(Q5LengthList.size() - 1).getSecond() + ")"));

                context.write(new Text("Median Length Songs"), new Text(meds.toString()));


        }
    }

    @Override
    protected void cleanup(Context context) throws IOException, InterruptedException {


    }

}
