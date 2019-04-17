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


/**
 * This is the main class. Hadoop will invoke the main method of this class.
 */

public class AnalyzeSongsJob {

     private static final boolean remote = true;
    //private static final boolean remote = false;

    public static void main(String[] args) {
        try {
            Configuration conf = new Configuration();
            // Give the MapRed job a name. You'll see this name in the Yarn webapp.
            Job job = Job.getInstance(conf, "its my job i do *what i wanna");
            // Current class.
            job.setJarByClass(AnalyzeSongsJob.class);
            // Mapper
            //job.setMapperClass(AnalyzeSongsMapper.class);
            // Combiner. We use the reducer as the combiner in this case.
            //job.setCombinerClass(AnalyzeSongsReducer.class);
            // Reducer
            job.setReducerClass(AnalyzeSongsReducer.class);
            job.setNumReduceTasks(1);
            // Outputs from the Mapper.
            job.setMapOutputKeyClass(Text.class);
            job.setMapOutputValueClass(Text.class);
            // Outputs from Reducer. It is sufficient to set only the following two properties
            // if the Mapper and Reducer has same key and value types. It is set separately for
            // elaboration.
            job.setOutputKeyClass(Text.class);
            job.setOutputValueClass(Text.class);
            // path to input in HDFS
            if (remote) {
                MultipleInputs.addInputPath(job, new Path("/home/data/analysis-reduced"), TextInputFormat.class, MapAnalysis.class);
                MultipleInputs.addInputPath(job, new Path("/home/data/metadata"), TextInputFormat.class, MapMetadata.class);

            } else {
                MultipleInputs.addInputPath(job, new Path("/data/analysis"), TextInputFormat.class, MapAnalysis.class);
                MultipleInputs.addInputPath(job, new Path("/data/metadata"), TextInputFormat.class, MapMetadata.class);
            }
            //FileInputFormat.addInputPath(job, new Path(args[0]));
            // path to output in HDFS
            //FileOutputFormat.setOutputPath(job, new Path(args[1]));
            Date now = new Date();
            if (remote) {
                FileOutputFormat.setOutputPath(job, new Path("/home/output" + now.getTime()));
            } else {
                FileOutputFormat.setOutputPath(job, new Path("/output" + now.getTime()));
            }
            // Block until the job is completed.
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
