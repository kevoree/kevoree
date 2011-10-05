/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kevoree.library.hadoop;

import java.io.IOException;
import java.util.StringTokenizer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;

import org.kevoree.ComponentInstance;
import org.kevoree.ContainerNode;
import org.kevoree.annotation.ComponentType;
import org.kevoree.annotation.DictionaryAttribute;
import org.kevoree.annotation.DictionaryType;
import org.kevoree.annotation.Library;
import org.kevoree.annotation.Port;
import org.kevoree.annotation.ProvidedPort;
import org.kevoree.annotation.Provides;
import org.kevoree.annotation.PortType;
import org.kevoree.annotation.Start;
import org.kevoree.annotation.Stop;
import org.kevoree.framework.Constants;
import org.kevoree.framework.KevoreePlatformHelper;

@Library(name = "Hadoop")
@ComponentType
@Provides({
    @ProvidedPort(name = "submit", type = PortType.MESSAGE)
})
@DictionaryType({
    @DictionaryAttribute(name = "inputDir", optional = false),
    @DictionaryAttribute(name = "outputDir", optional = false),
    @DictionaryAttribute(name = "jobTrackerName", optional = false)
})
public class WordCount extends HadoopComponent {
    
    /**
     * @TODO: Initialize these attributes 
     */
    private static final String JOB_TRACKER_NAME = "";
    private String input;
    private String output;
    private String jobTrackerNodeName;

    public static class TokenizerMapper
            extends Mapper<Object, Text, Text, IntWritable> {

        private final static IntWritable one = new IntWritable(1);
        private Text word = new Text();

        @Override
        public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
            StringTokenizer itr = new StringTokenizer(value.toString());
            while (itr.hasMoreTokens()) {
                word.set(itr.nextToken());
                context.write(word, one);
            }
        }
    }

    public static class IntSumReducer
            extends Reducer<Text, IntWritable, Text, IntWritable> {

        private IntWritable result = new IntWritable();

        @Override
        public void reduce(Text key, Iterable<IntWritable> values,
                Context context) throws IOException, InterruptedException {
            int sum = 0;
            for (IntWritable val : values) {
                sum += val.get();
            }
            result.set(sum);
            context.write(key, result);
        }
    }

    /**
     * @TODO: initialize configuration, especially TaskTracker
     */
    @Start
    public void start() throws IOException, InterruptedException, ClassNotFoundException {
        input = (String) this.getDictionary().get("inputDir");
        output = (String) this.getDictionary().get("outputDir");
        jobTrackerNodeName = (String) this.getDictionary().get("jobTrackerName");

        
        /*
         * @FIXME : use while instead of foreach
         */
        for (ContainerNode each : this.getModelService().getLastModel().getNodes()) {
            for (ComponentInstance ci : each.getComponents()) {
                if (JOB_TRACKER_NAME.equals(ci.getName())) {
                    jobTrackerNodeName = each.getName();
                    break;
                }
            }
        }

        // retrieve NameNode IP address
        String ip = KevoreePlatformHelper.getProperty(this.getModelService().getLastModel(),
                jobTrackerNodeName, Constants.KEVOREE_PLATFORM_REMOTE_NODE_IP());
        
        
        Configuration configuration = this.getConfiguration();
        configuration.set("hadoop.jobtracker", ip);

        this.submit(null);
    }
    
    
    @Stop
    public void stop() {
        
    }
    
    
    @Port(name = "submit")
    public void submit(Object arg) throws IOException, InterruptedException, 
            ClassNotFoundException {
        
        
        
        Job job = new Job(this.getConfiguration(), "word count");
        job.setJarByClass(WordCount.class);
        job.setMapperClass(TokenizerMapper.class);
        job.setCombinerClass(IntSumReducer.class);
        job.setReducerClass(IntSumReducer.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);
        FileInputFormat.addInputPath(job, new Path(input));
        FileOutputFormat.setOutputPath(job, new Path(output));
        
        boolean result = job.waitForCompletion(true); 
        System.exit(result ? 0 : 1);
    }
    
    
    
    public static void main(String[] args) throws Exception {
        Configuration conf = new Configuration();
        String[] otherArgs = new GenericOptionsParser(conf, args).getRemainingArgs();
        if (otherArgs.length != 2) {
            System.err.println("Usage: wordcount <in> <out>");
            System.exit(2);
        }
        
        
        Job job = new Job(conf, "word count");
        job.setJarByClass(WordCount.class);
        job.setMapperClass(TokenizerMapper.class);
        job.setCombinerClass(IntSumReducer.class);
        job.setReducerClass(IntSumReducer.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);
        FileInputFormat.addInputPath(job, new Path(otherArgs[0]));
        FileOutputFormat.setOutputPath(job, new Path(otherArgs[1]));
        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}
