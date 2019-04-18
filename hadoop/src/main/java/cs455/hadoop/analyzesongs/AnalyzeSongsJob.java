package cs455.hadoop.analyzesongs;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.MultipleInputs;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import java.io.IOException;
import java.util.Date;


public class AnalyzeSongsJob {

    public static void main(String[] args) {
        try {
            Configuration conf = new Configuration();
            Job job = Job.getInstance(conf, "1-6, 8-10");
            job.setJarByClass(AnalyzeSongsJob.class);
            job.setReducerClass(AnalyzeSongsReducer.class);
            job.setNumReduceTasks(10);
            job.setMapOutputKeyClass(Text.class);
            job.setMapOutputValueClass(Text.class);
            job.setOutputKeyClass(Text.class);
            job.setOutputValueClass(Text.class);
            MultipleInputs.addInputPath(job, new Path("/data/analysis"), TextInputFormat.class, MapAnalysis.class);
            MultipleInputs.addInputPath(job, new Path("/data/metadata"), TextInputFormat.class, MapMetadata.class);
            Date now = new Date();
            FileOutputFormat.setOutputPath(job, new Path("/home/output" + now.getTime()));
            System.exit(job.waitForCompletion(true) ? 0 : 1);

        } catch (IOException e) {
            System.err.println(e.getMessage());
        } catch (InterruptedException e) {
            System.err.println(e.getMessage());
        } catch (ClassNotFoundException e) {
            System.err.println(e.getMessage());
        }

    }
}
