package cs455.hadoop.analyzesongs;

import org.apache.hadoop.io.Writable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class MetadataWritable extends DataWritable implements Writable{

    private float artist_familiarity;
    private float artist_hotttnesss;
    private String artist_id;
    private String artist_name;
    // location, lat, long
    private String song_id;
    private String song_title;
    private String similar_artists;
    private String artist_terms;
    private String artist_terms_freq;
    private String artist_terms_weight;
    private int year;

    public MetadataWritable(String artist_familiarity, String artist_hotttnesss, String artist_id, String artist_name,
                            String song_id, String song_title, String similar_artists, String artist_terms,
                            String artist_terms_freq, String artist_terms_weight, String year) {
        this.artist_familiarity = Float.parseFloat(artist_familiarity);
        this.artist_hotttnesss = Float.parseFloat(artist_hotttnesss);
        this.artist_id = artist_id;
        this.artist_name = artist_name;
        this.song_id = song_id;
        this.song_title = song_title;
        this.similar_artists = similar_artists;
        this.artist_terms = artist_terms;
        this.artist_terms_freq = artist_terms_freq;
        this.artist_terms_weight = artist_terms_weight;
        this.year = Integer.parseInt(year);
    }

    @Override
    public void write(DataOutput out) throws IOException {
        out.writeFloat(artist_familiarity);
        out.writeFloat(artist_hotttnesss);
        out.writeUTF(artist_id);
        out.writeUTF(artist_name);
        out.writeUTF(song_id);
        out.writeUTF(song_title);
        out.writeUTF(similar_artists);
        out.writeUTF(artist_terms);
        out.writeUTF(artist_terms_freq);
        out.writeUTF(artist_terms_weight);
        out.writeInt(year);
    }

    @Override
    public void readFields(DataInput in) throws IOException {
        artist_familiarity = in.readInt();
        artist_hotttnesss = in.readInt();
        artist_id = in.readUTF();
        artist_name = in.readUTF();
        song_id = in.readUTF();
        song_title = in.readUTF();
        similar_artists = in.readUTF();
        artist_terms = in.readUTF();
        artist_terms_freq = in.readUTF();
        artist_terms_weight = in.readUTF();
        year = in.readInt();
    }

    public float getArtist_familiarity() {
        return artist_familiarity;
    }

    public float getArtist_hotttnesss() {
        return artist_hotttnesss;
    }

    public String getArtist_id() {
        return artist_id;
    }

    public String getArtist_name() {
        return artist_name;
    }

    public String getSong_id() {
        return song_id;
    }

    public String getSong_title() {
        return song_title;
    }

    public String getSimilar_artists() {
        return similar_artists;
    }

    public String getArtist_terms() {
        return artist_terms;
    }

    public String getArtist_terms_freq() {
        return artist_terms_freq;
    }

    public String getArtist_terms_weight() {
        return artist_terms_weight;
    }

    public int getYear() {
        return year;
    }

    @Override
    public String getType() {
        return "metadata";
    }
}
