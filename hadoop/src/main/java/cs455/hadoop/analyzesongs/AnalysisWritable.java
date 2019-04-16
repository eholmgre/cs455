package cs455.hadoop.analyzesongs;

import org.apache.hadoop.io.Writable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class AnalysisWritable extends DataWritable implements Writable {

    private String song_id;
    private float song_hotttnesss;
    private float danceability;
    private float duration;
    private float end_of_fade_in;
    private float energy;
    private int key;
    private float loudness;
    private int mode;
    private float start_of_fade_out;
    private float tempo;
    private int time_signature;
    private String track_id;
    private String segments_start;
    private String segments_pitches;
    private String segments_timbre;
    private String segments_loudness_max;
    private String segments_loudness_max_time;
    private String segments_loudness_start;

    public AnalysisWritable(String song_id, String song_hotttnesss, String danceability, String duration,
                            String end_of_fade_in, String energy, String key, String loudness, String mode,
                            String start_of_fade_out, String tempo, String time_signature, String track_id,
                            String segments_start, String segments_pitches, String segments_timbre,
                            String segments_loudness_max, String segments_loudness_max_time,
                            String segments_loudness_start) {
        this.song_id = song_id;
        this.song_hotttnesss = Float.parseFloat(song_hotttnesss);
        this.danceability = Float.parseFloat(danceability);
        this.duration = Float.parseFloat(duration);
        this.end_of_fade_in = Float.parseFloat(end_of_fade_in);
        this.energy = Float.parseFloat(energy);
        this.key = Integer.parseInt(key);
        this.loudness = Float.parseFloat(loudness);
        this.mode = Integer.parseInt(mode);
        this.start_of_fade_out = Float.parseFloat(start_of_fade_out);
        this.tempo = Float.parseFloat(tempo);
        this.time_signature = Integer.parseInt(time_signature);
        this.track_id = track_id;
        this.segments_start = segments_start;
        this.segments_pitches = segments_pitches;
        this.segments_timbre = segments_timbre;
        this.segments_loudness_max = segments_loudness_max;
        this.segments_loudness_max_time = segments_loudness_max_time;
        this.segments_loudness_start = segments_loudness_start;
    }

    @Override
    public void write(DataOutput out) throws IOException {
        out.writeUTF(song_id);
        out.writeFloat(song_hotttnesss);
        out.writeFloat(danceability);
        out.writeFloat(duration);
        out.writeFloat(end_of_fade_in);
        out.writeFloat(energy);
        out.writeInt(key);
        out.writeFloat(loudness);
        out.writeInt(mode);
        out.writeFloat(start_of_fade_out);
        out.writeFloat(tempo);
        out.writeInt(time_signature);
        out.writeUTF(track_id);
        out.writeUTF(segments_start);
        out.writeUTF(segments_pitches);
        out.writeUTF(segments_timbre);
        out.writeUTF(segments_loudness_max);
        out.writeUTF(segments_loudness_max_time);
        out.writeUTF(segments_loudness_start);

    }

    @Override
    public void readFields(DataInput in) throws IOException {
        song_id = in.readUTF();
        song_hotttnesss = in.readFloat();
        danceability = in.readFloat();
        duration = in.readFloat();
        end_of_fade_in = in.readFloat();
        energy = in.readFloat();
        key = in.readInt();
        loudness = in.readFloat();
        mode = in.readInt();
        start_of_fade_out = in.readFloat();
        tempo = in.readFloat();
        time_signature = in.readInt();
        track_id = in.readUTF();
        segments_start = in.readUTF();
        segments_pitches = in.readUTF();
        segments_timbre = in.readUTF();
        segments_loudness_max = in.readUTF();
        segments_loudness_max_time = in.readUTF();
        segments_loudness_start = in.readUTF();
    }

    public String getSong_id() {
        return song_id;
    }

    public float getSong_hotttnesss() {
        return song_hotttnesss;
    }

    public float getDanceability() {
        return danceability;
    }

    public float getDuration() {
        return duration;
    }

    public float getEnd_of_fade_in() {
        return end_of_fade_in;
    }

    public float getEnergy() {
        return energy;
    }

    public int getKey() {
        return key;
    }

    public float getLoudness() {
        return loudness;
    }

    public int getMode() {
        return mode;
    }

    public float getStart_of_fade_out() {
        return start_of_fade_out;
    }

    public float getTempo() {
        return tempo;
    }

    public int getTime_signature() {
        return time_signature;
    }

    public String getTrack_id() {
        return track_id;
    }

    public String getSegments_start() {
        return segments_start;
    }

    public String getSegments_pitches() {
        return segments_pitches;
    }

    public String getSegments_timbre() {
        return segments_timbre;
    }

    public String getSegments_loudness_max() {
        return segments_loudness_max;
    }

    public String getSegments_loudness_max_time() {
        return segments_loudness_max_time;
    }

    public String getSegments_loudness_start() {
        return segments_loudness_start;
    }

    @Override
    public String getType() {
        return "analysis";
    }
}
