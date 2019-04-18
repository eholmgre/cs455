package cs455.hadoop.analyzesongs;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import java.io.IOException;
import java.util.Date;


public class AnalyzeSegmentsJob {

    public static void main(String[] args) {
        try {
            Configuration conf = new Configuration();
            Job job = Job.getInstance(conf, "7");
            job.setJarByClass(AnalyzeSegmentsJob.class);
            job.setMapperClass(MapSegments.class);
            job.setCombinerClass(CombineSegments.class);
            job.setReducerClass(ReduceSegments.class);
            job.setNumReduceTasks(6);
            job.setMapOutputKeyClass(Text.class);
            job.setMapOutputValueClass(Text.class);
            job.setOutputKeyClass(Text.class);
            job.setOutputValueClass(Text.class);
            FileInputFormat.addInputPath(job, new Path("/home/data/analysis"));
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
