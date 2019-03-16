package com.news.mapreonre;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.StringTokenizer;

/**
 * @Author: Clb
 * @Date: 2019/3/15 21:06
 * @Description:
 */
public class WordCount {
    /**Mapper<Object,Text,Text,IntWritable>
     * Object           输入的key类型  下标 本行开始的位置
     * Text             输入的value类型 本行内容
     * Text             输出的key类型 分割后的词
     * IntWritable      输出的value类型 数量
     */
    public static class MyMapper extends Mapper<Object,Text,Text,IntWritable>{
        private final static IntWritable one=new IntWritable(1);
        private Text word=new Text();

        /**
         * map(Object key,Text value,Context context)
         * @param key 输入的key
         * @param value 输入的value
         * @param context 全局环境变量
         * @throws IOException
         * @throws InterruptedException
         */
        @Override
        public void map(Object key,Text value,Context context)throws IOException,InterruptedException{
            /**
             * 分词器 以制表符,空格,换行符分割
             */
            StringTokenizer itr=new StringTokenizer(value.toString());
            while (itr.hasMoreTokens()){
                word.set(itr.nextToken());
                /**
                 * 保存到环境变量
                 * word是key,one是value
                 */
                context.write(word,one);
            }
        }
    }

    /**
     * Reducer<Text,IntWritable,Text,IntWritable>
     *     Text         key类型
     *     IntWritable  值列表 111
     *     Text         计算和后 key类型
     *     IntWritable  计算和后 值列表 111
     */
    public static class MyReducer extends Reducer<Text,IntWritable,Text,IntWritable>{
        private IntWritable result=new IntWritable();

        /**
         * reduce(Text key,Iterable<IntWritable>values,Context context)
         * @param key key类型
         * @param values 值列表 111
         * @param context 环境
         * @throws IOException
         * @throws InterruptedException
         */
        @Override
        public void reduce(Text key,Iterable<IntWritable>values,Context context)throws IOException,InterruptedException{
            int sum=0;
            for (IntWritable val:values){
                sum+=val.get();
            }
            result.set(sum);
            context.write(key,result);
        }
    }

    public static void main(String[] args) throws URISyntaxException, IOException, ClassNotFoundException, InterruptedException {
//        String INPUT_PATH="hdfs://hadoop000:8020/wc";
        String INPUT_PATH=args[1];
//        String OUTPUT_PATH="hdfs://hadoop000:8020/outputwc";
        String OUTPUT_PATH=args[2];

        Configuration conf=new Configuration();
//        final FileSystem fileSystem=FileSystem.get(new URI(INPUT_PATH),conf);
//        if (fileSystem.exists(new Path(OUTPUT_PATH))){
//            fileSystem.delete(new Path(OUTPUT_PATH));
//        }
        //名称 随便
        Job job=Job.getInstance(conf,"WordCount");
//        运行jar类 主类
        job.setJarByClass(WordCount.class);
//        设置map
        job.setMapperClass(MyMapper.class);
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(IntWritable.class);
//        设置Reduce
        job.setReducerClass(MyReducer.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);
//        设置输入格式
        job.setInputFormatClass(TextInputFormat.class);
        Path inputPath=new Path(INPUT_PATH);
        FileInputFormat.addInputPath(job,inputPath);
//        设置输出格式
        job.setOutputFormatClass(TextOutputFormat.class);
        Path outputPath=new Path(OUTPUT_PATH);
        FileOutputFormat.setOutputPath(job,outputPath);
        //1非正常退出
        System.exit(job.waitForCompletion(true)?0:1);
    }
}
